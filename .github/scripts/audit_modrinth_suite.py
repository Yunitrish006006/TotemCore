"""Audit or apply reviewed metadata for the other active Totem projects."""

import hashlib
import json
import os
from pathlib import Path
import sys
import time

from repair_modrinth_metadata import ApiError, api as request_api, project_path


_last_request = 0.0


def api(*args, **kwargs):
    """Pace the historical-version audit and retry rate limits without logging credentials."""
    global _last_request
    for attempt in range(3):
        time.sleep(max(0.0, 0.3 - (time.monotonic() - _last_request)))
        _last_request = time.monotonic()
        try:
            return request_api(*args, **kwargs)
        except ApiError as error:
            if "HTTP 429" not in str(error) or attempt == 2:
                raise
            time.sleep(30)


MODULES = (
    "TotemAlchemy", "TotemAutomata", "TotemDiscordBridge", "TotemEnchanting",
    "TotemExcavation", "TotemLocksmith", "TotemNexus", "TotemRemnant",
    "TotemVanillaTweaks", "TotemVillagers",
)
SOURCES = {"https://github.com/Yunitrish006006/" + name: name for name in MODULES}
MANIFEST_ROOT = Path(".github/modrinth")


def dependencies_equal(left, right):
    return sorted(json.dumps(item, sort_keys=True) for item in left) == sorted(json.dumps(item, sort_keys=True) for item in right)


def disclosure_matches(actual, desired):
    return all(set(actual.get(key, [])) == set(value) if key == "uses" else actual.get(key) == value
               for key, value in desired.items())


def get_disclosures(path):
    response = api("v3", path + "/disclosures")
    if not isinstance(response, dict) or not isinstance(response.get("disclosures"), list):
        raise RuntimeError("Unexpected disclosure response")
    return response["disclosures"]


def prepare(record):
    module = record["module"]
    source = record["source_url"]
    if SOURCES.get(source) != module or not record.get("id"):
        raise RuntimeError("Manifest identity is not in the suite allowlist")
    path = project_path(record["id"])
    project = api("v2", path)
    if project.get("id") != record["id"] or project.get("source_url") != source:
        raise RuntimeError("Project identity does not match manifest")
    expected = record.get("expected")
    if not isinstance(expected, dict) or not {"title", "slug"}.issubset(expected):
        raise RuntimeError("Manifest requires expected title and slug")
    for key, value in expected.items():
        actual = hashlib.sha256(project.get("body", "").encode()).hexdigest() if key == "body_sha256" else project.get(key)
        if actual != value:
            raise RuntimeError("Project metadata changed since the audit")
    changes = record.get("changes", {})
    if set(changes) - {"description", "body_file", "issues_url"}:
        raise RuntimeError("Unsupported project metadata change")
    desired = {}
    for key, value in changes.items():
        if key == "body_file":
            candidate = (MANIFEST_ROOT / value).resolve()
            if not candidate.is_relative_to(MANIFEST_ROOT.resolve()):
                raise RuntimeError("Body file must be inside the manifest directory")
            desired["body"] = candidate.read_text(encoding="utf-8")
        else:
            desired[key] = value
    for key in desired:
        if key not in expected and not (key == "body" and "body_sha256" in expected):
            raise RuntimeError("Every changed field requires an expected value")
    before = get_disclosures(path)
    sets = list(record.get("disclosures", []))
    if any(not isinstance(item, dict) or not item.get("type") for item in sets):
        raise RuntimeError("Invalid disclosure record")
    ai = record.get("ai")
    if ai is not None:
        if any(item["type"] == "ai_content" for item in sets):
            raise RuntimeError("AI changes must use only the ai manifest field")
        matches = [item for item in before if item.get("type") == "ai_content" and not item.get("deleted_at")]
        if len(matches) > 1:
            raise RuntimeError("Duplicate AI disclosures")
        current = matches[0] if matches else {}
        uses = list(current.get("uses", []))
        if not isinstance(ai.get("uses"), list) or set(ai["uses"]) - {"code", "text", "assets", "functionality"}:
            raise RuntimeError("Invalid AI uses")
        for use in ai["uses"]:
            if use not in uses:
                uses.append(use)
        note = current.get("note") or ""
        addition = ai.get("note", "").strip()
        if addition and addition not in note:
            note = (note.rstrip() + "\n\n" + addition).lstrip()
        sets.append({"type": "ai_content", "uses": uses, "note": note})
    if len({item["type"] for item in sets}) != len(sets):
        raise RuntimeError("Duplicate requested disclosure types")
    versions = []
    for version in record.get("versions", []):
        current = api("v2", "version/" + version["id"])
        if current.get("project_id") != project["id"] or current.get("id") != version["id"]:
            raise RuntimeError("Version does not belong to manifest project")
        if not dependencies_equal(current.get("dependencies", []), version["expected_dependencies"]):
            raise RuntimeError("Version dependencies changed since audit")
        if not isinstance(version["dependencies"], list):
            raise RuntimeError("Invalid version dependencies")
        versions.append({"before": current, "dependencies": version["dependencies"]})
    if len({version["before"]["id"] for version in versions}) != len(versions):
        raise RuntimeError("Duplicate manifest versions")
    return {"module": module, "path": path, "before": project,
            "before_disclosures": before, "desired": desired, "sets": sets,
            "versions": versions, "guard_fields": sorted(set(expected) - {"body_sha256"} | {"body", "id", "source_url", "status"})}


def apply_manifest(report):
    manifest = json.loads((MANIFEST_ROOT / "suite-manifest.json").read_text(encoding="utf-8"))
    records = manifest["projects"]
    names = [record["module"] for record in records]
    if not records or len(set(names)) != len(names):
        raise RuntimeError("Manifest must contain unique projects")
    requested = {name.strip() for name in os.environ.get("SUITE_MODULES", "").split(",") if name.strip()}
    if requested - set(names):
        raise RuntimeError("Requested module is absent from manifest")
    selected = [record for record in records if not requested or record["module"] in requested]
    prepared = [prepare(record) for record in selected]
    report["projects"] = prepared
    report["mode"] = "apply"
    for item in prepared:
        path = item["path"]
        # Recheck immediately before each write, in addition to whole-batch preflight.
        current = api("v2", path)
        if any(current.get(key) != item["before"].get(key) for key in item["guard_fields"]) or get_disclosures(path) != item["before_disclosures"]:
            raise RuntimeError("Project changed after preflight")
        patch = {key: value for key, value in item["desired"].items() if item["before"].get(key) != value}
        if patch:
            api("v2", path, "PATCH", patch)
        if item["sets"]:
            api("v3", path + "/disclosures", "PATCH", {"set": item["sets"], "remove": []})
        # Modrinth reads may briefly lag a successful write. Retry reads only.
        for attempt in range(6):
            after = api("v2", path)
            disclosures = get_disclosures(path)
            metadata_ready = all(after.get(key) == value for key, value in item["desired"].items())
            disclosures_ready = all(any(entry.get("type") == desired["type"]
                                        and not entry.get("deleted_at")
                                        and disclosure_matches(entry, desired)
                                        for entry in disclosures) for desired in item["sets"])
            if metadata_ready and disclosures_ready:
                break
            if attempt < 5:
                time.sleep(1)
        item["after"] = after
        item["after_disclosures"] = disclosures
        for key in ("id", "slug", "title", "source_url", "status", "icon_url", "gallery", "versions", "license", "client_side", "server_side"):
            if after.get(key) != item["before"].get(key):
                raise RuntimeError("Unrequested project field changed")
        if any(after.get(key) != value for key, value in item["desired"].items()):
            raise RuntimeError("Requested project metadata verification failed")
        types = {entry["type"] for entry in item["sets"]}
        if any(entry not in disclosures for entry in item["before_disclosures"] if entry.get("type") not in types):
            raise RuntimeError("An unrelated disclosure changed")
        for desired in item["sets"]:
            matches = [entry for entry in disclosures if entry.get("type") == desired["type"] and not entry.get("deleted_at")]
            if len(matches) != 1 or not disclosure_matches(matches[0], desired):
                raise RuntimeError("Disclosure verification failed")
        for version in item["versions"]:
            version_path = "version/" + version["before"]["id"]
            current = api("v2", version_path)
            if current.get("project_id") != item["before"]["id"] or not dependencies_equal(current.get("dependencies", []), version["before"]["dependencies"]):
                raise RuntimeError("Version changed after preflight")
            if not dependencies_equal(current["dependencies"], version["dependencies"]):
                api("v2", version_path, "PATCH", {"dependencies": version["dependencies"]})
            version["after"] = api("v2", version_path)
            if not dependencies_equal(version["after"].get("dependencies", []), version["dependencies"]):
                raise RuntimeError("Version dependency verification failed")
            for key in ("id", "project_id", "files", "status", "version_number", "game_versions", "loaders"):
                if version["after"].get(key) != version["before"].get(key):
                    raise RuntimeError("Unrequested version metadata changed")
        item["verified"] = True
        print(json.dumps({"module": item["module"], "metadata": "verified", "status": after.get("status")}), flush=True)


def inspect():
    user = api("v2", "user")
    projects = api("v2", "user/" + user["id"] + "/projects")
    if not isinstance(projects, list):
        raise RuntimeError("Unexpected account projects response")
    report = {"schema_version": 1, "projects": [], "missing_modules": [],
              "unmatched_totem_projects": [], "errors": []}
    found = set()
    for listed in projects:
        source = listed.get("source_url")
        module = SOURCES.get(source)
        if module is None:
            if "totem" in (listed.get("title", "") + listed.get("slug", "")).lower():
                report["unmatched_totem_projects"].append({
                    key: listed.get(key) for key in ("id", "title", "slug", "source_url", "status")
                })
            continue
        found.add(module)
        entry = {"module": module, "project": listed}
        report["projects"].append(entry)
        try:
            path = project_path(listed["id"])
            project = api("v2", path)
            if project.get("id") != listed["id"] or project.get("source_url") != source:
                raise RuntimeError("Project identity changed during inspection")
            entry["project"] = project
            entry["disclosures"] = api("v3", path + "/disclosures")
            versions = api("v2", path + "/version")
            if not isinstance(versions, list):
                raise RuntimeError("Unexpected versions response")
            entry["latest_version"] = versions[0] if versions else None
            entry["all_versions"] = [{key: version.get(key) for key in
                                      ("id", "project_id", "version_number", "date_published", "dependencies", "files", "game_versions", "loaders")}
                                     for version in versions]
            print(json.dumps({"module": module, "id": project["id"],
                              "title": project.get("title"), "slug": project.get("slug"),
                              "status": project.get("status"),
                              "latest_version": versions[0].get("version_number") if versions else None}), flush=True)
        except (ApiError, RuntimeError, KeyError, TypeError, ValueError):
            entry["inspection_error"] = "Project inspection incomplete; inspect API access and metadata"
            report["errors"].append(module)
            print(json.dumps({"module": module, "inspection": "failed"}), flush=True)
    report["missing_modules"] = sorted(set(MODULES) - found)
    core = [p for p in projects if p.get("source_url") == "https://github.com/Yunitrish006006/TotemCore"]
    if len(core) == 1:
        report["core_versions"] = [{key: version.get(key) for key in ("id", "version_number", "project_id")}
                                   for version in api("v2", project_path(core[0]["id"]) + "/version")]
    return report


def main():
    mode = os.environ.get("SUITE_MODE", "inspect")
    if mode not in ("inspect", "apply"):
        raise RuntimeError("Unsupported suite mode")
    output = Path(os.environ["RUNNER_TEMP"]) / "modrinth-suite-audit.json"
    report = {"schema_version": 1, "mode": mode, "projects": []}
    try:
        if mode == "apply":
            apply_manifest(report)
        else:
            report = inspect()
    finally:
        encoded = json.dumps(report, ensure_ascii=False, indent=2)
        token = os.environ.get("MODRINTH_TOKEN", "").strip()
        if token:
            encoded = encoded.replace(token, "[REDACTED]")
        output.write_text(encoded + "\n", encoding="utf-8")
    if mode == "apply":
        return
    print(json.dumps({"missing_modules": report["missing_modules"],
                      "unmatched_totem_projects": report["unmatched_totem_projects"],
                      "failed_modules": report["errors"]}), flush=True)
    if report["errors"]:
        raise RuntimeError("One or more project inspections failed; partial audit saved")


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, KeyError, TypeError, ValueError, OSError) as error:
        # RuntimeError messages are controlled diagnostics; other exceptions may
        # embed response data, file contents, or environment values.
        detail = str(error) if isinstance(error, RuntimeError) else type(error).__name__
        token = os.environ.get("MODRINTH_TOKEN", "").strip()
        if token:
            detail = detail.replace(token, "[REDACTED]")
        print("Suite metadata operation failed: " + json.dumps(detail[:500]) +
              "; inspect the snapshot for completed operations.", file=sys.stderr)
        sys.exit(1)

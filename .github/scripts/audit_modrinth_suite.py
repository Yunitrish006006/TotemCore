"""Inspect the other active Totem projects without modifying Modrinth state."""

import json
import os
from pathlib import Path
import sys

from repair_modrinth_metadata import ApiError, api, project_path


MODULES = (
    "TotemAlchemy", "TotemAutomata", "TotemDiscordBridge", "TotemEnchanting",
    "TotemExcavation", "TotemLocksmith", "TotemNexus", "TotemRemnant",
    "TotemVanillaTweaks", "TotemVillagers",
)
SOURCES = {"https://github.com/Yunitrish006006/" + name: name for name in MODULES}


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
            versions = api("v2", path + "/version?limit=1")
            if not isinstance(versions, list):
                raise RuntimeError("Unexpected versions response")
            entry["latest_version"] = versions[0] if versions else None
            print(json.dumps({"module": module, "id": project["id"],
                              "title": project.get("title"), "slug": project.get("slug"),
                              "status": project.get("status"),
                              "latest_version": versions[0].get("version_number") if versions else None}), flush=True)
        except (ApiError, RuntimeError, KeyError, TypeError, ValueError):
            entry["inspection_error"] = "Project inspection incomplete; inspect API access and metadata"
            report["errors"].append(module)
            print(json.dumps({"module": module, "inspection": "failed"}), flush=True)
    report["missing_modules"] = sorted(set(MODULES) - found)
    return report


def main():
    if os.environ.get("SUITE_MODE", "inspect") != "inspect":
        raise RuntimeError("Only inspect mode is implemented; no remote changes were made")
    output = Path(os.environ["RUNNER_TEMP"]) / "modrinth-suite-audit.json"
    report = inspect()
    encoded = json.dumps(report, ensure_ascii=False, indent=2)
    token = os.environ.get("MODRINTH_TOKEN", "").strip()
    if token:
        encoded = encoded.replace(token, "[REDACTED]")
    output.write_text(encoded + "\n", encoding="utf-8")
    print(json.dumps({"missing_modules": report["missing_modules"],
                      "unmatched_totem_projects": report["unmatched_totem_projects"],
                      "failed_modules": report["errors"]}), flush=True)
    if report["errors"]:
        raise RuntimeError("One or more project inspections failed; partial audit saved")


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, KeyError, TypeError, ValueError, OSError):
        print("Suite audit failed; no remote changes were made.", file=sys.stderr)
        sys.exit(1)

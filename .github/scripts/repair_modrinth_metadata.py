"""Repair TotemCore project metadata using the publisher's existing credentials."""

import json
import os
from pathlib import Path
import re
import sys
import urllib.error
import urllib.parse
import urllib.request


SOURCE = "https://github.com/Yunitrish006006/TotemCore"
NOTE = "AI tools assisted with code and text during development."


class ApiError(RuntimeError):
    pass


def api(version, path, method="GET", payload=None):
    token = os.environ.get("MODRINTH_TOKEN", "").strip()
    if not token:
        raise RuntimeError("MODRINTH_TOKEN is missing or empty")
    request = urllib.request.Request(
        f"https://api.modrinth.com/{version}/{path}",
        data=None if payload is None else json.dumps(payload).encode(),
        headers={
            "Authorization": token,
            "User-Agent": "Yunitrish006006/totem-core-metadata-repair/1.0",
            "Content-Type": "application/json",
        },
        method=method,
    )
    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            body = response.read()
            return json.loads(body) if body else None
    except urllib.error.HTTPError as error:
        # Include only bounded API diagnostics, never request headers or credentials.
        detail = ""
        try:
            response_error = json.loads(error.read(4096))
            detail = str(response_error.get("description", response_error.get("error", "")))
            detail = detail.replace(token, "[redacted]")[:800]
        except (ValueError, AttributeError):
            pass
        raise ApiError(f"Modrinth {method} {version} request failed: HTTP {error.code} {detail}") from None
    except urllib.error.URLError:
        raise ApiError(f"Modrinth {method} {version} request failed: connection error") from None


def project_path(project_id):
    return "project/" + urllib.parse.quote(project_id, safe="")


def resolve_project():
    reference = os.environ.get("MODRINTH_PROJECT_ID", "").strip()
    if not reference:
        raise RuntimeError("MODRINTH_PROJECT_ID is missing or empty")
    for prefix in ("https://modrinth.com/project/", "https://modrinth.com/mod/"):
        if reference.startswith(prefix):
            reference = reference[len(prefix):]
            break
    try:
        project = api("v2", project_path(reference.rstrip("/")))
        if project.get("id"):
            return project
    except ApiError:
        pass
    markers = list(Path(".github/staging").glob("modrinth-published-*.json"))
    if not markers:
        raise RuntimeError("Project lookup failed and no publication marker exists")
    marker = max(markers, key=lambda p: tuple(int(n) for n in re.findall(r"\d+", p.name)))
    version_id = json.loads(marker.read_text())["modrinth_version_id"]
    version = api("v2", "version/" + urllib.parse.quote(version_id, safe=""))
    return api("v2", project_path(version["project_id"]))


def verify_identity(project):
    source = (project.get("source_url") or "").rstrip("/")
    if source.endswith(".git"):
        source = source[:-4]
    if source.lower() != SOURCE.lower():
        raise RuntimeError("Refusing repair: project source_url does not identify TotemCore")


def disclosures(path):
    result = api("v3", path + "/disclosures")
    items = result.get("disclosures") if isinstance(result, dict) else None
    if not isinstance(items, list):
        raise RuntimeError("Unexpected Modrinth disclosure response")
    return items


def ai_disclosure(existing, include_assets):
    matches = [item for item in existing if item.get("type") == "ai_content" and not item.get("deleted_at")]
    if len(matches) > 1:
        raise RuntimeError("Unexpected duplicate AI disclosures")
    current = matches[0] if matches else {}
    uses = list(current.get("uses", []))
    for use in ["code", "text"] + (["assets"] if include_assets else []):
        if use not in uses:
            uses.append(use)
    note = current.get("note") or ""
    additions = [NOTE, "AI assistance also includes translations and documentation."]
    if include_assets:
        additions.append("The icon uses hand-drawn base artwork with AI-assisted layout.")
    for addition in additions:
        if addition not in note:
            note = (note.rstrip() + "\n\n" + addition).lstrip()
    return {"type": "ai_content", "uses": uses, "note": note}


def report(project, items):
    summary = {key: project.get(key) for key in
               ("id", "slug", "title", "status", "issues_url", "source_url")}
    # Bounded output still shows the actual disclosures; do not dump the project body.
    summary["disclosures"] = items
    print(json.dumps(summary, ensure_ascii=True)[:16000], flush=True)


def main():
    mode = os.environ.get("REPAIR_MODE", "inspect")
    if mode not in ("inspect", "apply"):
        raise RuntimeError("REPAIR_MODE must be inspect or apply")
    submit = os.environ.get("SUBMIT_FOR_REVIEW", "false") == "true"
    if mode == "inspect" and submit:
        raise RuntimeError("Review submission requires apply mode")
    project = resolve_project()
    path = project_path(project["id"])
    before = disclosures(path)
    report(project, before)
    if mode == "inspect":
        return
    verify_identity(project)
    desired_slug = os.environ.get("PROJECT_SLUG", "").strip() or project["slug"]
    desired_title = os.environ.get("PROJECT_TITLE", "").strip() or project["title"]
    desired = ai_disclosure(before, os.environ.get("AI_ASSETS", "false") == "true")
    metadata = {}
    for key, value in {"slug": desired_slug, "title": desired_title, "issues_url": SOURCE + "/issues"}.items():
        if project.get(key) != value:
            metadata[key] = value
    if metadata:
        api("v2", path, "PATCH", metadata)
    api("v3", path + "/disclosures", "PATCH", {"set": [desired], "remove": []})
    project = api("v2", path)
    after = disclosures(path)
    verify_identity(project)
    if project.get("slug") != desired_slug or project.get("title") != desired_title or project.get("issues_url") != SOURCE + "/issues":
        raise RuntimeError("Project metadata verification failed")
    actual_ai = [item for item in after if item.get("type") == "ai_content" and not item.get("deleted_at")]
    if len(actual_ai) != 1 or set(actual_ai[0].get("uses", [])) != set(desired["uses"]) or actual_ai[0].get("note") != desired["note"]:
        raise RuntimeError("AI disclosure verification failed")
    for item in before:
        if item.get("type") != "ai_content" and item not in after:
            raise RuntimeError("An existing non-AI disclosure was changed")
    report(project, after)
    if submit:
        if project.get("status") not in ("approved", "processing"):
            api("v2", path, "PATCH", {"status": "processing", "requested_status": "approved"})
        project = api("v2", path)
        if project.get("status") not in ("approved", "processing"):
            raise RuntimeError("Review submission verification failed")
        print(json.dumps({key: project.get(key) for key in ("id", "status", "requested_status")}))


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, KeyError, TypeError, ValueError) as error:
        print(f"Metadata repair failed: {error}", file=sys.stderr)
        sys.exit(1)

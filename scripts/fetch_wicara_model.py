#!/usr/bin/env python3
"""Download upstream Wicara GGUF at an immutable revision and verify its LFS digest."""
import hashlib
import json
import re
import sys
import urllib.request
from pathlib import Path

REPOSITORY = "num1notsvn/wicara-56m-chat-GGUF"
FILENAME = "wicara-56m-chat-q8_0.gguf"
MAX_BYTES = 100_000_000
LLAMA_CPP_COMMIT = "2d8a32502909ca065c41c4cf5f2fb1c427c047fc"
ASSET_DIR = Path("app/src/main/assets")
MODEL_DIR = ASSET_DIR / "models"
MODEL_PATH = MODEL_DIR / FILENAME
METADATA_PATH = ASSET_DIR / "wicara-model-metadata.json"


def request(url: str):
    return urllib.request.Request(url, headers={"User-Agent": "DynamicIslandUltra-CI/1.0"})


def fetch_json(url: str) -> dict:
    with urllib.request.urlopen(request(url), timeout=60) as response:
        return json.loads(response.read().decode("utf-8"))


def main() -> None:
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    info = fetch_json(f"https://huggingface.co/api/models/{REPOSITORY}?blobs=true")
    revision = str(info.get("sha", ""))
    if not re.fullmatch(r"[0-9a-f]{40}", revision):
        raise RuntimeError("Hugging Face did not return an immutable 40-character model revision.")

    card = info.get("cardData") or {}
    license_id = str(card.get("license") or "").strip().lower()
    tags = [str(tag).lower() for tag in info.get("tags", [])]
    normalized_license = "".join(ch for ch in license_id if ch.isalnum())
    metadata_license_ok = normalized_license in (
        "apache20", "apachelicense20", "apachelicenseversion20"
    )
    license_tag_ok = any(
        tag.replace("_", "-").replace(" ", "") in ("license:apache-2.0", "license:apache2.0")
        for tag in tags
    )
    print(f"Upstream license metadata: cardData={license_id!r}; license tags={[t for t in tags if t.startswith('license:')]}")
    if not metadata_license_ok and not license_tag_ok:
        with urllib.request.urlopen(
            request(f"https://huggingface.co/{REPOSITORY}/raw/{revision}/README.md"),
            timeout=60
        ) as response:
            model_card = response.read().decode("utf-8")
        declared = False
        for line in model_card.lower().splitlines():
            compact = "".join(ch for ch in line if ch.isalnum() or ch in ".-:")
            if ("apache-2.0" in compact or "apache2.0" in compact or
                    ("license" in compact and "apache" in compact and "2.0" in compact)):
                declared = True
                break
        if not declared:
            raise RuntimeError("Upstream model card does not explicitly declare Apache-2.0.")

    matches = [item for item in info.get("siblings", []) if item.get("rfilename") == FILENAME]
    if len(matches) != 1:
        raise RuntimeError("Exact requested Wicara Q8_0 artifact is missing or ambiguous.")
    lfs = matches[0].get("lfs") or {}
    declared_size = int(matches[0].get("size") or lfs.get("size") or 0)

    # Hub Xet-backed files may not expose legacy lfs.oid in the repo-info response.
    # The Hub's HEAD metadata endpoint exposes the content SHA-256 as X-Linked-Etag.
    class NoRedirect(urllib.request.HTTPRedirectHandler):
        def redirect_request(self, req, fp, code, msg, headers, newurl):
            return None

    metadata_url = f"https://huggingface.co/{REPOSITORY}/resolve/{revision}/{FILENAME}"
    metadata_request = urllib.request.Request(
        metadata_url,
        headers={"User-Agent": "DynamicIslandUltra-CI/1.0"},
        method="HEAD"
    )
    metadata_opener = urllib.request.build_opener(NoRedirect)
    try:
        metadata_response = metadata_opener.open(metadata_request, timeout=60)
    except urllib.error.HTTPError as response:
        if response.code not in (301, 302, 303, 307, 308):
            raise
        metadata_response = response

    linked_etag = (
        metadata_response.headers.get("X-Linked-Etag")
        or metadata_response.headers.get("ETag")
        or ""
    ).strip()
    expected_sha = linked_etag.removeprefix("W/").strip('"').lower()
    if not re.fullmatch(r"[0-9a-f]{64}", expected_sha):
        raise RuntimeError(
            "Hugging Face HEAD metadata did not provide a 64-character file SHA-256 "
            "in X-Linked-Etag/ETag; refusing to substitute the Xet file ID."
        )

    if declared_size <= 0:
        declared_size = int(
            metadata_response.headers.get("X-Linked-Size")
            or metadata_response.headers.get("Content-Length")
            or 0
        )
    if not 0 < declared_size <= MAX_BYTES:
        raise RuntimeError(
            f"Upstream model size {declared_size} is missing or exceeds {MAX_BYTES} bytes."
        )
    print(
        f"Verified upstream file metadata: size={declared_size} bytes; "
        f"SHA-256={expected_sha}; revision={revision}"
    )

    url = f"https://huggingface.co/{REPOSITORY}/resolve/{revision}/{FILENAME}?download=true"
    temporary = MODEL_PATH.with_suffix(".gguf.part")
    digest = hashlib.sha256()
    actual_size = 0
    try:
        with urllib.request.urlopen(request(url), timeout=180) as response, temporary.open("wb") as output:
            while True:
                chunk = response.read(1024 * 1024)
                if not chunk:
                    break
                actual_size += len(chunk)
                if actual_size > MAX_BYTES:
                    raise RuntimeError("Downloaded model exceeds the 100,000,000-byte limit.")
                digest.update(chunk)
                output.write(chunk)
        actual_sha = digest.hexdigest()
        if actual_size != declared_size:
            raise RuntimeError(f"Model byte-size mismatch: upstream={declared_size}, downloaded={actual_size}.")
        if actual_sha != expected_sha:
            raise RuntimeError("Downloaded model SHA-256 differs from upstream LFS metadata.")
        with temporary.open("rb") as model:
            if model.read(4) != b"GGUF":
                raise RuntimeError("Downloaded file does not begin with GGUF magic.")
        temporary.replace(MODEL_PATH)
    finally:
        temporary.unlink(missing_ok=True)

    metadata = {
        "repository": REPOSITORY,
        "revision": revision,
        "filename": FILENAME,
        "sizeBytes": actual_size,
        "sha256": actual_sha,
        "license": "Apache-2.0",
        "runtime": {
            "name": "llama.cpp",
            "revision": LLAMA_CPP_COMMIT,
            "license": "MIT"
        }
    }
    METADATA_PATH.write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    print(json.dumps(metadata, indent=2))
    print(f"Verified GGUF: {actual_size} bytes; SHA-256 {actual_sha}")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"::error::{exc}", file=sys.stderr)
        raise

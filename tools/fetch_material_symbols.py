#!/usr/bin/env python3
"""
Fetch selected Material Symbols (Rounded, Fill=0, 24px) SVGs from Google Fonts CDN.

Reads manifest at doc/图标/icons_manifest.json and downloads each icon trying
several known gstatic URL patterns used by Material Symbols. Falls back to the
older Material Icons Round/Outlined sets if a Symbols path is not found.

Outputs to doc/图标/<camelCase>.svg

Usage:
  python tools/fetch_material_symbols.py
"""
from __future__ import annotations

import json
import os
import sys
import time
from pathlib import Path
from typing import List

import urllib.request
import urllib.error

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "doc" / "图标"
MANIFEST = OUT_DIR / "icons_manifest.json"


def http_get(url: str) -> bytes | None:
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Cc_xiaoji-icon-fetch/1.0"})
        with urllib.request.urlopen(req, timeout=20) as resp:
            if resp.status == 200:
                return resp.read()
            return None
    except urllib.error.HTTPError as e:
        # 404 etc
        return None
    except Exception:
        return None


def try_fetch_symbol(slug: str) -> bytes | None:
    """Try a series of Material Symbols Rounded URL patterns for Fill=0, 24px."""
    base = "https://fonts.gstatic.com/s/i"
    candidates = [
        f"{base}/materialsymbolsrounded/{slug}/fill0wght400grad0opsz24/24px.svg",
        f"{base}/materialsymbolsrounded/{slug}/wght400grad0opsz24/24px.svg",
        f"{base}/materialsymbolsrounded/{slug}/fill0/24px.svg",
        f"{base}/materialsymbolsrounded/{slug}/default/24px.svg",
        f"{base}/short-term/release/materialsymbolsrounded/{slug}/fill0wght400grad0opsz24/24px.svg",
        f"{base}/short-term/release/materialsymbolsrounded/{slug}/default/24px.svg",
    ]
    for url in candidates:
        data = http_get(url)
        if data and data.strip().startswith(b"<svg"):
            return data
    return None


def try_fetch_icons_legacy(slug: str) -> bytes | None:
    """Fallback to legacy Material Icons (Round first, then Outlined)."""
    base = "https://fonts.gstatic.com/s/i"
    candidates = [
        f"{base}/materialiconsround/{slug}/v1/24px.svg",
        f"{base}/materialiconsoutlined/{slug}/v1/24px.svg",
        f"{base}/materialicons/{slug}/v1/24px.svg",
    ]
    for url in candidates:
        data = http_get(url)
        if data and data.strip().startswith(b"<svg"):
            return data
    return None


def to_camel_case(s: str) -> str:
    parts = s.split("_")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


def main() -> int:
    if not MANIFEST.exists():
        print(f"Manifest not found: {MANIFEST}", file=sys.stderr)
        return 2
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    items: List[dict] = manifest.get("icons", [])
    ok = 0
    miss = []
    for it in items:
        slug = it["slug"]
        fname = it["fileName"]
        dest = OUT_DIR / fname
        # Skip if already exists
        if dest.exists() and dest.stat().st_size > 0:
            print(f"skip  {fname} (exists)")
            ok += 1
            continue
        data = try_fetch_symbol(slug)
        if not data:
            data = try_fetch_icons_legacy(slug)
        if data:
            dest.write_bytes(data)
            print(f"saved {fname}")
            ok += 1
        else:
            print(f"MISS  {slug}")
            miss.append(slug)
        time.sleep(0.05)
    print("---- summary ----")
    print(f"ok={ok} total={len(items)} miss={len(miss)}")
    if miss:
        print("missed:", ", ".join(miss))
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())


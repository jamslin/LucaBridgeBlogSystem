#!/usr/bin/env python3
"""
Stage A2 — poster frames for events whose only media is video.

19 events have a write-up but zero photos: their media is all .mp4, which phase 1 does
not upload. Without this their cards publish with a blank cover. Rather than pushing
~1.5 GB of video into MinIO, pull ONE still per event (a few hundred KB) and use it as
the cover. The video itself stays skipped and stays flagged in the report.

Frames are written to <assets>/_posters/<folder>.jpg — one obvious place, safe to delete.
Idempotent: an existing poster is left alone unless --force.
"""
import argparse, json, subprocess, shutil
from pathlib import Path

VIDEO_EXT = {".mp4", ".mov", ".m4v", ".avi"}

# Folders that need a still even though they DO have photos. 20250921 is split into two posts
# by fixups.py; the second half has no media of its own and would otherwise publish coverless.
FORCE_POSTER = {"20250921"}


def duration(path: Path) -> float:
    try:
        out = subprocess.run(
            ["ffprobe", "-v", "error", "-show_entries", "format=duration",
             "-of", "default=nw=1:nk=1", str(path)],
            capture_output=True, text=True, timeout=30)
        return float(out.stdout.strip())
    except Exception:
        return 0.0


def grab(video: Path, dest: Path) -> bool:
    """Frame at ~15% in — far enough past black/fade-in openings to be a usable cover."""
    d = duration(video)
    ts = max(0.5, d * 0.15) if d else 1.0
    cmd = ["ffmpeg", "-y", "-loglevel", "error", "-ss", f"{ts:.2f}", "-i", str(video),
           "-frames:v", "1", "-vf", "scale='min(1600,iw)':-2", "-q:v", "3", str(dest)]
    subprocess.run(cmd, capture_output=True, timeout=120)
    return dest.exists() and dest.stat().st_size > 1024


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--assets", required=True)
    ap.add_argument("--events", default="posts.json")
    ap.add_argument("--force", action="store_true")
    args = ap.parse_args()

    if not shutil.which("ffmpeg"):
        raise SystemExit("ffmpeg not found — cannot build poster frames")

    assets = Path(args.assets)
    outdir = assets / "_posters"
    outdir.mkdir(exist_ok=True)
    events = json.loads(Path(args.events).read_text(encoding="utf-8"))

    made = skipped = failed = 0
    for e in events:
        forced = e["folder"] in FORCE_POSTER
        if (e["galleryImages"] and not forced) or not e["skippedVideos"]:
            continue                       # already has a real photo, or has no video either
        folder = assets / e["folder"]
        vids = sorted((p for p in folder.iterdir() if p.suffix.lower() in VIDEO_EXT),
                      key=lambda p: p.stat().st_size, reverse=True)
        if not vids:
            continue
        dest = outdir / f"{e['folder']}.jpg"
        if dest.exists() and not args.force:
            skipped += 1
        elif grab(vids[0], dest):
            made += 1
        else:
            failed += 1
            print(f"  FAILED {e['folder']}")
            continue
        e["posterFrame"] = f"_posters/{dest.name}"
        if forced:
            continue                    # keep the real photos as this event's cover
        e["coverImage"] = dest.name
        e["coverFrom"] = "VIDEO_POSTER"
        if "COVER_FROM_VIDEO_FRAME" not in e["flags"]:
            e["flags"].append("COVER_FROM_VIDEO_FRAME")
            e["notes"].append(f"no photos in this folder — cover is a still pulled from {vids[0].name}")

    Path(args.events).write_text(json.dumps(events, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"posters: {made} created, {skipped} already present, {failed} failed -> {outdir}")


if __name__ == "__main__":
    main()

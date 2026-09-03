"""Report writer — shared by extract.py and fixups.py so both emit an identical document."""
import json
from pathlib import Path


def write_report(events, assets, report_path, problems=None):
    problems = problems or []
    # ---- report -------------------------------------------------------------
    def having(flag):
        return [e for e in events if flag in e["flags"]]

    L = ["# LucaBridge content import — stage A report", ""]
    L += [f"Source: `{assets}`  ",
          f"Events found: **{len(events)}**  ",
          f"Images to upload: **{sum(len(e['galleryImages']) for e in events)}**  ",
          f"Videos skipped: **{sum(len(e['skippedVideos']) for e in events)}**  ",
          f"Covers taken from a video still: **{len(having('COVER_FROM_VIDEO_FRAME'))}**  ",
          f"Titles written by Claude: **{len(having('TITLE_GENERATED'))}**", ""]
    L += ["Every event is set to `PUBLISHED` with `publish_at` = the event date.", ""]

    L += ["## Posts where I wrote the title myself", ""]
    g = having("TITLE_GENERATED")
    L += ["_None._" if not g else ""] if not g else []
    for e in g:
        L.append(f"- `{e['folder']}` → **{e['slug']}** — {e['tcTitle'] or '(pending)'}")
    L.append("")

    L += ["## Posts with a title taken from the date/venue line (not a 【headline】)", ""]
    for e in having("TITLE_FROM_VENUE_LINE"):
        L.append(f"- `{e['folder']}` → {e['tcTitle']}")
    L.append("")

    L += ["## Posts whose cover is a still pulled from their video", "",
          "_These events had a write-up but no photos at all — their only media was video._", ""]
    for e in having("COVER_FROM_VIDEO_FRAME"):
        if e.get("coverFrom") == "VIDEO_POSTER":
            L.append(f"- `{e['folder']}` ({e['slug']}) — cover `{e.get('posterFrame','')}`")
        else:
            L.append(f"- `{e['folder']}` ({e['slug']}) — still available at "
                     f"`{e.get('posterFrame','')}`, but a real photo is used as the cover instead")
    L.append("")

    L += ["## Folders merged into another event", ""]
    for e in events:
        for m in e.get("mergedFrom", []):
            L.append(f"- `{m['folder']}` merged into `{e['folder']}` ({e['slug']}) — {m['reason']}")
    L.append("")

    L += ["## Posts with video that was NOT uploaded", ""]
    for e in having("VIDEO_SKIPPED"):
        L.append(f"- `{e['folder']}` ({e['slug']}) — {len(e['skippedVideos'])} file(s)")
        for n in e["notes"]:
            if n.startswith(("0","1","2","3","4","5","6","7","8","9")) and "video" in n:
                L.append(f"  - {n}")
    L.append("")

    L += ["## Other flags needing a look", ""]
    for e in events:
        other = [f for f in e["flags"] if f not in
                 ("VIDEO_SKIPPED", "TITLE_GENERATED", "TITLE_FROM_VENUE_LINE")]
        if other:
            L.append(f"- `{e['folder']}` — {', '.join(other)}")
            for n in e["notes"]:
                if "video" not in n:
                    L.append(f"  - {n}")
    L.append("")

    if problems:
        L += ["## Folders skipped entirely", ""] + [f"- {p}" for p in problems] + [""]

    L += ["## Full manifest", "",
          "| # | slug | date | title | imgs | vids | flags |",
          "|---|------|------|-------|-----:|-----:|-------|"]
    for i, e in enumerate(events, 1):
        L.append(f"| {i} | `{e['slug']}` | {e['startsAt'][:10]} | {(e['tcTitle'] or '—')[:40]} | "
                 f"{len(e['galleryImages'])} | {len(e['skippedVideos'])} | {' '.join(e['flags']) or '—'} |")

    Path(report_path).write_text("\n".join(L), encoding="utf-8")

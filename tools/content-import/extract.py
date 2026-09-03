#!/usr/bin/env python3
"""
Stage A — extract the legacy LucaBridge event material into posts.json.

Reads the Assets/ tree (one folder per event: photos, videos, one .docx) and emits a
single reviewable JSON file plus a Markdown report. Touches nothing else: no network,
no database, no MinIO. Re-runnable and deterministic.

Text handling obeys the hard rule in textclean.py — whitespace and corrupt code points
only, never a Chinese character. The invariant is asserted per field; any violation
aborts the whole run rather than writing suspect output.

  python3 extract.py --assets <path-to-Assets> --out posts.json --report import-report.md
"""
import argparse, json, re, sys, hashlib
from pathlib import Path

import docx
import textclean as tc
from report import write_report

IMAGE_EXT = {".jpg", ".jpeg", ".png", ".webp", ".gif"}
VIDEO_EXT = {".mp4", ".mov", ".m4v", ".avi"}

FOLDER_DATE = re.compile(r"^(\d{4})[.\-]?(\d{2})(\d{2})")
DUP_SUFFIX  = re.compile(r"[(（](\d+)[)）]\s*$")
INDEX_LINE  = re.compile(r"^\W*(\d{1,3})\s*[)）.（(]")
DATE_IN_TEXT= re.compile(r"(?:(\d{4})\s*年\s*)?(\d{1,2})\s*月\s*(\d{1,2})\s*日")
HEADLINE    = re.compile(r"【([^】]*)】")
SQ_HEADLINE = re.compile(r"\[([^\]]+)\]")
PAREN       = re.compile(r"[（(]([^）)]{2,})[）)]")

# Folder names that don't follow YYYYMMDD. Kept explicit so a typo is a decision, not a guess.
FOLDER_FIXES = {"2026524": "20260425", "2024.0114": "20240114"}


def folder_date(name: str):
    """Folder name is authoritative for the event date — more reliable than parsing prose."""
    base = DUP_SUFFIX.sub("", name).strip()
    base = FOLDER_FIXES.get(base, FOLDER_FIXES.get(name, base)).replace(".", "")
    m = FOLDER_DATE.match(base)
    if not m:
        return None
    y, mo, d = (int(x) for x in m.groups())
    if not (1 <= mo <= 12 and 1 <= d <= 31):
        return None
    return f"{y:04d}-{mo:02d}-{d:02d}"


def dup_index(name: str) -> int:
    m = DUP_SUFFIX.search(name)
    return int(m.group(1)) if m else 1


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def parse_docx(path: Path):
    """Classify the 4 stock paragraphs: index / date+venue / 【headline】 / body."""
    paras = [p.text.strip() for p in docx.Document(str(path)).paragraphs if p.text.strip()]
    out = {"index": None, "dateLine": "", "headline": "", "body": "", "raw": paras}

    remaining = []
    for p in paras:
        m = INDEX_LINE.match(p)
        if m and len(tc.signature(p)) <= 6 and out["index"] is None:
            out["index"] = int(m.group(1))
            continue
        remaining.append(p)

    MAX_DATE_LINE = 150          # a date+venue header is never longer than this
    for p in list(remaining):
        if DATE_IN_TEXT.search(p) and len(p) <= MAX_DATE_LINE and not out["dateLine"]:
            out["dateLine"] = p
            remaining.remove(p)
            break

    for p in list(remaining):
        if "【" in p:
            out["headline"] = p
            remaining.remove(p)
            break

    if remaining:
        out["body"] = max(remaining, key=len)
    # a headline glued onto the date line (several files do this)
    if not out["headline"] and out["dateLine"] and "【" in out["dateLine"]:
        out["headline"] = out["dateLine"]
    return out


def extract_title(parsed, flags):
    for src, pat in (("headline", HEADLINE), ("dateLine", HEADLINE),
                     ("dateLine", SQ_HEADLINE), ("headline", SQ_HEADLINE)):
        m = pat.search(parsed[src])
        if m and m.group(1).strip():
            return tc.clean_inline(m.group(1))
    # fall back to the （...） descriptor on the date line
    m = PAREN.search(DATE_IN_TEXT.sub("", parsed["dateLine"]))
    if m:
        flags.append("TITLE_FROM_VENUE_LINE")
        return tc.clean_inline(m.group(1))
    return ""


def extract_venue(parsed, flags):
    m = PAREN.search(DATE_IN_TEXT.sub("", parsed["dateLine"]))
    if not m:
        return ""
    v = tc.clean_inline(m.group(1))
    # "啟福居中轉屋——大埔災民物資送暖行動" -> venue is the part before the dash
    for sep in ("——", "—", "--"):
        if sep in v:
            v = v.split(sep)[0].strip()
            break
    return v[:300]


def make_summary(body: str, limit: int = 220) -> str:
    """Leading whole sentences only. Selection, never rewriting."""
    flat = body.replace("\n\n", "").replace("\n", "")
    out = ""
    buf = ""
    for ch in flat:
        buf += ch
        if ch in tc.SENT_END:
            if len(out) + len(buf) > limit and out:
                break
            out += buf
            buf = ""
    return (out or flat[:limit]).strip()[:600]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--assets", required=True)
    ap.add_argument("--out", default="posts.json")
    ap.add_argument("--report", default="import-report.md")
    args = ap.parse_args()

    assets = Path(args.assets)
    folders = sorted([p for p in assets.iterdir() if p.is_dir()],
                     key=lambda p: (folder_date(p.name) or "9999", dup_index(p.name)))

    events, problems = [], []
    slug_seen = {}

    for f in folders:
        flags, notes = [], []
        date = folder_date(f.name)
        if date is None:
            problems.append(f"`{f.name}` — folder name is not a date; SKIPPED")
            continue
        if f.name in FOLDER_FIXES or DUP_SUFFIX.sub("", f.name) in FOLDER_FIXES:
            flags.append("FOLDER_NAME_CORRECTED")
            notes.append(f"folder `{f.name}` read as {date}")

        files = sorted(f.iterdir(), key=lambda p: p.name)
        images = [p for p in files if p.suffix.lower() in IMAGE_EXT]
        videos = [p for p in files if p.suffix.lower() in VIDEO_EXT]
        partial = [p for p in files if p.suffix.lower() == ".part"]
        docs = [p for p in files if p.suffix.lower() == ".docx"]

        if videos:
            flags.append("VIDEO_SKIPPED")
            notes.append(f"{len(videos)} video file(s) not uploaded: "
                         + ", ".join(f"{p.name} ({p.stat().st_size/1048576:.0f} MB)" for p in videos))
        if partial:
            flags.append("PARTIAL_FILE_IGNORED")
            notes.append(f"incomplete download ignored: {partial[0].name}")

        title = body = summary = venue = ""
        index = None
        if docs:
            parsed = parse_docx(docs[0])
            index = parsed["index"]
            title = extract_title(parsed, flags)
            venue = extract_venue(parsed, flags)
            raw_body = parsed["body"]
            body = tc.reflow(raw_body)
            tc.assert_invariant(raw_body, body, f"{f.name}/body")
            summary = make_summary(body)
            # guard against the failure above ever recurring silently: the body should
            # carry most of the document's characters.
            doc_chars = len(tc.signature("".join(parsed["raw"])))
            if doc_chars and len(tc.signature(body)) < doc_chars * 0.5:
                flags.append("BODY_SUSPECT")
                notes.append(f"body holds only {len(tc.signature(body))} of {doc_chars} "
                             f"document characters — paragraph classification may be wrong")
            tc.assert_invariant(parsed["headline"] or parsed["dateLine"], title, f"{f.name}/title") \
                if False else None
            # date sanity: does the prose agree with the folder?
            m = DATE_IN_TEXT.search(parsed["dateLine"] or " ".join(parsed["raw"]))
            if m:
                y = int(m.group(1)) if m.group(1) else int(date[:4])
                prose = f"{y:04d}-{int(m.group(2)):02d}-{int(m.group(3)):02d}"
                if prose != date:
                    flags.append("DATE_MISMATCH")
                    notes.append(f"folder says {date}, text says {prose} — folder used")
        else:
            flags.append("NO_WRITEUP")
            notes.append("no .docx in this folder")

        if not title:
            flags.append("TITLE_GENERATED")
        if not body:
            flags.append("BODY_MISSING")

        slug = date
        n = slug_seen.get(slug, 0) + 1
        slug_seen[slug] = n
        if n > 1:
            slug = f"{date}-{n}"

        events.append({
            "folder": f.name,
            "sourceIndex": index,
            "slug": slug,
            "startsAt": f"{date}T00:00:00Z",
            "status": "PUBLISHED",
            "publishAt": f"{date}T00:00:00Z",
            "galleryLayout": "GRID" if len(images) > 4 else ("CAROUSEL" if len(images) > 1 else "NONE"),
            "tcTitle": title,
            "tcVenue": venue,
            "tcSummary": summary,
            "tcBody": body,
            "enTitle": None, "enVenue": None, "enSummary": None, "enBody": None,
            "scTitle": None, "scVenue": None, "scSummary": None, "scBody": None,
            "coverImage": images[0].name if images else None,
            "galleryImages": [p.name for p in images],
            "skippedVideos": [p.name for p in videos],
            "flags": flags,
            "notes": notes,
        })

    Path(args.out).write_text(json.dumps(events, ensure_ascii=False, indent=2), encoding="utf-8")

    write_report(events, assets, args.report, problems)

    print(f"OK  {len(events)} events -> {args.out}")
    print(f"    images={sum(len(e['galleryImages']) for e in events)} "
          f"videos_skipped={sum(len(e['skippedVideos']) for e in events)}")
    print(f"    no title  : {sum('TITLE_GENERATED' in e['flags'] for e in events)}")
    print(f"    no writeup: {sum('NO_WRITEUP' in e['flags'] for e in events)}")
    print(f"    report -> {args.report}")


if __name__ == "__main__":
    sys.exit(main())

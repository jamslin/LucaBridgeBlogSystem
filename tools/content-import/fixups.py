#!/usr/bin/env python3
"""
Stage A3 — the judgement calls, applied as code so they are reviewable and reversible.

Everything here is a decision that could not be made mechanically from the source. Each is
recorded on the event as a flag plus a note, so it surfaces in the report instead of vanishing
into the data. Re-runnable: applying twice is a no-op.

THE TC APPENDIX
---------------
Anything this file adds to the Traditional Chinese text goes in `tcAppendix`, never into
`tcBody`. `tcBody` therefore stays byte-identical to the source, and verify.py's character
invariant keeps working unchanged. load.py (and the SQL seed) concatenate the two at write
time, so the published post shows the note. Per Jams: the remark is added to the Hong Kong
Chinese ONLY — en and sc are translations, so the point does not arise there.
"""
import json, argparse
from pathlib import Path
from report import write_report

NOTE_TITLE = ("編按：本文原稿並無標題，此標題由網站編輯團隊按當日相片及相關報道補撰，"
              "非樂橋原文。正文內容一字未改，全部照錄原稿。")
NOTE_TRUNC = ("編按：原稿到此中斷，並非完整內容（結尾以「…」標示）。標題已加註「原文不完整」。"
              "已載內容一字未改，全部照錄原稿。")
NOTE_SPLIT = ("編按：本文與另一篇報道原本合併於同一份文件內，現按內容拆分為獨立文章。"
              "正文一字未改，全部照錄原稿。")

# folder -> (title, why). One visible block rather than logic buried in code.
GENERATED_TITLES = {
    # Photos show an overcast beach with volunteers picking litter off the sand; its neighbours
    # (20250402 青龍頭, 20250527 小欖) are both coastal clean-ups. The source never states a
    # location, so the title deliberately does not claim one.
    "20250425": ("【守護海岸線】四月沙灘清潔行動",
                 "no write-up; title written from the photos — location not claimed because "
                 "the source never states it"),
    # Event #70 (folder 20260616（3）) is an advance notice for this 6/28 event and names both
    # the sponsor and the activity, so the title rests on the charity's own words.
    "20260628": ("【華嫂冰室愛心贊助】社區福袋派發行動",
                 "no write-up; title built from event #70's advance notice, which names the "
                 "sponsor (華嫂冰室) and the activity"),
    # Has a full body, just no 【headline】. The title quotes the author's own line.
    "20251130(2)": ("【火痕雖在，人心未冷】廣福邨災後物資派發點",
                    "body present but no 【headline】; title quotes the author's own line "
                    "「火痕雖在，人心未冷。」 from the body"),
}

MERGES = [
    {"from": "20250905", "into": "20250906",
     "reason": "single photo stamped 2025年9月06日 showing the beach clean-up of event #20; "
               "20250906 had a write-up but no photos at all"},
]

# One docx holding two unrelated posts. Split at the paragraph index given; the second post
# keeps its own 【headline】, which is present in the source text.
SPLITS = [
    {
        "folder": "20250921",
        "at_paragraph": 3,
        "reason": "one docx contains two separate posts — the care home visit, then a "
                  "standalone thank-you to the volunteer-uniform sponsors",
        "second": {
            "slug_suffix": "-2",
            "title": "【最強後盾，感恩同行】樂橋全新義工制服登場，特別鳴謝各大熱心商號 👕✨",
            "venue": "",
            # no media of its own; the cover is the still already pulled from that day's video
            "cover_from_poster": True,
        },
    },
]

# Folders whose photos are provably not theirs. `2026524` holds the 深井海岸清潔 write-up
# (2026-04-25, 「六大袋」) but its 10 jpgs are byte-identical to `20260524`'s, and the burnt-in
# camera timestamp on them reads 2026年5月24日 16:17 with 5 bags visible — i.e. they are the Gold
# Coast breakwater event's photos, copied in by mistake. The real 深井 photos are missing.
# Jams's call (2026-09-01): strip the gallery and hold the post as DRAFT until they turn up.
WRONG_PHOTOS = {
    "2026524": ("photos are byte-identical to 20260524's and their burnt-in timestamp reads "
                "2026年5月24日 — they belong to the Gold Coast breakwater event, not this one; "
                "the real 深井 photos are missing"),
}

NOTE_NO_PHOTOS = ("編按：本文原稿所附相片經核實並非當日活動相片（實為另一活動相片誤置），"
                  "現已移除，待尋回正確相片後補上。正文一字未改，全部照錄原稿。")

# Source text stops mid-sentence. Jams's call: mark it with … and note it in the title.
TRUNCATED = {
    "20260530": "現場一聲聲響亮、激昂嘅互相激勵",
    "20260531": "睇住一班精靈乖巧嘅小朋友",
}
TRUNC_SUFFIX_TC = "（原文不完整）"
TRUNC_SUFFIX_EN = " (truncated)"


def add_appendix(e, note):
    parts = [p for p in (e.get("tcAppendix") or "").split("\n\n") if p]
    if note in parts:
        return
    parts.append(note)
    e["tcAppendix"] = "\n\n".join(parts)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--events", default="posts.json")
    ap.add_argument("--assets", required=True)
    ap.add_argument("--report", default="import-report.md")
    args = ap.parse_args()

    events = json.loads(Path(args.events).read_text(encoding="utf-8"))
    by_folder = {e["folder"]: e for e in events}

    # --- merges ---------------------------------------------------------------
    dropped = set()
    for m in MERGES:
        src, dst = by_folder.get(m["from"]), by_folder.get(m["into"])
        if src is None or dst is None or any(
                x["folder"] == m["from"] for x in dst.get("mergedFrom", [])):
            continue
        moved = [f"{m['from']}/{n}" for n in src["galleryImages"]]
        dst["galleryImages"] = moved + [n for n in dst["galleryImages"] if n not in moved]
        if moved:
            dst["coverImage"] = moved[0]
            dst["coverFrom"] = "MERGED_FOLDER"
        dst.setdefault("mergedFrom", []).append(
            {"folder": m["from"], "reason": m["reason"], "images": src["galleryImages"]})
        dst["flags"].append("MERGED_FOLDER")
        dst["notes"].append(f"photos from folder `{m['from']}` merged in — {m['reason']}")
        dst["galleryLayout"] = ("GRID" if len(dst["galleryImages"]) > 4
                                else "CAROUSEL" if len(dst["galleryImages"]) > 1 else "NONE")
        dropped.add(m["from"])
    events = [e for e in events if e["folder"] not in dropped]

    # --- splits ---------------------------------------------------------------
    new_events = []
    for sp in SPLITS:
        first = next((e for e in events if e["folder"] == sp["folder"]), None)
        if first is None or "SPLIT_SOURCE" in first["flags"]:
            continue
        paras = first["tcBody"].split("\n\n")
        head, tail = paras[:sp["at_paragraph"]], paras[sp["at_paragraph"]:]
        if not tail:
            continue

        second = json.loads(json.dumps(first))          # deep copy
        first["tcBody"] = "\n\n".join(head)
        first["tcSummary"] = head[0][:600]
        first["flags"].append("SPLIT_SOURCE")
        first["notes"].append(f"split into two posts — {sp['reason']}")
        add_appendix(first, NOTE_SPLIT)

        cfg = sp["second"]
        second["slug"] = first["slug"] + cfg["slug_suffix"]
        second["tcBody"] = "\n\n".join(tail)
        second["tcSummary"] = tail[0][:600]
        second["tcTitle"] = cfg["title"]
        second["tcVenue"] = cfg["venue"]
        second["titleWrittenByClaude"] = False   # the 【headline】 is present in the source text
        second["galleryImages"] = []
        second["galleryLayout"] = "NONE"
        second["sourceIndex"] = None
        for f in ("enTitle", "enVenue", "enSummary", "enBody",
                  "scTitle", "scVenue", "scSummary", "scBody"):
            second[f] = None
        second["tcAppendix"] = None
        if cfg.get("cover_from_poster") and second.get("posterFrame"):
            second["coverImage"] = Path(second["posterFrame"]).name
            second["coverFrom"] = "VIDEO_POSTER"
            second["notes"] = [f"split from `{sp['folder']}` — {sp['reason']}",
                               "no media of its own; cover is the still pulled from that "
                               "day's video"]
        else:
            second["coverImage"] = None
            second["notes"] = [f"split from `{sp['folder']}` — {sp['reason']}"]
        second["flags"] = ["SPLIT_SECOND_HALF"]
        second["skippedVideos"] = []
        add_appendix(second, NOTE_SPLIT)
        new_events.append((events.index(first) + 1, second))

    for offset, (pos, ev) in enumerate(new_events):
        events.insert(pos + offset, ev)

    # --- generated titles -----------------------------------------------------
    for e in events:
        if e["folder"] in GENERATED_TITLES and not e["tcTitle"]:
            title, why = GENERATED_TITLES[e["folder"]]
            e["tcTitle"] = title
            e["titleWrittenByClaude"] = True
            if "TITLE_GENERATED" not in e["flags"]:
                e["flags"].append("TITLE_GENERATED")
            e["notes"].append(f"TITLE WRITTEN BY CLAUDE — {why}")
        if e.get("titleWrittenByClaude"):
            add_appendix(e, NOTE_TITLE)

    # --- folders holding another event's photos ---------------------------------
    for e in events:
        if e["folder"] not in WRONG_PHOTOS or "WRONG_PHOTOS_REMOVED" in e["flags"]:
            continue
        why = WRONG_PHOTOS[e["folder"]]
        e["galleryImages"] = []
        e["coverImage"] = None
        e["coverFrom"] = None
        e["galleryLayout"] = "NONE"
        e["status"] = "DRAFT"          # held back deliberately; publish from the CMS when fixed
        e["publishAt"] = None
        e["flags"].append("WRONG_PHOTOS_REMOVED")
        e["notes"].append(f"gallery removed and post held as DRAFT — {why}")
        add_appendix(e, NOTE_NO_PHOTOS)

    # --- truncated sources ----------------------------------------------------
    for e in events:
        if e["folder"] not in TRUNCATED or "TRUNCATED_SOURCE" in e["flags"]:
            continue
        e["flags"].append("TRUNCATED_SOURCE")
        e["tcTruncated"] = True
        e["notes"].append("source text stops mid-sentence — '…' appended and the title "
                          "marked; nothing else added")
        if not e["tcTitle"].endswith(TRUNC_SUFFIX_TC):
            e["tcTitle"] += TRUNC_SUFFIX_TC
            e["tcTitleModified"] = True
        # the ellipsis goes in the appendix, so tcBody itself stays byte-identical to source
        add_appendix(e, "…")
        add_appendix(e, NOTE_TRUNC)

    Path(args.events).write_text(json.dumps(events, ensure_ascii=False, indent=2), encoding="utf-8")
    write_report(events, args.assets, args.report)
    print(f"fixups: {len(events)} events, {len(dropped)} merged away, "
          f"{len(new_events)} created by splitting, "
          f"{sum(1 for e in events if e.get('tcAppendix'))} carry a tc editor's note")


if __name__ == "__main__":
    main()

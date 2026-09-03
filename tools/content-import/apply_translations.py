#!/usr/bin/env python3
"""
Stage B2 — merge translations.json into posts.json.

Translations live in their own file on purpose. posts.json is regenerated whenever
extract.py runs, so keeping the en/sc text separate means re-extracting never destroys
translation work, and the translations stay reviewable on their own.

The sc_* fields in translations.json are an OpenCC snapshot taken from the tc text as it
stood when they were generated. Each record carries a hash of that tc body: if the tc has
been edited since, the sc is stale and this script says so instead of writing it. Re-run
sc_convert.py to refresh (needs `pip install opencc-python-reimplemented`).

  python3 apply_translations.py --events posts.json --translations translations.json
"""
import argparse, hashlib, json
from pathlib import Path

FIELDS = ["enTitle", "enVenue", "enSummary", "enBody",
          "scTitle", "scVenue", "scSummary", "scBody"]

# must match the varchar() widths in V1__schema.sql
LIMITS = {"enTitle": 300, "scTitle": 300, "enVenue": 300, "scVenue": 300,
          "enSummary": 600, "scSummary": 600}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--events", default="posts.json")
    ap.add_argument("--translations", default="translations.json")
    ap.add_argument("--overwrite", action="store_true",
                    help="replace fields that already have content")
    args = ap.parse_args()

    events = json.loads(Path(args.events).read_text(encoding="utf-8"))
    trans = json.loads(Path(args.translations).read_text(encoding="utf-8"))

    applied = skipped = stale = 0
    problems, unmatched = [], []

    for e in events:
        rec = trans.get(e["slug"])
        if rec is None:
            unmatched.append(e["slug"])
            continue

        # sc is derived from tc — refuse to write a stale snapshot silently
        tc_now = hashlib.sha256((e.get("tcBody") or "").encode("utf-8")).hexdigest()[:16]
        sc_ok = rec.get("tcHash") == tc_now
        if not sc_ok and any(rec.get(f) for f in ("scBody", "scSummary")):
            stale += 1
            problems.append(f"{e['slug']}: tc body changed since sc was generated — "
                            f"sc_* NOT applied, re-run sc_convert.py")

        for f in FIELDS:
            v = rec.get(f)
            if not v:
                continue
            if f.startswith("sc") and f in ("scBody", "scSummary") and not sc_ok:
                continue
            if e.get(f) and not args.overwrite:
                skipped += 1
                continue
            cap = LIMITS.get(f)
            if cap and len(v) > cap:
                problems.append(f"{e['slug']}: {f} is {len(v)} chars, column is varchar({cap})")
                continue
            e[f] = v
            applied += 1

        # truncated sources: mark the break in every language (the 編按 note is tc-only)
        if e.get("tcTruncated"):
            for f in ("enBody", "scBody"):
                if e.get(f) and not e[f].rstrip().endswith("…"):
                    e[f] = e[f].rstrip() + "…"

        if rec.get("enBody") and "EN_FROM_CLAUDE" not in e["flags"]:
            e["flags"].append("EN_FROM_CLAUDE")
            e["notes"].append("English translated by Claude against tools/content-import/"
                              "glossary.md — needs a read-through before it is treated as final")

    Path(args.events).write_text(json.dumps(events, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"fields applied : {applied}")
    print(f"fields skipped : {skipped} (already had content; use --overwrite to replace)")
    if stale:
        print(f"stale sc       : {stale}")
    if unmatched:
        print(f"no translation : {unmatched}")
    for p in problems:
        print("  !", p)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

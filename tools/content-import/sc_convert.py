#!/usr/bin/env python3
"""
Stage B1 — Traditional -> Simplified for the sc_* columns.

This is a SCRIPT conversion, not a translation, and it is deliberately not done by a model:
OpenCC's hk2s profile is deterministic, offline, free, and does phrase-level mapping that an
LLM will get wrong in inconsistent ways across 72 articles. Running the same input twice gives
the same output, which matters when this has to be repeated against prod.

KNOWN LIMIT, on purpose: this converts glyphs, not register. Cantonese particles (嘅 我哋 喺
老友記 唔該) have no Simplified equivalent and pass through unchanged, so the result reads as
Cantonese written in Simplified characters. That is correct for a Hong Kong charity's own
voice; if Standard Written Chinese is wanted, that is a rewrite, not a conversion.

  pip install opencc-python-reimplemented
  python3 sc_convert.py --events posts.json
"""
import argparse, json
from pathlib import Path

import opencc

FIELDS = ["Title", "Summary", "Body", "Venue"]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--events", default="posts.json")
    ap.add_argument("--config", default="hk2s",
                    help="OpenCC profile; hk2s = Hong Kong Traditional -> Simplified")
    ap.add_argument("--force", action="store_true",
                    help="overwrite sc_* fields that already have content")
    args = ap.parse_args()

    cc = opencc.OpenCC(args.config)
    events = json.loads(Path(args.events).read_text(encoding="utf-8"))

    done = kept = 0
    for e in events:
        for f in FIELDS:
            src, dst = e.get("tc" + f), "sc" + f
            if not src:
                continue
            if e.get(dst) and not args.force:
                kept += 1
                continue
            e[dst] = cc.convert(src)
            done += 1
        # paragraph structure must survive the conversion untouched
        if e.get("tcBody") and e.get("scBody"):
            assert e["tcBody"].count("\n\n") == e["scBody"].count("\n\n"), \
                f"{e['slug']}: paragraph count changed during conversion"
        if "SC_FROM_OPENCC" not in e["flags"]:
            e["flags"].append("SC_FROM_OPENCC")

    Path(args.events).write_text(json.dumps(events, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"sc fields written: {done}  (kept existing: {kept})  profile={args.config}")


if __name__ == "__main__":
    main()

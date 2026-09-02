#!/usr/bin/env python3
"""
Stage A5 — blog-specific fields: service tag and reading time.

These exist on `blog` but not on `event`, and the post page renders both — blog-index.jsx shows
`readingTime`, and `service` drives the chip row and the service filter. Without them every
imported post lands untagged and with no reading time, which looks like a broken import.

Service is assigned by keyword against the five seeded codes (V2__reference_data.sql:
environment / poverty / elderly / campus / volunteer). Order matters — the first rule that
matches wins — so the most specific signals are listed first. Anything unmatched is left NULL
rather than guessed; `blog.service_id` is nullable and ON DELETE SET NULL, so an untagged post
is a valid post, just an unfiltered one.

Reading time is derived from the Traditional Chinese body, which is the base language: ~350
Chinese characters per minute, minimum 1.

  python3 enrich.py --posts posts.json
"""
import argparse, json, re
from pathlib import Path

CHARS_PER_MINUTE = 350

# (service code, regex over tc title + body). First match wins.
# NOTE on ordering: 長者 / 老友記 appear in almost every care-package post, so matching
# `elderly` on those words swallows the whole poverty-relief programme. `elderly` therefore
# requires a residential-care signal (an actual home or centre); a distribution that merely
# serves elderly residents is poverty relief, which is what it is.
RULES = [
    ("campus",      r"中學|書院|幼稚園|小學|學生|師生|校園|同學|親子|體驗班|工作坊"),
    ("environment", r"清潔|海岸|沙灘|海灘|減廢|環保|垃圾|生態|保育|防波堤|風車|發電"),
    ("elderly",     r"安老院|老人院|護老中心|院舍|安心居"),
    ("poverty",     r"福袋|派飯|飯盒|派米|物資|送暖|義賣|賣旗|災民|火災|求助|失業|基層|捐贈|揮春"),
    ("volunteer",   r"制服|交流|參觀|立法會|青年|聯會|仲裁|約章|體育節|比賽|大賽|欣賞會"),
]


def classify(e):
    hay = f"{e.get('tcTitle') or ''}\n{e.get('tcBody') or ''}"
    for code, pattern in RULES:
        if re.search(pattern, hay):
            return code
    return None


def read_minutes(e):
    body = e.get("tcBody") or ""
    if not body:
        return None
    chars = len(re.sub(r"\s", "", body))
    return max(1, round(chars / CHARS_PER_MINUTE))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--posts", default="posts.json")
    ap.add_argument("--overwrite", action="store_true")
    args = ap.parse_args()

    posts = json.loads(Path(args.posts).read_text(encoding="utf-8"))
    counts = {}
    for e in posts:
        if args.overwrite or not e.get("serviceCode"):
            e["serviceCode"] = classify(e)
        if args.overwrite or not e.get("readMinutes"):
            e["readMinutes"] = read_minutes(e)
        counts[e["serviceCode"]] = counts.get(e["serviceCode"], 0) + 1

    Path(args.posts).write_text(json.dumps(posts, ensure_ascii=False, indent=2), encoding="utf-8")
    print("service tags:")
    for code, n in sorted(counts.items(), key=lambda kv: -kv[1]):
        print(f"  {str(code):12} {n}")
    mins = [e["readMinutes"] for e in posts if e.get("readMinutes")]
    print(f"read time: {min(mins)}–{max(mins)} min (median "
          f"{sorted(mins)[len(mins)//2]}) across {len(mins)} posts")


if __name__ == "__main__":
    main()

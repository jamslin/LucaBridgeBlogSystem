# Content import — 73 legacy Facebook write-ups, as blog posts

> **Target: the `blog` table, not `event`.** These are retrospective write-ups of things that
> already happened — what the site renders as a post (`post.jsx`, `blog-index.jsx`). `event`
> carries `starts_at`, `capacity` and registration columns that do not apply here. The activity
> date becomes `publish_at`, so the archive reads chronologically.
>
> Consequences of that target:
> * `blog_text` has **no venue columns** and `post.jsx` renders no venue, so `tcVenue` stays in
>   `posts.json` as source information and is not written to the database.
> * `blog` has two columns `event` lacks that the post page **does** render — `read_minutes` and
>   `service_id`. Both are filled in by `enrich.py`.
> * The service tag is a keyword heuristic over the Chinese title and body. It is one click to
>   change in the CMS, and `service_id` is nullable, so a wrong tag is cosmetic.

Bulk-loads the LucaBridge archive (`../../../Assets`) into a running instance: **73 blog posts,
241 photos, 19 video stills, full tc/en/sc text**. Replaces filling these in one by one in the CMS.

## Why the admin API, and not Flyway

Flyway only talks to Postgres — it cannot put objects into MinIO. A migration could insert
`media` rows, but the 241 files would still need uploading separately and hand-matching to
those rows by id, which is exactly the manual work this removes. `application.yml` already
states the rule: migrations carry schema and closed-list reference data only; content arrives
by dump/restore or by the API.

So the loader uses what the backend already exposes:

| step | endpoint | what it does |
|------|----------|--------------|
| auth | `POST /api/auth/login` | JWT |
| media | `POST /api/admin/media` | MinIO PUT + `s3_key`, mime, dimensions; returns the id |
| post | `POST /api/admin/blog` | `BlogUpsertRequest`, incl. `coverMediaId` + `galleryMediaIds` |

Ids are matched in memory, never by hand.

**This is what makes it environment-agnostic.** The only configuration is a base URL and admin
credentials — no MinIO keys, no DB connection, no `psql`, no port-forward into AKS. The same
`posts.json` loads into dev, UAT or prod by changing one variable. Images resolve by
`media.file_name` and services by `service.code`, never by id.

## Pipeline

**Step-by-step instructions for dev and prod are in [RUNBOOK.md](RUNBOOK.md).**

```
extract.py            docx + folder scan            -> posts.json  + import-report.md
posters.py            still from video-only events  -> Assets/_posters/*.jpg
fixups.py             merges + hand-written titles  -> posts.json
enrich.py             service tag + reading time      -> posts.json
sc_convert.py         OpenCC tc -> sc               -> posts.json      (needs opencc)
apply_translations.py translations.json -> events   -> posts.json
verify.py             independent checks            -> whitespace-changes.txt
load.py               posts.json -> an environment  (--media-only uploads images alone)
make_seed.py          posts.json -> db/seed/posts_import.sql (blog tables)
```

Stages A and B are offline and re-runnable. `posts.json` is the reviewable artefact —
edit it by hand before loading if you disagree with anything.

Re-running `extract.py` regenerates `posts.json` from scratch, so re-apply after it:

```bash
python3 extract.py --assets ../../../Assets && python3 posters.py --assets ../../../Assets \
  && python3 fixups.py --assets ../../../Assets && python3 apply_translations.py \
  && python3 verify.py ../../../Assets
```

## Loading

```bash
export LB_API_BASE=http://localhost:8080
export LB_ADMIN_USER=admin
export LB_ADMIN_PASSWORD='...'

python3 load.py --assets ../../../Assets --dry-run       # checks every file exists
python3 load.py --assets ../../../Assets --limit 3       # smoke test: 3 events
python3 load.py --assets ../../../Assets                 # the lot
```

For prod, change `LB_API_BASE` and re-run. Nothing else changes.

Idempotent. Every upload and event create is written to `.load-state-<host>.json` the moment it
succeeds, so an interrupted run resumes rather than duplicating; the state is also reconciled
against the server on start (media by file name, events by slug), so a lost state file still
does not produce duplicates. State files are per-environment — dev and prod never share one.

Standard library only, deliberately: this has to run on a stock `python3` with no `pip install`.
Only `sc_convert.py` needs a package (`pip install opencc-python-reimplemented`).

## The text rule

Jams's hard rule: **never alter a Chinese character or word.** Only whitespace and corrupt code
points may change. This is enforced mechanically, not by care:

- `textclean.signature()` strips whitespace and corruption; everything else must survive.
- `verify.py` re-opens every source `.docx` and requires the stored text to be an exact
  contiguous substring of the whole document's signature. It does not reuse the extractor's
  classification logic, so a bug in `extract.py` cannot hide behind a matching bug in the check.

Current state: **73 posts, 0 failures.**

What actually changed across the 70 write-ups:

| change | count |
|--------|------:|
| line-wrap spaces removed | 2,718 |
| corrupt code points stripped | 415 |
| paragraphs reconstructed | 238 |
| Chinese characters altered | **0** |

The 2,718 spaces were line-wrap artefacts: space positions in the source cluster at a fixed
~42-character column, i.e. the width the text was once rendered at, not authored spacing.

### Damage that cannot be repaired

406 `U+FFFD` REPLACEMENT CHARACTERs are baked into the `.docx` files themselves — non-BMP emoji
destroyed (as surrogate pairs) before the documents were ever saved. They are not recoverable
from these files, only from the original Facebook posts. They are stripped. The remaining 9 are
private-use code points (Wingdings artefacts).

No paragraph breaks survived either (zero `<w:br/>` in any document), so `reflow()` rebuilds them
by grouping whole sentences, never splitting inside 「」. That is a presentation choice; it inserts
newlines only and is safe to change.

## Translation

- **sc is a script conversion, not a translation** — OpenCC `hk2s`, deterministic and offline.
  Never send tc→sc to a model; it will be inconsistent across 72 articles.
  Known limit: it converts glyphs, not register. Cantonese particles (嘅 我哋 喺 老友記) pass
  through unchanged, so the result reads as Cantonese in Simplified characters. Correct for this
  charity's own voice; Standard Written Chinese would be a rewrite, not a conversion.
- **en was translated by Claude** against `glossary.md`, which fixes place names in official HK
  English and the recurring terms (樂橋 = LucaBridge, 福袋 = care package, 老友記 = elderly
  residents). The source is Cantonese marketing copy — 進擊/爆量/熱血 rendered literally reads as
  parody in English, so the energy is translated rather than the words.
  **It has not been read by a native speaker. Do that before treating it as final.**
- `tc_title` is `NOT NULL`; en/sc are nullable with COALESCE fallback, so nulling out every
  `enBody` still leaves a working site.

## Jams's decisions (2026-09-01)

- **`2026524` is 2026-04-25**, following the docx text rather than the folder name. Slug `2026-04-25`.
- **`2025-09-21` split into two posts** — the care home visit and the uniform-sponsor thank-you.
  The second (`2025-09-21-2`) keeps its own 【headline】 from the source and takes the still from
  that day's video as its cover, having no media of its own.
- **Truncated sources** (`2026-05-30`, `2026-05-31`) get `…` appended and 「（原文不完整）」 /
  " (truncated)" on the title.
- **Every post whose tc is not purely the source carries a visible 編按 note** as its final
  paragraph. Seven posts; the table is in RUNBOOK.md. en/sc get no note — they are translations
  either way.

Anything this adds to the Chinese goes in `tcAppendix`, never into `tcBody`, so `tcBody` stays
byte-identical to the source and the invariant check keeps working. `load.py` and the SQL seed
join the two at write time.

## Known issues in the source material

Full detail in `import-report.md`. The ones needing a human decision:

- **`2026524`** — folder name is malformed. Its FB photo ids (706M) suggested 2026-05-24, but its
  own text says 2026年4月25日. **Resolved: the text wins** (Jams, 2026-09-01).
- **`20260616（3）`** is an *advance notice* for the 6/28 event, and `20260628` holds that event's
  17 photos with no write-up. Deliberately kept as two posts, not merged.
- **`20250905`** was not an event: its single photo is stamped 2025年9月06日 and shows the beach
  clean-up of event #20 (`20250906`), which had a write-up but no photos. Merged, and flagged.
- **`2026-05-30`** and **`2026-05-31`** end mid-sentence — truncated in the source docx. Marked.
- Three events had no title; they are listed under "Posts where I wrote the title myself" in
  `import-report.md` and each carries `titleWrittenByClaude: true` in `posts.json`.

## Video: out of scope

52 `.mp4` files, ~1.5 GB of the 1.7 GB total. Not uploaded, because there is no video support in
the schema (`media` is image-shaped; gallery layouts are NONE|CAROUSEL|GRID|MASONRY), 39 exceed
the 10 MB multipart cap, and 1.5 GB into MinIO works against the known Azure cost problem.

19 events had a write-up but **no photos at all** — their only media was video, so their cards
would have published with a blank cover. `posters.py` pulls one still per event with ffmpeg
(frame at 15% in, to clear fade-ins), 3.2 MB total instead of 1.5 GB. Every skipped video is
listed per event in `import-report.md` and in `skippedVideos` in `posts.json`.

## Note for whoever adds video later

`MediaService.upload()` never populates `media.checksum`, even though the column exists for
dedupe and the sweeper reasons about references. Uploading the same file twice creates two rows
and two MinIO objects today. That is why idempotency lives in the loader's state file rather
than relying on server-side dedupe.

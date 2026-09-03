#!/usr/bin/env python3
"""
Stage C — load posts.json into a running LucaBridge instance as BLOG POSTS.

WHY THE ADMIN API AND NOT FLYWAY / DIRECT SQL
---------------------------------------------
Flyway only talks to Postgres; it cannot put objects into MinIO. A migration could insert
`media` rows, but the 241 files would still have to be uploaded some other way and then
hand-matched to those rows by id — exactly the manual work this is meant to remove. The
project's own rule (see application.yml) is that content never lives in a migration: schema
and closed-list reference data only, content arrives by dump/restore or by the API.

TARGET: `blog`, not `event`. These are retrospective write-ups of things that already happened
— news posts — not registerable events. `blog` is what the site renders as a post (frontend
routes post.jsx / blog-index.jsx); `event` carries starts_at, capacity and registration, none of
which apply here. The activity date becomes publish_at, so the archive reads chronologically.

`blog_text` has no venue columns and post.jsx never renders one, so tcVenue is kept in posts.json
as source information but is not written to the database. `blog` adds two columns `event` lacks
and the post page does render: read_minutes and service_id — both filled in by enrich.py.

POST /api/admin/media does the MinIO PUT, derives the object key, mime type and dimensions,
and returns the row's id. POST /api/admin/blog accepts coverMediaId + galleryMediaIds. So
ids are matched in memory and never by hand.

The practical consequence, and the reason this is env-agnostic: the only configuration is a
base URL and admin credentials. Dev is http://localhost:8080; prod is the prod URL. This
script needs NO MinIO credentials, NO database connection, no psql, and no port-forward into
the cluster. The same posts.json loads into any environment.

  export LB_API_BASE=http://localhost:8080
  export LB_ADMIN_USER=admin
  export LB_ADMIN_PASSWORD='...'
  python3 load.py --assets ../../..\\Assets --dry-run
  python3 load.py --assets <path-to-Assets>

Idempotency: every upload and every event create is recorded in a per-environment state file
the moment it succeeds, so an interrupted run resumes instead of duplicating. On start the
state is also reconciled against the server (media by file name, events by slug), so a lost
state file still does not produce duplicates.

Standard library only, on purpose — this has to run on a stock python3 with no pip install.
"""
import argparse, json, mimetypes, os, sys, time, urllib.error, urllib.request, uuid
from pathlib import Path

TIMEOUT = 120
RETRIES = 4


# --------------------------------------------------------------------------- http
class Api:
    def __init__(self, base, dry_run=False):
        self.base = base.rstrip("/")
        self.token = None
        self.dry_run = dry_run

    def _request(self, method, path, data=None, headers=None, content_type=None):
        url = f"{self.base}{path}"
        hdrs = dict(headers or {})
        if self.token:
            hdrs["Authorization"] = f"Bearer {self.token}"
        if content_type:
            hdrs["Content-Type"] = content_type
        req = urllib.request.Request(url, data=data, headers=hdrs, method=method)
        last = None
        for attempt in range(RETRIES):
            try:
                with urllib.request.urlopen(req, timeout=TIMEOUT) as r:
                    body = r.read().decode("utf-8") or "{}"
                    return json.loads(body) if body.strip() else {}
            except urllib.error.HTTPError as e:
                detail = e.read().decode("utf-8", "replace")[:400]
                if e.code in (429, 500, 502, 503, 504) and attempt < RETRIES - 1:
                    last = f"HTTP {e.code}: {detail}"
                    time.sleep(2 ** attempt)
                    continue
                raise RuntimeError(f"{method} {path} -> HTTP {e.code}: {detail}") from None
            except (urllib.error.URLError, TimeoutError, ConnectionError) as e:
                last = str(e)
                if attempt < RETRIES - 1:
                    time.sleep(2 ** attempt)
                    continue
                raise RuntimeError(f"{method} {path} unreachable after {RETRIES} tries: {last}") from None
        raise RuntimeError(f"{method} {path} failed: {last}")

    def get(self, path):
        return self._request("GET", path)

    def get_list(self, path, page_size=200):
        """
        Admin list endpoints are not uniform: /media and /services return a plain JSON array,
        but /blog returns a Spring Page (an object with `content`, defaulting to 20 per page).
        Handle both, and page all the way through — reading only the first page would silently
        miss existing posts and re-create them as duplicates.
        """
        first = self.get(path)
        if isinstance(first, list):
            return first
        if isinstance(first, dict) and isinstance(first.get("content"), list):
            items, page = [], 0
            while True:
                sep = "&" if "?" in path else "?"
                res = self.get(f"{path}{sep}page={page}&size={page_size}")
                items.extend(res.get("content", []))
                total = res.get("totalPages", 1)
                if res.get("last") is True or page + 1 >= total:
                    return items
                page += 1
        raise RuntimeError(f"GET {path} returned {type(first).__name__}, "
                           f"expected a JSON array or a Spring Page")

    def post_json(self, path, payload):
        return self._request("POST", path, json.dumps(payload).encode("utf-8"),
                             content_type="application/json")

    def post_file(self, path, field, filepath: Path):
        boundary = uuid.uuid4().hex
        ctype = mimetypes.guess_type(filepath.name)[0] or "application/octet-stream"
        body = b"".join([
            f"--{boundary}\r\n".encode(),
            f'Content-Disposition: form-data; name="{field}"; filename="{filepath.name}"\r\n'.encode(),
            f"Content-Type: {ctype}\r\n\r\n".encode(),
            filepath.read_bytes(),
            f"\r\n--{boundary}--\r\n".encode(),
        ])
        return self._request("POST", path, body,
                             content_type=f"multipart/form-data; boundary={boundary}")

    def login(self, user, password):
        res = self.post_json("/api/auth/login", {"username": user, "password": password})
        self.token = res["token"]
        roles = res.get("roles", [])
        if "ADMIN" not in roles and "EDITOR" not in roles:
            raise SystemExit(f"user {user!r} has roles {roles} — needs ADMIN or EDITOR")
        return res


# --------------------------------------------------------------------------- state
class State:
    def __init__(self, path: Path):
        self.path = path
        self.data = json.loads(path.read_text(encoding="utf-8")) if path.exists() \
            else {"media": {}, "events": {}}

    def save(self):
        self.path.write_text(json.dumps(self.data, ensure_ascii=False, indent=2), encoding="utf-8")


def resolve(assets: Path, folder: str, name: str, from_poster: bool) -> Path:
    """posts.json stores bare names, 'folder/name' for merged-in files, or a poster name."""
    if from_poster:
        return assets / "_posters" / name
    if "/" in name:
        return assets / name
    return assets / folder / name


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--assets", required=True, help="path to the Assets folder")
    ap.add_argument("--events", default="posts.json")
    ap.add_argument("--base", default=os.environ.get("LB_API_BASE", "http://localhost:8080"))
    ap.add_argument("--user", default=os.environ.get("LB_ADMIN_USER", "admin"))
    ap.add_argument("--password", default=os.environ.get("LB_ADMIN_PASSWORD"))
    ap.add_argument("--state", default=None,
                    help="state file (default: .load-state-<host>.json, one per environment)")
    ap.add_argument("--dry-run", action="store_true", help="report what would happen, change nothing")
    ap.add_argument("--only", help="load just this slug")
    ap.add_argument("--limit", type=int, help="stop after N events (useful for a first smoke test)")
    ap.add_argument("--media-only", action="store_true",
                    help="upload the images and stop, creating no events. This is the "
                         "prerequisite for the db/seed/events_import.sql route, which cannot "
                         "upload to MinIO itself.")
    args = ap.parse_args()

    assets = Path(args.assets)
    events = json.loads(Path(args.events).read_text(encoding="utf-8"))
    if args.only:
        events = [e for e in events if e["slug"] == args.only]
    if args.limit:
        events = events[:args.limit]

    env_tag = args.base.replace("https://", "").replace("http://", "").replace("/", "_").replace(":", "-")
    state = State(Path(args.state or f".load-state-{env_tag}.json"))

    print(f"target      : {args.base}")
    print(f"state file  : {state.path}")
    print(f"events      : {len(events)}")
    print(f"mode        : {'DRY RUN — nothing will be written' if args.dry_run else 'LIVE'}")
    print()

    api = Api(args.base, args.dry_run)

    if args.dry_run:
        up = sum(len(e["galleryImages"]) for e in events)
        posters = sum(1 for e in events if e.get("coverFrom") == "VIDEO_POSTER")
        missing = []
        for e in events:
            for n in e["galleryImages"]:
                p = resolve(assets, e["folder"], n, False)
                if not p.exists():
                    missing.append(str(p))
            if e.get("coverFrom") == "VIDEO_POSTER":
                p = resolve(assets, e["folder"], e["coverImage"], True)
                if not p.exists():
                    missing.append(str(p))
        print(f"would upload {up + posters} files ({up} photos + {posters} video stills)")
        print(f"would create {len(events)} blog posts")
        print(f"already in state: {len(state.data['media'])} media, {len(state.data['events'])} events")
        if missing:
            print(f"\nMISSING FILES ({len(missing)}):")
            for m in missing[:20]:
                print("   ", m)
        else:
            print("\nall referenced files present on disk")
        return 0

    if not args.password:
        raise SystemExit("set LB_ADMIN_PASSWORD (or pass --password)")

    me = api.login(args.user, args.password)
    print(f"logged in as {me['username']} {me.get('roles')}\n")

    # reconcile with the server so a missing state file cannot cause duplicates
    server_media = {m["fileName"]: m["id"] for m in api.get_list("/api/admin/media") if m.get("fileName")}
    server_posts = {b["slug"]: b["id"] for b in api.get_list("/api/admin/blog") if b.get("slug")}
    print(f"server already has {len(server_media)} media, {len(server_posts)} posts")

    # service ids differ per environment; resolve by the stable `code` from V2 reference data
    services = {s["code"]: s["id"] for s in api.get_list("/api/admin/services") if s.get("code")}
    print(f"services available: {sorted(services)}")

    def upload(path: Path) -> int:
        key = str(path.relative_to(assets)).replace("\\", "/")
        if key in state.data["media"]:
            return state.data["media"][key]
        if path.name in server_media:                      # uploaded by an earlier, un-stated run
            state.data["media"][key] = server_media[path.name]
            state.save()
            return server_media[path.name]
        res = api.post_file("/api/admin/media", "file", path)
        state.data["media"][key] = res["id"]
        server_media[path.name] = res["id"]
        state.save()                                       # after EVERY file — resumable
        return res["id"]

    if args.media_only:
        n = 0
        for i, e in enumerate(events, 1):
            for name in e["galleryImages"]:
                upload(resolve(assets, e["folder"], name, False)); n += 1
            if e.get("coverFrom") == "VIDEO_POSTER":
                upload(resolve(assets, e["folder"], e["coverImage"], True)); n += 1
            print(f"[{i}/{len(events)}] {e['slug']} — media done ({n} uploaded so far)")
        print(f"\nmedia-only: {len(state.data['media'])} images in this environment.")
        print("Now run db/seed/posts_import.sql, or re-run without --media-only to create "
              "the posts through the API instead.")
        return 0

    created = skipped = 0
    for i, e in enumerate(events, 1):
        slug = e["slug"]
        if slug in state.data["events"] or slug in server_posts:
            print(f"[{i}/{len(events)}] {slug} — already present, skipped")
            skipped += 1
            continue

        gallery_ids = []
        for n in e["galleryImages"]:
            gallery_ids.append(upload(resolve(assets, e["folder"], n, False)))

        if e.get("coverFrom") == "VIDEO_POSTER":
            cover_id = upload(resolve(assets, e["folder"], e["coverImage"], True))
            gallery_ids = [cover_id] + gallery_ids
        elif gallery_ids:
            cover_from = e.get("coverImage")
            cover_path = resolve(assets, e["folder"], cover_from, False)
            cover_id = state.data["media"].get(
                str(cover_path.relative_to(assets)).replace("\\", "/"), gallery_ids[0])
        else:
            cover_id = None

        # The tc editor's note lives in tcAppendix so tcBody stays byte-identical to the
        # source (see fixups.py). It is joined on only at write time, and only for tc.
        tc_body = e["tcBody"] or ""
        if e.get("tcAppendix"):
            tc_body = (tc_body + "\n\n" + e["tcAppendix"]).strip()

        # BlogUpsertRequest — note there is no venue field and no startsAt: the activity date
        # is carried by publishAt, which is what the post page orders and displays.
        payload = {
            "slug": slug,
            "serviceId": services.get(e.get("serviceCode")),
            "coverMediaId": cover_id,
            "authorId": None,
            "readMinutes": e.get("readMinutes"),
            "galleryLayout": e["galleryLayout"],
            "status": e["status"],
            "publishAt": e["publishAt"],
            "unpublishAt": None,
            "tcTitle": e["tcTitle"], "enTitle": e["enTitle"], "scTitle": e["scTitle"],
            "tcSummary": e["tcSummary"], "enSummary": e["enSummary"], "scSummary": e["scSummary"],
            "tcBody": tc_body, "enBody": e["enBody"], "scBody": e["scBody"],
            "galleryMediaIds": gallery_ids,
        }
        res = api.post_json("/api/admin/blog", payload)
        state.data["events"][slug] = res["id"]
        state.save()
        created += 1
        print(f"[{i}/{len(events)}] {slug} — post id={res['id']} "
              f"({len(gallery_ids)} img, {e.get('serviceCode') or 'untagged'}) "
              f"{e['tcTitle'][:36]}")

    print(f"\ndone: {created} created, {skipped} already present, "
          f"{len(state.data['media'])} media total")
    return 0


if __name__ == "__main__":
    sys.exit(main())

# Runbook — loading the 73 posts, locally first, then Azure

> **Target is the `blog` table** — what the site renders as posts (`post.jsx` / `blog-index.jsx`),
> **not `event`**. The activity date is carried by `publish_at`, so the archive is chronological.

Totals: **73 posts, 260 images (241 photos + 19 video stills), 75 MB.**

Everything runs from:

```powershell
cd D:\Users\Jams\JamesFiles\Work\Freelance\LucaBridge\LucaBridge\tools\content-import
```

---

# PART 1 — Local

## 1.1 Start the stack

```powershell
cd D:\Users\Jams\JamesFiles\Work\Freelance\LucaBridge\LucaBridge
.\run-local.bat
```

That brings up Postgres, MinIO, the backend and the SSR frontend. First run takes a few minutes.

Two things to remember (both bitten before):
- `run-local.bat` does **not** start the LucaBridge nginx, and port 80 is taken by another host
  nginx. Use **http://localhost:3000**, never `http://localhost`.
- A failed backend build leaves the **old container running**. Don't assume a rebuild succeeded
  because the site loads.

Wait for the backend to be up:

```powershell
curl http://localhost:8080/actuator/health
```

You want `{"status":"UP"}`. It can take ~30 s after the container starts.

## 1.2 Check the bucket exists

```powershell
docker compose ps createbuckets
```

The `createbuckets` job creates `blog-media` and sets it to public download. If it did not run,
uploads succeed but the images 403 in the browser. MinIO console: http://localhost:9001
(`minioadmin` / `minioadmin`).

## 1.3 Point the tools at local

```powershell
cd tools\content-import
$env:LB_API_BASE       = "http://localhost:8080"
$env:LB_ADMIN_USER     = "admin"
$env:LB_ADMIN_PASSWORD = "admin123"
```

`admin/admin123` is seeded automatically by `AdminUserInitializer` on the dev profile whenever
`app_user` is empty. It never happens on prod.

## 1.4 Dry run — proves every file is on disk, writes nothing

```powershell
python3 load.py --assets ..\..\..\Assets --dry-run
```

Expect `would upload 260 files`, `would create 73 blog posts`, `all referenced files present`.

## 1.5 Upload the images

```powershell
python3 load.py --assets ..\..\..\Assets --media-only
```

260 files, 75 MB. Resumable — every upload is recorded in `.load-state-localhost-8080.json` the
moment it succeeds, so if it dies you re-run the same command and it carries on.

## 1.6 Create three posts and look at them

```powershell
python3 load.py --assets ..\..\..\Assets --limit 3
```

Then **look** before doing the other 70:

- http://localhost:3000/admin → log in `admin` / `admin123` → **Blog**. Three rows, PUBLISHED.
- http://localhost:3000/tc/blog → the public list. Check the cover image renders (that proves
  MinIO, the bucket policy and `STORAGE_PUBLIC_BASE_URL` all line up), and that the reading time
  and service chip appear.
- Click into one → http://localhost:3000/tc/blog/2024-01-14 — check the gallery and the body.
- Switch language: `/en/blog/2024-01-14` and `/sc/blog/2024-01-14`.

If the covers are broken here, stop — do not run prod. It means the bucket is not public or
`STORAGE_PUBLIC_BASE_URL` is wrong, and prod has the same failure mode with a worse blast radius.

## 1.7 Load the rest

```powershell
python3 load.py --assets ..\..\..\Assets
```

The three from step 1.6 are skipped, not duplicated.

## 1.8 Verify

```powershell
docker compose exec postgres psql -U lucabridge -d lucabridge -c "SELECT count(*) FROM blog; SELECT count(*) FROM blog_gallery; SELECT count(*) FROM blog WHERE cover_media_id IS NULL; SELECT count(*) FROM event;"
```

Expect **73 / 260 / 0 / 0**. That last one matters: nothing should land in `event`.

Then browse http://localhost:3000/tc/blog and page through. Check one of the seven posts with an
編按 note renders it as the final paragraph — e.g. `/tc/blog/2026-05-31`.

## Starting local over

```powershell
docker compose down -v          # wipes the Postgres and MinIO volumes
del .load-state-localhost-8080.json
.\run-local.bat
```

---

# PART 2 — Azure prod

Prod is `https://lucabridge.org.hk`, namespace `lucabridge`, images from
`lucabridgeacr.azurecr.io`. Postgres and MinIO both run **inside** the cluster.

The ingress routes `/api` to the backend, so **the loader runs from your laptop over the public
URL** — no `kubectl port-forward`, no database credentials, no MinIO keys. That is the whole
reason Route A exists.

## 2.1 STOP — three pre-flight checks

**The repo's `k8s-aks/` files are not necessarily what is running.** Check the live objects.

### (a) `STORAGE_PUBLIC_BASE_URL` — the one that will ruin the import

```powershell
kubectl get configmap backend-config -n lucabridge -o yaml | Select-String STORAGE_PUBLIC_BASE_URL
```

It **must** be `https://lucabridge.org.hk` (or `.../blog-media`) and **must not** be
`http://minio:9000`.

This value is baked into `media.url` **at upload time**. If it is still the in-cluster address,
all 260 rows get a URL no browser can reach and every image on the live site 404s — and fixing
the config afterwards does not repair the rows. Fix it *before* uploading anything.

### (b) The `/blog-media` ingress path

```powershell
kubectl get ingress lucabridge -n lucabridge -o yaml | Select-String "blog-media"
```

If it is missing, images 404 even with the right base URL. The repo's `50-ingress.yaml` has it;
the live object historically did not.

If either (a) or (b) needs changing, patch the live object and restart the backend:

```powershell
kubectl rollout restart deploy/backend -n lucabridge
kubectl rollout status  deploy/backend -n lucabridge
```

**Do not `kubectl apply -f 30-backend.yaml`.** It carries a placeholder `backend-secret` that
would overwrite the real `JWT_SECRET` and `APP_ADMIN_PASSWORD_HASH` and lock every admin out.

### (c) Disk for MinIO

```powershell
kubectl get pvc -n lucabridge
```

75 MB of images is going into MinIO's PVC. Confirm there is room.

## 2.1b Does a prod admin actually exist?

`AdminUserInitializer` seeds the admin **only** from `APP_ADMIN_PASSWORD_HASH`. There is no dev
fallback on prod: if that value is blank, no admin is created and admin login is disabled
entirely. It also only runs when the username is missing, so it never resets a changed password.

```powershell
kubectl exec -n lucabridge statefulset/postgres -- psql -U lucabridge -d lucabridge -c "SELECT id, username, is_active FROM app_user;"
kubectl logs deploy/backend -n lucabridge | Select-String "admin"
```

In the log you want `Seeded bootstrap admin 'admin'`. If you see
`No CMS admin exists and APP_ADMIN_PASSWORD_HASH is unset`, that is the answer.

To create one: generate a BCrypt hash, put it in the secret, restart.

```powershell
python -m pip install bcrypt
python -c "import bcrypt;print(bcrypt.hashpw(b'YOUR-PASSWORD', bcrypt.gensalt(10)).decode())"

$h = "<the $2b$... hash>"
$b64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($h))
kubectl patch secret backend-secret -n lucabridge -p "{\"data\":{\"APP_ADMIN_PASSWORD_HASH\":\"$b64\"}}"
kubectl rollout restart deploy/backend -n lucabridge
kubectl rollout status  deploy/backend -n lucabridge
```

`kubectl patch` merges, so the other keys in the secret (`JWT_SECRET` above all) are untouched.
**Never `kubectl apply -f 30-backend.yaml`** — its commented-out Secret block exists precisely
because applying it would overwrite the real values and lock everyone out.

If an `admin` row already exists with a password nobody knows, the initializer will not touch it.
Either delete that row and restart, or set a new hash directly:

```sql
UPDATE app_user SET password_hash = '<new bcrypt hash>' WHERE username = 'admin';
```

## 2.2 Point the tools at prod

```powershell
$env:LB_API_BASE       = "https://lucabridge.org.hk"
$env:LB_ADMIN_USER     = "admin"
$env:LB_ADMIN_PASSWORD = "<the real prod admin password>"
```

The prod password is whatever you generated `APP_ADMIN_PASSWORD_HASH` from. There is no dev
fallback on prod — `admin123` will not work.

**Use a new terminal**, or you will still be holding the local values.

## 2.3 Dry run, then media, then three, then look

Exactly the same sequence as local. The state file is keyed by host
(`.load-state-lucabridge.org.hk.json`), so local progress is not confused with prod.

```powershell
python3 load.py --assets ..\..\..\Assets --dry-run
python3 load.py --assets ..\..\..\Assets --media-only
python3 load.py --assets ..\..\..\Assets --limit 3
```

Now look at https://lucabridge.org.hk/tc/blog and confirm the covers load over HTTPS. **This is
the checkpoint that catches a wrong `STORAGE_PUBLIC_BASE_URL` after 3 posts instead of 73.**

```powershell
python3 load.py --assets ..\..\..\Assets
```

## 2.4 Verify prod

```powershell
kubectl exec -n lucabridge statefulset/postgres -- psql -U lucabridge -d lucabridge -c "SELECT count(*) FROM blog; SELECT count(*) FROM blog_gallery; SELECT count(*) FROM event;"
```

Expect **73 / 260 / 0**. Then browse the live site in all three languages.

## 2.5 Watch the cost

The cluster is on 2 nodes and the grant is already burning faster than planned. This import adds
no pods and no CPU — only ~75 MB of PVC — but check the node count did not move:

```powershell
kubectl get nodes
kubectl top nodes
```

Also confirm UAT is still parked at 0. Any push to `main` re-applies `uat/uat.yaml`, and if that
file ever says `replicas: 1` it silently pulls the cluster back to two nodes' worth of load:

```powershell
kubectl get deploy -n lucabridge-uat
```

---

# If something goes wrong

**413 Request Entity Too Large on upload.** nginx-ingress defaults to a 1 MB body. The largest
file here is 879 KB so it should pass, but if it bites:

```powershell
kubectl annotate ingress lucabridge -n lucabridge nginx.ingress.kubernetes.io/proxy-body-size=8m --overwrite
```

**401 on login.** Wrong password, or the user lacks ADMIN/EDITOR. Check with
`curl -u ... https://lucabridge.org.hk/api/auth/login`.

**Upload dies partway.** Re-run the same command. The state file resumes it.

**Images upload but show broken.** `STORAGE_PUBLIC_BASE_URL` — see 2.1(a). The already-created
`media` rows carry the bad URL and must be re-created, so fix the config, then:

```sql
DELETE FROM blog_gallery; DELETE FROM blog_text; DELETE FROM blog;
DELETE FROM media;
```

delete `.load-state-<host>.json`, and start again from `--media-only`. (MinIO objects are
orphaned by that, and the CMS "Sweep unused" clears them.)

## Rolling back everything

```sql
DELETE FROM blog_gallery WHERE blog_id IN (SELECT id FROM blog WHERE slug ~ '^\d{4}-\d{2}-\d{2}');
DELETE FROM blog_text    WHERE blog_id IN (SELECT id FROM blog WHERE slug ~ '^\d{4}-\d{2}-\d{2}');
DELETE FROM blog         WHERE slug ~ '^\d{4}-\d{2}-\d{2}';
```

Media rows and MinIO objects survive that deliberately, so re-running costs nothing. Delete
`.load-state-<host>.json` if you also want the images re-uploaded.

---

# Route B — the SQL seed, if you cannot reach the API

`backend/src/main/resources/db/seed/posts_import.sql`. It still needs step 1.5 / 2.3
(`--media-only`) first, because **SQL cannot upload to MinIO**.

Local:
```powershell
docker compose cp backend\src\main\resources\db\seed\posts_import.sql postgres:/tmp/posts.sql
docker compose exec postgres psql -U lucabridge -d lucabridge -f /tmp/posts.sql
```

Prod:
```powershell
kubectl cp backend\src\main\resources\db\seed\posts_import.sql lucabridge/<postgres-pod>:/tmp/posts.sql
kubectl exec -n lucabridge <postgres-pod> -- psql -U lucabridge -d lucabridge -f /tmp/posts.sql
```

**Never `psql < file` in PowerShell.** `<` is a reserved operator, and `Get-Content | psql`
re-encodes to ASCII on PS 5.1 — every Chinese character becomes a question mark. Copy the file in
and use `-f`, as `home_blocks.sql` says.

Images resolve by `media.file_name` and services by `service.code`, never by id, so the same file
loads into either environment. It refuses to run if `media` is empty, is safe to run twice, and
reports how many posts ended up without a cover or a service tag.

Verified against a real PostgreSQL 16 with V1–V5 applied: 73 blog, 73 blog_text, 260
blog_gallery, all with a cover and a service tag, `event` count 0.

---

# Regenerating posts.json

Only if you change the source material or a script. `extract.py` rebuilds from scratch, so
everything after it must re-run:

```powershell
python3 extract.py --assets ..\..\..\Assets
python3 posters.py --assets ..\..\..\Assets
python3 fixups.py  --assets ..\..\..\Assets
python3 enrich.py
python3 apply_translations.py
python3 verify.py  ..\..\..\Assets      # must print: failures : 0
```

`verify.py` re-opens every source `.docx` and requires the stored Chinese to be an exact
contiguous substring of it. It is what enforces "never alter a Chinese character".

If you edited any Chinese, `apply_translations.py` will refuse the now-stale Simplified text and
tell you to re-run `sc_convert.py` (`pip install opencc-python-reimplemented`).

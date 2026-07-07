# 樂橋 LucaBridge — Website

Trilingual (EN / 繁中 / 简中) public website for LucaBridge, a Hong Kong community &
environmental charity. Server-side rendered React, Spring Boot API, PostgreSQL, MinIO.

Full specs live in Notion (project hub → 01–06). This README covers local setup only.

## Architecture (one box, SSR)

```
browser ──> nginx :80 ──┬──> frontend (React Router v7 SSR, Node :3000) ──> backend API
                        └──> /api ──> backend (Spring Boot :8080) ──> PostgreSQL, MinIO
monitoring: Prometheus :9090 scrapes /actuator/prometheus ──> Grafana :3001
CI/CD: Jenkins (test → build images → deploy compose stack); GitHub Actions = PR test gate
```

Pages are rendered per request from live DB data, so crawlers and WhatsApp/Facebook
link scrapers get complete HTML (title/OG/hreflang) — no rebuild step on publish, and
Jenkins is never in the serving path.

## Stack

- **Frontend:** React Router v7 framework mode (`ssr: true`), served by `@react-router/serve`.
  Loaders fetch from the API server-side; `meta` exports handle SEO/OG. No client data
  library needed — the router is the data layer.
- **Backend:** Spring Boot 3 (Java 21), Spring Data JPA, Spring Security (JWT), Flyway,
  Micrometer → Prometheus.
- **DB:** PostgreSQL 16. Schema in `db/migration`, dev-only demo seed in `db/seed`
  (the seed location is simply not on prod's Flyway path — it cannot run there).
- **Images:** MinIO (S3-compatible) — swap `STORAGE_ENDPOINT` for any S3-compatible
  store in prod, no code change.
- **Infra:** Docker Compose profiles (see below), nginx reverse proxy with SSR
  micro-cache, Prometheus + Grafana.

## Local development (hot reload)

Prerequisites: Docker, JDK 21, Node 20+.

```bash
docker compose up -d                       # postgres :5432, minio :9000/:9001
cd backend && ./mvnw spring-boot:run       # :8080 — Flyway runs schema + dev seed
cd frontend && npm install && npm run dev  # :5173 — set API_URL if backend isn't :8080
```

MinIO console at http://localhost:9001 (minioadmin/minioadmin) — create bucket
`blog-media` before testing uploads.

## Full stack (prod-shaped)

```bash
docker compose --profile app up -d --build       # site at http://localhost
docker compose --profile monitoring up -d        # prometheus :9090, grafana :3001 (admin/admin)
```

On a real server set env first: `SPRING_PROFILE=prod`, `JWT_SECRET` (required — prod
refuses to start on the default), `APP_ADMIN_PASSWORD_HASH`, `DB_PASSWORD`, `SITE_ORIGIN`.

## Repo layout

```
LucaBridge/
  docker-compose.yml        # profiles: (default) dev deps | app | monitoring
  Jenkinsfile               # CI + deploy (Jenkins owns CD)
  .github/workflows/ci.yml  # PR test gate only
  infra/                    # nginx.conf, prometheus.yml, grafana provisioning
  backend/                  # Spring Boot + Dockerfile
  frontend/                 # React Router v7 SSR app + Dockerfile
    app/routes/             # route modules (loader + meta + component)
    app/i18n/               # lean dictionary i18n (lang always from URL)
```

## Security notes

- Login: 401 on bad credentials, in-memory rate limit (10 attempts / 5 min / IP).
- JWT secret: validated at startup in prod (`JwtSecretValidator`).
- Uploads: images only (JPEG/PNG/WebP/GIF/AVIF), 10MB cap.
- `/actuator` is never proxied by nginx — metrics stay on the Docker network.

## What's built vs TODO

**Built:** SSR public site (Home / Blog index with category+pagination / Post with
gallery, lightbox, press links, prev-next), full SEO (server-rendered meta, OG,
hreflang, live sitemap.xml + robots.txt), trilingual routing with 繁中 fallback,
public read API, JWT login, admin write endpoints, Docker/nginx/monitoring infra,
Jenkins CD + Actions test gate.

**TODO (Phase 4–5 per Coding Plan):**
- Admin SPA (login screen, post editor, drag-drop upload) — endpoints exist, nothing calls them yet.
- Set `APP_ADMIN_PASSWORD_HASH` (BCrypt) to enable admin login.
- Grafana dashboard JSON provisioning (JVM + HTTP dashboards are two clicks to import: 4701, 6756).
- TLS (certbot or Caddy) once the client's domain exists.

## Accent color — pending client decision

Defaults to 1A Warm Clay (`#b85c3c`) in exactly two places:
`frontend/app/theme/tokens.js` and `frontend/app/theme/global.css`.
Update both once the client picks (1A / 1B / 1C — see Notion "02 · Design System").

## Pushing to GitHub

```bash
./push-to-github.sh <repo-name>   # requires `gh auth login` first
```

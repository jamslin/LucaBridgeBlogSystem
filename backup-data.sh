#!/usr/bin/env bash
# Snapshot everything typed into the CMS: the database (posts, events, jobs,
# services, company record, home blocks, users) and the MinIO bucket (the actual
# logo / banner / gallery image files). Both halves are needed — the database only
# stores the URL of an image, never the image itself.
#
#   ./backup-data.sh            -> backups/latest/   (overwritten each run)
#   ./backup-data.sh before-v6  -> backups/before-v6/
set -euo pipefail
cd "$(dirname "$0")"

NAME="${1:-latest}"
BUCKET="${STORAGE_BUCKET:-blog-media}"

if ! docker compose ps --status running --services | grep -qx postgres; then
  echo "[X] Postgres is not running. Start the stack first, then run this again." >&2
  exit 1
fi

echo "Backing up to backups/$NAME"
mkdir -p "backups/$NAME"

echo "[1/2] Database..."
docker compose exec -T postgres pg_dump -U lucabridge -Fc -f "/backups/$NAME/db.dump" lucabridge

echo "[2/2] Media files..."
docker compose --profile tools run --rm mc \
  "mc mirror --overwrite --remove local/$BUCKET /backups/$NAME/media"

echo
echo "Done. Restore with:  ./restore-data.sh $NAME"

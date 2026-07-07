#!/usr/bin/env bash
# sync-wix-images.sh — download the original LucaBridge images off Wix and push
# them into MinIO under blog-media/wix/. Run on YOUR machine (needs the MinIO
# stack up). Idempotent: re-running overwrites the same keys.
#
# Prereqs:
#   - docker compose up -d   (MinIO reachable at $MINIO_ENDPOINT)
#   - MinIO client `mc`       (https://min.io/docs/minio/linux/reference/minio-mc.html)
#   - curl
#
# The seed (V4) references keys like blog-media/wix/02a841_02d65a97.jpg — the short
# names below MUST match those. We fetch the full-resolution original by using the
# bare Wix media id (no /v1/fill/... transform).
set -euo pipefail

MINIO_ALIAS="${MINIO_ALIAS:-local}"
MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:9000}"
MINIO_KEY="${MINIO_KEY:-minioadmin}"
MINIO_SECRET="${MINIO_SECRET:-minioadmin}"
BUCKET="${BUCKET:-blog-media}"
PREFIX="wix"
WIX="https://static.wixstatic.com/media"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# shortname  <TAB>  full Wix media id (~mv2.jpg)
MANIFEST=$(cat <<'EOF'
02a841_9edea948	02a841_9edea9488aa04bb8980d556348b8e6a9~mv2.jpg
02a841_9a6ab234	02a841_9a6ab23499454861b4a632712050a7a8~mv2.jpg
02a841_02d65a97	02a841_02d65a97ebb84ddeb6a33828b6ee1bad~mv2.jpg
02a841_ddc14c59	02a841_ddc14c596f4147339ee713efcae24a3a~mv2.jpg
02a841_ea19fbf6	02a841_ea19fbf68f9847b8ab6c5ecf58a695a8~mv2.jpg
02a841_ce489b52	02a841_ce489b52ec26452abdfd2aafbc5ddf4e~mv2.jpg
02a841_730abf01	02a841_730abf01079947569ad1d1f024e7b027~mv2.jpg
02a841_a0712079	02a841_a071207950244038ad379142aa0c38dc~mv2.jpg
02a841_b705ec69	02a841_b705ec69148a41cdbdd996edea4e017e~mv2.jpg
02a841_fa7bda0e	02a841_fa7bda0e231b424cb09d722450810159~mv2.jpg
02a841_dd10da57	02a841_dd10da5720214812a33d729093fdd2f3~mv2.jpg
02a841_9696824c	02a841_9696824c83d54f8b8f62fe89a669391e~mv2.jpg
8cb765_bb2e693c	8cb765_bb2e693cc9b34676b7fd790855865548~mv2.jpg
8cb765_22194d28	8cb765_22194d28108c4138bbf64b1650f5432f~mv2.jpg
8cb765_ec4f9760	8cb765_ec4f9760a94a425bb30152f51997ca24~mv2.jpg
8cb765_957a2a12	8cb765_957a2a1285e449c7849460f909a45a78~mv2.jpg
8cb765_746c3f6d	8cb765_746c3f6dcbd54f3995723a48184e2611~mv2.jpg
8cb765_317b4ecb	8cb765_317b4ecbddc64f6499c275cc2cbe2413~mv2.jpg
2a1a02_84cfbe35	2a1a02_84cfbe3521da4e34a5a002237fdbb811~mv2.jpg
EOF
)

echo "→ Configuring mc alias '$MINIO_ALIAS' → $MINIO_ENDPOINT"
mc alias set "$MINIO_ALIAS" "$MINIO_ENDPOINT" "$MINIO_KEY" "$MINIO_SECRET" >/dev/null
mc mb --ignore-existing "$MINIO_ALIAS/$BUCKET" >/dev/null
# Public read so the SSR frontend can hotlink the images
mc anonymous set download "$MINIO_ALIAS/$BUCKET" >/dev/null || true

count=0
while IFS=$'\t' read -r short id; do
  [ -z "$short" ] && continue
  echo "  ↓ $id"
  curl -fsSL "$WIX/$id" -o "$TMP/$short.jpg"
  mc cp "$TMP/$short.jpg" "$MINIO_ALIAS/$BUCKET/$PREFIX/$short.jpg" >/dev/null
  count=$((count+1))
done <<< "$MANIFEST"

echo "✓ Synced $count images to $MINIO_ALIAS/$BUCKET/$PREFIX/"
echo "  Verify: $MINIO_ENDPOINT/$BUCKET/$PREFIX/02a841_b705ec69.jpg"

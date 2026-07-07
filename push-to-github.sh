#!/bin/sh
# One-time helper: create the GitHub repo and push this scaffold.
# Requires: git, and the GitHub CLI (`gh`) authenticated (`gh auth login`).
#
# Usage: ./push-to-github.sh <repo-name> [--private]
set -e

REPO_NAME="${1:?Usage: ./push-to-github.sh <repo-name> [--private]}"
VISIBILITY="${2:---public}"

if [ ! -d .git ]; then
  git init
  git add -A
  git commit -m "Initial scaffold: db, backend, frontend, docker-compose, CI/CD"
fi

gh repo create "$REPO_NAME" "$VISIBILITY" --source=. --remote=origin --push

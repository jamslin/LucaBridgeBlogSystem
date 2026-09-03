@echo off
setlocal
cd /d "%~dp0"

rem Snapshot everything you typed into the CMS: the database (posts, events, jobs,
rem services, company record, home blocks, users) and the MinIO bucket (the actual
rem logo / banner / gallery image files). Both halves are needed — the database
rem only stores the URL of an image, never the image itself.
rem
rem   backup-data.bat            -> backups\latest\   (overwritten each run)
rem   backup-data.bat before-v6  -> backups\before-v6\

set NAME=%~1
if "%NAME%"=="" set NAME=latest
if "%STORAGE_BUCKET%"=="" set STORAGE_BUCKET=blog-media

echo ============================================================
echo  Backing up to backups\%NAME%
echo ============================================================

docker compose ps --status running --services | findstr /x postgres >nul
if errorlevel 1 (
  echo [X] Postgres is not running. Start the stack first, then run this again.
  pause
  exit /b 1
)

if not exist "backups\%NAME%" mkdir "backups\%NAME%"

echo [1/2] Database...
docker compose exec -T postgres pg_dump -U lucabridge -Fc -f /backups/%NAME%/db.dump lucabridge
if errorlevel 1 (
  echo [X] pg_dump failed. Nothing was written.
  pause
  exit /b 1
)

echo [2/2] Media files...
docker compose --profile tools run --rm mc "mc mirror --overwrite --remove local/%STORAGE_BUCKET% /backups/%NAME%/media"
if errorlevel 1 (
  echo [X] Media mirror failed. The database dump IS saved; the images are not.
  pause
  exit /b 1
)

echo.
echo Done. Restore with:  restore-data.bat %NAME%
echo.
pause

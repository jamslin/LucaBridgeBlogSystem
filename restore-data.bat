@echo off
setlocal
cd /d "%~dp0"

rem Put a backup back. This REPLACES the current database contents and re-uploads
rem the saved image files, so run it on a stack you are happy to overwrite.
rem
rem   restore-data.bat            <- backups\latest\
rem   restore-data.bat before-v6  <- backups\before-v6\

set NAME=%~1
if "%NAME%"=="" set NAME=latest
if "%STORAGE_BUCKET%"=="" set STORAGE_BUCKET=blog-media

if not exist "backups\%NAME%\db.dump" (
  echo [X] backups\%NAME%\db.dump not found. Nothing to restore.
  pause
  exit /b 1
)

echo ============================================================
echo  Restoring backups\%NAME%
echo  This OVERWRITES the current database and media.
echo ============================================================
set /p CONFIRM=Type RESTORE to continue:
if /i not "%CONFIRM%"=="RESTORE" (
  echo Cancelled. Nothing changed.
  pause
  exit /b 1
)

docker compose ps --status running --services | findstr /x postgres >nul
if errorlevel 1 (
  echo [X] Postgres is not running. Start the stack first, then run this again.
  pause
  exit /b 1
)

echo [1/2] Database...
rem --clean --if-exists drops each object before recreating it, so restoring over a
rem populated database works. Flyway's own history table rides along in the dump,
rem which is what keeps the schema version consistent with the data.
docker compose exec -T postgres pg_restore -U lucabridge -d lucabridge --clean --if-exists --no-owner /backups/%NAME%/db.dump
if errorlevel 1 (
  echo [!] pg_restore reported errors. These are often harmless "does not exist"
  echo     warnings from --clean on a fresh database. Check the site before panicking.
)

echo [2/2] Media files...
docker compose --profile tools run --rm mc "mc mirror --overwrite /backups/%NAME%/media local/%STORAGE_BUCKET%"
if errorlevel 1 (
  echo [X] Media mirror failed. The database is restored; the images are not.
  pause
  exit /b 1
)

echo.
echo Done. Restart the backend so it picks the data up cleanly:
echo   docker compose restart backend
echo.
pause

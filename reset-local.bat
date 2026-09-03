@echo off
setlocal
cd /d "%~dp0"

rem The only script that destroys data. Separated from stop-local.bat on purpose:
rem `docker compose down -v` used to be advertised there as a footnote, one keystroke
rem away from wiping every post and uploaded image.

echo ============================================================
echo  WARNING - this DELETES the database and every uploaded
echo  image (posts, events, logo, banners, galleries).
echo ============================================================
echo.
echo  Take a backup first if you have not:  backup-data.bat
echo.
set /p CONFIRM=Type WIPE to continue:
if /i not "%CONFIRM%"=="WIPE" (
  echo Cancelled. Nothing was deleted.
  pause
  exit /b 1
)

docker compose --profile app --profile monitoring --profile tools down -v
echo.
echo Volumes removed. Next run-local.bat starts from an empty database.
pause

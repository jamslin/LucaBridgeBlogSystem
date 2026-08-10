@echo off
setlocal
cd /d "%~dp0"
echo ============================================================
echo  LucaBridge - starting local stack
echo  (Postgres + MinIO + Spring backend + SSR frontend)
echo ============================================================
echo.

docker info >nul 2>&1
if errorlevel 1 (
  echo [X] Docker does not appear to be running.
  echo     Start Docker Desktop, wait for it to say "Engine running", then run this again.
  echo.
  pause
  exit /b 1
)

echo Building and starting containers (first run takes a few minutes)...
docker compose --profile app up -d --build backend frontend
if errorlevel 1 (
  echo.
  echo [X] docker compose failed. See the messages above.
  pause
  exit /b 1
)

echo.
echo Waiting a few seconds for the backend to boot...
timeout /t 8 >nul
start "" http://localhost:3000

echo.
echo ------------------------------------------------------------
echo  Site : http://localhost:3000   (redirects to /zh-Hant)
echo  API  : http://localhost:8080/api/jobs
echo  Logs : docker compose logs -f backend frontend
echo  Stop : stop-local.bat
echo ------------------------------------------------------------
echo  If the page shows an error, wait ~30s for the backend to
echo  finish starting, then refresh.
echo.
pause

@echo off
cd /d "%~dp0"
echo Stopping LucaBridge containers...
docker compose down
echo Done. (Use "docker compose down -v" to also wipe the database volume.)
pause

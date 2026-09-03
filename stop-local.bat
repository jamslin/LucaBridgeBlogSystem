@echo off
cd /d "%~dp0"
echo Stopping LucaBridge containers...
docker compose down
echo.
echo Done. Your posts, images and settings are SAFE - they live in named
echo volumes and survive this. Start again with run-local.bat.
echo.
echo To wipe the database and media on purpose, use reset-local.bat.
pause

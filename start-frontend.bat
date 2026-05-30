@echo off
echo ========================================
echo  Start Frontend - open-admin-process
echo ========================================
echo.

cd /d "%~dp0web"

echo [INFO] Starting Umi dev server...
echo [INFO] Proxy backend: http://127.0.0.1:8082
echo.

npm run dev

pause
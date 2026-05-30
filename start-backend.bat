@echo off
echo ========================================
echo  Start Backend - open-admin-process
echo ========================================
echo.

cd /d "%~dp0"

echo [INFO] Compiling and starting Spring Boot backend...
echo [INFO] Port: 8082  context-path: /process
echo.

mvnw spring-boot:run -Plocal

pause
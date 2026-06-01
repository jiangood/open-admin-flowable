@echo off
echo ========================================
echo  Start Backend - open-admin-process
echo ========================================
echo.

cd /d "%~dp0"

echo [INFO] Step 1: Clean and compile...
call mvnw clean install -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed, aborting.
    pause
    exit /b %errorlevel%
)
echo [INFO] Compilation successful.
echo.

echo [INFO] Step 2: Starting Spring Boot backend...
echo [INFO] Port: 8082  context-path: /process
echo.

mvnw spring-boot:run -pl open-admin-flowable-example

pause
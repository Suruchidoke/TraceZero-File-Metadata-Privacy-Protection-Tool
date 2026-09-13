@echo off
title TraceZero - File Metadata Privacy Protection Firewall
cd /d "%~dp0"

echo ========================================================
echo    TRACEZERO: File Metadata Privacy Protection Tool
echo ========================================================
echo.
echo Launching TraceZero Desktop Application...
echo.

if exist "target\TraceZero-1.0-SNAPSHOT.jar" (
    start "" javaw -jar "target\TraceZero-1.0-SNAPSHOT.jar"
    if %ERRORLEVEL% EQU 0 (
        echo [OK] TraceZero is now running!
        exit /b 0
    )
)

echo [INFO] Standalone JAR not found or javaw unavailable. Launching via Maven...
call .\mvnw.cmd javafx:run
pause

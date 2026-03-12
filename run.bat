@echo off
REM Flappy Bird Game - Run Script (via Maven)

echo Launching Flappy Bird...
echo.

REM Run using the local Maven wrapper
".\maven\apache-maven-3.9.6\bin\mvn" clean javafx:run

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Game failed to launch!
    pause
    exit /b 1
)

pause

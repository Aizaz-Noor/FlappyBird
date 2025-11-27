@echo off
REM Flappy Bird Game - Build and Run Script

REM Set your JavaFX SDK path here
SET JAVAFX_PATH=javafx-sdk-23.0.1\lib

REM Check if JavaFX path exists
IF NOT EXIST "%JAVAFX_PATH%" (
    echo ERROR: JavaFX SDK not found at %JAVAFX_PATH%
    echo.
    echo Please download JavaFX SDK from: https://gluonhq.com/products/javafx/
    echo Then update the JAVAFX_PATH variable in this script.
    echo.
    pause
    exit /b 1
)

echo Compiling Flappy Bird Game...
javac --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics,javafx.media -d out src\*.java

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Launching Flappy Bird Game...
echo.
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics,javafx.media -cp out FlappyBirdGame

pause

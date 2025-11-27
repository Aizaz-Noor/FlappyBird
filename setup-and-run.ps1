# PowerShell script to download and setup JavaFX for Flappy Bird Game

$javafxVersion = "23.0.1"
$javafxUrl = "https://download2.gluonhq.com/openjfx/$javafxVersion/openjfx-$javafxVersion_windows-x64_bin-sdk.zip"
$downloadPath = "$env:TEMP\javafx-sdk.zip"
$extractPath = ".\javafx-sdk-$javafxVersion"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Flappy Bird - JavaFX Setup Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Check if JavaFX already exists
if (Test-Path $extractPath) {
    Write-Host "JavaFX SDK already exists at: $extractPath" -ForegroundColor Green
    Write-Host "Skipping download..." -ForegroundColor Yellow
}
else {
    Write-Host "Downloading JavaFX SDK $javafxVersion..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $javafxUrl -OutFile $downloadPath -UseBasicParsing
        Write-Host "Download complete!" -ForegroundColor Green
        
        Write-Host "Extracting JavaFX SDK..." -ForegroundColor Yellow
        Expand-Archive -Path $downloadPath -DestinationPath "." -Force
        Remove-Item $downloadPath
        Write-Host "Extraction complete!" -ForegroundColor Green
    }
    catch {
        Write-Host "Error downloading JavaFX: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "Please manually download JavaFX from:" -ForegroundColor Yellow
        Write-Host "https://gluonhq.com/products/javafx/" -ForegroundColor Cyan
        pause
        exit 1
    }
}

Write-Host ""
Write-Host "Compiling Flappy Bird Game..." -ForegroundColor Yellow

$javafxLib = "$extractPath\lib"
$compileCmd = "javac --module-path `"$javafxLib`" --add-modules javafx.controls,javafx.graphics -d out src\*.java"

Invoke-Expression $compileCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Launching Flappy Bird Game..." -ForegroundColor Cyan
    Write-Host ""
    
    $runCmd = "java --module-path `"$javafxLib`" --add-modules javafx.controls,javafx.graphics -cp out FlappyBirdGame"
    Invoke-Expression $runCmd
}
else {
    Write-Host "Compilation failed!" -ForegroundColor Red
    pause
}

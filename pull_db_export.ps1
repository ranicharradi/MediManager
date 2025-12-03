# MediManager Database Export Puller
# This script pulls the latest database export from the emulator to your desktop.

$sourcePath = "/storage/emulated/0/Android/data/com.example.medimanager/files/MediManager_DB_Export/latest_export.txt"
$destPath = "$env:USERPROFILE\Desktop\medimanager_db_export.txt"

Write-Host "Pulling database export from emulator..." -ForegroundColor Cyan

# Check if adb is available
$adbCheck = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adbCheck) {
    Write-Host "ERROR: adb not found in PATH. Please ensure Android SDK platform-tools is in your PATH." -ForegroundColor Red
    exit 1
}

# Check if device/emulator is connected
$devices = adb devices | Select-String "device$"
if (-not $devices) {
    Write-Host "ERROR: No device/emulator connected. Please start your emulator first." -ForegroundColor Red
    exit 1
}

# Pull the file
adb pull $sourcePath $destPath 2>$null

if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS: Database export saved to: $destPath" -ForegroundColor Green
    Write-Host ""
    Write-Host "--- Export Preview (first 50 lines) ---" -ForegroundColor Yellow
    Get-Content $destPath -TotalCount 50
} else {
    Write-Host "ERROR: Failed to pull file. The export may not exist yet." -ForegroundColor Red
    Write-Host "Make sure the app has exported the database (add a patient or register a user)." -ForegroundColor Yellow
}

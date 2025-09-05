# Check APK and POI JAR file sizes

Write-Host "===== File Size Check ====="
Write-Host ""

# Check Release APK
$apkPath = "app\build\outputs\apk\release\app-release.apk"
if (Test-Path $apkPath) {
    $apkFile = Get-Item $apkPath
    $apkSizeMB = [math]::Round($apkFile.Length / 1MB, 2)
    $apkSizeKB = [math]::Round($apkFile.Length / 1KB, 0)
    
    Write-Host "Release APK:"
    Write-Host "  Path: $apkPath"
    Write-Host "  Size: $($apkFile.Length) bytes"
    Write-Host "  Size: $apkSizeKB KB"
    Write-Host "  Size: $apkSizeMB MB"
} else {
    Write-Host "ERROR: Release APK not found"
}

Write-Host ""

# Check POI relocated JAR
$poiJarPath = "app\build\relocated\app-poi-relocated.jar"
if (Test-Path $poiJarPath) {
    $poiFile = Get-Item $poiJarPath
    $poiSizeMB = [math]::Round($poiFile.Length / 1MB, 2)
    $poiSizeKB = [math]::Round($poiFile.Length / 1KB, 0)
    
    Write-Host "POI Relocated JAR:"
    Write-Host "  Path: $poiJarPath"
    Write-Host "  Size: $($poiFile.Length) bytes"
    Write-Host "  Size: $poiSizeKB KB"
    Write-Host "  Size: $poiSizeMB MB"
} else {
    Write-Host "ERROR: POI relocated JAR not found"
}

Write-Host ""
Write-Host "Note: To calculate accurate APK size increase, compare with baseline version without POI"
# FastExcel依赖诊断脚本
Write-Host "=== FastExcel依赖诊断工具 ===" -ForegroundColor Cyan
Write-Host ""

# 1. 检查网络连接
Write-Host "1. 测试网络连接到Maven Central..." -ForegroundColor Yellow
$pomUrl = "https://repo1.maven.org/maven2/cn/idev/excel/fastexcel/1.2.0/fastexcel-1.2.0.pom"
$jarUrl = "https://repo1.maven.org/maven2/cn/idev/excel/fastexcel/1.2.0/fastexcel-1.2.0.jar"

try {
    $response = Invoke-WebRequest -Uri $pomUrl -Method Head -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✓ POM文件存在" -ForegroundColor Green
        Write-Host "  大小: $($response.Headers.'Content-Length') bytes"
    }
} catch {
    Write-Host "✗ 无法访问POM文件" -ForegroundColor Red
    Write-Host "  错误: $_"
}

try {
    $response = Invoke-WebRequest -Uri $jarUrl -Method Head -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✓ JAR文件存在" -ForegroundColor Green
        Write-Host "  大小: $([math]::Round($response.Headers.'Content-Length' / 1MB, 2)) MB"
    }
} catch {
    Write-Host "✗ 无法访问JAR文件" -ForegroundColor Red
    Write-Host "  错误: $_"
}

Write-Host ""

# 2. 检查Gradle环境
Write-Host "2. 检查Gradle环境..." -ForegroundColor Yellow
try {
    $gradleVersion = gradle -v 2>$null | Select-String "Gradle" | Select-Object -First 1
    Write-Host "✓ $gradleVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Gradle未安装或不在PATH中" -ForegroundColor Red
}

Write-Host ""

# 3. 检查代理设置
Write-Host "3. 检查代理设置..." -ForegroundColor Yellow
$httpProxy = [System.Environment]::GetEnvironmentVariable("HTTP_PROXY")
$httpsProxy = [System.Environment]::GetEnvironmentVariable("HTTPS_PROXY")

if ($httpProxy) {
    Write-Host "  HTTP_PROXY: $httpProxy" -ForegroundColor Cyan
} else {
    Write-Host "  HTTP_PROXY: 未设置" -ForegroundColor Gray
}

if ($httpsProxy) {
    Write-Host "  HTTPS_PROXY: $httpsProxy" -ForegroundColor Cyan
} else {
    Write-Host "  HTTPS_PROXY: 未设置" -ForegroundColor Gray
}

Write-Host ""

# 4. 检查Gradle缓存
Write-Host "4. 检查Gradle缓存..." -ForegroundColor Yellow
$gradleCache = "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\cn.idev.excel"
if (Test-Path $gradleCache) {
    Write-Host "✓ 发现缓存目录: $gradleCache" -ForegroundColor Green
    Get-ChildItem -Path $gradleCache -Recurse | ForEach-Object {
        Write-Host "  - $($_.Name)" -ForegroundColor Gray
    }
} else {
    Write-Host "✗ 未发现缓存" -ForegroundColor Red
}

Write-Host ""

# 5. 生成测试脚本
Write-Host "5. 生成测试命令..." -ForegroundColor Yellow
Write-Host "请在项目根目录执行以下命令：" -ForegroundColor Cyan
Write-Host ""
Write-Host "# 清理并重新下载依赖" -ForegroundColor Gray
Write-Host "gradle clean build --refresh-dependencies --info" -ForegroundColor White
Write-Host ""
Write-Host "# 查看依赖树" -ForegroundColor Gray
Write-Host "gradle :app:dependencies --configuration implementation | findstr fastexcel" -ForegroundColor White
Write-Host ""
Write-Host "# 强制重新解析" -ForegroundColor Gray
Write-Host "gradle --stop" -ForegroundColor White
Write-Host "gradle clean build --no-build-cache --refresh-dependencies" -ForegroundColor White

Write-Host ""
Write-Host "=== 诊断完成 ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "如果依赖仍无法解析，请尝试：" -ForegroundColor Yellow
Write-Host "1. 检查防火墙/VPN设置"
Write-Host "2. 使用阿里云Maven镜像"
Write-Host "3. 手动下载JAR文件"
Write-Host "4. 使用备选方案（EasyExcel）"

# 保持窗口打开
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
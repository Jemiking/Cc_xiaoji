@echo off
echo === FastExcel依赖修复工具 ===
echo.

echo 步骤1: 停止Gradle守护进程...
call gradle --stop
echo.

echo 步骤2: 清理Gradle缓存...
echo 清理cn.idev.excel相关缓存...
rd /s /q "%USERPROFILE%\.gradle\caches\modules-2\files-2.1\cn.idev.excel" 2>nul
echo.

echo 步骤3: 创建临时测试项目...
mkdir test-fastexcel-direct 2>nul
cd test-fastexcel-direct

echo 创建build.gradle...
(
echo apply plugin: 'java'
echo.
echo repositories {
echo     // 尝试多个仓库源
echo     mavenCentral()
echo     maven { url 'https://repo1.maven.org/maven2/' }
echo     maven { url 'https://maven.aliyun.com/repository/central' }
echo     maven { url 'https://mirrors.cloud.tencent.com/nexus/repository/maven-public/' }
echo }
echo.
echo dependencies {
echo     implementation 'cn.idev.excel:fastexcel:1.2.0'
echo }
echo.
echo task testDownload {
echo     doLast {
echo         println "Testing cn.idev.excel:fastexcel:1.2.0..."
echo         configurations.runtimeClasspath.files.each { file -^>
echo             if ^(file.name.contains^('fastexcel'^)^) {
echo                 println "SUCCESS: Found ${file.name}"
echo             }
echo         }
echo     }
echo }
) > build.gradle

echo.
echo 步骤4: 测试依赖下载...
call gradle testDownload --refresh-dependencies --info

echo.
echo 步骤5: 如果成功，复制到主项目...
if exist "%USERPROFILE%\.gradle\caches\modules-2\files-2.1\cn.idev.excel\fastexcel\1.2.0" (
    echo 依赖下载成功！
    echo.
    cd ..
    echo 请在主项目中再次尝试构建。
) else (
    echo 依赖下载失败。
    echo.
    echo 建议方案：
    echo 1. 检查网络连接和代理设置
    echo 2. 尝试手动下载：
    echo    https://repo1.maven.org/maven2/cn/idev/excel/fastexcel/1.2.0/fastexcel-1.2.0.jar
    echo 3. 使用备选方案 com.alibaba:easyexcel:3.3.4
    cd ..
)

echo.
pause
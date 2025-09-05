@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    IDE 开发环境检查工具
echo ========================================
echo.

set ERROR_COUNT=0

:: 检查Java版本
echo [1/5] 检查Java环境...
java -version 2>&1 | findstr "version" > nul
if %errorlevel% neq 0 (
    echo    ❌ Java未安装或未配置
    set /a ERROR_COUNT+=1
) else (
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
        echo    ✅ Java版本: %%i
    )
)

:: 检查JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo    ⚠️  JAVA_HOME未设置
) else (
    echo    ✅ JAVA_HOME: %JAVA_HOME%
)

echo.

:: 检查Android SDK
echo [2/5] 检查Android SDK...
if "%ANDROID_HOME%"=="" (
    if "%ANDROID_SDK_ROOT%"=="" (
        echo    ❌ ANDROID_HOME/ANDROID_SDK_ROOT未配置
        set /a ERROR_COUNT+=1
    ) else (
        echo    ✅ ANDROID_SDK_ROOT: %ANDROID_SDK_ROOT%
        set ANDROID_HOME=%ANDROID_SDK_ROOT%
    )
) else (
    echo    ✅ ANDROID_HOME: %ANDROID_HOME%
)

:: 检查Android SDK组件
if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    echo    ✅ ADB已安装
) else (
    echo    ⚠️  ADB未找到
)

if exist "%ANDROID_HOME%\build-tools\34.0.0" (
    echo    ✅ Build Tools 34.0.0已安装
) else (
    echo    ⚠️  Build Tools 34.0.0未安装
)

echo.

:: 检查Gradle
echo [3/5] 检查Gradle...
if exist "gradlew.bat" (
    echo    ✅ Gradle Wrapper已配置
    for /f "tokens=2 delims==" %%i in ('findstr "distributionUrl" gradle\wrapper\gradle-wrapper.properties') do (
        set GRADLE_VERSION=%%i
        echo    ✅ Gradle版本: !GRADLE_VERSION:*gradle-=gradle-!
    )
) else (
    echo    ❌ Gradle Wrapper未找到
    set /a ERROR_COUNT+=1
)

echo.

:: 检查Git
echo [4/5] 检查Git...
git --version > nul 2>&1
if %errorlevel% neq 0 (
    echo    ❌ Git未安装
    set /a ERROR_COUNT+=1
) else (
    for /f "tokens=3" %%i in ('git --version') do (
        echo    ✅ Git版本: %%i
    )
)

echo.

:: 检查项目配置
echo [5/5] 检查项目配置...
if exist ".idea" (
    echo    ✅ IDE配置目录存在
) else (
    echo    ⚠️  IDE配置目录不存在（首次导入正常）
)

if exist "local.properties" (
    echo    ✅ local.properties已配置
) else (
    echo    ⚠️  local.properties未配置
    echo.
    echo    创建local.properties示例:
    echo    echo sdk.dir=%ANDROID_HOME:\=/% > local.properties
)

echo.
echo ========================================

if %ERROR_COUNT% equ 0 (
    echo    ✅ 环境检查通过！
) else (
    echo    ❌ 发现 %ERROR_COUNT% 个问题需要修复
    echo.
    echo    修复建议：
    if "%JAVA_HOME%"=="" (
        echo    - 设置JAVA_HOME环境变量
    )
    if "%ANDROID_HOME%"=="" (
        echo    - 设置ANDROID_HOME环境变量
    )
    echo    - 确保所有必需工具已安装
)

echo ========================================
echo.

:: 询问是否创建local.properties
if not exist "local.properties" (
    set /p CREATE_LOCAL="是否创建local.properties文件？(Y/N): "
    if /i "!CREATE_LOCAL!"=="Y" (
        echo sdk.dir=%ANDROID_HOME:\=/% > local.properties
        echo ✅ local.properties已创建
    )
)

pause
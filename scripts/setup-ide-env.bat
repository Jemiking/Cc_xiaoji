@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    IDE 环境自动配置工具
echo ========================================
echo.

:: 检测并设置JAVA_HOME
echo [1/4] 配置Java环境...
if "%JAVA_HOME%"=="" (
    :: 尝试自动检测Java安装路径
    for /f "tokens=*" %%i in ('where java 2^>nul') do (
        set JAVA_PATH=%%i
        :: 获取Java安装目录（去掉\bin\java.exe）
        for %%j in ("!JAVA_PATH!") do (
            set JAVA_DIR=%%~dpj
            set JAVA_DIR=!JAVA_DIR:~0,-5!
        )
    )
    
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot" (
        set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot
        echo    ✅ 自动检测到Java: !JAVA_HOME!
        echo    建议将以下内容添加到系统环境变量：
        echo    JAVA_HOME=!JAVA_HOME!
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set JAVA_HOME=C:\Program Files\Java\jdk-17
        echo    ✅ 自动检测到Java: !JAVA_HOME!
        echo    建议将以下内容添加到系统环境变量：
        echo    JAVA_HOME=!JAVA_HOME!
    ) else (
        echo    ❌ 未找到Java安装，请手动安装JDK 17
        echo    推荐下载: https://adoptium.net/temurin/releases/
    )
) else (
    echo    ✅ JAVA_HOME已配置: %JAVA_HOME%
)

echo.

:: 检测并设置ANDROID_HOME
echo [2/4] 配置Android SDK...
if "%ANDROID_HOME%"=="" (
    :: 检查常见的Android SDK安装位置
    if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk" (
        set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
        echo    ✅ 自动检测到Android SDK: !ANDROID_HOME!
        echo    建议将以下内容添加到系统环境变量：
        echo    ANDROID_HOME=!ANDROID_HOME!
    ) else if exist "C:\Android\Sdk" (
        set ANDROID_HOME=C:\Android\Sdk
        echo    ✅ 自动检测到Android SDK: !ANDROID_HOME!
        echo    建议将以下内容添加到系统环境变量：
        echo    ANDROID_HOME=!ANDROID_HOME!
    ) else (
        echo    ❌ 未找到Android SDK
        echo    请通过Android Studio安装SDK或下载命令行工具：
        echo    https://developer.android.com/studio#command-tools
    )
) else (
    echo    ✅ ANDROID_HOME已配置: %ANDROID_HOME%
)

:: 检查Android SDK组件
if not "%ANDROID_HOME%"=="" (
    echo.
    echo    检查SDK组件：
    
    :: 检查platform-tools
    if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
        echo    ✅ Platform Tools已安装
    ) else (
        echo    ❌ Platform Tools未安装
        echo       运行: sdkmanager "platform-tools"
    )
    
    :: 检查build-tools
    if exist "%ANDROID_HOME%\build-tools\34.0.0" (
        echo    ✅ Build Tools 34.0.0已安装
    ) else (
        echo    ❌ Build Tools 34.0.0未安装
        echo       运行: sdkmanager "build-tools;34.0.0"
    )
    
    :: 检查平台
    if exist "%ANDROID_HOME%\platforms\android-34" (
        echo    ✅ Android 34平台已安装
    ) else (
        echo    ❌ Android 34平台未安装
        echo       运行: sdkmanager "platforms;android-34"
    )
)

echo.

:: 更新或创建local.properties
echo [3/4] 配置local.properties...
if not "%ANDROID_HOME%"=="" (
    if exist "local.properties" (
        echo    更新local.properties...
    ) else (
        echo    创建local.properties...
    )
    
    :: 将路径转换为正斜杠格式（Gradle需要）
    set SDK_PATH=%ANDROID_HOME:\=/%
    echo sdk.dir=!SDK_PATH! > local.properties
    echo    ✅ local.properties已配置
) else (
    echo    ⚠️  无法创建local.properties（未找到Android SDK）
)

echo.

:: 创建或更新环境变量配置文件
echo [4/4] 生成环境变量配置...
(
    echo @echo off
    echo :: IDE环境变量配置
    echo :: 生成时间: %date% %time%
    echo.
    if not "%JAVA_HOME%"=="" (
        echo set JAVA_HOME=!JAVA_HOME!
        echo set PATH=%%JAVA_HOME%%\bin;%%PATH%%
    )
    if not "%ANDROID_HOME%"=="" (
        echo set ANDROID_HOME=!ANDROID_HOME!
        echo set PATH=%%ANDROID_HOME%%\platform-tools;%%ANDROID_HOME%%\tools;%%PATH%%
    )
) > scripts\env-config.bat

echo    ✅ 环境配置已保存到: scripts\env-config.bat
echo    可以运行此文件来临时设置环境变量

echo.
echo ========================================
echo    配置总结
echo ========================================

:: 检查是否所有配置都成功
set SUCCESS=1
if "%JAVA_HOME%"=="" set SUCCESS=0
if "%ANDROID_HOME%"=="" set SUCCESS=0

if %SUCCESS%==1 (
    echo    ✅ 环境配置完成！
    echo.
    echo    下一步：
    echo    1. 运行 scripts\env-config.bat 设置临时环境变量
    echo    2. 或将以上环境变量添加到系统设置中
    echo    3. 在IDE中打开项目进行开发
) else (
    echo    ⚠️  部分配置未完成
    echo.
    echo    请手动完成以下步骤：
    if "%JAVA_HOME%"=="" (
        echo    1. 安装JDK 17并设置JAVA_HOME
    )
    if "%ANDROID_HOME%"=="" (
        echo    2. 安装Android SDK并设置ANDROID_HOME
    )
    echo    3. 重新运行此脚本
)

echo ========================================
echo.
pause
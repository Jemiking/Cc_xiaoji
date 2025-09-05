@echo off
echo ===== APK体积检查脚本 =====
echo.

REM 设置变量
set PROJECT_DIR=%~dp0..
cd /d "%PROJECT_DIR%"

REM 清理之前的构建
echo 清理之前的构建...
call gradlew clean

REM 构建Release APK
echo 构建Release APK...
call gradlew assembleRelease

REM 获取APK文件路径
set APK_PATH=app\build\outputs\apk\release\app-release-unsigned.apk

REM 检查APK是否存在
if not exist "%APK_PATH%" (
    echo 错误：未找到APK文件
    exit /b 1
)

REM 获取文件大小
for %%F in ("%APK_PATH%") do set APK_SIZE=%%~zF

REM 转换为MB
set /a APK_SIZE_MB=%APK_SIZE% / 1048576
set /a APK_SIZE_KB=%APK_SIZE% / 1024

echo.
echo ===== APK体积分析结果 =====
echo APK路径: %APK_PATH%
echo 文件大小: %APK_SIZE% bytes
echo 文件大小: %APK_SIZE_KB% KB
echo 文件大小: %APK_SIZE_MB% MB

REM 检查poi-relocated.jar的大小
set POI_JAR=app\build\relocated\app-poi-relocated.jar
if exist "%POI_JAR%" (
    for %%F in ("%POI_JAR%") do set POI_SIZE=%%~zF
    set /a POI_SIZE_KB=!POI_SIZE! / 1024
    echo.
    echo POI重定位JAR大小: !POI_SIZE_KB! KB
)

echo.
echo 注意：要准确计算增量，需要与未添加POI依赖的版本比较
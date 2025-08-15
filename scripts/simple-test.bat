@echo off
echo ========================================
echo 简单测试脚本
echo ========================================

echo.
echo 测试1: 当前目录
cd

echo.
echo 测试2: 切换到项目目录
cd /d D:\kotlin\Cc_xiaoji
cd

echo.
echo 测试3: 检查gradlew.bat是否存在
if exist gradlew.bat (
    echo gradlew.bat 存在
) else (
    echo gradlew.bat 不存在！
)

echo.
echo 测试4: 显示gradlew.bat版本
call gradlew.bat --version

echo.
echo 测试5: 检查adb
where adb
if errorlevel 1 (
    echo adb不在PATH中
    echo 尝试常见位置...
    if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
        echo 找到adb在: C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\
        C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe version
    ) else (
        echo 找不到adb
    )
) else (
    echo adb在PATH中
    adb version
)

echo.
echo 测试完成！
pause
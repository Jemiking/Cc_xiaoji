@echo off
echo ========================================
echo 直接编译测试
echo ========================================

echo.
echo 编译Debug版本...
call "%~dp0..\\gradlew.bat" :app:assembleDebug

echo.
echo 编译结果：%ERRORLEVEL%
echo.
pause

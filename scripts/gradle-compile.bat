@echo off
echo === 直接Gradle编译 ===
echo.

REM 设置Gradle环境变量避免下载问题
set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set GRADLE_OPTS=-Dorg.gradle.daemon=false -Dorg.gradle.parallel=false

echo 开始编译app模块...
echo.

REM 直接调用gradlew编译
call gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain

echo.
echo 编译完成。查看上面的输出了解错误详情。
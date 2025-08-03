@echo off
setlocal enabledelayedexpansion

echo === Kotlin编译辅助工具 (Windows版) ===
echo.

if "%1"=="" goto show_usage
if "%1"=="help" goto show_usage
if "%1"=="build" goto build
if "%1"=="module" goto module
if "%1"=="clean" goto clean
if "%1"=="check" goto check

echo 错误：未知命令 '%1'
echo.
goto show_usage

:show_usage
echo 使用方法：
echo   scripts\compile-helper.bat [命令] [参数]
echo.
echo 命令：
echo   build         - 编译整个项目
echo   module ^<name^> - 编译指定模块
echo   clean         - 清理项目
echo   check         - 检查Gradle环境
echo   help          - 显示此帮助信息
echo.
echo 示例：
echo   scripts\compile-helper.bat build
echo   scripts\compile-helper.bat module app
echo   scripts\compile-helper.bat clean
goto end

:check
echo 检查Gradle环境...
call gradlew.bat --version
echo.
echo Android SDK路径：
echo   ANDROID_HOME: %ANDROID_HOME%
echo   ANDROID_SDK_ROOT: %ANDROID_SDK_ROOT%
goto end

:build
echo 编译整个项目...
echo 输出日志到: compile_output.log
call gradlew.bat build -x lint -x processDebugManifest -x processDebugResources -x mergeDebugResources --console=plain > compile_output.log 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！错误信息已保存到 compile_output.log
    echo.
    echo 显示最后100行错误信息：
    powershell -Command "Get-Content compile_output.log -Tail 100"
) else (
    echo 编译成功！
)
goto end

:module
if "%2"=="" (
    echo 错误：请指定模块名称
    echo 示例：scripts\compile-helper.bat module app
    goto end
)
echo 编译模块: %2
call gradlew.bat :%2:compileDebugKotlin -x lint --console=plain
goto end

:clean
echo 清理项目...
call gradlew.bat clean --console=plain
goto end

:end
endlocal
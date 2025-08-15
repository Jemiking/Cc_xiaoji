@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    IDE缓存清理工具
echo ========================================
echo.
echo 此脚本将清理IDE缓存和临时文件
echo 请先关闭Android Studio！
echo.

set /p CONFIRM="确认继续？(Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo 操作已取消
    exit /b 0
)

echo.
echo [1/6] 清理.idea缓存目录...
if exist ".idea\caches" (
    rmdir /s /q ".idea\caches" 2>nul
    echo    ✅ .idea\caches已清理
) else (
    echo    ⏭️  .idea\caches不存在
)

echo.
echo [2/6] 清理.gradle缓存...
if exist ".gradle" (
    rmdir /s /q ".gradle" 2>nul
    echo    ✅ .gradle已清理
) else (
    echo    ⏭️  .gradle不存在
)

echo.
echo [3/6] 清理所有build目录...
set COUNT=0
for /d /r %%d in (build) do (
    if exist "%%d" (
        echo    清理: %%d
        rmdir /s /q "%%d" 2>nul
        set /a COUNT+=1
    )
)
echo    ✅ 清理了 !COUNT! 个build目录

echo.
echo [4/6] 清理.iml文件...
set COUNT=0
for /r %%f in (*.iml) do (
    if exist "%%f" (
        del /f /q "%%f" 2>nul
        set /a COUNT+=1
    )
)
if !COUNT! gtr 0 (
    echo    ✅ 清理了 !COUNT! 个.iml文件
) else (
    echo    ⏭️  没有找到.iml文件
)

echo.
echo [5/6] 清理用户级Gradle缓存（可选）...
set /p CLEAN_USER_GRADLE="是否清理用户级Gradle缓存？这会影响所有项目(Y/N): "
if /i "%CLEAN_USER_GRADLE%"=="Y" (
    if exist "%USERPROFILE%\.gradle\caches" (
        echo    清理中... 这可能需要一些时间
        rmdir /s /q "%USERPROFILE%\.gradle\caches" 2>nul
        echo    ✅ 用户级Gradle缓存已清理
    ) else (
        echo    ⏭️  用户级Gradle缓存不存在
    )
) else (
    echo    ⏭️  跳过用户级Gradle缓存清理
)

echo.
echo [6/6] 重建Gradle Wrapper...
if exist "gradlew.bat" (
    call gradlew.bat --stop 2>nul
    echo    ✅ Gradle守护进程已停止
) else (
    echo    ⚠️  gradlew.bat不存在
)

echo.
echo ========================================
echo    ✅ 清理完成！
echo ========================================
echo.
echo 下一步操作：
echo 1. 打开Android Studio
echo 2. 选择 File -^> Invalidate Caches and Restart
echo 3. 等待项目重新索引
echo.

pause
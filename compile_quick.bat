@echo off
echo ===================================
echo 快速编译ledger模块
echo ===================================
echo.
echo 开始编译...
call gradlew.bat :feature:ledger:compileDebugKotlin --console=plain
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================
    echo 编译成功！
    echo ===================================
    echo.
    echo 请执行以下步骤验证修复：
    echo 1. 在Android Studio中运行应用
    echo 2. 打开记账模块
    echo 3. 切换到2024年10月
    echo 4. 查看Logcat中的调试日志：
    echo    - LEDGER_DEBUG: 查询过程
    echo    - DATA_MIGRATION: 数据修复
    echo    - QIANJI_DEBUG: 导入相关
    echo.
) else (
    echo.
    echo ===================================
    echo 编译失败！请检查错误信息
    echo ===================================
)
pause
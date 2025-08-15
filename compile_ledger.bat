@echo off
echo 编译ledger模块...
call gradlew.bat :feature:ledger:compileDebugKotlin --console=plain
echo.
echo 编译完成!
pause
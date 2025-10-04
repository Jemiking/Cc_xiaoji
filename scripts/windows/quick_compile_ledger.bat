@echo off
echo 开始编译 feature:ledger 模块...
"%~dp0..\\gradlew.bat" :feature:ledger:compileDebugKotlin --console=plain --no-daemon
echo 编译完成！

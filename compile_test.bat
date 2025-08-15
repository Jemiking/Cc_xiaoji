@echo off
echo Compiling ledger module...
call gradlew.bat :feature:ledger:compileDebugKotlin --console=plain
echo Compilation finished with exit code: %errorlevel%
@echo off
echo Compiling ledger module...
call "%~dp0..\\gradlew.bat" :feature:ledger:compileDebugKotlin --console=plain
echo Compilation finished with exit code: %errorlevel%

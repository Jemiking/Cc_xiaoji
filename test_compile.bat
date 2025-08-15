@echo off
echo Starting compilation...
gradlew.bat :feature:ledger:compileDebugKotlin --console=plain > compile_output.txt 2>&1
echo Compilation finished. Check compile_output.txt for results.
type compile_output.txt
@echo off
echo Deleting build directories...
if exist build rd /s /q build
if exist "feature\ledger\build" rd /s /q "feature\ledger\build"
if exist .gradle rd /s /q .gradle
echo Build directories deleted.

echo.
echo Running clean...
gradlew.bat.original clean

echo.
echo Compiling ledger module...
gradlew.bat.original :feature:ledger:compileDebugKotlin --console=plain

echo Done!
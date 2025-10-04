@echo off
echo Deleting build directories...
if exist "%~dp0..\build" rd /s /q "%~dp0..\build"
if exist "%~dp0..\feature\ledger\build" rd /s /q "%~dp0..\feature\ledger\build"
if exist "%~dp0..\.gradle" rd /s /q "%~dp0..\.gradle"
echo Build directories deleted.

echo.
echo Running clean...
"%~dp0..\gradlew.bat" clean

echo.
echo Compiling ledger module...
"%~dp0..\gradlew.bat" :feature:ledger:compileDebugKotlin --console=plain

echo Done!

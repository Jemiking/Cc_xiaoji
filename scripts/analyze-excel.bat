@echo off
echo 分析Excel文件结构...

set PRJ=%~dp0..
cd /d "%PRJ%"

:: 编译并运行分析器
call "%PRJ%\gradlew.bat" app:compileDebugKotlin

if %errorlevel% neq 0 (
    echo 编译失败
    exit /b 1
)

:: 运行分析器
echo 运行Excel分析器...
call "%PRJ%\gradlew.bat" --quiet app:runDebug -PmainClass=com.ccxiaoji.app.utils.ExcelAnalyzerKt

echo.
echo 分析完成！结果保存在: 读取文件\excel_analysis.txt
pause

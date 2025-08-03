@echo off
echo 分析Excel文件结构...

cd /d D:\kotlin\Cc_xiaoji

:: 编译并运行分析器
gradlew.bat app:compileDebugKotlin

if %errorlevel% neq 0 (
    echo 编译失败
    exit /b 1
)

:: 运行分析器
echo 运行Excel分析器...
gradlew.bat --quiet app:runDebug -PmainClass=com.ccxiaoji.app.utils.ExcelAnalyzerKt

echo.
echo 分析完成！结果保存在: 读取文件\excel_analysis.txt
pause
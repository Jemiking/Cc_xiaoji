@echo off
echo 运行Excel分析器...
cd /d D:\kotlin\Cc_xiaoji

:: 使用kotlin命令直接运行编译后的类
java -cp "app\build\classes\kotlin\debug;app\build\tmp\kotlin-classes\debug;^
C:\Users\%USERNAME%\.gradle\caches\modules-2\files-2.1\org.dhatim\fastexcel-reader\0.18.0\*.jar;^
C:\Users\%USERNAME%\.gradle\caches\modules-2\files-2.1\org.dhatim\fastexcel\0.18.0\*.jar" ^
com.ccxiaoji.app.utils.SimpleExcelAnalyzer

pause
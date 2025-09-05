@echo off
echo === 检查Maven依赖解析 ===
echo.

echo 1. 检查网络连接到Maven Central...
curl -I https://repo1.maven.org/maven2/cn/idev/excel/fastexcel/1.2.0/fastexcel-1.2.0.pom
echo.

echo 2. 下载POM文件...
curl -o fastexcel-1.2.0.pom https://repo1.maven.org/maven2/cn/idev/excel/fastexcel/1.2.0/fastexcel-1.2.0.pom
echo.

echo 3. 检查JAR文件是否存在...
curl -I https://repo1.maven.org/maven2/cn/idev/excel/fastexcel/1.2.0/fastexcel-1.2.0.jar
echo.

echo 4. 查看POM内容...
type fastexcel-1.2.0.pom
echo.

pause
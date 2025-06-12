#!/bin/bash

# 性能基准测试运行脚本
# 用于运行CC小记应用的性能测试并生成报告

echo "=== CC小记性能基准测试 ==="
echo "开始时间：$(date)"
echo ""

# 检查设备连接
echo "检查设备连接..."
adb devices
if [ $? -ne 0 ]; then
    echo "错误：没有检测到已连接的设备"
    exit 1
fi

# 清理之前的测试结果
echo "清理之前的测试结果..."
rm -rf app/build/outputs/androidTest-results/benchmark
rm -rf app/build/outputs/benchmark

# 构建应用
echo "构建benchmark版本..."
./gradlew :app:assembleBenchmark
if [ $? -ne 0 ]; then
    echo "错误：构建失败"
    exit 1
fi

# 运行性能测试
echo ""
echo "运行性能测试..."

# 1. 启动性能测试
echo "1. 运行启动性能测试..."
./gradlew :app:connectedBenchmarkAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.ccxiaoji.app.benchmark.StartupBenchmark

# 2. 导航性能测试
echo ""
echo "2. 运行导航性能测试..."
./gradlew :app:connectedBenchmarkAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.ccxiaoji.app.benchmark.NavigationBenchmark

# 3. 数据库性能测试
echo ""
echo "3. 运行数据库性能测试..."
./gradlew :app:connectedBenchmarkAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.ccxiaoji.app.benchmark.DatabaseBenchmark

# 4. 模块性能测试
echo ""
echo "4. 运行模块性能测试..."
./gradlew :app:connectedBenchmarkAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.ccxiaoji.app.benchmark.ModuleBenchmark

# 生成报告
echo ""
echo "生成测试报告..."
RESULT_DIR="app/build/outputs/androidTest-results/benchmark"
REPORT_DIR="doc/benchmark-results"

# 创建报告目录
mkdir -p $REPORT_DIR

# 复制测试结果
if [ -d "$RESULT_DIR" ]; then
    cp -r $RESULT_DIR/* $REPORT_DIR/
    echo "测试结果已保存到：$REPORT_DIR"
else
    echo "警告：未找到测试结果"
fi

# 输出总结
echo ""
echo "=== 测试完成 ==="
echo "结束时间：$(date)"
echo ""
echo "请查看以下文件获取详细结果："
echo "- $REPORT_DIR/benchmark-results.json"
echo "- doc/性能基准测试报告.md（需要手动更新数据）"
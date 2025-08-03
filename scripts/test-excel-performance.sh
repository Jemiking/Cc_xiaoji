#!/bin/bash
# Excel性能测试脚本

echo "================================"
echo "FastExcel 性能测试"
echo "================================"
echo ""

# 检查设备连接
if ! adb devices | grep -q "device$"; then
    echo "错误：没有连接的Android设备"
    exit 1
fi

# 清理之前的日志
adb logcat -c

# 运行集成测试
echo "1. 运行FastExcel集成测试..."
adb shell am instrument -w -e class com.ccxiaoji.app.FastExcelIntegrationTest \
    com.ccxiaoji.app.test/androidx.test.runner.AndroidJUnitRunner

echo ""
echo "2. 运行导出性能基准测试..."
adb shell am instrument -w -e class com.ccxiaoji.app.FastExcelIntegrationTest#benchmarkExportPerformance \
    com.ccxiaoji.app.test/androidx.test.runner.AndroidJUnitRunner

echo ""
echo "3. 运行导入性能基准测试..."
adb shell am instrument -w -e class com.ccxiaoji.app.FastExcelIntegrationTest#benchmarkImportPerformance \
    com.ccxiaoji.app.test/androidx.test.runner.AndroidJUnitRunner

echo ""
echo "4. 运行内存使用测试..."
adb shell am instrument -w -e class com.ccxiaoji.app.FastExcelIntegrationTest#testMemoryUsage \
    com.ccxiaoji.app.test/androidx.test.runner.AndroidJUnitRunner

echo ""
echo "5. 提取性能测试结果..."
echo "================================"
adb logcat -d | grep -E "(BENCHMARK|平均导出时间|导入.*耗时|内存使用增量)" | tail -20

echo ""
echo "6. 检查崩溃日志..."
echo "================================"
adb logcat -d | grep -E "(FATAL|AndroidRuntime)" | tail -10

echo ""
echo "测试完成！"
echo "================================"

# 保存完整日志
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="excel_performance_test_${TIMESTAMP}.log"
adb logcat -d > $LOG_FILE
echo "完整日志已保存到：$LOG_FILE"
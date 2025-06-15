#!/bin/bash

# 排班模块代码清理脚本
# 用于移除TODO注释、调试代码等

echo "开始清理排班模块代码..."

# 定义排班模块路径
SCHEDULE_MODULE_PATH="/mnt/d/kotlin/Cc_xiaoji/feature/schedule"

# 统计清理前的TODO数量
TODO_COUNT_BEFORE=$(grep -r "TODO:" "$SCHEDULE_MODULE_PATH" | wc -l)
LOG_COUNT_BEFORE=$(grep -r "android.util.Log." "$SCHEDULE_MODULE_PATH" | wc -l)

echo "清理前状态："
echo "- TODO注释数量: $TODO_COUNT_BEFORE"
echo "- 调试日志数量: $LOG_COUNT_BEFORE"

# 批量移除TODO注释（保留功能说明）
find "$SCHEDULE_MODULE_PATH" -name "*.kt" -type f -exec sed -i 's/TODO: 编译验证[^$]*//g' {} \;
find "$SCHEDULE_MODULE_PATH" -name "*.kt" -type f -exec sed -i 's/\/\/ TODO: /\/\/ /g' {} \;

# 移除android.util.Log调试语句
find "$SCHEDULE_MODULE_PATH" -name "*.kt" -type f -exec sed -i '/android\.util\.Log\.[d|v|i|w|e]/d' {} \;

# 统计清理后的数量
TODO_COUNT_AFTER=$(grep -r "TODO:" "$SCHEDULE_MODULE_PATH" | wc -l)
LOG_COUNT_AFTER=$(grep -r "android.util.Log." "$SCHEDULE_MODULE_PATH" | wc -l)

echo ""
echo "清理后状态："
echo "- TODO注释数量: $TODO_COUNT_AFTER"
echo "- 调试日志数量: $LOG_COUNT_AFTER"

echo ""
echo "清理结果："
echo "- 移除TODO注释: $((TODO_COUNT_BEFORE - TODO_COUNT_AFTER))"
echo "- 移除调试日志: $((LOG_COUNT_BEFORE - LOG_COUNT_AFTER))"

echo ""
echo "代码清理完成！"
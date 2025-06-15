#!/bin/bash
# 验证ScheduleNavigator接口方法的脚本
# 用于检查接口定义与实现是否匹配

echo "=== 验证 ScheduleNavigator 接口方法 ==="
echo ""

# 定义颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 接口文件路径
INTERFACE_FILE="feature/schedule/api/src/main/kotlin/com/ccxiaoji/feature/schedule/api/ScheduleNavigator.kt"
IMPL_FILE="app/src/main/java/com/ccxiaoji/app/navigation/ScheduleNavigatorImpl.kt"

echo "检查文件存在性..."
if [ ! -f "$INTERFACE_FILE" ]; then
    echo -e "${RED}错误: 找不到接口文件 $INTERFACE_FILE${NC}"
    exit 1
fi

if [ ! -f "$IMPL_FILE" ]; then
    echo -e "${RED}错误: 找不到实现文件 $IMPL_FILE${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 文件存在${NC}"
echo ""

echo "提取接口方法..."
# 使用grep提取接口中的方法签名
INTERFACE_METHODS=$(grep -E "fun\s+navigate" "$INTERFACE_FILE" | sed 's/^[[:space:]]*//' | sort)

echo "提取实现方法..."
# 使用grep提取实现中的方法签名（包含override）
IMPL_METHODS=$(grep -E "override\s+fun\s+navigate" "$IMPL_FILE" | sed 's/override //' | sed 's/^[[:space:]]*//' | sort)

echo ""
echo "=== 接口方法 ==="
echo "$INTERFACE_METHODS"

echo ""
echo "=== 实现方法 ==="
echo "$IMPL_METHODS"

echo ""
echo "=== 比较结果 ==="

# 保存到临时文件进行比较
echo "$INTERFACE_METHODS" > /tmp/interface_methods.txt
echo "$IMPL_METHODS" > /tmp/impl_methods.txt

# 检查是否完全匹配
if diff -q /tmp/interface_methods.txt /tmp/impl_methods.txt > /dev/null; then
    echo -e "${GREEN}✓ 所有方法签名匹配！${NC}"
else
    echo -e "${RED}✗ 方法签名不匹配！${NC}"
    echo ""
    echo "差异详情："
    diff -u /tmp/interface_methods.txt /tmp/impl_methods.txt
fi

# 清理临时文件
rm -f /tmp/interface_methods.txt /tmp/impl_methods.txt

echo ""
echo "=== 编译建议 ==="
echo "如果方法签名匹配但仍有编译错误，请执行："
echo -e "${YELLOW}1. ./gradlew clean${NC}"
echo -e "${YELLOW}2. ./gradlew build${NC}"
echo ""
echo "或查看 compile_commands.txt 获取详细的编译步骤。"
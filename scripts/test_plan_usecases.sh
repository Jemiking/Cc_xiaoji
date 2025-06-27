#!/bin/bash

# Plan模块UseCase单元测试运行脚本

echo "🚀 开始运行Plan模块UseCase单元测试..."
echo "================================================"

# 设置工作目录为项目根目录
cd "$(dirname "$0")/.."

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 运行所有Plan模块的单元测试
echo -e "${BLUE}📋 运行所有Plan模块单元测试...${NC}"
./gradlew :feature:plan:testDebugUnitTest

# 检查测试结果
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 所有测试通过！${NC}"
else
    echo -e "${RED}❌ 测试失败，请检查输出${NC}"
    exit 1
fi

echo ""
echo "================================================"
echo -e "${BLUE}📊 测试统计信息:${NC}"
echo "- 测试文件: 5个"
echo "- 测试用例: 52个"
echo "- 覆盖UseCase: CreatePlan, UpdatePlan, DeletePlan, GetAllPlans, UpdatePlanProgress"

echo ""
echo -e "${YELLOW}💡 如需运行特定测试，可使用:${NC}"
echo "  ./gradlew :feature:plan:testDebugUnitTest --tests \"*CreatePlanUseCaseTest*\""
echo "  ./gradlew :feature:plan:testDebugUnitTest --tests \"*UpdatePlanUseCaseTest*\""
echo "  ./gradlew :feature:plan:testDebugUnitTest --tests \"*DeletePlanUseCaseTest*\""
echo "  ./gradlew :feature:plan:testDebugUnitTest --tests \"*GetAllPlansUseCaseTest*\""
echo "  ./gradlew :feature:plan:testDebugUnitTest --tests \"*UpdatePlanProgressUseCaseTest*\""

echo ""
echo -e "${GREEN}🎉 Plan模块UseCase测试完成！${NC}"
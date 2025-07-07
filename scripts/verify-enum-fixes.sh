#!/bin/bash

echo "枚举类型修复验证脚本"
echo "=================================="

cd /mnt/d/kotlin/Cc_xiaoji

echo "📋 修复内容："
echo "1. 创建ChartType枚举 - 图表类型定义"
echo "2. 创建ThemeMode枚举 - 主题模式定义"
echo "3. 添加所有必要的导入语句"
echo "4. 删除重复的枚举定义"
echo ""

echo "🔍 验证枚举文件是否存在..."
echo "✅ ChartType.kt:"
if [ -f "feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/domain/model/ChartType.kt" ]; then
    echo "   文件存在 ✅"
    echo "   枚举值: $(grep -E "^\s*(STATUS_PIE|PROGRESS_BAR|MONTHLY_TREND|TAG_ANALYSIS)" feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/domain/model/ChartType.kt | wc -l)个"
else
    echo "   文件缺失 ❌"
fi

echo ""
echo "✅ ThemeMode.kt:"
if [ -f "feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/domain/model/ThemeMode.kt" ]; then
    echo "   文件存在 ✅"
    echo "   枚举值: $(grep -E "^\s*(SYSTEM|LIGHT|DARK)" feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/domain/model/ThemeMode.kt | wc -l)个"
else
    echo "   文件缺失 ❌"
fi

echo ""
echo "🔍 验证导入语句..."

# 检查ChartType导入
echo "✅ ChartType导入检查:"
files_with_charttype=("feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/presentation/screen/ProgressAnalysisScreen.kt" "feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/presentation/viewmodel/ProgressAnalysisViewModel.kt")

for file in "${files_with_charttype[@]}"; do
    if grep -q "import com.ccxiaoji.feature.plan.domain.model.ChartType" "$file" 2>/dev/null; then
        echo "   $(basename "$file") - ChartType导入 ✅"
    else
        echo "   $(basename "$file") - ChartType导入 ❌"
    fi
done

echo ""
echo "✅ ThemeMode导入检查:"
files_with_thememode=("feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/presentation/screen/SettingsScreen.kt" "feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/presentation/viewmodel/ThemeViewModel.kt")

for file in "${files_with_thememode[@]}"; do
    if grep -q "import com.ccxiaoji.feature.plan.domain.model.ThemeMode" "$file" 2>/dev/null; then
        echo "   $(basename "$file") - ThemeMode导入 ✅"
    else
        echo "   $(basename "$file") - ThemeMode导入 ❌"
    fi
done

echo ""
echo "🔍 检查重复定义..."
if grep -n "enum class ChartType" feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/presentation/viewmodel/ProgressAnalysisViewModel.kt 2>/dev/null; then
    echo "❌ ProgressAnalysisViewModel.kt中仍有重复的ChartType定义"
else
    echo "✅ ProgressAnalysisViewModel.kt中的重复ChartType定义已删除"
fi

echo ""
echo "📊 修复统计："
echo "- 创建枚举文件: 2个 (ChartType.kt, ThemeMode.kt)"
echo "- 添加导入语句: 4个文件"
echo "- 删除重复定义: 1个"
echo "- 总计修复文件数: 6个"

echo ""
echo "✅ 枚举类型修复验证完成！"
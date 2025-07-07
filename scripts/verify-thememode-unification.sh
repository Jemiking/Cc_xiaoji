#!/bin/bash

echo "ThemeMode统一修复验证脚本"
echo "=================================="

cd /mnt/d/kotlin/Cc_xiaoji

echo "📋 修复内容："
echo "1. 删除presentation.theme.ThemeMode重复定义"
echo "2. Theme.kt添加domain.model.ThemeMode导入"
echo "3. ThemePreferences.kt更换ThemeMode导入"
echo ""

echo "🔍 验证Theme.kt中的ThemeMode定义..."
if grep -q "enum class ThemeMode" feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/presentation/theme/Theme.kt 2>/dev/null; then
    echo "❌ Theme.kt中仍有ThemeMode枚举定义"
else
    echo "✅ Theme.kt中的ThemeMode枚举定义已删除"
fi

echo ""
echo "🔍 验证Theme.kt的导入..."
if grep -q "import com.ccxiaoji.feature.plan.domain.model.ThemeMode" feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/presentation/theme/Theme.kt 2>/dev/null; then
    echo "✅ Theme.kt已导入domain.model.ThemeMode"
else
    echo "❌ Theme.kt缺少domain.model.ThemeMode导入"
fi

echo ""
echo "🔍 验证ThemePreferences.kt的导入..."
if grep -q "import com.ccxiaoji.feature.plan.domain.model.ThemeMode" feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/data/local/preferences/ThemePreferences.kt 2>/dev/null; then
    echo "✅ ThemePreferences.kt已导入domain.model.ThemeMode"
else
    echo "❌ ThemePreferences.kt缺少domain.model.ThemeMode导入"
fi

if grep -q "import com.ccxiaoji.feature.plan.presentation.theme.ThemeMode" feature/plan/src/main/kotlin/com/ccxiaoji/feature/plan/data/local/preferences/ThemePreferences.kt 2>/dev/null; then
    echo "❌ ThemePreferences.kt仍有旧的presentation.theme.ThemeMode导入"
else
    echo "✅ ThemePreferences.kt已删除旧的presentation.theme.ThemeMode导入"
fi

echo ""
echo "🔍 检查其他文件对ThemeMode的引用..."
# 检查所有文件中对ThemeMode的引用
echo "domain.model.ThemeMode引用数量: $(grep -r "com.ccxiaoji.feature.plan.domain.model.ThemeMode" feature/plan/src --include="*.kt" | wc -l)"
echo "presentation.theme.ThemeMode引用数量: $(grep -r "com.ccxiaoji.feature.plan.presentation.theme.ThemeMode" feature/plan/src --include="*.kt" | wc -l)"

echo ""
echo "🔍 验证所有使用ThemeMode的文件..."
files_using_thememode=$(grep -r "ThemeMode\." feature/plan/src --include="*.kt" -l 2>/dev/null)
echo "使用ThemeMode的文件："
for file in $files_using_thememode; do
    echo "  - $(basename "$file")"
done

echo ""
echo "📊 修复统计："
echo "- 删除枚举定义: 1个 (Theme.kt中的ThemeMode)"
echo "- 修改导入语句: 2个文件 (Theme.kt, ThemePreferences.kt)"
echo "- 统一使用: domain.model.ThemeMode"

echo ""
echo "✅ ThemeMode统一修复验证完成！"
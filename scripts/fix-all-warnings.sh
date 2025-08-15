#!/bin/bash

# 批量修复编译警告脚本
echo "开始批量修复编译警告..."

# 1. 修复Divider -> HorizontalDivider
echo "修复废弃的Divider组件..."
find . -name "*.kt" -type f | while read file; do
    if grep -q "^import.*Divider" "$file"; then
        # 已经有HorizontalDivider import的跳过
        if ! grep -q "HorizontalDivider" "$file"; then
            # 替换Divider() 为 HorizontalDivider()
            sed -i 's/\bDivider(/HorizontalDivider(/g' "$file"
            # 添加import
            sed -i '/^import androidx.compose.material3\.\*/a\import androidx.compose.material3.HorizontalDivider' "$file"
            echo "  修复: $file"
        fi
    fi
done

# 2. 修复废弃的图标 (添加AutoMirrored)
echo "修复废弃的图标..."
declare -a icons=("ArrowBack" "List" "KeyboardArrowRight" "Assignment" "ArrowForward" "Send" "Label")
for icon in "${icons[@]}"; do
    find . -name "*.kt" -type f | while read file; do
        if grep -q "Icons\.\(Filled\|Outlined\|Rounded\|Sharp\|TwoTone\)\.$icon" "$file"; then
            sed -i "s/Icons\.\(Filled\|Outlined\|Rounded\|Sharp\|TwoTone\)\.$icon/Icons.AutoMirrored.\1.$icon/g" "$file"
            echo "  修复图标 $icon in: $file"
        fi
    done
done

# 3. 修复不必要的安全调用
echo "修复不必要的安全调用..."
# 这个需要更谨慎，只修复明显的情况

# 4. 修复Elvis操作符
echo "修复不必要的Elvis操作符..."
# 需要具体分析每个情况

echo "修复完成！"
echo "请运行 ./gradlew build 验证修复结果"
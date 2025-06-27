#!/bin/bash
# 快速修复Room编译器缺失问题

echo "🚀 开始修复Room编译器问题..."

# 需要修复的模块
modules=("feature/todo" "feature/ledger")

for module in "${modules[@]}"; do
    build_file="$module/build.gradle.kts"
    
    if [ ! -f "$build_file" ]; then
        echo "❌ 文件不存在: $build_file"
        continue
    fi
    
    echo "📝 处理模块: $module"
    
    # 检查是否已有room-compiler
    if grep -q "room-compiler" "$build_file"; then
        echo "✅ 该模块已配置room-compiler，跳过"
        continue
    fi
    
    # 检查是否使用了Room
    if ! grep -q "room-runtime\|room-ktx" "$build_file"; then
        echo "ℹ️ 该模块未使用Room，跳过"
        continue
    fi
    
    # 备份原文件
    cp "$build_file" "${build_file}.backup"
    echo "📋 已创建备份: ${build_file}.backup"
    
    # 在Room依赖后添加room-compiler
    # 查找最后一个Room依赖的行号
    last_room_line=$(grep -n "room-ktx\|room-runtime" "$build_file" | tail -1 | cut -d: -f1)
    
    if [ -n "$last_room_line" ]; then
        # 使用版本目录方式（如果可用）
        if grep -q "libs\." "$build_file"; then
            # 使用版本目录
            sed -i "${last_room_line}a\\    ksp(libs.room.compiler)" "$build_file"
            echo "✅ 已添加: ksp(libs.room.compiler)"
        else
            # 使用硬编码版本（临时方案）
            sed -i "${last_room_line}a\\    ksp(\"androidx.room:room-compiler:2.6.1\")" "$build_file"
            echo "✅ 已添加: ksp(\"androidx.room:room-compiler:2.6.1\")"
        fi
    else
        echo "❌ 未找到Room依赖位置，请手动添加"
    fi
done

echo "
🎉 修复完成！

下一步：
1. 检查修改: git diff
2. 编译验证: 
   ./gradlew :feature:todo:build
   ./gradlew :feature:ledger:build
3. 如果编译成功，提交修改
4. 如果有问题，从.backup文件恢复

注意：这是临时修复方案，建议后续迁移到版本目录管理。
"
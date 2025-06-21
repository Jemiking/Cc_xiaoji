#!/bin/bash
# 模块配置一致性检查脚本

echo "🔍 开始检查模块配置一致性..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 错误计数
error_count=0
warning_count=0

# 检查函数
check_file() {
    local file=$1
    local module_name=$(dirname "$file" | xargs basename)
    echo -e "\n📋 检查模块: ${module_name}"
    
    # 检查1: 是否使用版本目录
    if grep -q 'implementation(".*:.*:.*")' "$file" || grep -q "implementation('.*:.*:.*')" "$file"; then
        echo -e "${RED}❌ 错误: 发现硬编码版本依赖${NC}"
        grep -n 'implementation(".*:.*:.*")' "$file" || grep -n "implementation('.*:.*:.*')" "$file"
        ((error_count++))
    else
        echo -e "${GREEN}✅ 通过: 使用版本目录${NC}"
    fi
    
    # 检查2: Room编译器配置
    if grep -q 'room-runtime\|room-ktx' "$file"; then
        if ! grep -q 'room-compiler' "$file"; then
            echo -e "${RED}❌ 错误: 使用Room但缺少room-compiler${NC}"
            ((error_count++))
        else
            echo -e "${GREEN}✅ 通过: Room配置完整${NC}"
        fi
    fi
    
    # 检查3: Kotlin编译器参数一致性
    if ! grep -q 'freeCompilerArgs' "$file"; then
        echo -e "${YELLOW}⚠️ 警告: 未设置Kotlin编译器参数${NC}"
        ((warning_count++))
    else
        # 检查是否包含标准参数
        if ! grep -q '-Xjsr305=strict' "$file"; then
            echo -e "${YELLOW}⚠️ 警告: 缺少-Xjsr305=strict参数${NC}"
            ((warning_count++))
        fi
    fi
    
    # 检查4: Compose配置
    if grep -q 'compose = true' "$file"; then
        if ! grep -q 'kotlinCompilerExtensionVersion' "$file"; then
            echo -e "${RED}❌ 错误: 启用Compose但未设置编译器版本${NC}"
            ((error_count++))
        fi
    fi
    
    # 检查5: Java版本一致性
    if ! grep -q 'JavaVersion.VERSION_17' "$file"; then
        echo -e "${YELLOW}⚠️ 警告: 未使用Java 17${NC}"
        ((warning_count++))
    fi
}

# 主要检查逻辑
echo "🔍 扫描所有模块..."

# 检查feature模块
for file in feature/*/build.gradle.kts; do
    if [ -f "$file" ]; then
        check_file "$file"
    fi
done

# 检查shared模块
for file in shared/*/build.gradle.kts; do
    if [ -f "$file" ]; then
        check_file "$file"
    fi
done

# 检查core模块
for file in core/*/build.gradle.kts; do
    if [ -f "$file" ]; then
        check_file "$file"
    fi
done

# 额外检查：版本目录文件是否存在
echo -e "\n📋 检查版本目录文件..."
if [ -f "gradle/libs.versions.toml" ]; then
    echo -e "${GREEN}✅ 版本目录文件存在${NC}"
    
    # 检查是否有未定义的库
    echo "🔍 检查版本目录完整性..."
    missing_libs=()
    
    # 检查常用库是否定义
    required_libs=("room-compiler" "hilt-compiler" "compose-bom" "kotlin-coroutines")
    for lib in "${required_libs[@]}"; do
        if ! grep -q "$lib" "gradle/libs.versions.toml"; then
            missing_libs+=("$lib")
        fi
    done
    
    if [ ${#missing_libs[@]} -gt 0 ]; then
        echo -e "${YELLOW}⚠️ 警告: 版本目录缺少以下库定义:${NC}"
        printf '%s\n' "${missing_libs[@]}"
        ((warning_count++))
    fi
else
    echo -e "${RED}❌ 错误: 版本目录文件不存在${NC}"
    ((error_count++))
fi

# 总结报告
echo -e "\n📊 检查完成！"
echo "================================"
echo -e "错误数量: ${RED}${error_count}${NC}"
echo -e "警告数量: ${YELLOW}${warning_count}${NC}"
echo "================================"

if [ $error_count -gt 0 ]; then
    echo -e "${RED}❌ 存在配置错误，请修复后再继续${NC}"
    exit 1
elif [ $warning_count -gt 0 ]; then
    echo -e "${YELLOW}⚠️ 存在配置警告，建议优化${NC}"
    exit 0
else
    echo -e "${GREEN}✅ 所有检查通过！${NC}"
    exit 0
fi
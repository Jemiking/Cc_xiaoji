#!/bin/bash
# 技术债务自动化检查脚本

echo "🔍 CC小记技术债务检查"
echo "===================="
echo "检查时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 计数器
total_checks=0
passed_checks=0
failed_checks=0
warnings=0

# 检查函数
check() {
    local name=$1
    local result=$2
    local message=$3
    ((total_checks++))
    
    if [ "$result" = "pass" ]; then
        echo -e "${GREEN}✅ PASS${NC}: $name"
        ((passed_checks++))
    elif [ "$result" = "fail" ]; then
        echo -e "${RED}❌ FAIL${NC}: $name"
        echo "   原因: $message"
        ((failed_checks++))
    else
        echo -e "${YELLOW}⚠️ WARN${NC}: $name"
        echo "   警告: $message"
        ((warnings++))
    fi
}

echo "### 1. Room编译器检查"
echo "-------------------"
# 检查使用Room的模块是否配置了room-compiler
for module in feature/todo feature/ledger feature/habit feature/schedule; do
    if [ -f "$module/build.gradle.kts" ]; then
        if grep -q "room-runtime\|room-ktx" "$module/build.gradle.kts"; then
            if grep -q "room.compiler" "$module/build.gradle.kts"; then
                check "$module Room编译器" "pass" ""
            else
                check "$module Room编译器" "fail" "使用Room但缺少room-compiler"
            fi
        fi
    fi
done

echo ""
echo "### 2. 版本目录使用检查"
echo "---------------------"
# 检查是否使用版本目录
for dir in feature shared core; do
    for module in $dir/*; do
        if [ -f "$module/build.gradle.kts" ]; then
            module_name=$(basename "$module")
            if grep -q 'implementation(".*:.*:.*")' "$module/build.gradle.kts" 2>/dev/null; then
                check "$dir/$module_name 版本目录" "fail" "发现硬编码版本"
            else
                check "$dir/$module_name 版本目录" "pass" ""
            fi
        fi
    done
done

echo ""
echo "### 3. Kotlin编译参数一致性检查"
echo "------------------------------"
# 检查freeCompilerArgs
for module in feature/*; do
    if [ -f "$module/build.gradle.kts" ]; then
        module_name=$(basename "$module")
        if grep -q "freeCompilerArgs" "$module/build.gradle.kts"; then
            if grep -q -- "-Xjsr305" "$module/build.gradle.kts"; then
                check "feature/$module_name Kotlin参数" "pass" ""
            else
                check "feature/$module_name Kotlin参数" "warn" "编译参数不完整"
            fi
        else
            check "feature/$module_name Kotlin参数" "fail" "未设置freeCompilerArgs"
        fi
    fi
done

echo ""
echo "### 4. Desugaring配置检查"
echo "-----------------------"
# 检查Desugaring配置
for module in feature/*; do
    if [ -f "$module/build.gradle.kts" ]; then
        module_name=$(basename "$module")
        if grep -q "isCoreLibraryDesugaringEnabled = true" "$module/build.gradle.kts"; then
            if grep -q "desugar.jdk.libs" "$module/build.gradle.kts"; then
                check "feature/$module_name Desugaring" "pass" ""
            else
                check "feature/$module_name Desugaring" "warn" "启用但缺少依赖"
            fi
        else
            check "feature/$module_name Desugaring" "warn" "未启用Desugaring"
        fi
    fi
done

echo ""
echo "### 5. 模块结构标准检查"
echo "---------------------"
# 检查模块结构
for module in feature/*; do
    module_name=$(basename "$module")
    if [ -d "$module/src/main/kotlin" ]; then
        # 检查标准目录
        missing_dirs=""
        [ ! -d "$module/src/main/kotlin/com/ccxiaoji/feature/$module_name/api" ] && missing_dirs="$missing_dirs api"
        [ ! -d "$module/src/main/kotlin/com/ccxiaoji/feature/$module_name/data" ] && missing_dirs="$missing_dirs data"
        [ ! -d "$module/src/main/kotlin/com/ccxiaoji/feature/$module_name/di" ] && missing_dirs="$missing_dirs di"
        [ ! -d "$module/src/main/kotlin/com/ccxiaoji/feature/$module_name/domain" ] && missing_dirs="$missing_dirs domain"
        [ ! -d "$module/src/main/kotlin/com/ccxiaoji/feature/$module_name/presentation" ] && missing_dirs="$missing_dirs presentation"
        
        if [ -z "$missing_dirs" ]; then
            check "feature/$module_name 目录结构" "pass" ""
        else
            check "feature/$module_name 目录结构" "warn" "缺少目录:$missing_dirs"
        fi
    fi
done

echo ""
echo "### 6. 技术债务文档检查"
echo "---------------------"
# 检查关键文档是否存在
docs=(
    "doc/20250620-模块技术栈不一致性分析报告.md"
    "doc/20250620-技术债务修复完整方案.md"
    "doc/20250620-技术债务跟踪表.md"
    "doc/20250620-模块配置标准.md"
)

for doc in "${docs[@]}"; do
    if [ -f "$doc" ]; then
        check "$(basename "$doc")" "pass" ""
    else
        check "$(basename "$doc")" "fail" "文档不存在"
    fi
done

echo ""
echo "### 7. BuildSrc检查"
echo "-----------------"
if [ -d "buildSrc" ]; then
    if [ -f "buildSrc/src/main/kotlin/ccxiaoji.android.feature.gradle.kts" ]; then
        check "Convention插件" "pass" ""
    else
        check "Convention插件" "fail" "Convention插件文件不存在"
    fi
else
    check "BuildSrc目录" "fail" "BuildSrc目录不存在"
fi

echo ""
echo "### 8. 版本目录文件检查"
echo "---------------------"
if [ -f "gradle/libs.versions.toml" ]; then
    # 检查必要的版本定义
    missing_versions=""
    ! grep -q "desugar" "gradle/libs.versions.toml" && missing_versions="$missing_versions desugar"
    ! grep -q "room" "gradle/libs.versions.toml" && missing_versions="$missing_versions room"
    ! grep -q "compose-compiler" "gradle/libs.versions.toml" && missing_versions="$missing_versions compose-compiler"
    
    if [ -z "$missing_versions" ]; then
        check "版本目录完整性" "pass" ""
    else
        check "版本目录完整性" "warn" "缺少版本:$missing_versions"
    fi
else
    check "版本目录文件" "fail" "libs.versions.toml不存在"
fi

echo ""
echo "===================="
echo "📊 检查汇总"
echo "===================="
echo -e "总检查项: $total_checks"
echo -e "✅ 通过: ${GREEN}$passed_checks${NC}"
echo -e "❌ 失败: ${RED}$failed_checks${NC}"
echo -e "⚠️ 警告: ${YELLOW}$warnings${NC}"
echo ""

# 计算健康度
health=$((passed_checks * 100 / total_checks))
echo -n "技术债务健康度: "
if [ $health -ge 90 ]; then
    echo -e "${GREEN}${health}% - 优秀${NC}"
elif [ $health -ge 70 ]; then
    echo -e "${YELLOW}${health}% - 良好${NC}"
else
    echo -e "${RED}${health}% - 需要改进${NC}"
fi

echo ""
echo "生成时间: $(date '+%Y-%m-%d %H:%M:%S')"

# 返回状态码
if [ $failed_checks -gt 0 ]; then
    exit 1
else
    exit 0
fi
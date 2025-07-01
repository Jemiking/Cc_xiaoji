#!/bin/bash
# 版本目录迁移验证脚本
# 用途：自动检查版本迁移的正确性

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 函数：打印带颜色的消息
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "ℹ️  $1"
}

# 函数：检查模块的硬编码版本
check_hardcoded_versions() {
    local module=$1
    local build_file="${module}/build.gradle.kts"
    
    if [ ! -f "$build_file" ]; then
        print_error "找不到文件: $build_file"
        return 1
    fi
    
    print_info "检查硬编码版本: $module"
    
    # 查找硬编码版本（排除versionCode和versionName）
    local hardcoded=$(grep -E '"[0-9]+\.[0-9]+(\.[0-9]+)?"' "$build_file" | grep -v "versionCode\|versionName" || true)
    
    if [ -z "$hardcoded" ]; then
        print_success "没有发现硬编码版本"
        return 0
    else
        print_error "发现硬编码版本:"
        echo "$hardcoded"
        return 1
    fi
}

# 函数：验证模块编译
verify_module_build() {
    local module=$1
    local gradle_module=$(echo $module | sed 's/\//:/')
    
    print_info "验证模块编译: $gradle_module"
    
    if ./gradlew "$gradle_module:build" --quiet; then
        print_success "模块编译成功"
        return 0
    else
        print_error "模块编译失败"
        return 1
    fi
}

# 函数：检查git差异
check_git_diff() {
    local module=$1
    local build_file="${module}/build.gradle.kts"
    
    print_info "检查Git差异: $module"
    
    # 获取差异统计
    local changes=$(git diff --stat "$build_file" 2>/dev/null || echo "未提交")
    
    if [ "$changes" = "未提交" ]; then
        print_warning "文件未提交到Git"
    else
        echo "$changes"
        
        # 检查是否只有版本相关的改动
        local non_version_changes=$(git diff "$build_file" | grep -E "^\+|^-" | grep -v "version\|libs\." | grep -v "^+++\|^---" || true)
        
        if [ -n "$non_version_changes" ]; then
            print_warning "发现非版本相关的改动:"
            echo "$non_version_changes"
        else
            print_success "只包含版本相关改动"
        fi
    fi
}

# 函数：生成迁移报告
generate_report() {
    local module=$1
    local status=$2
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    
    cat >> migration_report.txt << EOF
模块: $module
时间: $timestamp
状态: $status
---
EOF
}

# 主函数
main() {
    echo "==================================="
    echo "版本目录迁移验证脚本"
    echo "==================================="
    
    # 检查参数
    if [ $# -eq 0 ]; then
        echo "用法: $0 <module_path>"
        echo "示例: $0 shared/user"
        exit 1
    fi
    
    local module=$1
    local all_passed=true
    
    echo ""
    echo "开始验证: $module"
    echo "-----------------------------------"
    
    # 1. 检查硬编码版本
    if ! check_hardcoded_versions "$module"; then
        all_passed=false
    fi
    echo ""
    
    # 2. 验证编译
    if ! verify_module_build "$module"; then
        all_passed=false
    fi
    echo ""
    
    # 3. 检查Git差异
    check_git_diff "$module"
    echo ""
    
    # 4. 生成报告
    if $all_passed; then
        print_success "✨ 模块迁移验证通过！"
        generate_report "$module" "PASSED"
    else
        print_error "💥 模块迁移验证失败！"
        generate_report "$module" "FAILED"
        exit 1
    fi
}

# 执行主函数
main "$@"
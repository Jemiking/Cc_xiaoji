#!/bin/bash

# 编译辅助脚本 - MCP服务器的临时替代方案

echo "=== Kotlin编译辅助工具 ==="
echo ""

# 显示使用说明
show_usage() {
    echo "使用方法："
    echo "  ./scripts/compile-helper.sh [命令] [参数]"
    echo ""
    echo "命令："
    echo "  build         - 编译整个项目"
    echo "  module <name> - 编译指定模块"
    echo "  clean         - 清理项目"
    echo "  check         - 检查Gradle环境"
    echo "  help          - 显示此帮助信息"
    echo ""
    echo "示例："
    echo "  ./scripts/compile-helper.sh build"
    echo "  ./scripts/compile-helper.sh module feature-ledger"
    echo "  ./scripts/compile-helper.sh clean"
}

# 检查Gradle环境
check_gradle() {
    echo "检查Gradle环境..."
    ./gradlew --version
    echo ""
    echo "Android SDK路径："
    echo "  ANDROID_HOME: $ANDROID_HOME"
    echo "  ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"
}

# 编译整个项目
build_project() {
    echo "编译整个项目..."
    ./gradlew build -x lint -x processDebugManifest -x processDebugResources -x mergeDebugResources --console=plain
}

# 编译指定模块
build_module() {
    local module=$1
    if [ -z "$module" ]; then
        echo "错误：请指定模块名称"
        echo "示例：./scripts/compile-helper.sh module feature-ledger"
        exit 1
    fi
    
    echo "编译模块: $module"
    ./gradlew :$module:compileDebugKotlin -x lint --console=plain
}

# 清理项目
clean_project() {
    echo "清理项目..."
    ./gradlew clean --console=plain
}

# 主逻辑
case "$1" in
    build)
        build_project
        ;;
    module)
        build_module "$2"
        ;;
    clean)
        clean_project
        ;;
    check)
        check_gradle
        ;;
    help|--help|-h|"")
        show_usage
        ;;
    *)
        echo "错误：未知命令 '$1'"
        echo ""
        show_usage
        exit 1
        ;;
esac
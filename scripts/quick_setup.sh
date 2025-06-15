#!/bin/bash
# CC小记WSL环境快速设置脚本

echo "================================================="
echo "         CC小记 WSL 环境快速设置"
echo "================================================="
echo ""

# 检查SDKMAN
if [ ! -d "$HOME/.sdkman" ]; then
    echo "⚠️  SDKMAN未安装"
    echo "建议先运行: ./scripts/setup_wsl_build_env.sh"
    exit 1
fi

# 加载SDKMAN
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 检查Java
echo "检查Java环境..."
if ! sdk list java | grep -q "17.0.15-tem"; then
    echo "安装Java 17..."
    sdk install java 17.0.15-tem
fi
sdk use java 17.0.15-tem

# 检查Gradle
echo "检查Gradle环境..."
if ! sdk list gradle | grep -q "8.4"; then
    echo "安装Gradle 8.4..."
    sdk install gradle 8.4
fi
sdk use gradle 8.4

# 设置Android SDK路径
export ANDROID_HOME="/mnt/c/Users/Hua/AppData/Local/Android/Sdk"
export PATH="$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH"

# 创建环境配置文件
cat > ~/.ccxiaoji_env << 'EOF'
# CC小记项目环境配置
export ANDROID_HOME="/mnt/c/Users/Hua/AppData/Local/Android/Sdk"
export PATH="$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH"

# 项目目录
export CC_PROJECT_DIR="/mnt/d/kotlin/Cc_xiaoji"

# Gradle优化
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=512m -XX:+UseParallelGC"

# 便捷命令
alias ccx='cd $CC_PROJECT_DIR'

# 加载SDKMAN
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"
EOF

# 添加到bashrc
if ! grep -q "ccxiaoji_env" ~/.bashrc; then
    echo "" >> ~/.bashrc
    echo "# CC小记项目环境" >> ~/.bashrc
    echo "[[ -f ~/.ccxiaoji_env ]] && source ~/.ccxiaoji_env" >> ~/.bashrc
fi

# 创建构建脚本
cat > ~/ccx-build.sh << 'EOF'
#!/bin/bash
# CC小记构建脚本

cd /mnt/d/kotlin/Cc_xiaoji

case "$1" in
    "build")
        echo "构建项目..."
        gradle build
        ;;
    "clean")
        echo "清理项目..."
        gradle clean
        ;;
    "test")
        echo "运行测试..."
        gradle test
        ;;
    "quick")
        echo "快速编译..."
        gradle compileDebugKotlin -x test
        ;;
    *)
        echo "用法: $0 {build|clean|test|quick}"
        echo "  build - 完整构建"
        echo "  clean - 清理缓存"
        echo "  test  - 运行测试"
        echo "  quick - 快速编译"
        ;;
esac
EOF

chmod +x ~/ccx-build.sh

echo ""
echo "✅ 环境设置完成！"
echo ""
echo "可用命令："
echo "  ccx              - 进入项目目录"
echo "  ~/ccx-build.sh   - 构建脚本"
echo ""
echo "请执行以下命令使配置生效："
echo "  source ~/.bashrc"
echo ""
echo "然后可以使用："
echo "  ccx"
echo "  gradle build"
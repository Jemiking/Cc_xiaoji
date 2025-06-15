#!/bin/bash
# WSL构建环境配置脚本 - 使用SDKMAN方案

echo "========================================"
echo "CC小记 WSL构建环境配置脚本"
echo "使用SDKMAN管理Java和Gradle版本"
echo "========================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查函数
check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓ $1 已安装${NC}"
        return 0
    else
        echo -e "${RED}✗ $1 未安装${NC}"
        return 1
    fi
}

# 步骤1：检查依赖
echo -e "\n${YELLOW}步骤1：检查系统依赖${NC}"
echo "请确保已安装以下工具："
echo "sudo apt update && sudo apt install -y unzip curl zip git"

# 步骤2：安装SDKMAN
echo -e "\n${YELLOW}步骤2：安装SDKMAN${NC}"
if [ -d "$HOME/.sdkman" ]; then
    echo -e "${GREEN}SDKMAN已安装${NC}"
else
    echo "正在安装SDKMAN..."
    curl -s "https://get.sdkman.io" | bash
    echo -e "${GREEN}SDKMAN安装完成${NC}"
fi

# 初始化SDKMAN
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

# 步骤3：安装Java 17
echo -e "\n${YELLOW}步骤3：安装Java 17${NC}"
if check_command java; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "当前Java版本: $JAVA_VERSION"
    if [[ $JAVA_VERSION == 17* ]]; then
        echo -e "${GREEN}Java 17已安装${NC}"
    else
        echo "需要安装Java 17..."
        sdk install java 17.0.9-tem
    fi
else
    echo "正在安装Java 17..."
    sdk install java 17.0.9-tem
fi

# 步骤4：安装Gradle
echo -e "\n${YELLOW}步骤4：安装Gradle${NC}"
if check_command gradle; then
    GRADLE_VERSION=$(gradle --version | grep Gradle | cut -d' ' -f2)
    echo "当前Gradle版本: $GRADLE_VERSION"
else
    echo "正在安装Gradle 8.4..."
    sdk install gradle 8.4
fi

# 步骤5：配置Android SDK
echo -e "\n${YELLOW}步骤5：配置Android SDK${NC}"
echo "请选择Android SDK配置方式："
echo "1) 使用Windows的Android SDK (推荐，如果已安装Android Studio)"
echo "2) 在WSL中安装Android SDK命令行工具"
echo "3) 跳过Android SDK配置"

read -p "请选择 (1/2/3): " SDK_CHOICE

case $SDK_CHOICE in
    1)
        echo "配置Windows Android SDK路径..."
        read -p "请输入你的Windows用户名: " WIN_USERNAME
        ANDROID_SDK_PATH="/mnt/c/Users/$WIN_USERNAME/AppData/Local/Android/Sdk"
        
        if [ -d "$ANDROID_SDK_PATH" ]; then
            echo -e "${GREEN}找到Android SDK: $ANDROID_SDK_PATH${NC}"
            export ANDROID_HOME="$ANDROID_SDK_PATH"
        else
            echo -e "${RED}未找到Android SDK，请检查路径${NC}"
            echo "尝试其他可能的路径..."
            # 尝试其他常见路径
            for path in "/mnt/c/Android/Sdk" "/mnt/d/Android/Sdk" "/mnt/c/android-sdk"; do
                if [ -d "$path" ]; then
                    echo -e "${GREEN}找到Android SDK: $path${NC}"
                    export ANDROID_HOME="$path"
                    break
                fi
            done
        fi
        ;;
    2)
        echo "在WSL中安装Android SDK..."
        mkdir -p ~/Android/Sdk
        cd ~/Android/Sdk
        
        # 下载命令行工具
        echo "下载Android命令行工具..."
        wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
        unzip -q commandlinetools-linux-9477386_latest.zip
        mkdir -p cmdline-tools/latest
        mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
        
        export ANDROID_HOME="$HOME/Android/Sdk"
        export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"
        
        # 接受许可证并安装基本组件
        yes | sdkmanager --licenses
        sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
        
        cd - > /dev/null
        ;;
    3)
        echo "跳过Android SDK配置"
        ;;
esac

# 步骤6：创建环境配置文件
echo -e "\n${YELLOW}步骤6：创建环境配置文件${NC}"
cat > ~/.ccxiaoji_env << EOF
# CC小记构建环境配置
export SDKMAN_DIR="\$HOME/.sdkman"
[[ -s "\$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "\$HOME/.sdkman/bin/sdkman-init.sh"

# Java配置
export JAVA_HOME=\$HOME/.sdkman/candidates/java/current

# Android SDK配置
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="\$PATH:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/cmdline-tools/latest/bin"

# Gradle配置
export GRADLE_USER_HOME=\$HOME/.gradle

# 项目路径
export CCXIAOJI_HOME="/mnt/d/kotlin/Cc_xiaoji"

# 别名
alias ccx="cd \$CCXIAOJI_HOME"
alias gradlew="\$CCXIAOJI_HOME/gradlew"
EOF

# 步骤7：更新shell配置
echo -e "\n${YELLOW}步骤7：更新shell配置${NC}"
if ! grep -q "ccxiaoji_env" ~/.bashrc; then
    echo "" >> ~/.bashrc
    echo "# CC小记构建环境" >> ~/.bashrc
    echo "[ -f ~/.ccxiaoji_env ] && source ~/.ccxiaoji_env" >> ~/.bashrc
    echo -e "${GREEN}已更新~/.bashrc${NC}"
fi

# 步骤8：创建构建脚本
echo -e "\n${YELLOW}步骤8：创建便捷构建脚本${NC}"
cat > ~/ccx-build.sh << 'EOF'
#!/bin/bash
# CC小记构建脚本

cd /mnt/d/kotlin/Cc_xiaoji

# 确保gradlew可执行
chmod +x gradlew

# 修复换行符问题
if file gradlew | grep -q "CRLF"; then
    echo "修复gradlew换行符..."
    sed -i 's/\r$//' gradlew
fi

# 执行构建
./gradlew "$@"
EOF

chmod +x ~/ccx-build.sh

# 完成提示
echo -e "\n${GREEN}========================================"
echo "环境配置完成！"
echo "========================================${NC}"
echo ""
echo "请执行以下命令使配置生效："
echo -e "${YELLOW}source ~/.bashrc${NC}"
echo ""
echo "常用命令："
echo "  ccx         - 进入项目目录"
echo "  ~/ccx-build.sh build - 构建项目"
echo "  ~/ccx-build.sh :feature:schedule:compileDebugKotlin - 编译排班模块"
echo ""
echo "验证安装："
echo "  java -version"
echo "  gradle --version"
echo "  echo \$ANDROID_HOME"
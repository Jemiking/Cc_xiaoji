#!/bin/bash
# SDKMAN安装脚本

echo "=== 安装SDKMAN ==="

# 下载并安装SDKMAN
curl -s "https://get.sdkman.io" | bash

# 初始化SDKMAN
source "$HOME/.sdkman/bin/sdkman-init.sh"

echo "=== SDKMAN安装完成 ==="
echo "请执行以下命令来激活SDKMAN："
echo "source \"$HOME/.sdkman/bin/sdkman-init.sh\""
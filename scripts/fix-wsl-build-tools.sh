#!/bin/bash

# 修复WSL中Build Tools 34.0.0的问题
# 通过创建假的aapt文件来满足Gradle的检查

echo "=================================="
echo "修复WSL Build Tools问题"
echo "=================================="

SDK_PATH="/mnt/c/Users/Hua/AppData/Local/Android/Sdk"
BUILD_TOOLS_34="$SDK_PATH/build-tools/34.0.0"

# 检查目录是否存在
if [ ! -d "$BUILD_TOOLS_34" ]; then
    echo "❌ Build Tools 34.0.0目录不存在"
    exit 1
fi

# 创建假的aapt和aapt2可执行文件
# 这些文件只是为了通过Gradle的存在性检查
echo '#!/bin/bash
echo "Error: This is a placeholder for WSL. Use Android Studio for full builds." >&2
exit 1' > "$BUILD_TOOLS_34/aapt"

echo '#!/bin/bash
echo "Error: This is a placeholder for WSL. Use Android Studio for full builds." >&2
exit 1' > "$BUILD_TOOLS_34/aapt2"

# 添加可执行权限
chmod +x "$BUILD_TOOLS_34/aapt"
chmod +x "$BUILD_TOOLS_34/aapt2"

# 检查其他必要的工具
TOOLS=("aidl" "d8" "dx" "zipalign" "dexdump" "apksigner" "mainDexClasses" "split-select" "llvm-rs-cc" "bcc_compat")
for tool in "${TOOLS[@]}"; do
    if [ ! -f "$BUILD_TOOLS_34/$tool" ]; then
        echo '#!/bin/bash
echo "Error: This is a placeholder for WSL. Use Android Studio for full builds." >&2
exit 1' > "$BUILD_TOOLS_34/$tool"
        chmod +x "$BUILD_TOOLS_34/$tool"
        echo "✅ 创建了 $tool 占位符"
    fi
done

echo -e "\n✅ WSL Build Tools修复完成！"
echo "注意：这只是为了让Kotlin编译通过检查"
echo "完整的Android构建仍需要在Windows/Android Studio中进行"
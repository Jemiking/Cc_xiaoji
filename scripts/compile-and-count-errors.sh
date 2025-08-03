#!/bin/bash

echo "=== 编译app模块并统计错误 ==="
echo ""

# 编译并保存输出
echo "开始编译..."
./gradlew :app:compileDebugKotlin --no-daemon --console=plain > compile_full_output.txt 2>&1

# 提取错误信息
grep "^e: file:" compile_full_output.txt > compile_errors_only.txt

# 统计错误数量
ERROR_COUNT=$(wc -l < compile_errors_only.txt)

echo "编译完成！"
echo "总错误数: $ERROR_COUNT"
echo ""

# 显示前20个错误
echo "前20个错误："
head -20 compile_errors_only.txt

# 统计各类错误
echo ""
echo "错误类型统计："
echo "- Unresolved reference: $(grep -c "Unresolved reference" compile_errors_only.txt)"
echo "- Type mismatch: $(grep -c "Type mismatch" compile_errors_only.txt)"  
echo "- when expression: $(grep -c "when.*expression" compile_errors_only.txt)"
echo "- Constructor: $(grep -c "constructor" compile_errors_only.txt)"
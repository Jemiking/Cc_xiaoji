#!/bin/bash
# 更新MCP测试项目Main.kt文件的脚本

MCP_TEST_FILE="/mnt/d/kotlin/mcp-kotlin-compiler/test-project/src/main/kotlin/com/example/Main.kt"

echo "=== 更新MCP测试文件 ==="

# 检查文件是否存在
if [ ! -f "$MCP_TEST_FILE" ]; then
    echo "错误：找不到测试文件：$MCP_TEST_FILE"
    exit 1
fi

# 创建修复后的Main.kt内容
cat > "$MCP_TEST_FILE" << 'EOF'
// test-project/src/main/kotlin/com/example/Main.kt

package com.example

// 这是一个测试文件，用于验证MCP编译器功能

fun main() {
    println("Kotlin MCP Compiler Test Project")
    
    // 示例1：正确的类型声明
    val number: String = "42"
    println("Number: $number")
    
    // 示例2：使用列表
    val list = listOf("Kotlin", "Java", "Scala")
    println("Languages: ${list.joinToString()}")
    
    // 示例3：空安全处理
    val nullableString: String? = null
    println("Length: ${nullableString?.length ?: 0}")
    
    testFunction()
}

// 测试函数
fun testFunction() {
    // 使用变量
    val message = "This is a test message"
    println(message)
    
    // 调用函数
    val result = calculateSum(5, 3)
    println("Sum: $result")
}

// 正确的返回类型
fun calculateSum(a: Int, b: Int): String {
    return "The sum is: ${a + b}"
}

// 数据类示例
data class User(
    val id: Int,
    val name: String,
    val email: String
)

// 扩展函数示例
fun String.isValidEmail(): Boolean {
    return this.contains("@") && this.contains(".")
}

// 高阶函数示例
fun processUsers(users: List<User>, action: (User) -> Unit) {
    users.forEach { user ->
        action(user)
    }
}

// 测试用例
fun testCases() {
    // 创建用户列表
    val users = listOf(
        User(1, "Alice", "alice@example.com"),
        User(2, "Bob", "bob@example.com")
    )
    
    // 处理用户
    processUsers(users) { user ->
        println("User: ${user.name}, Email Valid: ${user.email.isValidEmail()}")
    }
}
EOF

echo "✅ Main.kt文件已更新"
echo ""
echo "文件位置：$MCP_TEST_FILE"
echo ""
echo "现在可以运行以下命令测试MCP编译器："
echo "1. cd /mnt/d/kotlin/mcp-kotlin-compiler"
echo "2. npm run build"
echo "3. cd test-project && ./gradlew clean build"
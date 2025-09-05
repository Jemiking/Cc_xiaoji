# MCP + Claude 快速开发指南

## 🚀 快速开始

### 工作流程
1. **Claude编写代码** → 2. **文件自动保存** → 3. **Android Studio编译** → 4. **反馈错误给Claude**

### 推荐设置
**Android Studio**:
- ✅ Build automatically（自动构建）
- ✅ Problems视图（查看错误）
- ✅ Structure视图（代码结构）

## 📝 常用对话模板

### 开始新任务
```
"优化 [界面名称]，应用扁平化设计，拆分组件到独立文件"
```

### 反馈编译错误
```
"编译错误：
[错误信息]
文件：[文件名:行号]"
```

### 批量处理
```
"以下文件有编译错误：
1. FileA.kt:10 - Unresolved reference
2. FileB.kt:20 - Type mismatch
请批量修复"
```

## 🛠️ 实用命令

```bash
# 在Android Studio终端执行

# 编译特定模块
./gradlew :feature:ledger:compileDebugKotlin

# 清理构建
./gradlew clean

# 查看任务列表
./gradlew tasks
```

## ⚡ 效率技巧

1. **批量操作**：一次告诉Claude多个任务
2. **明确需求**：说明具体要求（如"创建5个组件"）
3. **错误优先**：先修复编译错误，再优化代码
4. **模块化**：一次专注一个功能模块

## 🔧 问题解决

| 问题 | 解决方法 |
|------|----------|
| 依赖未找到 | 告诉Claude添加到build.gradle |
| 导入错误 | 让Claude检查并修复import语句 |
| 类型不匹配 | 提供具体错误信息 |
| 资源未找到 | 检查资源文件路径 |

## 📋 检查清单

开发前：
- [ ] Android Studio已打开
- [ ] 自动构建已启用
- [ ] Problems视图可见

开发中：
- [ ] 定期查看编译状态
- [ ] 及时反馈错误
- [ ] 保持代码整洁

开发后：
- [ ] 运行完整构建
- [ ] 检查所有警告
- [ ] 提交代码

---
**记住**：MCP的价值在于AI辅助，而非编译环境！
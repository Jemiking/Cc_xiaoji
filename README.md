# CC小记 - 综合生活管理应用

CC小记是一款基于Android的综合生活管理应用，帮助用户管理日常生活的方方面面。

## 功能特点

### 📊 记账模块
- 收支记录管理
- 多账户支持
- 自定义分类
- 预算管理
- 统计分析报表

### ✅ 待办模块
- 任务创建与管理
- 优先级设置
- 任务提醒
- 完成进度追踪

### 🎯 习惯模块
- 习惯养成追踪
- 打卡记录
- 连续天数统计
- 成就系统

### 💾 数据管理
- 本地数据备份
- 云端同步
- 数据导入导出
- 批量操作

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **数据库**: Room
- **依赖注入**: Hilt
- **异步处理**: Coroutines + Flow
- **网络请求**: Retrofit
- **工作调度**: WorkManager

## 开发环境

- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17
- Gradle 8.4
- Android SDK 34

## 构建说明

1. 克隆项目
```bash
git clone https://github.com/Jemiking/Cc_xiaoji.git
```

2. 使用Android Studio打开项目

3. 同步Gradle依赖

4. 运行应用

## 项目结构

```
app/
├── data/           # 数据层
│   ├── local/      # 本地数据库
│   ├── remote/     # 远程API
│   └── repository/ # 数据仓库
├── domain/         # 领域层
│   └── model/      # 业务模型
├── presentation/   # 表现层
│   ├── ui/         # UI组件
│   └── viewmodel/  # 视图模型
└── di/             # 依赖注入
```

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

[待定]

---
🤖 使用 [Claude Code](https://claude.ai/code) 辅助开发
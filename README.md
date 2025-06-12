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
- **架构**: MVVM + Clean Architecture（模块化架构）
- **数据库**: Room
- **依赖注入**: Hilt
- **异步处理**: Coroutines + Flow
- **网络请求**: Retrofit
- **工作调度**: WorkManager

## 🎉 架构迁移里程碑

**2025年6月12日** - 我们成功完成了从单体架构到模块化架构的迁移！

### 新的模块化架构
- **Core模块**: 提供基础设施（common、ui、database、data）
- **Feature模块**: 业务功能模块（todo、habit、ledger）
- **Shared模块**: 共享服务（user、sync、backup、notification）

详见 [架构迁移总结报告](doc/架构迁移总结报告.md)

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
Cc_xiaoji/
├── app/                    # 应用主模块（仅负责组装和全局导航）
├── core/                   # 核心基础设施模块
│   ├── common/            # 基础工具和扩展
│   ├── data/              # 共享数据层基础设施
│   ├── database/          # Room数据库
│   └── ui/                # 共享UI组件和主题
├── feature/               # 业务功能模块
│   ├── todo/              # 待办事项管理
│   │   ├── api/          # 对外接口
│   │   ├── data/         # 数据层
│   │   ├── domain/       # 领域层
│   │   └── presentation/ # 表现层
│   ├── habit/             # 习惯养成
│   └── ledger/            # 记账管理
└── shared/                # 共享业务模块
    ├── user/              # 用户管理
    ├── sync/              # 同步功能
    ├── backup/            # 备份恢复
    └── notification/      # 通知管理
```

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

[待定]

---
🤖 使用 [Claude Code](https://claude.ai/code) 辅助开发
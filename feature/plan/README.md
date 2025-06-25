# Feature Plan

## 概述
Plan功能模块，提供[请填写功能描述]。

## 架构
本模块遵循Clean Architecture + MVVM架构模式：

- **api/**: 对外公开的API接口
- **data/**: 数据层实现
  - **local/**: 本地数据存储（Room）
  - **remote/**: 远程数据访问
  - **repository/**: 数据仓库实现
- **di/**: 依赖注入配置
- **domain/**: 业务逻辑层
  - **model/**: 业务模型
  - **repository/**: 仓库接口
  - **usecase/**: 业务用例
- **presentation/**: 展示层
  - **screen/**: Compose页面
  - **component/**: 可复用组件
  - **viewmodel/**: 视图模型
  - **navigation/**: 导航相关

## 主要功能
- [ ] 功能1
- [ ] 功能2
- [ ] 功能3

## 依赖关系
- core-common: 基础工具类
- core-ui: UI组件和主题
- core-database: 数据库基础设施
- shared-user: 用户管理

## 使用方式
```kotlin
// 在app模块中注册导航
planScreen()

// 导航到Plan页面
navController.navigateToPlan()
```

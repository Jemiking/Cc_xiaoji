# 排班模块 (Schedule Module)

[![Module](https://img.shields.io/badge/module-schedule-blue)](https://github.com/ccxiaoji/cc_xiaoji)
[![Architecture](https://img.shields.io/badge/architecture-clean-green)](https://github.com/ccxiaoji/cc_xiaoji)
[![Platform](https://img.shields.io/badge/platform-android-lightgrey)](https://github.com/ccxiaoji/cc_xiaoji)

## 📋 模块概述

排班模块是CC小记的核心功能模块之一，专注于工作排班管理。该模块支持灵活的班次配置、多种排班模式、统计分析和数据导出等功能，帮助用户高效管理工作时间。

### 核心价值
- 🎯 **灵活排班**：支持单次、循环、轮班等多种排班模式
- 📊 **数据分析**：提供工时统计、班次分布等分析功能
- 📱 **移动优先**：专为移动设备优化的交互体验
- 🔔 **智能提醒**：支持排班提醒通知

## ✨ 功能特性

### 1. 班次管理
- 创建和编辑班次（名称、时间、颜色）
- 班次激活/停用
- 预设颜色选择
- 快速班次选择

### 2. 排班功能
- **单日排班**：点选日期快速设置
- **批量排班**：
  - 单次模式：指定日期范围
  - 循环模式：2-365天循环
  - 轮班模式：多班次轮换
  - 自定义模式：灵活配置
- **长按快速选择**：快速切换常用班次

### 3. 日历视图
- 月度日历展示
- 左右滑动切换月份
- 今日快速定位
- 舒适/紧凑视图模式切换
- 班次颜色标识

### 4. 统计分析
- 月度工时统计
- 班次分布图表
- 工作/休息天数统计
- 自定义时间范围分析

### 5. 数据管理
- **导出功能**：
  - CSV格式（Excel兼容）
  - JSON格式（数据交换）
  - 统计报表
- **数据备份/恢复**
- **历史记录查看**

### 6. 设置功能
- 通知提醒配置
- 提醒时间设置
- 周起始日设置
- 深色模式支持

## 🏗 架构设计

### 模块结构
```
feature/schedule/
├── api/                    # 公开API接口
│   ├── ScheduleApi.kt      # 模块功能接口
│   └── ScheduleNavigator.kt # 导航接口
├── data/                   # 数据层实现
│   ├── local/              # 本地数据
│   ├── repository/         # 仓库实现
│   ├── scheduler/          # 任务调度
│   └── worker/             # 后台任务
├── domain/                 # 领域层
│   ├── model/              # 领域模型
│   ├── repository/         # 仓库接口
│   └── usecase/            # 业务用例
└── presentation/           # 展示层
    ├── navigation/         # 导航配置
    ├── ui/                 # UI组件
    └── viewmodel/          # 状态管理
```

### 关键设计模式
- **Clean Architecture**：清晰的层次划分
- **MVVM**：数据驱动的UI更新
- **Repository Pattern**：数据访问抽象
- **UseCase Pattern**：业务逻辑封装

## 📚 API 使用指南

### ScheduleApi
```kotlin
interface ScheduleApi {
    // 获取今日排班信息
    suspend fun getTodaySchedule(): ScheduleInfo?
    
    // 获取本月排班统计
    suspend fun getMonthStatistics(): ScheduleStatistics
    
    // 导航到排班模块
    fun navigateToSchedule()
}
```

### 使用示例
```kotlin
// 在其他模块中使用
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val scheduleApi: ScheduleApi
) : ViewModel() {
    
    fun loadTodaySchedule() {
        viewModelScope.launch {
            val schedule = scheduleApi.getTodaySchedule()
            // 处理排班信息
        }
    }
}
```

## 🛠 技术栈

### 核心技术
- **Kotlin**：100% Kotlin 编写
- **Jetpack Compose**：现代化声明式UI
- **Coroutines & Flow**：异步编程和响应式数据流
- **Hilt**：依赖注入
- **Room**：本地数据持久化
- **WorkManager**：后台任务调度

### 主要依赖
```kotlin
dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:database"))
    implementation(project(":core:data"))
    
    // Shared modules
    implementation(project(":shared:notification"))
    implementation(project(":shared:backup"))
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
}
```

## 📦 核心组件

### 1. 领域模型
- **Schedule**：排班实体（日期、班次、备注、实际工时）
- **Shift**：班次实体（名称、时间、颜色、状态）
- **SchedulePattern**：排班模式（单次、循环、轮班、自定义）
- **ScheduleStatistics**：统计数据

### 2. 关键用例
- **CreateScheduleUseCase**：创建排班（支持批量）
- **GetScheduleStatisticsUseCase**：获取统计信息
- **ExportScheduleDataUseCase**：导出数据
- **ManageShiftUseCase**：班次管理

### 3. UI组件
- **CalendarView**：日历视图组件
- **QuickShiftSelector**：快速班次选择器
- **CustomDatePickerDialog**：日期选择对话框
- **ShiftEditDialog**：班次编辑对话框

## 🚀 开发指南

### 添加新功能
1. 在 domain/usecase 中创建用例
2. 在 repository 接口中定义方法
3. 在 data/repository 中实现
4. 创建/更新 ViewModel
5. 实现 UI 组件

### 代码规范
- 遵循 [Kotlin 编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 使用 ktlint 进行代码格式化
- 保持函数简洁（建议不超过30行）
- 添加必要的 KDoc 注释

### 测试策略
- 单元测试：UseCase 和 ViewModel
- UI测试：关键用户流程
- 集成测试：Repository 层

## 📈 性能优化

### 已实施的优化
- 使用 `StateFlow` 避免不必要的重组
- 日历数据懒加载
- 使用 `remember` 缓存计算结果
- 合理使用 `@Stable` 和 `@Immutable`

### 优化建议
- 大数据集使用分页
- 避免在 Composable 中进行复杂计算
- 使用 `derivedStateOf` 处理派生状态

## 🔄 版本历史

### v1.0.0 (2025-06)
- ✅ 从独立项目迁移到模块化架构
- ✅ 完成所有核心功能
- ✅ 支持多种排班模式
- ✅ 实现数据导出功能

## 📝 待办事项

- [ ] 添加排班模板功能
- [ ] 支持多人排班协同
- [ ] 增加更多统计维度
- [ ] 优化大数据量性能
- [ ] 支持国际化

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本模块作为 CC小记 项目的一部分，遵循项目整体的许可证。

---

**最后更新**：2025-06-13
# Feature Todo Module

Todo功能模块，提供任务管理功能。

## 模块结构

```
feature/todo/
├── api/                      # 对外暴露的API接口
│   └── TodoApi.kt           # Todo模块公共接口
├── data/                     # 数据层实现
│   ├── TodoApiImpl.kt       # API接口实现
│   ├── repository/          # 数据仓库
│   └── di/                  # 依赖注入
├── domain/                   # 业务逻辑层
│   └── model/               # 领域模型
│       └── Task.kt          # 任务实体
└── presentation/            # 表示层
    ├── viewmodel/           # 视图模型
    ├── ui/                  # UI组件
    └── navigation/          # 导航定义
```

## 主要组件

### API层
- `TodoApi`: 模块对外暴露的接口，包含获取任务信息和导航功能

### Data层
- `TaskRepository`: 任务数据仓库，处理数据存取
- `TodoApiImpl`: TodoApi接口的具体实现
- `TodoModule`: Hilt依赖注入配置

### Domain层
- `Task`: 任务领域模型
- `Priority`: 任务优先级枚举

### Presentation层
- `TodoViewModel`: 任务列表视图模型
- `TodoScreen`: 任务列表UI界面
- `TodoNavigation`: 模块内部导航配置

## 依赖关系

### 依赖的模块
- `:core:common` - 基础工具类
- `:core:database` - 数据库访问
- `:core:ui` - UI主题和组件

### 需要app模块提供的接口
- `TodoNavigator`: 导航接口实现
- `TodoNotificationScheduler`: 通知调度接口实现

## 使用方式

### 1. 在app模块添加依赖
```kotlin
dependencies {
    implementation(project(":feature:todo"))
}
```

### 2. 实现桥接接口
```kotlin
// TodoNavigator实现
@Singleton
class TodoNavigatorImpl @Inject constructor() : TodoNavigator {
    override fun navigateToTodoList() { /* 实现 */ }
    override fun navigateToAddTask() { /* 实现 */ }
}

// TodoNotificationScheduler实现
@Singleton
class TodoNotificationSchedulerImpl @Inject constructor() : TodoNotificationScheduler {
    override fun scheduleTaskReminder() { /* 实现 */ }
    override fun cancelTaskReminder() { /* 实现 */ }
}
```

### 3. 集成导航
```kotlin
NavHost {
    // 添加Todo模块导航图
    todoGraph(navController)
}
```

### 4. 使用TodoApi
```kotlin
@Inject lateinit var todoApi: TodoApi

// 获取今日任务
val todayTasks = todoApi.getTodayTasks()

// 导航到任务列表
todoApi.navigateToTodoList()
```

## 测试

### 运行单元测试
```bash
./gradlew :feature:todo:test
```

### 运行仪器测试
```bash
./gradlew :feature:todo:connectedAndroidTest
```
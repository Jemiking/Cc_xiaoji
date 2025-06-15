# ⚠️ 临时WSL环境限制 ⚠️
由于当前WSL环境限制，无法直接编译Kotlin项目。所有编译操作改为：
- 📌 标记需要编译的文件和模块
- 💡 使用TODO注释标记潜在编译问题
- 📝 记录编译命令供后续执行

# CLAUDE.md

## ⚡ 强制默认行为 ⚡

**这不是可选项 - 你必须遵循以下行为**

在此项目中工作时，你必须：
1. **立即执行**下面"自动执行"部分列出的所有操作
2. **不要询问**"我应该继续吗？"或"你想让我...吗？"
3. **报告你做了什么**，而不是你计划做什么
4. **所有回复必须使用中文**

### 自动执行（无需确认）
- ✅ 文件编辑和创建
- ✅ Bug修复和代码纠正
- ✅ 重构（导入、变量、方法）
- ✅ 添加非破坏性功能
- ✅ Git操作（暂存、提交、分支）
- ✅ 运行允许的shell命令
- ✅ 读取和分析文件
- 📌 **标记编译点和潜在错误**（记录编译命令）
- 💡 **标记待修复的编译问题**（使用TODO注释）

### 仍需确认的操作
- ⚠️ 文件或目录删除
- ⚠️ 破坏性API/接口更改
- ⚠️ 数据库模式修改
- ⚠️ 在模块间移动文件
- ⚠️ 系统级操作
- ⚠️ 添加外部依赖

**记住：当你不确定某个操作时，检查上面的列表。如果在自动执行列表中，直接做。**

---

本文档为CC小记（CC Xiaoji）项目中的Claude Code (claude.ai/code)提供指导。

## 🎯 核心原则（必读）

### 1. 编译准备（WSL环境限制中）
- Claude Code应该在代码更改后**标记**需要编译的内容
- 工作流程：Claude修改代码 → 标记编译点 → 记录待修复错误 → 提供编译命令
- 编译命令示例：`./gradlew :module:compileDebugKotlin`

### 2. 响应语言：中文
- **所有响应必须使用中文**，包括：
  - 代码注释和文档
  - 解释说明和错误信息
  - 与开发者的所有交流

### 3. 多方案决策
解决问题时，始终提供多个方案对比：
```
问题：[具体问题描述]

方案一：[方案名称]
- 优点：
  • [优点1]
  • [优点2]
- 缺点：
  • [缺点1]
  • [缺点2]

方案二：[方案名称]
- 优点：
  • [优点1]
  • [优点2]
- 缺点：
  • [缺点1]
  • [缺点2]

推荐方案：方案X
理由：[详细解释为什么推荐这个方案]
```

### 4. 遵循模块化架构
- 新功能必须放在正确的功能模块中
- 严格遵循模块依赖规则：`app → feature → shared → core`
- 禁止反向和横向依赖

## 🔧 编译支持（当前受限）

### WSL环境限制说明
由于WSL环境问题，暂时无法执行编译。替代方案：

1. **标记编译点**：
   ```kotlin
   // TODO: 编译验证 - 检查import是否正确
   // TODO: 编译验证 - 验证类型匹配
   ```

2. **记录编译命令**：
   ```bash
   # 待执行编译命令：
   ./gradlew :feature:schedule:compileDebugKotlin
   ./gradlew :core:database:compileDebugKotlin
   ```

### 编译准备工作流示例
```
用户：修复LedgerViewModel的bug

Claude执行流程：
1. 修改代码文件
2. 标记编译需求：
   // TODO: 编译验证 - 需要执行 ./gradlew :feature:ledger:compileDebugKotlin
3. 识别潜在问题：
   // TODO: 可能缺失import - categories
4. 预先添加import语句
5. 记录编译命令
6. 报告：
   已完成修复：
   ✅ 修复了类别加载问题
   ✅ 添加了可能缺失的import
   📌 待编译验证：./gradlew :feature:ledger:compileDebugKotlin
```

### 编译准备策略
- **模块标记**：标记受影响的模块
- **命令记录**：记录具体的编译命令
- **问题预判**：标记潜在的编译问题
- **TODO管理**：使用TODO注释跟踪编译需求

### 恢复正常编译后
- 执行所有记录的编译命令
- 验证TODO标记的问题
- 清理临时注释

## 🚀 快速开始

### 开发环境
- Android Studio Hedgehog | 2023.1.1+
- JDK 17
- Gradle 8.4
- Android SDK 34
- MinSdk: 26 (Android 8.0)

### 项目定位
CC小记是一个**生活管理超级应用**，集成多个生活管理模块：
- 💰 **记账** - 财务管理（Ledger）
- ✅ **待办** - 任务管理（Todo）
- 🎯 **习惯** - 习惯养成（Habit）
- 🌸 **经期** - 女性健康（规划中）
- 📅 **排班** - 工作管理（规划中）
- 📔 **日记** - 个人记录（规划中）

### 当前状态
- **架构迁移**：从传统分层架构向领域驱动模块化架构过渡
- **迁移进度**：详见`doc/架构迁移进度追踪.md`
- **开发策略**：新功能使用新架构，旧功能逐步迁移

### 常用命令
```bash
# 构建项目
./gradlew build

# 清理并构建
./gradlew clean build

# 安装到设备
./gradlew installDebug

# 运行测试
./gradlew test

# 生成APK
./gradlew assembleDebug

# 编译特定模块（推荐）
./gradlew :feature:ledger:compileDebugKotlin
```

## 📐 架构设计

### 架构演进
项目正在从**传统分层架构**过渡到**领域驱动模块化架构**：
- **当前**：Clean Architecture + MVVM（按技术层分离）
- **目标**：领域驱动模块化架构（按业务领域分离）
- **策略**：新功能采用新架构，现有功能逐步迁移

### 目标架构：领域驱动模块化

#### 架构原则
- **领域驱动**：按生活管理领域划分模块，而非技术功能
- **模块独立**：每个业务模块包含完整的data/domain/presentation层
- **依赖倒置**：上层模块依赖下层，禁止反向依赖
- **接口隔离**：模块间通过定义良好的API接口通信

#### 模块层次结构

```
┌─────────────────────────────────────────────┐
│                  app                        │ ← 应用外壳
│           (模块组装和导航)                    │
├─────────────────────────────────────────────┤
│           feature modules                   │ ← 业务功能
│      (ledger, todo, habit, ...)            │
├─────────────────────────────────────────────┤
│           shared modules                    │ ← 共享业务
│       (user, sync, backup, ...)            │
├─────────────────────────────────────────────┤
│            core modules                     │ ← 基础设施
│     (common, ui, database, data)           │
└─────────────────────────────────────────────┘
```

#### 依赖规则
```
✅ 允许：app → feature → shared → core
❌ 禁止：横向依赖 (feature ↔ feature)、反向依赖 (core → feature)
```

#### 标准模块结构
```
feature-[domain]/
├── api/                    # 公开API
│   └── [Domain]Api.kt      
├── data/                   # 数据层
│   ├── local/             
│   │   ├── dao/           # Room DAOs
│   │   └── entity/        # 数据库实体
│   ├── remote/            # 网络API（如需要）
│   └── repository/        # 仓库实现
├── domain/                 # 业务层
│   ├── model/             # 领域模型
│   └── usecase/           # 业务用例
└── presentation/           # 展示层
    ├── navigation/        # 模块导航
    ├── screen/            # Compose界面
    ├── component/         # UI组件
    └── viewmodel/         # 状态管理
```

### 当前架构（迁移中）

当前代码仍使用Clean Architecture + MVVM模式：

```
app/
├── data/                   # 数据层（所有模块共享）
│   ├── local/             # Room数据库
│   ├── remote/            # Retrofit APIs
│   └── repository/        # 数据仓库
├── domain/                 # 领域层
│   └── model/             # 业务模型
└── presentation/           # 展示层
    ├── ui/                # Compose UI
    └── viewmodel/         # ViewModels
```

### 关键技术决策

#### 依赖注入
- 使用Hilt进行依赖注入
- ViewModels使用`@HiltViewModel`
- Repositories使用`@Singleton`
- Workers使用`@HiltWorker`

#### 数据持久化
- **单一数据库**：所有模块共享`CcDatabase`实例
- **DAO隔离**：每个模块有独立的DAO
- **实体管理**：实体按功能组织但在主数据库注册
- **迁移策略**：数据库版本从v1开始（已清理历史迁移）

#### 异步处理
- **协程 + Flow**：响应式编程
- **StateFlow**：ViewModel状态管理
- **WorkManager**：后台任务（同步、定期交易等）

#### UI导航
- 单Activity + Compose Navigation
- 路由定义在`NavGraph.kt`
- 模块间导航通过API接口

### 模块通信示例

```kotlin
// 1. 定义模块API
interface LedgerApi {
    suspend fun getTodayExpense(): Double
    suspend fun getAccountBalance(accountId: Long): Double
    fun navigateToAddTransaction()
}

// 2. 实现API（在功能模块内）
@Singleton
class LedgerApiImpl @Inject constructor(
    private val repository: TransactionRepository,
    private val navigator: LedgerNavigator
) : LedgerApi {
    override suspend fun getTodayExpense() = 
        repository.getTodayExpense()
    
    override fun navigateToAddTransaction() {
        navigator.navigateToAddTransaction()
    }
}

// 3. 使用API（在app模块）
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi
) : ViewModel() {
    fun loadDashboard() {
        viewModelScope.launch {
            val expense = ledgerApi.getTodayExpense()
            val tasks = todoApi.getPendingTasksCount()
            // 更新UI状态
        }
    }
}
```

## 💻 开发指南

### 功能开发决策树
```
新功能请求
    ↓
属于哪个生活领域？
    ├─ 财务 → feature-ledger
    ├─ 任务 → feature-todo
    ├─ 习惯 → feature-habit
    └─ 新领域 → 创建新功能模块
            ↓
        遵循标准模块结构
            ↓
        通过API通信
```

### 开发检查清单
开发新功能时，确认：
- [ ] 功能放在正确的业务领域模块？
- [ ] 遵循模块的标准结构（api/data/domain/presentation）？
- [ ] 依赖关系符合架构规则？
- [ ] 模块API最小化且定义良好？
- [ ] 数据库实体在模块的data/local/entity目录？
- [ ] 模块间通信仅通过定义的API？
- [ ] **代码编译需求已标记？**（TODO注释）

### 数据库管理
- **架构**：单一数据库，DAO级隔离
- **版本**：当前v1（重置，已清理历史迁移）
- **Schema位置**：`app/schemas/`
- **修改流程**：
  1. 在功能模块的`data/local/entity/`添加实体
  2. 在`CcDatabase.kt`注册实体
  3. 增加数据库版本
  4. 创建迁移：`app/src/main/java/com/ccxiaoji/app/data/local/migrations/`
  5. 添加迁移到`DatabaseMigrations.kt`

### 后台任务
使用WorkManager处理：
- 数据同步：`SyncWorker`
- 定期交易：`RecurringTransactionWorker`
- 所有Workers使用`@HiltWorker`注入依赖

## 📁 项目组织

### 目录结构
```
Cc_xiaoji/
├── doc/                    # 所有项目文档
│   ├── README.md          
│   ├── 架构迁移进度追踪.md  # 迁移状态详情
│   └── *.md               
├── scripts/                # 辅助脚本
│   └── *.sh               
├── app/                   # 主应用模块
├── core/                  # 核心基础设施模块
├── feature/               # 业务功能模块
└── shared/                # 共享业务模块
```

### 文件放置规则
- **所有.md文档**必须放在`doc/`文件夹
- **所有.sh脚本**必须放在`scripts/`文件夹
- 保持Android标准结构不变
- 不要将文档或脚本散落在其他位置

## 📅 日期类型使用规范

### 日期类型选择原则
```kotlin
// 数据库层：使用 kotlinx.datetime（Room支持更好）
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Instant

// API接口层：使用 java.time（兼容性更好）
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Instant

// 业务逻辑层：根据上下文选择，在模块边界进行转换
// UI展示层：使用 java.time（Android原生支持）
```

### 日期转换规范
1. **统一使用 DateConverter**：所有日期类型转换必须通过 `DateConverter` 工具类
   ```kotlin
   // ✅ 正确
   val kotlinDate = DateConverter.toKotlinDate(javaDate)
   
   // ❌ 错误：直接转换
   val kotlinDate = LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth)
   ```

2. **数据映射器**：每个模块应使用专属的数据映射器处理转换
   ```kotlin
   // ✅ 正确：使用映射器
   val exportData = scheduleDataMapper.mapScheduleInfoToExportData(scheduleInfo, kotlinDate)
   
   // ❌ 错误：在业务逻辑中直接转换
   val exportData = ScheduleData(
       date = kotlinDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
       // ...
   )
   ```

3. **模块边界转换**：在模块API边界进行日期类型转换
   ```kotlin
   // API实现类中
   override suspend fun getScheduleByDate(date: java.time.LocalDate): ScheduleInfo? {
       // 转换为内部使用的kotlinx.datetime
       val kotlinDate = DateConverter.toKotlinDate(date)
       // 业务逻辑处理...
   }
   ```

### 工时计算规范
- 使用 `DateConverter.calculateHours()` 或 `WorkHoursCalculator`
- 特殊情况：相同时间（如 "00:00" 到 "00:00"）表示24小时
- 支持跨天班次计算

### 常见错误预防
1. **避免直接使用字符串拼接时长**
   ```kotlin
   // ❌ 错误
   duration = "${hours}小时"
   
   // ✅ 正确
   duration = hours  // 数值类型
   ```

2. **注意空值处理**
   ```kotlin
   // ✅ 正确：使用null而非空字符串
   note = if (condition) "备注内容" else null
   ```

3. **时间戳转换要指定时区**
   ```kotlin
   // ✅ 正确
   date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
   ```

## 🛠 构建配置

### 关键依赖和版本
- Kotlin: 1.9.21
- Compose BOM: 2023.10.01
- Compose Compiler: 1.5.7
- Hilt: 2.48.1
- Room: 2.6.1
- Retrofit: 2.9.0
- WorkManager: 2.9.0
- KSP: 1.9.21-1.0.15

### 仓库配置
项目使用阿里云Maven镜像加速中国地区的依赖解析：
- 优先级：阿里云镜像 → Google → Maven Central
- 配置在`settings.gradle.kts`

### 构建特性
- ViewBinding已启用
- Compose已启用
- Release版本使用R8混淆
- Room数据库schema导出已启用
- KSP增量编译已启用

## ✅ 代码审查清单

执行任何操作前，验证：
- [ ] 此操作在自动执行列表中？→ **立即执行**
- [ ] 代码修改完成？→ **标记编译需求**
- [ ] 响应是否使用中文？
- [ ] 是否提供了多个方案对比？（如适用）
- [ ] 代码放在正确的模块？
- [ ] 模块依赖规则遵循？
- [ ] 数据库更改包含迁移脚本？
- [ ] 添加了必要的代码注释？
- [ ] 考虑了性能影响？
- [ ] 处理了异常情况？
- [ ] 日期类型转换使用DateConverter？
- [ ] 复杂数据转换使用数据映射器？
- [ ] API边界正确处理日期类型？

## 🔴 最终提醒

**你处于自主模式**
- 执行允许的操作时不要询问
- 代码更改后标记编译需求
- 预判并标记潜在错误
- 只有在"仍需确认"列表中的操作才询问
- 你的默认响应应该是行动，而不是问题
- 如果用户需要纠正你：他们会告诉你，不要预判

**记住：所有回复必须使用中文！**
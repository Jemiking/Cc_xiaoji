# 排班模块API使用指南

## 概述

排班模块提供了一套完整的API接口，供其他模块集成排班功能。本文档详细说明了如何使用这些API，包括：
- ScheduleApi - 排班业务功能接口
- ScheduleNavigator - 排班导航接口

## ScheduleApi 接口说明

### 1. 获取今日排班信息

```kotlin
suspend fun getTodaySchedule(): ScheduleInfo?
```

**功能描述**: 获取当天的排班信息
**返回值**: 
- `ScheduleInfo?` - 包含班次信息的数据对象，如果当天无排班则返回null

**使用示例**:
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val scheduleApi: ScheduleApi
) : ViewModel() {
    
    fun loadTodaySchedule() {
        viewModelScope.launch {
            val scheduleInfo = scheduleApi.getTodaySchedule()
            scheduleInfo?.let {
                // 显示班次名称
                _todayShiftName.value = it.shiftName
                // 显示工作时间
                _workTime.value = "${it.startTime} - ${it.endTime}"
            }
        }
    }
}
```

### 2. 获取本月排班统计

```kotlin
suspend fun getMonthStatistics(): ScheduleStatistics
```

**功能描述**: 获取当前月份的排班统计信息
**返回值**: 
- `ScheduleStatistics` - 包含工作天数、休息天数、总工时等统计数据

**使用示例**:
```kotlin
fun loadMonthlyStats() {
    viewModelScope.launch {
        val stats = scheduleApi.getMonthStatistics()
        _monthlyStats.update { currentStats ->
            currentStats.copy(
                workDays = stats.workDays,
                restDays = stats.restDays,
                totalHours = stats.totalHours
            )
        }
    }
}
```

### 3. 获取所有排班信息流

```kotlin
fun getAllSchedules(): Flow<List<Schedule>>
```

**功能描述**: 获取所有排班信息的响应式数据流
**返回值**: 
- `Flow<List<Schedule>>` - 排班列表的Flow，当数据变化时自动更新

**使用示例**:
```kotlin
class ScheduleListViewModel @Inject constructor(
    private val scheduleApi: ScheduleApi
) : ViewModel() {
    
    val schedules = scheduleApi.getAllSchedules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
```

### 4. 导航到排班模块

```kotlin
fun navigateToSchedule()
```

**功能描述**: 导航到排班模块主页面
**使用示例**:
```kotlin
// 在首页点击排班卡片时
@Composable
fun ScheduleCard(scheduleApi: ScheduleApi) {
    Card(
        modifier = Modifier.clickable {
            scheduleApi.navigateToSchedule()
        }
    ) {
        // 卡片内容
    }
}
```

## ScheduleNavigator 接口说明

ScheduleNavigator提供了排班模块内部的导航功能：

```kotlin
interface ScheduleNavigator {
    fun navigateToScheduleHome()
    fun navigateToShiftManage()
    fun navigateToScheduleEdit(date: LocalDate)
    fun navigateToSchedulePattern()
    fun navigateToStatistics()
    fun navigateToSettings()
    fun navigateUp()
}
```

### 导航方法说明

| 方法 | 功能描述 | 参数说明 |
|------|---------|----------|
| `navigateToScheduleHome()` | 导航到排班主页 | 无 |
| `navigateToShiftManage()` | 导航到班次管理页面 | 无 |
| `navigateToScheduleEdit(date)` | 导航到排班编辑页面 | date: 要编辑的日期 |
| `navigateToSchedulePattern()` | 导航到批量排班页面 | 无 |
| `navigateToStatistics()` | 导航到统计分析页面 | 无 |
| `navigateToSettings()` | 导航到设置页面 | 无 |
| `navigateUp()` | 返回上一页 | 无 |

## 依赖注入配置

### 1. 在App模块添加依赖

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":feature:schedule:api"))
}
```

### 2. 创建Navigator实现

```kotlin
// app/src/main/java/com/ccxiaoji/app/navigation/ScheduleNavigatorImpl.kt
@Singleton
class ScheduleNavigatorImpl @Inject constructor(
    @ApplicationScope private val navigationManager: NavigationManager
) : ScheduleNavigator {
    
    override fun navigateToScheduleHome() {
        navigationManager.navigate("schedule_home")
    }
    
    override fun navigateToScheduleEdit(date: LocalDate) {
        navigationManager.navigate("schedule_edit/${date.toString()}")
    }
    
    // 其他方法实现...
}
```

### 3. 配置Hilt模块

```kotlin
// app/src/main/java/com/ccxiaoji/app/di/NavigationModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {
    
    @Binds
    abstract fun bindScheduleNavigator(
        impl: ScheduleNavigatorImpl
    ): ScheduleNavigator
}
```

## 数据模型说明

### ScheduleInfo
```kotlin
data class ScheduleInfo(
    val date: LocalDate,
    val shiftName: String,
    val shiftColor: Int,
    val startTime: String?,
    val endTime: String?,
    val duration: Float,
    val note: String?
)
```

### ScheduleStatistics
```kotlin
data class ScheduleStatistics(
    val yearMonth: YearMonth,
    val workDays: Int,
    val restDays: Int,
    val totalHours: Float,
    val shiftDistribution: Map<String, Int>
)
```

### Schedule
```kotlin
data class Schedule(
    val id: Long = 0,
    val date: LocalDate,
    val shift: Shift,
    val note: String? = null,
    val actualStartTime: String? = null,
    val actualEndTime: String? = null
)
```

### Shift
```kotlin
data class Shift(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val startTime: String? = null,
    val endTime: String? = null,
    val duration: Float = 0f,
    val isActive: Boolean = true
)
```

## 最佳实践

### 1. 错误处理

```kotlin
fun loadScheduleData() {
    viewModelScope.launch {
        try {
            val schedule = scheduleApi.getTodaySchedule()
            // 处理数据
        } catch (e: Exception) {
            // 处理错误
            _errorMessage.value = "加载排班信息失败"
        }
    }
}
```

### 2. 缓存策略

使用StateFlow缓存数据，避免重复请求：

```kotlin
private val _todaySchedule = MutableStateFlow<ScheduleInfo?>(null)
val todaySchedule: StateFlow<ScheduleInfo?> = _todaySchedule.asStateFlow()

init {
    refreshTodaySchedule()
}

private fun refreshTodaySchedule() {
    viewModelScope.launch {
        _todaySchedule.value = scheduleApi.getTodaySchedule()
    }
}
```

### 3. 响应式更新

监听排班数据变化，自动更新UI：

```kotlin
@Composable
fun ScheduleDisplay(scheduleApi: ScheduleApi) {
    val schedules by scheduleApi.getAllSchedules()
        .collectAsState(initial = emptyList())
    
    LazyColumn {
        items(schedules) { schedule ->
            ScheduleItem(schedule = schedule)
        }
    }
}
```

## 集成示例

### 在首页显示今日排班

```kotlin
@Composable
fun HomeScreen(
    scheduleApi: ScheduleApi = hiltViewModel<HomeViewModel>().scheduleApi
) {
    var todaySchedule by remember { mutableStateOf<ScheduleInfo?>(null) }
    
    LaunchedEffect(Unit) {
        todaySchedule = scheduleApi.getTodaySchedule()
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { scheduleApi.navigateToSchedule() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "今日排班",
                style = MaterialTheme.typography.titleMedium
            )
            
            todaySchedule?.let { schedule ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(schedule.shiftColor),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = schedule.shiftName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        schedule.startTime?.let { start ->
                            Text(
                                text = "$start - ${schedule.endTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } ?: Text(
                text = "今日无排班",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
```

## 常见问题

### Q: 如何处理时区问题？
A: 排班模块使用设备本地时区，所有时间都基于LocalDate和LocalTime，不涉及时区转换。

### Q: 如何自定义班次颜色？
A: 班次颜色使用Int类型存储，可以使用Android的Color类进行转换：
```kotlin
val colorInt = Color.parseColor("#FF5722")
val composeColor = Color(shift.color)
```

### Q: 如何实现排班提醒？
A: 排班模块内部已集成WorkManager实现提醒功能，只需在设置中配置提醒时间即可。

### Q: 能否批量创建排班？
A: 可以通过导航到批量排班页面（navigateToSchedulePattern）使用批量创建功能。

## 版本兼容性

- 最低支持版本：Android 8.0 (API 26)
- 推荐版本：Android 10.0+ (API 29+)
- Kotlin版本：1.9.21+

## 更新日志

### v1.0.0 (2025-06-14)
- 初始版本发布
- 提供基础排班API
- 支持导航功能
- 集成响应式数据流

---

**最后更新**：2025-06-14
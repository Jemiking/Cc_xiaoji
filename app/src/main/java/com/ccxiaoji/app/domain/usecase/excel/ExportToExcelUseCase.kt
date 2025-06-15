package com.ccxiaoji.app.domain.usecase.excel

import com.ccxiaoji.app.data.mapper.HabitDataMapper
import com.ccxiaoji.app.data.mapper.LedgerDataMapper
import com.ccxiaoji.app.data.mapper.ScheduleDataMapper
import com.ccxiaoji.app.data.mapper.TodoDataMapper
import com.ccxiaoji.core.common.util.DateConverter
import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.schedule.api.ScheduleApi
import com.ccxiaoji.feature.todo.api.TodoApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinLocalDate
import javax.inject.Inject

// TODO: 编译验证 - 需要执行 ./gradlew :app:compileDebugKotlin
class ExportToExcelUseCase @Inject constructor(
    private val excelConverter: ExcelConverter,
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val scheduleApi: ScheduleApi,
    private val ledgerDataMapper: LedgerDataMapper,
    private val todoDataMapper: TodoDataMapper,
    private val habitDataMapper: HabitDataMapper,
    private val scheduleDataMapper: ScheduleDataMapper
) {
    
    sealed class ExportResult {
        data class Success(
            val data: ByteArray,
            val fileName: String,
            val sizeInBytes: Long,
            val recordCount: Int
        ) : ExportResult() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Success

                if (!data.contentEquals(other.data)) return false
                if (fileName != other.fileName) return false
                if (sizeInBytes != other.sizeInBytes) return false
                if (recordCount != other.recordCount) return false

                return true
            }

            override fun hashCode(): Int {
                var result = data.contentHashCode()
                result = 31 * result + fileName.hashCode()
                result = 31 * result + sizeInBytes.hashCode()
                result = 31 * result + recordCount
                return result
            }
        }
        
        data class Error(val message: String) : ExportResult()
        
        data class Progress(
            val stage: String,
            val progress: Float,
            val message: String
        ) : ExportResult()
    }
    
    data class ExportOptions(
        val includeLedger: Boolean = true,
        val includeTodo: Boolean = true,
        val includeHabit: Boolean = true,
        val includeSchedule: Boolean = true,
        val dateRange: DateRange? = null
    )
    
    data class DateRange(
        val startDate: LocalDate,
        val endDate: LocalDate
    )
    
    suspend operator fun invoke(
        options: ExportOptions,
        onProgress: (ExportResult.Progress) -> Unit = {}
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val mutableTransactions = mutableListOf<TransactionData>()
            val mutableAccounts = mutableListOf<AccountData>()
            val mutableCategories = mutableListOf<CategoryData>()
            val mutableTasks = mutableListOf<TaskData>()
            val mutableHabits = mutableListOf<HabitData>()
            val mutableBudgets = mutableListOf<BudgetData>()
            val mutableSavingsGoals = mutableListOf<SavingsGoalData>()
            val mutableSchedules = mutableListOf<ScheduleData>()
            var totalRecords = 0
            
            coroutineScope {
                // 并行获取所有数据
                
                // 获取记账数据
                val ledgerDeferred = if (options.includeLedger) {
                    async {
                        onProgress(ExportResult.Progress("记账", 0.2f, "正在导出记账数据..."))
                        
                        // 获取交易记录
                        val transactions = if (options.dateRange != null) {
                            val allTransactions = mutableListOf<TransactionData>()
                            var currentDate = options.dateRange.startDate
                            
                            while (currentDate <= options.dateRange.endDate) {
                                val year = currentDate.year
                                val month = currentDate.monthNumber
                                
                                val monthTransactions = ledgerApi.getTransactionsByMonth(year, month)
                                allTransactions.addAll(monthTransactions.map { transaction ->
                                    ledgerDataMapper.mapTransactionToExportData(transaction)
                                }.filter { transactionData ->
                                    val transactionDate = java.time.Instant.ofEpochMilli(transactionData.createdAt)
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate()
                                        .toKotlinLocalDate()
                                    transactionDate >= options.dateRange.startDate && 
                                    transactionDate <= options.dateRange.endDate
                                })
                                
                                // 移动到下个月
                                currentDate = if (month == 12) {
                                    LocalDate(year + 1, 1, 1)
                                } else {
                                    LocalDate(year, month + 1, 1)
                                }
                                
                                if (currentDate > options.dateRange.endDate) break
                            }
                            allTransactions
                        } else {
                            // 获取最近的交易记录
                            ledgerApi.getRecentTransactionsList(1000).map { transaction ->
                                ledgerDataMapper.mapTransactionToExportData(transaction)
                            }
                        }
                        
                        // 获取账户信息
                        val accounts = ledgerApi.getAccounts().map { account ->
                            ledgerDataMapper.mapAccountToExportData(account)
                        }
                        
                        // 获取分类信息
                        val categories = ledgerApi.getAllCategories().map { category ->
                            ledgerDataMapper.mapCategoryToExportData(category)
                        }
                        
                        // 获取预算信息（最近12个月）
                        val now = java.time.LocalDate.now()
                        val budgets = mutableListOf<BudgetData>()
                        for (i in 0..11) {
                            val date = now.minusMonths(i.toLong())
                            val budgetList = ledgerApi.getBudgetsWithSpent(
                                date.year, 
                                date.monthValue
                            ).first() // 获取Flow的第一个值
                            
                            budgets.addAll(budgetList.map { budget ->
                                ledgerDataMapper.mapBudgetToExportData(budget)
                            })
                        }
                        
                        // 获取存钱目标
                        val savingsGoals = ledgerApi.getSavingsGoals().map { goal ->
                            ledgerDataMapper.mapSavingsGoalToExportData(goal)
                        }
                        
                        LedgerExportData(
                            transactions = transactions,
                            accounts = accounts,
                            categories = categories,
                            budgets = budgets,
                            savingsGoals = savingsGoals
                        )
                    }
                } else null
                
                // 获取待办数据
                val todoDeferred = if (options.includeTodo) {
                    async {
                        onProgress(ExportResult.Progress("待办", 0.4f, "正在导出待办数据..."))
                        
                        val tasks = todoApi.getAllTasks().map { task ->
                            todoDataMapper.mapTodoTaskToExportData(task)
                        }
                        
                        TodoExportData(tasks = tasks)
                    }
                } else null
                
                // 获取习惯数据
                val habitDeferred = if (options.includeHabit) {
                    async {
                        onProgress(ExportResult.Progress("习惯", 0.6f, "正在导出习惯数据..."))
                        
                        val habits = habitApi.getAllHabits().map { habit ->
                            habitDataMapper.mapHabitToExportData(habit)
                        }
                        
                        HabitExportData(habits = habits)
                    }
                } else null
                
                // 获取排班数据
                val scheduleDeferred = if (options.includeSchedule) {
                    async {
                        onProgress(ExportResult.Progress("排班", 0.8f, "正在导出排班数据..."))
                        
                        val schedules = mutableListOf<ScheduleData>()
                        
                        if (options.dateRange != null) {
                            // 逐日查询指定日期范围的排班
                            var currentDate = options.dateRange.startDate
                            while (currentDate <= options.dateRange.endDate) {
                                val javaDate = scheduleDataMapper.convertDate(currentDate)
                                scheduleApi.getScheduleByDate(javaDate)?.let { schedule ->
                                    schedules.add(scheduleDataMapper.mapScheduleInfoToExportData(schedule, currentDate))
                                }
                                currentDate = currentDate.plus(DatePeriod(days = 1))
                            }
                        } else {
                            // 获取最近30天的排班
                            val endDate = java.time.LocalDate.now().toKotlinLocalDate()
                            val startDate = endDate.minus(kotlinx.datetime.DatePeriod(days = 30))
                            
                            var currentDate = startDate
                            while (currentDate <= endDate) {
                                val javaDate = scheduleDataMapper.convertDate(currentDate)
                                scheduleApi.getScheduleByDate(javaDate)?.let { schedule ->
                                    schedules.add(scheduleDataMapper.mapScheduleInfoToExportData(schedule, currentDate))
                                }
                                currentDate = currentDate.plus(DatePeriod(days = 1))
                            }
                        }
                        
                        ScheduleExportData(schedules = schedules)
                    }
                } else null
                
                // 等待所有数据获取完成
                ledgerDeferred?.await()?.let { data ->
                    mutableTransactions.addAll(data.transactions)
                    mutableAccounts.addAll(data.accounts)
                    mutableCategories.addAll(data.categories)
                    mutableBudgets.addAll(data.budgets)
                    mutableSavingsGoals.addAll(data.savingsGoals)
                    totalRecords += data.transactions.size + data.accounts.size + 
                                   data.categories.size + data.budgets.size + 
                                   data.savingsGoals.size
                }
                
                todoDeferred?.await()?.let { data ->
                    mutableTasks.addAll(data.tasks)
                    totalRecords += data.tasks.size
                }
                
                habitDeferred?.await()?.let { data ->
                    mutableHabits.addAll(data.habits)
                    totalRecords += data.habits.size
                }
                
                scheduleDeferred?.await()?.let { data ->
                    mutableSchedules.addAll(data.schedules)
                    totalRecords += data.schedules.size
                }
            }
            
            onProgress(ExportResult.Progress("转换", 0.9f, "正在生成Excel文件..."))
            
            // 转换为Excel
            val excelData = ExcelData(
                transactions = mutableTransactions,
                accounts = mutableAccounts,
                categories = mutableCategories,
                tasks = mutableTasks,
                habits = mutableHabits,
                budgets = mutableBudgets,
                savingsGoals = mutableSavingsGoals,
                schedules = mutableSchedules
            )
            val excelBytes = excelConverter.convertToExcel(excelData)
            
            // 生成文件名
            val fileName = generateFileName(options)
            
            onProgress(ExportResult.Progress("完成", 1.0f, "导出完成"))
            
            ExportResult.Success(
                data = excelBytes,
                fileName = fileName,
                sizeInBytes = excelBytes.size.toLong(),
                recordCount = totalRecords
            )
            
        } catch (e: Exception) {
            ExportResult.Error("导出失败: ${e.message}")
        }
    }
    
    private fun generateFileName(options: ExportOptions): String {
        val dateFormat = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val timestamp = java.time.LocalDateTime.now().format(dateFormat)
        
        val modules = mutableListOf<String>()
        if (options.includeLedger) modules.add("记账")
        if (options.includeTodo) modules.add("待办")
        if (options.includeHabit) modules.add("习惯")
        if (options.includeSchedule) modules.add("排班")
        
        val moduleStr = if (modules.size == 4) "全部数据" else modules.joinToString("_")
        
        return "CC小记_${moduleStr}_$timestamp.xlsx"
    }
}

// 内部数据类
private data class LedgerExportData(
    val transactions: List<TransactionData>,
    val accounts: List<AccountData>,
    val categories: List<CategoryData>,
    val budgets: List<BudgetData>,
    val savingsGoals: List<SavingsGoalData>
)

private data class TodoExportData(
    val tasks: List<TaskData>
)

private data class HabitExportData(
    val habits: List<HabitData>
)

private data class ScheduleExportData(
    val schedules: List<ScheduleData>
)


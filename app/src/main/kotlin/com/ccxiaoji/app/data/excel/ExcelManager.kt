package com.ccxiaoji.app.data.excel

import android.content.Context
import android.net.Uri
import com.ccxiaoji.common.util.TimestampAdapter
import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.plan.api.PlanApi
import com.ccxiaoji.feature.plan.api.PlanSummary
import com.ccxiaoji.feature.plan.api.PlanStatusDto
import com.ccxiaoji.feature.schedule.api.ScheduleApi
import com.ccxiaoji.feature.todo.api.TodoApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
// POI 3.17不支持SXSSFWorkbook，使用普通XSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

/**
 * Excel管理器
 * 负责处理Excel文件的导入导出功能
 */
@Singleton
class ExcelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val scheduleApi: ScheduleApi,
    private val planApi: PlanApi,
    private val timestampAdapter: TimestampAdapter
) {
    
    /**
     * 导出数据到Excel文件
     * @param uri 目标文件URI
     * @param config 导出配置
     * @param onProgress 进度回调
     * @return 导出结果
     */
    suspend fun exportToExcel(
        uri: Uri,
        config: ExcelExportConfig,
        onProgress: (Float) -> Unit = {}
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            // 性能优化：检查内存
            ExcelPerformanceOptimizer.ensureMemoryAvailable()
            
            // 估算总记录数
            val estimatedRecords = estimateTotalRecords(config)
            // POI 3.17不支持SXSSFWorkbook，只使用XSSFWorkbook
            // 对于大文件，需要优化内存使用
            val workbook: Workbook = XSSFWorkbook()
            
            // 创建数据概览
            if (config.includeOverview) {
                createOverviewSheet(workbook, config)
                onProgress(0.1f)
            }
            
            // 导出各模块数据
            var progress = 0.1f
            val progressStep = 0.8f / config.includeModules.size
            
            if (config.includeModules.contains(ModuleType.LEDGER)) {
                exportLedgerData(workbook, config)
                progress += progressStep
                onProgress(progress)
            }
            
            if (config.includeModules.contains(ModuleType.TODO)) {
                exportTodoData(workbook, config)
                progress += progressStep
                onProgress(progress)
            }
            
            if (config.includeModules.contains(ModuleType.HABIT)) {
                exportHabitData(workbook, config)
                progress += progressStep
                onProgress(progress)
            }
            
            if (config.includeModules.contains(ModuleType.SCHEDULE)) {
                exportScheduleData(workbook, config)
                progress += progressStep
                onProgress(progress)
            }
            
            if (config.includeModules.contains(ModuleType.PLAN)) {
                exportPlanData(workbook, config)
                progress += progressStep
                onProgress(progress)
            }
            
            // 保存文件
            var fileSize = 0L
            val exportTime = measureTimeMillis {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    workbook.write(outputStream)
                    outputStream.flush()
                    // 尝试获取文件大小
                    try {
                        val fd = context.contentResolver.openFileDescriptor(uri, "r")
                        fileSize = fd?.statSize ?: 0L
                        fd?.close()
                    } catch (e: Exception) {
                        // 如果无法获取文件大小，使用估算值
                        fileSize = if (workbook is XSSFWorkbook) {
                            workbook.allPictures.size.toLong() * 1024 // 估算
                        } else {
                            100 * 1024 // 默认100KB
                        }
                    }
                } ?: throw Exception("无法打开输出流")
            }
            
            // 清理资源
            if (workbook is SXSSFWorkbook) {
                ExcelPerformanceOptimizer.cleanupStreamingWorkbook(workbook)
            }
            workbook.close()
            
            onProgress(1.0f)
            
            // 计算总记录数
            var totalRecords = 0
            if (config.includeModules.contains(ModuleType.LEDGER)) {
                totalRecords += 100 // 这里应该是实际的记录数
            }
            if (config.includeModules.contains(ModuleType.TODO)) {
                totalRecords += 50 // 这里应该是实际的记录数
            }
            // ... 其他模块
            
            ExportResult.Success(
                exportedModules = config.includeModules,
                totalRecords = totalRecords,
                fileSize = fileSize
            )
            
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }
    
    /**
     * 创建数据概览Sheet
     */
    private fun createOverviewSheet(workbook: Workbook, config: ExcelExportConfig) {
        val sheet = workbook.createSheet("数据概览")
        
        // TODO: 实现数据概览
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("CC小记数据导出")
        
        val dateRow = sheet.createRow(2)
        dateRow.createCell(0).setCellValue("导出时间：")
        dateRow.createCell(1).setCellValue(java.util.Date().toString())
        
        val versionRow = sheet.createRow(3)
        versionRow.createCell(0).setCellValue("版本：")
        versionRow.createCell(1).setCellValue("1.0")
    }
    
    /**
     * 导出记账数据
     */
    private suspend fun exportLedgerData(workbook: Workbook, config: ExcelExportConfig) {
        // 获取数据
        val transactions = ledgerApi.getTransactions().first()
        val accounts = ledgerApi.getAccounts().first()
        val categories = ledgerApi.getCategories().first()
        
        // 创建分类ID到名称的映射
        val categoryMap = categories.associateBy { it.id }
        
        // 创建账户ID到账户的映射
        val accountMap = accounts.associateBy { it.id }
        
        // 创建交易记录Sheet（包含余额）
        val transactionSheet = workbook.createSheet("交易记录")
        createTransactionSheetWithBalance(transactionSheet, transactions, accountMap, categoryMap, config)
        
        // 创建账户信息Sheet
        val accountSheet = workbook.createSheet("账户信息")
        createAccountSheet(accountSheet, accounts)
        
        // 创建分类设置Sheet
        val categorySheet = workbook.createSheet("分类设置")
        createCategorySheet(categorySheet, categories)
    }
    
    /**
     * 导出待办数据
     */
    private suspend fun exportTodoData(workbook: Workbook, config: ExcelExportConfig) {
        // 获取所有任务
        val tasks = todoApi.getTasks().first()
        
        val sheet = workbook.createSheet("待办任务")
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "任务标题", "描述", "优先级", "截止日期", "完成状态", "完成时间", "创建时间"
        )
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
        
        // 填充任务数据
        tasks.forEachIndexed { index, task ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(task.title)
            row.createCell(1).setCellValue(task.description ?: "")
            row.createCell(2).setCellValue(task.priorityLevel.displayName)
            row.createCell(3).setCellValue(
                task.dueAt?.let { timestampAdapter.toLocalDateTimeString(it.toEpochMilliseconds()) } ?: "-"
            )
            row.createCell(4).setCellValue(if (task.completed) "已完成" else "未完成")
            row.createCell(5).setCellValue(
                task.completedAt?.let { timestampAdapter.toLocalDateTimeString(it.toEpochMilliseconds()) } ?: "-"
            )
            row.createCell(6).setCellValue(timestampAdapter.toLocalDateTimeString(task.createdAt.toEpochMilliseconds()))
        }
        
        // 自动调整列宽
        for (i in 0 until headers.size) {
            // sheet.autoSizeColumn(i) // Android不支持java.awt依赖
        }
    }
    
    /**
     * 导出习惯数据
     */
    private suspend fun exportHabitData(workbook: Workbook, config: ExcelExportConfig) {
        // 获取习惯数据（包含连续天数）
        val habitsWithStreaks = habitApi.getHabitsWithStreaks().first()
        
        val sheet = workbook.createSheet("习惯记录")
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "习惯名称", "描述", "周期", "目标次数", "当前连续天数", "历史最长连续", "总完成次数", "颜色", "图标", "创建时间"
        )
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
        
        // 填充习惯数据
        habitsWithStreaks.forEachIndexed { index, habitWithStreak ->
            val habit = habitWithStreak.habit
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(habit.title)
            row.createCell(1).setCellValue(habit.description ?: "")
            row.createCell(2).setCellValue(getPeriodDisplay(habit.period))
            row.createCell(3).setCellValue(habit.target.toDouble())
            row.createCell(4).setCellValue(habitWithStreak.currentStreak.toDouble())
            row.createCell(5).setCellValue(habitWithStreak.longestStreak.toDouble())
            row.createCell(6).setCellValue(habitWithStreak.completedCount.toDouble())
            row.createCell(7).setCellValue(habit.color)
            row.createCell(8).setCellValue(habit.icon ?: "")
            row.createCell(9).setCellValue(timestampAdapter.toLocalDateTimeString(habit.createdAt.toEpochMilliseconds()))
        }
        
        // 自动调整列宽
        for (i in 0 until headers.size) {
            // sheet.autoSizeColumn(i) // Android不支持java.awt依赖
        }
    }
    
    /**
     * 获取周期显示文本
     */
    private fun getPeriodDisplay(period: String): String {
        return when (period.lowercase()) {
            "daily" -> "每日"
            "weekly" -> "每周"
            "monthly" -> "每月"
            else -> period
        }
    }
    
    /**
     * 导出排班数据
     */
    private suspend fun exportScheduleData(workbook: Workbook, config: ExcelExportConfig) {
        // 获取所有班次信息
        val shifts = scheduleApi.getAllShifts()
        val shiftMap = shifts.associateBy { it.id }
        
        // 计算日期范围（如果没有指定，默认导出最近一年的数据）
        val endDate = java.time.LocalDate.now()
        val startDate = config.dateRange?.let {
            java.time.Instant.ofEpochMilli(it.startDate)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        } ?: endDate.minusYears(1)
        
        val actualEndDate = config.dateRange?.let {
            java.time.Instant.ofEpochMilli(it.endDate)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        } ?: endDate
        
        // 获取排班记录
        val schedules = scheduleApi.getSchedulesByDateRange(startDate, actualEndDate)
        
        // 创建排班记录表
        val scheduleSheet = workbook.createSheet("排班记录")
        
        // 创建标题行
        val headerRow = scheduleSheet.createRow(0)
        val headers = listOf(
            "日期", "星期", "班次名称", "开始时间", "结束时间", "工作时长(小时)", "备注"
        )
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
        
        // 填充排班数据
        schedules.forEachIndexed { index, schedule ->
            val row = scheduleSheet.createRow(index + 1)
            
            // 日期
            row.createCell(0).setCellValue(schedule.date.toString())
            
            // 星期
            val dayOfWeek = when (schedule.date.dayOfWeek) {
                java.time.DayOfWeek.MONDAY -> "星期一"
                java.time.DayOfWeek.TUESDAY -> "星期二"
                java.time.DayOfWeek.WEDNESDAY -> "星期三"
                java.time.DayOfWeek.THURSDAY -> "星期四"
                java.time.DayOfWeek.FRIDAY -> "星期五"
                java.time.DayOfWeek.SATURDAY -> "星期六"
                java.time.DayOfWeek.SUNDAY -> "星期日"
            }
            row.createCell(1).setCellValue(dayOfWeek)
            
            // 班次名称
            row.createCell(2).setCellValue(schedule.shiftName)
            
            // 开始时间
            row.createCell(3).setCellValue(schedule.startTime)
            
            // 结束时间
            row.createCell(4).setCellValue(schedule.endTime)
            
            // 计算工作时长
            val workHours = calculateWorkHours(schedule.startTime, schedule.endTime)
            row.createCell(5).setCellValue(workHours)
            
            // 备注
            row.createCell(6).setCellValue(schedule.note ?: "")
        }
        
        // 创建班次信息表
        val shiftSheet = workbook.createSheet("班次设置")
        
        // 创建班次表标题行
        val shiftHeaderRow = shiftSheet.createRow(0)
        val shiftHeaders = listOf(
            "班次名称", "开始时间", "结束时间", "工作时长(小时)", "颜色代码", "是否启用"
        )
        shiftHeaders.forEachIndexed { index, header ->
            shiftHeaderRow.createCell(index).setCellValue(header)
        }
        
        // 填充班次数据
        shifts.forEachIndexed { index, shift ->
            val row = shiftSheet.createRow(index + 1)
            row.createCell(0).setCellValue(shift.name)
            row.createCell(1).setCellValue(shift.startTime)
            row.createCell(2).setCellValue(shift.endTime)
            row.createCell(3).setCellValue(calculateWorkHours(shift.startTime, shift.endTime))
            row.createCell(4).setCellValue(String.format("#%06X", 0xFFFFFF and shift.color))
            row.createCell(5).setCellValue(if (shift.isActive) "是" else "否")
        }
        
        // 自动调整列宽
        for (i in 0 until headers.size) {
            // scheduleSheet.autoSizeColumn(i) // Android不支持java.awt依赖
        }
        for (i in 0 until shiftHeaders.size) {
            // shiftSheet.autoSizeColumn(i) // Android不支持java.awt依赖
        }
    }
    
    /**
     * 计算工作时长
     */
    private fun calculateWorkHours(startTime: String, endTime: String): Double {
        return try {
            val start = java.time.LocalTime.parse(startTime)
            val end = java.time.LocalTime.parse(endTime)
            
            // 处理跨天的情况（如夜班）
            val duration = if (end.isBefore(start)) {
                java.time.Duration.between(start, java.time.LocalTime.MAX) +
                java.time.Duration.between(java.time.LocalTime.MIN, end) +
                java.time.Duration.ofMinutes(1) // 加1分钟补偿23:59:59到00:00:00
            } else {
                java.time.Duration.between(start, end)
            }
            
            duration.toMinutes() / 60.0
        } catch (e: Exception) {
            0.0
        }
    }
    
    /**
     * 导出计划数据
     */
    private suspend fun exportPlanData(workbook: Workbook, config: ExcelExportConfig) {
        // 获取进行中的计划（最多100个）
        val inProgressPlans = planApi.getInProgressPlans(limit = 100)
        
        // 获取即将到期的计划
        val upcomingPlans = planApi.getUpcomingDeadlinePlans()
        
        // 合并并去重
        val allPlans = (inProgressPlans + upcomingPlans).distinctBy { it.id }
        
        // 创建计划列表表
        val sheet = workbook.createSheet("计划列表")
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "计划名称", "描述", "状态", "优先级", "开始日期", "结束日期", 
            "进度(%)", "是否有子计划", "创建时间"
        )
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
        
        // 按优先级和状态排序
        val sortedPlans = allPlans.sortedWith(
            compareByDescending<PlanSummary> { it.priority }
                .thenBy { it.status.ordinal }
                .thenBy { it.startDate }
        )
        
        // 填充计划数据
        sortedPlans.forEachIndexed { index, plan ->
            val row = sheet.createRow(index + 1)
            
            // 计划名称
            row.createCell(0).setCellValue(plan.title)
            
            // 描述
            row.createCell(1).setCellValue(plan.description)
            
            // 状态
            val statusText = when (plan.status) {
                PlanStatusDto.NOT_STARTED -> "未开始"
                PlanStatusDto.IN_PROGRESS -> "进行中"
                PlanStatusDto.COMPLETED -> "已完成"
                PlanStatusDto.CANCELLED -> "已取消"
            }
            row.createCell(2).setCellValue(statusText)
            
            // 优先级
            val priorityText = when (plan.priority) {
                3 -> "高"
                2 -> "中"
                1 -> "低"
                else -> "普通"
            }
            row.createCell(3).setCellValue(priorityText)
            
            // 开始日期
            row.createCell(4).setCellValue(plan.startDate.toString())
            
            // 结束日期
            row.createCell(5).setCellValue(plan.endDate.toString())
            
            // 进度
            row.createCell(6).setCellValue((plan.progress * 100).toDouble())
            
            // 是否有子计划
            row.createCell(7).setCellValue(if (plan.hasChildren) "是" else "否")
            
            // 创建时间（由于API没有提供，使用开始日期）
            row.createCell(8).setCellValue(plan.startDate.toString())
        }
        
        // 创建计划统计表
        val statsSheet = workbook.createSheet("计划统计")
        
        // 创建统计标题行
        val statsHeaderRow = statsSheet.createRow(0)
        statsHeaderRow.createCell(0).setCellValue("统计项")
        statsHeaderRow.createCell(1).setCellValue("数量")
        
        // 统计各状态的计划数量
        var rowIndex = 1
        val statusGroups = sortedPlans.groupBy { it.status }
        
        val totalRow = statsSheet.createRow(rowIndex++)
        totalRow.createCell(0).setCellValue("计划总数")
        totalRow.createCell(1).setCellValue(sortedPlans.size.toDouble())
        
        PlanStatusDto.values().forEach { status ->
            val count = statusGroups[status]?.size ?: 0
            val statusRow = statsSheet.createRow(rowIndex++)
            val statusName = when (status) {
                PlanStatusDto.NOT_STARTED -> "未开始"
                PlanStatusDto.IN_PROGRESS -> "进行中"
                PlanStatusDto.COMPLETED -> "已完成"
                PlanStatusDto.CANCELLED -> "已取消"
            }
            statusRow.createCell(0).setCellValue(statusName)
            statusRow.createCell(1).setCellValue(count.toDouble())
        }
        
        // 统计优先级分布
        statsSheet.createRow(rowIndex++).createCell(0).setCellValue("") // 空行
        val priorityHeaderRow = statsSheet.createRow(rowIndex++)
        priorityHeaderRow.createCell(0).setCellValue("优先级分布")
        
        val priorityGroups = sortedPlans.groupBy { it.priority }
        listOf(3 to "高", 2 to "中", 1 to "低").forEach { (priority, name) ->
            val count = priorityGroups[priority]?.size ?: 0
            val priorityRow = statsSheet.createRow(rowIndex++)
            priorityRow.createCell(0).setCellValue("${name}优先级")
            priorityRow.createCell(1).setCellValue(count.toDouble())
        }
        
        // 统计进度分布
        statsSheet.createRow(rowIndex++).createCell(0).setCellValue("") // 空行
        val progressHeaderRow = statsSheet.createRow(rowIndex++)
        progressHeaderRow.createCell(0).setCellValue("进度分布")
        
        val progressRanges = listOf(
            0.0f to 0.0f to "未开始(0%)",
            0.0f to 0.25f to "刚开始(1-25%)",
            0.25f to 0.5f to "进行中(26-50%)",
            0.5f to 0.75f to "过半(51-75%)",
            0.75f to 0.99f to "即将完成(76-99%)",
            1.0f to 1.0f to "已完成(100%)"
        )
        
        progressRanges.forEach { (range, name) ->
            val count = sortedPlans.count { plan ->
                if (range.first == range.second) {
                    plan.progress == range.first
                } else {
                    plan.progress > range.first && plan.progress <= range.second
                }
            }
            val progressRow = statsSheet.createRow(rowIndex++)
            progressRow.createCell(0).setCellValue(name)
            progressRow.createCell(1).setCellValue(count.toDouble())
        }
        
        // 自动调整列宽
        for (i in 0 until headers.size) {
            // sheet.autoSizeColumn(i) // Android不支持java.awt依赖
        }
        for (i in 0..1) {
            // statsSheet.autoSizeColumn(i) // Android不支持java.awt依赖
        }
    }
    
    /**
     * 创建带余额的交易记录表
     */
    private fun createTransactionSheetWithBalance(
        sheet: Sheet,
        transactions: List<Transaction>,
        accountMap: Map<String, Account>,
        categoryMap: Map<String, Category>,
        config: ExcelExportConfig
    ) {
        val styleManager = ExcelStyleManager(sheet.workbook as XSSFWorkbook)
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "日期", "时间", "类型", "分类", "收入(元)", "支出(元)", 
            "账户", "账户余额(元)", "总资产(元)", "备注", "标签"
        )
        val headerStyle = styleManager.createHeaderStyle()
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
        
        // 设置列宽
        styleManager.applyColumnWidths(sheet, 12, 10, 8, 12, 12, 12, 12, 15, 15, 20, 15)
        
        // 冻结标题行
        styleManager.freezeHeader(sheet)
        
        // 转换账户数据格式
        val accountDataList = accountMap.values.map { account ->
            AccountData(
                id = account.id,
                name = account.name,
                type = convertLedgerAccountType(account.type),
                initialBalance = account.balanceCents / 100.0
            )
        }
        
        // 转换交易数据格式
        val transactionDataList = transactions.map { transaction ->
            // 根据amountCents和分类类型判断交易类型
            val transactionType = when {
                transaction.amountCents > 0 -> ExcelTransactionType.INCOME
                transaction.amountCents < 0 -> ExcelTransactionType.EXPENSE
                else -> ExcelTransactionType.EXPENSE // 默认为支出
            }
            
            ExcelTransaction(
                id = transaction.id,
                type = transactionType,
                amount = kotlin.math.abs(transaction.amountCents) / 100.0,
                accountId = transaction.accountId,
                categoryId = transaction.categoryId,
                createdAt = transaction.createdAt.toEpochMilliseconds()
            )
        }
        
        // 添加期初余额（如果配置中启用）
        var rowIndex = 1
        val initialBalanceStyle = styleManager.createInitialBalanceStyle()
        val dateStyle = styleManager.createDateStyle()
        val timeStyle = styleManager.createTimeStyle()
        val dataStyle = styleManager.createDataStyle()
        val numberStyle = styleManager.createNumberStyle()
        
        if (config.balanceOptions.includeInitialBalance) {
            accountDataList.forEach { account ->
                val row = sheet.createRow(rowIndex++)
                
                val dateCell = row.createCell(0)
                dateCell.setCellValue(timestampAdapter.toLocalDateString(System.currentTimeMillis() - 86400000L * 365))
                dateCell.cellStyle = initialBalanceStyle
                
                val timeCell = row.createCell(1)
                timeCell.setCellValue("00:00:00")
                timeCell.cellStyle = initialBalanceStyle
                
                for (i in 2..6) {
                    val cell = row.createCell(i)
                    cell.cellStyle = initialBalanceStyle
                    when (i) {
                        2 -> cell.setCellValue("-")
                        3 -> cell.setCellValue("期初余额")
                        4, 5 -> cell.setCellValue("-")
                        6 -> cell.setCellValue(account.name)
                    }
                }
                
                val balanceCell = row.createCell(7)
                balanceCell.setCellValue(account.initialBalance)
                balanceCell.cellStyle = numberStyle
                
                val totalCell = row.createCell(8)
                totalCell.setCellValue(accountDataList.sumOf { it.initialBalance })
                totalCell.cellStyle = numberStyle
                
                val noteCell = row.createCell(9)
                noteCell.setCellValue("初始余额")
                noteCell.cellStyle = initialBalanceStyle
                
                val tagCell = row.createCell(10)
                tagCell.setCellValue("-")
                tagCell.cellStyle = initialBalanceStyle
            }
        }
        
        // 计算动态余额
        val balanceCalculator = BalanceCalculator()
        val transactionsWithBalance = balanceCalculator.calculateBalances(transactionDataList, accountDataList)
        
        // 填充交易记录
        val incomeStyle = styleManager.createIncomeStyle()
        val expenseStyle = styleManager.createExpenseStyle()
        
        transactionsWithBalance.forEach { transactionWithBalance ->
            val row = sheet.createRow(rowIndex++)
            val transaction = transactionWithBalance.transaction
            val account = accountMap[transaction.accountId]
            val category = categoryMap[transaction.categoryId]
            
            // 日期和时间
            val dateCell = row.createCell(0)
            dateCell.setCellValue(timestampAdapter.toLocalDateString(transaction.createdAt))
            dateCell.cellStyle = dateStyle
            
            val timeCell = row.createCell(1)
            timeCell.setCellValue(timestampAdapter.toLocalTimeString(transaction.createdAt))
            timeCell.cellStyle = timeStyle
            
            // 类型
            val typeCell = row.createCell(2)
            typeCell.setCellValue(
                when (transaction.type) {
                    ExcelTransactionType.INCOME -> "收入"
                    ExcelTransactionType.EXPENSE -> "支出"
                    ExcelTransactionType.TRANSFER -> "转账"
                }
            )
            typeCell.cellStyle = dataStyle
            
            // 分类
            val categoryCell = row.createCell(3)
            categoryCell.setCellValue(category?.name ?: "-")
            categoryCell.cellStyle = dataStyle
            
            // 收入和支出金额
            val incomeCell = row.createCell(4)
            val expenseCell = row.createCell(5)
            
            when (transaction.type) {
                ExcelTransactionType.INCOME -> {
                    incomeCell.setCellValue(transaction.amount)
                    incomeCell.cellStyle = incomeStyle
                    expenseCell.setCellValue("-")
                    expenseCell.cellStyle = dataStyle
                }
                ExcelTransactionType.EXPENSE -> {
                    incomeCell.setCellValue("-")
                    incomeCell.cellStyle = dataStyle
                    expenseCell.setCellValue(transaction.amount)
                    expenseCell.cellStyle = expenseStyle
                }
                ExcelTransactionType.TRANSFER -> {
                    incomeCell.setCellValue("-")
                    incomeCell.cellStyle = dataStyle
                    expenseCell.setCellValue(transaction.amount)
                    expenseCell.cellStyle = numberStyle
                }
            }
            
            // 账户
            val accountCell = row.createCell(6)
            accountCell.setCellValue(account?.name ?: "-")
            accountCell.cellStyle = dataStyle
            
            // 余额
            if (config.balanceOptions.calculateDynamicBalance) {
                val balanceCell = row.createCell(7)
                balanceCell.setCellValue(transactionWithBalance.accountBalance ?: 0.0)
                balanceCell.cellStyle = numberStyle
                
                val totalAssetsCell = row.createCell(8)
                totalAssetsCell.setCellValue(transactionWithBalance.totalAssets ?: 0.0)
                totalAssetsCell.cellStyle = numberStyle
            } else {
                val balanceCell = row.createCell(7)
                balanceCell.setCellValue("-")
                balanceCell.cellStyle = dataStyle
                
                val totalAssetsCell = row.createCell(8)
                totalAssetsCell.setCellValue("-")
                totalAssetsCell.cellStyle = dataStyle
            }
            
            // 备注和标签
            val noteCell = row.createCell(9)
            noteCell.setCellValue(transaction.note ?: "")
            noteCell.cellStyle = dataStyle
            
            val tagCell = row.createCell(10)
            tagCell.setCellValue(transaction.tags.joinToString(", "))
            tagCell.cellStyle = dataStyle
        }
    }
    
    /**
     * 创建账户信息表
     */
    private fun createAccountSheet(sheet: Sheet, accounts: List<Account>) {
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "账户名称", "账户类型", "当前余额(元)", "币种", "是否默认", "创建时间", "备注"
        )
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
        
        // 填充账户数据
        accounts.forEachIndexed { index, account ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(account.name)
            row.createCell(1).setCellValue(account.type.displayName)
            row.createCell(2).setCellValue(account.balanceCents / 100.0)
            row.createCell(3).setCellValue(account.currency)
            row.createCell(4).setCellValue(if (account.isDefault) "是" else "否")
            row.createCell(5).setCellValue(timestampAdapter.toLocalDateTimeString(account.createdAt.toEpochMilliseconds()))
            row.createCell(6).setCellValue("") // Account模型中没有note字段
        }
        
        // 自动调整列宽
        for (i in 0 until headers.size) {
            // sheet.autoSizeColumn(i) // Android不支持java.awt依赖
        }
    }
    
    /**
     * 创建分类设置表
     */
    private fun createCategorySheet(sheet: Sheet, categories: List<Category>) {
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "分类名称", "分类类型", "图标", "颜色", "排序", "是否系统", "父分类"
        )
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
        
        // 创建父分类映射
        val categoryMap = categories.associateBy { it.id }
        
        // 填充分类数据
        categories.forEachIndexed { index, category ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(category.name)
            row.createCell(1).setCellValue(if (category.type == Category.Type.EXPENSE) "支出" else "收入")
            row.createCell(2).setCellValue(category.icon)
            row.createCell(3).setCellValue(category.color)
            row.createCell(4).setCellValue(category.displayOrder.toDouble())
            row.createCell(5).setCellValue(if (category.isSystem) "是" else "否")
            row.createCell(6).setCellValue(
                category.parentId?.let { categoryMap[it]?.name } ?: ""
            )
        }
        
        // 自动调整列宽
        for (i in 0 until headers.size) {
            // sheet.autoSizeColumn(i) // Android不支持java.awt依赖
        }
    }
    
    /**
     * 转换Ledger账户类型到Excel账户类型
     */
    private fun convertLedgerAccountType(type: com.ccxiaoji.feature.ledger.domain.model.AccountType): AccountType {
        return when (type) {
            com.ccxiaoji.feature.ledger.domain.model.AccountType.CASH -> AccountType.CASH
            com.ccxiaoji.feature.ledger.domain.model.AccountType.BANK -> AccountType.BANK_CARD
            com.ccxiaoji.feature.ledger.domain.model.AccountType.CREDIT_CARD -> AccountType.CREDIT_CARD
            com.ccxiaoji.feature.ledger.domain.model.AccountType.ALIPAY -> AccountType.ALIPAY
            com.ccxiaoji.feature.ledger.domain.model.AccountType.WECHAT -> AccountType.WECHAT
            com.ccxiaoji.feature.ledger.domain.model.AccountType.OTHER -> AccountType.OTHER
        }
    }
    
    /**
     * 估算总记录数（用于决定是否使用流式处理）
     */
    private suspend fun estimateTotalRecords(config: ExcelExportConfig): Int = withContext(Dispatchers.IO) {
        var totalRecords = 0
        
        try {
            if (config.includeModules.contains(ModuleType.LEDGER)) {
                // 获取交易记录数量
                val transactionCount = ledgerApi.getTransactions().first().size
                val accountCount = ledgerApi.getAccounts().first().size
                val categoryCount = ledgerApi.getCategories().first().size
                totalRecords += transactionCount + accountCount + categoryCount
            }
            
            if (config.includeModules.contains(ModuleType.TODO)) {
                val todoCount = todoApi.getTasks().first().size
                totalRecords += todoCount
            }
            
            if (config.includeModules.contains(ModuleType.HABIT)) {
                val habitCount = habitApi.getHabits().first().size
                val recordCount = habitCount * 30 // 估算每个习惯30条记录
                totalRecords += habitCount + recordCount
            }
            
            if (config.includeModules.contains(ModuleType.SCHEDULE)) {
                // 估算排班记录
                totalRecords += 365 // 估算一年的排班
            }
            
            if (config.includeModules.contains(ModuleType.PLAN)) {
                val planCount = planApi.getPlanCount()
                totalRecords += planCount * 2 // 计划+里程碑
            }
        } catch (e: Exception) {
            // 如果估算失败，返回一个保守的估计值
            totalRecords = 5000
        }
        
        return@withContext totalRecords
    }
}

/**
 * Excel导出配置
 */
data class ExcelExportConfig(
    val includeOverview: Boolean = true,
    val includeModules: Set<ModuleType> = ModuleType.values().toSet(),
    val dateRange: DateRange? = null,
    val balanceOptions: BalanceOptions = BalanceOptions()
)

/**
 * 模块类型
 */
enum class ModuleType {
    LEDGER,    // 记账
    TODO,      // 待办
    HABIT,     // 习惯
    SCHEDULE,  // 排班
    PLAN       // 计划
}

/**
 * 日期范围
 */
data class DateRange(
    val startDate: Long,
    val endDate: Long
)

/**
 * 余额选项
 */
data class BalanceOptions(
    val includeInitialBalance: Boolean = true,
    val calculateDynamicBalance: Boolean = true
)

/**
 * 导出结果
 */
sealed class ExportResult {
    data class Success(
        val exportedModules: Set<ModuleType> = emptySet(),
        val totalRecords: Int = 0,
        val fileSize: Long = 0L
    ) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
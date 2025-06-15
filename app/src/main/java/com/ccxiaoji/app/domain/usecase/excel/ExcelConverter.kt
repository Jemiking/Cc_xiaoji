package com.ccxiaoji.app.domain.usecase.excel

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

// TODO: 编译验证 - 需要执行 ./gradlew :app:compileDebugKotlin
@Singleton
class ExcelConverter @Inject constructor() {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    /**
     * 将数据转换为Excel文件
     */
    fun convertToExcel(data: ExcelData): ByteArray {
        val workbook = XSSFWorkbook()
        
        // 创建样式
        val headerStyle = createHeaderStyle(workbook)
        val dateStyle = createDateStyle(workbook)
        val moneyStyle = createMoneyStyle(workbook)
        
        // 导出交易记录
        if (data.transactions.isNotEmpty()) {
            createTransactionSheet(workbook, data.transactions, headerStyle, dateStyle, moneyStyle)
        }
        
        // 导出账户信息
        if (data.accounts.isNotEmpty()) {
            createAccountSheet(workbook, data.accounts, headerStyle, moneyStyle)
        }
        
        // 导出分类信息
        if (data.categories.isNotEmpty()) {
            createCategorySheet(workbook, data.categories, headerStyle)
        }
        
        // 导出待办任务
        if (data.tasks.isNotEmpty()) {
            createTaskSheet(workbook, data.tasks, headerStyle, dateStyle)
        }
        
        // 导出习惯记录
        if (data.habits.isNotEmpty()) {
            createHabitSheet(workbook, data.habits, headerStyle)
        }
        
        // 导出预算信息
        if (data.budgets.isNotEmpty()) {
            createBudgetSheet(workbook, data.budgets, headerStyle, moneyStyle)
        }
        
        // 导出存钱目标
        if (data.savingsGoals.isNotEmpty()) {
            createSavingsGoalSheet(workbook, data.savingsGoals, headerStyle, moneyStyle, dateStyle)
        }
        
        // 导出排班信息
        if (data.schedules.isNotEmpty()) {
            createScheduleSheet(workbook, data.schedules, headerStyle, dateStyle)
        }
        
        // 写入字节数组
        val outputStream = ByteArrayOutputStream()
        workbook.use {
            it.write(outputStream)
        }
        
        return outputStream.toByteArray()
    }
    
    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            
            val font = workbook.createFont().apply {
                bold = true
            }
            setFont(font)
        }
    }
    
    private fun createDateStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            dataFormat = workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss")
        }
    }
    
    private fun createMoneyStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            dataFormat = workbook.createDataFormat().getFormat("#,##0.00")
        }
    }
    
    private fun createTransactionSheet(
        workbook: Workbook,
        transactions: List<TransactionData>,
        headerStyle: CellStyle,
        dateStyle: CellStyle,
        moneyStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("交易记录")
        
        // 设置列宽
        sheet.setColumnWidth(0, 5000)  // 日期
        sheet.setColumnWidth(1, 3000)  // 类型
        sheet.setColumnWidth(2, 4000)  // 分类
        sheet.setColumnWidth(3, 3000)  // 金额
        sheet.setColumnWidth(4, 4000)  // 账户
        sheet.setColumnWidth(5, 8000)  // 备注
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf("交易时间", "类型", "分类", "金额", "账户", "备注")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // 填充数据
        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            
            row.createCell(0).apply {
                setCellValue(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(transaction.createdAt), 
                    ZoneId.systemDefault()
                ))
                cellStyle = dateStyle
            }
            
            row.createCell(1).setCellValue(transaction.type)
            row.createCell(2).setCellValue(transaction.categoryName)
            
            row.createCell(3).apply {
                setCellValue(transaction.amount)
                cellStyle = moneyStyle
            }
            
            row.createCell(4).setCellValue(transaction.accountName ?: "默认账户")
            row.createCell(5).setCellValue(transaction.note ?: "")
        }
        
        // 添加自动筛选
        sheet.setAutoFilter(org.apache.poi.ss.util.CellRangeAddress(0, transactions.size, 0, 5))
    }
    
    private fun createAccountSheet(
        workbook: Workbook,
        accounts: List<AccountData>,
        headerStyle: CellStyle,
        moneyStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("账户列表")
        
        // 设置列宽
        sheet.setColumnWidth(0, 4000)  // 账户名称
        sheet.setColumnWidth(1, 3000)  // 类型
        sheet.setColumnWidth(2, 4000)  // 余额
        sheet.setColumnWidth(3, 3000)  // 货币
        sheet.setColumnWidth(4, 5000)  // 创建时间
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf("账户名称", "类型", "余额", "货币", "创建时间")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // 填充数据
        accounts.forEachIndexed { index, account ->
            val row = sheet.createRow(index + 1)
            
            row.createCell(0).setCellValue(account.name)
            row.createCell(1).setCellValue(account.type)
            row.createCell(2).apply {
                setCellValue(account.balance)
                cellStyle = moneyStyle
            }
            row.createCell(3).setCellValue(account.currency)
            row.createCell(4).setCellValue(
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(account.createdAt),
                    ZoneId.systemDefault()
                ).format(dateFormatter)
            )
        }
    }
    
    private fun createCategorySheet(
        workbook: Workbook,
        categories: List<CategoryData>,
        headerStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("分类列表")
        
        // 设置列宽
        sheet.setColumnWidth(0, 4000)  // 分类名称
        sheet.setColumnWidth(1, 3000)  // 类型
        sheet.setColumnWidth(2, 4000)  // 父分类
        sheet.setColumnWidth(3, 3000)  // 图标
        sheet.setColumnWidth(4, 3000)  // 颜色
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf("分类名称", "类型", "父分类", "图标", "颜色")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // 填充数据
        categories.forEachIndexed { index, category ->
            val row = sheet.createRow(index + 1)
            
            row.createCell(0).setCellValue(category.name)
            row.createCell(1).setCellValue(category.type)
            row.createCell(2).setCellValue(category.parentName ?: "")
            row.createCell(3).setCellValue(category.icon)
            row.createCell(4).setCellValue(category.color)
        }
    }
    
    private fun createTaskSheet(
        workbook: Workbook,
        tasks: List<TaskData>,
        headerStyle: CellStyle,
        dateStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("待办任务")
        
        // 设置列宽
        sheet.setColumnWidth(0, 6000)  // 任务标题
        sheet.setColumnWidth(1, 8000)  // 描述
        sheet.setColumnWidth(2, 3000)  // 优先级
        sheet.setColumnWidth(3, 3000)  // 状态
        sheet.setColumnWidth(4, 5000)  // 截止日期
        sheet.setColumnWidth(5, 5000)  // 创建时间
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf("任务标题", "描述", "优先级", "状态", "截止日期", "创建时间")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // 填充数据
        tasks.forEachIndexed { index, task ->
            val row = sheet.createRow(index + 1)
            
            row.createCell(0).setCellValue(task.title)
            row.createCell(1).setCellValue(task.description ?: "")
            row.createCell(2).setCellValue(getPriorityText(task.priority))
            row.createCell(3).setCellValue(if (task.completed) "已完成" else "未完成")
            
            row.createCell(4).apply {
                task.dueAt?.let { due ->
                    setCellValue(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(due),
                        ZoneId.systemDefault()
                    ))
                    cellStyle = dateStyle
                }
            }
            
            row.createCell(5).apply {
                setCellValue(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(task.createdAt),
                    ZoneId.systemDefault()
                ))
                cellStyle = dateStyle
            }
        }
    }
    
    private fun createHabitSheet(
        workbook: Workbook,
        habits: List<HabitData>,
        headerStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("习惯列表")
        
        // 设置列宽
        sheet.setColumnWidth(0, 5000)  // 习惯名称
        sheet.setColumnWidth(1, 8000)  // 描述
        sheet.setColumnWidth(2, 3000)  // 周期
        sheet.setColumnWidth(3, 3000)  // 目标
        sheet.setColumnWidth(4, 3000)  // 颜色
        sheet.setColumnWidth(5, 4000)  // 当前连续天数
        sheet.setColumnWidth(6, 4000)  // 最长连续天数
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf("习惯名称", "描述", "周期", "目标", "颜色", "当前连续", "最长连续")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // 填充数据
        habits.forEachIndexed { index, habit ->
            val row = sheet.createRow(index + 1)
            
            row.createCell(0).setCellValue(habit.title)
            row.createCell(1).setCellValue(habit.description ?: "")
            row.createCell(2).setCellValue(getPeriodText(habit.period))
            row.createCell(3).setCellValue(habit.target.toDouble())
            row.createCell(4).setCellValue(habit.color)
            row.createCell(5).setCellValue(habit.currentStreak.toDouble())
            row.createCell(6).setCellValue(habit.longestStreak.toDouble())
        }
    }
    
    private fun createBudgetSheet(
        workbook: Workbook,
        budgets: List<BudgetData>,
        headerStyle: CellStyle,
        moneyStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("预算管理")
        
        // 设置列宽
        sheet.setColumnWidth(0, 3000)  // 年份
        sheet.setColumnWidth(1, 3000)  // 月份
        sheet.setColumnWidth(2, 4000)  // 分类
        sheet.setColumnWidth(3, 4000)  // 预算金额
        sheet.setColumnWidth(4, 4000)  // 已花费
        sheet.setColumnWidth(5, 3000)  // 使用率
        sheet.setColumnWidth(6, 8000)  // 备注
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf("年份", "月份", "分类", "预算金额", "已花费", "使用率", "备注")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // 填充数据
        budgets.forEachIndexed { index, budget ->
            val row = sheet.createRow(index + 1)
            
            row.createCell(0).setCellValue(budget.year.toDouble())
            row.createCell(1).setCellValue(budget.month.toDouble())
            row.createCell(2).setCellValue(budget.categoryName ?: "总预算")
            
            row.createCell(3).apply {
                setCellValue(budget.budgetAmount)
                cellStyle = moneyStyle
            }
            
            row.createCell(4).apply {
                setCellValue(budget.spentAmount)
                cellStyle = moneyStyle
            }
            
            val usageRate = if (budget.budgetAmount > 0) {
                (budget.spentAmount / budget.budgetAmount * 100)
            } else 0.0
            row.createCell(5).setCellValue(String.format("%.1f%%", usageRate))
            
            row.createCell(6).setCellValue(budget.note ?: "")
        }
    }
    
    private fun createSavingsGoalSheet(
        workbook: Workbook,
        savingsGoals: List<SavingsGoalData>,
        headerStyle: CellStyle,
        moneyStyle: CellStyle,
        dateStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("存钱目标")
        
        // 设置列宽
        sheet.setColumnWidth(0, 5000)  // 目标名称
        sheet.setColumnWidth(1, 8000)  // 描述
        sheet.setColumnWidth(2, 4000)  // 目标金额
        sheet.setColumnWidth(3, 4000)  // 当前金额
        sheet.setColumnWidth(4, 3000)  // 进度
        sheet.setColumnWidth(5, 5000)  // 目标日期
        sheet.setColumnWidth(6, 3000)  // 状态
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf("目标名称", "描述", "目标金额", "当前金额", "进度", "目标日期", "状态")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // 填充数据
        savingsGoals.forEachIndexed { index, goal ->
            val row = sheet.createRow(index + 1)
            
            row.createCell(0).setCellValue(goal.name)
            row.createCell(1).setCellValue(goal.description ?: "")
            
            row.createCell(2).apply {
                setCellValue(goal.targetAmount)
                cellStyle = moneyStyle
            }
            
            row.createCell(3).apply {
                setCellValue(goal.currentAmount)
                cellStyle = moneyStyle
            }
            
            val progress = if (goal.targetAmount > 0) {
                (goal.currentAmount / goal.targetAmount * 100)
            } else 0.0
            row.createCell(4).setCellValue(String.format("%.1f%%", progress))
            
            row.createCell(5).apply {
                goal.targetDate?.let { date ->
                    setCellValue(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(date),
                        ZoneId.systemDefault()
                    ))
                    cellStyle = dateStyle
                }
            }
            
            row.createCell(6).setCellValue(if (goal.isActive) "进行中" else "已完成")
        }
    }
    
    private fun createScheduleSheet(
        workbook: Workbook,
        schedules: List<ScheduleData>,
        headerStyle: CellStyle,
        dateStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("排班记录")
        
        // 设置列宽
        sheet.setColumnWidth(0, 5000)  // 日期
        sheet.setColumnWidth(1, 4000)  // 班次名称
        sheet.setColumnWidth(2, 5000)  // 开始时间
        sheet.setColumnWidth(3, 5000)  // 结束时间
        sheet.setColumnWidth(4, 3000)  // 时长
        sheet.setColumnWidth(5, 3000)  // 颜色
        sheet.setColumnWidth(6, 8000)  // 备注
        
        // 创建标题行
        val headerRow = sheet.createRow(0)
        val headers = listOf("日期", "班次", "开始时间", "结束时间", "时长(小时)", "颜色", "备注")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // 填充数据
        schedules.forEachIndexed { index, schedule ->
            val row = sheet.createRow(index + 1)
            
            row.createCell(0).apply {
                setCellValue(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(schedule.date),
                    ZoneId.systemDefault()
                ).toLocalDate().toString())
            }
            
            row.createCell(1).setCellValue(schedule.shiftName)
            row.createCell(2).setCellValue(schedule.startTime)
            row.createCell(3).setCellValue(schedule.endTime)
            row.createCell(4).setCellValue(schedule.duration)
            row.createCell(5).setCellValue(schedule.color)
            row.createCell(6).setCellValue(schedule.note ?: "")
        }
    }
    
    private fun getPriorityText(priority: Int): String {
        return when (priority) {
            3 -> "高"
            2 -> "中"
            1 -> "低"
            else -> "普通"
        }
    }
    
    private fun getPeriodText(period: String): String {
        return when (period) {
            "daily" -> "每日"
            "weekly" -> "每周"
            "monthly" -> "每月"
            else -> period
        }
    }
}

// 数据类定义
data class ExcelData(
    val transactions: List<TransactionData> = emptyList(),
    val accounts: List<AccountData> = emptyList(),
    val categories: List<CategoryData> = emptyList(),
    val tasks: List<TaskData> = emptyList(),
    val habits: List<HabitData> = emptyList(),
    val budgets: List<BudgetData> = emptyList(),
    val savingsGoals: List<SavingsGoalData> = emptyList(),
    val schedules: List<ScheduleData> = emptyList()
)

data class TransactionData(
    val createdAt: Long,
    val type: String,
    val categoryName: String,
    val amount: Double,
    val accountName: String?,
    val note: String?
)

data class AccountData(
    val name: String,
    val type: String,
    val balance: Double,
    val currency: String,
    val createdAt: Long
)

data class CategoryData(
    val name: String,
    val type: String,
    val parentName: String?,
    val icon: String,
    val color: String
)

data class TaskData(
    val title: String,
    val description: String?,
    val priority: Int,
    val completed: Boolean,
    val dueAt: Long?,
    val createdAt: Long
)

data class HabitData(
    val title: String,
    val description: String?,
    val period: String,
    val target: Int,
    val color: String,
    val currentStreak: Int,
    val longestStreak: Int
)

data class BudgetData(
    val year: Int,
    val month: Int,
    val categoryName: String?,
    val budgetAmount: Double,
    val spentAmount: Double,
    val note: String?
)

data class SavingsGoalData(
    val name: String,
    val description: String?,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long?,
    val isActive: Boolean
)

data class ScheduleData(
    val date: Long,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val duration: Double,
    val color: String,
    val note: String?
)
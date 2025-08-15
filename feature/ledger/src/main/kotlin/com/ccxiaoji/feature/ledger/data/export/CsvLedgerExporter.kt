package com.ccxiaoji.feature.ledger.data.export

import android.content.Context
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.RecurringTransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.SavingsGoalDao
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardBillDao
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardPaymentDao
import com.ccxiaoji.feature.ledger.domain.export.ExportConfig
import com.ccxiaoji.feature.ledger.domain.export.ExportFormat
import com.ccxiaoji.feature.ledger.domain.export.LedgerExporter
import com.ccxiaoji.shared.user.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import android.util.Log

/**
 * CSV格式的记账数据导出器
 * 增强版单文件格式 v2.1 - 支持格式说明和清晰字段名
 */
class CsvLedgerExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val creditCardBillDao: CreditCardBillDao,
    private val creditCardPaymentDao: CreditCardPaymentDao,
    private val userRepository: UserRepository
) : LedgerExporter {
    
    companion object {
        private const val TAG = "CsvLedgerExporter"
    }
    
    /**
     * 每种数据类型的字段说明映射
     */
    private val fieldDescriptions = mapOf(
        "HEADER" to listOf("导出时间", "版本", "货币", "用户ID", "交易数", "账户数", "分类数", "", "描述"),
        "ACCOUNT" to listOf("创建日期", "账户名称", "账户类型", "余额", "信用额度", "账单日", "还款日", "默认账户", "图标"),
        "CATEGORY" to listOf("创建日期", "分类名称", "分类类型", "图标", "颜色", "父分类", "显示顺序", "", ""),
        "TRANSACTION" to listOf("交易时间", "账户", "分类", "金额", "备注", "定期生成", "", "", ""),
        "BUDGET" to listOf("年月", "分类", "预算额", "警告阈值", "已使用", "剩余", "", "", "备注"),
        "RECURRING" to listOf("频率", "执行日", "账户", "分类", "金额", "名称", "开始日期", "结束日期", "备注"),
        "SAVINGS" to listOf("目标名称", "目标金额", "当前金额", "目标日期", "完成进度", "颜色", "", "", "描述"),
        "CREDITBILL" to listOf("账户", "账单开始", "账单结束", "账单总额", "已还金额", "最低还款", "还款截止", "是否逾期", "")
    )
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val exportDir: File by lazy {
        File(context.getExternalFilesDir(null), "exports").apply {
            if (!exists()) mkdirs()
        }
    }
    
    override suspend fun exportTransactions(
        startDate: Long?,
        endDate: Long?,
        accountIds: List<String>?,
        categoryIds: List<String>?
    ): File = withContext(Dispatchers.IO) {
        // 使用统一的导出格式，只导出交易记录
        val config = ExportConfig(
            includeTransactions = true,
            includeAccounts = false,
            includeCategories = false,
            includeBudgets = false,
            includeRecurringTransactions = false,
            includeSavingsGoals = false,
            startDate = startDate,
            endDate = endDate,
            format = ExportFormat.CSV
        )
        exportAll(config)
    }
    
    override suspend fun exportAccounts(): File = withContext(Dispatchers.IO) {
        // 使用统一的导出格式，只导出账户信息
        val config = ExportConfig(
            includeTransactions = false,
            includeAccounts = true,
            includeCategories = false,
            includeBudgets = false,
            includeRecurringTransactions = false,
            includeSavingsGoals = false,
            startDate = null,
            endDate = null,
            format = ExportFormat.CSV
        )
        exportAll(config)
    }
    
    override suspend fun exportCategories(): File = withContext(Dispatchers.IO) {
        // 使用统一的导出格式，只导出分类信息
        val config = ExportConfig(
            includeTransactions = false,
            includeAccounts = false,
            includeCategories = true,
            includeBudgets = false,
            includeRecurringTransactions = false,
            includeSavingsGoals = false,
            startDate = null,
            endDate = null,
            format = ExportFormat.CSV
        )
        exportAll(config)
    }
    
    override suspend fun exportBudgets(year: Int?, month: Int?): File = withContext(Dispatchers.IO) {
        // 使用统一的导出格式，只导出预算信息
        val config = ExportConfig(
            includeTransactions = false,
            includeAccounts = false,
            includeCategories = false,
            includeBudgets = true,
            includeRecurringTransactions = false,
            includeSavingsGoals = false,
            startDate = null,
            endDate = null,
            format = ExportFormat.CSV
        )
        exportAll(config)
    }
    
    override suspend fun exportAll(config: ExportConfig): File = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val fileName = "ledger_export_${dateFormat.format(Date(timestamp)).replace(" ", "_").replace(":", "")}.csv"
        val file = File(exportDir, fileName)
        
        // 获取当前用户
        val currentUser = userRepository.getCurrentUser()
        if (currentUser == null) {
            throw IllegalStateException("当前用户不存在")
        }
        Log.d(TAG, "开始导出所有数据，用户ID: ${currentUser.id}")
        
        FileWriter(file).use { writer ->
            // 添加文件格式说明
            writer.appendLine("# CC小记记账数据导出文件")
            writer.appendLine("# 格式版本: 2.1")
            writer.appendLine("# 说明: 每行第一列为数据类型标识，后续列为对应数据")
            writer.appendLine("# ===============================================")
            writer.appendLine("")
            
            // 添加字段说明部分
            writer.appendLine("# 字段说明")
            fieldDescriptions.forEach { (type, fields) ->
                writer.appendLine("# $type: ${fields.filter { it.isNotEmpty() }.joinToString(", ")}")
            }
            writer.appendLine("# ===============================================")
            writer.appendLine("")
            
            // 写入CSV标题行（使用更清晰的表头）
            writer.appendLine("类型,数据1,数据2,数据3,数据4,数据5,数据6,数据7,数据8,数据9")
            
            // 1. 写入文件头/元数据
            writeHeader(writer, currentUser.id, timestamp)
            
            // 2. 导出账户信息
            if (config.includeAccounts) {
                writeAccounts(writer, currentUser.id)
            }
            
            // 3. 导出分类信息
            if (config.includeCategories) {
                writeCategories(writer, currentUser.id)
            }
            
            // 4. 导出交易记录
            if (config.includeTransactions) {
                writeTransactions(writer, currentUser.id, config.startDate, config.endDate)
            }
            
            // 5. 导出预算信息
            if (config.includeBudgets) {
                writeBudgets(writer, currentUser.id)
            }
            
            // 6. 导出定期交易
            if (config.includeRecurringTransactions) {
                writeRecurringTransactions(writer, currentUser.id)
            }
            
            // 7. 导出储蓄目标
            if (config.includeSavingsGoals) {
                writeSavingsGoals(writer, currentUser.id)
            }
            
            // 8. 导出信用卡账单
            if (config.includeAccounts) { // 如果导出账户，也导出相关的信用卡账单
                writeCreditCardBills(writer, currentUser.id)
            }
        }
        
        Log.d(TAG, "导出完成: $fileName")
        file
    }
    
    /**
     * 写入文件头/元数据
     */
    private suspend fun writeHeader(writer: FileWriter, userId: String, timestamp: Long) {
        val version = "2.1"
        val currency = "CNY"
        val date = dateFormat.format(Date(timestamp))
        
        // 统计记录数
        val transactionCount = transactionDao.getTransactionsByUser(userId).first().size
        val accountCount = accountDao.getAccountsByUser(userId).first().size
        val categoryCount = categoryDao.getCategoriesByUser(userId).first().size
        
        writer.appendLine("HEADER,${csvEscape(date)},$version,$currency,$userId,$transactionCount,$accountCount,$categoryCount,,CC小记数据导出")
    }
    
    /**
     * 写入账户信息
     */
    private suspend fun writeAccounts(writer: FileWriter, userId: String) {
        val accounts = accountDao.getAccountsByUser(userId).first()
        
        accounts.forEach { account ->
            val createDate = dateFormat.format(Date(account.createdAt))
            val isDefault = if (account.isDefault) "是" else "否"
            
            // 对于信用卡账户，包含额外信息
            val creditLimit = account.creditLimitCents?.let { it / 100.0 }?.toString() ?: ""
            val billingDay = account.billingDay?.toString() ?: ""
            val paymentDay = account.paymentDueDay?.toString() ?: ""
            
            writer.appendLine(
                "ACCOUNT,${csvEscape(createDate)},${csvEscape(account.name)}," +
                "${account.type},${account.balanceCents / 100.0},$creditLimit," +
                "$billingDay,$paymentDay,$isDefault,${csvEscape(account.icon ?: "")}"
            )
        }
    }
    
    /**
     * 写入分类信息
     */
    private suspend fun writeCategories(writer: FileWriter, userId: String) {
        val categories = categoryDao.getCategoriesByUser(userId).first()
        val categoryMap = categories.associateBy { it.id }
        
        categories.forEach { category ->
            val createDate = dateFormat.format(Date(category.createdAt))
            val parentName = category.parentId?.let { categoryMap[it]?.name } ?: ""
            
            writer.appendLine(
                "CATEGORY,${csvEscape(createDate)},${csvEscape(category.name)}," +
                "${category.type},${category.icon},${category.color}," +
                "${csvEscape(parentName)},${category.displayOrder},,"
            )
        }
    }
    
    /**
     * 写入交易记录
     */
    private suspend fun writeTransactions(writer: FileWriter, userId: String, startDate: Long?, endDate: Long?) {
        val transactions = when {
            startDate != null && endDate != null -> {
                transactionDao.getTransactionsByDateRangeSync(userId, startDate, endDate)
            }
            else -> {
                transactionDao.getTransactionsByUser(userId).first()
            }
        }
        
        val accounts = accountDao.getAccountsByUser(userId).first().associateBy { it.id }
        val categories = categoryDao.getCategoriesByUser(userId).first().associateBy { it.id }
        
        transactions.forEach { transaction ->
            val date = dateFormat.format(Date(transaction.createdAt))
            val accountName = accounts[transaction.accountId]?.name ?: "未知账户"
            val categoryName = categories[transaction.categoryId]?.name ?: "未知分类"
            val amount = transaction.amountCents / 100.0
            
            writer.appendLine(
                "TRANSACTION,${csvEscape(date)},${csvEscape(accountName)}," +
                "${csvEscape(categoryName)},$amount,${csvEscape(transaction.note ?: "")}," +
                "否,,,"// 是否定期生成，暂时都是"否"
            )
        }
    }
    
    /**
     * 写入预算信息
     */
    private suspend fun writeBudgets(writer: FileWriter, userId: String) {
        val budgets = budgetDao.getBudgetsByUser(userId).first()
        val categories = categoryDao.getCategoriesByUser(userId).first().associateBy { it.id }
        
        budgets.forEach { budget ->
            val yearMonth = "${budget.year}-${String.format("%02d", budget.month)}"
            val categoryName = budget.categoryId?.let { categories[it]?.name } ?: "总预算"
            val amount = budget.budgetAmountCents / 100.0
            val threshold = "${(budget.alertThreshold * 100).toInt()}%"
            
            // 计算已使用金额（需要查询交易记录）
            val used = 0.0 // TODO: 计算实际使用金额
            val remaining = amount - used
            
            writer.appendLine(
                "BUDGET,$yearMonth,${csvEscape(categoryName)}," +
                "$amount,$threshold,$used,$remaining,,," +
                "${csvEscape(budget.note ?: "")}"
            )
        }
    }
    
    /**
     * 写入定期交易
     */
    private suspend fun writeRecurringTransactions(writer: FileWriter, userId: String) {
        val recurringTransactions = recurringTransactionDao.getAllRecurringTransactions(userId).first()
        val accounts = accountDao.getAccountsByUser(userId).first().associateBy { it.id }
        val categories = categoryDao.getCategoriesByUser(userId).first().associateBy { it.id }
        
        recurringTransactions.forEach { recurring ->
            val accountName = accounts[recurring.accountId]?.name ?: "未知账户"
            val categoryName = categories[recurring.categoryId]?.name ?: "未知分类"
            val amount = recurring.amountCents / 100.0
            val startDate = dateFormat.format(Date(recurring.startDate))
            val endDate = recurring.endDate?.let { dateFormat.format(Date(it)) } ?: ""
            
            // 频率和执行日
            val frequency = when (recurring.frequency.name) {
                "DAILY" -> "每日"
                "WEEKLY" -> "每周"
                "MONTHLY" -> "每月"
                "YEARLY" -> "每年"
                else -> recurring.frequency.name
            }
            val executionDay = when {
                recurring.dayOfMonth != null -> "${recurring.dayOfMonth}号"
                recurring.dayOfWeek != null -> "周${recurring.dayOfWeek}"
                else -> ""
            }
            
            writer.appendLine(
                "RECURRING,$frequency,$executionDay,${csvEscape(accountName)}," +
                "${csvEscape(categoryName)},$amount,${csvEscape(recurring.name)}," +
                "${csvEscape(startDate)},${csvEscape(endDate)}," +
                "${csvEscape(recurring.note ?: "")}"
            )
        }
    }
    
    /**
     * 写入储蓄目标
     */
    private suspend fun writeSavingsGoals(writer: FileWriter, userId: String) {
        val savingsGoals = savingsGoalDao.getAllSavingsGoals().first()
        
        savingsGoals.forEach { goal ->
            val progress = if (goal.targetAmount > 0) {
                "${((goal.currentAmount / goal.targetAmount) * 100).toInt()}%"
            } else "0%"
            
            val targetDate = goal.targetDate?.toString() ?: ""
            
            writer.appendLine(
                "SAVINGS,${csvEscape(goal.name)},${goal.targetAmount}," +
                "${goal.currentAmount},${csvEscape(targetDate)},$progress," +
                "${goal.color},,," +
                "${csvEscape(goal.description ?: "")}"
            )
        }
    }
    
    /**
     * 写入信用卡账单
     */
    private suspend fun writeCreditCardBills(writer: FileWriter, userId: String) {
        // 获取所有信用卡账户
        val creditCardAccounts = accountDao.getAccountsByUser(userId).first()
            .filter { it.type == "CREDIT_CARD" }
        
        creditCardAccounts.forEach { account ->
            val bills = creditCardBillDao.getBillsByAccount(account.id).first()
            
            bills.forEach { bill ->
                val startDate = dateFormat.format(Date(bill.billStartDate))
                val endDate = dateFormat.format(Date(bill.billEndDate))
                val dueDate = dateFormat.format(Date(bill.paymentDueDate))
                val totalAmount = bill.totalAmountCents / 100.0
                val paidAmount = bill.paidAmountCents / 100.0
                val minimumPayment = bill.minimumPaymentCents / 100.0
                val isOverdue = if (bill.isOverdue) "是" else "否"
                
                writer.appendLine(
                    "CREDITBILL,${csvEscape(account.name)},${csvEscape(startDate)}," +
                    "${csvEscape(endDate)},$totalAmount,$paidAmount," +
                    "$minimumPayment,${csvEscape(dueDate)},$isOverdue,"
                )
            }
        }
    }
    
    /**
     * CSV转义处理
     */
    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
    
}
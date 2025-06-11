package com.ccxiaoji.feature.ledger.api

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.ccxiaoji.core.common.utils.daysUntil

/**
 * Ledger模块对外暴露的API接口
 * 分步迁移：先提供统计功能相关API
 */
interface LedgerApi {
    // ========== 统计功能 ==========
    
    /**
     * 获取今日收支统计
     */
    suspend fun getTodayStatistics(): DailyStatistics
    
    /**
     * 获取本月支出总额
     */
    suspend fun getMonthlyExpense(year: Int, month: Int): Double
    
    /**
     * 获取指定日期范围的收支统计
     */
    suspend fun getStatisticsByDateRange(startDate: LocalDate, endDate: LocalDate): PeriodStatistics
    
    /**
     * 获取账户总余额
     */
    suspend fun getTotalBalance(): Double
    
    /**
     * 获取最近交易记录（用于首页展示）
     */
    fun getRecentTransactions(limit: Int): Flow<List<TransactionItem>>
    
    // ========== 导航功能 ==========
    
    /**
     * 导航到记账主页
     */
    fun navigateToLedger()
    
    /**
     * 导航到快速记账
     */
    fun navigateToQuickAdd()
    
    /**
     * 导航到统计页面
     */
    fun navigateToStatistics()
    
    /**
     * 导航到账户管理
     */
    fun navigateToAccounts()
    
    /**
     * 导航到分类管理
     */
    fun navigateToCategories()
    
    // ========== 分类管理功能 ==========
    
    /**
     * 获取所有分类
     */
    suspend fun getAllCategories(): List<CategoryItem>
    
    /**
     * 根据类型获取分类
     */
    suspend fun getCategoriesByType(type: String): List<CategoryItem>
    
    /**
     * 添加分类
     */
    suspend fun addCategory(name: String, type: String, icon: String, color: String, parentId: String? = null)
    
    /**
     * 更新分类
     */
    suspend fun updateCategory(categoryId: String, name: String, icon: String, color: String)
    
    /**
     * 删除分类
     */
    suspend fun deleteCategory(categoryId: String)
    
    /**
     * 获取分类使用次数
     */
    suspend fun getCategoryUsageCount(categoryId: String): Int
    
    // ========== 交易记录功能 ==========
    
    /**
     * 获取指定月份的交易记录
     */
    suspend fun getTransactionsByMonth(year: Int, month: Int): List<TransactionItem>
    
    /**
     * 获取最近交易记录列表
     */
    suspend fun getRecentTransactionsList(limit: Int = 10): List<TransactionItem>
    
    /**
     * 添加交易记录
     */
    suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String? = null
    ): String // 返回交易ID
    
    /**
     * 更新交易记录
     */
    suspend fun updateTransaction(
        transactionId: String,
        amountCents: Int,
        categoryId: String,
        note: String?
    )
    
    /**
     * 删除交易记录
     */
    suspend fun deleteTransaction(transactionId: String)
    
    /**
     * 批量删除交易记录
     */
    suspend fun deleteTransactions(transactionIds: List<String>)
    
    /**
     * 搜索交易记录
     */
    suspend fun searchTransactions(query: String): List<TransactionItem>
    
    /**
     * 根据账户获取交易记录
     */
    suspend fun getTransactionsByAccount(accountId: String): List<TransactionItem>
    
    /**
     * 获取交易详情
     */
    suspend fun getTransactionDetail(transactionId: String): TransactionDetail?
    
    /**
     * 获取日期范围内的交易统计
     */
    suspend fun getTransactionStatsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): TransactionStats
    
    /**
     * 导航到交易详情
     */
    fun navigateToTransactionDetail(transactionId: String)
    
    // ========== 账户管理功能 ==========
    
    /**
     * 获取所有账户列表
     */
    fun getAccountsFlow(): Flow<List<AccountItem>>
    
    /**
     * 获取账户列表（非Flow）
     */
    suspend fun getAccounts(): List<AccountItem>
    
    /**
     * 获取账户详情
     */
    suspend fun getAccountById(accountId: String): AccountItem?
    
    /**
     * 获取默认账户
     */
    suspend fun getDefaultAccount(): AccountItem?
    
    /**
     * 创建账户
     */
    suspend fun createAccount(
        name: String,
        type: String,
        initialBalanceCents: Long = 0L,
        currency: String = "CNY",
        icon: String? = null,
        color: String? = null,
        creditLimitCents: Long? = null,
        billingDay: Int? = null,
        paymentDueDay: Int? = null,
        gracePeriodDays: Int? = null
    ): AccountItem
    
    /**
     * 更新账户信息
     */
    suspend fun updateAccount(account: AccountItem)
    
    /**
     * 设置默认账户
     */
    suspend fun setDefaultAccount(accountId: String)
    
    /**
     * 删除账户
     */
    suspend fun deleteAccount(accountId: String)
    
    /**
     * 账户间转账
     */
    suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long
    )
    
    /**
     * 获取信用卡账户列表
     */
    fun getCreditCardAccountsFlow(): Flow<List<AccountItem>>
    
    /**
     * 更新信用卡信息
     */
    suspend fun updateCreditCardInfo(
        accountId: String,
        creditLimitCents: Long,
        billingDay: Int,
        paymentDueDay: Int,
        gracePeriodDays: Int = 3
    )
    
    /**
     * 记录信用卡还款
     */
    suspend fun recordCreditCardPayment(
        accountId: String,
        paymentAmountCents: Long,
        paymentType: String,
        dueAmountCents: Long,
        note: String? = null
    )
    
    /**
     * 获取信用卡还款记录
     */
    fun getCreditCardPaymentsFlow(accountId: String): Flow<List<PaymentRecord>>
    
    /**
     * 生成信用卡账单
     */
    suspend fun generateCreditCardBill(accountId: String)
    
    /**
     * 获取信用卡账单列表
     */
    fun getCreditCardBillsFlow(accountId: String): Flow<List<CreditCardBill>>
    
    /**
     * 获取当前账单
     */
    suspend fun getCurrentCreditCardBill(accountId: String): CreditCardBill?
    
    /**
     * 获取账单内交易记录
     */
    suspend fun getTransactionsForBill(billId: String): List<TransactionItem>
    
    /**
     * 更新账单支付状态
     */
    suspend fun updateBillPaymentStatus(billId: String, paymentAmountCents: Long)
    
    // ========== 预算管理功能 ==========
    
    /**
     * 获取指定月份的预算列表（包含花费信息）
     */
    fun getBudgetsWithSpent(year: Int, month: Int): Flow<List<BudgetItem>>
    
    /**
     * 获取总预算（指定月份）
     */
    suspend fun getTotalBudget(year: Int, month: Int): BudgetItem?
    
    /**
     * 获取分类预算（指定月份和分类）
     */
    suspend fun getCategoryBudget(year: Int, month: Int, categoryId: String): BudgetItem?
    
    /**
     * 创建或更新预算
     */
    suspend fun upsertBudget(
        year: Int,
        month: Int,
        budgetAmountCents: Int,
        categoryId: String? = null,
        alertThreshold: Float = 0.8f,
        note: String? = null
    ): BudgetItem
    
    /**
     * 更新预算
     */
    suspend fun updateBudget(
        budgetId: String,
        budgetAmountCents: Int? = null,
        alertThreshold: Float? = null,
        note: String? = null
    ): BudgetItem?
    
    /**
     * 删除预算
     */
    suspend fun deleteBudget(budgetId: String)
    
    /**
     * 检查预算是否超支
     */
    suspend fun checkBudgetExceeded(year: Int, month: Int, categoryId: String? = null): Boolean
    
    /**
     * 检查预算是否触发预警
     */
    suspend fun checkBudgetAlert(year: Int, month: Int, categoryId: String? = null): Boolean
    
    /**
     * 获取预算使用百分比
     */
    suspend fun getBudgetUsagePercentage(year: Int, month: Int, categoryId: String? = null): Float?
    
    /**
     * 获取所有预算报警信息
     */
    suspend fun getBudgetAlerts(year: Int, month: Int): List<BudgetAlert>
    
    // ========== 存钱目标管理功能 ==========
    
    /**
     * 获取所有存钱目标列表
     */
    fun getSavingsGoalsFlow(): Flow<List<SavingsGoalItem>>
    
    /**
     * 获取存钱目标列表（非Flow）
     */
    suspend fun getSavingsGoals(): List<SavingsGoalItem>
    
    /**
     * 获取活跃的存钱目标列表
     */
    suspend fun getActiveSavingsGoals(): List<SavingsGoalItem>
    
    /**
     * 获取存钱目标详情
     */
    suspend fun getSavingsGoalById(goalId: Long): SavingsGoalItem?
    
    /**
     * 创建存钱目标
     */
    suspend fun createSavingsGoal(
        name: String,
        targetAmountCents: Long,
        targetDate: LocalDate? = null,
        description: String? = null,
        color: String = "#4CAF50",
        iconName: String = "savings"
    ): SavingsGoalItem
    
    /**
     * 更新存钱目标
     */
    suspend fun updateSavingsGoal(
        goalId: Long,
        name: String,
        targetAmountCents: Long,
        targetDate: LocalDate? = null,
        description: String? = null,
        color: String,
        iconName: String
    ): SavingsGoalItem?
    
    /**
     * 删除存钱目标
     */
    suspend fun deleteSavingsGoal(goalId: Long)
    
    /**
     * 设置存钱目标活跃状态
     */
    suspend fun setSavingsGoalActive(goalId: Long, isActive: Boolean)
    
    /**
     * 添加存钱贡献记录
     */
    suspend fun addSavingsContribution(
        goalId: Long,
        amountCents: Long,
        note: String? = null
    ): SavingsContributionItem
    
    /**
     * 获取存钱目标的贡献记录
     */
    suspend fun getSavingsContributions(goalId: Long): List<SavingsContributionItem>
    
    /**
     * 删除存钱贡献记录
     */
    suspend fun deleteSavingsContribution(contributionId: Long)
    
    /**
     * 获取存钱目标统计摘要（用于首页展示）
     */
    suspend fun getSavingsGoalsSummary(): SavingsGoalsSummary
    
    /**
     * 获取活跃存钱目标数量
     */
    suspend fun getActiveSavingsGoalsCount(): Int
    
    /**
     * 导航到存钱目标页面
     */
    fun navigateToSavingsGoals()
    
    /**
     * 导航到存钱目标详情页面
     */
    fun navigateToSavingsGoalDetail(goalId: Long)
    
    // ==================== 信用卡管理相关 API ====================
    
    /**
     * 获取所有信用卡账户列表
     */
    fun getCreditCards(): Flow<List<AccountItem>>
    
    /**
     * 添加信用卡
     */
    suspend fun addCreditCard(
        name: String,
        creditLimitYuan: Double,
        usedAmountYuan: Double,
        billingDay: Int,
        paymentDueDay: Int
    )
    
    /**
     * 更新信用卡信息
     */
    suspend fun updateCreditCardInfo(
        accountId: String,
        creditLimitYuan: Double,
        usedAmountYuan: Double,
        billingDay: Int,
        paymentDueDay: Int
    )
    
    /**
     * 记录信用卡还款
     */
    suspend fun recordCreditCardPayment(
        accountId: String,
        paymentAmountYuan: Double,
        paymentType: String,
        note: String? = null
    )
    
    /**
     * 获取信用卡还款记录
     */
    fun getCreditCardPayments(accountId: String): Flow<List<PaymentRecord>>
    
    /**
     * 获取信用卡还款统计
     */
    suspend fun getPaymentStats(accountId: String): PaymentStats
    
    /**
     * 删除还款记录
     */
    suspend fun deletePaymentRecord(paymentId: String)
    
    /**
     * 检查还款提醒
     */
    suspend fun checkPaymentReminders(): List<AccountItem>
    
    // ==================== 信用卡账单相关 API ====================
    
    /**
     * 获取信用卡账单列表
     */
    fun getCreditCardBills(accountId: String): Flow<List<CreditCardBill>>
    
    /**
     * 获取账单详情
     */
    suspend fun getCreditCardBillDetail(billId: String): CreditCardBill?
    
    /**
     * 标记逾期账单
     */
    suspend fun markOverdueBills(accountId: String)
    
    /**
     * 获取需要还款提醒的信用卡
     */
    suspend fun getCreditCardsWithPaymentDue(dayOfMonth: Int): List<AccountItem>
    
    /**
     * 获取有欠款的信用卡
     */
    suspend fun getCreditCardsWithDebt(): List<AccountItem>
    
    /**
     * 导航到信用卡管理页面
     */
    fun navigateToCreditCards()
    
    /**
     * 导航到信用卡账单页面
     */
    fun navigateToCreditCardBills(accountId: String)
    
    /**
     * 导航到按账户筛选的交易列表
     */
    fun navigateToTransactionsByAccount(accountId: String)
    
    // ========== 定期交易管理功能 ==========
    
    /**
     * 获取所有定期交易
     */
    fun getAllRecurringTransactions(): Flow<List<RecurringTransactionItem>>
    
    /**
     * 获取启用的定期交易
     */
    fun getEnabledRecurringTransactions(): Flow<List<RecurringTransactionItem>>
    
    /**
     * 根据ID获取定期交易
     */
    suspend fun getRecurringTransactionById(id: String): RecurringTransactionItem?
    
    /**
     * 创建定期交易
     */
    suspend fun createRecurringTransaction(
        name: String,
        accountId: String,
        amountCents: Int,
        categoryId: String,
        note: String? = null,
        frequency: String,
        dayOfWeek: Int? = null,
        dayOfMonth: Int? = null,
        monthOfYear: Int? = null,
        startDate: Long,
        endDate: Long? = null
    ): RecurringTransactionItem
    
    /**
     * 更新定期交易
     */
    suspend fun updateRecurringTransaction(
        id: String,
        name: String,
        accountId: String,
        amountCents: Int,
        categoryId: String,
        note: String? = null,
        frequency: String,
        dayOfWeek: Int? = null,
        dayOfMonth: Int? = null,
        monthOfYear: Int? = null,
        startDate: Long,
        endDate: Long? = null
    )
    
    /**
     * 切换定期交易启用状态
     */
    suspend fun toggleRecurringTransactionEnabled(id: String)
    
    /**
     * 删除定期交易
     */
    suspend fun deleteRecurringTransaction(id: String)
    
    /**
     * 执行到期的定期交易
     */
    suspend fun executeDueRecurringTransactions(): Int
    
    /**
     * 导航到定期交易页面
     */
    fun navigateToRecurringTransactions()
}

/**
 * 每日统计数据
 */
data class DailyStatistics(
    val income: Double,
    val expense: Double,
    val balance: Double
)

/**
 * 期间统计数据
 */
data class PeriodStatistics(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val transactionCount: Int
)

/**
 * 交易记录条目（简化版，用于列表展示）
 */
data class TransactionItem(
    val id: String,
    val amount: Double,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: String,
    val accountName: String,
    val note: String?,
    val date: LocalDate
)

/**
 * 分类信息（用于展示）
 */
data class CategoryItem(
    val id: String,
    val name: String,
    val type: String,
    val icon: String,
    val color: String,
    val parentId: String? = null,
    val isSystem: Boolean = false,
    val usageCount: Int = 0
)

/**
 * 交易详情（完整信息）
 */
data class TransactionDetail(
    val id: String,
    val amountCents: Int,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val categoryType: String,
    val accountId: String,
    val accountName: String,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val amountYuan: Double
        get() = amountCents / 100.0
}

/**
 * 交易统计信息
 */
data class TransactionStats(
    val totalIncome: Int,
    val totalExpense: Int,
    val transactionCount: Int,
    val categoryStats: List<CategoryStat>
) {
    val totalIncomeYuan: Double
        get() = totalIncome / 100.0
    
    val totalExpenseYuan: Double
        get() = totalExpense / 100.0
    
    val balance: Int
        get() = totalIncome - totalExpense
    
    val balanceYuan: Double
        get() = balance / 100.0
}

/**
 * 分类统计
 */
data class CategoryStat(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val totalAmount: Int,
    val transactionCount: Int,
    val percentage: Float
) {
    val totalAmountYuan: Double
        get() = totalAmount / 100.0
}

/**
 * 账户信息（用于展示）
 */
data class AccountItem(
    val id: String,
    val name: String,
    val type: String,
    val balanceCents: Long,
    val currency: String,
    val icon: String? = null,
    val color: String? = null,
    val isDefault: Boolean = false,
    val creditLimitCents: Long? = null,
    val billingDay: Int? = null,
    val paymentDueDay: Int? = null,
    val gracePeriodDays: Int? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val balanceYuan: Double
        get() = balanceCents / 100.0
    
    val creditLimitYuan: Double?
        get() = creditLimitCents?.let { it / 100.0 }
    
    val availableCreditYuan: Double?
        get() = creditLimitCents?.let {
            val available = it + balanceCents
            available / 100.0
        }
    
    val creditUsageRate: Double?
        get() = creditLimitCents?.let {
            if (it == 0L) 0.0
            else ((it - (it + balanceCents)).toDouble() / it) * 100
        }
}

/**
 * 信用卡还款记录
 */
data class PaymentRecord(
    val id: String,
    val accountId: String,
    val paymentAmountCents: Long,
    val paymentType: String,
    val paymentDate: Long,
    val dueAmountCents: Long,
    val isOnTime: Boolean,
    val note: String? = null
) {
    val paymentAmountYuan: Double
        get() = paymentAmountCents / 100.0
    
    val dueAmountYuan: Double
        get() = dueAmountCents / 100.0
}

/**
 * 信用卡支付统计
 */
data class PaymentStats(
    val onTimeRate: Double,       // 准时还款率（百分比）
    val totalPayments: Int,       // 总还款次数
    val totalAmountYuan: Double   // 总还款金额（元）
)

/**
 * 信用卡账单
 */
data class CreditCardBill(
    val id: String,
    val accountId: String,
    val billStartDate: Long,
    val billEndDate: Long,
    val paymentDueDate: Long,
    val totalAmountCents: Long,
    val newChargesCents: Long,
    val previousBalanceCents: Long,
    val paymentsCents: Long,
    val adjustmentsCents: Long,
    val minimumPaymentCents: Long,
    val isPaid: Boolean,
    val paidAmountCents: Long,
    val isOverdue: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    val totalAmountYuan: Double
        get() = totalAmountCents / 100.0
    
    val newChargesYuan: Double
        get() = newChargesCents / 100.0
    
    val previousBalanceYuan: Double
        get() = previousBalanceCents / 100.0
    
    val paymentsYuan: Double
        get() = paymentsCents / 100.0
    
    val minimumPaymentYuan: Double
        get() = minimumPaymentCents / 100.0
    
    val paidAmountYuan: Double
        get() = paidAmountCents / 100.0
    
    val remainingAmountCents: Long
        get() = totalAmountCents - paidAmountCents
    
    val remainingAmountYuan: Double
        get() = remainingAmountCents / 100.0
}

/**
 * 预算项目（包含花费信息）
 */
data class BudgetItem(
    val id: String,
    val year: Int,
    val month: Int,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null,
    val budgetAmountCents: Int,
    val spentAmountCents: Int,
    val alertThreshold: Float,
    val note: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val budgetAmountYuan: Double
        get() = budgetAmountCents / 100.0
    
    val spentAmountYuan: Double
        get() = spentAmountCents / 100.0
    
    val remainingAmountCents: Int
        get() = budgetAmountCents - spentAmountCents
    
    val remainingAmountYuan: Double
        get() = remainingAmountCents / 100.0
    
    val usagePercentage: Float
        get() = if (budgetAmountCents > 0) {
            (spentAmountCents.toFloat() / budgetAmountCents.toFloat()) * 100f
        } else {
            0f
        }
    
    val isExceeded: Boolean
        get() = spentAmountCents > budgetAmountCents
    
    val isAlert: Boolean
        get() = usagePercentage >= alertThreshold * 100
    
    val isTotalBudget: Boolean
        get() = categoryId == null
}

/**
 * 预算警告信息
 */
data class BudgetAlert(
    val budgetId: String,
    val categoryName: String?,
    val budgetAmountYuan: Double,
    val spentAmountYuan: Double,
    val usagePercentage: Float,
    val isExceeded: Boolean,
    val alertThreshold: Float
)

/**
 * 存钱目标项目（用于展示）
 */
data class SavingsGoalItem(
    val id: Long,
    val name: String,
    val targetAmountCents: Long,
    val currentAmountCents: Long,
    val targetDate: LocalDate? = null,
    val description: String? = null,
    val color: String = "#4CAF50",
    val iconName: String = "savings",
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val targetAmountYuan: Double
        get() = targetAmountCents / 100.0
    
    val currentAmountYuan: Double
        get() = currentAmountCents / 100.0
    
    val remainingAmountCents: Long
        get() = (targetAmountCents - currentAmountCents).coerceAtLeast(0L)
    
    val remainingAmountYuan: Double
        get() = remainingAmountCents / 100.0
    
    val progress: Float
        get() = if (targetAmountCents > 0) {
            (currentAmountCents.toFloat() / targetAmountCents.toFloat()).coerceIn(0f, 1f)
        } else 0f
    
    val progressPercentage: Int
        get() = (progress * 100).toInt()
    
    val isCompleted: Boolean
        get() = currentAmountCents >= targetAmountCents
    
    val daysRemaining: Int?
        get() = targetDate?.let { target ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            today.daysUntil(target).takeIf { it > 0 }
        }
}

/**
 * 存钱贡献记录项目
 */
data class SavingsContributionItem(
    val id: Long,
    val goalId: Long,
    val amountCents: Long,
    val note: String? = null,
    val createdAt: Instant
) {
    val amountYuan: Double
        get() = amountCents / 100.0
    
    val isDeposit: Boolean
        get() = amountCents > 0
    
    val isWithdrawal: Boolean
        get() = amountCents < 0
}

/**
 * 存钱目标统计摘要
 */
data class SavingsGoalsSummary(
    val activeGoalsCount: Int,
    val completedGoalsCount: Int,
    val totalTargetAmountCents: Long,
    val totalCurrentAmountCents: Long,
    val totalProgress: Float
) {
    val totalTargetAmountYuan: Double
        get() = totalTargetAmountCents / 100.0
    
    val totalCurrentAmountYuan: Double
        get() = totalCurrentAmountCents / 100.0
    
    val totalRemainingAmountCents: Long
        get() = (totalTargetAmountCents - totalCurrentAmountCents).coerceAtLeast(0L)
    
    val totalRemainingAmountYuan: Double
        get() = totalRemainingAmountCents / 100.0
}

/**
 * 定期交易项目（用于展示）
 */
data class RecurringTransactionItem(
    val id: String,
    val name: String,
    val accountId: String,
    val accountName: String? = null,
    val amountCents: Int,
    val categoryId: String,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null,
    val note: String? = null,
    val frequency: String,
    val dayOfWeek: Int? = null,
    val dayOfMonth: Int? = null,
    val monthOfYear: Int? = null,
    val startDate: Long,
    val endDate: Long? = null,
    val isEnabled: Boolean = true,
    val lastExecutionDate: Long? = null,
    val nextExecutionDate: Long,
    val createdAt: Long,
    val updatedAt: Long
) {
    val amountYuan: Double
        get() = amountCents / 100.0
    
    val isIncome: Boolean
        get() = amountCents > 0
    
    val isExpense: Boolean
        get() = amountCents < 0
    
    val isActive: Boolean
        get() = isEnabled && (endDate == null || System.currentTimeMillis() < endDate)
}
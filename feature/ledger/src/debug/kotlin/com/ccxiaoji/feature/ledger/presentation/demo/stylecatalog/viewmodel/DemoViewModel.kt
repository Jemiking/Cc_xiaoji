package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoStyle
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// Demo专用的简化Transaction模型
data class DemoTransaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val category: DemoCategory?,
    val account: DemoAccount?,
    val note: String?,
    val dateTime: Instant,
    val tags: List<DemoTag> = emptyList()
) {
    fun toLocalDate(): java.time.LocalDate {
        return dateTime.toJavaInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
}

enum class TransactionType {
    INCOME, EXPENSE
}

data class DemoCategory(
    val id: String,
    val name: String,
    val icon: String,
    val type: TransactionType
)

data class DemoAccount(
    val id: String,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val icon: String
)

enum class AccountType {
    CASH, BANK_CARD, ALIPAY, WECHAT, CREDIT_CARD, OTHER
}

data class DemoBudget(
    val id: String,
    val name: String,
    val amount: Double,
    val spent: Double,
    val category: DemoCategory,
    val startDate: java.time.LocalDate,
    val endDate: java.time.LocalDate
)

data class DemoTag(
    val id: String,
    val name: String,
    val color: String
)

data class TransactionStats(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val transactionCount: Int
)

/**
 * Demo ViewModel - 管理Demo的所有状态和沙箱数据
 * 使用内存存储，不影响真实数据库
 */
class DemoViewModel : ViewModel() {
    
    // ========== 风格和主题状态 ==========
    private val _currentStyle = MutableStateFlow(DemoStyle.MaterialYou)
    val currentStyle: StateFlow<DemoStyle> = _currentStyle.asStateFlow()
    
    private val _currentDensity = MutableStateFlow(DemoDensity.Medium)
    val currentDensity: StateFlow<DemoDensity> = _currentDensity.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // 系统栏模式（状态栏颜色策略）
    enum class SystemBarsMode { FollowPrimary, CustomColor }
    private val _systemBarsMode = MutableStateFlow(SystemBarsMode.CustomColor)
    val systemBarsMode: StateFlow<SystemBarsMode> = _systemBarsMode.asStateFlow()
    private val _systemBarsCustomColor = MutableStateFlow<Int?>(0xFF3B82F6.toInt()) // ARGB
    val systemBarsCustomColor: StateFlow<Int?> = _systemBarsCustomColor.asStateFlow()
    
    // ========== 沙箱数据 ==========
    // 交易记录（内存存储）
    private val _transactions = MutableStateFlow<List<DemoTransaction>>(emptyList())
    val transactions: StateFlow<List<DemoTransaction>> = _transactions.asStateFlow()
    
    // 分类（内存存储）
    private val _categories = MutableStateFlow<List<DemoCategory>>(emptyList())
    val categories: StateFlow<List<DemoCategory>> = _categories.asStateFlow()
    
    // 账户（内存存储）
    private val _accounts = MutableStateFlow<List<DemoAccount>>(emptyList())
    val accounts: StateFlow<List<DemoAccount>> = _accounts.asStateFlow()
    
    // 预算（内存存储）
    private val _budgets = MutableStateFlow<List<DemoBudget>>(emptyList())
    val budgets: StateFlow<List<DemoBudget>> = _budgets.asStateFlow()
    
    // 标签（内存存储）
    private val _tags = MutableStateFlow<List<DemoTag>>(emptyList())
    val tags: StateFlow<List<DemoTag>> = _tags.asStateFlow()
    
    init {
        // 初始化演示数据
        initializeDemoData()
    }
    
    // ========== 风格和主题操作 ==========
    fun setStyle(style: DemoStyle) {
        _currentStyle.value = style
    }
    
    fun setDensity(density: DemoDensity) {
        _currentDensity.value = density
    }
    
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setSystemBarsMode(mode: SystemBarsMode) {
        _systemBarsMode.value = mode
    }

    fun setSystemBarsCustomColor(argb: Int?) {
        _systemBarsCustomColor.value = argb
    }
    
    // ========== 数据操作 ==========
    fun addTransaction(transaction: DemoTransaction) {
        viewModelScope.launch {
            _transactions.value = _transactions.value + transaction
        }
    }
    
    fun updateTransaction(transaction: DemoTransaction) {
        viewModelScope.launch {
            _transactions.value = _transactions.value.map {
                if (it.id == transaction.id) transaction else it
            }
        }
    }
    
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _transactions.value = _transactions.value.filter { it.id != transactionId }
        }
    }
    
    fun addCategory(category: DemoCategory) {
        viewModelScope.launch {
            _categories.value = _categories.value + category
        }
    }
    
    fun addAccount(account: DemoAccount) {
        viewModelScope.launch {
            _accounts.value = _accounts.value + account
        }
    }
    
    // ========== 初始化演示数据 ==========
    private fun initializeDemoData() {
        // 初始化分类
        val defaultCategories = listOf(
            DemoCategory(id = "cat1", name = "餐饮", icon = "restaurant", type = TransactionType.EXPENSE),
            DemoCategory(id = "cat2", name = "交通", icon = "directions_car", type = TransactionType.EXPENSE),
            DemoCategory(id = "cat3", name = "购物", icon = "shopping_bag", type = TransactionType.EXPENSE),
            DemoCategory(id = "cat4", name = "娱乐", icon = "sports_esports", type = TransactionType.EXPENSE),
            DemoCategory(id = "cat5", name = "医疗", icon = "local_hospital", type = TransactionType.EXPENSE),
            DemoCategory(id = "cat6", name = "工资", icon = "payments", type = TransactionType.INCOME),
            DemoCategory(id = "cat7", name = "奖金", icon = "card_giftcard", type = TransactionType.INCOME),
            DemoCategory(id = "cat8", name = "投资收益", icon = "trending_up", type = TransactionType.INCOME)
        )
        _categories.value = defaultCategories
        
        // 初始化账户
        val defaultAccounts = listOf(
            DemoAccount(id = "acc1", name = "现金", type = AccountType.CASH, balance = 5000.0, icon = "account_balance_wallet"),
            DemoAccount(id = "acc2", name = "支付宝", type = AccountType.ALIPAY, balance = 12000.0, icon = "account_balance"),
            DemoAccount(id = "acc3", name = "微信", type = AccountType.WECHAT, balance = 8000.0, icon = "account_balance"),
            DemoAccount(id = "acc4", name = "银行卡", type = AccountType.BANK_CARD, balance = 50000.0, icon = "credit_card")
        )
        _accounts.value = defaultAccounts
        
        // 初始化交易记录（最近7天的数据）
        val demoTransactions = mutableListOf<DemoTransaction>()
        val today = java.time.LocalDate.now()
        
        // 生成7天的演示数据
        for (dayOffset in 0..6) {
            val date = today.minusDays(dayOffset.toLong())
            
            // 每天生成3-5笔交易
            val transactionCount = (3..5).random()
            for (i in 1..transactionCount) {
                val isExpense = Math.random() > 0.3 // 70%概率是支出
                val category = if (isExpense) {
                    defaultCategories.filter { it.type == TransactionType.EXPENSE }.random()
                } else {
                    defaultCategories.filter { it.type == TransactionType.INCOME }.random()
                }
                
                val amount = when (category.name) {
                    "餐饮" -> (20..100).random().toDouble()
                    "交通" -> (5..50).random().toDouble()
                    "购物" -> (50..500).random().toDouble()
                    "娱乐" -> (30..200).random().toDouble()
                    "医疗" -> (50..1000).random().toDouble()
                    "工资" -> 8000.0
                    "奖金" -> (500..2000).random().toDouble()
                    "投资收益" -> (100..1000).random().toDouble()
                    else -> (10..100).random().toDouble()
                }
                
                val instant = date.atStartOfDay(ZoneId.systemDefault())
                    .plusHours(i.toLong() * 3)
                    .toInstant()
                    .let { Instant.fromEpochMilliseconds(it.toEpochMilli()) }
                
                val transaction = DemoTransaction(
                    id = UUID.randomUUID().toString(),
                    amount = amount,
                    type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                    category = category,
                    account = defaultAccounts.random(),
                    note = generateNote(category.name),
                    dateTime = instant,
                    tags = emptyList()
                )
                demoTransactions.add(transaction)
            }
        }
        
        _transactions.value = demoTransactions.sortedByDescending { it.dateTime }
        
        // 初始化预算
        val defaultBudgets = listOf(
            DemoBudget(
                id = "budget1",
                name = "本月餐饮预算",
                amount = 3000.0,
                spent = 1250.0,
                category = defaultCategories[0],
                startDate = today.withDayOfMonth(1),
                endDate = today.withDayOfMonth(today.lengthOfMonth())
            ),
            DemoBudget(
                id = "budget2",
                name = "本月交通预算",
                amount = 500.0,
                spent = 235.0,
                category = defaultCategories[1],
                startDate = today.withDayOfMonth(1),
                endDate = today.withDayOfMonth(today.lengthOfMonth())
            )
        )
        _budgets.value = defaultBudgets
        
        // 初始化标签
        val defaultTags = listOf(
            DemoTag(id = "tag1", name = "必需品", color = "#FF5722"),
            DemoTag(id = "tag2", name = "非必需", color = "#9C27B0"),
            DemoTag(id = "tag3", name = "计划内", color = "#4CAF50"),
            DemoTag(id = "tag4", name = "冲动消费", color = "#F44336")
        )
        _tags.value = defaultTags
    }
    
    private fun generateNote(categoryName: String): String {
        return when (categoryName) {
            "餐饮" -> listOf("午餐", "晚餐", "下午茶", "夜宵", "聚餐").random()
            "交通" -> listOf("地铁", "公交", "打车", "加油", "停车费").random()
            "购物" -> listOf("日用品", "衣服", "电子产品", "生活用品", "零食").random()
            "娱乐" -> listOf("电影", "游戏", "KTV", "健身", "旅游").random()
            "医疗" -> listOf("看病", "买药", "体检", "牙科", "保健品").random()
            "工资" -> "月工资"
            "奖金" -> listOf("项目奖金", "年终奖", "绩效奖金", "季度奖").random()
            "投资收益" -> listOf("股票收益", "基金收益", "理财收益", "分红").random()
            else -> ""
        }
    }
    
    // ========== 数据查询 ==========
    fun getTransactionById(id: String): DemoTransaction? {
        return _transactions.value.find { it.id == id }
    }
    
    fun getTransactionsByDateRange(start: java.time.LocalDate, end: java.time.LocalDate): List<DemoTransaction> {
        return _transactions.value.filter { 
            val date = it.toLocalDate()
            date in start..end
        }
    }
    
    fun getTransactionsByCategory(categoryId: String): List<DemoTransaction> {
        return _transactions.value.filter { it.category?.id == categoryId }
    }
    
    fun getTransactionsByAccount(accountId: String): List<DemoTransaction> {
        return _transactions.value.filter { it.account?.id == accountId }
    }
    
    // ========== 统计数据 ==========
    fun getTodayStats(): TransactionStats {
        val today = java.time.LocalDate.now()
        val todayTransactions = getTransactionsByDateRange(today, today)
        return calculateStats(todayTransactions)
    }
    
    fun getMonthStats(): TransactionStats {
        val today = java.time.LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
        val monthTransactions = getTransactionsByDateRange(monthStart, monthEnd)
        return calculateStats(monthTransactions)
    }
    
    private fun calculateStats(transactions: List<DemoTransaction>): TransactionStats {
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val expense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        return TransactionStats(
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            transactionCount = transactions.size
        )
    }
}

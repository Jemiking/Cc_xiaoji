package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.usecase.*
import com.ccxiaoji.feature.ledger.data.cache.LedgerCacheManager
import com.ccxiaoji.feature.ledger.data.migration.DataMigrationTool
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getPaginatedTransactionsUseCase: GetPaginatedTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase,
    private val checkBudgetUseCase: CheckBudgetUseCase,
    private val cacheManager: LedgerCacheManager,
    private val dataMigrationTool: DataMigrationTool,
    private val userApi: UserApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState = _uiState.asStateFlow()
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()
    private var currentPage = 0
    private val pageSize = 20
    private var hasMoreData = true
    private var isLoadingMore = false
    
    init {
        // 首次启动时自动修复数据
        fixOrphanAccountData()
        loadAccounts()
        loadCategories()
        loadTransactions()
        loadMonthlySummary()
    }
    
    /**
     * 修复孤儿账户数据
     * 将导入时创建的临时账户交易迁移到默认账户
     */
    fun fixOrphanAccountData() = viewModelScope.launch {
        try {
            android.util.Log.e("LEDGER_DEBUG", "开始修复孤儿账户数据...")
            val userId = userApi.getCurrentUserId()
            dataMigrationTool.fixOrphanAccountTransactions(userId)
            android.util.Log.e("LEDGER_DEBUG", "数据修复完成，重新加载账户和交易")
            // 修复后重新加载数据
            loadAccounts()
            loadTransactions()
            loadMonthlySummary()
        } catch (e: Exception) {
            android.util.Log.e("LEDGER_DEBUG", "数据修复失败: ${e.message}", e)
        }
    }
    
    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        currentPage = 0
        hasMoreData = true
        loadTransactions()
        loadMonthlySummary()
    }
    
    private fun getMonthDateRange(month: YearMonth): Pair<Long, Long> {
        val start = LocalDate(month.year, month.monthValue, 1)
            .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val nextMonth = month.plusMonths(1)
        val end = LocalDate(nextMonth.year, nextMonth.monthValue, 1)
            .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return start to end
    }
    
    private fun loadTransactions(loadMore: Boolean = false) {
        if (loadMore && (isLoadingMore || !hasMoreData)) return
        viewModelScope.launch {
            android.util.Log.e("LEDGER_DEBUG", "========== 加载交易记录 ==========")
            android.util.Log.e("LEDGER_DEBUG", "loadMore: $loadMore, currentPage: $currentPage")
            
            if (loadMore) {
                isLoadingMore = true
                currentPage++
            } else {
                currentPage = 0
            }
            _uiState.update { it.copy(isLoading = !loadMore, isLoadingMore = loadMore) }
            
            val (start, end) = getMonthDateRange(_selectedMonth.value)
            android.util.Log.e("LEDGER_DEBUG", "查询月份: ${_selectedMonth.value}")
            android.util.Log.e("LEDGER_DEBUG", "日期范围: $start - $end")
            android.util.Log.e("LEDGER_DEBUG", "时间转换: 开始=${java.util.Date(start)}, 结束=${java.util.Date(end)}")
            // 如果选择了特定账户ID，并且不是默认账户（default_account_开头），则使用该ID
            // 否则不指定账户ID，查询所有账户
            val accountIdForQuery = _uiState.value.selectedAccountId?.let { id ->
                if (id.startsWith("default_account_")) {
                    null // 默认账户查询所有交易
                } else {
                    id // 特定账户只查询该账户
                }
            }
            android.util.Log.e("LEDGER_DEBUG", "账户ID: ${_uiState.value.selectedAccountId}")
            android.util.Log.e("LEDGER_DEBUG", "查询账户ID: $accountIdForQuery")
            android.util.Log.e("LEDGER_DEBUG", "分页: page=$currentPage, size=$pageSize")
            
            getPaginatedTransactionsUseCase(
                currentPage, pageSize, accountIdForQuery, start, end
            ).collect { result ->
                android.util.Log.e("LEDGER_DEBUG", "查询结果类型: ${result::class.simpleName}")
                handleTransactionResult(result, loadMore)
                if (loadMore) isLoadingMore = false
            }
        }
    }
    
    fun loadMoreTransactions() = loadTransactions(true)
    
    private fun handleTransactionResult(result: BaseResult<GetPaginatedTransactionsUseCase.PaginatedResult>, loadMore: Boolean) {
        when (result) {
            is BaseResult.Success -> {
                val data = result.data
                android.util.Log.e("LEDGER_DEBUG", "查询成功: 返回 ${data.transactions.size} 条记录, 总数: ${data.totalCount}")
                android.util.Log.e("LEDGER_DEBUG", "hasMore: ${data.hasMore}")
                if (data.transactions.isNotEmpty()) {
                    val first = data.transactions.first()
                    android.util.Log.e("LEDGER_DEBUG", "第一条记录: ID=${first.id}, Amount=${first.amountCents}, Date=${first.createdAt}")
                    android.util.Log.e("LEDGER_DEBUG", "第一条时间: ${java.util.Date(first.createdAt.toEpochMilliseconds())}")
                } else {
                    android.util.Log.e("LEDGER_DEBUG", "警告：查询返回空列表！")
                }
                _uiState.update {
                    it.copy(
                        transactions = if (loadMore) it.transactions + data.transactions else data.transactions,
                        isLoading = false,
                        isLoadingMore = false,
                        hasMoreData = data.hasMore
                    )
                }
                hasMoreData = data.hasMore
                cacheManager.updateRecentTransactionsCache(data.transactions)
            }
            is BaseResult.Error -> {
                android.util.Log.e("LEDGER_DEBUG", "查询失败: ${result.exception.message}", result.exception)
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false) }
                if (loadMore) currentPage--
            }
        }
    }
    
    fun addTransaction(
        amountCents: Int, categoryId: String, note: String?, 
        accountId: String? = null, onBudgetAlert: (String, Boolean) -> Unit = { _, _ -> }
    ) = launch {
        val finalAccountId = accountId ?: _uiState.value.selectedAccountId 
            ?: _uiState.value.accounts.firstOrNull()?.id ?: return@launch
        runCatching {
            addTransactionUseCase(amountCents, categoryId, note, finalAccountId)
            checkBudget(categoryId, onBudgetAlert)
            loadMonthlySummary()
            loadTransactions()  // 刷新交易列表
        }
    }
    
    private suspend fun checkBudget(categoryId: String, onAlert: (String, Boolean) -> Unit) = runCatching {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        when (val status = checkBudgetUseCase.checkBudgetStatus(now.year, now.monthNumber, categoryId)) {
            is BudgetStatus.Exceeded -> onAlert("该分类预算已超支！", true)
            is BudgetStatus.Alert -> onAlert("该分类预算即将用完！", false)
            else -> Unit
        }
    }
    
    fun updateTransaction(transaction: Transaction) = launch {
        runCatching { updateTransactionUseCase(transaction); loadMonthlySummary() }
    }
    
    fun deleteTransaction(transactionId: String) = launch {
        runCatching { deleteTransactionUseCase(transactionId); loadMonthlySummary() }
    }
    
    fun copyTransaction(transaction: Transaction) = launch {
        runCatching {
            addTransactionUseCase(
                transaction.amountCents, transaction.categoryId,
                "[复制] ${transaction.note ?: ""}", transaction.accountId
            )
            loadTransactions()
            loadMonthlySummary()
        }
    }
    
    private fun loadMonthlySummary() = launch {
        runCatching {
            val month = _selectedMonth.value
            val (income, expense) = getMonthlyStatsUseCase(month.year, month.monthValue)
            _uiState.update { it.copy(monthlyIncome = income / 100.0, monthlyExpense = expense / 100.0) }
        }
    }
    
    private fun loadAccounts() = launch {
        getAccountsUseCase().collect { accounts ->
            android.util.Log.e("LEDGER_DEBUG", "加载账户列表: ${accounts.size} 个账户")
            
            // 智能选择默认账户
            val selectedAccountId = when {
                accounts.isEmpty() -> {
                    android.util.Log.e("LEDGER_DEBUG", "无账户，selectedAccountId = null")
                    null
                }
                accounts.any { it.isDefault } -> {
                    val defaultAccount = accounts.first { it.isDefault }
                    android.util.Log.e("LEDGER_DEBUG", "找到默认账户: ${defaultAccount.id}")
                    defaultAccount.id
                }
                else -> {
                    // 如果没有默认账户，选择第一个
                    val firstAccount = accounts.first()
                    android.util.Log.e("LEDGER_DEBUG", "无默认账户，选择第一个: ${firstAccount.id}")
                    firstAccount.id
                }
            }
            
            _uiState.update {
                it.copy(accounts = accounts, selectedAccountId = selectedAccountId)
            }
            cacheManager.updateAccountsCache(accounts)
            
            // 如果账户选择变化，重新加载交易
            if (_uiState.value.selectedAccountId != selectedAccountId) {
                android.util.Log.e("LEDGER_DEBUG", "账户选择变化，重新加载交易")
                loadTransactions()
            }
        }
    }
    
    private fun loadCategories() = launch {
        getCategoriesUseCase().collect { categories ->
            _uiState.update { it.copy(categories = categories) }
            cacheManager.updateCategoriesCache(categories)
        }
    }
    
    fun selectAccount(accountId: String?) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
        loadTransactions()
    }
    
    fun refreshTransactions() {
        loadTransactions()
        loadMonthlySummary()
    }
    
    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}

// UI状态 - 核心数据
data class LedgerUiState(
    val transactions: List<Transaction> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String? = null,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true
)
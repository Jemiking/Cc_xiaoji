package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.usecase.*
import com.ccxiaoji.feature.ledger.data.cache.LedgerCacheManager
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
    private val cacheManager: LedgerCacheManager
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
        loadAccounts()
        loadCategories()
        loadTransactions()
        loadMonthlySummary()
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
            if (loadMore) {
                isLoadingMore = true
                currentPage++
            } else {
                currentPage = 0
            }
            _uiState.update { it.copy(isLoading = !loadMore, isLoadingMore = loadMore) }
            
            val (start, end) = getMonthDateRange(_selectedMonth.value)
            getPaginatedTransactionsUseCase(
                currentPage, pageSize, _uiState.value.selectedAccountId, start, end
            ).collect { result ->
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
            _uiState.update {
                it.copy(accounts = accounts, selectedAccountId = accounts.find { it.isDefault }?.id)
            }
            cacheManager.updateAccountsCache(accounts)
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
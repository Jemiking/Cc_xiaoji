package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.usecase.*
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.LedgerUIPreferencesRepository
import com.ccxiaoji.feature.ledger.data.cache.LedgerCacheManager
import com.ccxiaoji.feature.ledger.domain.service.DefaultLedgerInitializationService
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
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val transactionRepository: TransactionRepository,
    private val ledgerUIPreferencesRepository: LedgerUIPreferencesRepository,
    private val cacheManager: LedgerCacheManager,
    private val userApi: UserApi,
    private val defaultLedgerInitService: DefaultLedgerInitializationService
) : ViewModel() {
    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState = _uiState.asStateFlow()
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()
    private var currentPage = 0
    private val pageSize = 10000  // 设置足够大的数值，一次获取所有交易
    private var hasMoreData = true
    private var isLoadingMore = false
    
    init {
        loadAccounts()
        loadCategories()
        loadLedgers()
        loadTransactions()
        loadMonthlySummary()
    }
    
    
    
    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        currentPage = 0  // 总是从第0页开始，一次加载所有数据
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
    
    private fun loadTransactions() {
        viewModelScope.launch {
            currentPage = 0
            _uiState.update { it.copy(isLoading = true) }
            val (start, end) = getMonthDateRange(_selectedMonth.value)
            val accountIdForQuery = _uiState.value.selectedAccountId?.let { id ->
                if (id.startsWith("default_account_")) null else id
            }
            val ledgerIdForQuery = _uiState.value.selectedLedgerId
            val transactionFlow = if (ledgerIdForQuery != null) {
                transactionRepository.getTransactionsPaginatedByLedger(
                    ledgerId = ledgerIdForQuery,
                    offset = currentPage * pageSize,
                    limit = pageSize,
                    accountId = accountIdForQuery,
                    startDate = start,
                    endDate = end
                ).map { result ->
                    when (result) {
                        is BaseResult.Success -> {
                            val (transactions, totalCount) = result.data
                            BaseResult.Success(
                                GetPaginatedTransactionsUseCase.PaginatedResult(
                                    transactions = transactions,
                                    hasMore = (currentPage + 1) * pageSize < totalCount,
                                    totalCount = totalCount
                                )
                            )
                        }
                        is BaseResult.Error -> BaseResult.Error(result.exception)
                    }
                }
            } else {
                getPaginatedTransactionsUseCase(currentPage, pageSize, accountIdForQuery, start, end)
            }
            transactionFlow.collect { result -> handleTransactionResult(result) }
        }
    }
    
    private fun handleTransactionResult(result: BaseResult<GetPaginatedTransactionsUseCase.PaginatedResult>) {
        when (result) {
            is BaseResult.Success -> {
                val data = result.data
                _uiState.update {
                    it.copy(
                        transactions = data.transactions,  // 直接设置所有交易
                        isLoading = false,
                        isLoadingMore = false,
                        hasMoreData = false  // 一次加载所有，不需要更多
                    )
                }
                cacheManager.updateRecentTransactionsCache(data.transactions)
            }
            is BaseResult.Error -> {
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false) }
            }
        }
    }
    
    fun addTransaction(
        amountCents: Int, categoryId: String, note: String?, 
        accountId: String? = null, onBudgetAlert: (String, Boolean) -> Unit = { _, _ -> }
    ) = launch {
        val finalAccountId = accountId ?: _uiState.value.selectedAccountId 
            ?: _uiState.value.accounts.firstOrNull()?.id ?: return@launch
        
        // 获取当前选中的账本 ID
        val currentLedgerId = _uiState.value.currentLedger?.id
        
        runCatching {
            addTransactionUseCase(
                amountCents = amountCents, 
                categoryId = categoryId, 
                note = note, 
                accountId = finalAccountId,
                ledgerId = currentLedgerId  // 传递当前账本 ID
            )
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
        // 获取当前选中的账本 ID
        val currentLedgerId = _uiState.value.currentLedger?.id
        
        runCatching {
            addTransactionUseCase(
                amountCents = transaction.amountCents, 
                categoryId = transaction.categoryId,
                note = "[复制] ${transaction.note ?: ""}", 
                accountId = transaction.accountId,
                ledgerId = currentLedgerId  // 传递当前记账簿ID
            )
            loadTransactions()
            loadMonthlySummary()
            
        }
    }
    
    private fun loadMonthlySummary() = launch {
        runCatching {
            val month = _selectedMonth.value
            val currentLedger = _uiState.value.currentLedger
            
            if (currentLedger != null) {
                // 使用账本过滤的月度统计
                val result = transactionRepository.getMonthlyIncomesAndExpensesByLedger(
                    ledgerId = currentLedger.id,
                    year = month.year,
                    month = month.monthValue
                )
                when (result) {
                    is BaseResult.Success -> {
                        val (income, expense) = result.data
                        _uiState.update { 
                            it.copy(
                                monthlyIncome = income / 100.0, 
                                monthlyExpense = expense / 100.0
                            ) 
                        }
                    }
                    is BaseResult.Error -> {
                        _uiState.update { it.copy(monthlyIncome = 0.0, monthlyExpense = 0.0) }
                    }
                }
            } else {
                // 使用全局月度统计（兼容原有逻辑）
                val (income, expense) = getMonthlyStatsUseCase(month.year, month.monthValue)
                _uiState.update { it.copy(monthlyIncome = income / 100.0, monthlyExpense = expense / 100.0) }
            }
        }
    }
    
    private fun loadAccounts() = launch {
        getAccountsUseCase().collect { accounts ->
            val previousSelectedId = _uiState.value.selectedAccountId
            val selectedAccountId = when {
                accounts.isEmpty() -> null
                previousSelectedId != null && accounts.any { it.id == previousSelectedId } -> previousSelectedId
                accounts.any { it.isDefault } -> accounts.first { it.isDefault }.id
                else -> accounts.first().id
            }
            _uiState.update { it.copy(accounts = accounts, selectedAccountId = selectedAccountId) }
            cacheManager.updateAccountsCache(accounts)
            if (previousSelectedId != selectedAccountId) {
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
    
    // ==================== 账本管理方法 ====================
    
    /**
     * 加载账本列表
     */
    private fun loadLedgers() = launch {
        try {
            _uiState.update { it.copy(isLedgerLoading = true) }
            
            val userId = userApi.getCurrentUserId()
            
            // 使用专门的默认记账簿初始化服务
            val initResult = defaultLedgerInitService.initializeDefaultLedger(userId)
            when (initResult) {
                is com.ccxiaoji.feature.ledger.domain.service.DefaultLedgerInitResult.Success -> { /* no-op */ }
                is com.ccxiaoji.feature.ledger.domain.service.DefaultLedgerInitResult.Failure -> { /* no-op */ }
            }
            
            // 收集账本列表 Flow
            manageLedgerUseCase.getUserLedgers(userId).collect { ledgers ->
                // 获取用户偏好的账本选择
                ledgerUIPreferencesRepository.getUIPreferences().collect { preferences ->
                    val preferredLedgerId = preferences.selectedLedgerId
                    val preferredLedger = ledgers.find { ledger -> ledger.id == preferredLedgerId }
                    val defaultLedger = ledgers.find { ledger -> ledger.isDefault }
                    
                    // 选择逻辑：偏好账本 -> 默认账本 -> 第一个账本
                    val selectedLedger = preferredLedger ?: defaultLedger ?: ledgers.firstOrNull()
                    
                    _uiState.update { 
                        it.copy(
                            ledgers = ledgers,
                            currentLedger = selectedLedger,
                            selectedLedgerId = selectedLedger?.id,
                            isLedgerLoading = false
                        )
                    }
                    // 如果实际选择的账本与偏好不同，更新偏好设置
                    if (selectedLedger?.id != preferredLedgerId) {
                        selectedLedger?.id?.let { ledgerId -> 
                            launch {
                                ledgerUIPreferencesRepository.updateSelectedLedgerId(ledgerId)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLedgerLoading = false) }
        }
    }
    
    /**
     * 选择账本
     */
    fun selectLedger(ledgerId: String) = launch {
        try {
            val currentLedgers = _uiState.value.ledgers
            val selectedLedger = currentLedgers.find { it.id == ledgerId }
            
            if (selectedLedger != null) {
                _uiState.update { 
                    it.copy(
                        currentLedger = selectedLedger,
                        selectedLedgerId = ledgerId
                    )
                }
                
                // 保存用户的账本选择到偏好设置
                ledgerUIPreferencesRepository.updateSelectedLedgerId(ledgerId)
                
                // 重新加载基于账本的数据
                loadTransactions()
                loadMonthlySummary()
            } else {
                
            }
        } catch (e: Exception) {
            
        }
    }
    
    /**
     * 获取当前账本
     */
    fun getCurrentLedger(): Ledger? {
        return _uiState.value.currentLedger
    }
    
    /**
     * 刷新账本列表
     */
    fun refreshLedgers() {
        loadLedgers()
    }
    
    // ==================== 账户管理方法 ====================
    
    fun selectAccount(accountId: String?) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
        loadTransactions()  // 重新加载所有交易
    }
    
    fun refreshTransactions() {
        loadTransactions()  // 刷新所有交易
        loadMonthlySummary()
    }
    
    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}

// UI状态 - 核心数据
data class LedgerUiState(
    // 交易相关数据
    val transactions: List<Transaction> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    
    // 账户相关数据
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String? = null,
    
    // 分类相关数据
    val categories: List<Category> = emptyList(),
    
    // 账本相关数据
    val ledgers: List<Ledger> = emptyList(),
    val currentLedger: Ledger? = null,
    val selectedLedgerId: String? = null,
    val isLedgerLoading: Boolean = false,
    
    // 加载状态
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true
)

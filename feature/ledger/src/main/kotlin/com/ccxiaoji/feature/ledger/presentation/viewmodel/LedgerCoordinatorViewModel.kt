package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.usecase.*
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.YearMonth
import javax.inject.Inject

/**
 * 协调各个子ViewModel的主ViewModel
 * 负责管理整体状态和协调不同功能模块
 */
@HiltViewModel
class LedgerCoordinatorViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase,
    private val checkBudgetUseCase: CheckBudgetUseCase
) : ViewModel() {
    
    private val _coordinatorState = MutableStateFlow(LedgerCoordinatorState())
    val coordinatorState: StateFlow<LedgerCoordinatorState> = _coordinatorState.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    /**
     * 加载初始数据
     */
    private fun loadInitialData() {
        loadAccounts()
        loadCategories()
        loadTransactions()
        loadMonthlySummary()
    }
    
    /**
     * 选择月份
     */
    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        refreshMonthlyData()
    }
    
    /**
     * 刷新月度数据
     */
    private fun refreshMonthlyData() {
        loadTransactions()
        loadMonthlySummary()
    }
    
    /**
     * 加载交易数据
     */
    private fun loadTransactions() {
        viewModelScope.launch {
            val accountId = _coordinatorState.value.selectedAccountId
            val transactionsFlow = if (accountId != null) {
                getTransactionsUseCase.getByAccount(accountId)
            } else {
                getTransactionsUseCase()
            }
            
            transactionsFlow.collect { allTransactions ->
                // 按选中月份过滤交易
                val filteredByMonth = filterTransactionsByMonth(allTransactions, _selectedMonth.value)
                _coordinatorState.update { 
                    it.copy(monthlyTransactions = filteredByMonth)
                }
            }
        }
    }
    
    /**
     * 加载月度统计
     */
    private fun loadMonthlySummary() {
        viewModelScope.launch {
            try {
                val selectedMonth = _selectedMonth.value
                val (income, expense) = getMonthlyStatsUseCase(
                    selectedMonth.year, 
                    selectedMonth.monthValue
                )
                
                _coordinatorState.update { 
                    it.copy(
                        monthlyIncome = income / 100.0,
                        monthlyExpense = expense / 100.0
                    )
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    
    /**
     * 加载账户列表
     */
    private fun loadAccounts() {
        viewModelScope.launch {
            getAccountsUseCase().collect { accounts ->
                _coordinatorState.update { 
                    it.copy(
                        accounts = accounts,
                        selectedAccountId = accounts.find { acc -> acc.isDefault }?.id
                    )
                }
            }
        }
    }
    
    /**
     * 加载分类列表
     */
    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                _coordinatorState.update { 
                    it.copy(categories = categories)
                }
            }
        }
    }
    
    /**
     * 设置选中的账户
     */
    fun selectAccount(accountId: String?) {
        _coordinatorState.update { it.copy(selectedAccountId = accountId) }
        loadTransactions()
    }
    
    /**
     * 检查预算
     */
    suspend fun checkBudgetStatus(categoryId: String): BudgetStatus {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return checkBudgetUseCase.checkBudgetStatus(now.year, now.monthNumber, categoryId)
    }
    
    /**
     * 刷新所有数据
     */
    fun refreshAll() {
        loadInitialData()
    }
    
    /**
     * 按月份过滤交易
     */
    private fun filterTransactionsByMonth(
        transactions: List<Transaction>, 
        yearMonth: YearMonth
    ): List<Transaction> {
        return transactions.filter { transaction ->
            val transactionDate = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
            val transactionMonth = YearMonth.of(transactionDate.year, transactionDate.monthNumber)
            transactionMonth == yearMonth
        }
    }
}

/**
 * 协调器状态
 */
data class LedgerCoordinatorState(
    val monthlyTransactions: List<Transaction> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String? = null,
    val categories: List<Category> = emptyList()
)
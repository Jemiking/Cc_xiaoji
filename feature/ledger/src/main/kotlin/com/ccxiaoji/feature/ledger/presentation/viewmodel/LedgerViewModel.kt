package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.YearMonth
import javax.inject.Inject

/**
 * 精简后的LedgerViewModel - 核心交易管理(200行以内)
 * 功能委托：SelectionViewModel(选择)、SearchViewModel(搜索)
 * DialogViewModel(对话框)、FilterViewModel(过滤)
 */
@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase,
    private val checkBudgetUseCase: CheckBudgetUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        loadAccounts()
        loadCategories()
        loadTransactions()
        loadMonthlySummary()
    }
    
    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        loadTransactions()
        loadMonthlySummary()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            val accountId = _uiState.value.selectedAccountId
            val transactionsFlow = if (accountId != null) {
                getTransactionsUseCase.getByAccount(accountId)
            } else {
                getTransactionsUseCase()
            }
            
            transactionsFlow.collect { allTransactions ->
                val filteredByMonth = filterTransactionsByMonth(allTransactions, _selectedMonth.value)
                _uiState.update { it.copy(transactions = filteredByMonth) }
            }
        }
    }
    
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
    
    fun addTransaction(
        amountCents: Int, 
        categoryId: String, 
        note: String?, 
        accountId: String? = null,
        onBudgetAlert: (String, Boolean) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val finalAccountId = accountId ?: _uiState.value.selectedAccountId 
                ?: _uiState.value.accounts.firstOrNull()?.id ?: return@launch
            
            try {
                addTransactionUseCase(
                    amountCents = amountCents,
                    categoryId = categoryId,
                    note = note,
                    accountId = finalAccountId
                )
                checkBudgetAfterTransaction(categoryId, onBudgetAlert)
                loadMonthlySummary()
            } catch (e: Exception) {
                // 错误处理委托给基类或其他机制
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun checkBudgetAfterTransaction(
        categoryId: String,
        onBudgetAlert: (String, Boolean) -> Unit
    ) {
        try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val status = checkBudgetUseCase.checkBudgetStatus(now.year, now.monthNumber, categoryId)
            
            when (status) {
                is BudgetStatus.Exceeded -> {
                    onBudgetAlert("该分类预算已超支！", true)
                }
                is BudgetStatus.Alert -> {
                    onBudgetAlert("该分类预算即将用完！", false)
                }
                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                updateTransactionUseCase(transaction)
                loadMonthlySummary()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transactionId)
                loadMonthlySummary()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun copyTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                addTransactionUseCase(
                    accountId = transaction.accountId,
                    amountCents = transaction.amountCents,
                    categoryId = transaction.categoryId,
                    note = "[复制] ${transaction.note ?: ""}"
                )
                loadTransactions()
                loadMonthlySummary()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadMonthlySummary() {
        viewModelScope.launch {
            try {
                val selectedMonth = _selectedMonth.value
                val (income, expense) = getMonthlyStatsUseCase(
                    selectedMonth.year,
                    selectedMonth.monthValue
                )
                
                _uiState.update {
                    it.copy(
                        monthlyIncome = income / 100.0,
                        monthlyExpense = expense / 100.0
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            getAccountsUseCase().collect { accounts ->
                _uiState.update {
                    it.copy(
                        accounts = accounts,
                        selectedAccountId = accounts.find { acc -> acc.isDefault }?.id
                    )
                }
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }
    
    fun selectAccount(accountId: String?) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
        loadTransactions()
    }
}

// UI状态 - 核心数据
data class LedgerUiState(
    val transactions: List<Transaction> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String? = null,
    val categories: List<Category> = emptyList()
)
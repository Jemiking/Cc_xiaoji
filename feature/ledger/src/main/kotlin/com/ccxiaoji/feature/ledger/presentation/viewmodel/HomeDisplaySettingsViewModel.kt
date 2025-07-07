package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.HomeDisplaySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeDisplaySettingsUiState(
    val showTodayExpense: Boolean = true,
    val showTodayIncome: Boolean = true,
    val showMonthExpense: Boolean = true,
    val showMonthIncome: Boolean = true,
    val showAccountBalance: Boolean = true,
    val showBudgetProgress: Boolean = true,
    val showRecentTransactions: Boolean = true,
    val recentTransactionCount: Int = 5,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeDisplaySettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeDisplaySettingsUiState())
    val uiState: StateFlow<HomeDisplaySettingsUiState> = _uiState.asStateFlow()
    
    companion object {
        private val SHOW_TODAY_EXPENSE_KEY = booleanPreferencesKey("ledger_show_today_expense")
        private val SHOW_TODAY_INCOME_KEY = booleanPreferencesKey("ledger_show_today_income")
        private val SHOW_MONTH_EXPENSE_KEY = booleanPreferencesKey("ledger_show_month_expense")
        private val SHOW_MONTH_INCOME_KEY = booleanPreferencesKey("ledger_show_month_income")
        private val SHOW_ACCOUNT_BALANCE_KEY = booleanPreferencesKey("ledger_show_account_balance")
        private val SHOW_BUDGET_PROGRESS_KEY = booleanPreferencesKey("ledger_show_budget_progress")
        private val SHOW_RECENT_TRANSACTIONS_KEY = booleanPreferencesKey("ledger_show_recent_transactions")
        private val RECENT_TRANSACTION_COUNT_KEY = intPreferencesKey("ledger_recent_transaction_count")
    }
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                HomeDisplaySettingsUiState(
                    showTodayExpense = preferences[SHOW_TODAY_EXPENSE_KEY] ?: true,
                    showTodayIncome = preferences[SHOW_TODAY_INCOME_KEY] ?: true,
                    showMonthExpense = preferences[SHOW_MONTH_EXPENSE_KEY] ?: true,
                    showMonthIncome = preferences[SHOW_MONTH_INCOME_KEY] ?: true,
                    showAccountBalance = preferences[SHOW_ACCOUNT_BALANCE_KEY] ?: true,
                    showBudgetProgress = preferences[SHOW_BUDGET_PROGRESS_KEY] ?: true,
                    showRecentTransactions = preferences[SHOW_RECENT_TRANSACTIONS_KEY] ?: true,
                    recentTransactionCount = preferences[RECENT_TRANSACTION_COUNT_KEY] ?: 5
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun updateShowTodayExpense(show: Boolean) {
        _uiState.update { it.copy(showTodayExpense = show) }
    }
    
    fun updateShowTodayIncome(show: Boolean) {
        _uiState.update { it.copy(showTodayIncome = show) }
    }
    
    fun updateShowMonthExpense(show: Boolean) {
        _uiState.update { it.copy(showMonthExpense = show) }
    }
    
    fun updateShowMonthIncome(show: Boolean) {
        _uiState.update { it.copy(showMonthIncome = show) }
    }
    
    fun updateShowAccountBalance(show: Boolean) {
        _uiState.update { it.copy(showAccountBalance = show) }
    }
    
    fun updateShowBudgetProgress(show: Boolean) {
        _uiState.update { it.copy(showBudgetProgress = show) }
    }
    
    fun updateShowRecentTransactions(show: Boolean) {
        _uiState.update { it.copy(showRecentTransactions = show) }
    }
    
    fun increaseRecentTransactionCount() {
        val currentCount = _uiState.value.recentTransactionCount
        if (currentCount < 10) {
            _uiState.update { it.copy(recentTransactionCount = currentCount + 1) }
        }
    }
    
    fun decreaseRecentTransactionCount() {
        val currentCount = _uiState.value.recentTransactionCount
        if (currentCount > 1) {
            _uiState.update { it.copy(recentTransactionCount = currentCount - 1) }
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            dataStore.edit { preferences ->
                preferences[SHOW_TODAY_EXPENSE_KEY] = state.showTodayExpense
                preferences[SHOW_TODAY_INCOME_KEY] = state.showTodayIncome
                preferences[SHOW_MONTH_EXPENSE_KEY] = state.showMonthExpense
                preferences[SHOW_MONTH_INCOME_KEY] = state.showMonthIncome
                preferences[SHOW_ACCOUNT_BALANCE_KEY] = state.showAccountBalance
                preferences[SHOW_BUDGET_PROGRESS_KEY] = state.showBudgetProgress
                preferences[SHOW_RECENT_TRANSACTIONS_KEY] = state.showRecentTransactions
                preferences[RECENT_TRANSACTION_COUNT_KEY] = state.recentTransactionCount
            }
        }
    }
}
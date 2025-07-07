package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.FilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AdvancedFilterViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val filterRepository: FilterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdvancedFilterUiState())
    val uiState: StateFlow<AdvancedFilterUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
        loadCurrentFilter()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 加载账户和分类
            val accounts = accountRepository.getAccounts().first()
            val categories = categoryRepository.getCategories().first()
            
            // 创建预设筛选器
            val presets = createFilterPresets()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    accounts = accounts,
                    categories = categories,
                    filterPresets = presets
                )
            }
        }
    }
    
    private fun loadCurrentFilter() {
        viewModelScope.launch {
            filterRepository.currentFilter.collect { filter ->
                _uiState.update { state ->
                    state.copy(
                        keyword = filter.keyword ?: "",
                        transactionType = filter.transactionType,
                        minAmountText = filter.minAmount?.toString() ?: "",
                        maxAmountText = filter.maxAmount?.toString() ?: "",
                        selectedAccountId = filter.accountId,
                        selectedCategoryIds = filter.categoryIds,
                        dateRange = filter.dateRange
                    )
                }
            }
        }
    }
    
    private fun createFilterPresets(): List<FilterPreset> {
        val today = LocalDate.now()
        return listOf(
            FilterPreset(
                id = "today",
                name = "今日",
                icon = "today",
                filter = TransactionFilter(
                    dateRange = DateRange(today, today)
                )
            ),
            FilterPreset(
                id = "week",
                name = "本周",
                icon = "week",
                filter = TransactionFilter(
                    dateRange = DateRange(
                        today.minusDays(today.dayOfWeek.value.toLong() - 1),
                        today.plusDays(7 - today.dayOfWeek.value.toLong())
                    )
                )
            ),
            FilterPreset(
                id = "month",
                name = "本月",
                icon = "month",
                filter = TransactionFilter(
                    dateRange = DateRange(
                        today.withDayOfMonth(1),
                        today.withDayOfMonth(today.lengthOfMonth())
                    )
                )
            ),
            FilterPreset(
                id = "income_only",
                name = "仅收入",
                icon = "income",
                filter = TransactionFilter(
                    transactionType = TransactionType.INCOME
                )
            ),
            FilterPreset(
                id = "expense_only",
                name = "仅支出",
                icon = "expense",
                filter = TransactionFilter(
                    transactionType = TransactionType.EXPENSE
                )
            ),
            FilterPreset(
                id = "large_amount",
                name = "大额交易",
                icon = "large",
                filter = TransactionFilter(
                    minAmount = 1000.0
                )
            )
        )
    }
    
    fun updateKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
    }
    
    fun updateTransactionType(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type) }
    }
    
    fun updateMinAmount(amount: String) {
        _uiState.update { it.copy(minAmountText = amount) }
    }
    
    fun updateMaxAmount(amount: String) {
        _uiState.update { it.copy(maxAmountText = amount) }
    }
    
    fun selectAccount(accountId: String?) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }
    
    fun toggleCategory(categoryId: String) {
        _uiState.update { state ->
            val newSet = if (state.selectedCategoryIds.contains(categoryId)) {
                state.selectedCategoryIds - categoryId
            } else {
                state.selectedCategoryIds + categoryId
            }
            state.copy(selectedCategoryIds = newSet)
        }
    }
    
    fun clearCategorySelection() {
        _uiState.update { it.copy(selectedCategoryIds = emptySet()) }
    }
    
    fun updateDateRange(dateRange: DateRange?) {
        _uiState.update { it.copy(dateRange = dateRange) }
    }
    
    fun applyFilter() {
        val state = _uiState.value
        val filter = TransactionFilter(
            categoryIds = state.selectedCategoryIds,
            dateRange = state.dateRange,
            minAmount = state.minAmountText.toDoubleOrNull(),
            maxAmount = state.maxAmountText.toDoubleOrNull(),
            transactionType = state.transactionType,
            accountId = state.selectedAccountId,
            keyword = state.keyword.ifBlank { null }
        )
        
        viewModelScope.launch {
            filterRepository.updateFilter(filter)
            _uiState.update { it.copy(isFilterApplied = true) }
        }
    }
    
    fun applyPreset(preset: FilterPreset) {
        viewModelScope.launch {
            filterRepository.updateFilter(preset.filter)
            _uiState.update { it.copy(isFilterApplied = true) }
        }
    }
    
    fun clearFilter() {
        viewModelScope.launch {
            filterRepository.clearFilter()
            _uiState.update { it.copy(isFilterApplied = true) }
        }
    }
}

data class AdvancedFilterUiState(
    val isLoading: Boolean = false,
    val keyword: String = "",
    val transactionType: TransactionType = TransactionType.ALL,
    val minAmountText: String = "",
    val maxAmountText: String = "",
    val selectedAccountId: String? = null,
    val selectedCategoryIds: Set<String> = emptySet(),
    val dateRange: DateRange? = null,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val filterPresets: List<FilterPreset> = emptyList(),
    val isFilterApplied: Boolean = false
)
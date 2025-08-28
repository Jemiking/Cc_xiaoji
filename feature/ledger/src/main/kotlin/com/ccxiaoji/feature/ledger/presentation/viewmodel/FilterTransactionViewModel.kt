package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate

data class FilterTransactionUiState(
    val transactionType: TransactionType = TransactionType.ALL,
    val selectedCategoryIds: Set<String> = emptySet(),
    val minAmount: String = "",
    val maxAmount: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val allCategories: List<Category> = emptyList(),
    val filteredCategories: List<Category> = emptyList()
)

@HiltViewModel
class FilterTransactionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FilterTransactionUiState())
    val uiState: StateFlow<FilterTransactionUiState> = _uiState.asStateFlow()
    
    init {
        // 获取当前的accountId参数
        val accountId = savedStateHandle.get<String>("accountId")
        
        // 从LedgerScreen传递过来的当前筛选条件
        // TODO: 需要在导航时传递当前筛选条件
        val currentFilter = TransactionFilter(accountId = accountId)
        
        _uiState.update { state ->
            state.copy(
                transactionType = currentFilter.transactionType,
                selectedCategoryIds = currentFilter.categoryIds,
                minAmount = currentFilter.minAmount?.toString() ?: "",
                maxAmount = currentFilter.maxAmount?.toString() ?: "",
                startDate = currentFilter.dateRange?.startDate,
                endDate = currentFilter.dateRange?.endDate
            )
        }
        
        // 加载分类
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { categories ->
                _uiState.update { state ->
                    state.copy(
                        allCategories = categories,
                        filteredCategories = filterCategoriesByType(categories, state.transactionType)
                    )
                }
            }
        }
    }
    
    private fun filterCategoriesByType(categories: List<Category>, type: TransactionType): List<Category> {
        return when (type) {
            TransactionType.INCOME -> categories.filter { it.type == Category.Type.INCOME }
            TransactionType.EXPENSE -> categories.filter { it.type == Category.Type.EXPENSE }
            TransactionType.TRANSFER -> emptyList() // 转账不需要分类
            TransactionType.ALL -> categories
        }
    }
    
    fun updateTransactionType(type: TransactionType) {
        _uiState.update { state ->
            state.copy(
                transactionType = type,
                filteredCategories = filterCategoriesByType(state.allCategories, type),
                // 清除不匹配的分类选择
                selectedCategoryIds = state.selectedCategoryIds.filter { categoryId ->
                    val category = state.allCategories.find { it.id == categoryId }
                    when (type) {
                        TransactionType.ALL -> true
                        TransactionType.INCOME -> category?.type == Category.Type.INCOME
                        TransactionType.EXPENSE -> category?.type == Category.Type.EXPENSE
                        TransactionType.TRANSFER -> false // 转账不使用分类
                    }
                }.toSet()
            )
        }
    }
    
    fun toggleCategory(categoryId: String) {
        _uiState.update { state ->
            val newSelectedIds = if (state.selectedCategoryIds.contains(categoryId)) {
                state.selectedCategoryIds - categoryId
            } else {
                state.selectedCategoryIds + categoryId
            }
            state.copy(selectedCategoryIds = newSelectedIds)
        }
    }
    
    fun updateMinAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(minAmount = filtered) }
    }
    
    fun updateMaxAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(maxAmount = filtered) }
    }
    
    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }
    
    fun updateEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }
    
    fun setToday() {
        val today = LocalDate.now()
        _uiState.update { it.copy(startDate = today, endDate = today) }
    }
    
    fun setThisWeek() {
        val now = LocalDate.now()
        val startOfWeek = now.with(java.time.DayOfWeek.MONDAY)
        _uiState.update { it.copy(startDate = startOfWeek, endDate = now) }
    }
    
    fun setThisMonth() {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1)
        _uiState.update { it.copy(startDate = startOfMonth, endDate = now) }
    }
    
    fun setThisYear() {
        val now = LocalDate.now()
        val startOfYear = now.withDayOfYear(1)
        _uiState.update { it.copy(startDate = startOfYear, endDate = now) }
    }
    
    fun clearFilter() {
        // 设置清除筛选的结果
        savedStateHandle["filterCleared"] = true
        _uiState.update {
            FilterTransactionUiState(
                allCategories = it.allCategories,
                filteredCategories = it.allCategories
            )
        }
    }
    
    fun applyFilter() {
        val state = _uiState.value
        val filter = TransactionFilter(
            transactionType = state.transactionType,
            categoryIds = state.selectedCategoryIds,
            minAmount = state.minAmount.toDoubleOrNull(),
            maxAmount = state.maxAmount.toDoubleOrNull(),
            dateRange = if (state.startDate != null && state.endDate != null) {
                DateRange(state.startDate, state.endDate)
            } else null,
            accountId = savedStateHandle.get<String>("accountId")
        )
        
        // 设置筛选结果
        savedStateHandle["filterResult"] = filter
    }
}
package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.core.database.dao.BudgetWithSpent
import com.ccxiaoji.core.database.dao.CategoryDao
import com.ccxiaoji.core.database.entity.BudgetEntity
import com.ccxiaoji.core.database.entity.CategoryEntity
import com.ccxiaoji.app.data.repository.BudgetRepository
import com.ccxiaoji.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetUiState(
    val budgets: List<BudgetWithSpent> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val editingBudget: BudgetWithSpent? = null,
    val totalBudget: BudgetWithSpent? = null
)

data class BudgetAlert(
    val categoryName: String?,
    val usagePercentage: Float,
    val isExceeded: Boolean
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val userRepository: UserRepository,
    private val categoryDao: CategoryDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val currentUserId = userRepository.getCurrentUserId()

    init {
        loadBudgets()
        loadCategories()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                budgetRepository.getBudgetsWithSpent(
                    userId = currentUserId,
                    year = _uiState.value.selectedYear,
                    month = _uiState.value.selectedMonth
                ).collect { budgets ->
                    val totalBudget = budgets.find { it.categoryId == null }
                    val categoryBudgets = budgets.filter { it.categoryId != null }
                    
                    _uiState.update {
                        it.copy(
                            budgets = categoryBudgets,
                            totalBudget = totalBudget,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryDao.getExpenseCategories(currentUserId).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun changeMonth(year: Int, month: Int) {
        _uiState.update {
            it.copy(
                selectedYear = year,
                selectedMonth = month
            )
        }
        loadBudgets()
    }

    fun showAddBudgetDialog(categoryId: String? = null) {
        val editingBudget = if (categoryId != null) {
            _uiState.value.budgets.find { it.categoryId == categoryId }
        } else {
            _uiState.value.totalBudget
        }
        
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                editingBudget = editingBudget
            )
        }
    }

    fun hideAddEditDialog() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                editingBudget = null
            )
        }
    }

    fun saveBudget(
        budgetAmountCents: Int,
        categoryId: String? = null,
        alertThreshold: Float = 0.8f,
        note: String? = null
    ) {
        viewModelScope.launch {
            try {
                budgetRepository.upsertBudget(
                    userId = currentUserId,
                    year = _uiState.value.selectedYear,
                    month = _uiState.value.selectedMonth,
                    budgetAmountCents = budgetAmountCents,
                    categoryId = categoryId,
                    alertThreshold = alertThreshold,
                    note = note
                )
                hideAddEditDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(budgetId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun checkBudgetAlerts(): Flow<List<BudgetAlert>> = flow {
        val alerts = mutableListOf<BudgetAlert>()
        
        // 检查总预算
        _uiState.value.totalBudget?.let { totalBudget ->
            val usagePercentage = if (totalBudget.budgetAmountCents > 0) {
                (totalBudget.spentAmountCents.toFloat() / totalBudget.budgetAmountCents.toFloat()) * 100f
            } else {
                0f
            }
            
            if (usagePercentage >= totalBudget.alertThreshold * 100) {
                alerts.add(
                    BudgetAlert(
                        categoryName = null,
                        usagePercentage = usagePercentage,
                        isExceeded = usagePercentage > 100f
                    )
                )
            }
        }
        
        // 检查分类预算
        _uiState.value.budgets.forEach { budget ->
            val usagePercentage = if (budget.budgetAmountCents > 0) {
                (budget.spentAmountCents.toFloat() / budget.budgetAmountCents.toFloat()) * 100f
            } else {
                0f
            }
            
            if (usagePercentage >= budget.alertThreshold * 100) {
                val category = _uiState.value.categories.find { it.id == budget.categoryId }
                alerts.add(
                    BudgetAlert(
                        categoryName = category?.name,
                        usagePercentage = usagePercentage,
                        isExceeded = usagePercentage > 100f
                    )
                )
            }
        }
        
        emit(alerts)
    }

    fun getBudgetSummary(): BudgetSummary {
        val totalBudget = _uiState.value.totalBudget
        val totalBudgetAmount = totalBudget?.budgetAmountCents ?: 0
        val totalSpentAmount = totalBudget?.spentAmountCents ?: 0
        val totalRemainingAmount = totalBudgetAmount - totalSpentAmount
        val totalUsagePercentage = if (totalBudgetAmount > 0) {
            (totalSpentAmount.toFloat() / totalBudgetAmount.toFloat()) * 100f
        } else {
            0f
        }
        
        return BudgetSummary(
            totalBudgetAmount = totalBudgetAmount,
            totalSpentAmount = totalSpentAmount,
            totalRemainingAmount = totalRemainingAmount,
            totalUsagePercentage = totalUsagePercentage,
            categoryCount = _uiState.value.budgets.size
        )
    }
}

data class BudgetSummary(
    val totalBudgetAmount: Int,
    val totalSpentAmount: Int,
    val totalRemainingAmount: Int,
    val totalUsagePercentage: Float,
    val categoryCount: Int
)
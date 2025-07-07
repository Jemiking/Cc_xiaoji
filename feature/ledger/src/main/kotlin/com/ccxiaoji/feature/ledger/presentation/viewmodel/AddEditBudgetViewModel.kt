package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.domain.model.Budget
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AddEditBudgetUiState(
    val isEditMode: Boolean = false,
    val editingBudgetId: String? = null,
    val selectedCategoryId: String? = null,
    val selectedCategory: CategoryEntity? = null,
    val amountText: String = "",
    val alertThreshold: Float = 0.8f,
    val note: String = "",
    val isLoading: Boolean = false,
    val amountError: String? = null,
    val canSave: Boolean = false
)

@HiltViewModel
class AddEditBudgetViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository,
    private val categoryDao: CategoryDao,
    private val userApi: UserApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEditBudgetUiState())
    val uiState: StateFlow<AddEditBudgetUiState> = _uiState.asStateFlow()
    
    private val currentUserId = userApi.getCurrentUserId()
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    
    val expenseCategories: StateFlow<List<CategoryEntity>> = categoryDao
        .getExpenseCategories(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun init(categoryId: String?) {
        viewModelScope.launch {
            // 如果传入了categoryId，说明是编辑模式或预选分类
            if (categoryId != null) {
                loadBudgetForCategory(categoryId)
            }
        }
    }
    
    private suspend fun loadBudgetForCategory(categoryId: String) {
        try {
            // 查找该分类是否已有预算
            val budgets = budgetRepository.getBudgetsWithSpent(currentYear, currentMonth).first()
            val existingBudget = budgets.find { it.categoryId == categoryId }
            
            if (existingBudget != null) {
                // 编辑模式
                val category = categoryDao.getCategoryById(categoryId)
                _uiState.update {
                    it.copy(
                        isEditMode = true,
                        editingBudgetId = existingBudget.id,
                        selectedCategoryId = categoryId,
                        selectedCategory = category,
                        amountText = (existingBudget.budgetAmountCents / 100.0).toString(),
                        alertThreshold = existingBudget.alertThreshold,
                        note = existingBudget.note ?: ""
                    )
                }
            } else {
                // 新建模式，预选分类
                val category = categoryDao.getCategoryById(categoryId)
                _uiState.update {
                    it.copy(
                        selectedCategoryId = categoryId,
                        selectedCategory = category
                    )
                }
            }
        } catch (e: Exception) {
            // 处理错误
        }
        
        updateCanSave()
    }
    
    fun selectTotalBudget() {
        _uiState.update {
            it.copy(
                selectedCategoryId = null,
                selectedCategory = null
            )
        }
        updateCanSave()
    }
    
    fun selectCategory(categoryId: String) {
        viewModelScope.launch {
            val category = categoryDao.getCategoryById(categoryId)
            _uiState.update {
                it.copy(
                    selectedCategoryId = categoryId,
                    selectedCategory = category
                )
            }
            updateCanSave()
        }
    }
    
    fun updateAmount(amount: String) {
        val filteredAmount = amount.filter { it.isDigit() || it == '.' }
        val error = when {
            filteredAmount.isEmpty() -> null
            filteredAmount.toDoubleOrNull() == null -> "请输入有效金额"
            filteredAmount.toDouble() <= 0 -> "金额必须大于0"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                amountText = filteredAmount,
                amountError = error
            )
        }
        updateCanSave()
    }
    
    fun updateAlertThreshold(threshold: Float) {
        _uiState.update {
            it.copy(alertThreshold = threshold)
        }
    }
    
    fun updateNote(note: String) {
        _uiState.update {
            it.copy(note = note)
        }
    }
    
    private fun updateCanSave() {
        _uiState.update {
            it.copy(
                canSave = it.amountText.isNotEmpty() && 
                         it.amountError == null && 
                         it.amountText.toDoubleOrNull() != null &&
                         it.amountText.toDouble() > 0
            )
        }
    }
    
    fun saveBudget(onSuccess: () -> Unit) {
        if (!_uiState.value.canSave) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val state = _uiState.value
                val amountCents = ((state.amountText.toDoubleOrNull() ?: 0.0) * 100).toInt()
                
                if (state.isEditMode && state.editingBudgetId != null) {
                    // 更新现有预算
                    val existingBudget = budgetRepository.getBudgetById(state.editingBudgetId)
                    if (existingBudget != null) {
                        val updatedBudget = existingBudget.copy(
                            budgetAmountCents = amountCents,
                            alertThreshold = state.alertThreshold,
                            note = state.note.ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                        budgetRepository.updateBudget(updatedBudget)
                    }
                } else {
                    // 创建新预算
                    budgetRepository.createBudget(
                        year = currentYear,
                        month = currentMonth,
                        categoryId = state.selectedCategoryId,
                        amountCents = amountCents
                    )
                }
                
                onSuccess()
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
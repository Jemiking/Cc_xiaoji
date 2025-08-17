package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.util.Log
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
    val canSave: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddEditBudgetViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository,
    private val categoryDao: CategoryDao,
    private val userApi: UserApi
) : ViewModel() {
    
    companion object {
        private const val TAG = "AddEditBudgetViewModel"
    }
    
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
        Log.d(TAG, "初始化预算编辑，categoryId: $categoryId")
        viewModelScope.launch {
            try {
                // 如果传入了categoryId，说明是编辑模式或预选分类
                if (categoryId != null) {
                    Log.d(TAG, "开始加载分类预算数据")
                    loadBudgetForCategory(categoryId)
                } else {
                    Log.d(TAG, "新建预算模式")
                }
            } catch (e: Exception) {
                Log.e(TAG, "初始化预算编辑时异常", e)
                _uiState.update { it.copy(errorMessage = "初始化失败: ${e.message}") }
            }
        }
    }
    
    private suspend fun loadBudgetForCategory(categoryId: String) {
        try {
            Log.d(TAG, "加载分类预算数据，分类ID: $categoryId, 年月: $currentYear-$currentMonth")
            
            // 查找该分类是否已有预算
            val budgets = budgetRepository.getBudgetsWithSpent(currentYear, currentMonth).first()
            Log.d(TAG, "获取到${budgets.size}个预算记录")
            
            val existingBudget = budgets.find { it.categoryId == categoryId }
            
            if (existingBudget != null) {
                Log.d(TAG, "找到现有预算，编辑模式 - 预算ID: ${existingBudget.id}, 金额: ${existingBudget.budgetAmountCents}")
                // 编辑模式
                val category = categoryDao.getCategoryById(categoryId)
                Log.d(TAG, "获取分类信息: ${category?.name}")
                
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
                Log.d(TAG, "编辑模式状态更新完成")
            } else {
                Log.d(TAG, "未找到现有预算，新建模式 - 预选分类: $categoryId")
                // 新建模式，预选分类
                val category = categoryDao.getCategoryById(categoryId)
                Log.d(TAG, "获取分类信息: ${category?.name}")
                
                _uiState.update {
                    it.copy(
                        selectedCategoryId = categoryId,
                        selectedCategory = category,
                        errorMessage = null
                    )
                }
                Log.d(TAG, "新建模式状态更新完成")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载分类预算数据时异常", e)
            _uiState.update { it.copy(errorMessage = "加载预算数据失败: ${e.message}") }
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
        Log.d(TAG, "开始保存预算，canSave: ${_uiState.value.canSave}")
        if (!_uiState.value.canSave) {
            Log.w(TAG, "无法保存：canSave为false")
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            Log.d(TAG, "设置保存状态为loading")
            
            try {
                val state = _uiState.value
                val amountCents = ((state.amountText.toDoubleOrNull() ?: 0.0) * 100).toInt()
                Log.d(TAG, "保存预算数据 - 金额: ${state.amountText} -> ${amountCents}分, 编辑模式: ${state.isEditMode}")
                
                if (state.isEditMode && state.editingBudgetId != null) {
                    Log.d(TAG, "更新现有预算，ID: ${state.editingBudgetId}")
                    // 更新现有预算
                    val existingBudget = budgetRepository.getBudgetById(state.editingBudgetId)
                    if (existingBudget != null) {
                        Log.d(TAG, "找到现有预算: ${existingBudget.categoryId}")
                        val updatedBudget = existingBudget.copy(
                            budgetAmountCents = amountCents,
                            alertThreshold = state.alertThreshold,
                            note = state.note.ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                        budgetRepository.updateBudget(updatedBudget)
                        Log.d(TAG, "预算更新成功")
                    } else {
                        Log.e(TAG, "未找到要更新的预算，ID: ${state.editingBudgetId}")
                        _uiState.update { it.copy(isLoading = false, errorMessage = "未找到要更新的预算") }
                        return@launch
                    }
                } else {
                    Log.d(TAG, "创建新预算，分类: ${state.selectedCategoryId}")
                    // 创建新预算
                    budgetRepository.createBudget(
                        year = currentYear,
                        month = currentMonth,
                        categoryId = state.selectedCategoryId,
                        amountCents = amountCents
                    )
                    Log.d(TAG, "新预算创建成功")
                }
                
                Log.d(TAG, "预算保存操作完成，调用成功回调")
                _uiState.update { it.copy(saveSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "保存预算时发生异常", e)
                _uiState.update { it.copy(errorMessage = "保存失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
                Log.d(TAG, "预算保存流程结束")
            }
        }
    }
}
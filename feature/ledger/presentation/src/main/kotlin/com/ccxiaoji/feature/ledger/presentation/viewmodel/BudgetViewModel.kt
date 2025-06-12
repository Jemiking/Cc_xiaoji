package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.api.BudgetAlert
import com.ccxiaoji.feature.ledger.api.BudgetItem
import com.ccxiaoji.feature.ledger.api.CategoryItem
import com.ccxiaoji.feature.ledger.api.LedgerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 预算管理的UI状态
 */
data class BudgetUiState(
    val budgets: List<BudgetItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val editingBudget: BudgetItem? = null,
    val totalBudget: BudgetItem? = null,
    val budgetAlerts: List<BudgetAlert> = emptyList()
)

/**
 * 预算管理ViewModel
 * 使用LedgerApi进行数据操作，实现模块间解耦
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val ledgerApi: LedgerApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
        loadCategories()
        loadBudgetAlerts()
    }

    /**
     * 加载预算列表
     */
    private fun loadBudgets() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                ledgerApi.getBudgetsWithSpent(_uiState.value.selectedYear, _uiState.value.selectedMonth)
                    .catch { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "加载预算失败: ${exception.message}"
                            )
                        }
                    }
                    .collect { budgets ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                budgets = budgets,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                
                // 同时加载总预算
                loadTotalBudget()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "加载预算失败: ${e.message}"
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
            try {
                val categories = ledgerApi.getAllCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "加载分类失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 加载总预算
     */
    private fun loadTotalBudget() {
        viewModelScope.launch {
            try {
                val totalBudget = ledgerApi.getTotalBudget(_uiState.value.selectedYear, _uiState.value.selectedMonth)
                _uiState.update { it.copy(totalBudget = totalBudget) }
            } catch (e: Exception) {
                // 总预算加载失败不影响主要功能，只记录错误
                _uiState.update { 
                    it.copy(error = "加载总预算失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 加载预算警告
     */
    private fun loadBudgetAlerts() {
        viewModelScope.launch {
            try {
                val alerts = ledgerApi.getBudgetAlerts(_uiState.value.selectedYear, _uiState.value.selectedMonth)
                _uiState.update { it.copy(budgetAlerts = alerts) }
            } catch (e: Exception) {
                // 预算警告加载失败不影响主要功能
                _uiState.update { 
                    it.copy(error = "加载预算警告失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 切换选择的年月
     */
    fun selectYearMonth(year: Int, month: Int) {
        if (year != _uiState.value.selectedYear || month != _uiState.value.selectedMonth) {
            _uiState.update { 
                it.copy(selectedYear = year, selectedMonth = month)
            }
            loadBudgets()
            loadBudgetAlerts()
        }
    }

    /**
     * 显示添加/编辑预算对话框
     */
    fun showAddEditDialog(budget: BudgetItem? = null) {
        _uiState.update { 
            it.copy(showAddEditDialog = true, editingBudget = budget)
        }
    }

    /**
     * 隐藏添加/编辑预算对话框
     */
    fun hideAddEditDialog() {
        _uiState.update { 
            it.copy(showAddEditDialog = false, editingBudget = null)
        }
    }

    /**
     * 保存预算（创建或更新）
     */
    fun saveBudget(
        budgetAmountCents: Int,
        categoryId: String? = null,
        alertThreshold: Float = 0.8f,
        note: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val editingBudget = _uiState.value.editingBudget
                if (editingBudget != null) {
                    // 更新现有预算
                    ledgerApi.updateBudget(
                        budgetId = editingBudget.id,
                        budgetAmountCents = budgetAmountCents,
                        alertThreshold = alertThreshold,
                        note = note
                    )
                } else {
                    // 创建新预算
                    ledgerApi.upsertBudget(
                        year = _uiState.value.selectedYear,
                        month = _uiState.value.selectedMonth,
                        budgetAmountCents = budgetAmountCents,
                        categoryId = categoryId,
                        alertThreshold = alertThreshold,
                        note = note
                    )
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        showAddEditDialog = false,
                        editingBudget = null
                    )
                }
                
                // 重新加载预算列表
                loadBudgets()
                loadBudgetAlerts()

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "保存预算失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 删除预算
     */
    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                ledgerApi.deleteBudget(budgetId)
                
                _uiState.update { it.copy(isLoading = false) }
                
                // 重新加载预算列表
                loadBudgets()
                loadBudgetAlerts()

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "删除预算失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 检查预算是否超支
     */
    fun checkBudgetExceeded(categoryId: String? = null, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isExceeded = ledgerApi.checkBudgetExceeded(
                    _uiState.value.selectedYear,
                    _uiState.value.selectedMonth,
                    categoryId
                )
                callback(isExceeded)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "检查预算超支失败: ${e.message}")
                }
                callback(false)
            }
        }
    }

    /**
     * 检查预算是否触发预警
     */
    fun checkBudgetAlert(categoryId: String? = null, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isAlert = ledgerApi.checkBudgetAlert(
                    _uiState.value.selectedYear,
                    _uiState.value.selectedMonth,
                    categoryId
                )
                callback(isAlert)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "检查预算预警失败: ${e.message}")
                }
                callback(false)
            }
        }
    }

    /**
     * 获取预算使用百分比
     */
    fun getBudgetUsagePercentage(categoryId: String? = null, callback: (Float?) -> Unit) {
        viewModelScope.launch {
            try {
                val percentage = ledgerApi.getBudgetUsagePercentage(
                    _uiState.value.selectedYear,
                    _uiState.value.selectedMonth,
                    categoryId
                )
                callback(percentage)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "获取预算使用率失败: ${e.message}")
                }
                callback(null)
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadBudgets()
        loadCategories()
        loadBudgetAlerts()
    }
}
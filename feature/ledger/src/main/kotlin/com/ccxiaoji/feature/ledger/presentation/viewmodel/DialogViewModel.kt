package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * 管理各种对话框状态的ViewModel
 * 负责控制添加、编辑、过滤等对话框的显示隐藏
 */
@HiltViewModel
class DialogViewModel @Inject constructor() : ViewModel() {
    
    private val _dialogState = MutableStateFlow(DialogState())
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()
    
    /**
     * 显示添加交易对话框
     */
    fun showAddTransactionDialog() {
        _dialogState.update { it.copy(showAddTransactionDialog = true) }
    }
    
    /**
     * 隐藏添加交易对话框
     */
    fun hideAddTransactionDialog() {
        _dialogState.update { it.copy(showAddTransactionDialog = false) }
    }
    
    /**
     * 显示编辑交易对话框
     */
    fun showEditTransactionDialog(transaction: Transaction) {
        _dialogState.update { 
            it.copy(
                editingTransaction = transaction,
                showEditTransactionDialog = true
            )
        }
    }
    
    /**
     * 隐藏编辑交易对话框
     */
    fun hideEditTransactionDialog() {
        _dialogState.update { 
            it.copy(
                editingTransaction = null,
                showEditTransactionDialog = false
            )
        }
    }
    
    /**
     * 显示过滤器对话框
     */
    fun showFilterDialog() {
        _dialogState.update { it.copy(showFilterDialog = true) }
    }
    
    /**
     * 隐藏过滤器对话框
     */
    fun hideFilterDialog() {
        _dialogState.update { it.copy(showFilterDialog = false) }
    }
    
    /**
     * 显示预算提醒
     */
    fun showBudgetAlert(message: String, isExceeded: Boolean) {
        _dialogState.update { 
            it.copy(
                budgetAlert = BudgetAlertInfo(
                    message = message,
                    isExceeded = isExceeded
                )
            )
        }
    }
    
    /**
     * 关闭预算提醒
     */
    fun dismissBudgetAlert() {
        _dialogState.update { it.copy(budgetAlert = null) }
    }
    
    /**
     * 清除所有对话框
     */
    fun clearAllDialogs() {
        _dialogState.update { DialogState() }
    }
    
    /**
     * 显示批量删除对话框
     */
    fun showBatchDeleteDialog() {
        _dialogState.update { it.copy(showBatchDeleteDialog = true) }
    }
    
    /**
     * 隐藏批量删除对话框
     */
    fun hideBatchDeleteDialog() {
        _dialogState.update { it.copy(showBatchDeleteDialog = false) }
    }
    
    /**
     * 显示批量修改分类对话框
     */
    fun showBatchCategoryDialog() {
        _dialogState.update { it.copy(showBatchCategoryDialog = true) }
    }
    
    /**
     * 隐藏批量修改分类对话框
     */
    fun hideBatchCategoryDialog() {
        _dialogState.update { it.copy(showBatchCategoryDialog = false) }
    }
    
    /**
     * 显示批量修改账户对话框
     */
    fun showBatchAccountDialog() {
        _dialogState.update { it.copy(showBatchAccountDialog = true) }
    }
    
    /**
     * 隐藏批量修改账户对话框
     */
    fun hideBatchAccountDialog() {
        _dialogState.update { it.copy(showBatchAccountDialog = false) }
    }
}

/**
 * 对话框状态
 */
data class DialogState(
    // 对话框状态
    val showAddTransactionDialog: Boolean = false,
    val showEditTransactionDialog: Boolean = false,
    val showFilterDialog: Boolean = false,
    val showBatchDeleteDialog: Boolean = false,
    val showBatchCategoryDialog: Boolean = false,
    val showBatchAccountDialog: Boolean = false,
    
    // 编辑数据
    val editingTransaction: Transaction? = null,
    
    // 预算提醒
    val budgetAlert: BudgetAlertInfo? = null
)

/**
 * 预算提醒信息
 */
data class BudgetAlertInfo(
    val message: String,
    val isExceeded: Boolean
)
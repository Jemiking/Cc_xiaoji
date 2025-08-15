package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.usecase.DeleteTransactionUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.BatchDeleteTransactionsUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.BatchUpdateTransactionsCategoryUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.BatchUpdateTransactionsAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 处理交易选择模式的ViewModel
 * 负责多选、全选、批量删除等功能
 */
@HiltViewModel
class SelectionViewModel @Inject constructor(
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val batchDeleteTransactionsUseCase: BatchDeleteTransactionsUseCase,
    private val batchUpdateTransactionsCategoryUseCase: BatchUpdateTransactionsCategoryUseCase,
    private val batchUpdateTransactionsAccountUseCase: BatchUpdateTransactionsAccountUseCase
) : ViewModel() {
    
    private val _selectionState = MutableStateFlow(SelectionState())
    val selectionState: StateFlow<SelectionState> = _selectionState.asStateFlow()
    
    /**
     * 切换选择模式
     */
    fun toggleSelectionMode() {
        _selectionState.update { 
            it.copy(
                isSelectionMode = !it.isSelectionMode,
                selectedTransactionIds = if (it.isSelectionMode) emptySet() else it.selectedTransactionIds
            )
        }
    }
    
    /**
     * 切换交易选择
     */
    fun toggleTransactionSelection(transactionId: String) {
        _selectionState.update { state ->
            val newSelection = if (state.selectedTransactionIds.contains(transactionId)) {
                state.selectedTransactionIds - transactionId
            } else {
                state.selectedTransactionIds + transactionId
            }
            state.copy(selectedTransactionIds = newSelection)
        }
    }
    
    /**
     * 选择所有交易
     */
    fun selectAllTransactions(allTransactionIds: List<String>) {
        _selectionState.update { 
            it.copy(selectedTransactionIds = allTransactionIds.toSet())
        }
    }
    
    /**
     * 清除所有选择
     */
    fun clearSelection() {
        _selectionState.update { 
            it.copy(selectedTransactionIds = emptySet())
        }
    }
    
    /**
     * 删除选中的交易
     */
    fun deleteSelectedTransactions(onComplete: () -> Unit) {
        viewModelScope.launch {
            val selectedIds = _selectionState.value.selectedTransactionIds
            selectedIds.forEach { id ->
                deleteTransactionUseCase(id)
            }
            _selectionState.update { 
                it.copy(
                    isSelectionMode = false,
                    selectedTransactionIds = emptySet()
                )
            }
            onComplete()
        }
    }
    
    /**
     * 退出选择模式
     */
    fun exitSelectionMode() {
        _selectionState.update { 
            it.copy(
                isSelectionMode = false,
                selectedTransactionIds = emptySet()
            )
        }
    }
    
    /**
     * 批量更新分类
     */
    fun batchUpdateCategory(newCategoryId: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val selectedIds = _selectionState.value.selectedTransactionIds
            when (val result = batchUpdateTransactionsCategoryUseCase(selectedIds, newCategoryId)) {
                is BaseResult.Success -> {
                    exitSelectionMode()
                    onComplete(result.data)
                }
                is BaseResult.Error -> {
                    // 处理错误
                    onComplete(0)
                }
            }
        }
    }
    
    /**
     * 批量更新账户
     */
    fun batchUpdateAccount(newAccountId: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val selectedIds = _selectionState.value.selectedTransactionIds
            when (val result = batchUpdateTransactionsAccountUseCase(selectedIds, newAccountId)) {
                is BaseResult.Success -> {
                    exitSelectionMode()
                    onComplete(result.data)
                }
                is BaseResult.Error -> {
                    // 处理错误
                    onComplete(0)
                }
            }
        }
    }
    
    /**
     * 批量删除（支持撤销）
     */
    fun batchDeleteTransactions(onComplete: (Int, List<String>) -> Unit) {
        viewModelScope.launch {
            val selectedIds = _selectionState.value.selectedTransactionIds
            when (val result = batchDeleteTransactionsUseCase(selectedIds)) {
                is BaseResult.Success -> {
                    exitSelectionMode()
                    onComplete(result.data.first, result.data.second)
                }
                is BaseResult.Error -> {
                    // 处理错误
                    onComplete(0, emptyList())
                }
            }
        }
    }
}

/**
 * 选择模式状态
 */
data class SelectionState(
    val isSelectionMode: Boolean = false,
    val selectedTransactionIds: Set<String> = emptySet()
) {
    val selectedCount: Int get() = selectedTransactionIds.size
    val hasSelection: Boolean get() = selectedTransactionIds.isNotEmpty()
}
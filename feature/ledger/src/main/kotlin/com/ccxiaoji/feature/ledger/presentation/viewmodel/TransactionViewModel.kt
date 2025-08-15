package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.usecase.*
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 处理交易相关逻辑的ViewModel
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()
    
    /**
     * 加载交易记录
     */
    fun loadTransactions(accountId: String? = null) {
        viewModelScope.launch {
            val transactionsFlow = if (accountId != null) {
                getTransactionsUseCase.getByAccount(accountId)
            } else {
                getTransactionsUseCase()
            }
            
            transactionsFlow.collect { transactions ->
                _uiState.update { it.copy(transactions = transactions) }
            }
        }
    }
    
    /**
     * 添加交易
     */
    fun addTransaction(
        amountCents: Int, 
        categoryId: String, 
        note: String?, 
        accountId: String
    ) {
        viewModelScope.launch {
            try {
                addTransactionUseCase(
                    amountCents = amountCents,
                    categoryId = categoryId,
                    note = note,
                    accountId = accountId
                )
                // 触发交易添加事件
                _uiState.update { it.copy(lastAction = TransactionAction.Added) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * 更新交易
     */
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                updateTransactionUseCase(transaction)
                _uiState.update { it.copy(lastAction = TransactionAction.Updated) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * 删除交易
     */
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transactionId)
                _uiState.update { it.copy(lastAction = TransactionAction.Deleted) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * 设置编辑中的交易
     */
    fun setEditingTransaction(transaction: Transaction?) {
        _uiState.update { it.copy(editingTransaction = transaction) }
    }
    
    /**
     * 切换选择模式
     */
    fun toggleSelectionMode() {
        _uiState.update { 
            it.copy(
                isSelectionMode = !it.isSelectionMode,
                selectedTransactionIds = if (it.isSelectionMode) emptySet() else it.selectedTransactionIds
            )
        }
    }
    
    /**
     * 切换交易选择状态
     */
    fun toggleTransactionSelection(transactionId: String) {
        _uiState.update { state ->
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
    fun selectAllTransactions() {
        _uiState.update { state ->
            state.copy(selectedTransactionIds = state.transactions.map { it.id }.toSet())
        }
    }
    
    /**
     * 清除选择
     */
    fun clearSelection() {
        _uiState.update { 
            it.copy(selectedTransactionIds = emptySet())
        }
    }
    
    /**
     * 删除选中的交易
     */
    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedTransactionIds
            selectedIds.forEach { id ->
                try {
                    deleteTransactionUseCase(id)
                } catch (e: Exception) {
                    // 记录错误但继续删除其他交易
                }
            }
            _uiState.update { 
                it.copy(
                    isSelectionMode = false,
                    selectedTransactionIds = emptySet(),
                    lastAction = TransactionAction.BatchDeleted
                )
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 交易UI状态
 */
data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val editingTransaction: Transaction? = null,
    val isSelectionMode: Boolean = false,
    val selectedTransactionIds: Set<String> = emptySet(),
    val error: String? = null,
    val lastAction: TransactionAction? = null
)

/**
 * 交易操作类型
 */
enum class TransactionAction {
    Added, Updated, Deleted, BatchDeleted
}
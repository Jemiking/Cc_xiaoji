package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DeleteTransactionUiState())
    val uiState: StateFlow<DeleteTransactionUiState> = _uiState.asStateFlow()
    
    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.getTransactionById(transactionId)?.let { transaction ->
                _uiState.value = _uiState.value.copy(
                    transaction = transaction,
                    transactionId = transactionId
                )
            }
        }
    }
    
    fun deleteTransaction() {
        val transactionId = _uiState.value.transactionId ?: return
        
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
        }
    }
}

data class DeleteTransactionUiState(
    val transaction: Transaction? = null,
    val transactionId: String? = null
)
package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    data class TransferUiState(
        val isLoading: Boolean = false,
        val accounts: List<Account> = emptyList(),
        val fromAccount: Account? = null,
        val toAccount: Account? = null,
        val amount: String = "",
        val note: String = "",
        val fromAccountError: String? = null,
        val toAccountError: String? = null,
        val amountError: String? = null,
        val transferSuccess: Boolean = false,
        val canTransfer: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()
    
    init {
        loadAccounts()
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            accountRepository.getAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(
                    accounts = accounts,
                    isLoading = false
                )
            }
        }
    }
    
    fun selectFromAccount(account: Account) {
        _uiState.value = _uiState.value.copy(
            fromAccount = account,
            fromAccountError = null
        )
        validateTransfer()
    }
    
    fun selectToAccount(account: Account) {
        _uiState.value = _uiState.value.copy(
            toAccount = account,
            toAccountError = null
        )
        validateTransfer()
    }
    
    fun updateAmount(amount: String) {
        val filtered = amount.filter { char -> 
            char.isDigit() || char == '.' 
        }
        
        val error = when {
            filtered.isEmpty() -> null
            filtered.toDoubleOrNull() == null -> "请输入有效的金额"
            filtered.toDoubleOrNull()!! <= 0 -> "金额必须大于0"
            else -> null
        }
        
        _uiState.value = _uiState.value.copy(
            amount = filtered,
            amountError = error
        )
        validateTransfer()
    }
    
    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }
    
    private fun validateTransfer() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull() ?: 0.0
        val fromBalance = state.fromAccount?.balanceYuan ?: 0.0
        
        val canTransfer = state.fromAccount != null &&
                          state.toAccount != null &&
                          state.amount.isNotEmpty() &&
                          amountValue > 0 &&
                          amountValue <= fromBalance &&
                          state.amountError == null
        
        // 检查余额是否充足
        if (state.fromAccount != null && amountValue > fromBalance) {
            _uiState.value = state.copy(
                amountError = "余额不足",
                canTransfer = false
            )
        } else {
            _uiState.value = state.copy(canTransfer = canTransfer)
        }
    }
    
    fun performTransfer() {
        val state = _uiState.value
        
        // 验证必填字段
        if (state.fromAccount == null) {
            _uiState.value = state.copy(fromAccountError = "请选择转出账户")
            return
        }
        
        if (state.toAccount == null) {
            _uiState.value = state.copy(toAccountError = "请选择转入账户")
            return
        }
        
        val amountValue = state.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _uiState.value = state.copy(amountError = "请输入有效的金额")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                val amountCents = (amountValue * 100).toLong()
                accountRepository.transferBetweenAccounts(
                    fromAccountId = state.fromAccount.id,
                    toAccountId = state.toAccount.id,
                    amountCents = amountCents,
                    note = state.note.ifEmpty { null }
                )
                
                _uiState.value = state.copy(
                    isLoading = false,
                    transferSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    amountError = "转账失败：${e.message}"
                )
            }
        }
    }
}
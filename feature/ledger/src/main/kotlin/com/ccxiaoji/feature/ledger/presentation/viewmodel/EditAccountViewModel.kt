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
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class EditAccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    data class EditAccountUiState(
        val isLoading: Boolean = false,
        val name: String = "",
        val balance: String = "",
        val accountTypeDisplay: String = "",
        val nameError: String? = null,
        val balanceError: String? = null,
        val isSaved: Boolean = false,
        val account: Account? = null
    )
    
    private val _uiState = MutableStateFlow(EditAccountUiState())
    val uiState: StateFlow<EditAccountUiState> = _uiState.asStateFlow()
    
    fun loadAccount(accountId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                accountRepository.getAccountById(accountId)?.let { account ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        name = account.name,
                        balance = account.balanceYuan.toString(),
                        accountTypeDisplay = "${account.type.icon} ${account.type.displayName}",
                        account = account
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = if (name.isEmpty()) "账户名称不能为空" else null
        )
    }
    
    fun updateBalance(balance: String) {
        val filtered = balance.filter { char -> 
            char.isDigit() || char == '.' || char == '-' 
        }
        
        val error = when {
            filtered.isEmpty() -> null
            filtered.toDoubleOrNull() == null -> "请输入有效的金额"
            else -> null
        }
        
        _uiState.value = _uiState.value.copy(
            balance = filtered,
            balanceError = error
        )
    }
    
    fun saveAccount() {
        val state = _uiState.value
        val account = state.account ?: return
        
        if (state.name.isEmpty()) {
            _uiState.value = state.copy(nameError = "账户名称不能为空")
            return
        }
        
        val balanceValue = state.balance.toDoubleOrNull()
        if (balanceValue == null) {
            _uiState.value = state.copy(balanceError = "请输入有效的金额")
            return
        }
        
        viewModelScope.launch {
            val balanceCents = (balanceValue * 100).toLong()
            val updatedAccount = account.copy(
                name = state.name,
                balanceCents = balanceCents,
                updatedAt = Clock.System.now()
            )
            
            accountRepository.updateAccount(updatedAccount)
            _uiState.value = state.copy(isSaved = true)
        }
    }
}
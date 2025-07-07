package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddAccountUiState(
    val name: String = "",
    val selectedType: AccountType = AccountType.BANK,
    val balance: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val balanceError: String? = null,
    val canCreate: Boolean = false
)

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        val error = when {
            name.isEmpty() -> null
            name.isBlank() -> "账户名称不能为空"
            name.length > 50 -> "账户名称不能超过50个字符"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                name = name,
                nameError = error
            )
        }
        updateCanCreate()
    }
    
    fun selectType(type: AccountType) {
        _uiState.update {
            it.copy(selectedType = type)
        }
    }
    
    fun updateBalance(balance: String) {
        val filteredBalance = balance.filter { char -> 
            char.isDigit() || char == '.' || char == '-'
        }
        
        val error = when {
            filteredBalance.isEmpty() -> null
            filteredBalance.toDoubleOrNull() == null -> "请输入有效金额"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                balance = filteredBalance,
                balanceError = error
            )
        }
        updateCanCreate()
    }
    
    private fun updateCanCreate() {
        _uiState.update {
            it.copy(
                canCreate = it.name.isNotBlank() && 
                           it.nameError == null && 
                           it.balanceError == null &&
                           it.balance.toDoubleOrNull() != null
            )
        }
    }
    
    fun createAccount(onSuccess: () -> Unit) {
        if (!_uiState.value.canCreate) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val state = _uiState.value
                val balanceCents = ((state.balance.toDoubleOrNull() ?: 0.0) * 100).toLong()
                
                val accountId = accountRepository.createAccount(
                    name = state.name.trim(),
                    type = state.selectedType,
                    initialBalanceCents = balanceCents
                )
                
                // 如果成功创建账户，accountId 大于 0
                if (accountId > 0) {
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            error = "创建失败",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "创建失败：${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}
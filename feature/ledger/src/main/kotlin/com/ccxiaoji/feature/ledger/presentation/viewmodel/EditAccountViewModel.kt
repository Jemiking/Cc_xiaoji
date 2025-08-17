package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.util.Log
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
    
    companion object {
        private const val TAG = "EditAccountViewModel"
    }
    
    data class EditAccountUiState(
        val isLoading: Boolean = false,
        val name: String = "",
        val balance: String = "",
        val accountTypeDisplay: String = "",
        val nameError: String? = null,
        val balanceError: String? = null,
        val isSaved: Boolean = false,
        val account: Account? = null,
        val errorMessage: String? = null
    )
    
    private val _uiState = MutableStateFlow(EditAccountUiState())
    val uiState: StateFlow<EditAccountUiState> = _uiState.asStateFlow()
    
    fun loadAccount(accountId: String) {
        Log.d(TAG, "开始加载账户，ID: $accountId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            Log.d(TAG, "设置加载状态为true")
            try {
                Log.d(TAG, "调用accountRepository.getAccountById")
                val account = accountRepository.getAccountById(accountId)
                if (account != null) {
                    Log.d(TAG, "成功获取账户: ${account.name}, 余额: ${account.balanceYuan}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        name = account.name,
                        balance = account.balanceYuan.toString(),
                        accountTypeDisplay = "${account.type.icon} ${account.type.displayName}",
                        account = account,
                        errorMessage = null
                    )
                    Log.d(TAG, "成功更新UI状态")
                } else {
                    Log.e(TAG, "未找到账户，ID: $accountId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "未找到指定账户"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载账户时异常", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "加载账户失败: ${e.message}"
                )
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
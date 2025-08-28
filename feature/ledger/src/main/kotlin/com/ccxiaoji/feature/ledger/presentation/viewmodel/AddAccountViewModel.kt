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
    val canCreate: Boolean = false,
    
    // 信用卡专用字段
    val creditLimit: String = "",
    val billingDay: String = "",
    val paymentDueDay: String = "",
    val creditLimitError: String? = null,
    val billingDayError: String? = null,
    val paymentDueDayError: String? = null
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
            it.copy(
                selectedType = type,
                // 切换到非信用卡类型时清空信用卡字段
                creditLimit = if (type != AccountType.CREDIT_CARD) "" else it.creditLimit,
                billingDay = if (type != AccountType.CREDIT_CARD) "" else it.billingDay,
                paymentDueDay = if (type != AccountType.CREDIT_CARD) "" else it.paymentDueDay,
                creditLimitError = if (type != AccountType.CREDIT_CARD) null else it.creditLimitError,
                billingDayError = if (type != AccountType.CREDIT_CARD) null else it.billingDayError,
                paymentDueDayError = if (type != AccountType.CREDIT_CARD) null else it.paymentDueDayError
            )
        }
        updateCanCreate()
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
    
    // 信用卡字段处理方法
    fun updateCreditLimit(limit: String) {
        val filteredLimit = limit.filter { char -> 
            char.isDigit() || char == '.'
        }
        
        val error = when {
            filteredLimit.isEmpty() && _uiState.value.selectedType == AccountType.CREDIT_CARD -> "请输入信用额度"
            filteredLimit.isNotEmpty() && filteredLimit.toDoubleOrNull() == null -> "请输入有效金额"
            filteredLimit.toDoubleOrNull()?.let { it <= 0 } == true -> "信用额度必须大于0"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                creditLimit = filteredLimit,
                creditLimitError = error
            )
        }
        updateCanCreate()
    }
    
    fun updateBillingDay(day: String) {
        val filteredDay = day.filter { it.isDigit() }
        
        val error = when {
            filteredDay.isEmpty() && _uiState.value.selectedType == AccountType.CREDIT_CARD -> "请输入账单日"
            filteredDay.toIntOrNull()?.let { it < 1 || it > 28 } == true -> "账单日必须在1-28之间"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                billingDay = filteredDay,
                billingDayError = error
            )
        }
        updateCanCreate()
    }
    
    fun updatePaymentDueDay(day: String) {
        val filteredDay = day.filter { it.isDigit() }
        
        val error = when {
            filteredDay.isEmpty() && _uiState.value.selectedType == AccountType.CREDIT_CARD -> "请输入还款日"
            filteredDay.toIntOrNull()?.let { it < 1 || it > 28 } == true -> "还款日必须在1-28之间"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                paymentDueDay = filteredDay,
                paymentDueDayError = error
            )
        }
        updateCanCreate()
    }
    
    private fun updateCanCreate() {
        _uiState.update { state ->
            val baseValid = state.name.isNotBlank() && 
                           state.nameError == null && 
                           state.balanceError == null &&
                           state.balance.toDoubleOrNull() != null
            
            val creditCardValid = if (state.selectedType == AccountType.CREDIT_CARD) {
                state.creditLimit.isNotEmpty() &&
                state.billingDay.isNotEmpty() &&
                state.paymentDueDay.isNotEmpty() &&
                state.creditLimitError == null &&
                state.billingDayError == null &&
                state.paymentDueDayError == null &&
                state.creditLimit.toDoubleOrNull() != null &&
                state.billingDay.toIntOrNull() != null &&
                state.paymentDueDay.toIntOrNull() != null
            } else {
                true
            }
            
            state.copy(
                canCreate = baseValid && creditCardValid
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
                
                val accountId = if (state.selectedType == AccountType.CREDIT_CARD) {
                    // 创建信用卡账户
                    val creditLimitCents = ((state.creditLimit.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val billingDay = state.billingDay.toIntOrNull()
                    val paymentDueDay = state.paymentDueDay.toIntOrNull()
                    
                    accountRepository.createAccount(
                        name = state.name.trim(),
                        type = state.selectedType,
                        initialBalanceCents = balanceCents,
                        creditLimitCents = creditLimitCents,
                        billingDay = billingDay,
                        paymentDueDay = paymentDueDay
                    )
                } else {
                    // 创建普通账户
                    accountRepository.createAccount(
                        name = state.name.trim(),
                        type = state.selectedType,
                        initialBalanceCents = balanceCents
                    )
                }
                
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
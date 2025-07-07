package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCreditCardViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddCreditCardUiState())
    val uiState: StateFlow<AddCreditCardUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.update { 
            it.copy(
                name = name,
                nameError = if (name.isBlank()) "请输入卡片名称" else null
            )
        }
    }
    
    fun updateCreditLimit(creditLimit: String) {
        val filtered = creditLimit.filter { it.isDigit() || it == '.' }
        _uiState.update { 
            it.copy(
                creditLimit = filtered,
                creditLimitError = when {
                    filtered.isEmpty() -> "请输入信用额度"
                    (filtered.toDoubleOrNull() ?: 0.0) <= 0 -> "信用额度必须大于0"
                    else -> null
                }
            )
        }
    }
    
    fun updateUsedAmount(usedAmount: String) {
        val filtered = usedAmount.filter { it.isDigit() || it == '.' }
        val creditLimitValue = _uiState.value.creditLimit.toDoubleOrNull() ?: 0.0
        val usedAmountValue = filtered.toDoubleOrNull() ?: 0.0
        
        _uiState.update { 
            it.copy(
                usedAmount = filtered,
                usedAmountError = when {
                    usedAmountValue < 0 -> "已用额度不能为负数"
                    creditLimitValue > 0 && usedAmountValue > creditLimitValue -> "已用额度不能超过信用额度"
                    else -> null
                }
            )
        }
    }
    
    fun updateBillingDay(billingDay: String) {
        val filtered = billingDay.filter { it.isDigit() }
        val dayValue = filtered.toIntOrNull() ?: 0
        
        _uiState.update { 
            it.copy(
                billingDay = if (dayValue in 0..28) filtered else it.billingDay,
                billingDayError = when {
                    filtered.isEmpty() -> "请输入账单日"
                    dayValue !in 1..28 -> "账单日必须在1-28之间"
                    else -> null
                }
            )
        }
    }
    
    fun updatePaymentDueDay(paymentDueDay: String) {
        val filtered = paymentDueDay.filter { it.isDigit() }
        val dayValue = filtered.toIntOrNull() ?: 0
        
        _uiState.update { 
            it.copy(
                paymentDueDay = if (dayValue in 0..28) filtered else it.paymentDueDay,
                paymentDueDayError = when {
                    filtered.isEmpty() -> "请输入还款日"
                    dayValue !in 1..28 -> "还款日必须在1-28之间"
                    else -> null
                }
            )
        }
    }
    
    fun isFormValid(): Boolean {
        val state = _uiState.value
        return state.name.isNotBlank() &&
                state.nameError == null &&
                (state.creditLimit.toDoubleOrNull() ?: 0.0) > 0 &&
                state.creditLimitError == null &&
                state.usedAmountError == null &&
                (state.billingDay.toIntOrNull() ?: 0) in 1..28 &&
                state.billingDayError == null &&
                (state.paymentDueDay.toIntOrNull() ?: 0) in 1..28 &&
                state.paymentDueDayError == null
    }
    
    fun saveCreditCard() {
        if (!isFormValid()) {
            _uiState.update { it.copy(error = "请检查输入信息") }
            return
        }
        
        val state = _uiState.value
        val creditLimitYuan = state.creditLimit.toDoubleOrNull() ?: 0.0
        val usedAmountYuan = state.usedAmount.toDoubleOrNull() ?: 0.0
        val billingDay = state.billingDay.toIntOrNull() ?: 1
        val paymentDueDay = state.paymentDueDay.toIntOrNull() ?: 20
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                accountRepository.createAccount(
                    name = state.name.trim(),
                    type = AccountType.CREDIT_CARD,
                    initialBalanceCents = -(usedAmountYuan * 100).toLong(), // 信用卡余额为负数表示欠款
                    creditLimitCents = (creditLimitYuan * 100).toLong(),
                    billingDay = billingDay,
                    paymentDueDay = paymentDueDay,
                    gracePeriodDays = 3 // 默认3天宽限期
                )
                
                // 保存成功
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "添加失败，请重试"
                    )
                }
            }
        }
    }
}

data class AddCreditCardUiState(
    val name: String = "",
    val nameError: String? = null,
    val creditLimit: String = "",
    val creditLimitError: String? = null,
    val usedAmount: String = "0",
    val usedAmountError: String? = null,
    val billingDay: String = "",
    val billingDayError: String? = null,
    val paymentDueDay: String = "",
    val paymentDueDayError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
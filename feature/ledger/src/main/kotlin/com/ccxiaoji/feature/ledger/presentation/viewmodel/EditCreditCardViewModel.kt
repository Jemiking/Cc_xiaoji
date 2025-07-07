package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditCreditCardUiState(
    val isLoading: Boolean = true,
    val creditCard: Account? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditCreditCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val accountId: String = savedStateHandle.get<String>("accountId") ?: ""
    
    private val _uiState = MutableStateFlow(EditCreditCardUiState())
    val uiState: StateFlow<EditCreditCardUiState> = _uiState.asStateFlow()
    
    init {
        loadCreditCard()
    }
    
    private fun loadCreditCard() {
        viewModelScope.launch {
            try {
                val account = accountRepository.getAccountById(accountId)
                if (account != null && account.type == com.ccxiaoji.feature.ledger.domain.model.AccountType.CREDIT_CARD) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        creditCard = account
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "信用卡账户不存在"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "加载信用卡信息失败：${e.message}"
                ) }
            }
        }
    }
    
    fun updateCreditCard(
        creditLimitYuan: Double,
        usedAmountYuan: Double,
        billingDay: Int,
        paymentDueDay: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val currentCard = _uiState.value.creditCard
                if (currentCard == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "信用卡信息未加载"
                    ) }
                    return@launch
                }
                
                // 验证输入
                if (creditLimitYuan <= 0) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "信用额度必须大于0"
                    ) }
                    return@launch
                }
                
                if (usedAmountYuan > creditLimitYuan) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "已用额度不能大于信用额度"
                    ) }
                    return@launch
                }
                
                if (billingDay !in 1..28 || paymentDueDay !in 1..28) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "账单日和还款日必须在1-28之间"
                    ) }
                    return@launch
                }
                
                // 更新信用卡信息
                val updatedCard = currentCard.copy(
                    creditLimitCents = (creditLimitYuan * 100).toLong(),
                    balanceCents = -(usedAmountYuan * 100).toLong(), // 信用卡余额为负数表示欠款
                    billingDay = billingDay,
                    paymentDueDay = paymentDueDay
                )
                
                accountRepository.updateAccount(updatedCard)
                
                _uiState.update { it.copy(
                    isLoading = false,
                    saveSuccess = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "保存失败：${e.message}"
                ) }
            }
        }
    }
}
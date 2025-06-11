package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.api.AccountItem
import com.ccxiaoji.feature.ledger.api.PaymentRecord
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.PaymentStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val ledgerApi: LedgerApi
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(CreditCardUiState())
    val uiState: StateFlow<CreditCardUiState> = _uiState.asStateFlow()
    
    // 信用卡列表
    val creditCards: StateFlow<List<AccountItem>> = ledgerApi.getCreditCards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 添加信用卡
    fun addCreditCard(
        name: String,
        creditLimitYuan: Double,
        usedAmountYuan: Double,
        billingDay: Int,
        paymentDueDay: Int
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                ledgerApi.addCreditCard(
                    name = name,
                    creditLimitYuan = creditLimitYuan,
                    usedAmountYuan = usedAmountYuan,
                    billingDay = billingDay,
                    paymentDueDay = paymentDueDay
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "信用卡添加成功"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "添加失败：${e.message}"
                    )
                }
            }
        }
    }
    
    // 更新信用卡信息
    fun updateCreditCardInfo(
        accountId: String,
        creditLimitYuan: Double,
        usedAmountYuan: Double,
        billingDay: Int,
        paymentDueDay: Int
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                ledgerApi.updateCreditCardInfo(
                    accountId = accountId,
                    creditLimitYuan = creditLimitYuan,
                    usedAmountYuan = usedAmountYuan,
                    billingDay = billingDay,
                    paymentDueDay = paymentDueDay
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "信用卡信息更新成功"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "更新失败：${e.message}"
                    )
                }
            }
        }
    }
    
    // 记录还款
    fun recordPayment(accountId: String, paymentAmountYuan: Double) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                ledgerApi.recordCreditCardPayment(
                    accountId = accountId,
                    paymentAmountYuan = paymentAmountYuan,
                    paymentType = "CUSTOM",
                    note = null
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "还款记录成功"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "还款失败：${e.message}"
                    )
                }
            }
        }
    }
    
    // 获取需要还款提醒的信用卡
    fun checkPaymentReminders() {
        viewModelScope.launch {
            try {
                val cardsNeedingPayment = ledgerApi.checkPaymentReminders()
                
                if (cardsNeedingPayment.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            paymentReminders = cardsNeedingPayment
                        )
                    }
                }
            } catch (e: Exception) {
                // 静默处理，不影响用户体验
            }
        }
    }
    
    // 清除消息
    fun clearMessage() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }
    
    // Payment History Methods
    private val _selectedCardPayments = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val selectedCardPayments: StateFlow<List<PaymentRecord>> = _selectedCardPayments.asStateFlow()
    
    private val _paymentStats = MutableStateFlow<PaymentStats?>(null)
    val paymentStats: StateFlow<PaymentStats?> = _paymentStats.asStateFlow()
    
    fun loadPaymentHistory(accountId: String) {
        viewModelScope.launch {
            ledgerApi.getCreditCardPayments(accountId)
                .collect { payments ->
                    _selectedCardPayments.value = payments
                }
        }
        
        viewModelScope.launch {
            try {
                val stats = ledgerApi.getPaymentStats(accountId)
                _paymentStats.value = stats
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun deletePaymentRecord(paymentId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                ledgerApi.deletePaymentRecord(paymentId)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "还款记录已删除"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "删除失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun recordPaymentWithType(
        accountId: String,
        paymentAmountYuan: Double,
        paymentType: String,
        note: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                ledgerApi.recordCreditCardPayment(
                    accountId = accountId,
                    paymentAmountYuan = paymentAmountYuan,
                    paymentType = paymentType,
                    note = note
                )
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "还款成功"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "还款失败：${e.message}"
                    )
                }
            }
        }
    }
}

data class CreditCardUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val paymentReminders: List<AccountItem> = emptyList()
)
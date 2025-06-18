package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardPaymentEntity
import com.ccxiaoji.feature.ledger.data.local.entity.PaymentType
import com.ccxiaoji.feature.ledger.data.repository.AccountRepository
import com.ccxiaoji.feature.ledger.data.repository.PaymentStats
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(CreditCardUiState())
    val uiState: StateFlow<CreditCardUiState> = _uiState.asStateFlow()
    
    // 信用卡列表
    val creditCards: StateFlow<List<Account>> = accountRepository.getCreditCardAccounts()
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
                
                accountRepository.createAccount(
                    name = name,
                    type = AccountType.CREDIT_CARD,
                    initialBalanceCents = -(usedAmountYuan * 100).toLong(), // 信用卡余额为负数表示欠款
                    creditLimitCents = (creditLimitYuan * 100).toLong(),
                    billingDay = billingDay,
                    paymentDueDay = paymentDueDay,
                    gracePeriodDays = 3 // 默认3天宽限期
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
                
                // 先获取当前账户信息
                val currentAccount = accountRepository.getAccountById(accountId)
                if (currentAccount != null) {
                    // 计算余额变化（信用卡余额为负数表示欠款）
                    val newBalanceCents = -(usedAmountYuan * 100).toLong()
                    val balanceChangeCents = newBalanceCents - currentAccount.balanceCents
                    
                    // 更新余额
                    if (balanceChangeCents != 0L) {
                        accountRepository.updateBalance(accountId, balanceChangeCents)
                    }
                }
                
                // 更新信用卡信息
                accountRepository.updateCreditCardInfo(
                    accountId = accountId,
                    creditLimitCents = (creditLimitYuan * 100).toLong(),
                    billingDay = billingDay,
                    paymentDueDay = paymentDueDay,
                    gracePeriodDays = 3
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
                
                accountRepository.recordCreditCardPayment(
                    accountId = accountId,
                    paymentAmountCents = (paymentAmountYuan * 100).toLong()
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
                val currentDay = java.time.LocalDate.now().dayOfMonth
                val cardsWithPaymentDue = accountRepository.getCreditCardsWithPaymentDueDay(currentDay)
                val cardsWithDebt = accountRepository.getCreditCardsWithDebt()
                
                val cardsNeedingPayment = cardsWithPaymentDue.intersect(cardsWithDebt.toSet())
                
                if (cardsNeedingPayment.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            paymentReminders = cardsNeedingPayment.toList()
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
    private val _selectedCardPayments = MutableStateFlow<List<CreditCardPaymentEntity>>(emptyList())
    val selectedCardPayments: StateFlow<List<CreditCardPaymentEntity>> = _selectedCardPayments.asStateFlow()
    
    private val _paymentStats = MutableStateFlow<PaymentStats?>(null)
    val paymentStats: StateFlow<PaymentStats?> = _paymentStats.asStateFlow()
    
    fun loadPaymentHistory(accountId: String) {
        viewModelScope.launch {
            accountRepository.getCreditCardPayments(accountId)
                .collect { payments ->
                    _selectedCardPayments.value = payments
                }
        }
        
        viewModelScope.launch {
            try {
                val stats = accountRepository.getPaymentStats(accountId)
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
                
                accountRepository.deletePaymentRecord(paymentId)
                
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
        paymentType: PaymentType,
        note: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Get current debt amount
                val account = creditCards.value.find { it.id == accountId }
                val dueAmountCents = if (account != null && account.balanceCents < 0) {
                    -account.balanceCents
                } else {
                    0L
                }
                
                accountRepository.recordCreditCardPaymentWithHistory(
                    accountId = accountId,
                    paymentAmountCents = (paymentAmountYuan * 100).toLong(),
                    paymentType = paymentType,
                    dueAmountCents = dueAmountCents,
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
    val paymentReminders: List<Account> = emptyList()
)
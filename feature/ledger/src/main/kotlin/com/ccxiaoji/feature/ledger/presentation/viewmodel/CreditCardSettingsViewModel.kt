package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.worker.creditcard.PaymentReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 信用卡设置界面的ViewModel
 */
@HiltViewModel
class CreditCardSettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val paymentReminderScheduler: PaymentReminderScheduler
) : ViewModel() {
    
    private val accountId: String = checkNotNull(savedStateHandle["accountId"])
    
    private val _uiState = MutableStateFlow(CreditCardSettingsUiState())
    val uiState: StateFlow<CreditCardSettingsUiState> = _uiState.asStateFlow()
    
    private var originalAccount: Account? = null
    
    fun loadCreditCard(accountId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val account = accountRepository.getAccountById(accountId)
                if (account != null && account.type == com.ccxiaoji.feature.ledger.domain.model.AccountType.CREDIT_CARD) {
                    originalAccount = account
                    
                    val isReminderEnabled = paymentReminderScheduler.isPaymentReminderEnabled()
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            cardName = account.name,
                            creditLimit = (account.creditLimitYuan ?: 0.0).toInt().toString(),
                            cashAdvanceLimit = (account.cashAdvanceLimitYuan ?: 0.0).toInt().toString(),
                            billingDay = (account.billingDay ?: 1).toString(),
                            paymentDueDay = (account.paymentDueDay ?: 20).toString(),
                            gracePeriodDays = (account.gracePeriodDays ?: 25).toString(),
                            annualFee = (account.annualFeeAmountYuan ?: 0.0).toInt().toString(),
                            annualFeeWaiverThreshold = (account.annualFeeWaiverThresholdYuan ?: 0.0).toInt().toString(),
                            interestRate = ((account.dailyInterestRatePercent ?: 0.05)).toString(),
                            isReminderEnabled = isReminderEnabled,
                            hasChanges = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "未找到信用卡信息"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载信用卡信息失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateCardName(name: String) {
        _uiState.update { it.copy(cardName = name, hasChanges = true) }
    }
    
    fun updateCreditLimit(limit: String) {
        if (limit.isEmpty() || limit.toDoubleOrNull() != null) {
            _uiState.update { it.copy(creditLimit = limit, hasChanges = true) }
        }
    }
    
    fun updateCashAdvanceLimit(limit: String) {
        if (limit.isEmpty() || limit.toDoubleOrNull() != null) {
            _uiState.update { it.copy(cashAdvanceLimit = limit, hasChanges = true) }
        }
    }
    
    fun updateBillingDay(day: String) {
        val dayInt = day.toIntOrNull()
        if (day.isEmpty() || (dayInt != null && dayInt in 1..28)) {
            _uiState.update { it.copy(billingDay = day, hasChanges = true) }
        }
    }
    
    fun updatePaymentDueDay(day: String) {
        val dayInt = day.toIntOrNull()
        if (day.isEmpty() || (dayInt != null && dayInt in 1..28)) {
            _uiState.update { it.copy(paymentDueDay = day, hasChanges = true) }
        }
    }
    
    fun updateGracePeriodDays(days: String) {
        if (days.isEmpty() || days.toIntOrNull() != null) {
            _uiState.update { it.copy(gracePeriodDays = days, hasChanges = true) }
        }
    }
    
    fun updateAnnualFee(fee: String) {
        if (fee.isEmpty() || fee.toDoubleOrNull() != null) {
            _uiState.update { it.copy(annualFee = fee, hasChanges = true) }
        }
    }
    
    fun updateAnnualFeeWaiverThreshold(threshold: String) {
        if (threshold.isEmpty() || threshold.toDoubleOrNull() != null) {
            _uiState.update { it.copy(annualFeeWaiverThreshold = threshold, hasChanges = true) }
        }
    }
    
    fun updateInterestRate(rate: String) {
        if (rate.isEmpty() || rate.toDoubleOrNull() != null) {
            _uiState.update { it.copy(interestRate = rate, hasChanges = true) }
        }
    }
    
    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReminderEnabled = enabled, hasChanges = true) }
            
            if (enabled) {
                paymentReminderScheduler.schedulePaymentReminders()
            } else {
                paymentReminderScheduler.cancelPaymentReminders()
            }
        }
    }
    
    fun saveChanges() {
        val currentState = _uiState.value
        val currentAccount = originalAccount ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                // 创建更新后的账户对象
                val updatedAccount = currentAccount.copy(
                    name = currentState.cardName,
                    creditLimitCents = (currentState.creditLimit.toDoubleOrNull() ?: 0.0).toLong() * 100,
                    cashAdvanceLimitCents = (currentState.cashAdvanceLimit.toDoubleOrNull() ?: 0.0).toLong() * 100,
                    billingDay = currentState.billingDay.toIntOrNull() ?: 1,
                    paymentDueDay = currentState.paymentDueDay.toIntOrNull() ?: 20,
                    gracePeriodDays = currentState.gracePeriodDays.toIntOrNull() ?: 25,
                    annualFeeAmountCents = (currentState.annualFee.toDoubleOrNull() ?: 0.0).toLong() * 100,
                    annualFeeWaiverThresholdCents = (currentState.annualFeeWaiverThreshold.toDoubleOrNull() ?: 0.0).toLong() * 100,
                    interestRate = (currentState.interestRate.toDoubleOrNull() ?: 0.05) / 100.0
                )
                
                // 保存更新
                accountRepository.updateAccount(updatedAccount)
                
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        hasChanges = false,
                        saveSuccessMessage = "设置已保存"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "保存失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

/**
 * 信用卡设置界面的UI状态
 */
data class CreditCardSettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    
    // 基本信息
    val cardName: String = "",
    
    // 额度信息
    val creditLimit: String = "",
    val cashAdvanceLimit: String = "",
    
    // 日期信息
    val billingDay: String = "",
    val paymentDueDay: String = "",
    val gracePeriodDays: String = "",
    
    // 费用信息
    val annualFee: String = "",
    val annualFeeWaiverThreshold: String = "",
    val interestRate: String = "",
    
    // 提醒设置
    val isReminderEnabled: Boolean = false,
    
    // 消息
    val errorMessage: String? = null,
    val saveSuccessMessage: String? = null
)
package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardPaymentEntity
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.PaymentStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentHistoryUiState(
    val account: Account? = null,
    val payments: List<CreditCardPaymentEntity> = emptyList(),
    val paymentStats: PaymentStats? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val accountId: String = savedStateHandle.get<String>("accountId") 
        ?: throw IllegalArgumentException("accountId is required")
    
    private val _uiState = MutableStateFlow(PaymentHistoryUiState())
    val uiState: StateFlow<PaymentHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 加载账户信息
                val account = accountRepository.getAccountById(accountId)
                _uiState.update { it.copy(account = account) }
                
                // 加载还款历史
                accountRepository.getCreditCardPayments(accountId).collect { payments ->
                    _uiState.update { it.copy(payments = payments) }
                }
                
                // 加载还款统计
                val stats = accountRepository.getPaymentStats(accountId)
                _uiState.update { it.copy(paymentStats = stats, isLoading = false) }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载还款历史失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            try {
                accountRepository.deletePaymentRecord(paymentId)
                // 删除后重新加载数据
                loadData()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "删除还款记录失败：${e.message}")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.CreditCardBill
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CreditCardBillRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.usecase.RecordCreditCardPaymentUseCase
import com.ccxiaoji.feature.ledger.data.local.entity.PaymentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * 信用卡账单详情视图模型
 */
@HiltViewModel
class CreditCardBillDetailViewModel @Inject constructor(
    private val creditCardBillRepository: CreditCardBillRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val recordCreditCardPaymentUseCase: RecordCreditCardPaymentUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreditCardBillDetailUiState())
    val uiState: StateFlow<CreditCardBillDetailUiState> = _uiState.asStateFlow()
    
    init {
        // 加载所有账户
        viewModelScope.launch {
            accountRepository.getAccounts().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
    }
    
    /**
     * 加载账单详情
     */
    fun loadBillDetail(billId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            when (val result = creditCardBillRepository.getBillById(billId)) {
                is BaseResult.Success -> {
                    val bill = result.data
                    _uiState.update { 
                        it.copy(
                            bill = bill,
                            isLoading = false
                        )
                    }
                    
                    // 加载账单期间的交易记录
                    loadBillTransactions(bill)
                }
                is BaseResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "加载账单失败"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 加载账单期间的交易记录
     */
    private fun loadBillTransactions(bill: CreditCardBill) {
        viewModelScope.launch {
            // 将Instant转换为LocalDate
            val startDate = bill.billStartDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val endDate = bill.billEndDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
            
            transactionRepository.getTransactionsByAccountAndDateRange(
                accountId = bill.accountId,
                startDate = startDate,
                endDate = endDate
            ).collect { transactions ->
                _uiState.update { 
                    it.copy(transactions = transactions)
                }
            }
        }
    }
    
    /**
     * 记录还款
     * @param billId 账单ID
     * @param amount 还款金额（分）
     * @param fromAccountId 还款账户ID（可选）
     */
    fun recordPayment(billId: String, amount: Int, fromAccountId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            // 判断还款类型
            val bill = _uiState.value.bill
            val paymentType = when {
                bill == null -> PaymentType.CUSTOM
                amount >= bill.totalAmountCents.toInt() -> PaymentType.FULL
                amount == bill.minimumPaymentCents.toInt() -> PaymentType.MINIMUM
                else -> PaymentType.CUSTOM
            }
            
            when (val result = recordCreditCardPaymentUseCase(
                billId = billId,
                amount = amount,
                fromAccountId = fromAccountId,
                paymentType = paymentType
            )) {
                is BaseResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            successMessage = "还款成功"
                        )
                    }
                    // 重新加载账单详情
                    loadBillDetail(billId)
                }
                is BaseResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            errorMessage = result.exception.message ?: "还款失败"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}

/**
 * 信用卡账单详情UI状态
 */
data class CreditCardBillDetailUiState(
    val bill: CreditCardBill? = null,
    val transactions: List<Transaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
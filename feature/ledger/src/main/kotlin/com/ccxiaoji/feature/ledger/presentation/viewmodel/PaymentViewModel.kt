package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

data class PaymentUiState(
    val isLoading: Boolean = true,
    val cardName: String = "",
    val currentDebt: Double = 0.0,
    val creditLimit: Double = 0.0,
    val availableCredit: Double = 0.0,
    val paymentSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {
    
    private val accountId: String = savedStateHandle.get<String>("accountId") ?: ""
    
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    
    private var creditCardAccount: Account? = null
    private var paymentCategory: Category? = null
    
    init {
        loadCreditCardInfo()
        loadPaymentCategory()
    }
    
    private fun loadCreditCardInfo() {
        viewModelScope.launch {
            try {
                val account = accountRepository.getAccountById(accountId)
                creditCardAccount = account
                account?.let {
                    if (it.type == com.ccxiaoji.feature.ledger.domain.model.AccountType.CREDIT_CARD) {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                cardName = it.name,
                                currentDebt = -it.balanceYuan, // 信用卡余额为负数表示欠款
                                creditLimit = it.creditLimitYuan ?: 0.0,
                                availableCredit = it.availableCreditYuan ?: 0.0
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "加载信用卡信息失败：${e.message}"
                ) }
            }
        }
    }
    
    private fun loadPaymentCategory() {
        viewModelScope.launch {
            try {
                // 获取系统预设的信用卡还款分类
                categoryRepository.getCategoriesByType(Category.Type.INCOME)
                    .collect { categories ->
                        paymentCategory = categories.find { it.id == "credit_card_payment" }
                            ?: categories.find { it.name == "信用卡还款" }
                            ?: categories.find { it.isSystem } // 如果找不到，使用任意系统分类
                    }
            } catch (e: Exception) {
                // 如果获取分类失败，不影响还款操作
            }
        }
    }
    
    fun makePayment(amountYuan: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val account = creditCardAccount
                val category = paymentCategory
                
                if (account == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "信用卡账户信息未加载"
                    ) }
                    return@launch
                }
                
                if (category == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "还款分类未设置"
                    ) }
                    return@launch
                }
                
                // 保存交易记录（同时会更新账户余额）
                addTransactionUseCase(
                    amountCents = (amountYuan * 100).toInt(),
                    categoryId = category.id,
                    note = "${account.name} 还款",
                    accountId = accountId
                )
                
                _uiState.update { it.copy(
                    isLoading = false,
                    paymentSuccess = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "还款失败：${e.message}"
                ) }
            }
        }
    }
}
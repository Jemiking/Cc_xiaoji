package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardBillEntity
import com.ccxiaoji.feature.ledger.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditCardBillViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountDao: AccountDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreditCardBillUiState())
    val uiState = _uiState.asStateFlow()
    
    fun loadAccount(accountId: String) {
        viewModelScope.launch {
            try {
                val account = accountDao.getAccountById(accountId)
                _uiState.update { it.copy(accountName = account?.name) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "加载账户信息失败") }
            }
        }
    }
    
    fun getBills(accountId: String): Flow<List<CreditCardBillEntity>> {
        return accountRepository.getCreditCardBills(accountId)
    }
    
    fun generateBillForAccount(accountId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                accountRepository.generateCreditCardBill(accountId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "账单生成成功"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "账单生成失败：${e.message}"
                    )
                }
            }
        }
    }
    
    suspend fun getBillDetail(billId: String): CreditCardBillEntity? {
        return try {
            accountRepository.getCurrentCreditCardBill(billId)
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "加载账单详情失败") }
            null
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

data class CreditCardBillUiState(
    val isLoading: Boolean = false,
    val accountName: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
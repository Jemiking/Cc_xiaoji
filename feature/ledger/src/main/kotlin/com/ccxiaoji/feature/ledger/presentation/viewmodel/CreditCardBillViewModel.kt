package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardBillEntity
import com.ccxiaoji.feature.ledger.domain.model.CreditCardBill
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CreditCardBillRepository
import com.ccxiaoji.common.base.BaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class CreditCardBillViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val creditCardBillRepository: CreditCardBillRepository,
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
    
    fun getBills(accountId: String): Flow<List<CreditCardBill>> {
        // 从Repository获取账单列表，Repository会返回响应式的Flow
        return creditCardBillRepository.getBillsByAccount(accountId)
    }
    
    fun generateBillForAccount(accountId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // 获取账户信息以确定账单日
                val account = accountDao.getAccountById(accountId)
                if (account == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "账户不存在"
                        )
                    }
                    return@launch
                }
                
                // 计算账单周期（上个月的账单日到这个月的账单日）
                val now = kotlinx.datetime.Clock.System.now()
                val today = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                val billingDay = account.billingDay ?: 1
                
                // 计算本期账单的起止日期
                val periodEnd = kotlinx.datetime.LocalDate(today.year, today.month, billingDay)
                val periodStart = periodEnd.minus(kotlinx.datetime.DatePeriod(months = 1))
                
                // 生成账单
                when (val result = creditCardBillRepository.generateBill(accountId, periodStart, periodEnd)) {
                    is BaseResult.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "账单生成成功"
                            )
                        }
                    }
                    is BaseResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "账单生成失败：${result.exception.message}"
                            )
                        }
                    }
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
    
    suspend fun getBillDetail(billId: String): CreditCardBill? {
        return try {
            when (val result = creditCardBillRepository.getBillById(billId)) {
                is BaseResult.Success -> result.data
                is BaseResult.Error -> {
                    _uiState.update { it.copy(errorMessage = "加载账单详情失败：${result.exception.message}") }
                    null
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "加载账单详情失败：${e.message}") }
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
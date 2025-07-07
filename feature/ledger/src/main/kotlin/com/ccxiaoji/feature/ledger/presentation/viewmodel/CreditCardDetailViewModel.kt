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

@HiltViewModel
class CreditCardDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val accountId = savedStateHandle.get<String>("accountId") ?: ""
    
    private val _uiState = MutableStateFlow(CreditCardDetailUiState())
    val uiState: StateFlow<CreditCardDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadCreditCard()
    }
    
    private fun loadCreditCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val account = accountRepository.getAccountById(accountId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        creditCard = account,
                        error = if (account == null) "信用卡不存在" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }
    
    fun refresh() {
        loadCreditCard()
    }
}

data class CreditCardDetailUiState(
    val creditCard: Account? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
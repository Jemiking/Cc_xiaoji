package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()
    
    init {
        loadAccounts()
        loadTotalBalance()
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(
                    accounts = accounts,
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadTotalBalance() {
        viewModelScope.launch {
            val totalBalance = accountRepository.getTotalBalance()
            _uiState.value = _uiState.value.copy(totalBalance = totalBalance)
        }
    }
    
    fun createAccount(
        name: String,
        type: AccountType,
        initialBalanceCents: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                accountRepository.createAccount(
                    name = name,
                    type = type,
                    initialBalanceCents = initialBalanceCents
                )
                loadTotalBalance() // Refresh total balance
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun updateAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountRepository.updateAccount(account)
                loadTotalBalance() // Refresh total balance
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
    
    fun setDefaultAccount(accountId: String) {
        viewModelScope.launch {
            try {
                accountRepository.setDefaultAccount(accountId)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
    
    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount(accountId)
                loadTotalBalance() // Refresh total balance
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
    
    fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long
    ) {
        viewModelScope.launch {
            try {
                accountRepository.transferBetweenAccounts(
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amountCents = amountCents,
                    note = null
                )
                loadTotalBalance() // Refresh total balance
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}

data class AccountUiState(
    val accounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
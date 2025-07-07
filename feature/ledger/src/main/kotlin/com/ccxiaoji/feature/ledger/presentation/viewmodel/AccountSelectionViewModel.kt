package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountSelectionUiState(
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AccountSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccountSelectionUiState())
    val uiState: StateFlow<AccountSelectionUiState> = _uiState.asStateFlow()
    
    companion object {
        private val DEFAULT_ACCOUNT_ID_KEY = longPreferencesKey("ledger_default_account_id")
    }
    
    init {
        loadAccounts()
        loadCurrentSelection()
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            accountRepository.getAccounts().collect { accounts ->
                _uiState.update { 
                    it.copy(
                        accounts = accounts,
                        isLoading = false
                    ) 
                }
            }
        }
    }
    
    private fun loadCurrentSelection() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                preferences[DEFAULT_ACCOUNT_ID_KEY]
            }.collect { accountId ->
                _uiState.update { it.copy(selectedAccountId = accountId) }
            }
        }
    }
    
    fun selectAccount(accountId: Long) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DEFAULT_ACCOUNT_ID_KEY] = accountId
            }
            _uiState.update { it.copy(selectedAccountId = accountId) }
        }
    }
}
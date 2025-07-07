package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatchUpdateAccountUiState(
    val selectedCount: Int = 0,
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BatchUpdateAccountViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BatchUpdateAccountUiState())
    val uiState: StateFlow<BatchUpdateAccountUiState> = _uiState.asStateFlow()
    
    var selectedAccountId by mutableStateOf<String?>(null)
        private set
    
    init {
        // 获取传递过来的选中数量
        val selectedCount = savedStateHandle.get<Int>("selectedCount") ?: 0
        _uiState.update { it.copy(selectedCount = selectedCount) }
        
        loadAccounts()
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
    
    fun selectAccount(accountId: String) {
        selectedAccountId = if (selectedAccountId == accountId) null else accountId
    }
}
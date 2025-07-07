package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val categories: List<Category> = emptyList(),
    val filteredCategories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val isIncome: Boolean = false,
    val amountText: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val amountError: String? = null,
    val canSave: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val userApi: UserApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()
    
    private val currentUserId = userApi.getCurrentUserId()
    private val preselectedAccountId: String? = savedStateHandle["accountId"]
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // 加载账户
            accountRepository.getAccounts().collect { accounts ->
                val selectedAccount = if (preselectedAccountId != null) {
                    accounts.find { it.id == preselectedAccountId }
                } else {
                    accounts.firstOrNull()
                }
                
                _uiState.update {
                    it.copy(
                        accounts = accounts,
                        selectedAccount = selectedAccount
                    )
                }
            }
        }
        
        viewModelScope.launch {
            // 加载分类
            categoryRepository.getCategories().collect { categories ->
                _uiState.update {
                    it.copy(categories = categories)
                }
                updateFilteredCategories()
            }
        }
    }
    
    fun selectAccount(account: Account) {
        _uiState.update {
            it.copy(selectedAccount = account)
        }
        updateCanSave()
    }
    
    fun setIncomeType(isIncome: Boolean) {
        _uiState.update {
            it.copy(
                isIncome = isIncome,
                selectedCategoryId = null // 重置分类选择
            )
        }
        updateFilteredCategories()
    }
    
    fun selectCategory(categoryId: String) {
        _uiState.update {
            it.copy(selectedCategoryId = categoryId)
        }
        updateCanSave()
    }
    
    fun updateAmount(amount: String) {
        val filteredAmount = amount.filter { it.isDigit() || it == '.' }
        val error = when {
            filteredAmount.isEmpty() -> null
            filteredAmount.toDoubleOrNull() == null -> "请输入有效金额"
            filteredAmount.toDouble() <= 0 -> "金额必须大于0"
            else -> null
        }
        
        _uiState.update {
            it.copy(
                amountText = filteredAmount,
                amountError = error
            )
        }
        updateCanSave()
    }
    
    fun updateNote(note: String) {
        _uiState.update {
            it.copy(note = note)
        }
    }
    
    private fun updateFilteredCategories() {
        val state = _uiState.value
        val filteredCategories = state.categories.filter { category ->
            category.type == if (state.isIncome) Category.Type.INCOME else Category.Type.EXPENSE
        }
        
        // 如果当前选中的分类不在过滤后的列表中，选择第一个
        val newSelectedId = if (filteredCategories.any { it.id == state.selectedCategoryId }) {
            state.selectedCategoryId
        } else {
            filteredCategories.firstOrNull()?.id
        }
        
        _uiState.update {
            it.copy(
                filteredCategories = filteredCategories,
                selectedCategoryId = newSelectedId
            )
        }
        updateCanSave()
    }
    
    private fun updateCanSave() {
        _uiState.update {
            it.copy(
                canSave = it.amountText.isNotEmpty() && 
                         it.amountError == null && 
                         it.amountText.toDoubleOrNull() != null &&
                         it.amountText.toDouble() > 0 &&
                         it.selectedCategoryId != null &&
                         it.selectedAccount != null
            )
        }
    }
    
    fun saveTransaction(onSuccess: () -> Unit) {
        if (!_uiState.value.canSave) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val state = _uiState.value
                val amountCents = ((state.amountText.toDoubleOrNull() ?: 0.0) * 100).toInt()
                
                transactionRepository.addTransaction(
                    amountCents = amountCents,
                    categoryId = state.selectedCategoryId!!,
                    accountId = state.selectedAccount!!.id,
                    note = state.note.ifBlank { null }
                )
                
                onSuccess()
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
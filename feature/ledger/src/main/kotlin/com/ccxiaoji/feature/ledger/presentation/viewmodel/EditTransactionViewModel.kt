package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTransactionUiState(
    val transaction: Transaction? = null,
    val categories: List<Category> = emptyList(),
    val filteredCategories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val isIncome: Boolean = false,
    val amountText: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val amountError: String? = null,
    val canSave: Boolean = false
)

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()
    
    private val transactionId: String = savedStateHandle["transactionId"] ?: ""
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { categories ->
                _uiState.update {
                    it.copy(categories = categories)
                }
                // 如果交易已加载，更新过滤的分类
                if (_uiState.value.transaction != null) {
                    updateFilteredCategories()
                }
            }
        }
    }
    
    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                if (transaction != null) {
                    val isIncome = transaction.categoryDetails?.type == "INCOME"
                    _uiState.update {
                        it.copy(
                            transaction = transaction,
                            isIncome = isIncome,
                            selectedCategoryId = transaction.categoryId,
                            amountText = transaction.amountYuan.toString(),
                            note = transaction.note ?: "",
                            isLoading = false
                        )
                    }
                    updateFilteredCategories()
                } else {
                    _uiState.update {
                        it.copy(
                            error = "交易记录不存在",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "加载失败：${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun setIncomeType(isIncome: Boolean) {
        _uiState.update {
            it.copy(isIncome = isIncome)
        }
        updateFilteredCategories()
        
        // 如果当前选中的分类不在新类型中，选择第一个
        val filteredCategories = _uiState.value.filteredCategories
        if (filteredCategories.none { it.id == _uiState.value.selectedCategoryId }) {
            _uiState.update {
                it.copy(selectedCategoryId = filteredCategories.firstOrNull()?.id)
            }
        }
        updateCanSave()
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
        
        _uiState.update {
            it.copy(filteredCategories = filteredCategories)
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
                         it.transaction != null
            )
        }
    }
    
    fun saveTransaction(onSuccess: () -> Unit) {
        if (!_uiState.value.canSave) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val state = _uiState.value
                val transaction = state.transaction!!
                val amountCents = ((state.amountText.toDoubleOrNull() ?: 0.0) * 100).toInt()
                val selectedCategory = state.filteredCategories.find { it.id == state.selectedCategoryId }
                
                val updatedTransaction = transaction.copy(
                    amountCents = amountCents,
                    categoryId = state.selectedCategoryId!!,
                    categoryDetails = selectedCategory?.let { category ->
                        com.ccxiaoji.feature.ledger.domain.model.CategoryDetails(
                            id = category.id,
                            name = category.name,
                            icon = category.icon,
                            color = category.color,
                            type = category.type.name
                        )
                    },
                    note = state.note.ifBlank { null }
                )
                
                val result = transactionRepository.updateTransaction(updatedTransaction)
                when (result) {
                    is com.ccxiaoji.common.base.BaseResult.Success -> {
                        onSuccess()
                    }
                    is com.ccxiaoji.common.base.BaseResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "保存失败: ${result.exception.message}",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "保存失败：${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}
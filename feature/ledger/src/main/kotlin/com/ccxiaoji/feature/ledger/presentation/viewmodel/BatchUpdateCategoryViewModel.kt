package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatchUpdateCategoryUiState(
    val selectedCount: Int = 0,
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BatchUpdateCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BatchUpdateCategoryUiState())
    val uiState: StateFlow<BatchUpdateCategoryUiState> = _uiState.asStateFlow()
    
    var selectedCategoryId by mutableStateOf<String?>(null)
        private set
    
    init {
        // 获取传递过来的选中数量
        val selectedCount = savedStateHandle.get<Int>("selectedCount") ?: 0
        _uiState.update { it.copy(selectedCount = selectedCount) }
        
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            categoryRepository.getCategories().collect { categories ->
                val expenseCategories = categories.filter { it.type == Category.Type.EXPENSE }
                val incomeCategories = categories.filter { it.type == Category.Type.INCOME }
                
                _uiState.update { 
                    it.copy(
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories,
                        isLoading = false
                    ) 
                }
            }
        }
    }
    
    fun selectCategory(categoryId: String) {
        selectedCategoryId = if (selectedCategoryId == categoryId) null else categoryId
    }
}
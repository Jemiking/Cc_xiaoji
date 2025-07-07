package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val categoryType = savedStateHandle.get<String>("categoryType") ?: "EXPENSE"
    private val type = if (categoryType == "INCOME") Category.Type.INCOME else Category.Type.EXPENSE
    
    private val _uiState = MutableStateFlow(AddCategoryUiState(
        selectedIcon = if (type == Category.Type.EXPENSE) {
            Category.DEFAULT_EXPENSE_ICONS.firstOrNull() ?: ""
        } else {
            Category.DEFAULT_INCOME_ICONS.firstOrNull() ?: ""
        },
        selectedColor = Category.DEFAULT_COLORS.firstOrNull() ?: "#FF6B6B"
    ))
    val uiState: StateFlow<AddCategoryUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.update { 
            it.copy(
                name = name,
                nameError = if (name.isBlank()) "请输入分类名称" else null
            )
        }
    }
    
    fun updateIcon(icon: String) {
        _uiState.update { it.copy(selectedIcon = icon) }
    }
    
    fun updateColor(color: String) {
        _uiState.update { it.copy(selectedColor = color) }
    }
    
    fun saveCategory() {
        val currentState = _uiState.value
        
        // 验证输入
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "请输入分类名称") }
            return
        }
        
        if (currentState.selectedIcon.isBlank()) {
            _uiState.update { it.copy(error = "请选择图标") }
            return
        }
        
        if (currentState.selectedColor.isBlank()) {
            _uiState.update { it.copy(error = "请选择颜色") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                categoryRepository.createCategory(
                    name = currentState.name.trim(),
                    type = type.name,
                    icon = currentState.selectedIcon,
                    color = currentState.selectedColor,
                    parentId = null
                )
                
                // 保存成功
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "保存失败，请重试"
                    )
                }
            }
        }
    }
}

data class AddCategoryUiState(
    val name: String = "",
    val nameError: String? = null,
    val selectedIcon: String = "",
    val selectedColor: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
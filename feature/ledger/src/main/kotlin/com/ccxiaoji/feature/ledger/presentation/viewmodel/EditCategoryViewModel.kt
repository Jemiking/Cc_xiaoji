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
class EditCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val categoryId = savedStateHandle.get<String>("categoryId") ?: ""
    
    private val _uiState = MutableStateFlow(EditCategoryUiState())
    val uiState: StateFlow<EditCategoryUiState> = _uiState.asStateFlow()
    
    init {
        loadCategory()
    }
    
    private fun loadCategory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val category = categoryRepository.getCategoryById(categoryId)
                if (category != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            category = category,
                            name = category.name,
                            selectedIcon = category.icon,
                            selectedColor = category.color
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "分类不存在"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载分类失败"
                    )
                }
            }
        }
    }
    
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
        val category = currentState.category ?: return
        
        // 验证输入
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "请输入分类名称") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 创建更新后的分类对象
                val updatedCategory = category.copy(
                    name = if (category.isSystem) category.name else currentState.name.trim(),
                    icon = currentState.selectedIcon,
                    color = currentState.selectedColor
                )
                
                categoryRepository.updateCategory(updatedCategory)
                
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

data class EditCategoryUiState(
    val category: Category? = null,
    val name: String = "",
    val nameError: String? = null,
    val selectedIcon: String = "",
    val selectedColor: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
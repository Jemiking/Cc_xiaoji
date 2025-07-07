package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditSavingsGoalUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val targetAmount: String = "",
    val targetDate: LocalDate? = null,
    val description: String = "",
    val selectedIcon: String = "savings",
    val selectedColorIndex: Int = 0,
    val selectedColor: Color = Color(0xFF4CAF50),
    val colorOptions: List<Color> = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4)  // Cyan
    ),
    val nameError: String? = null,
    val amountError: String? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditSavingsGoalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savingsGoalRepository: SavingsGoalRepository
) : ViewModel() {
    
    private val goalId: Long? = savedStateHandle.get<String>("goalId")?.toLongOrNull()
    
    private val _uiState = MutableStateFlow(AddEditSavingsGoalUiState())
    val uiState: StateFlow<AddEditSavingsGoalUiState> = _uiState.asStateFlow()
    
    init {
        loadGoal()
    }
    
    private fun loadGoal() {
        if (goalId != null) {
            viewModelScope.launch {
                try {
                    val goal = savingsGoalRepository.getSavingsGoalById(goalId)
                    if (goal != null) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                name = goal.name,
                                targetAmount = goal.targetAmount.toString(),
                                targetDate = goal.targetDate,
                                description = goal.description ?: "",
                                selectedIcon = goal.iconName,
                                selectedColorIndex = findColorIndex(goal.color),
                                selectedColor = parseColor(goal.color)
                            ) 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "储蓄目标不存在"
                            ) 
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "加载储蓄目标失败：${e.message}"
                        ) 
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun updateName(name: String) {
        _uiState.update { 
            it.copy(
                name = name,
                nameError = null
            ) 
        }
    }
    
    fun updateTargetAmount(amount: String) {
        val filtered = amount.filter { char -> char.isDigit() || char == '.' }
        _uiState.update { 
            it.copy(
                targetAmount = filtered,
                amountError = null
            ) 
        }
    }
    
    fun updateTargetDate(date: LocalDate) {
        _uiState.update { it.copy(targetDate = date) }
    }
    
    fun clearTargetDate() {
        _uiState.update { it.copy(targetDate = null) }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }
    
    fun updateIcon(iconName: String) {
        _uiState.update { it.copy(selectedIcon = iconName) }
    }
    
    fun updateColorIndex(index: Int) {
        _uiState.update { 
            it.copy(
                selectedColorIndex = index,
                selectedColor = it.colorOptions[index]
            ) 
        }
    }
    
    fun saveSavingsGoal() {
        viewModelScope.launch {
            // Validate input
            var hasError = false
            
            if (_uiState.value.name.isBlank()) {
                _uiState.update { it.copy(nameError = "请输入目标名称") }
                hasError = true
            }
            
            val targetAmount = _uiState.value.targetAmount.toDoubleOrNull()
            if (targetAmount == null || targetAmount <= 0) {
                _uiState.update { it.copy(amountError = "请输入有效金额") }
                hasError = true
            }
            
            if (hasError) return@launch
            
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val colorHex = "#${_uiState.value.selectedColor.value.toString(16).uppercase()}"
                
                if (goalId == null) {
                    // Create new goal
                    val newGoal = SavingsGoal(
                        name = _uiState.value.name,
                        targetAmount = targetAmount!!,
                        targetDate = _uiState.value.targetDate,
                        description = _uiState.value.description.ifBlank { null },
                        color = colorHex,
                        iconName = _uiState.value.selectedIcon
                    )
                    savingsGoalRepository.createSavingsGoal(newGoal)
                } else {
                    // Update existing goal
                    val existingGoal = savingsGoalRepository.getSavingsGoalById(goalId)
                    existingGoal?.let { goal ->
                        val updatedGoal = goal.copy(
                            name = _uiState.value.name,
                            targetAmount = targetAmount!!,
                            targetDate = _uiState.value.targetDate,
                            description = _uiState.value.description.ifBlank { null },
                            color = colorHex,
                            iconName = _uiState.value.selectedIcon,
                            updatedAt = java.time.LocalDateTime.now()
                        )
                        savingsGoalRepository.updateSavingsGoal(updatedGoal)
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        saveSuccess = true
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "保存失败：${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    private fun findColorIndex(colorHex: String): Int {
        val color = parseColor(colorHex)
        return _uiState.value.colorOptions.indexOfFirst { 
            it.value == color.value 
        }.takeIf { it >= 0 } ?: 0
    }
    
    private fun parseColor(colorHex: String): Color {
        return try {
            val hex = colorHex.removePrefix("#")
            Color(hex.toLong(16) or 0xFF000000)
        } catch (e: Exception) {
            Color(0xFF4CAF50) // Default green
        }
    }
}
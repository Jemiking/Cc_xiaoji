package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import com.ccxiaoji.shared.user.api.UserApi
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
    private val savingsGoalRepository: SavingsGoalRepository,
    private val userApi: UserApi
) : ViewModel() {
    
    companion object {
        private const val TAG = "AddEditSavingsGoalViewModel"
    }
    
    private val goalId: Long? = savedStateHandle.get<String>("goalId")?.toLongOrNull()
    
    private val _uiState = MutableStateFlow(AddEditSavingsGoalUiState())
    val uiState: StateFlow<AddEditSavingsGoalUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "ViewModel初始化，goalId: $goalId")
        loadGoal()
    }
    
    private fun loadGoal() {
        if (goalId != null) {
            Log.d(TAG, "编辑模式，加载储蓄目标，ID: $goalId")
            viewModelScope.launch {
                try {
                    Log.d(TAG, "开始从数据库加载储蓄目标")
                    val goal = savingsGoalRepository.getSavingsGoalById(goalId)
                    if (goal != null) {
                        Log.d(TAG, "成功加载储蓄目标: ${goal.name}")
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
                        Log.e(TAG, "储蓄目标不存在，ID: $goalId")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "储蓄目标不存在"
                            ) 
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "加载储蓄目标失败", e)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "加载储蓄目标失败：${e.message}"
                        ) 
                    }
                }
            }
        } else {
            Log.d(TAG, "新增模式")
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
        Log.d(TAG, "开始保存储蓄目标")
        viewModelScope.launch {
            // Validate input
            var hasError = false
            
            Log.d(TAG, "验证输入 - 名称: '${_uiState.value.name}', 金额: '${_uiState.value.targetAmount}'")
            
            if (_uiState.value.name.isBlank()) {
                Log.e(TAG, "名称为空")
                _uiState.update { it.copy(nameError = "请输入目标名称") }
                hasError = true
            }
            
            val targetAmount = _uiState.value.targetAmount.toDoubleOrNull()
            if (targetAmount == null || targetAmount <= 0) {
                Log.e(TAG, "金额无效: ${_uiState.value.targetAmount}")
                _uiState.update { it.copy(amountError = "请输入有效金额") }
                hasError = true
            }
            
            if (hasError) {
                Log.e(TAG, "输入验证失败，停止保存")
                return@launch
            }
            
            Log.d(TAG, "输入验证通过，开始保存")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // 获取当前用户ID
                val currentUser = userApi.getCurrentUser()
                val userId = currentUser?.id
                
                if (userId == null) {
                    Log.e(TAG, "无法获取当前用户ID")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "保存失败：无法获取用户信息"
                        ) 
                    }
                    return@launch
                }
                
                Log.d(TAG, "用户ID: $userId")
                
                val colorHex = "#${_uiState.value.selectedColor.value.toString(16).uppercase()}"
                Log.d(TAG, "颜色值: $colorHex")
                
                if (goalId == null) {
                    Log.d(TAG, "创建新储蓄目标")
                    // Create new goal
                    val newGoal = SavingsGoal(
                        userId = userId,
                        name = _uiState.value.name,
                        targetAmount = targetAmount!!,
                        targetDate = _uiState.value.targetDate,
                        description = _uiState.value.description.ifBlank { null },
                        color = colorHex,
                        iconName = _uiState.value.selectedIcon
                    )
                    Log.d(TAG, "保存储蓄目标: ${newGoal}")
                    savingsGoalRepository.createSavingsGoal(newGoal)
                    Log.d(TAG, "创建储蓄目标成功")
                } else {
                    Log.d(TAG, "更新现有储蓄目标，ID: $goalId")
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
                        Log.d(TAG, "更新储蓄目标: ${updatedGoal}")
                        savingsGoalRepository.updateSavingsGoal(updatedGoal)
                        Log.d(TAG, "更新储蓄目标成功")
                    } ?: run {
                        Log.e(TAG, "无法找到要更新的储蓄目标")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "保存失败：储蓄目标不存在"
                            ) 
                        }
                        return@launch
                    }
                }
                
                Log.d(TAG, "保存操作完成，更新UI状态为成功")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        saveSuccess = true
                    ) 
                }
                Log.d(TAG, "储蓄目标保存成功")
            } catch (e: Exception) {
                Log.e(TAG, "保存储蓄目标时发生异常", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "保存失败：${e.message}"
                    ) 
                }
                Log.e(TAG, "储蓄目标保存失败: ${e.message}")
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
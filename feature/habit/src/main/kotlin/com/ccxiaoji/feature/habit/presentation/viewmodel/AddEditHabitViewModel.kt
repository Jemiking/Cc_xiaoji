package com.ccxiaoji.feature.habit.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.habit.domain.usecase.*
import com.ccxiaoji.ui.theme.DesignTokens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val getHabitByIdUseCase: GetHabitByIdUseCase
) : ViewModel() {
    
    data class AddEditHabitUiState(
        val isLoading: Boolean = false,
        val title: String = "",
        val description: String = "",
        val period: String = "daily",
        val target: String = "1",
        val selectedIcon: String = "💪",
        val selectedColor: String = "#4CAF50",
        val titleError: String? = null,
        val targetError: String? = null,
        val isSaved: Boolean = false,
        val habitId: String? = null,
        val availableIcons: List<String> = listOf(
            "💪", "📚", "🏃", "🧘", "💤", "💧", "🎯", "✍️",
            "🎨", "🎵", "🌱", "🧠", "🏋️", "🚴", "🏊", "🤸",
            "📖", "💻", "🎮", "🍎", "🥗", "🚭", "🙏", "⏰"
        ),
        val availableColors: List<Pair<String, Color>> = listOf(
            "#4CAF50" to DesignTokens.BrandColors.Success,
            "#2196F3" to DesignTokens.BrandColors.Primary,
            "#FF9800" to DesignTokens.BrandColors.Warning,
            "#F44336" to DesignTokens.BrandColors.Error,
            "#9C27B0" to DesignTokens.BrandColors.Todo,
            "#00BCD4" to DesignTokens.BrandColors.Info,
            "#795548" to DesignTokens.BrandColors.Plan,
            "#607D8B" to DesignTokens.BrandColors.Schedule
        )
    )
    
    private val _uiState = MutableStateFlow(AddEditHabitUiState())
    val uiState: StateFlow<AddEditHabitUiState> = _uiState.asStateFlow()
    
    fun loadHabit(habitId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val habitWithStreak = getHabitByIdUseCase(habitId)
                habitWithStreak?.let { 
                    val habit = it.habit
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = habit.title,
                        description = habit.description ?: "",
                        period = habit.period,
                        target = habit.target.toString(),
                        selectedIcon = habit.icon ?: "💪",
                        selectedColor = habit.color ?: "#4CAF50",
                        habitId = habitId
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = if (title.isEmpty()) "习惯名称不能为空" else null
        )
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun updatePeriod(period: String) {
        _uiState.value = _uiState.value.copy(period = period)
    }
    
    fun updateTarget(target: String) {
        val filtered = target.filter { it.isDigit() }
        val error = when {
            filtered.isEmpty() -> null
            filtered.toIntOrNull() == null -> "请输入有效的数字"
            filtered.toInt() <= 0 -> "目标次数必须大于0"
            else -> null
        }
        
        _uiState.value = _uiState.value.copy(
            target = filtered,
            targetError = error
        )
    }
    
    fun updateIcon(icon: String) {
        _uiState.value = _uiState.value.copy(selectedIcon = icon)
    }
    
    fun updateColor(color: String) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }
    
    fun randomizeIcon() {
        val randomIcon = _uiState.value.availableIcons.random()
        _uiState.value = _uiState.value.copy(selectedIcon = randomIcon)
    }
    
    fun saveHabit() {
        val state = _uiState.value
        
        if (state.title.isEmpty()) {
            _uiState.value = state.copy(titleError = "习惯名称不能为空")
            return
        }
        
        val targetValue = state.target.toIntOrNull()
        if (targetValue == null || targetValue <= 0) {
            _uiState.value = state.copy(targetError = "请输入有效的目标次数")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                if (state.habitId != null) {
                    // 编辑模式
                    updateHabitUseCase(
                        habitId = state.habitId,
                        title = state.title,
                        description = state.description.ifEmpty { null },
                        period = state.period,
                        target = targetValue,
                        color = state.selectedColor,
                        icon = state.selectedIcon
                    )
                } else {
                    // 添加模式
                    createHabitUseCase(
                        title = state.title,
                        description = state.description.ifEmpty { null },
                        period = state.period,
                        target = targetValue,
                        color = state.selectedColor,
                        icon = state.selectedIcon
                    )
                }
                
                _uiState.value = state.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    titleError = "保存失败：${e.message}"
                )
            }
        }
    }
}
package com.ccxiaoji.feature.habit.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseViewModel
import com.ccxiaoji.feature.habit.domain.usecase.*
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val checkInHabitUseCase: CheckInHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val searchHabitsUseCase: SearchHabitsUseCase
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        observeHabitsWithSearch()
        loadCheckedToday()
    }
    
    private fun observeHabitsWithSearch() {
        viewModelScope.launch {
            searchQuery
                .flatMapLatest { query ->
                    searchHabitsUseCase(query)
                }
                .collect { filteredHabits ->
                _uiState.update { it.copy(habits = filteredHabits) }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    private fun loadCheckedToday() {
        // 获取今日打卡的习惯
        // 注：当前HabitWithStreak模型没有lastCheckInDate字段
        // 需要通过Repository层获取今日打卡记录
        // 暂时保持checkedToday为空，等待后续完善数据获取逻辑
        _uiState.update { it.copy(checkedToday = emptySet()) }
    }
    
    fun addHabit(
        title: String,
        description: String?,
        period: String,
        target: Int
    ) {
        launchWithErrorHandling {
            createHabitUseCase(
                title = title,
                description = description,
                period = period,
                target = target,
                color = "#3A7AFE",
                icon = null
            )
            showSuccess("Habit created successfully")
        }
    }
    
    fun updateHabit(
        habitId: String,
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String,
        icon: String?
    ) {
        launchWithErrorHandling {
            updateHabitUseCase(
                habitId = habitId,
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            )
            showSuccess("Habit updated successfully")
        }
    }
    
    fun checkInHabit(habitId: String) {
        launchWithErrorHandling {
            checkInHabitUseCase(habitId)
            _uiState.update { 
                it.copy(checkedToday = it.checkedToday + habitId)
            }
            showSuccess("Check-in successful")
        }
    }
    
    fun deleteHabit(habitId: String) {
        launchWithErrorHandling {
            deleteHabitUseCase(habitId)
            showSuccess("Habit deleted successfully")
        }
    }
}

data class HabitUiState(
    val habits: List<HabitWithStreak> = emptyList(),
    val checkedToday: Set<String> = emptySet(),
    val isLoading: Boolean = false
)
package com.ccxiaoji.feature.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {
    
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
            combine(
                searchQuery,
                habitRepository.getHabitsWithStreaks()
            ) { query, allHabits ->
                if (query.isBlank()) {
                    allHabits
                } else {
                    allHabits.filter { habitWithStreak ->
                        habitWithStreak.habit.title.contains(query, ignoreCase = true) ||
                        habitWithStreak.habit.description?.contains(query, ignoreCase = true) == true
                    }
                }
            }.collect { filteredHabits ->
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
        viewModelScope.launch {
            habitRepository.createHabit(
                title = title,
                description = description,
                period = period,
                target = target,
                color = "#3A7AFE",
                icon = null
            )
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
        viewModelScope.launch {
            habitRepository.updateHabit(
                habitId = habitId,
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            )
        }
    }
    
    fun checkInHabit(habitId: String) {
        viewModelScope.launch {
            val today = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
            habitRepository.checkInHabit(habitId, today)
            _uiState.update { 
                it.copy(checkedToday = it.checkedToday + habitId)
            }
        }
    }
    
    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
        }
    }
}

data class HabitUiState(
    val habits: List<HabitWithStreak> = emptyList(),
    val checkedToday: Set<String> = emptySet(),
    val isLoading: Boolean = false
)
package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.data.repository.HabitRepository
import com.ccxiaoji.app.data.repository.HabitWithStreak
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
    
    init {
        loadHabits()
        loadCheckedToday()
    }
    
    private fun loadHabits() {
        viewModelScope.launch {
            habitRepository.getHabitsWithStreaks().collect { habits ->
                _uiState.update { it.copy(habits = habits) }
            }
        }
    }
    
    private fun loadCheckedToday() {
        // TODO: Load habits that have been checked in today
        // This would require a method in HabitRepository to get today's check-ins
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
                target = target
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
            habitRepository.checkInHabit(habitId)
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
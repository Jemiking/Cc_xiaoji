package com.ccxiaoji.feature.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.habit.data.repository.HabitRepository
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
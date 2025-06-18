package com.ccxiaoji.feature.habit.api

import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Public API for habit feature module
 */
interface HabitApi {
    // Query methods
    fun getHabits(): Flow<List<Habit>>
    fun getActiveHabitsCount(): Flow<Int>
    fun getTodayCheckedHabitsCount(): Flow<Int>
    fun searchHabits(query: String): Flow<List<Habit>>
    fun getHabitsWithStreaks(): Flow<List<HabitWithStreak>>
    
    // Command methods
    suspend fun createHabit(
        title: String,
        description: String? = null,
        period: String = "daily",
        target: Int = 1,
        color: String = "#3A7AFE",
        icon: String? = null
    ): Habit
    
    suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String? = null,
        period: String = "daily",
        target: Int = 1,
        color: String = "#3A7AFE",
        icon: String? = null
    )
    
    suspend fun checkInHabit(habitId: String, date: LocalDate? = null)
    
    suspend fun deleteHabit(habitId: String)
}
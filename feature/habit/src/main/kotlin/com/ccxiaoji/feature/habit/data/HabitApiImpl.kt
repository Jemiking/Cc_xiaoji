package com.ccxiaoji.feature.habit.data

import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitApiImpl @Inject constructor(
    private val habitRepository: HabitRepository
) : HabitApi {
    
    override fun getHabits(): Flow<List<Habit>> {
        return habitRepository.getHabits()
    }
    
    override fun getActiveHabitsCount(): Flow<Int> {
        return habitRepository.getActiveHabitsCount()
    }
    
    override fun getTodayCheckedHabitsCount(): Flow<Int> {
        return habitRepository.getTodayCheckedHabitsCount()
    }
    
    override fun searchHabits(query: String): Flow<List<Habit>> {
        return habitRepository.searchHabits(query)
    }
    
    override fun getHabitsWithStreaks(): Flow<List<HabitWithStreak>> {
        return habitRepository.getHabitsWithStreaks()
    }
    
    override suspend fun createHabit(
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String,
        icon: String?
    ): Habit {
        return habitRepository.createHabit(title, description, period, target, color, icon).getOrThrow()
    }
    
    override suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String,
        icon: String?
    ) {
        habitRepository.updateHabit(habitId, title, description, period, target, color, icon).getOrThrow()
    }
    
    override suspend fun checkInHabit(habitId: String, date: LocalDate?) {
        val checkInDate = date ?: kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
        habitRepository.checkInHabit(habitId, checkInDate).getOrThrow()
    }
    
    override suspend fun deleteHabit(habitId: String) {
        habitRepository.deleteHabit(habitId).getOrThrow()
    }
}
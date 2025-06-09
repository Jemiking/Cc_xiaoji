package com.ccxiaoji.feature.habit.data

import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.habit.api.HabitItem
import com.ccxiaoji.feature.habit.api.HabitStatistics
import com.ccxiaoji.feature.habit.api.HabitNavigator
import com.ccxiaoji.feature.habit.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitApiImpl @Inject constructor(
    private val habitRepository: HabitRepository,
    private val habitNavigator: HabitNavigator
) : HabitApi {
    
    override suspend fun getTodayHabitStatistics(): HabitStatistics {
        val habits = habitRepository.getHabitsWithStreaks().first()
        val todayCheckedCount = habitRepository.getTodayCheckedHabitsCount().first()
        val longestStreak = habits.maxByOrNull { it.longestStreak }?.longestStreak ?: 0
        
        return HabitStatistics(
            totalHabits = habits.size,
            todayCheckedCount = todayCheckedCount,
            todayUncompletedCount = habits.size - todayCheckedCount,
            longestStreak = longestStreak
        )
    }
    
    override suspend fun getAllHabits(): List<HabitItem> {
        return habitRepository.getHabitsWithStreaks().first().map { habitWithStreak ->
            HabitItem(
                id = habitWithStreak.habit.id,
                title = habitWithStreak.habit.title,
                description = habitWithStreak.habit.description,
                period = habitWithStreak.habit.period,
                target = habitWithStreak.habit.target,
                currentStreak = habitWithStreak.currentStreak,
                checkedToday = false // TODO: 需要实现检查今日是否已打卡的逻辑
            )
        }
    }
    
    override fun getUncompletedHabitCount(): Flow<Int> {
        return habitRepository.getActiveHabitsCount().map { total ->
            val checked = habitRepository.getTodayCheckedHabitsCount().first()
            total - checked
        }
    }
    
    override fun getTodayCheckedCount(): Flow<Int> {
        return habitRepository.getTodayCheckedHabitsCount()
    }
    
    override suspend fun getLongestStreak(): Int {
        val habits = habitRepository.getHabitsWithStreaks().first()
        return habits.maxByOrNull { it.longestStreak }?.longestStreak ?: 0
    }
    
    override fun navigateToHabitList() {
        habitNavigator.navigateToHabitList()
    }
    
    override fun navigateToQuickCheckIn() {
        habitNavigator.navigateToQuickCheckIn()
    }
}
package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取习惯列表的用例
 */
class GetHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    /**
     * 获取所有习惯
     * @return 习惯列表的Flow
     */
    operator fun invoke(): Flow<List<Habit>> {
        return habitRepository.getHabits()
    }
    
    /**
     * 获取习惯及其连续记录信息
     * @return 带有连续记录信息的习惯列表
     */
    fun getHabitsWithStreaks(): Flow<List<HabitWithStreak>> {
        return habitRepository.getHabitsWithStreaks()
    }
    
    /**
     * 搜索习惯
     * @param query 搜索关键词
     * @return 匹配的习惯列表
     */
    fun searchHabits(query: String): Flow<List<Habit>> {
        return habitRepository.searchHabits(query)
    }
    
    /**
     * 获取今日已打卡习惯数量
     * @return 已打卡数量的Flow
     */
    fun getTodayCheckedCount(): Flow<Int> {
        return habitRepository.getTodayCheckedHabitsCount()
    }
    
    /**
     * 获取活跃习惯数量
     * @return 活跃习惯数量的Flow
     */
    fun getActiveHabitsCount(): Flow<Int> {
        return habitRepository.getActiveHabitsCount()
    }
}
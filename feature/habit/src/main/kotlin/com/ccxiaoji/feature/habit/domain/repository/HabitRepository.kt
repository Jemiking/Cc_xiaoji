package com.ccxiaoji.feature.habit.domain.repository

import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * 习惯领域层的仓库接口
 * 定义了所有习惯相关的数据操作
 */
interface HabitRepository {
    /**
     * 获取所有习惯
     */
    fun getHabits(): Flow<List<Habit>>
    
    /**
     * 获取活跃习惯数量
     */
    fun getActiveHabitsCount(): Flow<Int>
    
    /**
     * 获取今日已打卡习惯数量
     */
    fun getTodayCheckedHabitsCount(): Flow<Int>
    
    /**
     * 搜索习惯
     */
    fun searchHabits(query: String): Flow<List<Habit>>
    
    /**
     * 获取习惯及其连续记录信息
     */
    fun getHabitsWithStreaks(): Flow<List<HabitWithStreak>>
    
    /**
     * 创建习惯
     */
    suspend fun createHabit(
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String,
        icon: String?
    ): Habit
    
    /**
     * 更新习惯
     */
    suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String,
        icon: String?
    )
    
    /**
     * 习惯打卡
     */
    suspend fun checkInHabit(habitId: String, date: LocalDate)
    
    /**
     * 删除习惯
     */
    suspend fun deleteHabit(habitId: String)
}
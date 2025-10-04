package com.ccxiaoji.feature.habit.domain.repository

import com.ccxiaoji.common.base.BaseResult
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
    ): BaseResult<Habit>
    
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
    ): BaseResult<Unit>
    
    /**
     * 习惯打卡
     */
    suspend fun checkInHabit(habitId: String, date: LocalDate): BaseResult<Unit>
    
    /**
     * 删除习惯
     */
    suspend fun deleteHabit(habitId: String): BaseResult<Unit>
    
    /**
     * 根据ID获取习惯
     */
    suspend fun getHabitById(habitId: String): HabitWithStreak?

    /**
     * 更新习惯提醒配置（Phase 2）
     *
     * @param habitId 习惯ID
     * @param reminderEnabled 是否启用提醒（null=继承全局配置，true=强制启用，false=强制禁用）
     * @param reminderTime 提醒时间（HH:mm格式，null=使用全局配置时间）
     * @return BaseResult<Unit> 成功返回Unit，失败返回错误信息
     *
     * 示例：
     * - 晨间跑步：reminderTime = "07:00"
     * - 睡前阅读：reminderTime = "21:00"
     */
    suspend fun updateHabitReminder(
        habitId: String,
        reminderEnabled: Boolean? = null,
        reminderTime: String? = null
    ): BaseResult<Unit>

}
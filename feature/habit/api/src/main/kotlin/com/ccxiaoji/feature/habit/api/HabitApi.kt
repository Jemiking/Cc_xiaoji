package com.ccxiaoji.feature.habit.api

import kotlinx.coroutines.flow.Flow

/**
 * Habit模块对外暴露的API接口
 * 其他模块通过此接口访问Habit功能
 */
interface HabitApi {
    /**
     * 获取今日习惯统计信息
     */
    suspend fun getTodayHabitStatistics(): HabitStatistics
    
    /**
     * 获取所有习惯（用于数据导出）
     */
    suspend fun getAllHabits(): List<HabitItem>
    
    /**
     * 获取未完成的习惯数量
     */
    fun getUncompletedHabitCount(): Flow<Int>
    
    /**
     * 获取今日已打卡的习惯数量
     */
    fun getTodayCheckedCount(): Flow<Int>
    
    /**
     * 获取最长连续打卡天数
     */
    suspend fun getLongestStreak(): Int
    
    /**
     * 导航到习惯列表页面
     */
    fun navigateToHabitList()
    
    /**
     * 导航到快速打卡页面
     */
    fun navigateToQuickCheckIn()
    
    /**
     * 批量导入习惯数据
     * @param habits 习惯数据列表
     * @param conflictResolution 冲突处理策略
     * @return 导入结果
     */
    suspend fun importHabits(
        habits: List<Map<String, Any>>,
        conflictResolution: String = "SKIP"
    ): ImportHabitsResult
}

/**
 * 习惯数据导入结果
 */
data class ImportHabitsResult(
    val totalCount: Int = 0,
    val successCount: Int = 0,
    val skippedCount: Int = 0,
    val failedCount: Int = 0,
    val errors: List<String> = emptyList()
)

/**
 * 习惯统计信息
 */
data class HabitStatistics(
    val totalHabits: Int,
    val todayCheckedCount: Int,
    val todayUncompletedCount: Int,
    val longestStreak: Int
)

/**
 * 习惯数据模型（简化版）
 */
data class HabitItem(
    val id: String,
    val title: String,
    val description: String?,
    val period: String,
    val target: Int,
    val currentStreak: Int,
    val checkedToday: Boolean
)
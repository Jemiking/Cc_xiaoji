package com.ccxiaoji.feature.habit.api

/**
 * Habit模块导航接口
 * 在app模块中实现具体的导航逻辑
 */
interface HabitNavigator {
    /**
     * 导航到习惯列表页面
     */
    fun navigateToHabitList()
    
    /**
     * 导航到快速打卡页面
     */
    fun navigateToQuickCheckIn()
    
    /**
     * 导航到添加习惯页面
     */
    fun navigateToAddHabit()
    
    /**
     * 导航到习惯详情页面
     */
    fun navigateToHabitDetail(habitId: String)
}
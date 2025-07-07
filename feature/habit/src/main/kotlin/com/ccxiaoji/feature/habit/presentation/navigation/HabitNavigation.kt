package com.ccxiaoji.feature.habit.presentation.navigation

// 定义习惯模块的导航路由
object HabitNavigation {
    const val AddEditHabitRoute = "add_edit_habit?habitId={habitId}"
    
    fun addEditHabitRoute(habitId: String? = null) = if (habitId != null) {
        "add_edit_habit?habitId=$habitId"
    } else {
        "add_edit_habit"
    }
}
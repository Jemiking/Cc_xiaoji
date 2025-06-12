package com.ccxiaoji.feature.habit.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ccxiaoji.feature.habit.presentation.ui.HabitScreen

/**
 * Habit模块的导航路由
 */
object HabitRoute {
    const val HABIT_LIST = "habit"
    const val QUICK_CHECK_IN = "habit/quick_check_in"
    const val ADD_HABIT = "habit/add"
    const val HABIT_DETAIL = "habit/detail"
}

const val habitRoute = HabitRoute.HABIT_LIST

fun NavController.navigateToHabit() {
    navigate(habitRoute)
}

fun NavGraphBuilder.habitScreen() {
    composable(route = habitRoute) {
        HabitScreen()
    }
}
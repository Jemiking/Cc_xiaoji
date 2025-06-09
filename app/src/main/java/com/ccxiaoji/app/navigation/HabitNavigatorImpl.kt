package com.ccxiaoji.app.navigation

import androidx.navigation.NavController
import com.ccxiaoji.feature.habit.api.HabitNavigator
import com.ccxiaoji.feature.habit.presentation.navigation.HabitRoute
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HabitNavigator的实现
 * 在app模块中实现feature-habit模块的导航接口
 */
@Singleton
class HabitNavigatorImpl @Inject constructor() : HabitNavigator {
    
    private var navController: NavController? = null
    
    fun setNavController(navController: NavController) {
        this.navController = navController
    }
    
    override fun navigateToHabitList() {
        navController?.navigate(HabitRoute.HABIT_LIST)
    }
    
    override fun navigateToQuickCheckIn() {
        navController?.navigate(HabitRoute.QUICK_CHECK_IN)
    }
    
    override fun navigateToAddHabit() {
        navController?.navigate(HabitRoute.ADD_HABIT)
    }
    
    override fun navigateToHabitDetail(habitId: String) {
        navController?.navigate("${HabitRoute.HABIT_DETAIL}/$habitId")
    }
}
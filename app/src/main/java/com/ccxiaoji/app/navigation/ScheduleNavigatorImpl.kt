package com.ccxiaoji.app.navigation

import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.api.ScheduleNavigator
import com.ccxiaoji.feature.schedule.presentation.navigation.Screen
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 排班模块导航器的实现
 * 负责处理排班模块的导航逻辑
 */
@Singleton
class ScheduleNavigatorImpl @Inject constructor() : ScheduleNavigator {
    
    private var navController: NavController? = null
    
    /**
     * 设置导航控制器
     * 需要在Activity中初始化
     */
    fun setNavController(controller: NavController) {
        navController = controller
    }
    
    override fun navigateToScheduleHome() {
        navController?.navigate(Screen.Calendar.route)
    }
    
    override fun navigateToShiftManagement() {
        navController?.navigate(Screen.ShiftManage.route)
    }
    
    override fun navigateToScheduleEdit(date: LocalDate) {
        navController?.navigate(Screen.ScheduleEdit.createRoute(date.toString()))
    }
    
    override fun navigateToSchedulePattern() {
        navController?.navigate(Screen.SchedulePattern.route)
    }
    
    override fun navigateToScheduleStatistics() {
        navController?.navigate(Screen.ScheduleStatistics.route)
    }
    
    override fun navigateToScheduleExport() {
        navController?.navigate(Screen.Export.route)
    }
    
    override fun navigateToScheduleSettings() {
        navController?.navigate(Screen.Settings.route)
    }
    
    override fun navigateBack() {
        navController?.popBackStack()
    }
}
package com.ccxiaoji.feature.schedule.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ccxiaoji.feature.schedule.presentation.ui.calendar.CalendarScreen
import com.ccxiaoji.feature.schedule.presentation.ui.shift.ShiftManageScreen
import com.ccxiaoji.feature.schedule.presentation.ui.pattern.SchedulePatternScreen
import com.ccxiaoji.feature.schedule.presentation.ui.schedule.ScheduleEditScreen
import com.ccxiaoji.feature.schedule.presentation.ui.statistics.ScheduleStatisticsScreen
import com.ccxiaoji.feature.schedule.presentation.ui.export.ExportScreen
import com.ccxiaoji.feature.schedule.presentation.ui.settings.SettingsScreen
import com.ccxiaoji.feature.schedule.presentation.ui.settings.AboutScreen

/**
 * 导航路由定义
 */
sealed class Screen(val route: String) {
    object Calendar : Screen("schedule_calendar")
    object ShiftManage : Screen("schedule_shift_manage")
    object ScheduleEdit : Screen("schedule_edit/{date}") {
        fun createRoute(date: String) = "schedule_edit/$date"
    }
    object SchedulePattern : Screen("schedule_pattern")
    object ScheduleStatistics : Screen("schedule_statistics")
    object Export : Screen("schedule_export")
    object Settings : Screen("schedule_settings")
    object About : Screen("schedule_about")
}

/**
 * 排班模块导航主机
 */
@Composable
fun ScheduleNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Calendar.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 日历主界面
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateToShiftManage = {
                    navController.navigate(Screen.ShiftManage.route)
                },
                onNavigateToScheduleEdit = { date ->
                    navController.navigate(Screen.ScheduleEdit.createRoute(date.toString()))
                },
                onNavigateToSchedulePattern = {
                    navController.navigate(Screen.SchedulePattern.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.ScheduleStatistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        // 班次管理界面
        composable(Screen.ShiftManage.route) {
            ShiftManageScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 排班编辑界面
        composable(Screen.ScheduleEdit.route) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            ScheduleEditScreen(
                date = date,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 排班模式界面
        composable(Screen.SchedulePattern.route) {
            SchedulePatternScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 排班统计界面
        composable(Screen.ScheduleStatistics.route) {
            ScheduleStatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToExport = {
                    navController.navigate(Screen.Export.route)
                }
            )
        }
        
        // 数据导出界面
        composable(Screen.Export.route) {
            ExportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 设置界面
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                },
                onNavigateToShiftManage = {
                    navController.navigate(Screen.ShiftManage.route)
                }
            )
        }
        
        // 关于页面
        composable(Screen.About.route) {
            AboutScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
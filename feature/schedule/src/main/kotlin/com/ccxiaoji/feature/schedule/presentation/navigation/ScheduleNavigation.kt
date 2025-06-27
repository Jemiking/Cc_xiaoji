package com.ccxiaoji.feature.schedule.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ccxiaoji.feature.schedule.presentation.calendar.CalendarScreen
import com.ccxiaoji.feature.schedule.presentation.shift.ShiftManageScreen
import com.ccxiaoji.feature.schedule.presentation.pattern.SchedulePatternScreen
import com.ccxiaoji.feature.schedule.presentation.schedule.ScheduleEditScreen
import com.ccxiaoji.feature.schedule.presentation.statistics.ScheduleStatisticsScreen
import com.ccxiaoji.feature.schedule.presentation.export.ExportScreen
import com.ccxiaoji.feature.schedule.presentation.settings.SettingsScreen
import com.ccxiaoji.feature.schedule.presentation.settings.AboutScreen

/**
 * 导航路由定义
 */
sealed class Screen(val route: String) {
    object Calendar : Screen("calendar")
    object ShiftManage : Screen("shift_manage")
    object ScheduleEdit : Screen("schedule_edit/{date}") {
        fun createRoute(date: String) = "schedule_edit/$date"
    }
    object SchedulePattern : Screen("schedule_pattern")
    object ScheduleStatistics : Screen("schedule_statistics")
    object Export : Screen("export")
    object Settings : Screen("settings")
    object About : Screen("about")
}

/**
 * 排班模块导航主机
 */
@Composable
fun ScheduleNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Calendar.route
) {
    android.util.Log.d("ScheduleNavHost", "ScheduleNavHost called with startDestination: $startDestination")
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 日历主界面
        composable(Screen.Calendar.route) {
            android.util.Log.d("ScheduleNavHost", "Navigating to Calendar screen")
            CalendarScreen(
                onNavigateToShiftManage = {
                    android.util.Log.d("ScheduleNavHost", "Navigate to ShiftManage")
                    navController.navigate(Screen.ShiftManage.route)
                },
                onNavigateToScheduleEdit = { date ->
                    android.util.Log.d("ScheduleNavHost", "Navigate to ScheduleEdit: $date")
                    navController.navigate(Screen.ScheduleEdit.createRoute(date.toString()))
                },
                onNavigateToSchedulePattern = {
                    android.util.Log.d("ScheduleNavHost", "Navigate to SchedulePattern")
                    navController.navigate(Screen.SchedulePattern.route)
                },
                onNavigateToStatistics = {
                    android.util.Log.d("ScheduleNavHost", "Navigate to ScheduleStatistics")
                    navController.navigate(Screen.ScheduleStatistics.route)
                },
                onNavigateToSettings = {
                    android.util.Log.d("ScheduleNavHost", "Navigate to Settings")
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
            android.util.Log.d("ScheduleNavHost", "Navigating to ScheduleEdit screen with date: $date")
            ScheduleEditScreen(
                date = date,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 排班模式界面
        composable(Screen.SchedulePattern.route) {
            android.util.Log.d("ScheduleNavHost", "Navigating to SchedulePattern screen")
            SchedulePatternScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 排班统计界面
        composable(Screen.ScheduleStatistics.route) {
            android.util.Log.d("ScheduleNavHost", "Navigating to ScheduleStatistics screen")
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
            android.util.Log.d("ScheduleNavHost", "Navigating to Export screen")
            ExportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 设置界面
        composable(Screen.Settings.route) {
            android.util.Log.d("ScheduleNavHost", "Navigating to Settings screen")
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
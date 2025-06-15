package com.ccxiaoji.feature.schedule.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.ccxiaoji.feature.schedule.presentation.ui.calendar.CalendarScreen
import com.ccxiaoji.feature.schedule.presentation.ui.shift.ShiftManageScreen
import com.ccxiaoji.feature.schedule.presentation.ui.pattern.SchedulePatternScreen
import com.ccxiaoji.feature.schedule.presentation.ui.schedule.ScheduleEditScreen
import com.ccxiaoji.feature.schedule.presentation.ui.statistics.ScheduleStatisticsScreen
import com.ccxiaoji.feature.schedule.presentation.ui.export.ExportScreen
import com.ccxiaoji.feature.schedule.presentation.ui.settings.SettingsScreen
import com.ccxiaoji.feature.schedule.presentation.ui.settings.AboutScreen

/**
 * 排班模块导航图扩展函数
 * 符合主项目的导航架构
 */
fun NavGraphBuilder.scheduleGraph(
    navController: NavController
) {
    navigation(
        startDestination = Screen.Calendar.route,
        route = "schedule_graph"
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
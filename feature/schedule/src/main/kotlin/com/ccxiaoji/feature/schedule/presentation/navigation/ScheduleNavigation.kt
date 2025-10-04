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
import com.ccxiaoji.feature.schedule.presentation.settings.SettingsScreen
import com.ccxiaoji.feature.schedule.presentation.settings.AboutScreen
import com.ccxiaoji.feature.schedule.presentation.screen.ErrorScreen
import com.ccxiaoji.feature.schedule.presentation.screen.ClearDataScreen
import com.ccxiaoji.feature.schedule.presentation.screen.BackupLocationScreen
import com.ccxiaoji.feature.schedule.presentation.screen.WeekStartDayScreen
import com.ccxiaoji.feature.schedule.presentation.screen.TimePickerScreen
import com.ccxiaoji.feature.schedule.presentation.screen.CustomTimePickerScreen
import com.ccxiaoji.feature.schedule.presentation.screen.DatePickerScreen
import com.ccxiaoji.feature.schedule.presentation.debug.CalendarDebugScreen

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
    object Settings : Screen("settings")
    object About : Screen("about")
    object Error : Screen("error/{message}") {
        fun createRoute(message: String) = "error/${java.net.URLEncoder.encode(message, "UTF-8")}"
    }
    object ClearData : Screen("clear_data")
    object BackupLocation : Screen("backup_location")
    object WeekStartDay : Screen("week_start_day/{currentDay}") {
        fun createRoute(currentDay: String) = "week_start_day/$currentDay"
    }
    object TimePicker : Screen("time_picker/{initialTime}") {
        fun createRoute(initialTime: String) = "time_picker/${java.net.URLEncoder.encode(initialTime, "UTF-8")}"
    }
    object CustomTimePicker : Screen("custom_time_picker/{initialHour}/{initialMinute}") {
        fun createRoute(initialTime: java.time.LocalTime) = "custom_time_picker/${initialTime.hour}/${initialTime.minute}"
    }
    object DatePicker : Screen("date_picker/{initialDate}") {
        fun createRoute(initialDate: String?) = "date_picker/${initialDate ?: "null"}"
    }
    // UI Demo（仅调试使用）
    object UiDemo : Screen("ui_demo")
    // 排班调试器
    object CalendarDebug : Screen("calendar_debug")
    // 扁平化排班 Demo（无阴影布局）
    object CalendarFlatDemo : Screen("calendar_flat_demo")
    // UI设计风格Demo（包含5种设计风格对比）
    object StyleDemo : Screen("style_demo")
    // 排班主页重构 UI 设计（A3 单页演示）
    object HomeRedesignA3Demo : Screen("home_redesign_a3_demo")
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
                },
                onNavigateToDebug = {
                    android.util.Log.d("ScheduleNavHost", "Navigate to CalendarDebug")
                    navController.navigate(Screen.CalendarDebug.route)
                },
                onNavigateToFlatDemo = {
                    android.util.Log.d("ScheduleNavHost", "Navigate to CalendarFlatDemo")
                    navController.navigate(Screen.CalendarFlatDemo.route)
                },
                onNavigateToStyleDemo = {
                    android.util.Log.d("ScheduleNavHost", "Navigate to StyleDemo - function called")
                    try {
                        navController.navigate(Screen.StyleDemo.route)
                        android.util.Log.d("ScheduleNavHost", "Navigate to StyleDemo - navigate call completed")
                    } catch (e: Exception) {
                        android.util.Log.e("ScheduleNavHost", "Navigate to StyleDemo - error: ${e.message}")
                    }
                },
                onNavigateToHomeRedesignA3Demo = {
                    android.util.Log.d("ScheduleNavHost", "Navigate to HomeRedesignA3Demo")
                    navController.navigate(Screen.HomeRedesignA3Demo.route)
                },
                navController = navController
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
                },
                navController = navController
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
        
        // 错误页面
        composable(Screen.Error.route) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: "未知错误"
            ErrorScreen(
                errorMessage = message,
                navController = navController
            )
        }
        
        // 清除数据确认页面
        composable(Screen.ClearData.route) {
            ClearDataScreen(
                navController = navController
            )
        }
        
        // 备份位置选择页面
        composable(Screen.BackupLocation.route) {
            BackupLocationScreen(
                navController = navController
            )
        }
        
        // 周起始日选择页面
        composable(Screen.WeekStartDay.route) { backStackEntry ->
            val currentDayString = backStackEntry.arguments?.getString("currentDay") ?: "MONDAY"
            val currentDay = try {
                java.time.DayOfWeek.valueOf(currentDayString)
            } catch (e: IllegalArgumentException) {
                java.time.DayOfWeek.MONDAY
            }
            WeekStartDayScreen(
                currentWeekStartDay = currentDay,
                navController = navController
            )
        }
        
        // 时间选择页面
        composable(Screen.TimePicker.route) { backStackEntry ->
            val initialTimeEncoded = backStackEntry.arguments?.getString("initialTime") ?: "08:00"
            val initialTime = java.net.URLDecoder.decode(initialTimeEncoded, "UTF-8")
            TimePickerScreen(
                initialTime = initialTime,
                navController = navController
            )
        }
        
        // 自定义时间选择页面
        composable(Screen.CustomTimePicker.route) { backStackEntry ->
            val initialHour = backStackEntry.arguments?.getString("initialHour")?.toIntOrNull() ?: 8
            val initialMinute = backStackEntry.arguments?.getString("initialMinute")?.toIntOrNull() ?: 0
            val initialTime = java.time.LocalTime.of(initialHour, initialMinute)
            CustomTimePickerScreen(
                initialTime = initialTime,
                navController = navController
            )
        }
        
        // 日期选择页面
        composable(Screen.DatePicker.route) { backStackEntry ->
            val initialDate = backStackEntry.arguments?.getString("initialDate")
            DatePickerScreen(
                initialDate = if (initialDate == "null") null else initialDate,
                navController = navController
            )
        }

        // UI Demo 页面
        composable(Screen.UiDemo.route) {
            android.util.Log.d("UiDemoNav", "Entering UI Demo route")
            com.ccxiaoji.feature.schedule.presentation.demo.CalendarDemoApi.CalendarUiDemo(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 排班调试器页面
        composable(Screen.CalendarDebug.route) {
            android.util.Log.d("CalendarDebugNav", "Entering Calendar Debug route")
            CalendarDebugScreen(
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }

        // 扁平化排班 Demo 页面
        composable(Screen.CalendarFlatDemo.route) {
            com.ccxiaoji.feature.schedule.presentation.demo.CalendarDemoApi.CalendarFlatDemo(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // UI设计风格Demo页面
        composable(Screen.StyleDemo.route) {
            android.util.Log.d("ScheduleNavHost", "StyleDemo composable reached!")
            com.ccxiaoji.feature.schedule.presentation.demo.CalendarDemoApi.StyleDemo(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 排班主页 UI 设计 Demo（A3 单页演示）
        composable(Screen.HomeRedesignA3Demo.route) {
            com.ccxiaoji.feature.schedule.presentation.demo.CalendarDemoApi.HomeRedesignDemo(
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}

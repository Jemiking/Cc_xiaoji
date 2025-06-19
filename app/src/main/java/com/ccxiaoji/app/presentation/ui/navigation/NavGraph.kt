package com.ccxiaoji.app.presentation.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.ui.home.HomeScreen
import com.ccxiaoji.feature.todo.presentation.screen.TodoScreen
import com.ccxiaoji.feature.habit.presentation.screen.HabitScreen
import com.ccxiaoji.app.presentation.ui.profile.ProfileScreen
import com.ccxiaoji.app.presentation.ui.profile.DataExportScreen
import com.ccxiaoji.app.presentation.ui.profile.ThemeSettingsScreen
import com.ccxiaoji.app.presentation.ui.profile.NotificationSettingsScreen
import com.ccxiaoji.feature.ledger.api.LedgerApi

@Composable
fun NavGraph(
    navController: NavHostController,
    ledgerApi: LedgerApi,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLedger = { navController.navigate(Screen.Ledger.route) },
                onNavigateToTodo = { navController.navigate(Screen.Todo.route) },
                onNavigateToHabit = { navController.navigate(Screen.Habit.route) },
                onQuickAddTransaction = { navController.navigate(Screen.Ledger.route) },
                onNavigateToStatistics = { navController.navigate(StatisticsRoute.route) },
                onNavigateToSavingsGoal = { navController.navigate(SavingsGoalRoute.route) }
            )
        }
        
        composable(Screen.Ledger.route) {
            ledgerApi.getLedgerScreen(navController, null)
        }
        
        composable(LedgerWithAccountRoute.route) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            ledgerApi.getLedgerScreen(
                navController = navController,
                accountId = accountId
            )
        }
        
        composable(Screen.Todo.route) {
            TodoScreen()
        }
        
        composable(Screen.Habit.route) {
            HabitScreen()
        }
        
        composable(Screen.Schedule.route) {
            com.ccxiaoji.feature.schedule.presentation.calendar.CalendarScreen(
                onNavigateToShiftManage = {
                    navController.navigate(ShiftManageRoute.route)
                },
                onNavigateToScheduleEdit = { date ->
                    navController.navigate(ScheduleEditRoute.createRoute(date.toString()))
                },
                onNavigateToSchedulePattern = {
                    navController.navigate(SchedulePatternRoute.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(ScheduleStatisticsRoute.route)
                },
                onNavigateToSettings = {
                    navController.navigate(ScheduleSettingsRoute.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        // Detail screens
        composable(TransactionDetailRoute.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            ledgerApi.getTransactionDetailScreen(
                transactionId = transactionId,
                navController = navController
            )
        }
        
        composable(AccountManagementRoute.route) {
            ledgerApi.getAccountScreen(navController = navController)
        }
        
        composable(CreditCardRoute.route) {
            ledgerApi.getCreditCardScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAccount = { accountId ->
                    // 导航到账户详情页面（显示该信用卡的交易记录）
                    navController.navigate(LedgerWithAccountRoute.createRoute(accountId))
                }
            )
        }
        
        composable(CreditCardBillsRoute.route) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            ledgerApi.getCreditCardBillsScreen(
                accountId = accountId,
                navController = navController
            )
        }
        
        composable(CreditCardBillDetailRoute.route) { backStackEntry ->
            val billId = backStackEntry.arguments?.getString("billId") ?: ""
            // TODO: 实现账单详情界面
            PlaceholderScreen(title = "账单详情", navController = navController)
        }
        
        composable(CategoryManagementRoute.route) {
            ledgerApi.getCategoryManagementScreen(navController = navController)
        }
        
        composable(BudgetRoute.route) {
            ledgerApi.getBudgetScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(StatisticsRoute.route) {
            ledgerApi.getStatisticsScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(RecurringTransactionRoute.route) {
            ledgerApi.getRecurringTransactionScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(SavingsGoalRoute.route) {
            ledgerApi.getSavingsGoalScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { goalId ->
                    navController.navigate(SavingsGoalDetailRoute.createRoute(goalId))
                }
            )
        }
        
        composable(SavingsGoalDetailRoute.route) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")?.toLongOrNull() ?: 0L
            ledgerApi.getSavingsGoalDetailScreen(
                goalId = goalId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Settings screens - Placeholder implementations
        composable(LedgerSettingsRoute.route) {
            PlaceholderScreen(title = "记账设置", navController = navController)
        }
        
        composable(DataExportRoute.route) {
            DataExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(BatchOperationRoute.route) {
            PlaceholderScreen(title = "批量操作", navController = navController)
        }
        
        composable(ThemeSettingsRoute.route) {
            ThemeSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NotificationSettingsRoute.route) {
            NotificationSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(AppLockSettingsRoute.route) {
            PlaceholderScreen(title = "应用锁", navController = navController)
        }
        
        composable(PrivacySettingsRoute.route) {
            PlaceholderScreen(title = "隐私设置", navController = navController)
        }
        
        composable(HelpRoute.route) {
            PlaceholderScreen(title = "使用帮助", navController = navController)
        }
        
        composable(FeedbackRoute.route) {
            PlaceholderScreen(title = "意见反馈", navController = navController)
        }
        
        composable(AboutRoute.route) {
            PlaceholderScreen(title = "关于我们", navController = navController)
        }
        
        // Schedule module routes
        composable(ShiftManageRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.shift.ShiftManageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(ScheduleEditRoute.route) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            com.ccxiaoji.feature.schedule.presentation.schedule.ScheduleEditScreen(
                date = date,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(SchedulePatternRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.pattern.SchedulePatternScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(ScheduleStatisticsRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.statistics.ScheduleStatisticsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToExport = { navController.navigate(ScheduleExportRoute.route) }
            )
        }
        
        composable(ScheduleSettingsRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.settings.SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(ScheduleAboutRoute.route) },
                onNavigateToShiftManage = { navController.navigate(ShiftManageRoute.route) }
            )
        }
        
        composable(ScheduleExportRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.export.ExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(ScheduleAboutRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.settings.AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "功能开发中...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
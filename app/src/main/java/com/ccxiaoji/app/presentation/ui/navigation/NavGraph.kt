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
import com.ccxiaoji.app.presentation.ui.ledger.LedgerScreen
import com.ccxiaoji.feature.todo.presentation.screen.TodoScreen
import com.ccxiaoji.feature.habit.presentation.screen.HabitScreen
import com.ccxiaoji.app.presentation.ui.profile.ProfileScreen
import com.ccxiaoji.app.presentation.ui.ledger.TransactionDetailScreen
import com.ccxiaoji.app.presentation.ui.account.AccountScreen
import com.ccxiaoji.app.presentation.ui.category.CategoryManagementScreen
import com.ccxiaoji.app.presentation.ui.budget.BudgetScreen
import com.ccxiaoji.app.presentation.ui.statistics.StatisticsScreen
import com.ccxiaoji.app.presentation.ui.recurring.RecurringTransactionScreen
import com.ccxiaoji.app.presentation.ui.savings.SavingsGoalScreen
import com.ccxiaoji.app.presentation.ui.savings.SavingsGoalDetailScreen
import com.ccxiaoji.app.presentation.ui.profile.DataExportScreen
import com.ccxiaoji.app.presentation.ui.profile.ThemeSettingsScreen
import com.ccxiaoji.app.presentation.ui.profile.NotificationSettingsScreen
import com.ccxiaoji.app.presentation.ui.creditcard.CreditCardScreen
import com.ccxiaoji.app.presentation.ui.creditcard.CreditCardBillsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
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
            LedgerScreen(navController = navController)
        }
        
        composable(LedgerWithAccountRoute.route) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            LedgerScreen(
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
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        // Detail screens
        composable(TransactionDetailRoute.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            TransactionDetailScreen(
                transactionId = transactionId,
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        
        composable(AccountManagementRoute.route) {
            AccountScreen(navController = navController)
        }
        
        composable(CreditCardRoute.route) {
            CreditCardScreen(
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
            CreditCardBillsScreen(
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
            CategoryManagementScreen(navController = navController)
        }
        
        composable(BudgetRoute.route) {
            BudgetScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(StatisticsRoute.route) {
            StatisticsScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(RecurringTransactionRoute.route) {
            RecurringTransactionScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(SavingsGoalRoute.route) {
            SavingsGoalScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { goalId ->
                    navController.navigate(SavingsGoalDetailRoute.createRoute(goalId))
                }
            )
        }
        
        composable(SavingsGoalDetailRoute.route) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")?.toLongOrNull() ?: 0L
            SavingsGoalDetailScreen(
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
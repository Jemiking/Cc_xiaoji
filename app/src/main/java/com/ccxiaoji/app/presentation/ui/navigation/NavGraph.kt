package com.ccxiaoji.app.presentation.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.MainActivity
import com.ccxiaoji.app.presentation.ui.home.HomeScreen
import com.ccxiaoji.feature.todo.presentation.navigation.todoGraph
import com.ccxiaoji.feature.habit.presentation.navigation.habitRoute
import com.ccxiaoji.feature.habit.presentation.navigation.habitScreen
import com.ccxiaoji.app.presentation.ui.profile.ProfileScreen
import com.ccxiaoji.feature.ledger.presentation.ui.TransactionDetailScreen
import com.ccxiaoji.feature.ledger.presentation.ui.account.AccountScreen
import com.ccxiaoji.feature.ledger.presentation.ui.category.CategoryManagementScreen
import com.ccxiaoji.feature.ledger.presentation.navigation.budgetScreen
import com.ccxiaoji.app.presentation.ui.statistics.StatisticsScreen
import com.ccxiaoji.feature.ledger.presentation.ui.savings.SavingsGoalScreen
import com.ccxiaoji.feature.ledger.presentation.ui.savings.SavingsGoalDetailScreen
import com.ccxiaoji.app.presentation.ui.profile.DataExportScreen
import com.ccxiaoji.app.presentation.ui.profile.ThemeSettingsScreen
import com.ccxiaoji.app.presentation.ui.profile.NotificationSettingsScreen

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
            val ledgerNavigator = (LocalContext.current as? MainActivity)?.ledgerNavigator
            if (ledgerNavigator != null) {
                com.ccxiaoji.feature.ledger.presentation.ui.LedgerScreen(
                    navigator = ledgerNavigator
                )
            }
        }
        
        composable(LedgerWithAccountRoute.route) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            val ledgerNavigator = (LocalContext.current as? MainActivity)?.ledgerNavigator
            if (ledgerNavigator != null) {
                // TODO: Handle account-specific ledger view
                com.ccxiaoji.feature.ledger.presentation.ui.LedgerScreen(
                    navigator = ledgerNavigator
                )
            }
        }
        
        // Todo模块导航图
        todoGraph(navController)
        
        // Habit模块导航图
        habitScreen()
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        // Detail screens
        composable(TransactionDetailRoute.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            val ledgerNavigator = (LocalContext.current as? MainActivity)?.ledgerNavigator
            if (ledgerNavigator != null) {
                TransactionDetailScreen(
                    transactionId = transactionId,
                    navigator = ledgerNavigator
                )
            }
        }
        
        composable(AccountManagementRoute.route) {
            val ledgerNavigator = (LocalContext.current as? MainActivity)?.ledgerNavigator
            if (ledgerNavigator != null) {
                AccountScreen(navigator = ledgerNavigator)
            }
        }
        
        composable(CreditCardRoute.route) {
            val ledgerNavigator = (LocalContext.current as? MainActivity)?.ledgerNavigator
                ?: object : com.ccxiaoji.feature.ledger.api.LedgerNavigator {
                    override fun navigateToLedger() {}
                    override fun navigateToQuickAdd() {}
                    override fun navigateToStatistics() {}
                    override fun navigateToAccounts() {}
                    override fun navigateToCategories() {}
                    override fun navigateToTransactionDetail(transactionId: String) {}
                    override fun navigateToCreditCards() {}
                    override fun navigateToCreditCardBills(accountId: String) {
                        navController.navigate(CreditCardBillsRoute.createRoute(accountId))
                    }
                    override fun navigateToBudget() {}
                    override fun navigateToRecurringTransactions() {}
                    override fun navigateToSavingsGoals() {}
                    override fun navigateToSavingsGoalDetail(goalId: Long) {}
                    override fun navigateToTransactionsByAccount(accountId: String) {
                        navController.navigate(LedgerWithAccountRoute.createRoute(accountId))
                    }
                    override fun navigateUp() { navController.popBackStack() }
                }
            com.ccxiaoji.feature.ledger.presentation.ui.creditcard.CreditCardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAccount = { accountId ->
                    navController.navigate(LedgerWithAccountRoute.createRoute(accountId))
                },
                ledgerNavigator = ledgerNavigator
            )
        }
        
        composable(CreditCardBillsRoute.route) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            com.ccxiaoji.feature.ledger.presentation.ui.creditcard.CreditCardBillsScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBillDetail = { billId ->
                    navController.navigate(CreditCardBillDetailRoute.createRoute(billId))
                }
            )
        }
        
        composable(CreditCardBillDetailRoute.route) { backStackEntry ->
            val billId = backStackEntry.arguments?.getString("billId") ?: ""
            // TODO: 实现账单详情界面
            PlaceholderScreen(title = "账单详情", navController = navController)
        }
        
        composable(CategoryManagementRoute.route) {
            val ledgerNavigator = (LocalContext.current as? MainActivity)?.ledgerNavigator
            if (ledgerNavigator != null) {
                CategoryManagementScreen(ledgerNavigator = ledgerNavigator)
            }
        }
        
        // 预算管理（使用feature-ledger模块）
        composable(com.ccxiaoji.feature.ledger.presentation.navigation.BUDGET_ROUTE) {
            val ledgerNavigator = (LocalContext.current as? MainActivity)?.ledgerNavigator
                ?: object : com.ccxiaoji.feature.ledger.api.LedgerNavigator {
                    override fun navigateToLedger() {}
                    override fun navigateToQuickAdd() {}
                    override fun navigateToStatistics() {}
                    override fun navigateToAccounts() {}
                    override fun navigateToCategories() {}
                    override fun navigateToTransactionDetail(transactionId: String) {}
                    override fun navigateToCreditCards() {}
                    override fun navigateToCreditCardBills(accountId: String) {
                        navController.navigate(CreditCardBillsRoute.createRoute(accountId))
                    }
                    override fun navigateToBudget() {}
                    override fun navigateToRecurringTransactions() {}
                    override fun navigateToSavingsGoals() {}
                    override fun navigateToSavingsGoalDetail(goalId: Long) {
                        navController.navigate(SavingsGoalDetailRoute.createRoute(goalId))
                    }
                    override fun navigateToTransactionsByAccount(accountId: String) {}
                    override fun navigateUp() { navController.popBackStack() }
                }
            com.ccxiaoji.feature.ledger.presentation.ui.budget.BudgetScreen(navigator = ledgerNavigator)
        }
        
        composable(StatisticsRoute.route) {
            StatisticsScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(RecurringTransactionRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.ui.recurring.RecurringTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(SavingsGoalRoute.route) {
            SavingsGoalScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { goalId ->
                    navController.navigate("${SavingsGoalDetailRoute.route}/$goalId")
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
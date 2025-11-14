package com.ccxiaoji.app.presentation.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.ui.home.HomeScreen
import com.ccxiaoji.app.presentation.ui.home.ModularHomeScreen
import com.ccxiaoji.feature.todo.presentation.screen.TodoScreen
import com.ccxiaoji.feature.todo.presentation.screen.DatePickerScreen as TodoDatePickerScreen
import com.ccxiaoji.feature.habit.presentation.screen.HabitScreen
import com.ccxiaoji.app.presentation.ui.profile.ProfileScreen
import com.ccxiaoji.app.presentation.ui.profile.ThemeSettingsScreen
import com.ccxiaoji.app.presentation.ui.profile.NotificationSettingsScreen
import com.ccxiaoji.app.presentation.notification.NotificationHistoryScreen
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.presentation.screen.import.LedgerImportScreen
import com.ccxiaoji.feature.ledger.presentation.screen.import.QianjiImportScreen
import com.ccxiaoji.feature.plan.api.PlanApi
import com.ccxiaoji.app.presentation.ui.navigation.*

@Composable
fun NavGraph(
    navController: NavHostController,
    ledgerApi: LedgerApi,
    planApi: PlanApi,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            // 使用新的模块化首页
            ModularHomeScreen(
                navController = navController
            )
            
            // 保留原首页代码，方便回滚
            /*
            HomeScreen(
                onNavigateToLedger = { navController.navigate(Screen.Ledger.route) },
                onNavigateToTodo = { navController.navigate(Screen.Todo.route) },
                onNavigateToHabit = { navController.navigate(Screen.Habit.route) },
                onNavigateToPlan = { navController.navigate(PlanRoute.route) },
                onQuickAddTransaction = { navController.navigate(Screen.Ledger.route) },
                onNavigateToStatistics = { navController.navigate(StatisticsRoute.route) },
                onNavigateToSavingsGoal = { navController.navigate(SavingsGoalRoute.route) }
            )
            */
        }
        
        composable(Screen.Ledger.route) {
            ledgerApi.getLedgerScreen(
                navController = navController,
                accountId = null,
                onBack = { navController.smartBackToHome() }
            )
        }
        
        composable(LedgerWithAccountRoute.route) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            ledgerApi.getLedgerScreen(
                navController = navController,
                accountId = accountId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Todo.route) {
            TodoScreen(
                onNavigateToAddTask = {
                    navController.navigate(AddEditTaskRoute.createRoute())
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(AddEditTaskRoute.createRoute(taskId))
                }
            )
        }
        
        composable(Screen.Habit.route) {
            HabitScreen(
                onNavigateToAddHabit = {
                    navController.navigate(AddEditHabitRoute.createRoute())
                },
                onNavigateToEditHabit = { habitId ->
                    navController.navigate(AddEditHabitRoute.createRoute(habitId))
                }
            )
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
                },
                onNavigateToDebug = { navController.navigate("CalendarDebug") },
                onNavigateToFlatDemo = { /* demo已移除 */ },
                onNavigateToStyleDemo = { /* demo已移除 */ },
                onNavigateToHomeRedesignA3Demo = { /* demo已移除 */ },
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                navController = navController
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
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(
            route = DeleteTransactionRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("transactionId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            com.ccxiaoji.feature.ledger.presentation.screen.transaction.delete.DeleteTransactionScreen(
                transactionId = transactionId,
                navController = navController
            )
        }
        
        composable(AccountManagementRoute.route) {
            ledgerApi.getAccountScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        // TODO: 信用卡功能暂时禁用 - 相关屏幕组件已删除
        /*
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
        */
        
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
            ledgerApi.getCategoryManagementScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }

        // 卡片管理（非资金账户的卡片信息）
        composable(CardManagementRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.card.CardManagementScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }

        composable(
            route = AddEditCardRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("cardId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")
            com.ccxiaoji.feature.ledger.presentation.screen.card.AddEditCardScreen(
                navController = navController,
                cardId = cardId,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(BudgetRoute.route) {
            ledgerApi.getBudgetScreen(
                onNavigateBack = { navController.smartBackToLedger() },
                onNavigateToAddEditBudget = { categoryId ->
                    navController.navigate(AddEditBudgetRoute.createRoute(categoryId))
                }
            )
        }
        
        composable(
            route = AddEditBudgetRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("categoryId") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            com.ccxiaoji.feature.ledger.presentation.screen.budget.AddEditBudgetScreen(
                categoryId = categoryId,
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(SelectCategoryRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.budget.CategorySelectionScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(
            route = AddTransactionRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("transactionId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                // 预填参数（来自自动记账 DeepLink）
                // 注意：IntType 不支持可空，使用哨兵默认值表示“未提供”
                androidx.navigation.navArgument("amountCents") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = Int.MIN_VALUE
                },
                androidx.navigation.navArgument("direction") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("merchant") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("categoryId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("note") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            deepLinks = listOf(
                androidx.navigation.navDeepLink {
                    uriPattern = "ccxiaoji://app/add_transaction?amountCents={amountCents}&direction={direction}&merchant={merchant}&accountId={accountId}&categoryId={categoryId}&note={note}"
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            com.ccxiaoji.feature.ledger.presentation.screen.transaction.TransactionEditorScreen(
                navController = navController,
                transactionId = transactionId,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(
            route = EditTransactionRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("transactionId") {
                    type = androidx.navigation.NavType.StringType
                }
            ),
            deepLinks = listOf(
                androidx.navigation.navDeepLink {
                    uriPattern = "ccxiaoji://app/edit_transaction/{transactionId}"
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            println("🔍 [NavGraph] EditTransactionRoute 被导航！")
            println("   - 接收到的transactionId: '$transactionId'")
            println("   - backStackEntry: $backStackEntry")
            println("   - 准备启动TransactionEditorScreen编辑模式")

            com.ccxiaoji.feature.ledger.presentation.screen.transaction.TransactionEditorScreen(
                transactionId = transactionId,
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(AddAccountRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.account.AccountEditorScreen(
                navController = navController,
                accountId = null,  // 新增模式
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(
            route = EditAccountRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") { 
                    type = androidx.navigation.NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            com.ccxiaoji.feature.ledger.presentation.screen.account.AccountEditorScreen(
                accountId = accountId,  // 编辑模式
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(TransferRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.account.TransferScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(
            route = AddEditTaskRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("taskId") { 
                    type = androidx.navigation.NavType.StringType 
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            com.ccxiaoji.feature.todo.presentation.screen.AddEditTaskScreen(
                taskId = taskId,
                navController = navController
            )
        }
        
        composable(
            route = TodoDatePickerRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("initialMillis") { 
                    type = androidx.navigation.NavType.LongType
                    defaultValue = 0L  // 使用0表示无初始日期
                }
            )
        ) { backStackEntry ->
            val initialMillis = backStackEntry.arguments?.getLong("initialMillis") ?: 0L
            TodoDatePickerScreen(
                initialDateMillis = if (initialMillis == 0L) null else initialMillis,
                navController = navController
            )
        }
        
        composable(
            route = AddEditHabitRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("habitId") { 
                    type = androidx.navigation.NavType.StringType 
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            com.ccxiaoji.feature.habit.presentation.screen.AddEditHabitScreen(
                habitId = habitId,
                navController = navController
            )
        }
        
        composable(StatisticsRoute.route) {
            ledgerApi.getStatisticsScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(AssetOverviewRoute.route) {
            ledgerApi.getAssetOverviewScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(UnifiedAccountAssetRoute.route) {
            ledgerApi.getUnifiedAccountAssetScreen(
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }
        
        composable(RecurringTransactionRoute.route) {
            ledgerApi.getRecurringTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddEdit = { recurringId ->
                    navController.navigate(AddEditRecurringTransactionRoute.createRoute(recurringId))
                }
            )
        }
        
        composable(SavingsGoalRoute.route) {
            ledgerApi.getSavingsGoalScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { goalId ->
                    navController.navigate(SavingsGoalDetailRoute.createRoute(goalId))
                },
                onNavigateToAddGoal = {
                    navController.navigate(AddSavingsGoalRoute.route)
                }
            )
        }
        
        composable(SavingsGoalDetailRoute.route) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")?.toLongOrNull() ?: 0L
            ledgerApi.getSavingsGoalDetailScreen(
                goalId = goalId,
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditGoal = { id ->
                    navController.navigate(EditSavingsGoalRoute.createRoute(id.toString()))
                },
                onNavigateToContribution = { id ->
                    navController.navigate(ContributionRoute.createRoute(id.toString()))
                }
            )
        }
        
        // Settings screens - Placeholder implementations
        composable(LedgerSettingsRoute.route) {
            ledgerApi.getLedgerSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategory = { navController.navigate(CategoryManagementRoute.route) },
                onNavigateToAccount = { navController.navigate(AccountManagementRoute.route) },
                onNavigateToBudget = { navController.navigate(BudgetRoute.route) },
                onNavigateToRecurring = { navController.navigate(RecurringTransactionRoute.route) },
                onNavigateToCurrencySelection = { navController.navigate(CurrencySelectionRoute.route) },
                onNavigateToAccountSelection = { navController.navigate(AccountSelectionRoute.route) },
                onNavigateToReminderSettings = { navController.navigate(ReminderSettingsRoute.route) },
                onNavigateToHomeDisplaySettings = { navController.navigate(HomeDisplaySettingsRoute.route) },
                onNavigateToUIStyleSettings = { navController.navigate(LedgerUIStyleRoute.route) },
                onNavigateToLedgerBookManagement = { navController.navigate(LedgerBookManagementRoute.route) },
                onNavigateToPermissionGuide = { navController.navigate(PermissionGuideRoute.route) },
                onNavigateToAutoLedgerDebug = {
                    if (com.ccxiaoji.app.BuildConfig.DEBUG) {
                        navController.navigate(AutoLedgerDebugRoute.route)
                    }
                },
                onNavigateToAutoLedgerSettings = { navController.navigate(AutoLedgerSettingsRoute.route) },
                navController = navController
            )
        }
        
        composable(LedgerImportRoute.route) {
            LedgerImportScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQianjiImport = { navController.navigate(QianjiImportRoute.route) }
            )
        }
        
        composable(QianjiImportRoute.route) {
            QianjiImportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ThemeSettingsRoute.route) {
            ThemeSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NotificationSettingsRoute.route) {
            NotificationSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(NotificationHistoryRoute.route) }
            )
        }

        composable(AboutRoute.route) {
            com.ccxiaoji.app.presentation.ui.profile.AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(PermissionManagementRoute.route) {
            com.ccxiaoji.app.presentation.ui.profile.PermissionManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Notification routes
        composable(NotificationHistoryRoute.route) {
            NotificationHistoryScreen()
        }

        // Schedule module routes
        composable(ShiftManageRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.shift.ShiftManageScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditShift = { shiftId ->
                    navController.navigate(EditShiftRoute.createRoute(shiftId))
                }
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(ScheduleSettingsRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.settings.SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(ScheduleAboutRoute.route) },
                onNavigateToShiftManage = { navController.navigate(ShiftManageRoute.route) }
            )
        }
        
        composable(ScheduleAboutRoute.route) {
            com.ccxiaoji.feature.schedule.presentation.settings.AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("CalendarDebug") {
            com.ccxiaoji.feature.schedule.presentation.debug.CalendarDebugScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 已移除：快速班次选择页面
        
        // 自定义时间选择页面 - 修复EditShiftScreen导航问题
        composable(
            route = "custom_time_picker/{initialHour}/{initialMinute}",
            arguments = listOf(
                androidx.navigation.navArgument("initialHour") { 
                    type = androidx.navigation.NavType.IntType 
                },
                androidx.navigation.navArgument("initialMinute") { 
                    type = androidx.navigation.NavType.IntType 
                }
            )
        ) { backStackEntry ->
            val initialHour = backStackEntry.arguments?.getInt("initialHour") ?: 8
            val initialMinute = backStackEntry.arguments?.getInt("initialMinute") ?: 0
            val initialTime = java.time.LocalTime.of(initialHour, initialMinute)
            com.ccxiaoji.feature.schedule.presentation.screen.CustomTimePickerScreen(
                initialTime = initialTime,
                navController = navController
            )
        }

        
        // Plan module route
        composable(PlanRoute.route) {
            planApi.getPlanScreen()
        }
        
        composable(
            route = EditShiftRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("shiftId") { 
                    type = androidx.navigation.NavType.StringType 
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val shiftId = backStackEntry.arguments?.getString("shiftId")?.toLongOrNull()
            com.ccxiaoji.feature.schedule.presentation.screen.EditShiftScreen(
                shiftId = shiftId,
                navController = navController
            )
        }
        
        composable(
            route = UpdateProgressRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("planId") { 
                    type = androidx.navigation.NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            com.ccxiaoji.feature.plan.presentation.screen.UpdateProgressScreen(
                planId = planId,
                navController = navController
            )
        }
        
        composable(
            route = AddEditMilestoneRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("planId") { 
                    type = androidx.navigation.NavType.StringType 
                },
                androidx.navigation.navArgument("milestoneId") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            val milestoneId = backStackEntry.arguments?.getString("milestoneId")
            com.ccxiaoji.feature.plan.presentation.screen.AddEditMilestoneScreen(
                planId = planId,
                milestoneId = milestoneId,
                navController = navController
            )
        }
        
        composable(AdvancedFilterRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.AdvancedFilterScreen(
                navController = navController
            )
        }
        
        composable(
            route = AddCategoryRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("categoryType") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val categoryType = backStackEntry.arguments?.getString("categoryType") ?: "EXPENSE"
            // 使用统一的 CategoryEditorScreen，categoryId 为 null 表示新增模式
            com.ccxiaoji.feature.ledger.presentation.screen.category.CategoryEditorScreen(
                categoryId = null,
                categoryType = categoryType,
                navController = navController
            )
        }

        composable(
            route = EditCategoryRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("categoryId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            // 使用统一的 CategoryEditorScreen，有 categoryId 表示编辑模式
            com.ccxiaoji.feature.ledger.presentation.screen.category.CategoryEditorScreen(
                categoryId = categoryId,
                categoryType = null,
                navController = navController
            )
        }
        
        // TODO: 信用卡功能暂时禁用 - 相关屏幕组件已删除
        /*
        composable(AddCreditCardRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.creditcard.AddCreditCardScreen(
                navController = navController
            )
        }
        */
        
        composable(
            route = CreditCardDetailRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") { 
                    type = androidx.navigation.NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            com.ccxiaoji.feature.ledger.presentation.screen.creditcard.CreditCardDetailScreen(
                accountId = accountId,
                navController = navController,
                onNavigateToTransactions = {
                    navController.navigate(LedgerWithAccountRoute.createRoute(accountId))
                },
                onNavigateToBills = {
                    navController.navigate(CreditCardBillsRoute.createRoute(accountId))
                },
                onNavigateToPaymentHistory = {
                    navController.navigate(PaymentHistoryRoute.createRoute(accountId))
                },
                onNavigateToSettings = {
                    navController.navigate(CreditCardSettingsRoute.createRoute(accountId))
                }
            )
        }
        
        composable(
            route = PaymentRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") { 
                    type = androidx.navigation.NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            com.ccxiaoji.feature.ledger.presentation.screen.creditcard.PaymentScreen(
                accountId = accountId,
                navController = navController
            )
        }
        
        // TODO: 信用卡功能暂时禁用 - 相关屏幕组件已删除
        /*
        composable(
            route = EditCreditCardRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") { 
                    type = androidx.navigation.NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            com.ccxiaoji.feature.ledger.presentation.screen.creditcard.EditCreditCardScreen(
                accountId = accountId,
                navController = navController
            )
        }
        */
        
        composable(
            route = PaymentHistoryRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") { 
                    type = androidx.navigation.NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            com.ccxiaoji.feature.ledger.presentation.screen.creditcard.PaymentHistoryScreen(
                accountId = accountId,
                navController = navController
            )
        }
        
        composable(AddSavingsGoalRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.savings.AddEditSavingsGoalScreen(
                navController = navController
            )
        }
        
        composable(
            route = EditSavingsGoalRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("goalId") { 
                    type = androidx.navigation.NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")
            com.ccxiaoji.feature.ledger.presentation.screen.savings.AddEditSavingsGoalScreen(
                navController = navController,
                goalId = goalId
            )
        }
        
        composable(
            route = ContributionRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("goalId") { 
                    type = androidx.navigation.NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
            com.ccxiaoji.feature.ledger.presentation.screen.savings.ContributionScreen(
                navController = navController,
                goalId = goalId
            )
        }
        
        composable(
            route = AddEditRecurringTransactionRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("recurringId") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val recurringId = backStackEntry.arguments?.getString("recurringId")
            com.ccxiaoji.feature.ledger.presentation.screen.recurring.AddEditRecurringTransactionScreen(
                navController = navController,
                recurringId = recurringId
            )
        }
        
        composable(CurrencySelectionRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.settings.CurrencySelectionScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(AccountSelectionRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.settings.AccountSelectionScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(ReminderSettingsRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.settings.ReminderSettingsScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }

        composable(AutoLedgerSettingsRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.settings.AutoLedgerSettingsScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() },
                onNavigateToPermissionGuide = { navController.navigate(PermissionGuideRoute.route) }
            )
        }

        if (com.ccxiaoji.app.BuildConfig.DEBUG) {
            composable(AutoLedgerDeveloperSettingsRoute.route) {
                com.ccxiaoji.feature.ledger.presentation.screen.settings.AutoLedgerDeveloperSettingsScreen(
                    navController = navController,
                    onNavigateBack = { navController.smartBackToLedger() }
                )
            }
        }
        
        composable(HomeDisplaySettingsRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.settings.HomeDisplaySettingsScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(PermissionGuideRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.settings.PermissionGuideScreen(
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        if (com.ccxiaoji.app.BuildConfig.DEBUG) {
            composable(AutoLedgerDebugRoute.route) {
                ledgerApi.getAutoLedgerDebugScreen(
                    navController = navController
                )
            }
        }
        
        composable(LedgerUIStyleRoute.route) {
            com.ccxiaoji.feature.ledger.presentation.screen.settings.LedgerUIStyleScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(
            route = BatchUpdateCategoryRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("selectedCount") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val selectedCount = backStackEntry.arguments?.getInt("selectedCount") ?: 0
            com.ccxiaoji.feature.ledger.presentation.screen.batch.BatchUpdateCategoryScreen(
                navController = navController
            )
        }
        
        composable(
            route = BatchDeleteRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("selectedCount") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val selectedCount = backStackEntry.arguments?.getInt("selectedCount") ?: 0
            com.ccxiaoji.feature.ledger.presentation.screen.batch.BatchDeleteScreen(
                navController = navController
            )
        }
        
        composable(
            route = BatchUpdateAccountRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("selectedCount") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val selectedCount = backStackEntry.arguments?.getInt("selectedCount") ?: 0
            com.ccxiaoji.feature.ledger.presentation.screen.batch.BatchUpdateAccountScreen(
                navController = navController
            )
        }
        
        composable(
            route = DeleteGoalRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("goalId") {
                    type = androidx.navigation.NavType.LongType
                },
                androidx.navigation.navArgument("goalName") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
            val goalName = backStackEntry.arguments?.getString("goalName") ?: ""
            com.ccxiaoji.feature.ledger.presentation.screen.savings.DeleteGoalScreen(
                navController = navController
            )
        }
        
        composable(
            route = FilterTransactionRoute.route,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            com.ccxiaoji.feature.ledger.presentation.screen.transaction.FilterTransactionScreen(
                navController = navController,
                onNavigateBack = { navController.smartBackToLedger() }
            )
        }
        
        composable(LogoutConfirmationRoute.route) {
            com.ccxiaoji.app.presentation.ui.profile.LogoutConfirmationScreen(
                navController = navController
            )
        }
        
        // 统一数据导出中心
        composable(DataExportRoute.route) {
            com.ccxiaoji.app.presentation.ui.export.UnifiedExportScreen(
                navController = navController
            )
        }
        
        // Module Management
        composable(ModuleManagementRoute.route) {
            com.ccxiaoji.app.presentation.ui.settings.ModuleManagementScreen(
                navController = navController
            )
        }
        
        
        
        
        
        
        
        
        
        
        // 记账簿管理页面
        composable(LedgerBookManagementRoute.route) {
            ledgerApi.getLedgerManagementScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLedgerDetail = { ledgerId ->
                    // TODO: 添加记账簿详情页面导航
                },
                onNavigateToAddLedger = {
                    // TODO: 添加新建记账簿页面导航
                }
            )
        }
        
        // Style Catalog Demo - only when explicitly enabled
        if (com.ccxiaoji.app.BuildConfig.SHOW_STYLE_DEMO) {
            composable(StyleCatalogDemoRoute.route) {
                // Launch the demo activity via reflection to avoid release build references
                val context = androidx.compose.ui.platform.LocalContext.current
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    try {
                        val clazz = Class.forName(
                            "com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.StyleCatalogDemoActivity"
                        )
                        val intent = android.content.Intent(context, clazz)
                        context.startActivity(intent)
                    } catch (e: ClassNotFoundException) {
                        android.widget.Toast.makeText(
                            context,
                            "Demo 不可用",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } finally {
                        navController.popBackStack()
                    }
                }
            }
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

package com.ccxiaoji.app.presentation.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.ccxiaoji.app.R

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        titleRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    object Ledger : Screen(
        route = "ledger",
        titleRes = R.string.nav_ledger,
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet
    )
    
    object Todo : Screen(
        route = "todo",
        titleRes = R.string.nav_todo,
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    )
    
    object Habit : Screen(
        route = "habit",
        titleRes = R.string.nav_habit,
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents
    )
    
    object Schedule : Screen(
        route = "schedule",
        titleRes = R.string.nav_schedule,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )
    
    object Profile : Screen(
        route = "profile",
        titleRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Ledger,
    Screen.Todo,
    Screen.Habit,
    Screen.Schedule,
    Screen.Profile
)

// Detail screens routes
object TransactionDetailRoute {
    const val route = "transaction_detail/{transactionId}"
    fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
}

object LedgerWithAccountRoute {
    const val route = "ledger_with_account/{accountId}"
    fun createRoute(accountId: String) = "ledger_with_account/$accountId"
}

object AccountManagementRoute {
    const val route = "account_management"
}

object CreditCardRoute {
    const val route = "credit_card"
}

object CreditCardBillsRoute {
    const val route = "credit_card_bills/{accountId}"
    fun createRoute(accountId: String) = "credit_card_bills/$accountId"
}

object CreditCardBillDetailRoute {
    const val route = "credit_card_bill_detail/{billId}"
    fun createRoute(billId: String) = "credit_card_bill_detail/$billId"
}

object CategoryManagementRoute {
    const val route = "category_management"
}

object BudgetRoute {
    const val route = "budget"
}

object StatisticsRoute {
    const val route = "statistics"
}

object RecurringTransactionRoute {
    const val route = "recurring_transaction"
}

object SavingsGoalRoute {
    const val route = "savings_goal"
}

object SavingsGoalDetailRoute {
    const val route = "savings_goal_detail/{goalId}"
    fun createRoute(goalId: Long) = "savings_goal_detail/$goalId"
}

// Settings routes
object LedgerSettingsRoute {
    const val route = "ledger_settings"
}

object DataExportRoute {
    const val route = "data_export"
}

object BatchOperationRoute {
    const val route = "batch_operation"
}

object ThemeSettingsRoute {
    const val route = "theme_settings"
}

object NotificationSettingsRoute {
    const val route = "notification_settings"
}

object AppLockSettingsRoute {
    const val route = "app_lock_settings"
}

object PrivacySettingsRoute {
    const val route = "privacy_settings"
}

object HelpRoute {
    const val route = "help"
}

object FeedbackRoute {
    const val route = "feedback"
}

object AboutRoute {
    const val route = "about"
}

// Schedule module routes
object ShiftManageRoute {
    const val route = "shift_manage"
}

object ScheduleEditRoute {
    const val route = "schedule_edit/{date}"
    fun createRoute(date: String) = "schedule_edit/$date"
}

object SchedulePatternRoute {
    const val route = "schedule_pattern"
}

object ScheduleStatisticsRoute {
    const val route = "schedule_statistics"
}

object ScheduleSettingsRoute {
    const val route = "schedule_settings"
}

object ScheduleExportRoute {
    const val route = "schedule_export"
}

object ScheduleAboutRoute {
    const val route = "schedule_about"
}
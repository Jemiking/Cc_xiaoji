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

object DeleteTransactionRoute {
    const val route = "delete_transaction/{transactionId}"
    fun createRoute(transactionId: String) = "delete_transaction/$transactionId"
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

object AddEditBudgetRoute {
    const val route = "add_edit_budget?categoryId={categoryId}"
    fun createRoute(categoryId: String? = null) = if (categoryId != null) {
        "add_edit_budget?categoryId=$categoryId"
    } else {
        "add_edit_budget"
    }
}

object SelectCategoryRoute {
    const val route = "select_category"
}

object AddTransactionRoute {
    const val route = "add_transaction?accountId={accountId}"
    fun createRoute(accountId: String? = null) = if (accountId != null) {
        "add_transaction?accountId=$accountId"
    } else {
        "add_transaction"
    }
}

object EditTransactionRoute {
    const val route = "edit_transaction/{transactionId}"
    fun createRoute(transactionId: String) = "edit_transaction/$transactionId"
}

object AddAccountRoute {
    const val route = "add_account"
}

object EditAccountRoute {
    const val route = "edit_account/{accountId}"
    fun createRoute(accountId: String) = "edit_account/$accountId"
}

object TransferRoute {
    const val route = "account_transfer"
}

object AddEditTaskRoute {
    const val route = "add_edit_task?taskId={taskId}"
    fun createRoute(taskId: String? = null) = if (taskId != null) {
        "add_edit_task?taskId=$taskId"
    } else {
        "add_edit_task"
    }
}

object TodoDatePickerRoute {
    const val route = "todo_date_picker?initialMillis={initialMillis}"
    fun createRoute(initialMillis: Long? = null) = if (initialMillis != null) {
        "todo_date_picker?initialMillis=$initialMillis"
    } else {
        "todo_date_picker"
    }
}

object AddEditHabitRoute {
    const val route = "add_edit_habit?habitId={habitId}"
    fun createRoute(habitId: String? = null) = if (habitId != null) {
        "add_edit_habit?habitId=$habitId"
    } else {
        "add_edit_habit"
    }
}

object StatisticsRoute {
    const val route = "statistics"
}

object AssetOverviewRoute {
    const val route = "asset_overview"
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

object DataImportRoute {
    const val route = "data_import"
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

// Plan module routes
object PlanRoute {
    const val route = "plan"
}

object EditShiftRoute {
    const val route = "edit_shift?shiftId={shiftId}"
    fun createRoute(shiftId: Long? = null) = if (shiftId != null) {
        "edit_shift?shiftId=$shiftId"
    } else {
        "edit_shift"
    }
}

object UpdateProgressRoute {
    const val route = "update_progress/{planId}"
    fun createRoute(planId: String) = "update_progress/$planId"
}

object AddEditMilestoneRoute {
    const val route = "add_edit_milestone/{planId}?milestoneId={milestoneId}"
    fun createRoute(planId: String, milestoneId: String? = null) = 
        if (milestoneId == null) "add_edit_milestone/$planId"
        else "add_edit_milestone/$planId?milestoneId=$milestoneId"
}

object AdvancedFilterRoute {
    const val route = "advanced_filter"
}

object AddCategoryRoute {
    const val route = "add_category/{categoryType}"
    fun createRoute(categoryType: String) = "add_category/$categoryType"
}

object EditCategoryRoute {
    const val route = "edit_category/{categoryId}"
    fun createRoute(categoryId: String) = "edit_category/$categoryId"
}

object AddCreditCardRoute {
    const val route = "add_credit_card"
}

object CreditCardDetailRoute {
    const val route = "credit_card_detail/{accountId}"
    fun createRoute(accountId: String) = "credit_card_detail/$accountId"
}

object CreditCardSettingsRoute {
    const val route = "credit_card_settings/{accountId}"
    fun createRoute(accountId: String) = "credit_card_settings/$accountId"
}

object PaymentRoute {
    const val route = "payment/{accountId}"
    fun createRoute(accountId: String) = "payment/$accountId"
}

object EditCreditCardRoute {
    const val route = "edit_credit_card/{accountId}"
    fun createRoute(accountId: String) = "edit_credit_card/$accountId"
}

object PaymentHistoryRoute {
    const val route = "payment_history/{accountId}"
    fun createRoute(accountId: String) = "payment_history/$accountId"
}

object AddSavingsGoalRoute {
    const val route = "add_savings_goal"
}

object EditSavingsGoalRoute {
    const val route = "edit_savings_goal/{goalId}"
    fun createRoute(goalId: String) = "edit_savings_goal/$goalId"
}

object ContributionRoute {
    const val route = "contribution/{goalId}"
    fun createRoute(goalId: String) = "contribution/$goalId"
}

object AddEditRecurringTransactionRoute {
    const val route = "add_edit_recurring_transaction?recurringId={recurringId}"
    fun createRoute(recurringId: String? = null) = if (recurringId != null) {
        "add_edit_recurring_transaction?recurringId=$recurringId"
    } else {
        "add_edit_recurring_transaction"
    }
}

object CurrencySelectionRoute {
    const val route = "currency_selection"
}

object AccountSelectionRoute {
    const val route = "account_selection"
}

object ReminderSettingsRoute {
    const val route = "reminder_settings"
}

object HomeDisplaySettingsRoute {
    const val route = "home_display_settings"
}

object BatchUpdateCategoryRoute {
    const val route = "batch_update_category?selectedCount={selectedCount}"
    fun createRoute(selectedCount: Int) = "batch_update_category?selectedCount=$selectedCount"
}

object BatchDeleteRoute {
    const val route = "batch_delete?selectedCount={selectedCount}"
    fun createRoute(selectedCount: Int) = "batch_delete?selectedCount=$selectedCount"
}

object BatchUpdateAccountRoute {
    const val route = "batch_update_account?selectedCount={selectedCount}"
    fun createRoute(selectedCount: Int) = "batch_update_account?selectedCount=$selectedCount"
}

object DeleteGoalRoute {
    const val route = "delete_goal/{goalId}?goalName={goalName}"
    fun createRoute(goalId: Long, goalName: String) = "delete_goal/$goalId?goalName=$goalName"
}

object FilterTransactionRoute {
    const val route = "filter_transaction?accountId={accountId}"
    fun createRoute(accountId: Long? = null) = if (accountId != null) {
        "filter_transaction?accountId=$accountId"
    } else {
        "filter_transaction"
    }
}

object LogoutConfirmationRoute {
    const val route = "logout_confirmation"
}
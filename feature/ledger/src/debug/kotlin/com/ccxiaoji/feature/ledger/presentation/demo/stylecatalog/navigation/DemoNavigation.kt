package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DEMO_ROUTE_EXPENSE_PREVIEW
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DEMO_ROUTE_BOOK_SETTINGS
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DEMO_ROUTE_BOOK_EDIT
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DEMO_ROUTE_REPORT_STATS
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DEMO_ROUTE_CATEGORY_MANAGEMENT
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DEMO_ROUTE_MIGRATE_BOOK
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DEMO_ROUTE_CLEAR_BILLS
 
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoViewModel

// Demo 璺敱瀹氫箟
sealed class DemoScreen(val route: String) {
    // 仅保留需求的5个路由
    object ExpenseTrackerPreview : DemoScreen(DEMO_ROUTE_EXPENSE_PREVIEW)
    object Books : DemoScreen("demo_accounts_books")
    object CardBackup : DemoScreen("demo_card_backup")
    object Installments : DemoScreen("demo_installments")
    object SettingsAbout : DemoScreen("demo_settings_about")
    object BookSettings : DemoScreen(DEMO_ROUTE_BOOK_SETTINGS)
    object BookEdit : DemoScreen(DEMO_ROUTE_BOOK_EDIT)
    object ReportStats : DemoScreen(DEMO_ROUTE_REPORT_STATS)
    object CategoryManagement : DemoScreen(DEMO_ROUTE_CATEGORY_MANAGEMENT)
    object MigrateBook : DemoScreen(DEMO_ROUTE_MIGRATE_BOOK)
    object ClearBills : DemoScreen(DEMO_ROUTE_CLEAR_BILLS)
}

@Composable
fun DemoNavHost(
    viewModel: DemoViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DemoScreen.ExpenseTrackerPreview.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        // 仅保留预览页及其可达页面
        composable(DemoScreen.ExpenseTrackerPreview.route) { ExpenseTrackerPreviewScreen(navController) }
        composable(DemoScreen.Books.route) { BooksScreen(navController) }
        composable(DemoScreen.CardBackup.route) { CardBackupScreen(navController) }
        composable(DemoScreen.Installments.route) { InstallmentsScreen(navController) }
        composable(DemoScreen.SettingsAbout.route) { DemoSettingsAboutScreen(navController) }
        composable(DemoScreen.BookSettings.route) { BookSettingsScreen(navController) }
        composable(DemoScreen.BookEdit.route) { BookEditScreen(navController) }
        composable(DemoScreen.ReportStats.route) { BookReportV2Screen(navController) }
        composable(DemoScreen.CategoryManagement.route) { CategoryManagementScreen(navController) }
        composable(DemoScreen.MigrateBook.route) { MigrateBookScreen(navController) }
        composable(DemoScreen.ClearBills.route) { ClearBillsScreen(navController) }
    }
}

    // 已移除底部导航相关定义（Home 已删除）

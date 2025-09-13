package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoViewModel

/**
 * Demo导航系统 - 完全独立于主App的导航
 * 包含记账模块的所有核心页面
 */

// Demo内部的路由定义
sealed class DemoScreen(val route: String) {
    // 主页面
    object Home : DemoScreen("demo_home")                      // 主页（交易列表）
    object AddTransaction : DemoScreen("demo_add_transaction") // 添加交易
    object EditTransaction : DemoScreen("demo_edit_transaction/{id}") {
        fun createRoute(id: String) = "demo_edit_transaction/$id"
    }
    
    // 统计页面
    object Statistics : DemoScreen("demo_statistics")          // 统计分析
    object Charts : DemoScreen("demo_charts")                  // 图表展示
    object Reports : DemoScreen("demo_reports")                // 报表
    
    // 管理页面
    object Categories : DemoScreen("demo_categories")          // 分类管理
    object Accounts : DemoScreen("demo_accounts")             // 账户管理
    object Budget : DemoScreen("demo_budget")                 // 预算管理
    object Tags : DemoScreen("demo_tags")                     // 标签管理
    
    // 设置页面
    object Settings : DemoScreen("demo_settings")              // 设置
    object StyleSelector : DemoScreen("demo_style_selector")   // 风格选择器
    object DensitySettings : DemoScreen("demo_density")        // 密度设置
    object About : DemoScreen("demo_about")                   // 关于
    
    // 导入导出
    object Import : DemoScreen("demo_import")                  // 导入数据
    object Export : DemoScreen("demo_export")                  // 导出数据
    
    // 搜索
    object Search : DemoScreen("demo_search")                  // 搜索

    // 参考样式预览（Expense Tracker 1:1）
    object ExpenseTrackerPreview : DemoScreen("demo_expense_tracker_preview")

    // 新增页面
    object Books : DemoScreen("demo_accounts_books")           // 我的账本
    object CardBackup : DemoScreen("demo_card_backup")         // 卡片备份
    object Installments : DemoScreen("demo_installments")      // 分期管理/周期记账（含标签）
    object LedgerSettings : DemoScreen("demo_ledger_settings") // 账本设置
    object SettingsAbout : DemoScreen("demo_settings_about")   // 设置·关于（占位）
    object EditLedger : DemoScreen("demo_edit_ledger")         // 修改账本
}

/**
 * Demo导航主机
 */
@Composable
fun DemoNavHost(
    viewModel: DemoViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DemoScreen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 主页面 - 交易列表
        composable(DemoScreen.Home.route) {
            DemoHomeScreen(navController, viewModel)
        }
        
        // 添加交易
        composable(DemoScreen.AddTransaction.route) {
            DemoAddTransactionScreen(navController, viewModel)
        }
        
        // 编辑交易
        composable(DemoScreen.EditTransaction.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("id") ?: ""
            DemoEditTransactionScreen(navController, transactionId, viewModel)
        }
        
        // 统计分析
        composable(DemoScreen.Statistics.route) {
            DemoStatisticsScreen(navController, viewModel)
        }
        
        // 图表
        composable(DemoScreen.Charts.route) {
            DemoChartsScreen(navController)
        }
        
        // 报表
        composable(DemoScreen.Reports.route) {
            DemoReportsScreen(navController)
        }
        
        // 分类管理
        composable(DemoScreen.Categories.route) {
            DemoCategoriesScreen(navController)
        }
        
        // 账户管理
        composable(DemoScreen.Accounts.route) {
            DemoAccountsScreen(navController)
        }
        
        // 预算管理
        composable(DemoScreen.Budget.route) {
            DemoBudgetScreen(navController)
        }
        
        // 标签管理
        composable(DemoScreen.Tags.route) {
            DemoTagsScreen(navController)
        }
        
        // 设置
        composable(DemoScreen.Settings.route) {
            DemoSettingsScreen(navController)
        }
        
        // 风格选择器
        composable(DemoScreen.StyleSelector.route) {
            DemoStyleSelectorScreen(navController, viewModel)
        }
        
        // 密度设置
        composable(DemoScreen.DensitySettings.route) {
            DemoDensitySettingsScreen(navController)
        }
        
        // 关于
        composable(DemoScreen.About.route) {
            DemoAboutScreen(navController)
        }
        
        // 导入
        composable(DemoScreen.Import.route) {
            DemoImportScreen(navController)
        }
        
        // 导出
        composable(DemoScreen.Export.route) {
            DemoExportScreen(navController)
        }
        
        // 搜索
        composable(DemoScreen.Search.route) {
            DemoSearchScreen(navController)
        }

        // 参考样式预览
        composable(DemoScreen.ExpenseTrackerPreview.route) {
            com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.ExpenseTrackerPreviewScreen(navController)
        }

        // 我的账本
        composable(DemoScreen.Books.route) {
            com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.BooksScreen(navController)
        }

        // 卡片备份
        composable(DemoScreen.CardBackup.route) {
            com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.CardBackupScreen(navController)
        }

        // 分期管理 / 周期记账（同一屏内标签切换）
        composable(DemoScreen.Installments.route) {
            com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.InstallmentsScreen(navController)
        }

        // 账本设置
        composable(DemoScreen.LedgerSettings.route) {
            com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.LedgerSettingsScreen(navController)
        }

        // 修改账本
        composable(DemoScreen.EditLedger.route) {
            com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.EditLedgerScreen(navController)
        }

        // 设置·关于（占位）
        composable(DemoScreen.SettingsAbout.route) {
            com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens.DemoSettingsAboutScreen(navController)
        }
    }
}

/**
 * Demo底部导航项
 */
enum class DemoBottomNavItem(
    val screen: DemoScreen,
    val label: String,
    val icon: String // 简化：使用字符串表示图标名称
) {
    HOME(DemoScreen.Home, "记账", "home"),
    STATISTICS(DemoScreen.Statistics, "统计", "chart"),
    CATEGORIES(DemoScreen.Categories, "分类", "category"),
    ACCOUNTS(DemoScreen.Accounts, "账户", "wallet"),
    SETTINGS(DemoScreen.Settings, "设置", "settings")
}

package com.ccxiaoji.feature.ledger.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Ledger模块的导航路由
 */
object LedgerRoute {
    const val LEDGER_HOME = "ledger"
    const val QUICK_ADD = "ledger/quick_add"
    const val STATISTICS = "ledger/statistics"
    const val ACCOUNTS = "ledger/accounts"
    const val CATEGORIES = "ledger/categories"
    const val TRANSACTION_DETAIL = "ledger/transaction"
    const val CREDIT_CARDS = "ledger/credit_cards"
    const val BUDGET = "ledger/budget"
    const val RECURRING = "ledger/recurring"
    const val SAVINGS = "ledger/savings"
}

/**
 * Ledger模块的导航图构建
 * 暂时只添加基础路由，具体页面将逐步迁移
 */
fun NavGraphBuilder.ledgerGraph(
    navController: NavController
) {
    // 记账主页（暂时使用占位符）
    composable(LedgerRoute.LEDGER_HOME) {
        // TODO: 迁移LedgerScreen
    }
    
    // 统计页面（暂时使用占位符）
    composable(LedgerRoute.STATISTICS) {
        // TODO: 迁移StatisticsScreen
    }
}

/**
 * 导航扩展函数
 */
fun NavController.navigateToLedger() {
    navigate(LedgerRoute.LEDGER_HOME)
}

fun NavController.navigateToStatistics() {
    navigate(LedgerRoute.STATISTICS)
}

fun NavController.navigateToTransactionDetail(transactionId: String) {
    navigate("${LedgerRoute.TRANSACTION_DETAIL}/$transactionId")
}
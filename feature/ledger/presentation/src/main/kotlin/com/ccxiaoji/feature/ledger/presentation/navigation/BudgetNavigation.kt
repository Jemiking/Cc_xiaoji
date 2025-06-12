package com.ccxiaoji.feature.ledger.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.presentation.ui.budget.BudgetScreen

/**
 * 预算管理导航路由
 */
const val BUDGET_ROUTE = "ledger/budget"

/**
 * 预算管理导航扩展函数
 */
fun NavGraphBuilder.budgetScreen(
    navigator: LedgerNavigator
) {
    composable(route = BUDGET_ROUTE) {
        BudgetScreen(navigator = navigator)
    }
}

/**
 * 导航到预算管理界面
 */
fun NavController.navigateToBudget() {
    navigate(BUDGET_ROUTE)
}
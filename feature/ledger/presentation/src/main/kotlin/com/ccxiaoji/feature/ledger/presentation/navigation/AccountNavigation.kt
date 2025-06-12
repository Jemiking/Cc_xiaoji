package com.ccxiaoji.feature.ledger.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.presentation.ui.account.AccountScreen

/**
 * 账户管理导航路由
 */
const val ACCOUNT_ROUTE = "ledger/accounts"

/**
 * 账户管理导航扩展函数
 */
fun NavGraphBuilder.accountScreen(
    navigator: LedgerNavigator
) {
    composable(route = ACCOUNT_ROUTE) {
        AccountScreen(navigator = navigator)
    }
}

/**
 * 导航到账户管理界面
 */
fun NavController.navigateToAccounts() {
    navigate(ACCOUNT_ROUTE)
}
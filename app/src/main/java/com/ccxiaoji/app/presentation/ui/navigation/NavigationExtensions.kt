package com.ccxiaoji.app.presentation.ui.navigation

import androidx.navigation.NavController

/**
 * 智能返回：优先尝试 popBackStack，失败则导航到指定父级路由。
 */
fun NavController.smartBack(fallbackRoute: String) {
    val popped = try {
        popBackStack()
    } catch (_: Throwable) {
        false
    }
    if (!popped) {
        navigate(fallbackRoute) {
            // 防止重复叠加同一路由，同时保留状态
            launchSingleTop = true
            restoreState = true
        }
    }
}

fun NavController.smartBackToHome() {
    smartBack(Screen.Home.route)
}

fun NavController.smartBackToLedger() {
    smartBack(Screen.Ledger.route)
}


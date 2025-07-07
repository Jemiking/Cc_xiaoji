package com.ccxiaoji.feature.ledger.presentation.navigation

object FilterTransactionRoute {
    const val route = "filter_transaction?accountId={accountId}"
    fun createRoute(accountId: String? = null) = if (accountId != null) {
        "filter_transaction?accountId=$accountId"
    } else {
        "filter_transaction"
    }
}
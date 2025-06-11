package com.ccxiaoji.feature.ledger.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ccxiaoji.feature.ledger.presentation.ui.recurring.RecurringTransactionScreen

const val RECURRING_TRANSACTION_ROUTE = "recurring_transaction"

fun NavController.navigateToRecurringTransaction() {
    navigate(RECURRING_TRANSACTION_ROUTE)
}

fun NavGraphBuilder.recurringTransactionScreen(
    onNavigateBack: () -> Unit
) {
    composable(route = RECURRING_TRANSACTION_ROUTE) {
        RecurringTransactionScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
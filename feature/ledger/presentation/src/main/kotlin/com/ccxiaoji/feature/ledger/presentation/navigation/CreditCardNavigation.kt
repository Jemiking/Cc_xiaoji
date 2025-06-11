package com.ccxiaoji.feature.ledger.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.presentation.ui.creditcard.CreditCardBillsScreen
import com.ccxiaoji.feature.ledger.presentation.ui.creditcard.CreditCardScreen

const val CREDIT_CARD_ROUTE = "credit_card"
const val CREDIT_CARD_BILLS_ROUTE = "credit_card_bills"

fun NavGraphBuilder.creditCardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAccount: (String) -> Unit,
    ledgerNavigator: LedgerNavigator
) {
    composable(CREDIT_CARD_ROUTE) {
        CreditCardScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAccount = onNavigateToAccount,
            ledgerNavigator = ledgerNavigator
        )
    }
}

fun NavGraphBuilder.creditCardBillsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBillDetail: (String) -> Unit
) {
    composable(
        route = "$CREDIT_CARD_BILLS_ROUTE/{accountId}",
        arguments = listOf(
            navArgument("accountId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
        CreditCardBillsScreen(
            accountId = accountId,
            onNavigateBack = onNavigateBack,
            onNavigateToBillDetail = onNavigateToBillDetail
        )
    }
}

fun NavController.navigateToCreditCard() {
    navigate(CREDIT_CARD_ROUTE)
}

fun NavController.navigateToCreditCardBills(accountId: String) {
    navigate("$CREDIT_CARD_BILLS_ROUTE/$accountId")
}
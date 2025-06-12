package com.ccxiaoji.feature.ledger.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ccxiaoji.feature.ledger.presentation.ui.savings.SavingsGoalDetailScreen
import com.ccxiaoji.feature.ledger.presentation.ui.savings.SavingsGoalScreen

const val SAVINGS_GOAL_ROUTE = "savings_goal"
const val SAVINGS_GOAL_DETAIL_ROUTE = "savings_goal_detail"

fun NavGraphBuilder.savingsGoalScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    composable(SAVINGS_GOAL_ROUTE) {
        SavingsGoalScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail
        )
    }
}

fun NavGraphBuilder.savingsGoalDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable(
        route = "$SAVINGS_GOAL_DETAIL_ROUTE/{goalId}",
        arguments = listOf(
            navArgument("goalId") { type = NavType.LongType }
        )
    ) { backStackEntry ->
        val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
        SavingsGoalDetailScreen(
            goalId = goalId,
            onNavigateBack = onNavigateBack
        )
    }
}

fun NavController.navigateToSavingsGoal() {
    navigate(SAVINGS_GOAL_ROUTE)
}

fun NavController.navigateToSavingsGoalDetail(goalId: Long) {
    navigate("$SAVINGS_GOAL_DETAIL_ROUTE/$goalId")
}
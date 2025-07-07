package com.ccxiaoji.feature.ledger.presentation.navigation

/**
 * 删除储蓄目标路由
 */
object DeleteGoalRoute {
    const val route = "delete_goal/{goalId}?goalName={goalName}"
    fun createRoute(goalId: Long, goalName: String) = "delete_goal/$goalId?goalName=$goalName"
}
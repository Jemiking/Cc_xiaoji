package com.ccxiaoji.feature.ledger.presentation.navigation

// 定义记账模块的导航路由
object LedgerNavigation {
    const val StatisticsRoute = "statistics"
    const val AccountManagementRoute = "account_management"
    const val CategoryManagementRoute = "category_management"
    const val RecurringTransactionRoute = "recurring_transaction"
    const val BudgetRoute = "budget"
    const val SavingsGoalRoute = "savings_goal"
    const val CreditCardRoute = "credit_card"
    const val TransactionDetailRoute = "transaction_detail/{transactionId}"
    
    fun transactionDetailRoute(transactionId: String) = "transaction_detail/$transactionId"
}
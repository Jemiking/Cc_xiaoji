package com.ccxiaoji.feature.ledger.api

/**
 * Ledger模块的导航接口
 * 用于处理模块内部的导航逻辑
 */
interface LedgerNavigator {
    fun navigateToLedger()
    fun navigateToQuickAdd() 
    fun navigateToStatistics()
    fun navigateToAccounts()
    fun navigateToCategories()
    fun navigateToTransactionDetail(transactionId: String)
    fun navigateToCreditCards()
    fun navigateToCreditCardBills(accountId: String)
    fun navigateToBudget()
    fun navigateToRecurringTransactions()
    fun navigateToSavingsGoals()
    fun navigateToSavingsGoalDetail(goalId: Long)
    fun navigateToTransactionsByAccount(accountId: String)
    fun navigateUp()
}
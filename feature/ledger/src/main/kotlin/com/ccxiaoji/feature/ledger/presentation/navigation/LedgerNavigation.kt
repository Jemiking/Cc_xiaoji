package com.ccxiaoji.feature.ledger.presentation.navigation

// 定义记账模块的导航路由
object LedgerNavigation {
    const val StatisticsRoute = "statistics"
    const val AccountManagementRoute = "account_management"
    const val CategoryManagementRoute = "category_management"
    const val RecurringTransactionRoute = "recurring_transaction"
    const val BudgetRoute = "budget"
    const val AddEditBudgetRoute = "add_edit_budget?categoryId={categoryId}"
    const val SelectCategoryRoute = "select_category"
    const val SavingsGoalRoute = "savings_goal"
    const val CreditCardRoute = "credit_card"
    const val AssetOverviewRoute = "asset_overview"
    const val TransactionDetailRoute = "transaction_detail/{transactionId}"
    const val AddTransactionRoute = "add_transaction?accountId={accountId}"
    const val EditTransactionRoute = "edit_transaction/{transactionId}"
    const val AddAccountRoute = "add_account"
    const val EditAccountRoute = "edit_account/{accountId}"
    const val TransferRoute = "account_transfer"
    const val AddCategoryRoute = "add_category/{categoryType}"
    const val EditCategoryRoute = "edit_category/{categoryId}"
    const val AddCreditCardRoute = "add_credit_card"
    const val CreditCardDetailRoute = "credit_card_detail/{accountId}"
    const val PaymentRoute = "payment/{accountId}"
    const val EditCreditCardRoute = "edit_credit_card/{accountId}"
    const val AddSavingsGoalRoute = "add_savings_goal"
    const val EditSavingsGoalRoute = "edit_savings_goal/{goalId}"
    const val ContributionRoute = "contribution/{goalId}"
    const val AddEditRecurringTransactionRoute = "add_edit_recurring_transaction?recurringId={recurringId}"
    const val PaymentHistoryRoute = "payment_history/{accountId}"
    const val DeleteTransactionRoute = "delete_transaction/{transactionId}"
    const val DataExportRoute = "data_export"
    const val DataImportRoute = "data_import"
    const val QianjiImportRoute = "qianji_import"
    
    fun transactionDetailRoute(transactionId: String) = "transaction_detail/$transactionId"
    fun deleteTransaction(transactionId: String) = "delete_transaction/$transactionId"
    fun editTransactionRoute(transactionId: String) = "edit_transaction/$transactionId"
    fun addAccountRoute() = "add_account"
    fun editAccountRoute(accountId: String) = "edit_account/$accountId"
    fun addTransactionRoute(accountId: String? = null) = if (accountId != null) {
        "add_transaction?accountId=$accountId"
    } else {
        "add_transaction"
    }
    fun addEditBudgetRoute(categoryId: String? = null) = if (categoryId != null) {
        "add_edit_budget?categoryId=$categoryId"
    } else {
        "add_edit_budget"
    }
    fun addCategoryRoute(categoryType: String) = "add_category/$categoryType"
    fun editCategoryRoute(categoryId: String) = "edit_category/$categoryId"
    fun creditCardDetailRoute(accountId: String) = "credit_card_detail/$accountId"
    fun paymentRoute(accountId: String) = "payment/$accountId"
    fun editCreditCardRoute(accountId: String) = "edit_credit_card/$accountId"
    fun addSavingsGoalRoute() = "add_savings_goal"
    fun editSavingsGoalRoute(goalId: String) = "edit_savings_goal/$goalId"
    fun paymentHistoryRoute(accountId: String) = "payment_history/$accountId"
}
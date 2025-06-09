package com.ccxiaoji.app.navigation

import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerRoute
import com.ccxiaoji.app.presentation.ui.navigation.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LedgerNavigator的实现
 * 在app模块中实现feature-ledger模块的导航接口
 */
@Singleton
class LedgerNavigatorImpl @Inject constructor() : LedgerNavigator {
    
    private var navController: NavController? = null
    
    fun setNavController(navController: NavController) {
        this.navController = navController
    }
    
    override fun navigateToLedger() {
        navController?.navigate(Screen.Ledger.route)
    }
    
    override fun navigateToQuickAdd() {
        // 暂时导航到Ledger主页
        navController?.navigate(Screen.Ledger.route)
    }
    
    override fun navigateToStatistics() {
        navController?.navigate(StatisticsRoute.route)
    }
    
    override fun navigateToAccounts() {
        navController?.navigate(AccountManagementRoute.route)
    }
    
    override fun navigateToCategories() {
        navController?.navigate(CategoryManagementRoute.route)
    }
    
    override fun navigateToTransactionDetail(transactionId: String) {
        navController?.navigate(TransactionDetailRoute.createRoute(transactionId))
    }
    
    override fun navigateToCreditCards() {
        navController?.navigate(CreditCardRoute.route)
    }
    
    override fun navigateToBudget() {
        navController?.navigate(BudgetRoute.route)
    }
    
    override fun navigateToRecurringTransactions() {
        navController?.navigate(RecurringTransactionRoute.route)
    }
    
    override fun navigateToSavingsGoals() {
        navController?.navigate(SavingsGoalRoute.route)
    }
}
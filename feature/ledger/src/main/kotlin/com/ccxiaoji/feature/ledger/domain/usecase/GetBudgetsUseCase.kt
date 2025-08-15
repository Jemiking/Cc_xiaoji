package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取预算列表用例
 */
class GetBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    /**
     * 获取所有预算
     */
    operator fun invoke(): Flow<List<Budget>> {
        return repository.getBudgets()
    }
    
    /**
     * 获取指定月份的预算
     * @param year 年份
     * @param month 月份
     */
    fun getMonthlyBudgets(year: Int, month: Int): Flow<List<Budget>> {
        return repository.getMonthlyBudgets(year, month)
    }
}
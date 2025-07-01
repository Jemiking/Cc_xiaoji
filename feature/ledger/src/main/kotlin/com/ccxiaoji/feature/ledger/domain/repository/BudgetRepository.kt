package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.feature.ledger.domain.model.Budget
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import kotlinx.coroutines.flow.Flow

/**
 * 预算仓库接口
 * 定义所有预算相关的数据操作
 */
interface BudgetRepository {
    /**
     * 获取所有预算
     */
    fun getBudgets(): Flow<List<Budget>>
    
    /**
     * 获取指定月份的预算
     */
    fun getMonthlyBudgets(year: Int, month: Int): Flow<List<Budget>>
    
    /**
     * 创建预算
     */
    suspend fun createBudget(
        year: Int,
        month: Int,
        categoryId: String?,
        amountCents: Int
    ): Long
    
    /**
     * 更新预算
     */
    suspend fun updateBudget(budget: Budget)
    
    /**
     * 删除预算
     */
    suspend fun deleteBudget(budgetId: String)
    
    /**
     * 根据ID获取预算
     */
    suspend fun getBudgetById(budgetId: String): Budget?
    
    /**
     * 检查预算是否即将超支（达到80%）
     */
    suspend fun checkBudgetAlert(year: Int, month: Int, categoryId: String?): Boolean
    
    /**
     * 检查预算是否已超支
     */
    suspend fun checkBudgetExceeded(year: Int, month: Int, categoryId: String?): Boolean
    
    /**
     * 获取带支出信息的预算
     */
    fun getBudgetsWithSpent(year: Int, month: Int): Flow<List<BudgetWithSpent>>
    
    /**
     * 获取预算使用百分比
     */
    suspend fun getBudgetUsagePercentage(year: Int, month: Int, categoryId: String?): Float?
    
}
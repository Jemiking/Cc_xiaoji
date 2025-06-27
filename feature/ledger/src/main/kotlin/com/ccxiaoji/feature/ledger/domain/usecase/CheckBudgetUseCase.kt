package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import javax.inject.Inject

/**
 * 检查预算用例
 * 检查预算使用情况和预警状态
 */
class CheckBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    /**
     * 检查预算是否即将超支（达到80%）
     * @param year 年份
     * @param month 月份
     * @param categoryId 分类ID（null表示检查总预算）
     * @return 是否触发预警
     */
    suspend fun checkAlert(year: Int, month: Int, categoryId: String? = null): Boolean {
        return repository.checkBudgetAlert(year, month, categoryId)
    }
    
    /**
     * 检查预算是否已超支
     * @param year 年份
     * @param month 月份
     * @param categoryId 分类ID（null表示检查总预算）
     * @return 是否已超支
     */
    suspend fun checkExceeded(year: Int, month: Int, categoryId: String? = null): Boolean {
        return repository.checkBudgetExceeded(year, month, categoryId)
    }
    
    /**
     * 综合检查预算状态
     * @param year 年份
     * @param month 月份
     * @param categoryId 分类ID
     * @return BudgetStatus 预算状态
     */
    suspend fun checkBudgetStatus(year: Int, month: Int, categoryId: String): BudgetStatus {
        val categoryAlert = checkAlert(year, month, categoryId)
        val categoryExceeded = checkExceeded(year, month, categoryId)
        val totalAlert = checkAlert(year, month, null)
        val totalExceeded = checkExceeded(year, month, null)
        
        return when {
            categoryExceeded || totalExceeded -> BudgetStatus.Exceeded(
                categoryExceeded = categoryExceeded,
                totalExceeded = totalExceeded
            )
            categoryAlert || totalAlert -> BudgetStatus.Alert(
                categoryAlert = categoryAlert,
                totalAlert = totalAlert
            )
            else -> BudgetStatus.Normal
        }
    }
}

/**
 * 预算状态
 */
sealed class BudgetStatus {
    object Normal : BudgetStatus()
    data class Alert(val categoryAlert: Boolean, val totalAlert: Boolean) : BudgetStatus()
    data class Exceeded(val categoryExceeded: Boolean, val totalExceeded: Boolean) : BudgetStatus()
}
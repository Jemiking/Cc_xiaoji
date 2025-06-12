package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.core.database.dao.BudgetDao
import com.ccxiaoji.core.database.dao.BudgetWithSpent
import com.ccxiaoji.core.database.entity.BudgetEntity
import com.ccxiaoji.core.database.model.SyncStatus
import com.ccxiaoji.feature.ledger.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 预算管理数据仓库
 * 处理预算相关的数据操作
 */
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    /**
     * 创建预算
     */
    suspend fun createBudget(
        userId: String,
        year: Int,
        month: Int,
        budgetAmountCents: Int,
        categoryId: String? = null,
        alertThreshold: Float = 0.8f,
        note: String? = null
    ): BudgetEntity {
        val budget = BudgetEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            year = year,
            month = month,
            categoryId = categoryId,
            budgetAmountCents = budgetAmountCents,
            alertThreshold = alertThreshold,
            note = note,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        budgetDao.insertBudget(budget)
        return budget
    }

    /**
     * 更新预算
     */
    suspend fun updateBudget(
        budgetId: String,
        budgetAmountCents: Int? = null,
        alertThreshold: Float? = null,
        note: String? = null
    ): BudgetEntity? {
        val existingBudget = budgetDao.getBudgetById(budgetId) ?: return null
        val updatedBudget = existingBudget.copy(
            budgetAmountCents = budgetAmountCents ?: existingBudget.budgetAmountCents,
            alertThreshold = alertThreshold ?: existingBudget.alertThreshold,
            note = note ?: existingBudget.note,
            updatedAt = System.currentTimeMillis(),
            syncStatus = if (existingBudget.syncStatus == SyncStatus.SYNCED) 
                SyncStatus.MODIFIED else existingBudget.syncStatus
        )
        budgetDao.updateBudget(updatedBudget)
        return updatedBudget
    }

    /**
     * 删除预算
     */
    suspend fun deleteBudget(budgetId: String) {
        budgetDao.deleteBudget(budgetId, System.currentTimeMillis())
    }

    /**
     * 获取指定月份的预算列表
     */
    fun getBudgetsByMonth(userId: String, year: Int, month: Int): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsByMonth(userId, year, month)
    }

    /**
     * 获取指定月份的预算列表（包含花费信息）
     */
    fun getBudgetsWithSpent(userId: String, year: Int, month: Int): Flow<List<BudgetWithSpent>> {
        return budgetDao.getBudgetsWithSpent(userId, year, month)
    }

    /**
     * 获取总预算（指定月份）
     */
    suspend fun getTotalBudget(userId: String, year: Int, month: Int): BudgetEntity? {
        return budgetDao.getTotalBudget(userId, year, month)
    }

    /**
     * 获取总预算（包含花费信息）
     */
    suspend fun getTotalBudgetWithSpent(userId: String, year: Int, month: Int): BudgetWithSpent? {
        return budgetDao.getTotalBudgetWithSpent(userId, year, month)
    }

    /**
     * 获取分类预算
     */
    suspend fun getCategoryBudget(userId: String, year: Int, month: Int, categoryId: String): BudgetEntity? {
        return budgetDao.getCategoryBudget(userId, year, month, categoryId)
    }

    /**
     * 获取分类预算（包含花费信息）
     */
    suspend fun getCategoryBudgetWithSpent(userId: String, year: Int, month: Int, categoryId: String): BudgetWithSpent? {
        return budgetDao.getBudgetWithSpent(userId, year, month, categoryId)
    }

    /**
     * 检查预算是否超支
     */
    suspend fun checkBudgetExceeded(userId: String, year: Int, month: Int, categoryId: String?): Boolean {
        val budget = if (categoryId == null) {
            budgetDao.getTotalBudgetWithSpent(userId, year, month)
        } else {
            budgetDao.getBudgetWithSpent(userId, year, month, categoryId)
        }
        
        return budget?.let {
            it.spentAmountCents > it.budgetAmountCents
        } ?: false
    }
    
    /**
     * 获取所有预算
     */
    fun getBudgets(userId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsByUser(userId)
    }

    /**
     * 检查预算是否触发预警
     */
    suspend fun checkBudgetAlert(userId: String, year: Int, month: Int, categoryId: String?): Boolean {
        val budget = if (categoryId == null) {
            budgetDao.getTotalBudgetWithSpent(userId, year, month)
        } else {
            budgetDao.getBudgetWithSpent(userId, year, month, categoryId)
        }
        
        return budget?.let {
            val usageRatio = it.spentAmountCents.toFloat() / it.budgetAmountCents.toFloat()
            usageRatio >= it.alertThreshold
        } ?: false
    }

    /**
     * 获取预算使用百分比
     */
    suspend fun getBudgetUsagePercentage(userId: String, year: Int, month: Int, categoryId: String?): Float? {
        val budget = if (categoryId == null) {
            budgetDao.getTotalBudgetWithSpent(userId, year, month)
        } else {
            budgetDao.getBudgetWithSpent(userId, year, month, categoryId)
        }
        
        return budget?.let {
            if (it.budgetAmountCents == 0) {
                0f
            } else {
                (it.spentAmountCents.toFloat() / it.budgetAmountCents.toFloat()) * 100f
            }
        }
    }

    /**
     * 创建或更新预算
     */
    suspend fun upsertBudget(
        userId: String,
        year: Int,
        month: Int,
        budgetAmountCents: Int,
        categoryId: String? = null,
        alertThreshold: Float = 0.8f,
        note: String? = null
    ): BudgetEntity {
        val existingBudget = if (categoryId == null) {
            budgetDao.getTotalBudget(userId, year, month)
        } else {
            budgetDao.getCategoryBudget(userId, year, month, categoryId)
        }

        return if (existingBudget != null) {
            // 更新现有预算
            updateBudget(
                budgetId = existingBudget.id,
                budgetAmountCents = budgetAmountCents,
                alertThreshold = alertThreshold,
                note = note
            )!!
        } else {
            // 创建新预算
            createBudget(
                userId = userId,
                year = year,
                month = month,
                budgetAmountCents = budgetAmountCents,
                categoryId = categoryId,
                alertThreshold = alertThreshold,
                note = note
            )
        }
    }
    
    /**
     * 将BudgetEntity转换为Budget domain model
     */
    fun BudgetEntity.toDomainModel(spentAmountCents: Int = 0): Budget {
        return Budget(
            id = id,
            userId = userId,
            year = year,
            month = month,
            categoryId = categoryId,
            budgetAmountCents = budgetAmountCents,
            spentAmountCents = spentAmountCents,
            alertThreshold = alertThreshold,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    /**
     * 将BudgetWithSpent转换为Budget domain model
     */
    fun BudgetWithSpent.toDomainModel(): Budget {
        return Budget(
            id = id,
            userId = userId,
            year = year,
            month = month,
            categoryId = categoryId,
            budgetAmountCents = budgetAmountCents,
            spentAmountCents = spentAmountCents,
            alertThreshold = alertThreshold,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
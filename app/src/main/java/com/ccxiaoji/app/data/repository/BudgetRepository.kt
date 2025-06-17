package com.ccxiaoji.app.data.repository

import com.ccxiaoji.app.data.local.dao.BudgetDao
import com.ccxiaoji.app.data.local.dao.BudgetWithSpent
import com.ccxiaoji.app.data.local.entity.BudgetEntity
import com.ccxiaoji.app.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
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

    suspend fun deleteBudget(budgetId: String) {
        budgetDao.deleteBudget(budgetId, System.currentTimeMillis())
    }

    fun getBudgetsByMonth(userId: String, year: Int, month: Int): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsByMonth(userId, year, month)
    }

    fun getBudgetsWithSpent(userId: String, year: Int, month: Int): Flow<List<BudgetWithSpent>> {
        return budgetDao.getBudgetsWithSpent(userId, year, month)
    }

    suspend fun getTotalBudget(userId: String, year: Int, month: Int): BudgetEntity? {
        return budgetDao.getTotalBudget(userId, year, month)
    }

    suspend fun getTotalBudgetWithSpent(userId: String, year: Int, month: Int): BudgetWithSpent? {
        return budgetDao.getTotalBudgetWithSpent(userId, year, month)
    }

    suspend fun getCategoryBudget(userId: String, year: Int, month: Int, categoryId: String): BudgetEntity? {
        return budgetDao.getCategoryBudget(userId, year, month, categoryId)
    }

    suspend fun getCategoryBudgetWithSpent(userId: String, year: Int, month: Int, categoryId: String): BudgetWithSpent? {
        return budgetDao.getBudgetWithSpent(userId, year, month, categoryId)
    }

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
    
    fun getBudgets(userId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsByUser(userId)
    }

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

    // 创建或更新预算
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
}
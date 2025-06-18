package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.feature.ledger.data.local.dao.BudgetDao
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import com.ccxiaoji.feature.ledger.data.local.entity.BudgetEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val userApi: UserApi
) {
    suspend fun createBudget(
        year: Int,
        month: Int,
        budgetAmountCents: Int,
        categoryId: String? = null,
        alertThreshold: Float = 0.8f,
        note: String? = null
    ): BudgetEntity {
        val budget = BudgetEntity(
            id = UUID.randomUUID().toString(),
            userId = userApi.getCurrentUserId(),
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

    fun getBudgetsByMonth(year: Int, month: Int): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsByMonth(userApi.getCurrentUserId(), year, month)
    }

    fun getBudgetsWithSpent(year: Int, month: Int): Flow<List<BudgetWithSpent>> {
        return budgetDao.getBudgetsWithSpent(userApi.getCurrentUserId(), year, month)
    }

    suspend fun getTotalBudget(year: Int, month: Int): BudgetEntity? {
        return budgetDao.getTotalBudget(userApi.getCurrentUserId(), year, month)
    }

    suspend fun getTotalBudgetWithSpent(year: Int, month: Int): BudgetWithSpent? {
        return budgetDao.getTotalBudgetWithSpent(userApi.getCurrentUserId(), year, month)
    }

    suspend fun getCategoryBudget(year: Int, month: Int, categoryId: String): BudgetEntity? {
        return budgetDao.getCategoryBudget(userApi.getCurrentUserId(), year, month, categoryId)
    }

    suspend fun getCategoryBudgetWithSpent(year: Int, month: Int, categoryId: String): BudgetWithSpent? {
        return budgetDao.getBudgetWithSpent(userApi.getCurrentUserId(), year, month, categoryId)
    }

    suspend fun checkBudgetExceeded(year: Int, month: Int, categoryId: String?): Boolean {
        val budget = if (categoryId == null) {
            budgetDao.getTotalBudgetWithSpent(userApi.getCurrentUserId(), year, month)
        } else {
            budgetDao.getBudgetWithSpent(userApi.getCurrentUserId(), year, month, categoryId)
        }
        
        return budget?.let {
            it.spentAmountCents > it.budgetAmountCents
        } ?: false
    }
    
    fun getBudgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsByUser(userApi.getCurrentUserId())
    }

    suspend fun checkBudgetAlert(year: Int, month: Int, categoryId: String?): Boolean {
        val budget = if (categoryId == null) {
            budgetDao.getTotalBudgetWithSpent(userApi.getCurrentUserId(), year, month)
        } else {
            budgetDao.getBudgetWithSpent(userApi.getCurrentUserId(), year, month, categoryId)
        }
        
        return budget?.let {
            val usageRatio = it.spentAmountCents.toFloat() / it.budgetAmountCents.toFloat()
            usageRatio >= it.alertThreshold
        } ?: false
    }

    suspend fun getBudgetUsagePercentage(year: Int, month: Int, categoryId: String?): Float? {
        val budget = if (categoryId == null) {
            budgetDao.getTotalBudgetWithSpent(userApi.getCurrentUserId(), year, month)
        } else {
            budgetDao.getBudgetWithSpent(userApi.getCurrentUserId(), year, month, categoryId)
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
        year: Int,
        month: Int,
        budgetAmountCents: Int,
        categoryId: String? = null,
        alertThreshold: Float = 0.8f,
        note: String? = null
    ): BudgetEntity {
        val existingBudget = if (categoryId == null) {
            budgetDao.getTotalBudget(userApi.getCurrentUserId(), year, month)
        } else {
            budgetDao.getCategoryBudget(userApi.getCurrentUserId(), year, month, categoryId)
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
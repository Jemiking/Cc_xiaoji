package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.feature.ledger.data.local.dao.BudgetDao
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import com.ccxiaoji.feature.ledger.data.local.entity.BudgetEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val userApi: UserApi
) : BudgetRepository {
    override suspend fun createBudget(
        year: Int,
        month: Int,
        categoryId: String?,
        amountCents: Int
    ): Long {
        val budget = BudgetEntity(
            id = UUID.randomUUID().toString(),
            userId = userApi.getCurrentUserId(),
            year = year,
            month = month,
            categoryId = categoryId,
            budgetAmountCents = amountCents,
            alertThreshold = 0.8f,
            note = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        budgetDao.insertBudget(budget)
        return 1L // TODO: 返回实际的ID
    }

    override suspend fun updateBudget(budget: Budget) {
        // TODO: 实现
    }
    
    suspend fun updateBudgetDetailed(
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

    override suspend fun deleteBudget(budgetId: String) {
        budgetDao.deleteBudget(budgetId, System.currentTimeMillis())
    }

    override fun getMonthlyBudgets(year: Int, month: Int): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(userApi.getCurrentUserId(), year, month)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override fun getBudgetsWithSpent(year: Int, month: Int): Flow<List<BudgetWithSpent>> {
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

    override suspend fun checkBudgetExceeded(year: Int, month: Int, categoryId: String?): Boolean {
        val budget = if (categoryId == null) {
            budgetDao.getTotalBudgetWithSpent(userApi.getCurrentUserId(), year, month)
        } else {
            budgetDao.getBudgetWithSpent(userApi.getCurrentUserId(), year, month, categoryId)
        }
        
        return budget?.let {
            it.spentAmountCents > it.budgetAmountCents
        } ?: false
    }
    
    override fun getBudgets(): Flow<List<Budget>> {
        return budgetDao.getBudgetsByUser(userApi.getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun checkBudgetAlert(year: Int, month: Int, categoryId: String?): Boolean {
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

    override suspend fun getBudgetUsagePercentage(year: Int, month: Int, categoryId: String?): Float? {
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
            val updatedBudget = existingBudget.copy(
                budgetAmountCents = budgetAmountCents,
                alertThreshold = alertThreshold,
                note = note,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC
            )
            budgetDao.updateBudget(updatedBudget)
            updatedBudget
        } else {
            // 创建新预算
            val budgetId = createBudget(
                year = year,
                month = month,
                categoryId = categoryId,
                amountCents = budgetAmountCents
            )
            // 获取创建的预算
            budgetDao.getBudgetById(budgetId.toString())!!
        }
    }
}

private fun BudgetEntity.toDomainModel(): Budget {
    return Budget(
        id = id,
        userId = userId,
        year = year,
        month = month,
        categoryId = categoryId,
        budgetAmountCents = budgetAmountCents,
        alertThreshold = alertThreshold,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
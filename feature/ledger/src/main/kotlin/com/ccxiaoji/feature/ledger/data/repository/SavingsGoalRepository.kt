package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.feature.ledger.data.local.dao.SavingsGoalDao
import com.ccxiaoji.feature.ledger.data.local.entity.SavingsContributionEntity
import com.ccxiaoji.feature.ledger.data.local.entity.SavingsGoalEntity
import com.ccxiaoji.feature.ledger.domain.model.SavingsContribution
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsGoalRepository @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao
) {
    fun getActiveSavingsGoals(): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getActiveSavingsGoals().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getAllSavingsGoals().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getSavingsGoalById(goalId: Long): SavingsGoal? {
        return savingsGoalDao.getSavingsGoalById(goalId)?.toDomainModel()
    }
    
    suspend fun createSavingsGoal(goal: SavingsGoal): Long {
        return savingsGoalDao.insertSavingsGoal(goal.toEntity())
    }
    
    suspend fun updateSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.updateSavingsGoal(goal.toEntity())
    }
    
    suspend fun deleteSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.deleteSavingsGoal(goal.toEntity())
    }
    
    fun getContributionsByGoalId(goalId: Long): Flow<List<SavingsContribution>> {
        return savingsGoalDao.getContributionsByGoalId(goalId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getRecentContributions(goalId: Long, limit: Int = 10): List<SavingsContribution> {
        return savingsGoalDao.getRecentContributions(goalId, limit).map { it.toDomainModel() }
    }
    
    suspend fun addContribution(contribution: SavingsContribution) {
        savingsGoalDao.addContributionAndUpdateGoal(contribution.toEntity())
    }
    
    suspend fun deleteContribution(contribution: SavingsContribution) {
        // When deleting a contribution, we need to update the goal amount
        val goal = savingsGoalDao.getSavingsGoalById(contribution.goalId)
        if (goal != null) {
            savingsGoalDao.deleteContribution(contribution.toEntity())
            // Recalculate the total from all contributions
            val total = savingsGoalDao.getTotalContributions(contribution.goalId) ?: 0.0
            savingsGoalDao.updateSavingsGoal(goal.copy(currentAmount = total))
        }
    }
    
    suspend fun getTotalContributions(goalId: Long): Double {
        return savingsGoalDao.getTotalContributions(goalId) ?: 0.0
    }
}

// Extension functions for mapping between entity and domain models
private fun SavingsGoalEntity.toDomainModel() = SavingsGoal(
    id = id,
    userId = userId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    targetDate = targetDate,
    description = description,
    color = color,
    iconName = iconName,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun SavingsGoal.toEntity() = SavingsGoalEntity(
    id = id,
    userId = userId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    targetDate = targetDate,
    description = description,
    color = color,
    iconName = iconName,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun SavingsContributionEntity.toDomainModel() = SavingsContribution(
    id = id,
    goalId = goalId,
    amount = amount,
    note = note,
    createdAt = createdAt
)

private fun SavingsContribution.toEntity() = SavingsContributionEntity(
    id = id,
    goalId = goalId,
    amount = amount,
    note = note,
    createdAt = createdAt
)
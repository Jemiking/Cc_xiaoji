package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.core.database.dao.SavingsGoalDao
import com.ccxiaoji.core.database.entity.SavingsContributionEntity
import com.ccxiaoji.core.database.entity.SavingsGoalEntity
import com.ccxiaoji.feature.ledger.domain.model.SavingsContribution
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
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
    
    suspend fun deleteSavingsGoalById(goalId: Long) {
        savingsGoalDao.getSavingsGoalById(goalId)?.let { entity ->
            savingsGoalDao.deleteSavingsGoal(entity)
        }
    }
    
    suspend fun setSavingsGoalActive(goalId: Long, isActive: Boolean) {
        savingsGoalDao.getSavingsGoalById(goalId)?.let { entity ->
            savingsGoalDao.updateSavingsGoal(entity.copy(isActive = isActive))
        }
    }
    
    fun getContributionsByGoalId(goalId: Long): Flow<List<SavingsContribution>> {
        return savingsGoalDao.getContributionsByGoalId(goalId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getRecentContributions(goalId: Long, limit: Int = 10): List<SavingsContribution> {
        return savingsGoalDao.getRecentContributions(goalId, limit).map { it.toDomainModel() }
    }
    
    suspend fun addContribution(contribution: SavingsContribution): Long {
        val contributionEntity = contribution.toEntity()
        // First insert the contribution to get the ID
        val contributionId = savingsGoalDao.insertContribution(contributionEntity)
        // Then update the goal amount
        val currentTime = java.time.LocalDateTime.now().toString()
        savingsGoalDao.updateGoalAmount(contribution.goalId, contribution.amountCents.toDouble() / 100, currentTime)
        return contributionId
    }
    
    suspend fun deleteContribution(contribution: SavingsContribution) {
        // When deleting a contribution, we need to update the goal amount
        val goal = savingsGoalDao.getSavingsGoalById(contribution.goalId)
        if (goal != null) {
            savingsGoalDao.deleteContribution(contribution.toEntity())
            // Recalculate the total from all contributions
            val totalCents = savingsGoalDao.getTotalContributions(contribution.goalId)?.let { 
                (it * 100).toLong() 
            } ?: 0L
            savingsGoalDao.updateSavingsGoal(goal.copy(currentAmount = totalCents / 100.0))
        }
    }
    
    suspend fun deleteContributionById(contributionId: Long) {
        // 首先获取contribution以得到goalId
        val contributions = savingsGoalDao.getAllSavingsGoals()
            .map { goals ->
                goals.flatMap { goal ->
                    savingsGoalDao.getRecentContributions(goal.id, Int.MAX_VALUE)
                }
            }
        
        // 由于我们无法直接通过ID查询，需要找到对应的contribution
        savingsGoalDao.getAllSavingsGoals().collect { goals ->
            for (goal in goals) {
                val goalContributions = savingsGoalDao.getRecentContributions(goal.id, Int.MAX_VALUE)
                val contribution = goalContributions.find { it.id == contributionId }
                if (contribution != null) {
                    deleteContribution(contribution.toDomainModel())
                    break
                }
            }
        }
    }
    
    suspend fun getTotalContributions(goalId: Long): Double {
        return savingsGoalDao.getTotalContributions(goalId) ?: 0.0
    }
    
    suspend fun getActiveSavingsGoalsCount(): Int {
        return savingsGoalDao.getActiveSavingsGoals()
            .map { goals -> goals.count { !it.isCompleted() } }
            .let { flow ->
                var count = 0
                flow.collect { count = it }
                count
            }
    }
    
    suspend fun getSavingsGoalsSummary(): SavingsGoalsSummary {
        val goals = mutableListOf<SavingsGoal>()
        savingsGoalDao.getAllSavingsGoals().collect { entityList ->
            goals.addAll(entityList.map { it.toDomainModel() })
        }
        
        val activeGoals = goals.filter { it.isActive && !it.isCompleted }
        val completedGoals = goals.filter { it.isCompleted }
        val totalTargetCents = goals.sumOf { it.targetAmountCents }
        val totalCurrentCents = goals.sumOf { it.currentAmountCents }
        val totalProgress = if (totalTargetCents > 0) {
            (totalCurrentCents.toFloat() / totalTargetCents.toFloat())
        } else 0f
        
        return SavingsGoalsSummary(
            activeGoalsCount = activeGoals.size,
            completedGoalsCount = completedGoals.size,
            totalTargetAmountCents = totalTargetCents,
            totalCurrentAmountCents = totalCurrentCents,
            totalProgress = totalProgress
        )
    }
}

/**
 * 存钱目标统计摘要
 */
data class SavingsGoalsSummary(
    val activeGoalsCount: Int,
    val completedGoalsCount: Int,
    val totalTargetAmountCents: Long,
    val totalCurrentAmountCents: Long,
    val totalProgress: Float
)

// Extension functions for mapping between entity and domain models
private fun SavingsGoalEntity.toDomainModel() = SavingsGoal(
    id = id,
    name = name,
    targetAmountCents = (targetAmount * 100).toLong(),
    currentAmountCents = (currentAmount * 100).toLong(),
    targetDate = targetDate?.toKotlinLocalDate(),
    description = description,
    color = color,
    iconName = iconName,
    isActive = isActive,
    createdAt = createdAt.toInstant(java.time.ZoneOffset.UTC).toKotlinInstant(),
    updatedAt = updatedAt.toInstant(java.time.ZoneOffset.UTC).toKotlinInstant()
)

private fun SavingsGoal.toEntity() = SavingsGoalEntity(
    id = id,
    name = name,
    targetAmount = targetAmountCents / 100.0,
    currentAmount = currentAmountCents / 100.0,
    targetDate = targetDate?.toJavaLocalDate(),
    description = description,
    color = color,
    iconName = iconName,
    isActive = isActive,
    createdAt = createdAt.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
    updatedAt = updatedAt.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
)

private fun SavingsContributionEntity.toDomainModel() = SavingsContribution(
    id = id,
    goalId = goalId,
    amountCents = (amount * 100).toLong(),
    note = note,
    createdAt = createdAt.toInstant(java.time.ZoneOffset.UTC).toKotlinInstant()
)

private fun SavingsContribution.toEntity() = SavingsContributionEntity(
    id = id,
    goalId = goalId,
    amount = amountCents / 100.0,
    note = note,
    createdAt = createdAt.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
)

private fun SavingsGoalEntity.isCompleted(): Boolean {
    return currentAmount >= targetAmount
}
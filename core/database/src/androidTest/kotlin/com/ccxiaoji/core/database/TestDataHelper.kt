package com.ccxiaoji.core.database

import com.ccxiaoji.core.database.entity.*
import com.ccxiaoji.core.database.model.RecurringFrequency
import com.ccxiaoji.core.database.model.SyncStatus
import java.util.*

/**
 * 测试数据帮助类
 * 提供创建测试数据的便捷方法
 */
object TestDataHelper {
    
    fun createTestUser(
        id: String = UUID.randomUUID().toString(),
        email: String = "test@example.com",
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ): UserEntity {
        return UserEntity(
            id = id,
            email = email,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = syncStatus
        )
    }
    
    fun createTestAccount(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        name: String = "测试账户",
        type: String = "CASH",
        balanceCents: Long = 100000,
        isDefault: Boolean = true
    ): AccountEntity {
        return AccountEntity(
            id = id,
            userId = userId,
            name = name,
            type = type,
            balanceCents = balanceCents,
            currency = "CNY",
            isDefault = isDefault,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
    
    fun createTestCategory(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        name: String,
        type: CategoryType,
        parentId: String? = null
    ): CategoryEntity {
        return CategoryEntity(
            id = id,
            userId = userId,
            name = name,
            type = type,
            icon = "category",
            color = "#000000",
            parentId = parentId,
            orderIndex = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
    
    fun createTestTransaction(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        accountId: String,
        categoryId: String,
        type: String,
        amountCents: Long,
        note: String? = null
    ): TransactionEntity {
        return TransactionEntity(
            id = id,
            userId = userId,
            accountId = accountId,
            categoryId = categoryId,
            type = type,
            amountCents = amountCents,
            note = note,
            transactionDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
    
    fun createTestTask(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        title: String,
        isCompleted: Boolean = false,
        priority: Int = 1
    ): TaskEntity {
        return TaskEntity(
            id = id,
            userId = userId,
            title = title,
            description = null,
            isCompleted = isCompleted,
            priority = priority,
            dueDate = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
    
    fun createTestHabit(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        name: String,
        targetDays: Int = 7
    ): HabitEntity {
        return HabitEntity(
            id = id,
            userId = userId,
            name = name,
            description = null,
            icon = "star",
            color = "#FFD700",
            targetDays = targetDays,
            reminderTime = null,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
    
    fun createTestBudget(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        categoryId: String,
        amountCents: Long,
        period: String = "MONTHLY"
    ): BudgetEntity {
        return BudgetEntity(
            id = id,
            userId = userId,
            categoryId = categoryId,
            amountCents = amountCents,
            period = period,
            startDate = System.currentTimeMillis(),
            endDate = null,
            note = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
    
    fun createTestRecurringTransaction(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        accountId: String,
        categoryId: String,
        type: String,
        amountCents: Long,
        frequency: RecurringFrequency
    ): RecurringTransactionEntity {
        return RecurringTransactionEntity(
            id = id,
            userId = userId,
            accountId = accountId,
            categoryId = categoryId,
            type = type,
            amountCents = amountCents,
            note = null,
            frequency = frequency,
            dayOfMonth = if (frequency == RecurringFrequency.MONTHLY) 1 else null,
            dayOfWeek = if (frequency == RecurringFrequency.WEEKLY) 1 else null,
            isEnabled = true,
            nextExecutionDate = System.currentTimeMillis() + 86400000,
            lastExecutionDate = null,
            endDate = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
    
    fun createTestSavingsGoal(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        name: String,
        targetAmountCents: Long,
        currentAmountCents: Long = 0
    ): SavingsGoalEntity {
        return SavingsGoalEntity(
            id = id,
            userId = userId,
            name = name,
            targetAmountCents = targetAmountCents,
            currentAmountCents = currentAmountCents,
            targetDate = System.currentTimeMillis() + 365 * 86400000,
            icon = "savings",
            color = "#4CAF50",
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
}
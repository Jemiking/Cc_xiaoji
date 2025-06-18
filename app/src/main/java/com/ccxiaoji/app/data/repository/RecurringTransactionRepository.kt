package com.ccxiaoji.app.data.repository

import com.ccxiaoji.app.data.local.dao.RecurringTransactionDao
import com.ccxiaoji.app.data.local.dao.TransactionDao
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.app.data.local.entity.RecurringTransactionEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao
) {
    fun getAllRecurringTransactions(userId: String): Flow<List<RecurringTransactionEntity>> {
        return recurringTransactionDao.getAllRecurringTransactions(userId)
    }
    
    fun getEnabledRecurringTransactions(userId: String): Flow<List<RecurringTransactionEntity>> {
        return recurringTransactionDao.getEnabledRecurringTransactions(userId)
    }
    
    suspend fun getRecurringTransactionById(id: String): RecurringTransactionEntity? {
        return recurringTransactionDao.getRecurringTransactionById(id)
    }
    
    suspend fun createRecurringTransaction(
        userId: String,
        name: String,
        accountId: String,
        amountCents: Int,
        categoryId: String,
        note: String?,
        frequency: RecurringFrequency,
        dayOfWeek: Int? = null,
        dayOfMonth: Int? = null,
        monthOfYear: Int? = null,
        startDate: Long,
        endDate: Long? = null
    ) {
        val now = System.currentTimeMillis()
        val nextExecutionDate = calculateNextExecutionDate(
            startDate,
            frequency,
            dayOfWeek,
            dayOfMonth,
            monthOfYear
        )
        
        val recurringTransaction = RecurringTransactionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name,
            accountId = accountId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            frequency = frequency,
            dayOfWeek = dayOfWeek,
            dayOfMonth = dayOfMonth,
            monthOfYear = monthOfYear,
            startDate = startDate,
            endDate = endDate,
            isEnabled = true,
            lastExecutionDate = null,
            nextExecutionDate = nextExecutionDate,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )
        
        recurringTransactionDao.insertRecurringTransaction(recurringTransaction)
    }
    
    suspend fun updateRecurringTransaction(
        id: String,
        name: String,
        accountId: String,
        amountCents: Int,
        categoryId: String,
        note: String?,
        frequency: RecurringFrequency,
        dayOfWeek: Int? = null,
        dayOfMonth: Int? = null,
        monthOfYear: Int? = null,
        startDate: Long,
        endDate: Long? = null
    ) {
        val existing = recurringTransactionDao.getRecurringTransactionById(id) ?: return
        
        val nextExecutionDate = calculateNextExecutionDate(
            startDate,
            frequency,
            dayOfWeek,
            dayOfMonth,
            monthOfYear,
            existing.lastExecutionDate
        )
        
        val updated = existing.copy(
            name = name,
            accountId = accountId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            frequency = frequency,
            dayOfWeek = dayOfWeek,
            dayOfMonth = dayOfMonth,
            monthOfYear = monthOfYear,
            startDate = startDate,
            endDate = endDate,
            nextExecutionDate = nextExecutionDate,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        
        recurringTransactionDao.updateRecurringTransaction(updated)
    }
    
    suspend fun toggleEnabled(id: String) {
        val recurring = recurringTransactionDao.getRecurringTransactionById(id) ?: return
        val now = System.currentTimeMillis()
        
        if (!recurring.isEnabled) {
            // 重新启用时，重新计算下次执行日期
            val nextExecutionDate = calculateNextExecutionDate(
                now,
                recurring.frequency,
                recurring.dayOfWeek,
                recurring.dayOfMonth,
                recurring.monthOfYear
            )
            
            val updated = recurring.copy(
                isEnabled = true,
                nextExecutionDate = nextExecutionDate,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING
            )
            recurringTransactionDao.updateRecurringTransaction(updated)
        } else {
            recurringTransactionDao.updateEnabledStatus(id, false, now)
        }
    }
    
    suspend fun deleteRecurringTransaction(id: String) {
        val recurring = recurringTransactionDao.getRecurringTransactionById(id) ?: return
        recurringTransactionDao.deleteRecurringTransaction(recurring)
    }
    
    suspend fun executeDueRecurringTransactions(): Int {
        val now = System.currentTimeMillis()
        val dueTransactions = recurringTransactionDao.getDueRecurringTransactions(now)
        var executedCount = 0
        
        for (recurring in dueTransactions) {
            // 检查是否已过结束日期
            if (recurring.endDate != null && now > recurring.endDate) {
                // 禁用已过期的定期交易
                recurringTransactionDao.updateEnabledStatus(recurring.id, false, now)
                continue
            }
            
            // 创建交易记录
            val transaction = TransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = recurring.userId,
                accountId = recurring.accountId,
                amountCents = recurring.amountCents,
                categoryId = recurring.categoryId,
                note = recurring.note ?: recurring.name,
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
                syncStatus = SyncStatus.PENDING
            )
            
            transactionDao.insertTransaction(transaction)
            
            // 计算下次执行日期
            val nextExecutionDate = calculateNextExecutionDate(
                recurring.nextExecutionDate,
                recurring.frequency,
                recurring.dayOfWeek,
                recurring.dayOfMonth,
                recurring.monthOfYear,
                now
            )
            
            // 更新执行日期
            recurringTransactionDao.updateExecutionDates(
                recurring.id,
                now,
                nextExecutionDate,
                now
            )
            
            executedCount++
        }
        
        return executedCount
    }
    
    private fun calculateNextExecutionDate(
        baseDate: Long,
        frequency: RecurringFrequency,
        dayOfWeek: Int?,
        dayOfMonth: Int?,
        monthOfYear: Int?,
        lastExecutionDate: Long? = null
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = if (lastExecutionDate != null && lastExecutionDate > baseDate) {
                lastExecutionDate
            } else {
                baseDate
            }
        }
        
        val now = Calendar.getInstance()
        
        when (frequency) {
            RecurringFrequency.DAILY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            RecurringFrequency.WEEKLY -> {
                if (dayOfWeek != null) {
                    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                    if (calendar.before(now) || calendar.timeInMillis == lastExecutionDate) {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }
            }
            
            RecurringFrequency.MONTHLY -> {
                if (dayOfMonth != null) {
                    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    calendar.set(Calendar.DAY_OF_MONTH, minOf(dayOfMonth, maxDay))
                    if (calendar.before(now) || calendar.timeInMillis == lastExecutionDate) {
                        calendar.add(Calendar.MONTH, 1)
                        val newMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        calendar.set(Calendar.DAY_OF_MONTH, minOf(dayOfMonth, newMaxDay))
                    }
                }
            }
            
            RecurringFrequency.YEARLY -> {
                if (monthOfYear != null && dayOfMonth != null) {
                    calendar.set(Calendar.MONTH, monthOfYear - 1)
                    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    calendar.set(Calendar.DAY_OF_MONTH, minOf(dayOfMonth, maxDay))
                    if (calendar.before(now) || calendar.timeInMillis == lastExecutionDate) {
                        calendar.add(Calendar.YEAR, 1)
                    }
                }
            }
        }
        
        // 设置时间为当天的开始
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return calendar.timeInMillis
    }
}
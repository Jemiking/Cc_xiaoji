package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.common.base.safeSuspendCall
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryStatistic as DaoCategoryStatistic
import com.ccxiaoji.feature.ledger.domain.model.CategoryStatistic
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.shared.sync.data.local.dao.ChangeLogDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.shared.sync.data.local.entity.ChangeLogEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import com.ccxiaoji.feature.ledger.domain.model.CategoryDetails
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.shared.user.api.UserApi
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val changeLogDao: ChangeLogDao,
    private val userApi: UserApi,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val gson: Gson
) : TransactionRepository {
    override fun getTransactions(): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUser(userApi.getCurrentUserId())
            .map { entities -> 
                entities.map { entity ->
                    val categoryDetails = categoryDao.getCategoryById(entity.categoryId)?.let { category ->
                        CategoryDetails(
                            id = category.id,
                            name = category.name,
                            icon = category.icon,
                            color = category.color,
                            type = category.type
                        )
                    }
                    entity.toDomainModel(categoryDetails)
                }
            }
    }
    
    override fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return transactionDao.getTransactionsByDateRange(userApi.getCurrentUserId(), startMillis, endMillis)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    
    override suspend fun getMonthlyTotal(year: Int, month: Int): BaseResult<Int> = safeSuspendCall {
        val startDate = LocalDate(year, month, 1)
        val endDate = startDate.plus(1, DateTimeUnit.MONTH)
        
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        transactionDao.getTotalAmountByDateRange(userApi.getCurrentUserId(), startMillis, endMillis) ?: 0
    }
    
    
    override suspend fun getMonthlyIncomesAndExpenses(year: Int, month: Int): BaseResult<Pair<Int, Int>> = safeSuspendCall {
        val startDate = LocalDate(year, month, 1)
        val endDate = startDate.plus(1, DateTimeUnit.MONTH)
        
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        // Use new category-based queries
        val income = transactionDao.getTotalByType(userApi.getCurrentUserId(), startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByType(userApi.getCurrentUserId(), startMillis, endMillis, "EXPENSE") ?: 0
        
        income to expense
    }
    
    override suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String
    ): BaseResult<Long> = safeSuspendCall {
        val transactionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        // Use provided accountId
        val actualAccountId = accountId
        
        // Get category details
        val categoryEntity = categoryDao.getCategoryById(categoryId)
        
        val entity = TransactionEntity(
            id = transactionId,
            userId = userApi.getCurrentUserId(),
            accountId = actualAccountId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        transactionDao.insertTransaction(entity)
        
        // Update account balance
        val category = categoryEntity
        val balanceChange = if (category?.type == "INCOME") amountCents.toLong() else -amountCents.toLong()
        accountDao.updateBalance(actualAccountId, balanceChange, now)
        
        // Increment category usage count
        categoryDao.incrementUsageCount(categoryId)
        
        // Log the change for sync
        logChange("transactions", transactionId, "INSERT", entity)
        
        transactionId.hashCode().toLong()
    }
    
    override suspend fun updateTransaction(transaction: Transaction): BaseResult<Unit> = safeSuspendCall {
        val now = System.currentTimeMillis()
        val entity = transaction.toEntity(userApi.getCurrentUserId(), now)
        
        transactionDao.updateTransaction(entity)
        
        // Log the change for sync
        logChange("transactions", transaction.id, "UPDATE", entity)
    }
    
    override suspend fun deleteTransaction(transactionId: String): BaseResult<Unit> = safeSuspendCall {
        val now = System.currentTimeMillis()
        
        transactionDao.softDeleteTransaction(transactionId, now)
        
        // Log the change for sync
        logChange("transactions", transactionId, "DELETE", mapOf("id" to transactionId))
    }
    
    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUser(userApi.getCurrentUserId())
            .map { entities -> 
                entities.take(limit).map { it.toDomainModel() }
            }
    }
    
    private suspend fun logChange(table: String, rowId: String, operation: String, payload: Any) {
        val changeLog = ChangeLogEntity(
            tableName = table,
            rowId = rowId,
            operation = operation,
            payload = gson.toJson(payload),
            timestamp = System.currentTimeMillis()
        )
        changeLogDao.insertChange(changeLog)
    }
    
    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(userApi.getCurrentUserId(), query)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByAccount(userApi.getCurrentUserId(), accountId)
            .map { entities -> 
                entities.map { entity ->
                    val categoryDetails = categoryDao.getCategoryById(entity.categoryId)?.let { category ->
                        CategoryDetails(
                            id = category.id,
                            name = category.name,
                            icon = category.icon,
                            color = category.color,
                            type = category.type
                        )
                    }
                    entity.toDomainModel(categoryDetails)
                }
            }
    }
    
    override fun getTransactionsByCategory(categoryId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUser(userApi.getCurrentUserId())
            .map { entities -> 
                entities.filter { it.categoryId == categoryId }.map { entity ->
                    entity.toDomainModel()
                }
            }
    }
    
    override fun getTransactionsByAccountAndDateRange(accountId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return transactionDao.getTransactionsByAccountAndDateRange(userApi.getCurrentUserId(), accountId, startMillis, endMillis)
            .map { entities -> 
                entities.map { entity ->
                    val categoryDetails = categoryDao.getCategoryById(entity.categoryId)?.let { category ->
                        CategoryDetails(
                            id = category.id,
                            name = category.name,
                            icon = category.icon,
                            color = category.color,
                            type = category.type
                        )
                    }
                    entity.toDomainModel(categoryDetails)
                }
            }
    }
    
    // Statistics methods
    override suspend fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): BaseResult<Map<LocalDate, Pair<Int, Int>>> = safeSuspendCall {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val transactions = transactionDao.getTransactionsByDateRangeSync(userApi.getCurrentUserId(), startMillis, endMillis)
        
        transactions.groupBy { transaction ->
            Instant.fromEpochMilliseconds(transaction.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        }.mapValues { (_, dayTransactions) ->
            val income = dayTransactions.filter { 
                categoryDao.getCategoryById(it.categoryId)?.type == "INCOME" 
            }.sumOf { it.amountCents }
            
            val expense = dayTransactions.filter { 
                categoryDao.getCategoryById(it.categoryId)?.type == "EXPENSE" 
            }.sumOf { it.amountCents }
            
            income to expense
        }
    }
    
    override suspend fun getCategoryStatistics(
        categoryType: String?,
        startDate: Long,
        endDate: Long
    ): BaseResult<List<CategoryStatistic>> = safeSuspendCall {
        val stats = if (categoryType != null) {
            transactionDao.getCategoryStatisticsByType(userApi.getCurrentUserId(), startDate, endDate, categoryType)
        } else {
            transactionDao.getCategoryStatisticsByType(userApi.getCurrentUserId(), startDate, endDate, "EXPENSE")
        }
        
        stats.map { dao ->
            CategoryStatistic(
                categoryId = dao.categoryId,
                categoryName = dao.categoryName,
                categoryIcon = dao.categoryIcon,
                categoryColor = dao.categoryColor,
                totalAmount = dao.totalAmount,
                transactionCount = dao.transactionCount
            )
        }
    }
    
    override suspend fun getTopTransactions(startDate: LocalDate, endDate: LocalDate, type: String, limit: Int): BaseResult<List<Transaction>> = safeSuspendCall {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        transactionDao.getTopTransactionsByType(userApi.getCurrentUserId(), startMillis, endMillis, type, limit)
            .map { entity ->
                val categoryDetails = categoryDao.getCategoryById(entity.categoryId)?.let { category ->
                    CategoryDetails(
                        id = category.id,
                        name = category.name,
                        icon = category.icon,
                        color = category.color,
                        type = category.type
                    )
                }
                entity.toDomainModel(categoryDetails)
            }
    }
    
    override suspend fun calculateSavingsRate(startDate: LocalDate, endDate: LocalDate): BaseResult<Float> = safeSuspendCall {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val income = transactionDao.getTotalByType(userApi.getCurrentUserId(), startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByType(userApi.getCurrentUserId(), startMillis, endMillis, "EXPENSE") ?: 0
        
        if (income > 0) {
            ((income - expense).toFloat() / income) * 100
        } else {
            0f
        }
    }
    
}

private fun TransactionEntity.toDomainModel(categoryDetails: CategoryDetails? = null): Transaction {
    return Transaction(
        id = id,
        accountId = accountId,
        amountCents = amountCents,
        categoryId = categoryId,
        categoryDetails = categoryDetails,
        note = note,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}

private fun Transaction.toEntity(userId: String, updatedAt: Long): TransactionEntity {
    return TransactionEntity(
        id = id,
        userId = userId,
        accountId = accountId,
        amountCents = amountCents,
        categoryId = categoryId,
        note = note,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt,
        syncStatus = SyncStatus.PENDING_SYNC
    )
}
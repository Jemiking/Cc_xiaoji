package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.core.database.dao.AccountDao
import com.ccxiaoji.core.database.dao.CategoryDao
import com.ccxiaoji.core.database.dao.CategoryStatistic
import com.ccxiaoji.core.database.dao.ChangeLogDao
import com.ccxiaoji.core.database.dao.TransactionDao
import com.ccxiaoji.core.database.entity.ChangeLogEntity
import com.ccxiaoji.core.database.entity.TransactionEntity
import com.ccxiaoji.core.database.model.SyncStatus
import com.ccxiaoji.feature.ledger.api.CategoryStat
import com.ccxiaoji.feature.ledger.api.TransactionDetail
import com.ccxiaoji.feature.ledger.api.TransactionItem
import com.ccxiaoji.feature.ledger.api.TransactionStats
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import kotlinx.datetime.Instant as KotlinInstant
import com.ccxiaoji.core.common.util.DateConverter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val changeLogDao: ChangeLogDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val gson: Gson
) {
    
    /**
     * 获取指定月份的交易记录
     */
    suspend fun getTransactionsByMonth(userId: String, year: Int, month: Int): List<TransactionItem> {
        val startDate = LocalDate(year, month, 1)
        val endDate = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
        
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return transactionDao.getTransactionsByDateRange(userId, startMillis, endMillis)
            .first()
            .map { entity -> entity.toTransactionItem() }
    }
    
    /**
     * 获取最近交易记录
     */
    suspend fun getRecentTransactions(userId: String, limit: Int): List<TransactionItem> {
        return transactionDao.getTransactionsByUser(userId)
            .first()
            .take(limit)
            .map { entity -> entity.toTransactionItem() }
    }
    
    /**
     * 添加交易记录
     */
    suspend fun addTransaction(
        userId: String,
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String? = null
    ): String {
        val transactionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        // 使用提供的accountId或获取默认账户
        val actualAccountId = accountId ?: accountDao.getDefaultAccount(userId)?.id 
            ?: throw IllegalStateException("No default account found")
        
        // 获取分类详情
        val categoryEntity = categoryDao.getCategoryById(categoryId)
        
        val entity = TransactionEntity(
            id = transactionId,
            userId = userId,
            accountId = actualAccountId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        transactionDao.insertTransaction(entity)
        
        // 更新账户余额
        val category = categoryEntity
        val balanceChange = if (category?.type == "INCOME") amountCents.toLong() else -amountCents.toLong()
        accountDao.updateBalance(actualAccountId, balanceChange, now)
        
        // 增加分类使用次数
        categoryDao.incrementUsageCount(categoryId)
        
        // 记录变更日志用于同步
        logChange("transactions", transactionId, "INSERT", entity)
        
        return transactionId
    }
    
    /**
     * 更新交易记录
     */
    suspend fun updateTransaction(
        userId: String,
        transactionId: String,
        amountCents: Int,
        categoryId: String,
        note: String?
    ) {
        val now = System.currentTimeMillis()
        
        // 获取原交易记录
        val originalTransaction = transactionDao.getTransactionByIdSync(transactionId)
            ?: throw IllegalArgumentException("Transaction not found: $transactionId")
        
        // 如果金额或分类变化，需要更新账户余额
        if (originalTransaction.amountCents != amountCents || originalTransaction.categoryId != categoryId) {
            val originalCategory = categoryDao.getCategoryById(originalTransaction.categoryId)
            val newCategory = categoryDao.getCategoryById(categoryId)
            
            // 撤销原交易对余额的影响
            val originalBalanceChange = if (originalCategory?.type == "INCOME") 
                -originalTransaction.amountCents.toLong() 
            else 
                originalTransaction.amountCents.toLong()
            accountDao.updateBalance(originalTransaction.accountId, originalBalanceChange, now)
            
            // 应用新交易对余额的影响
            val newBalanceChange = if (newCategory?.type == "INCOME") 
                amountCents.toLong() 
            else 
                -amountCents.toLong()
            accountDao.updateBalance(originalTransaction.accountId, newBalanceChange, now)
            
            // 更新分类使用次数
            if (originalTransaction.categoryId != categoryId) {
                categoryDao.decrementUsageCount(originalTransaction.categoryId)
                categoryDao.incrementUsageCount(categoryId)
            }
        }
        
        // 更新交易记录
        val updatedEntity = originalTransaction.copy(
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        transactionDao.updateTransaction(updatedEntity)
        
        // 记录变更日志
        logChange("transactions", transactionId, "UPDATE", updatedEntity)
    }
    
    /**
     * 删除交易记录
     */
    suspend fun deleteTransaction(userId: String, transactionId: String) {
        val now = System.currentTimeMillis()
        
        // 获取交易记录
        val transaction = transactionDao.getTransactionByIdSync(transactionId)
            ?: throw IllegalArgumentException("Transaction not found: $transactionId")
        
        // 撤销对账户余额的影响
        val category = categoryDao.getCategoryById(transaction.categoryId)
        val balanceChange = if (category?.type == "INCOME") 
            -transaction.amountCents.toLong() 
        else 
            transaction.amountCents.toLong()
        accountDao.updateBalance(transaction.accountId, balanceChange, now)
        
        // 减少分类使用次数
        categoryDao.decrementUsageCount(transaction.categoryId)
        
        // 软删除交易记录
        transactionDao.softDeleteTransaction(transactionId, now)
        
        // 记录变更日志
        logChange("transactions", transactionId, "DELETE", mapOf("id" to transactionId))
    }
    
    /**
     * 批量删除交易记录
     */
    suspend fun deleteTransactions(userId: String, transactionIds: List<String>) {
        transactionIds.forEach { transactionId ->
            deleteTransaction(userId, transactionId)
        }
    }
    
    /**
     * 搜索交易记录
     */
    suspend fun searchTransactions(userId: String, query: String): List<TransactionItem> {
        return transactionDao.searchTransactions(userId, query)
            .first()
            .map { entity -> entity.toTransactionItem() }
    }
    
    /**
     * 根据账户获取交易记录
     */
    suspend fun getTransactionsByAccount(userId: String, accountId: String): List<TransactionItem> {
        return transactionDao.getTransactionsByAccount(userId, accountId)
            .first()
            .map { entity -> entity.toTransactionItem() }
    }
    
    /**
     * 获取交易详情
     */
    suspend fun getTransactionDetail(transactionId: String): TransactionDetail? {
        val entity = transactionDao.getTransactionByIdSync(transactionId) ?: return null
        val category = categoryDao.getCategoryById(entity.categoryId) ?: return null
        val account = accountDao.getAccountById(entity.accountId) ?: return null
        
        return TransactionDetail(
            id = entity.id,
            amountCents = entity.amountCents,
            categoryId = category.id,
            categoryName = category.name,
            categoryIcon = category.icon,
            categoryColor = category.color,
            categoryType = category.type,
            accountId = account.id,
            accountName = account.name,
            note = entity.note,
            createdAt = DateConverter.toJavaInstant(KotlinInstant.fromEpochMilliseconds(entity.createdAt)),
            updatedAt = DateConverter.toJavaInstant(KotlinInstant.fromEpochMilliseconds(entity.updatedAt))
        )
    }
    
    /**
     * 获取日期范围内的交易统计
     */
    suspend fun getTransactionStatsByDateRange(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): TransactionStats {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        // 获取收入和支出总额
        val income = transactionDao.getTotalByType(userId, startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByType(userId, startMillis, endMillis, "EXPENSE") ?: 0
        
        // 获取交易数量
        val transactions = transactionDao.getTransactionsByDateRangeSync(userId, startMillis, endMillis)
        val transactionCount = transactions.size
        
        // 获取分类统计
        val incomeStats = transactionDao.getCategoryStatisticsByType(userId, startMillis, endMillis, "INCOME")
        val expenseStats = transactionDao.getCategoryStatisticsByType(userId, startMillis, endMillis, "EXPENSE")
        
        val categoryStats = (incomeStats + expenseStats).map { stat ->
            val category = categoryDao.getCategoryById(stat.categoryId)
            val totalByType = if (category?.type == "INCOME") income else expense
            CategoryStat(
                categoryId = stat.categoryId,
                categoryName = stat.categoryName,
                categoryIcon = stat.categoryIcon,
                categoryColor = stat.categoryColor,
                totalAmount = stat.totalAmount,
                transactionCount = stat.transactionCount,
                percentage = if (totalByType > 0) (stat.totalAmount.toFloat() / totalByType * 100) else 0f
            )
        }
        
        return TransactionStats(
            totalIncome = income,
            totalExpense = expense,
            transactionCount = transactionCount,
            categoryStats = categoryStats
        )
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
    
    /**
     * 将实体转换为TransactionItem
     */
    private suspend fun TransactionEntity.toTransactionItem(): TransactionItem {
        val category = categoryDao.getCategoryById(categoryId)
        val account = accountDao.getAccountById(accountId)
        
        return TransactionItem(
            id = id,
            amount = amountCents / 100.0,
            categoryName = category?.name ?: "未知分类",
            categoryIcon = category?.icon,
            categoryColor = category?.color ?: "#808080",
            accountName = account?.name ?: "未知账户",
            note = note,
            date = com.ccxiaoji.core.common.util.DateConverter.toJavaDate(
                KotlinInstant.fromEpochMilliseconds(createdAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            )
        )
    }
}
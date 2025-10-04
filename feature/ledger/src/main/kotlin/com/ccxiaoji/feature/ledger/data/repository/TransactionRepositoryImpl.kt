package com.ccxiaoji.feature.ledger.data.repository

import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.ccxiaoji.feature.ledger.presentation.widget.WidgetRefreshBroadcaster

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
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
                    toDomainModelWithEnrichment(entity, categoryDetails)
                }
            }
            .flowOn(Dispatchers.IO)
    }
    
    override fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return transactionDao.getTransactionsByDateRange(userApi.getCurrentUserId(), startMillis, endMillis)
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
                    toDomainModelWithEnrichment(entity, categoryDetails)
                }
            }
            .flowOn(Dispatchers.IO)
    }
    
    
    override suspend fun getMonthlyTotal(year: Int, month: Int): BaseResult<Int> = safeSuspendCall {
        withContext(Dispatchers.IO) {
            val startDate = LocalDate(year, month, 1)
            val endDate = startDate.plus(1, DateTimeUnit.MONTH)

            val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

            transactionDao.getTotalAmountByDateRange(userApi.getCurrentUserId(), startMillis, endMillis) ?: 0
        }
    }
    
    
    override suspend fun getMonthlyIncomesAndExpenses(year: Int, month: Int): BaseResult<Pair<Int, Int>> = safeSuspendCall {
        withContext(Dispatchers.IO) {
            val startDate = LocalDate(year, month, 1)
            val endDate = startDate.plus(1, DateTimeUnit.MONTH)

            val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

            val currentUserId = userApi.getCurrentUserId()

            // Use new category-based queries
            val income = transactionDao.getTotalByType(currentUserId, startMillis, endMillis, "INCOME") ?: 0
            val expense = transactionDao.getTotalByType(currentUserId, startMillis, endMillis, "EXPENSE") ?: 0

            income to expense
        }
    }
    
    override suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String,
        ledgerId: String,
        transactionDate: kotlinx.datetime.Instant?,
        location: com.ccxiaoji.feature.ledger.domain.model.LocationData?,
        transactionId: String?
    ): BaseResult<String> = safeSuspendCall { withContext(Dispatchers.IO) {
        val actualTransactionId = transactionId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        println("馃攳 [TransactionRepo] 浣跨敤浜ゆ槗ID: '$actualTransactionId'")
        println("馃攳 [TransactionRepo] ID鏉ユ簮: ${if (transactionId != null) "澶栭儴浼犲叆" else "鍐呴儴鐢熸垚"}")
        
        // Use provided accountId
        val actualAccountId = accountId
        
        // Get category details
        val categoryEntity = categoryDao.getCategoryById(categoryId)
        
        val entity = TransactionEntity(
            id = actualTransactionId,
            userId = userApi.getCurrentUserId(),
            accountId = actualAccountId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            ledgerId = ledgerId,
            createdAt = now,
            updatedAt = now,
            transactionDate = transactionDate?.toEpochMilliseconds() ?: now,
            locationLatitude = location?.latitude,
            locationLongitude = location?.longitude,
            locationAddress = location?.address,
            locationPrecision = location?.precision,
            locationProvider = location?.provider,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        transactionDao.insertTransaction(entity)
        
        // Update account balance
        val category = categoryEntity
        val balanceChange = if (category?.type == "INCOME") amountCents.toLong() else -amountCents.toLong()
        accountDao.updateBalance(actualAccountId, balanceChange, now)
        
        // Increment category usage count锛堣烦杩囪浆璐︾浉鍏冲垎绫伙級
        val isTransferLike = category?.name?.contains("杞处") == true
        if (!isTransferLike) {
            categoryDao.incrementUsageCount(categoryId)
        }
        
        // Log the change for sync
        logChange("transactions", actualTransactionId, "INSERT", entity)
        // Notify widgets to refresh (fire-and-forget)
        try { com.ccxiaoji.feature.ledger.presentation.widget.WidgetRefreshBroadcaster.send(appContext) } catch (_: Exception) {}
        
        println("鉁?[TransactionRepo] 浜ゆ槗鍒涘缓鎴愬姛: '$actualTransactionId'")
        actualTransactionId
    }}
    
    override suspend fun updateTransaction(transaction: Transaction): BaseResult<Unit> = safeSuspendCall { withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val entity = transaction.toEntity(userApi.getCurrentUserId(), now)
        
        transactionDao.updateTransaction(entity)
        
        // Log the change for sync
        logChange("transactions", transaction.id, "UPDATE", entity)
        // Notify widgets to refresh
        try { com.ccxiaoji.feature.ledger.presentation.widget.WidgetRefreshBroadcaster.send(appContext) } catch (_: Exception) {}
    }}
    
    override suspend fun deleteTransaction(transactionId: String): BaseResult<Unit> = safeSuspendCall { withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        
        transactionDao.softDeleteTransaction(transactionId, now)
        
        // Log the change for sync
        logChange("transactions", transactionId, "DELETE", mapOf("id" to transactionId))
        // Notify widgets to refresh
        try { com.ccxiaoji.feature.ledger.presentation.widget.WidgetRefreshBroadcaster.send(appContext) } catch (_: Exception) {}
    }}
    
    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactionsByUser(userApi.getCurrentUserId(), limit)
            .map { entities ->
                val list = mutableListOf<Transaction>()
                for (entity in entities) {
                    list.add(toDomainModelWithEnrichment(entity, null))
                }
                list
            }
            .flowOn(Dispatchers.IO)
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
            .map { entities ->
                val list = mutableListOf<Transaction>()
                for (entity in entities) {
                    list.add(toDomainModelWithEnrichment(entity, null))
                }
                list
            }
            .flowOn(Dispatchers.IO)
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
                    toDomainModelWithEnrichment(entity, categoryDetails)
                }
            }
            .flowOn(Dispatchers.IO)
    }
    
    override fun getTransactionsByCategory(categoryId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUser(userApi.getCurrentUserId())
            .map { entities -> 
                entities.filter { it.categoryId == categoryId }.map { entity ->
                    entity.toDomainModel()
                }
            }
            .flowOn(Dispatchers.IO)
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
                    toDomainModelWithEnrichment(entity, categoryDetails)
                }
            }
            .flowOn(Dispatchers.IO)
    }
    
    // Statistics methods
    override suspend fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): BaseResult<Map<LocalDate, Pair<Int, Int>>> = safeSuspendCall { withContext(Dispatchers.IO) {
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
    }}
    
    override suspend fun getCategoryStatistics(
        categoryType: String?,
        startDate: Long,
        endDate: Long
    ): BaseResult<List<CategoryStatistic>> = safeSuspendCall { withContext(Dispatchers.IO) {
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
    }}
    
    override suspend fun getTopTransactions(startDate: LocalDate, endDate: LocalDate, type: String, limit: Int): BaseResult<List<Transaction>> = safeSuspendCall { withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val topEntities = transactionDao.getTopTransactionsByType(userApi.getCurrentUserId(), startMillis, endMillis, type, limit)
        val list = mutableListOf<Transaction>()
        for (entity in topEntities) {
            val categoryDetails = categoryDao.getCategoryById(entity.categoryId)?.let { category ->
                CategoryDetails(
                    id = category.id,
                    name = category.name,
                    icon = category.icon,
                    color = category.color,
                    type = category.type
                )
            }
            list.add(toDomainModelWithEnrichment(entity, categoryDetails))
        }
        list
    }}

    // 鏋勫缓甯﹀绔处鎴蜂俊鎭殑棰嗗煙妯″瀷
    private suspend fun toDomainModelWithEnrichment(entity: TransactionEntity, categoryDetails: CategoryDetails?): Transaction {
        val base = entity.toDomainModel(categoryDetails)
        val relatedId = entity.relatedTransactionId
        var cpId: String? = null
        var cpName: String? = null
        if (relatedId != null) {
            val other = transactionDao.getTransactionById(relatedId)
            cpId = other?.accountId
            if (cpId != null) {
                cpName = accountDao.getAccountById(cpId!!)?.name
            }
        }
        return base.copy(counterpartyAccountId = cpId, counterpartyAccountName = cpName)
    }
    
    override suspend fun calculateSavingsRate(startDate: LocalDate, endDate: LocalDate): BaseResult<Float> = safeSuspendCall { withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val income = transactionDao.getTotalByType(userApi.getCurrentUserId(), startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByType(userApi.getCurrentUserId(), startMillis, endMillis, "EXPENSE") ?: 0
        
        if (income > 0) {
            ((income - expense).toFloat() / income) * 100
        } else {
            0f
        }
    }}
    
    override suspend fun getTransactionById(transactionId: String): Transaction? {
        return withContext(Dispatchers.IO) {
            val entity = transactionDao.getTransactionById(transactionId) ?: return@withContext null
            val categoryDetails = categoryDao.getCategoryById(entity.categoryId)?.let { category ->
                CategoryDetails(
                    id = category.id,
                    name = category.name,
                    icon = category.icon,
                    color = category.color,
                    type = category.type
                )
            }
            toDomainModelWithEnrichment(entity, categoryDetails)
        }
    }
    
    override fun getTransactionsPaginated(
        offset: Int,
        limit: Int,
        accountId: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<BaseResult<Pair<List<Transaction>, Int>>> = flow<BaseResult<Pair<List<Transaction>, Int>>> {
        val currentUserId = userApi.getCurrentUserId()
        
        val result = transactionDao.getTransactionsPaginated(
            userId = currentUserId,
            offset = offset,
            limit = limit,
            accountId = accountId,
            startDateMillis = startDate,
            endDateMillis = endDate
        )
        
        val transactions = mutableListOf<Transaction>()
        for (entity in result.first) {
            val categoryDetails = categoryDao.getCategoryById(entity.categoryId)?.let { category ->
                CategoryDetails(
                    id = category.id,
                    name = category.name,
                    icon = category.icon,
                    color = category.color,
                    type = category.type
                )
            }
            transactions.add(toDomainModelWithEnrichment(entity, categoryDetails))
        }
        
        emit(BaseResult.Success(Pair(transactions, result.second)))
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(BaseResult.Error(if (e is Exception) e else Exception(e)))
    }
    
    // 璁拌处绨跨浉鍏崇殑鏂规硶瀹炵幇
    override fun getTransactionsByLedger(ledgerId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByLedger(ledgerId)
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
            .flowOn(Dispatchers.IO)
    }
    
    override fun getTransactionsByLedgerAndDateRange(
        ledgerId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>> {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return transactionDao.getTransactionsByLedgerAndDateRange(ledgerId, startMillis, endMillis)
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
            .flowOn(Dispatchers.IO)
    }
    
    override fun getTransactionsByLedgers(ledgerIds: List<String>): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByLedgers(ledgerIds)
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getTransactionsByLedgerFlow(
        ledgerId: String,
        startMillis: Long,
        endMillis: Long,
        accountId: String?
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByLedgerAndDateRangeWithAccount(
            ledgerId = ledgerId,
            startTime = startMillis,
            endTime = endMillis,
            accountId = accountId
        ).map { entities ->
            // 富化分类信息，避免 UI 出现“未分类”
            entities.map { entity ->
                val category = categoryDao.getCategoryByIdSync(entity.categoryId)
                val details = category?.let { c ->
                    CategoryDetails(
                        id = c.id,
                        name = c.name,
                        icon = c.icon,
                        color = c.color,
                        type = c.type
                    )
                }
                toDomainModelWithEnrichment(entity, details)
            }
        }.flowOn(Dispatchers.IO)
    }
    
    override suspend fun getMonthlyIncomesAndExpensesByLedger(
        ledgerId: String,
        year: Int,
        month: Int
    ): BaseResult<Pair<Int, Int>> {
        return safeSuspendCall { withContext(Dispatchers.IO) {
            val startOfMonth = LocalDate(year, month, 1)
            val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            val startMillis = startOfMonth.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = endOfMonth.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            
            val result = transactionDao.getMonthlyIncomesAndExpensesByLedger(ledgerId, startMillis, endMillis)
            Pair(result?.income ?: 0, result?.expense ?: 0)
        }}
    }

    override fun getMonthlyIncomesAndExpensesByLedgerFlow(
        ledgerId: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<Pair<Int, Int>> {
        return transactionDao.getMonthlyIncomesAndExpensesByLedgerFlow(ledgerId, startMillis, endMillis)
            .map { pair ->
                // pair 鍙兘涓?null锛堟棤璁板綍锛夛紝缁熶竴杩斿洖 0
                val income = pair?.income ?: 0
                val expense = pair?.expense ?: 0
                income to expense
            }
            .flowOn(Dispatchers.IO)
    }
    
    // 璁拌处绨跨瓫閫夌殑缁熻鏂规硶瀹炵幇
    override suspend fun getCategoryStatisticsByLedger(
        ledgerId: String,
        categoryType: String?,
        startDate: Long,
        endDate: Long
    ): BaseResult<List<CategoryStatistic>> = safeSuspendCall { withContext(Dispatchers.IO) {
        val stats = if (categoryType != null) {
            transactionDao.getCategoryStatisticsByLedgerAndType(ledgerId, startDate, endDate, categoryType)
        } else {
            transactionDao.getCategoryStatisticsByLedgerAndType(ledgerId, startDate, endDate, "EXPENSE")
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
    }}
    
    override suspend fun getCategoryStatisticsByLedgers(
        ledgerIds: List<String>,
        categoryType: String?,
        startDate: Long,
        endDate: Long
    ): BaseResult<List<CategoryStatistic>> = safeSuspendCall { withContext(Dispatchers.IO) {
        val stats = if (categoryType != null) {
            transactionDao.getCategoryStatisticsByLedgersAndType(ledgerIds, startDate, endDate, categoryType)
        } else {
            transactionDao.getCategoryStatisticsByLedgersAndType(ledgerIds, startDate, endDate, "EXPENSE")
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
    }}
    
    override suspend fun getDailyTotalsByLedger(
        ledgerId: String,
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Map<LocalDate, Pair<Int, Int>>> = safeSuspendCall { withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val transactions = transactionDao.getTransactionsByLedgerAndDateRangeSync(ledgerId, startMillis, endMillis)
        
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
    }}
    
    override suspend fun getDailyTotalsByLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Map<LocalDate, Pair<Int, Int>>> = safeSuspendCall { withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val transactions = transactionDao.getTransactionsByLedgersAndDateRangeSync(ledgerIds, startMillis, endMillis)
        
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
    }}
    
    override suspend fun getTopTransactionsByLedger(
        ledgerId: String,
        startDate: LocalDate, 
        endDate: LocalDate, 
        type: String, 
        limit: Int
    ): BaseResult<List<Transaction>> = safeSuspendCall { withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        transactionDao.getTopTransactionsByLedgerAndType(ledgerId, startMillis, endMillis, type, limit)
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
    }}
    
    override suspend fun getTopTransactionsByLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate, 
        endDate: LocalDate, 
        type: String, 
        limit: Int
    ): BaseResult<List<Transaction>> = safeSuspendCall { withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        transactionDao.getTopTransactionsByLedgersAndType(ledgerIds, startMillis, endMillis, type, limit)
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
    }}
    
    override suspend fun calculateSavingsRateByLedger(
        ledgerId: String,
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Float> = safeSuspendCall { withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val income = transactionDao.getTotalByLedgerAndType(ledgerId, startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByLedgerAndType(ledgerId, startMillis, endMillis, "EXPENSE") ?: 0
        
        if (income > 0) {
            ((income - expense).toFloat() / income) * 100
        } else {
            0f
        }
    }}
    
    override suspend fun calculateSavingsRateByLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Float> = safeSuspendCall { withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val income = transactionDao.getTotalByLedgersAndType(ledgerIds, startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByLedgersAndType(ledgerIds, startMillis, endMillis, "EXPENSE") ?: 0
        
        if (income > 0) {
            ((income - expense).toFloat() / income) * 100
        } else {
            0f
        }
    }}
    
    override fun getTransactionsPaginatedByLedger(
        ledgerId: String,
        offset: Int,
        limit: Int,
        accountId: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<BaseResult<Pair<List<Transaction>, Int>>> = flow<BaseResult<Pair<List<Transaction>, Int>>> {
        val result = transactionDao.getTransactionsPaginatedByLedger(
            ledgerId = ledgerId,
            offset = offset,
            limit = limit,
            accountId = accountId,
            startDateMillis = startDate,
            endDateMillis = endDate
        )
        
        val transactions = mutableListOf<Transaction>()
        for (entity in result.first) {
            val categoryDetails = categoryDao.getCategoryById(entity.categoryId)?.let { category ->
                CategoryDetails(
                    id = category.id,
                    name = category.name,
                    icon = category.icon,
                    color = category.color,
                    type = category.type
                )
            }
            transactions.add(toDomainModelWithEnrichment(entity, categoryDetails))
        }
        
        emit(BaseResult.Success(Pair(transactions, result.second)))
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(BaseResult.Error(if (e is Exception) e else Exception(e)))
    }
    
    override fun getTransactionsPaginatedByLedgers(
        ledgerIds: List<String>,
        offset: Int,
        limit: Int,
        accountId: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<BaseResult<Pair<List<Transaction>, Int>>> = flow<BaseResult<Pair<List<Transaction>, Int>>> {
        val result = transactionDao.getTransactionsPaginatedByLedgers(
            ledgerIds = ledgerIds,
            offset = offset,
            limit = limit,
            accountId = accountId,
            startDateMillis = startDate,
            endDateMillis = endDate
        )
        
        val transactions = result.first.map { entity ->
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
        
        emit(BaseResult.Success(Pair(transactions, result.second)))
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(BaseResult.Error(if (e is Exception) e else Exception(e)))
    }
}

private fun TransactionEntity.toDomainModel(categoryDetails: CategoryDetails? = null): Transaction {
    val location = if (locationLatitude != null && locationLongitude != null) {
        com.ccxiaoji.feature.ledger.domain.model.LocationData(
            latitude = locationLatitude,
            longitude = locationLongitude,
            address = locationAddress,
            precision = locationPrecision,
            provider = locationProvider
        )
    } else null
    
    return Transaction(
        id = id,
        accountId = accountId,
        amountCents = amountCents,
        categoryId = categoryId,
        categoryDetails = categoryDetails,
        note = note,
        ledgerId = ledgerId,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        transactionDate = transactionDate?.let { Instant.fromEpochMilliseconds(it) },
        location = location,
        transferId = transferId,
        transferType = transferType?.let { com.ccxiaoji.feature.ledger.domain.model.TransferType.valueOf(it) },
        relatedTransactionId = relatedTransactionId
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
        ledgerId = ledgerId,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt,
        transactionDate = transactionDate?.toEpochMilliseconds(),
        locationLatitude = location?.latitude,
        locationLongitude = location?.longitude,
        locationAddress = location?.address,
        locationPrecision = location?.precision,
        locationProvider = location?.provider,
        syncStatus = SyncStatus.PENDING_SYNC,
        transferId = transferId,
        transferType = transferType?.name,
        relatedTransactionId = relatedTransactionId
    )
}

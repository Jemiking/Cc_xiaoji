package com.ccxiaoji.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.app.data.local.CcDatabase
import com.ccxiaoji.app.data.sync.SyncStatus
import com.ccxiaoji.app.data.remote.api.SyncApi
import com.ccxiaoji.app.data.remote.api.ConflictItem
import com.ccxiaoji.app.data.repository.UserRepository
import com.ccxiaoji.app.data.local.entity.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: CcDatabase,
    private val syncApi: SyncApi,
    private val userRepository: UserRepository,
    private val gson: Gson
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentUser = userRepository.getCurrentUser() ?: return@withContext Result.failure()
            
            // Upload local changes
            uploadChanges()
            
            // Download remote changes
            val lastSyncTime = userRepository.getLastSyncTime()
            downloadChanges(lastSyncTime)
            
            // Update last sync time
            userRepository.updateLastSyncTime(System.currentTimeMillis())
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun uploadChanges() {
        val changeLogDao = database.changeLogDao()
        val pendingChanges = changeLogDao.getPendingChanges(limit = BATCH_SIZE)
        
        if (pendingChanges.isEmpty()) return
        
        val uploadRequest = pendingChanges.map { change ->
            SyncUploadItem(
                table = change.tableName,
                rowId = change.rowId,
                operation = change.operation,
                payload = change.payload,
                timestamp = change.timestamp
            )
        }
        
        val response = syncApi.uploadChanges(uploadRequest)
        if (response.isSuccessful) {
            val uploadResponse = response.body()
            
            // Handle conflicts if any
            uploadResponse?.conflicts?.forEach { conflict ->
                handleConflict(conflict)
            }
            
            // Mark successfully synced changes
            changeLogDao.updateSyncStatus(
                pendingChanges.map { it.id },
                SyncStatus.SYNCED
            )
            
            // Update server time
            uploadResponse?.serverTime?.let { serverTime ->
                userRepository.updateServerTime(serverTime)
            }
        } else {
            // Handle upload error
            throw Exception("Upload failed: ${response.code()} ${response.message()}")
        }
    }
    
    private suspend fun downloadChanges(since: Long) {
        val response = syncApi.getChanges(since)
        if (!response.isSuccessful) return
        
        val changes = response.body() ?: return
        
        // Apply changes to local database
        changes.forEach { change ->
            when (change.table) {
                "transactions" -> applyTransactionChanges(change)
                "tasks" -> applyTaskChanges(change)
                "habits" -> applyHabitChanges(change)
                "habit_records" -> applyHabitRecordChanges(change)
                "countdowns" -> applyCountdownChanges(change)
                "accounts" -> applyAccountChanges(change)
                "categories" -> applyCategoryChanges(change)
                "budgets" -> applyBudgetChanges(change)
                "savings_goals" -> applySavingsGoalChanges(change)
                "savings_contributions" -> applySavingsContributionChanges(change)
                "recurring_transactions" -> applyRecurringTransactionChanges(change)
            }
        }
    }
    
    private suspend fun applyTransactionChanges(change: SyncChange) {
        val transactionDao = database.transactionDao()
        val type = object : TypeToken<List<TransactionEntity>>() {}.type
        val transactions: List<TransactionEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        transactions.forEach { transaction ->
            // 检查本地是否存在该记录
            val localTransaction = transactionDao.getTransactionByIdSync(transaction.id)
            
            if (localTransaction == null) {
                // 新记录，直接插入
                transactionDao.insertTransaction(transaction.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                // 更新记录，使用服务器版本（最后写入优先策略）
                if (transaction.updatedAt > localTransaction.updatedAt) {
                    transactionDao.updateTransaction(transaction.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applyTaskChanges(change: SyncChange) {
        val taskDao = database.taskDao()
        val type = object : TypeToken<List<TaskEntity>>() {}.type
        val tasks: List<TaskEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        tasks.forEach { task ->
            val localTask = taskDao.getTaskByIdSync(task.id)
            
            if (localTask == null) {
                taskDao.insertTask(task.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (task.updatedAt > localTask.updatedAt) {
                    taskDao.updateTask(task.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applyHabitChanges(change: SyncChange) {
        val habitDao = database.habitDao()
        val type = object : TypeToken<List<HabitEntity>>() {}.type
        val habits: List<HabitEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        habits.forEach { habit ->
            val localHabit = habitDao.getHabitById(habit.id)
            
            if (localHabit == null) {
                habitDao.insertHabit(habit.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (habit.updatedAt > localHabit.updatedAt) {
                    habitDao.updateHabit(habit.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applyHabitRecordChanges(change: SyncChange) {
        val habitDao = database.habitDao()
        val type = object : TypeToken<List<HabitRecordEntity>>() {}.type
        val records: List<HabitRecordEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        records.forEach { record ->
            // 习惯记录通常按日期唯一，使用habitId+recordDate检查
            val localRecord = habitDao.getHabitRecordByDate(record.habitId, record.recordDate)
            
            if (localRecord == null) {
                habitDao.insertHabitRecord(record.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (record.updatedAt > localRecord.updatedAt) {
                    habitDao.updateHabitRecord(record.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applyCountdownChanges(change: SyncChange) {
        val countdownDao = database.countdownDao()
        val type = object : TypeToken<List<CountdownEntity>>() {}.type
        val countdowns: List<CountdownEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        countdowns.forEach { countdown ->
            val localCountdown = countdownDao.getCountdownByIdSync(countdown.id)
            
            if (localCountdown == null) {
                countdownDao.insertCountdown(countdown.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (countdown.updatedAt > localCountdown.updatedAt) {
                    countdownDao.updateCountdown(countdown.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applyAccountChanges(change: SyncChange) {
        val accountDao = database.accountDao()
        val type = object : TypeToken<List<AccountEntity>>() {}.type
        val accounts: List<AccountEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        accounts.forEach { account ->
            val localAccount = accountDao.getAccountByIdSync(account.id)
            
            if (localAccount == null) {
                accountDao.insertAccount(account.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (account.updatedAt > localAccount.updatedAt) {
                    accountDao.updateAccount(account.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applyCategoryChanges(change: SyncChange) {
        val categoryDao = database.categoryDao()
        val type = object : TypeToken<List<CategoryEntity>>() {}.type
        val categories: List<CategoryEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        categories.forEach { category ->
            val localCategory = categoryDao.getCategoryByIdSync(category.id)
            
            if (localCategory == null) {
                categoryDao.insertCategory(category.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (category.updatedAt > localCategory.updatedAt) {
                    categoryDao.updateCategory(category.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applyBudgetChanges(change: SyncChange) {
        val budgetDao = database.budgetDao()
        val type = object : TypeToken<List<BudgetEntity>>() {}.type
        val budgets: List<BudgetEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        budgets.forEach { budget ->
            val localBudget = budgetDao.getBudgetByIdSync(budget.id)
            
            if (localBudget == null) {
                budgetDao.insertBudget(budget.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (budget.updatedAt > localBudget.updatedAt) {
                    budgetDao.updateBudget(budget.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applySavingsGoalChanges(change: SyncChange) {
        val savingsGoalDao = database.savingsGoalDao()
        val type = object : TypeToken<List<SavingsGoalEntity>>() {}.type
        val goals: List<SavingsGoalEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        goals.forEach { goal ->
            val localGoal = savingsGoalDao.getSavingsGoalByIdSync(goal.id)
            
            if (localGoal == null) {
                savingsGoalDao.insertSavingsGoal(goal.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (goal.updatedAt > localGoal.updatedAt) {
                    savingsGoalDao.updateSavingsGoal(goal.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applySavingsContributionChanges(change: SyncChange) {
        val savingsGoalDao = database.savingsGoalDao()
        val type = object : TypeToken<List<SavingsContributionEntity>>() {}.type
        val contributions: List<SavingsContributionEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        contributions.forEach { contribution ->
            val localContribution = savingsGoalDao.getContributionByIdSync(contribution.id)
            
            if (localContribution == null) {
                savingsGoalDao.insertContribution(contribution.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (contribution.createdAt > localContribution.createdAt) {
                    savingsGoalDao.updateContribution(contribution.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun applyRecurringTransactionChanges(change: SyncChange) {
        val recurringTransactionDao = database.recurringTransactionDao()
        val type = object : TypeToken<List<RecurringTransactionEntity>>() {}.type
        val recurringTransactions: List<RecurringTransactionEntity> = gson.fromJson(gson.toJson(change.rows), type)
        
        recurringTransactions.forEach { recurring ->
            val localRecurring = recurringTransactionDao.getRecurringTransactionByIdSync(recurring.id)
            
            if (localRecurring == null) {
                recurringTransactionDao.insertRecurringTransaction(recurring.copy(syncStatus = SyncStatus.SYNCED))
            } else {
                if (recurring.updatedAt > localRecurring.updatedAt) {
                    recurringTransactionDao.updateRecurringTransaction(recurring.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }
    
    private suspend fun handleConflict(conflict: ConflictItem) {
        // 冲突解决策略：使用服务器版本（最后写入优先）
        // 在实际应用中，可以根据resolution字段选择不同的策略
        when (conflict.resolution) {
            "server_wins" -> {
                // 服务器版本优先，不需要额外操作
                // 下载阶段会自动应用服务器版本
            }
            "client_wins" -> {
                // 客户端版本优先，重新标记为待同步
                val changeLogDao = database.changeLogDao()
                changeLogDao.markForResync(conflict.table, conflict.rowId)
            }
            "merge" -> {
                // 合并策略，需要根据具体业务逻辑实现
                // TODO: 实现合并逻辑
            }
            else -> {
                // 默认使用服务器版本
            }
        }
    }
    
    companion object {
        private const val MAX_RETRY_COUNT = 3
        private const val BATCH_SIZE = 100
        const val SYNC_WORK_NAME = "periodic_sync"
        
        fun buildPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            return PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
        
        fun buildOneTimeWorkRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            return OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
    }
}

data class SyncUploadItem(
    val table: String,
    val rowId: String,
    val operation: String,
    val payload: String,
    val timestamp: Long
)

data class SyncChange(
    val table: String,
    val rows: List<Map<String, Any>>
)
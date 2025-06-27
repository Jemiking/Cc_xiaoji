package com.ccxiaoji.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.app.data.local.CcDatabase
import com.ccxiaoji.common.base.BaseWorker
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.sync.data.remote.api.SyncService
import com.ccxiaoji.shared.sync.data.remote.dto.ConflictItem
import com.ccxiaoji.shared.sync.data.remote.dto.SyncUploadItem
import com.ccxiaoji.shared.sync.data.remote.dto.SyncChange
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.app.data.local.entity.CountdownEntity
import com.ccxiaoji.shared.sync.data.local.entity.ChangeLogEntity
import com.ccxiaoji.feature.ledger.data.local.entity.*
import com.ccxiaoji.feature.todo.data.local.entity.TaskEntity
import com.ccxiaoji.feature.habit.data.local.entity.HabitEntity
import com.ccxiaoji.feature.habit.data.local.entity.HabitRecordEntity
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
    private val syncService: SyncService,
    private val userApi: UserApi,
    private val gson: Gson
) : BaseWorker(context, params) {
    
    override fun getWorkerName(): String = "SyncWorker"
    
    override fun getMaxRetryCount(): Int = MAX_RETRY_COUNT
    
    override suspend fun performWork(): Result {
        val currentUser = userApi.getCurrentUser() 
        if (currentUser == null) {
            logError("No current user found, skipping sync")
            return Result.failure()
        }
        
        logInfo("Starting sync for user: ${currentUser.email}")
        
        // Upload local changes
        uploadChanges()
        
        // Download remote changes
        val lastSyncTime = userApi.getLastSyncTime()
        downloadChanges(lastSyncTime)
        
        // Update last sync time
        userApi.updateLastSyncTime(System.currentTimeMillis())
        
        logInfo("Sync completed successfully")
        return Result.success()
    }
    
    private suspend fun uploadChanges() {
        val changeLogDao = database.changeLogDao()
        val pendingChanges = changeLogDao.getPendingChanges(limit = BATCH_SIZE)
        
        if (pendingChanges.isEmpty()) {
            logInfo("No pending changes to upload")
            return
        }
        
        logInfo("Uploading ${pendingChanges.size} changes")
        updateProgress(10) // 10% progress for starting upload
        
        val uploadRequest = pendingChanges.map { change ->
            SyncUploadItem(
                table = change.tableName,
                rowId = change.rowId,
                operation = change.operation,
                payload = change.payload,
                timestamp = change.timestamp
            )
        }
        
        val response = syncService.uploadChanges(uploadRequest)
        if (response.isSuccessful) {
            val uploadResponse = response.body()
            
            // Handle conflicts if any
            uploadResponse?.conflicts?.forEach { conflict ->
                logWarning("Conflict detected for ${conflict.table}:${conflict.rowId}")
                handleConflict(conflict)
            }
            
            // Mark successfully synced changes
            changeLogDao.updateSyncStatus(
                pendingChanges.map { it.id },
                SyncStatus.SYNCED
            )
            
            // Update server time
            uploadResponse?.serverTime?.let { serverTime ->
                userApi.updateServerTime(serverTime)
            }
            
            logInfo("Upload completed successfully")
            updateProgress(50) // 50% progress after upload
        } else {
            // Handle upload error
            logError("Upload failed: ${response.code()} ${response.message()}")
            throw Exception("Upload failed: ${response.code()} ${response.message()}")
        }
    }
    
    private suspend fun downloadChanges(since: Long) {
        logInfo("Downloading changes since: $since")
        
        val response = syncService.getChanges(since)
        if (!response.isSuccessful) {
            logError("Failed to download changes: ${response.code()} ${response.message()}")
            return
        }
        
        val changes = response.body()
        if (changes == null || changes.isEmpty()) {
            logInfo("No new changes to download")
            return
        }
        
        logInfo("Downloading ${changes.size} changes")
        updateProgress(60) // 60% progress for starting download
        
        // Apply changes to local database
        changes.forEachIndexed { index, change ->
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
            
            // Update progress
            val progress = 60 + (30 * (index + 1) / changes.size)
            updateProgress(progress)
        }
        
        logInfo("Download and apply completed successfully")
        updateProgress(90) // 90% progress after download
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
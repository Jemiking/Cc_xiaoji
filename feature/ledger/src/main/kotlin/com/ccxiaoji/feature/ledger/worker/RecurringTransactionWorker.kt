package com.ccxiaoji.feature.ledger.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.common.base.BaseWorker
import com.ccxiaoji.feature.ledger.data.repository.RecurringTransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * 定期交易执行Worker
 * 负责执行到期的定期交易（如房租、订阅费等）
 */
@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringTransactionRepository: RecurringTransactionRepository
) : BaseWorker(context, workerParams) {
    
    override fun getWorkerName(): String = "RecurringTransactionWorker"
    
    override suspend fun performWork(): Result {
        val executedCount = recurringTransactionRepository.executeDueRecurringTransactions()
        
        if (executedCount > 0) {
            logInfo("Executed $executedCount recurring transactions")
            // TODO: 发送通知给用户
        } else {
            logInfo("No recurring transactions to execute")
        }
        
        return Result.success(
            workDataOf("executed_count" to executedCount)
        )
    }
    
    companion object {
        const val WORK_NAME = "recurring_transaction_worker"
        
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            return PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
                1, TimeUnit.DAYS // 每天执行一次
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()
        }
        
        fun createOneTimeWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<RecurringTransactionWorker>()
                .build()
        }
        
        private fun calculateInitialDelay(): Long {
            val now = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = now
                add(java.util.Calendar.DAY_OF_MONTH, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            
            return calendar.timeInMillis - now
        }
    }
}
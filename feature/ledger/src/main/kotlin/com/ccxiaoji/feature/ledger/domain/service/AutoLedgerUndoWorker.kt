package com.ccxiaoji.feature.ledger.domain.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 自动记账撤销Worker
 * 
 * 功能：
 * - 幂等的撤销已记账交易
 * - 发送撤销成功通知
 * - 记录撤销操作日志
 */
@HiltWorker
class AutoLedgerUndoWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val notificationManager: AutoLedgerNotificationManager
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "AutoLedgerUndoWorker"
    }
    
    override suspend fun doWork(): Result {
        val transactionId = inputData.getString("transactionId")
        val notificationId = inputData.getInt("notificationId", -1)
        
        if (transactionId == null) {
            Log.e(TAG, "Missing transactionId in input data")
            return Result.failure()
        }
        
        return try {
            Log.d(TAG, "Starting undo operation for transaction: $transactionId")
            
            // 查找交易记录
            val transaction = transactionRepository.getTransactionById(transactionId)
            if (transaction == null) {
                Log.w(TAG, "Transaction not found or already deleted: $transactionId")
                // 这可能是重复撤销操作，视为成功
                return Result.success()
            }
            
            // 检查是否为自动记账产生的交易（通过某个标记字段）
            if (!isAutoLedgerTransaction(transaction)) {
                Log.w(TAG, "Attempted to undo non-auto-ledger transaction: $transactionId")
                return Result.failure()
            }
            
            // 记录交易摘要，用于通知显示
            val transactionSummary = formatTransactionSummary(transaction)
            
            // 执行删除操作（幂等的）
            val deleteResult = transactionRepository.deleteTransaction(transactionId)
            
            when (deleteResult) {
                is com.ccxiaoji.common.base.BaseResult.Success -> {
                    Log.d(TAG, "Transaction successfully deleted: $transactionId")
                    
                    // 发送撤销成功通知
                    notificationManager.showUndoSuccessNotification(transactionSummary)
                    
                    Result.success()
                }
                is com.ccxiaoji.common.base.BaseResult.Error -> {
                    Log.w(TAG, "Transaction deletion failed: ${deleteResult.exception?.message ?: "Unknown error"}")
                    // 可能已经被删除，视为成功
                    Result.success()
                }
                else -> {
                    Log.w(TAG, "Unknown result type for transaction deletion")
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to undo transaction: $transactionId", e)
            Result.retry()
        }
    }
    
    /**
     * 检查是否为自动记账产生的交易
     * TODO: 需要在Transaction模型中添加标记字段，或通过其他方式识别
     */
    private fun isAutoLedgerTransaction(transaction: com.ccxiaoji.feature.ledger.domain.model.Transaction): Boolean {
        // 改进：标准化标记以提高可靠性（无需DB迁移）
        val note = transaction.note ?: return false
        return note.contains("[AUTO]") || note.contains("#auto") || note.contains("自动记账")
    }
    
    /**
     * 格式化交易摘要
     */
    private fun formatTransactionSummary(transaction: com.ccxiaoji.feature.ledger.domain.model.Transaction): String {
        // Transaction模型没有isIncome字段，暂时使用固定文本
        val amount = "%.2f".format(transaction.amountCents / 100.0)
        return "交易 ¥$amount"
    }
}

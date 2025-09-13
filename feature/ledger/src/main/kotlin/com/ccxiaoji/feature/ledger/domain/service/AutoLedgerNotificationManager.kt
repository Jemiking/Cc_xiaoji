package com.ccxiaoji.feature.ledger.domain.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.model.PaymentDirection
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.service.AutoLedgerUndoWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自动记账通知交互管理器
 * 
 * 功能：
 * - 自动落账后发送通知
 * - 提供撤销/编辑Action
 * - 管理30秒撤销窗口
 * - 支持半自动模式通知
 */
@Singleton
class AutoLedgerNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    
    companion object {
        private const val TAG = "AutoLedger_Popup"
        // 低重要级：状态类（成功/撤销结果等）
        private const val CHANNEL_STATUS_ID = "auto_ledger_status"
        private const val CHANNEL_STATUS_NAME = "自动记账状态"
        // 高重要级：提示类（半自动确认，需弹出横幅）
        private const val CHANNEL_PROMPT_ID = "auto_ledger_prompt"
        private const val CHANNEL_PROMPT_NAME = "自动记账确认"
        private const val UNDO_WINDOW_SECONDS = 30L

        // 通知Action的RequestCode
        private const val REQUEST_UNDO = 1001
        private const val REQUEST_EDIT = 1002
        private const val REQUEST_CONTENT = 1003
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        Log.i(TAG, "🔔 初始化自动记账通知管理器")
        createNotificationChannels()
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannels() {
        Log.d(TAG, "📱 检查通知渠道创建需求 (SDK: ${Build.VERSION.SDK_INT})")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 状态渠道（低重要级）
            runCatching {
                val statusChannel = NotificationChannel(
                    CHANNEL_STATUS_ID,
                    CHANNEL_STATUS_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "用于展示已记账/撤销等状态通知。"
                    setShowBadge(false)
                    enableVibration(false)
                    enableLights(false)
                }
                notificationManager.createNotificationChannel(statusChannel)
                Log.i(TAG, "✅ 通知渠道创建完成: $CHANNEL_STATUS_NAME")
            }

            // 提示渠道（高重要级，触发HUN）
            runCatching {
                val promptChannel = NotificationChannel(
                    CHANNEL_PROMPT_ID,
                    CHANNEL_PROMPT_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "用于半自动记账确认，显示横幅/弹出式通知。"
                    setShowBadge(true)
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(promptChannel)
                Log.i(TAG, "✅ 通知渠道创建完成: $CHANNEL_PROMPT_NAME")
            }
        } else {
            Log.d(TAG, "⚪ 旧版本Android，无需创建通知渠道")
        }
        
        // 检查通知权限
        val areNotificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true // 旧版本默认允许
        }
        
        Log.i(TAG, "🔔 通知权限状态: $areNotificationsEnabled")
        if (!areNotificationsEnabled) {
            Log.w(TAG, "⚠️ 通知权限被禁用，弹窗可能无法显示!")
        }
    }
    
    /**
     * 发送自动记账成功通知（全自动模式）
     */
    fun showAutoLedgerSuccessNotification(
        transaction: Transaction,
        paymentNotification: PaymentNotification
    ) {
        Log.i(TAG, "🎉 开始显示自动记账成功通知")
        
        val notificationId = generateNotificationId(transaction.id)
        Log.d(TAG, "🆔 生成通知ID: $notificationId (交易ID: ${transaction.id})")
        
        val summaryText = formatTransactionSummary(transaction, paymentNotification)
        Log.d(TAG, "📝 通知摘要: $summaryText")
        
        // 创建撤销Action
        Log.d(TAG, "🔧 创建撤销Action...")
        val undoIntent = createUndoIntent(transaction.id, notificationId)
        val undoAction = NotificationCompat.Action.Builder(
            R.drawable.ic_undo_24,
            "撤销",
            undoIntent
        ).build()
        
        // 创建编辑Action
        Log.d(TAG, "🔧 创建编辑Action...")
        val editIntent = createEditIntent(transaction.id, notificationId)
        val editAction = NotificationCompat.Action.Builder(
            R.drawable.ic_edit_24,
            "编辑",
            editIntent
        ).build()
        
        Log.d(TAG, "🛠️ 构建通知对象...")
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS_ID)
            .setSmallIcon(R.drawable.ic_auto_ledger_24)
            .setContentTitle("已自动记账")
            .setContentText(summaryText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .addAction(undoAction)
            .addAction(editAction)
            .build()
        
        Log.i(TAG, "🚀 发送通知到系统 (ID: $notificationId)")
        try {
            notificationManager.notify(notificationId, notification)
            Log.i(TAG, "✅ 自动记账成功通知已显示!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 发送通知失败", e)
            return
        }
        
        // 启动30秒后自动清理通知的Worker
        Log.d(TAG, "⏰ 安排通知清理任务 (${UNDO_WINDOW_SECONDS}秒后)")
        scheduleNotificationCleanup(notificationId, UNDO_WINDOW_SECONDS)
    }
    
    /**
     * 发送半自动记账确认通知
     */
    fun showSemiAutoConfirmNotification(
        paymentNotification: PaymentNotification,
        recommendedTransactions: List<Transaction>
    ) {
        Log.i(TAG, "🤔 开始显示半自动记账确认通知")

        val notificationId = generateNotificationId("pending_${paymentNotification.notificationKey}")
        Log.d(TAG, "🆔 生成确认通知ID: $notificationId (原通知key: ${paymentNotification.notificationKey})")

        val paymentSummary = formatPaymentSummary(paymentNotification)
        Log.d(TAG, "📝 支付摘要: $paymentSummary")
        Log.d(TAG, "🎯 置信度: ${paymentNotification.confidence} (需要确认)")
        
        // 仅保留“点击通知主体 → 打开标准添加交易页”的极简交互
        // 优先带上primary推荐；若不存在，则仅携带支付信息参数
        val topN = recommendedTransactions.take(1)
        val primaryTx = topN.firstOrNull()
        // 使用 Uri.Builder 构建 DeepLink，避免手工编码导致的 '+' 等问题
        val deepLink = Uri.Builder()
            .scheme("ccxiaoji")
            .authority("app")
            .appendPath("add_transaction")
            .appendQueryParameter("amountCents", paymentNotification.amountCents.toInt().toString())
            .appendQueryParameter("direction", paymentNotification.direction.name)
            .apply {
                paymentNotification.normalizedMerchant?.let { m ->
                    appendQueryParameter("merchant", m)
                }
                primaryTx?.accountId?.let { id -> appendQueryParameter("accountId", id) }
                primaryTx?.categoryId?.let { id -> appendQueryParameter("categoryId", id) }
                primaryTx?.note?.let { n -> appendQueryParameter("note", n) }
            }
            .build()
        val intent = Intent(Intent.ACTION_VIEW, deepLink).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            `package` = context.packageName
        }
        val contentIntent: PendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CONTENT + notificationId + 8888,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        Log.d(TAG, "🛠️ 构建确认通知对象...")
        val builder = NotificationCompat.Builder(context, CHANNEL_PROMPT_ID)
            .setSmallIcon(R.drawable.ic_notification_24)
            .setContentTitle("发现支付通知")
            .setContentText(paymentSummary)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        val notification = builder.build()
        
        Log.i(TAG, "🚀 发送确认通知到系统 (ID: $notificationId)")
        try {
            notificationManager.notify(notificationId, notification)
            Log.i(TAG, "✅ 半自动记账确认通知已显示!")
            Log.d(TAG, "💡 用户可点击'记账'按钮确认或等待${UNDO_WINDOW_SECONDS}秒自动清理")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 发送确认通知失败", e)
            return
        }
        
        // 30秒后自动清理未确认的通知
        Log.d(TAG, "⏰ 安排确认通知清理任务 (${UNDO_WINDOW_SECONDS}秒后)")
        scheduleNotificationCleanup(notificationId, UNDO_WINDOW_SECONDS)
    }

    // 旧的弹窗/一键相关意图已移除，半自动仅通过点击主体跳转到添加页
    
    /**
     * 撤销记账操作
     */
    fun handleUndoAction(transactionId: String, notificationId: Int) {
        // 立即取消原通知
        notificationManager.cancel(notificationId)
        
        // 启动撤销Worker
        val undoData = Data.Builder()
            .putString("transactionId", transactionId)
            .putInt("notificationId", notificationId)
            .build()
        
        val undoWork = OneTimeWorkRequestBuilder<AutoLedgerUndoWorker>()
            .setInputData(undoData)
            .build()
        
        workManager.enqueue(undoWork)
    }
    
    /**
     * 显示撤销成功通知
     */
    fun showUndoSuccessNotification(transactionSummary: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS_ID)
            .setSmallIcon(R.drawable.ic_undo_24)
            .setContentTitle("撤销成功")
            .setContentText("已撤销：$transactionSummary")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(5000) // 5秒后自动消失
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    /**
     * 创建撤销Intent
     */
    private fun createUndoIntent(transactionId: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, AutoLedgerBroadcastReceiver::class.java).apply {
            action = AutoLedgerBroadcastReceiver.ACTION_UNDO
            putExtra("transactionId", transactionId)
            putExtra("notificationId", notificationId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            REQUEST_UNDO + notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * 创建编辑Intent
     */
    private fun createEditIntent(transactionId: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, AutoLedgerBroadcastReceiver::class.java).apply {
            action = AutoLedgerBroadcastReceiver.ACTION_EDIT
            putExtra("transactionId", transactionId)
            putExtra("notificationId", notificationId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            REQUEST_EDIT + notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    
    
    /**
     * 安排通知清理任务
     */
    private fun scheduleNotificationCleanup(notificationId: Int, delaySeconds: Long) {
        val cleanupData = Data.Builder()
            .putInt("notificationId", notificationId)
            .build()
        
        val cleanupWork = OneTimeWorkRequestBuilder<NotificationCleanupWorker>()
            .setInputData(cleanupData)
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .build()
        
        workManager.enqueue(cleanupWork)
    }
    
    /**
     * 生成通知ID
     */
    private fun generateNotificationId(seed: Any): Int {
        return seed.hashCode().let { if (it < 0) -it else it }
    }
    
    /**
     * 格式化交易摘要
     */
    private fun formatTransactionSummary(transaction: Transaction, paymentNotification: PaymentNotification): String {
        val direction = when (paymentNotification.direction) {
            PaymentDirection.EXPENSE -> "支出"
            PaymentDirection.INCOME -> "收入"
            PaymentDirection.REFUND -> "退款"
            PaymentDirection.TRANSFER -> "转账"
            PaymentDirection.UNKNOWN -> "未知"
        }
        val amount = "%.2f".format(transaction.amountCents / 100.0)
        val merchant = paymentNotification.normalizedMerchant ?: "未知商户"
        return "$direction ¥$amount · $merchant"
    }
    
    /**
     * 格式化支付摘要
     */
    private fun formatPaymentSummary(paymentNotification: PaymentNotification): String {
        val direction = when (paymentNotification.direction) {
            PaymentDirection.EXPENSE -> "支出"
            PaymentDirection.INCOME -> "收入"
            PaymentDirection.REFUND -> "退款"
            PaymentDirection.TRANSFER -> "转账"
            PaymentDirection.UNKNOWN -> "未知"
        }
        val amount = "%.2f".format(paymentNotification.amountCents / 100.0)
        val merchant = paymentNotification.normalizedMerchant ?: "未知商户"
        return "$direction ¥$amount · $merchant"
    }
}

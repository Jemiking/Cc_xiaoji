package com.ccxiaoji.feature.ledger.domain.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.app.RemoteInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 自动记账通知Action广播接收器
 * 
 * 处理：
 * - 撤销记账操作
 * - 编辑记账操作  
 * - 半自动模式确认记账
 */
@AndroidEntryPoint
class AutoLedgerBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_UNDO = "com.ccxiaoji.feature.ledger.ACTION_UNDO"
        const val ACTION_EDIT = "com.ccxiaoji.feature.ledger.ACTION_EDIT"
        const val ACTION_MANUAL_CONFIRM = "com.ccxiaoji.feature.ledger.ACTION_MANUAL_CONFIRM"
        const val ACTION_QUICK_SAVE = "com.ccxiaoji.feature.ledger.ACTION_QUICK_SAVE"
        
        private const val TAG = "AutoLedgerReceiver"
    }
    
    @Inject
    lateinit var notificationManager: AutoLedgerNotificationManager
    
    @Inject
    lateinit var autoLedgerManager: com.ccxiaoji.feature.ledger.domain.usecase.AutoLedgerManager
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action: ${intent.action}")
        
        when (intent.action) {
            ACTION_UNDO -> handleUndoAction(intent)
            ACTION_EDIT -> handleEditAction(intent)
            ACTION_MANUAL_CONFIRM -> handleManualConfirmAction(context, intent)
            ACTION_QUICK_SAVE -> handleQuickSaveAction(context, intent)
            else -> Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }
    
    /**
     * 处理撤销操作
     */
    private fun handleUndoAction(intent: Intent) {
        val transactionId = intent.getStringExtra("transactionId") ?: return
        val notificationId = intent.getIntExtra("notificationId", -1)
        
        Log.d(TAG, "Processing undo for transaction: $transactionId")
        
        coroutineScope.launch {
            try {
                notificationManager.handleUndoAction(transactionId, notificationId)
                Log.d(TAG, "Undo action processed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process undo action", e)
            }
        }
    }
    
    /**
     * 处理编辑操作
     */
    private fun handleEditAction(intent: Intent) {
        val transactionId = intent.getStringExtra("transactionId") ?: return
        val notificationId = intent.getIntExtra("notificationId", -1)
        
        Log.d(TAG, "Processing edit for transaction: $transactionId")
        
        // 取消通知
        // 注意：这里应该启动编辑Activity，暂时只是日志记录
        // TODO: 启动AddTransactionActivity并传入transactionId进行编辑
        Log.d(TAG, "Edit action would open transaction edit screen for: $transactionId")
    }
    
    /**
     * 处理半自动模式确认记账
     * 
     * 注意：这里只能获取到关键标识符，需要从缓存或数据库重新构建对象
     * 为了简化实现，暂时记录日志，实际使用时需要扩展数据存储机制
     */
    private fun handleManualConfirmAction(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", -1)
        Log.d(TAG, "Processing manual confirm action → open QuickLedgerActivity")

        // 直接打开快速记账弹窗界面（Activity 以对话框样式显示）
        val activityIntent = Intent(context, com.ccxiaoji.feature.ledger.presentation.quickadd.QuickLedgerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtras(intent.extras ?: android.os.Bundle())
        }
        try {
            context.startActivity(activityIntent)
            // 取消确认通知
            if (notificationId != -1) {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).cancel(notificationId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start QuickLedgerActivity", e)
        }
    }

    /**
     * 处理一键记账（无需切前台，直接保存）
     * 若缺少必要字段（账户/分类），自动回退到弹窗编辑路径。
     */
    private fun handleQuickSaveAction(context: Context, intent: Intent) {
        val amount = intent.getIntExtra("pn_amount_cents", 0)
        val direction = intent.getStringExtra("pn_direction") ?: "EXPENSE"
        val merchant = intent.getStringExtra("pn_merchant")
        val accountId = intent.getStringExtra("rec_account_id")
        val categoryId = intent.getStringExtra("rec_category_id")
        val ledgerId = intent.getStringExtra("rec_ledger_id")
        val note = intent.getStringExtra("rec_note") ?: merchant?.let { "自动记账: $it #auto" }

        if (amount <= 0 || accountId.isNullOrBlank() || categoryId.isNullOrBlank()) {
            Log.w(TAG, "QuickSave 缺少必填字段，回退到弹窗编辑: amount=$amount, account=$accountId, category=$categoryId")
            // 直接拉起对话框Activity进行编辑
            val activityIntent = Intent(context, com.ccxiaoji.feature.ledger.presentation.quickadd.QuickLedgerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtras(intent.extras ?: android.os.Bundle())
            }
            try {
                context.startActivity(activityIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start QuickLedgerActivity from QuickSave fallback", e)
            }
            return
        }

        coroutineScope.launch {
            try {
                Log.d(TAG, "QuickSave 开始保存: amount=$amount, account=$accountId, category=$categoryId")
                // 构造 PaymentNotification 与 Transaction，再复用手动确认流程
                val sourceApp = intent.getStringExtra("pn_source_app") ?: ""
                val sourceType = mapPackageToSourceType(sourceApp)
                val postedTime = intent.getLongExtra("pn_post_time", System.currentTimeMillis())
                val key = intent.getStringExtra("pn_key") ?: ""
                val origTitle = intent.getStringExtra("pn_title") ?: ""
                val origText = intent.getStringExtra("pn_text") ?: ""
                val pn = PaymentNotification(
                    sourceApp = sourceApp,
                    sourceType = sourceType,
                    direction = com.ccxiaoji.feature.ledger.domain.model.PaymentDirection.valueOf(direction),
                    amountCents = amount.toLong(),
                    currency = "CNY",
                    rawMerchant = merchant,
                    normalizedMerchant = merchant,
                    paymentMethod = null,
                    postedTime = postedTime,
                    notificationKey = key,
                    parserVersion = 1,
                    confidence = 1.0,
                    rawText = null,
                    originalTitle = origTitle,
                    originalText = origText,
                    fingerprint = if (key.isNotEmpty()) key else "${sourceApp}|${amount}|${merchant}|${postedTime}"
                )
                // 读取通知行内备注（RemoteInput）
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                val inlineNote = remoteInput?.getCharSequence("extra_note")?.toString()
                val finalNote = inlineNote?.takeIf { it.isNotBlank() } ?: note

                val tx = Transaction(
                    id = "placeholder",
                    accountId = accountId,
                    amountCents = amount,
                    categoryId = categoryId,
                    note = finalNote,
                    ledgerId = ledgerId ?: "",
                    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                    updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis())
                )
                autoLedgerManager.processManualConfirmation(pn, tx)
                Log.d(TAG, "QuickSave 完成")
            } catch (e: Exception) {
                Log.e(TAG, "QuickSave 失败", e)
            }
        }
    }

    private fun mapPackageToSourceType(pkg: String): com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType = when (pkg) {
        "com.eg.android.AlipayGphone" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.ALIPAY
        "com.tencent.mm" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.WECHAT
        "com.unionpay" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.UNIONPAY
        else -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.UNKNOWN
    }
}

package com.ccxiaoji.feature.ledger.domain.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
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
 * - 半自动模式仅通过点击通知主体进入添加页（本接收器不再处理确认/一键）
 */
@AndroidEntryPoint
class AutoLedgerBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_UNDO = "com.ccxiaoji.feature.ledger.ACTION_UNDO"
        const val ACTION_EDIT = "com.ccxiaoji.feature.ledger.ACTION_EDIT"
        // 半自动确认/一键已移除
        
        private const val TAG = "AutoLedgerReceiver"
    }
    
    @Inject
    lateinit var notificationManager: AutoLedgerNotificationManager
    
    // 不再需要处理确认/一键，保留撤销/编辑所需依赖
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action: ${intent.action}")
        
        when (intent.action) {
            ACTION_UNDO -> handleUndoAction(intent)
            ACTION_EDIT -> handleEditAction(context, intent)
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
    private fun handleEditAction(context: Context, intent: Intent) {
        val transactionId = intent.getStringExtra("transactionId") ?: return
        val notificationId = intent.getIntExtra("notificationId", -1)

        Log.d(TAG, "Processing edit for transaction: $transactionId")

        // 通过 DeepLink 打开编辑页：ccxiaoji://app/edit_transaction/{transactionId}
        val uri = android.net.Uri.Builder()
            .scheme("ccxiaoji")
            .authority("app")
            .appendPath("edit_transaction")
            .appendPath(transactionId)
            .build()
        val editIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            `package` = context.packageName
        }
        try {
            context.startActivity(editIntent)
            if (notificationId != -1) {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).cancel(notificationId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open edit screen", e)
        }
    }
    
    private fun mapPackageToSourceType(pkg: String): com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType = when (pkg) {
        "com.eg.android.AlipayGphone" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.ALIPAY
        "com.tencent.mm" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.WECHAT
        "com.unionpay" -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.UNIONPAY
        else -> com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType.UNKNOWN
    }
}

package com.ccxiaoji.feature.ledger.presentation.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object WidgetRefreshBroadcaster {
    private const val ACTION_REFRESH = "com.ccxiaoji.app.widget.ledger.ACTION_REFRESH"
    private const val TAG = "CCXJ/WIDGET"

    fun send(context: Context) {
        val intent = Intent(ACTION_REFRESH).apply {
            // Use explicit component broadcast to ensure delivery to our Provider
            setClassName(
                context.packageName,
                "com.ccxiaoji.app.presentation.widget.ledger.LedgerWidgetProvider"
            )
            putExtra("ts", System.currentTimeMillis())
        }
        try {
            Log.i(TAG, "Broadcast refresh -> pkg=${context.packageName}")
            context.sendBroadcast(intent)
        } catch (t: Throwable) {
            Log.e(TAG, "Broadcast refresh failed: ${t.message}", t)
        }
    }
}

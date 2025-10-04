package com.ccxiaoji.app.presentation.widget.ledger

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

object WidgetUpdateScheduler {
    private const val GLOBAL_WORK_NAME = "ledger_widget_update_all"
    private const val TAG = "CCXJ/WIDGET"
    private const val TAG_WIDGET_REFRESH = "tag:widget_refresh"
    private const val TAG_WIDGET_CLEANUP = "tag:widget_cleanup"

    fun enqueueAll(context: Context) {
        val req = OneTimeWorkRequestBuilder<LedgerWidgetWorker>()
            .addTag(TAG_WIDGET_REFRESH)
            .build()
        Log.i(TAG, "enqueueAll work=${GLOBAL_WORK_NAME}")
        WorkManager.getInstance(context)
            .enqueueUniqueWork(GLOBAL_WORK_NAME, ExistingWorkPolicy.REPLACE, req)
    }

    fun enqueueForIds(context: Context, ids: IntArray) {
        ids.forEach { id ->
            val workName = "ledger_widget_update_$id"
            val req = OneTimeWorkRequestBuilder<LedgerWidgetWorker>()
                .setInputData(workDataOf(LedgerWidgetActions.EXTRA_APPWIDGET_ID to id))
                .addTag(TAG_WIDGET_REFRESH)
                .build()
            Log.i(TAG, "enqueueForId id=$id work=$workName")
            WorkManager.getInstance(context)
                .enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, req)
        }
    }

    fun enqueueCleanupForIds(context: Context, ids: IntArray) {
        if (ids.isEmpty()) return
        val workName = "ledger_widget_cleanup_${ids.joinToString("_")}"
        val req = OneTimeWorkRequestBuilder<LedgerWidgetCleanupWorker>()
            .setInputData(workDataOf(LedgerWidgetActions.EXTRA_APPWIDGET_IDS to ids))
            .addTag(TAG_WIDGET_CLEANUP)
            .build()
        Log.i(TAG, "enqueueCleanup ids=${ids.joinToString()} work=$workName")
        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName, ExistingWorkPolicy.APPEND_OR_REPLACE, req)
    }
}

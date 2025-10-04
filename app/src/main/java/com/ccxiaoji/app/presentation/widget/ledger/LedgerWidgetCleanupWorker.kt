package com.ccxiaoji.app.presentation.widget.ledger

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LedgerWidgetCleanupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val widgetPrefs: LedgerWidgetPreferences
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val ids = inputData.getIntArray(LedgerWidgetActions.EXTRA_APPWIDGET_IDS) ?: intArrayOf()
            if (ids.isEmpty()) return Result.success()
            for (id in ids) {
                try {
                    widgetPrefs.remove(id)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove mapping for widget $id: ${e.message}")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup worker error: ${e.message}", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "CCXJ/WIDGET"
    }
}


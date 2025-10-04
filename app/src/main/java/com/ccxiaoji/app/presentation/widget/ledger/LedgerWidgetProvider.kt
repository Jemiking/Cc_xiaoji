package com.ccxiaoji.app.presentation.widget.ledger

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.content.res.Configuration
import com.ccxiaoji.app.R

class LedgerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.i(TAG, "onUpdate ids=${appWidgetIds.joinToString()}")
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildPlaceholderViews(context, id))
        }
        WidgetUpdateScheduler.enqueueForIds(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.i(TAG, "onReceive action=${intent.action} extras=${intent.extras?.keySet()?.joinToString()}")
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(TAG, "schedule enqueueAll due to system action")
                WidgetUpdateScheduler.enqueueAll(context)
            }
            LedgerWidgetActions.ACTION_REFRESH -> {
                // Debounce duplicate refresh within a short window
                val now = System.currentTimeMillis()
                val delta = now - lastRefreshAtMs
                if (delta in 0..REFRESH_DEBOUNCE_MS) {
                    Log.i(TAG, "ACTION_REFRESH debounced; delta=${delta}ms < ${REFRESH_DEBOUNCE_MS}ms")
                    return
                }
                lastRefreshAtMs = now
                val id = intent.getIntExtra(LedgerWidgetActions.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                Log.i(TAG, "ACTION_REFRESH received appWidgetId=$id")
                if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    WidgetUpdateScheduler.enqueueForIds(context, intArrayOf(id))
                } else {
                    WidgetUpdateScheduler.enqueueAll(context)
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.i(TAG, "onDeleted ids=${appWidgetIds.joinToString()}")
        // 璋冨害娓呯悊鏄犲皠锛屽紓姝ョЩ闄?DataStore 涓殑 widget->ledger 缁戝畾
        WidgetUpdateScheduler.enqueueCleanupForIds(context, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        WidgetUpdateScheduler.enqueueForIds(context, intArrayOf(appWidgetId))
    }

    companion object {
        private const val TAG = "CCXJ/WIDGET"
        private const val REFRESH_DEBOUNCE_MS = 1500L
        @Volatile private var lastRefreshAtMs: Long = 0L

        fun buildPlaceholderViews(context: Context, appWidgetId: Int): RemoteViews {
            val rv = RemoteViews(context.packageName, R.layout.ledger_widget)
            rv.setTextViewText(R.id.tvLedgerName, context.getString(R.string.widget_ledger_title_placeholder))
            rv.setTextViewText(R.id.tvTodayIncome, "-")
            rv.setTextViewText(R.id.tvTodayExpense, "-")
            rv.setTextViewText(R.id.tvMonthIncome, "-")
            rv.setTextViewText(R.id.tvMonthExpense, "-")

            // Ensure refresh icon visible in both light/dark modes
            val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val iconRes = if (isDark) R.drawable.ic_refresh_white_24 else R.drawable.ic_refresh_black_24
            rv.setImageViewResource(R.id.btnRefresh, iconRes)

            // Accessibility: placeholder descriptions
            rv.setContentDescription(
                R.id.tvLedgerName,
                context.getString(
                    R.string.widget_desc_ledger_name_with_name,
                    context.getString(R.string.widget_ledger_title_placeholder)
                )
            )
            rv.setContentDescription(R.id.tvTodayIncome, context.getString(R.string.widget_cd_today_income, "-"))
            rv.setContentDescription(R.id.tvTodayExpense, context.getString(R.string.widget_cd_today_expense, "-"))
            rv.setContentDescription(R.id.tvMonthIncome, context.getString(R.string.widget_cd_month_income, "-"))
            rv.setContentDescription(R.id.tvMonthExpense, context.getString(R.string.widget_cd_month_expense, "-"))
            rv.setOnClickPendingIntent(R.id.btnRefresh, createRefreshPendingIntent(context, appWidgetId))
            rv.setOnClickPendingIntent(R.id.container, createOpenAppPendingIntent(context))
            rv.setOnClickPendingIntent(R.id.btnQuickAdd, createQuickAddPendingIntent(context))
            return rv
        }

        fun createRefreshPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, LedgerWidgetProvider::class.java).apply {
                action = LedgerWidgetActions.ACTION_REFRESH
                putExtra(LedgerWidgetActions.EXTRA_APPWIDGET_ID, appWidgetId)
                data = android.net.Uri.parse("ccxj://widget/refresh/$appWidgetId")
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else 0
            return PendingIntent.getBroadcast(context, appWidgetId, intent, flags)
        }

        fun createOpenAppPendingIntent(context: Context): PendingIntent {
            val intent = Intent().apply {
                component = ComponentName(context, com.ccxiaoji.app.presentation.MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            return PendingIntent.getActivity(context, 0, intent, flags)
        }

        fun createQuickAddPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, com.ccxiaoji.feature.ledger.presentation.quickadd.QuickLedgerActivity::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            return PendingIntent.getActivity(context, 1, intent, flags)
        }
    }
}


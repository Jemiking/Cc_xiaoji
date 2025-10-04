package com.ccxiaoji.app.presentation.widget.ledger

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ccxiaoji.app.R
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.repository.LedgerUIPreferencesRepository
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.shared.user.api.UserApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@HiltWorker
class LedgerWidgetWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val widgetPrefs: LedgerWidgetPreferences,
    private val ledgerUiPrefs: LedgerUIPreferencesRepository,
    private val transactions: TransactionRepository,
    private val manageLedger: ManageLedgerUseCase,
    private val ledgerRepository: LedgerRepository,
    private val userApi: UserApi
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val targetId = inputData.getInt(LedgerWidgetActions.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val ids: IntArray = if (targetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                intArrayOf(targetId)
            } else {
                val cn = ComponentName(appContext, LedgerWidgetProvider::class.java)
                appWidgetManager.getAppWidgetIds(cn)
            }
            if (ids.isEmpty()) return@withContext Result.success()
            Log.i(TAG, "Worker start targetId=$targetId ids=${ids.joinToString()}")

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val year = today.year
            val month = today.monthNumber

            ids.forEach { id ->
                try {
                    val ledgerId = resolveLedgerId(id)
                    val rv = if (ledgerId == null) {
                        buildViews(
                            ledgerName = appContext.getString(R.string.widget_ledger_title_unconfigured),
                            todayIncome = null,
                            todayExpense = null,
                            monthIncome = null,
                            monthExpense = null,
                            widgetId = id
                        )
                    } else {
                        val ledgerName = when (val r = manageLedger.getLedgerById(ledgerId)) {
                            is BaseResult.Success -> r.data.name
                            else -> appContext.getString(R.string.widget_ledger_title_placeholder)
                        }

                        // 今日统计
                        var todayIncomeCents = 0
                        var todayExpenseCents = 0
                        when (val daily = transactions.getDailyTotalsByLedger(ledgerId, today, today)) {
                            is BaseResult.Success -> {
                                daily.data[today]?.let { pair ->
                                    todayIncomeCents = pair.first
                                    todayExpenseCents = pair.second
                                }
                            }
                            else -> Unit
                        }

                        // 本月统计
                        var monthIncomeCents = 0
                        var monthExpenseCents = 0
                        when (val monthPair = transactions.getMonthlyIncomesAndExpensesByLedger(ledgerId, year, month)) {
                            is BaseResult.Success -> {
                                monthIncomeCents = monthPair.data.first
                                monthExpenseCents = monthPair.data.second
                            }
                            else -> Unit
                        }

                        Log.i(
                            TAG,
                            "Update id=$id ledger='${ledgerName}' today=($todayIncomeCents,$todayExpenseCents) month=($monthIncomeCents,$monthExpenseCents)"
                        )

                        buildViews(
                            ledgerName = ledgerName,
                            todayIncome = todayIncomeCents,
                            todayExpense = todayExpenseCents,
                            monthIncome = monthIncomeCents,
                            monthExpense = monthExpenseCents,
                            widgetId = id
                        )
                    }

                    appWidgetManager.updateAppWidget(id, rv)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed update for id=$id: ${e.message}", e)
                }
            }

            Log.i(TAG, "Worker done updated=${ids.size}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker error: ${e.message}", e)
            Result.failure()
        }
    }

    private suspend fun resolveLedgerId(widgetId: Int): String? {
        widgetPrefs.getLedgerId(widgetId)?.let { if (it.isNotBlank()) return it }
        val selectedId = try {
            ledgerUiPrefs.getUIPreferences().first().selectedLedgerId
        } catch (_: Exception) { null }
        if (!selectedId.isNullOrBlank()) return selectedId
        return try {
            val r = manageLedger.getDefaultLedger(userApi.getCurrentUserId())
            if (r is BaseResult.Success) r.data.id else null
        } catch (_: Exception) { null }
    }

    private fun buildViews(
        ledgerName: String,
        todayIncome: Int?,
        todayExpense: Int?,
        monthIncome: Int?,
        monthExpense: Int?,
        widgetId: Int
    ): RemoteViews {
        val rv = RemoteViews(appContext.packageName, R.layout.ledger_widget)
        rv.setTextViewText(R.id.tvLedgerName, ledgerName)
        rv.setTextViewText(R.id.tvTodayIncome, formatAmountSafe(todayIncome))
        rv.setTextViewText(R.id.tvTodayExpense, formatAmountSafe(todayExpense))
        rv.setTextViewText(R.id.tvMonthIncome, formatAmountSafe(monthIncome))
        rv.setTextViewText(R.id.tvMonthExpense, formatAmountSafe(monthExpense))

        // Dark/Light adaptation
        val isDark = (appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val bgColor = if (isDark) ContextCompat.getColor(appContext, R.color.widget_bg_dark) else ContextCompat.getColor(appContext, R.color.widget_bg_light)
        val textPrimary = if (isDark) ContextCompat.getColor(appContext, R.color.widget_text_primary_dark) else ContextCompat.getColor(appContext, R.color.widget_text_primary_light)
        rv.setInt(R.id.container, "setBackgroundColor", bgColor)
        rv.setTextColor(R.id.tvLedgerName, textPrimary)
        rv.setTextColor(R.id.tvTodayIncome, textPrimary)
        rv.setTextColor(R.id.tvTodayExpense, textPrimary)
        rv.setTextColor(R.id.tvMonthIncome, textPrimary)
        rv.setTextColor(R.id.tvMonthExpense, textPrimary)
        // Refresh icon contrast per theme
        rv.setImageViewResource(R.id.btnRefresh, if (isDark) R.drawable.ic_refresh_white_24 else R.drawable.ic_refresh_black_24)

        // Size adaptation using current widget options (dp)
        val options = AppWidgetManager.getInstance(appContext).getAppWidgetOptions(widgetId)
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val showMonth = minW >= appContext.resources.getInteger(R.integer.widget_threshold_min_width_show_month)
        val showQuickAdd = minH >= appContext.resources.getInteger(R.integer.widget_threshold_min_height_show_quick_add)
        rv.setViewVisibility(R.id.colMonth, if (showMonth) View.VISIBLE else View.GONE)
        rv.setViewVisibility(R.id.btnQuickAdd, if (showQuickAdd) View.VISIBLE else View.GONE)

        // Accessibility: content descriptions for screen readers
        rv.setContentDescription(R.id.tvLedgerName, appContext.getString(R.string.widget_desc_ledger_name_with_name, ledgerName))
        rv.setContentDescription(R.id.tvTodayIncome, appContext.getString(R.string.widget_cd_today_income, formatAmountSafe(todayIncome)))
        rv.setContentDescription(R.id.tvTodayExpense, appContext.getString(R.string.widget_cd_today_expense, formatAmountSafe(todayExpense)))
        rv.setContentDescription(R.id.tvMonthIncome, appContext.getString(R.string.widget_cd_month_income, formatAmountSafe(monthIncome)))
        rv.setContentDescription(R.id.tvMonthExpense, appContext.getString(R.string.widget_cd_month_expense, formatAmountSafe(monthExpense)))

        rv.setOnClickPendingIntent(R.id.btnRefresh, LedgerWidgetProvider.createRefreshPendingIntent(appContext, widgetId))
        rv.setOnClickPendingIntent(R.id.container, LedgerWidgetProvider.createOpenAppPendingIntent(appContext))
        rv.setOnClickPendingIntent(R.id.btnQuickAdd, LedgerWidgetProvider.createQuickAddPendingIntent(appContext))
        return rv
    }

    private fun formatAmountSafe(value: Int?): String {
        if (value == null) return "-"
        val negative = value < 0
        val abs = kotlin.math.abs(value)
        val yuan = abs / 100.0
        val text = if (yuan >= 10000) {
            val base = String.format("%.1f", yuan / 10000)
            val trimmed = base.trimEnd('0').trimEnd('.')
            "${trimmed}万"
        } else {
            val nf = java.text.NumberFormat.getNumberInstance()
            nf.maximumFractionDigits = 2
            nf.minimumFractionDigits = if ((abs % 100) == 0) 0 else 2
            nf.format(yuan)
        }
        return if (negative) "-$text" else text
    }

    companion object {
        private const val TAG = "CCXJ/WIDGET"
    }
}


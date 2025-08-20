package com.ccxiaoji.feature.schedule.debug

import com.ccxiaoji.feature.schedule.BuildConfig
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

object ScheduleDebug {
    private const val BASE_TAG = "ScheduleDebug"

    val enabled: Boolean
        get() = BuildConfig.DEBUG && (try { BuildConfig.DEBUG_CALENDAR_LOGS } catch (_: Throwable) { true })

    fun log(tag: String, msg: String) {
        if (enabled) android.util.Log.d("$BASE_TAG-$tag", msg)
    }

    fun logCalendarGrid(
        yearMonth: YearMonth,
        weekStartDay: DayOfWeek,
        firstDayOffset: Int,
        daysInMonth: Int,
        gridCount: Int,
        schedulesCount: Int
    ) {
        log(
            tag = "CalendarGrid",
            msg = "ym=$yearMonth, weekStart=$weekStartDay, firstDayOffset=$firstDayOffset, days=$daysInMonth, grid=$gridCount, schedules=$schedulesCount"
        )
    }

    fun logCalendarCell(date: LocalDate, hasSchedule: Boolean, isToday: Boolean, isSelected: Boolean) {
        log("Cell", "date=$date, has=$hasSchedule, today=$isToday, sel=$isSelected")
    }

    fun logMonthQuery(startMillis: Long, endMillis: Long) {
        log("RepoQuery", "start=${java.time.Instant.ofEpochMilli(startMillis)} end=${java.time.Instant.ofEpochMilli(endMillis)}")
    }

    fun logMonthResult(count: Int, sample: String) {
        log("RepoResult", "count=$count sample=$sample")
    }
}


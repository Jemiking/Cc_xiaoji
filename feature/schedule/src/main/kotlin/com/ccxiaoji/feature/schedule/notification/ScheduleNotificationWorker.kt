package com.ccxiaoji.feature.schedule.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ccxiaoji.feature.schedule.domain.usecase.GetScheduleByDateUseCase
import com.ccxiaoji.shared.notification.data.manager.NotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class ScheduleNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getScheduleByDateUseCase: GetScheduleByDateUseCase,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "schedule_daily_reminder"
    }

    override suspend fun doWork(): Result {
        return try {
            // 获取今天的日期
            val today = LocalDate.now()
            
            // 获取今天的排班信息
            val schedule = getScheduleByDateUseCase(today).first()
            
            if (schedule != null && schedule.shift != null) {
                val shift = schedule.shift
                // 发送有排班的通知
                notificationManager.sendScheduleReminder(
                    hasSchedule = true,
                    shiftName = shift.name,
                    shiftTime = "${shift.startTime} - ${shift.endTime}"
                )
            } else {
                // 发送无排班的通知
                notificationManager.sendScheduleReminder(
                    hasSchedule = false
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

}
package com.ccxiaoji.feature.schedule.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.common.base.BaseWorker
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
) : BaseWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "schedule_daily_reminder"
    }

    override fun getWorkerName(): String = "ScheduleNotificationWorker"
    
    override suspend fun performWork(): Result {
        logInfo("Starting schedule notification check")
        
        // 获取今天的日期
        val today = LocalDate.now()
        logInfo("Checking schedule for date: $today")
        
        // 获取今天的排班信息
        val schedule = getScheduleByDateUseCase(today).first()
        
        if (schedule != null && schedule.shift != null) {
            val shift = schedule.shift
            logInfo("Found schedule: ${shift.name} (${shift.startTime} - ${shift.endTime})")
            
            // 发送有排班的通知
            notificationManager.sendScheduleReminder(
                hasSchedule = true,
                shiftName = shift.name,
                shiftTime = "${shift.startTime} - ${shift.endTime}"
            )
            
            return Result.success(
                workDataOf(
                    "has_schedule" to true,
                    "shift_name" to shift.name
                )
            )
        } else {
            logInfo("No schedule found for today")
            
            // 发送无排班的通知
            notificationManager.sendScheduleReminder(
                hasSchedule = false
            )
            
            return Result.success(
                workDataOf("has_schedule" to false)
            )
        }
    }

}
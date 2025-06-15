package com.ccxiaoji.feature.schedule.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

import com.ccxiaoji.feature.schedule.domain.usecase.GetScheduleByDateUseCase
import com.ccxiaoji.shared.notification.api.NotificationApi

/**
 * 排班提醒通知工作器
 * 每日定时发送排班提醒通知
 */
@HiltWorker
class ScheduleNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getScheduleByDateUseCase: GetScheduleByDateUseCase,
    private val notificationApi: NotificationApi
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_ID = 2001 // 避免与其他模块冲突
        const val WORK_NAME = "schedule_daily_reminder"
    }

    /**
     * 执行工作任务
     * @return 工作执行结果
     */
    override suspend fun doWork(): Result {
        return try {
            // 获取今天的日期
            val today = LocalDate.now()
            
            // 获取今天的排班信息
            val schedule = getScheduleByDateUseCase(today).first()
            
            if (schedule != null) {
                // 直接使用 schedule 中的 shift 对象
                val shift = schedule.shift
                
                // 发送通知
                notificationApi.sendGeneralNotification(
                    title = "今日排班提醒",
                    message = "今天的班次：${shift.name}" +
                            "（${shift.startTime} - ${shift.endTime}）",
                    notificationId = NOTIFICATION_ID
                )
            } else {
                // 今天没有排班
                notificationApi.sendGeneralNotification(
                    title = "今日排班提醒",
                    message = "今天没有排班安排",
                    notificationId = NOTIFICATION_ID
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
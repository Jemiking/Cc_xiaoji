package com.ccxiaoji.feature.ledger.worker.creditcard

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CreditCardBillRepository
import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.user.api.UserApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

/**
 * 信用卡还款提醒Worker
 * 每天运行一次，检查即将到期的信用卡账单并发送提醒
 */
@HiltWorker
class PaymentReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val creditCardBillRepository: CreditCardBillRepository,
    private val notificationApi: NotificationApi,
    private val userApi: UserApi
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "payment_reminder_work"
        const val TAG = "PaymentReminderWorker"
        
        // 提醒天数配置
        private const val DAYS_BEFORE_DUE_FIRST = 3  // 第一次提醒：3天前
        private const val DAYS_BEFORE_DUE_SECOND = 1 // 第二次提醒：1天前
        private const val DAYS_BEFORE_DUE_FINAL = 0  // 最后提醒：当天
    }
    
    override suspend fun doWork(): Result {
        return try {
            val currentDate = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            
            // 获取所有信用卡账户
            val creditCards = accountRepository.getAccounts()
                .first()
                .filter { it.type == com.ccxiaoji.feature.ledger.domain.model.AccountType.CREDIT_CARD }
            
            // 检查每个信用卡的还款情况
            creditCards.forEach { account ->
                checkAndSendReminder(account.id, account.name, currentDate, timeZone)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private suspend fun checkAndSendReminder(
        accountId: String,
        accountName: String,
        currentDate: Instant,
        timeZone: TimeZone
    ) {
        // 获取待还款账单
        val pendingBills = creditCardBillRepository.getPendingBills()
            .first()
            .filter { it.accountId == accountId && !it.isPaid }
        
        pendingBills.forEach { bill ->
            val daysUntilDue = calculateDaysUntilDue(
                currentDate,
                bill.paymentDueDate,
                timeZone
            )
            
            when (daysUntilDue) {
                DAYS_BEFORE_DUE_FIRST -> {
                    sendReminder(
                        accountName,
                        bill.totalAmountYuan,
                        daysUntilDue,
                        "您的${accountName}还有3天到期"
                    )
                }
                DAYS_BEFORE_DUE_SECOND -> {
                    sendReminder(
                        accountName,
                        bill.totalAmountYuan,
                        daysUntilDue,
                        "您的${accountName}明天到期"
                    )
                }
                DAYS_BEFORE_DUE_FINAL -> {
                    sendReminder(
                        accountName,
                        bill.totalAmountYuan,
                        daysUntilDue,
                        "您的${accountName}今天到期",
                        isUrgent = true
                    )
                }
                in Int.MIN_VALUE..-1 -> {
                    // 已逾期
                    if (daysUntilDue == -1) {
                        sendOverdueReminder(
                            accountName,
                            bill.totalAmountYuan,
                            -daysUntilDue
                        )
                    }
                }
            }
        }
    }
    
    private fun calculateDaysUntilDue(
        currentDate: Instant,
        dueDate: Instant,
        timeZone: TimeZone
    ): Int {
        val currentLocalDate = currentDate.toLocalDateTime(timeZone).date
        val dueLocalDate = dueDate.toLocalDateTime(timeZone).date
        
        return currentLocalDate.daysUntil(dueLocalDate)
    }
    
    private suspend fun sendReminder(
        accountName: String,
        amountYuan: Double,
        daysUntilDue: Int,
        title: String,
        isUrgent: Boolean = false
    ) {
        val message = buildString {
            append("应还金额：¥%.2f".format(amountYuan))
            when (daysUntilDue) {
                0 -> append("\n请今天完成还款，避免逾期")
                1 -> append("\n请明天前完成还款")
                else -> append("\n请在${daysUntilDue}天内完成还款")
            }
        }
        
        notificationApi.sendGeneralNotification(
            title = title,
            message = message
        )
    }
    
    private suspend fun sendOverdueReminder(
        accountName: String,
        amountYuan: Double,
        daysOverdue: Int
    ) {
        val title = "【逾期】${accountName}已逾期${daysOverdue}天"
        val message = "逾期金额：¥%.2f\n请尽快还款，避免影响信用记录".format(amountYuan)
        
        notificationApi.sendGeneralNotification(
            title = title,
            message = message
        )
    }
}
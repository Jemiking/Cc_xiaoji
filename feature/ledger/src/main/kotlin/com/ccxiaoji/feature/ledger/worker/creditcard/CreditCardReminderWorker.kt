package com.ccxiaoji.feature.ledger.worker.creditcard

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.common.base.BaseWorker
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.shared.notification.api.NotificationApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import com.ccxiaoji.common.utils.CreditCardDateUtils
import kotlinx.datetime.*

@HiltWorker
class CreditCardReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountDao: AccountDao,
    private val userApi: UserApi,
    private val notificationApi: NotificationApi
) : BaseWorker(context, workerParams) {
    
    override fun getWorkerName(): String = "CreditCardReminderWorker"
    
    override suspend fun performWork(): Result {
        logInfo("Starting credit card payment reminders")
        
        val userId = userApi.getCurrentUserId()
        val creditCardsWithDebt = accountDao.getCreditCardsWithDebt(userId)
        
        if (creditCardsWithDebt.isEmpty()) {
            logInfo("No credit cards with debt found")
            return Result.success()
        }
        
        logInfo("Checking ${creditCardsWithDebt.size} credit cards for payment reminders")
        var remindersSent = 0
        
        creditCardsWithDebt.forEach { creditCard ->
            val dueDay = creditCard.paymentDueDay
            val billingDay = creditCard.billingDay
            
            if (dueDay != null && billingDay != null) {
                // Calculate days until due using the new utility
                val daysUntilDue = CreditCardDateUtils.calculateDaysUntilPayment(
                    paymentDueDay = dueDay,
                    billingDay = billingDay
                )
                
                // Format debt amount
                val debtAmount = formatCurrency(abs(creditCard.balanceCents))
                
                when (daysUntilDue) {
                    3, 1, 0 -> {
                        logInfo("Sending reminder for ${creditCard.name}: $daysUntilDue days until due, amount: $debtAmount")
                        notificationApi.sendCreditCardReminder(
                            cardId = creditCard.id,
                            cardName = creditCard.name,
                            debtAmount = debtAmount,
                            daysUntilDue = daysUntilDue,
                            paymentDueDay = dueDay
                        )
                        remindersSent++
                    }
                }
            }
        }
        
        logInfo("Credit card reminders completed. Sent $remindersSent reminders")
        return Result.success(
            workDataOf("reminders_sent" to remindersSent)
        )
    }
    
    
    private fun formatCurrency(amountCents: Long): String {
        val amount = amountCents / 100.0
        val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
        return format.format(amount)
    }
    
    companion object {
        const val WORK_NAME = "credit_card_reminder_worker"
        
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            return PeriodicWorkRequestBuilder<CreditCardReminderWorker>(
                1, TimeUnit.DAYS // Check once per day
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()
        }
        
        fun createOneTimeWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<CreditCardReminderWorker>()
                .build()
        }
        
        private fun calculateInitialDelay(): Long {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                // Set to 10:00 AM next day
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // If it's already past 10 AM today, the above calculation is correct
            // If it's before 10 AM today, we should run today at 10 AM
            val todayAt10AM = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            return if (now < todayAt10AM.timeInMillis) {
                todayAt10AM.timeInMillis - now
            } else {
                calendar.timeInMillis - now
            }
        }
    }
}
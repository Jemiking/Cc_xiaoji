package com.ccxiaoji.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.app.data.local.dao.AccountDao
import com.ccxiaoji.app.data.repository.UserRepository
import com.ccxiaoji.app.notification.NotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@HiltWorker
class CreditCardReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountDao: AccountDao,
    private val userRepository: UserRepository,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val userId = userRepository.getCurrentUserId()
            val creditCardsWithDebt = accountDao.getCreditCardsWithDebt(userId)
            
            if (creditCardsWithDebt.isEmpty()) {
                return@withContext Result.success()
            }
            
            val today = Calendar.getInstance()
            val currentDayOfMonth = today.get(Calendar.DAY_OF_MONTH)
            val currentMonth = today.get(Calendar.MONTH)
            val currentYear = today.get(Calendar.YEAR)
            
            creditCardsWithDebt.forEach { creditCard ->
                creditCard.paymentDueDay?.let { dueDay ->
                    // Calculate the actual payment due date
                    val paymentDueDate = calculatePaymentDueDate(dueDay, currentDayOfMonth, currentMonth, currentYear)
                    val daysUntilDue = calculateDaysUntilDue(today, paymentDueDate)
                    
                    // Format debt amount
                    val debtAmount = formatCurrency(abs(creditCard.balanceCents))
                    
                    when (daysUntilDue) {
                        3 -> {
                            notificationManager.sendCreditCardReminder(
                                cardId = creditCard.id,
                                cardName = creditCard.name,
                                debtAmount = debtAmount,
                                daysUntilDue = 3,
                                paymentDueDay = dueDay
                            )
                        }
                        1 -> {
                            notificationManager.sendCreditCardReminder(
                                cardId = creditCard.id,
                                cardName = creditCard.name,
                                debtAmount = debtAmount,
                                daysUntilDue = 1,
                                paymentDueDay = dueDay
                            )
                        }
                        0 -> {
                            notificationManager.sendCreditCardReminder(
                                cardId = creditCard.id,
                                cardName = creditCard.name,
                                debtAmount = debtAmount,
                                daysUntilDue = 0,
                                paymentDueDay = dueDay
                            )
                        }
                    }
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    private fun calculatePaymentDueDate(dueDay: Int, currentDay: Int, currentMonth: Int, currentYear: Int): Calendar {
        val paymentDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Handle end-of-month edge cases
        val lastDayOfMonth = paymentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        val actualDueDay = minOf(dueDay, lastDayOfMonth)
        paymentDate.set(Calendar.DAY_OF_MONTH, actualDueDay)
        
        // If the due date has already passed this month, calculate for next month
        if (actualDueDay < currentDay) {
            paymentDate.add(Calendar.MONTH, 1)
            // Recalculate for the new month in case it has fewer days
            val newLastDay = paymentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            paymentDate.set(Calendar.DAY_OF_MONTH, minOf(dueDay, newLastDay))
        }
        
        return paymentDate
    }
    
    private fun calculateDaysUntilDue(today: Calendar, dueDate: Calendar): Int {
        val todayMillis = today.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val dueMillis = dueDate.timeInMillis
        val diffMillis = dueMillis - todayMillis
        
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
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
package com.ccxiaoji.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.app.data.local.dao.AccountDao
import com.ccxiaoji.app.data.repository.AccountRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class CreditCardBillWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountDao: AccountDao,
    private val accountRepository: AccountRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 获取当前用户的所有信用卡账户
            val creditCards = accountDao.getCreditCardAccounts("current_user_id").first()
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            
            // 检查每张信用卡是否需要生成账单
            for (creditCard in creditCards) {
                val billingDay = creditCard.billingDay ?: continue
                
                // 如果今天是账单日，生成账单
                if (currentDay == billingDay) {
                    accountRepository.generateCreditCardBill(creditCard.id)
                }
                
                // 标记逾期账单
                accountRepository.markOverdueBills(creditCard.id)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "credit_card_bill_worker"
        
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            return PeriodicWorkRequestBuilder<CreditCardBillWorker>(
                1, TimeUnit.DAYS // 每天执行一次
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()
        }
        
        /**
         * 计算初始延迟，让任务在每天凌晨1点执行
         */
        private fun calculateInitialDelay(): Long {
            val currentTime = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // 如果当前时间已经过了凌晨1点，设置为明天凌晨1点
                if (timeInMillis <= currentTime.timeInMillis) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            return targetTime.timeInMillis - currentTime.timeInMillis
        }
    }
}
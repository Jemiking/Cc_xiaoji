package com.ccxiaoji.feature.ledger.worker.creditcard

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.common.base.BaseWorker
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
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
) : BaseWorker(context, workerParams) {
    
    override fun getWorkerName(): String = "CreditCardBillWorker"
    
    override suspend fun performWork(): Result {
        logInfo("Starting credit card bill generation")
        
        // 获取当前用户的所有信用卡账户
        val creditCards = accountDao.getCreditCardAccounts("current_user_id").first()
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        
        if (creditCards.isEmpty()) {
            logInfo("No credit card accounts found")
            return Result.success()
        }
        
        logInfo("Processing ${creditCards.size} credit card accounts")
        
        // 检查每张信用卡是否需要生成账单
        var processedCount = 0
        for (creditCard in creditCards) {
            val billingDay = creditCard.billingDay ?: continue
            
            // 如果今天是账单日，生成账单
            if (currentDay == billingDay) {
                logInfo("Generating bill for credit card: ${creditCard.name}")
                // TODO: 需要实现BillRepository或在AccountRepository中添加此方法
                // accountRepository.generateCreditCardBill(creditCard.id)
                processedCount++
            }
            
            // 标记逾期账单
            // TODO: 需要实现BillRepository或在AccountRepository中添加此方法
            // accountRepository.markOverdueBills(creditCard.id)
        }
        
        logInfo("Credit card bill generation completed. Processed $processedCount bills")
        return Result.success(
            workDataOf("processed_count" to processedCount)
        )
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
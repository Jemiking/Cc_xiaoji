package com.ccxiaoji.feature.ledger.worker.creditcard

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.common.base.BaseWorker
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CreditCardBillRepository
import com.ccxiaoji.shared.user.api.UserApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class CreditCardBillWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountDao: AccountDao,
    private val accountRepository: AccountRepository,
    private val creditCardBillRepository: CreditCardBillRepository,
    private val userApi: UserApi
) : BaseWorker(context, workerParams) {
    
    override fun getWorkerName(): String = "CreditCardBillWorker"
    
    override suspend fun performWork(): Result {
        logInfo("Starting credit card bill generation")
        
        try {
            // 获取当前用户ID
            val userId = userApi.getCurrentUserId()
            
            // 获取当前用户的所有信用卡账户
            val creditCards = accountDao.getCreditCardAccounts(userId).first()
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val currentDay = today.dayOfMonth
            
            if (creditCards.isEmpty()) {
                logInfo("No credit card accounts found")
                return Result.success()
            }
            
            logInfo("Processing ${creditCards.size} credit card accounts")
            
            // 检查每张信用卡是否需要生成账单
            var processedCount = 0
            var errorCount = 0
            
            for (creditCard in creditCards) {
                val billingDay = creditCard.billingDay ?: continue
                
                // 如果今天是账单日，生成账单
                if (currentDay == billingDay) {
                    logInfo("Generating bill for credit card: ${creditCard.name}")
                    
                    // 计算账单周期（上个月的账单日到这个月的账单日）
                    val billEndDate = today
                    val billStartDate = if (today.monthNumber == 1) {
                        LocalDate(today.year - 1, 12, billingDay)
                    } else {
                        LocalDate(today.year, today.monthNumber - 1, billingDay)
                    }
                    
                    // 生成账单
                    when (val result = creditCardBillRepository.generateBill(
                        accountId = creditCard.id,
                        periodStart = billStartDate,
                        periodEnd = billEndDate
                    )) {
                        is BaseResult.Success -> {
                            logInfo("Bill generated successfully for ${creditCard.name}")
                            processedCount++
                        }
                        is BaseResult.Error -> {
                            logError("Failed to generate bill for ${creditCard.name}: ${result.exception.message}")
                            errorCount++
                        }
                    }
                }
            }
            
            // 标记逾期账单
            when (val result = creditCardBillRepository.markOverdueBills()) {
                is BaseResult.Success -> {
                    logInfo("Marked ${result.data} bills as overdue")
                }
                is BaseResult.Error -> {
                    logError("Failed to mark overdue bills: ${result.exception.message}")
                }
            }
            
            logInfo("Credit card bill generation completed. Processed: $processedCount, Errors: $errorCount")
            
            return if (errorCount > 0) {
                Result.failure(
                    workDataOf(
                        "processed_count" to processedCount,
                        "error_count" to errorCount
                    )
                )
            } else {
                Result.success(
                    workDataOf("processed_count" to processedCount)
                )
            }
        } catch (e: Exception) {
            logError("Unexpected error in credit card bill generation: ${e.message}")
            return Result.failure()
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
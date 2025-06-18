package com.ccxiaoji.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.ccxiaoji.app.data.local.dao.AccountDao
import com.ccxiaoji.shared.user.data.local.dao.UserDao
import com.ccxiaoji.app.data.local.entity.AccountEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity
import com.ccxiaoji.app.data.repository.CategoryRepository
import com.ccxiaoji.app.data.sync.RecurringTransactionWorker
import com.ccxiaoji.app.data.sync.CreditCardReminderManager
import com.ccxiaoji.app.data.sync.CreditCardBillWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltAndroidApp
class CcXiaoJiApplication : Application(), Configuration.Provider {
    
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var userDao: UserDao
    
    @Inject
    lateinit var accountDao: AccountDao
    
    @Inject
    lateinit var categoryRepository: CategoryRepository
    
    @Inject
    lateinit var creditCardReminderManager: CreditCardReminderManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate started")
        
        try {
            Log.d(TAG, "Starting database initialization")
            
            // 确保默认用户存在
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "Coroutine launched for database init")
                try {
                    val defaultUserId = "current_user_id"
                    Log.d(TAG, "Checking for existing user: $defaultUserId")
                    val existingUser = userDao.getUserById(defaultUserId)
                    
                    if (existingUser == null) {
                        Log.d(TAG, "Creating default user")
                        val defaultUser = UserEntity(
                            id = defaultUserId,
                            email = "default@ccxiaoji.com",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        userDao.insertUser(defaultUser)
                        Log.d(TAG, "Default user created")
                    } else {
                        Log.d(TAG, "Default user already exists")
                    }
                    
                    // 确保默认账户存在
                    Log.d(TAG, "Checking for default account")
                    val defaultAccount = accountDao.getDefaultAccount(defaultUserId)
                    if (defaultAccount == null) {
                        Log.d(TAG, "Creating default account")
                val newAccount = AccountEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = defaultUserId,
                    name = "现金账户",
                    type = "CASH",
                    balanceCents = 0,
                    currency = "CNY",
                    isDefault = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.SYNCED
                        )
                        accountDao.insertAccount(newAccount)
                        Log.d(TAG, "Default account created")
                    } else {
                        Log.d(TAG, "Default account already exists")
                    }
                    
                    // 初始化默认分类
                    Log.d(TAG, "Initializing default categories")
                    categoryRepository.initializeDefaultCategories()
                    Log.d(TAG, "Default categories initialized")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during database initialization", e)
                }
            }
            
            // 注册定期交易Worker
            Log.d(TAG, "Registering RecurringTransactionWorker")
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RecurringTransactionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            RecurringTransactionWorker.createPeriodicWorkRequest()
            )
            Log.d(TAG, "RecurringTransactionWorker registered")
            
            // 启动信用卡提醒服务
            Log.d(TAG, "Starting CreditCardReminderManager")
            creditCardReminderManager.startPeriodicReminders()
            Log.d(TAG, "CreditCardReminderManager started")
            
            // 注册信用卡账单生成Worker
            Log.d(TAG, "Registering CreditCardBillWorker")
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                CreditCardBillWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                CreditCardBillWorker.createPeriodicWorkRequest()
            )
            Log.d(TAG, "CreditCardBillWorker registered")
            
            Log.d(TAG, "Application onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in Application onCreate", e)
            throw e
        }
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
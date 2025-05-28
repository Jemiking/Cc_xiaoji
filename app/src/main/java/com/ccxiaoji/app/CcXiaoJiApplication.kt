package com.ccxiaoji.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.ccxiaoji.app.data.local.dao.AccountDao
import com.ccxiaoji.app.data.local.dao.UserDao
import com.ccxiaoji.app.data.local.entity.AccountEntity
import com.ccxiaoji.app.data.sync.SyncStatus
import com.ccxiaoji.app.data.local.entity.UserEntity
import com.ccxiaoji.app.data.repository.CategoryRepository
import com.ccxiaoji.app.data.sync.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CcXiaoJiApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var userDao: UserDao
    
    @Inject
    lateinit var accountDao: AccountDao
    
    @Inject
    lateinit var categoryRepository: CategoryRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // 确保默认用户存在
        CoroutineScope(Dispatchers.IO).launch {
            val defaultUserId = "current_user_id"
            val existingUser = userDao.getUserById(defaultUserId)
            
            if (existingUser == null) {
                val defaultUser = UserEntity(
                    id = defaultUserId,
                    email = "default@ccxiaoji.com",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                userDao.insertUser(defaultUser)
            }
            
            // 确保默认账户存在
            val defaultAccount = accountDao.getDefaultAccount(defaultUserId)
            if (defaultAccount == null) {
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
            }
            
            // 初始化默认分类
            categoryRepository.initializeDefaultCategories()
        }
        
        // 注册定期交易Worker
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RecurringTransactionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            RecurringTransactionWorker.createPeriodicWorkRequest()
        )
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
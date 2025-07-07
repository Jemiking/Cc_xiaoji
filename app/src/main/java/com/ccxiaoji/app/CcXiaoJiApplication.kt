package com.ccxiaoji.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.shared.user.data.local.dao.UserDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity
import com.ccxiaoji.feature.ledger.data.repository.CategoryRepositoryImpl
import com.ccxiaoji.feature.ledger.worker.RecurringTransactionWorker
import com.ccxiaoji.feature.ledger.worker.creditcard.CreditCardReminderManager
import com.ccxiaoji.feature.ledger.worker.creditcard.CreditCardBillWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
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
    lateinit var categoryDao: CategoryDao
    
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
                    val existingCategories = categoryDao.getCategoriesByUser(defaultUserId).first()
                    if (existingCategories.isEmpty()) {
                        // 创建默认分类
                        createDefaultCategories(categoryDao, defaultUserId)
                        Log.d(TAG, "Default categories created")
                    } else {
                        Log.d(TAG, "Categories already exist: ${existingCategories.size}")
                    }
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
            
    private suspend fun createDefaultCategories(categoryDao: CategoryDao, userId: String) {
        val timestamp = System.currentTimeMillis()
        val defaultCategories = listOf(
            // 支出分类（10个）
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "餐饮",
                icon = "🍜",
                color = "#FF5252",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 0,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "交通",
                icon = "🚗",
                color = "#448AFF",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 1,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "购物",
                icon = "🛍️",
                color = "#FF9800",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 2,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "娱乐",
                icon = "🎮",
                color = "#9C27B0",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 3,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "医疗",
                icon = "🏥",
                color = "#00BCD4",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 4,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "教育",
                icon = "📚",
                color = "#FFEB3B",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 5,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "居住",
                icon = "🏠",
                color = "#795548",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 6,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "水电煤",
                icon = "💡",
                color = "#009688",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 7,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "通讯",
                icon = "📱",
                color = "#673AB7",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 8,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "其他",
                icon = "📝",
                color = "#607D8B",
                type = "EXPENSE",
                parentId = null,
                displayOrder = 9,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            // 收入分类（5个）
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "工资",
                icon = "💰",
                color = "#4CAF50",
                type = "INCOME",
                parentId = null,
                displayOrder = 0,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "奖金",
                icon = "🎁",
                color = "#8BC34A",
                type = "INCOME",
                parentId = null,
                displayOrder = 1,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "投资",
                icon = "📈",
                color = "#FFC107",
                type = "INCOME",
                parentId = null,
                displayOrder = 2,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "兼职",
                icon = "💼",
                color = "#00BCD4",
                type = "INCOME",
                parentId = null,
                displayOrder = 3,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                name = "其他",
                icon = "💵",
                color = "#9E9E9E",
                type = "INCOME",
                parentId = null,
                displayOrder = 4,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )
        
        categoryDao.insertCategories(defaultCategories)
    }
}
package com.ccxiaoji.app.initialization

import android.util.Log
import com.ccxiaoji.core.database.dao.AccountDao
import com.ccxiaoji.core.database.dao.UserDao
import com.ccxiaoji.core.database.entity.AccountEntity
import com.ccxiaoji.core.database.entity.UserEntity
import com.ccxiaoji.core.database.model.SyncStatus
import com.ccxiaoji.feature.ledger.data.CategoryInitializer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据库初始化任务
 * 
 * 负责创建默认用户、账户和分类
 * 使用延迟初始化，仅在首次访问数据库时执行
 */
@Singleton
class DatabaseInitTask @Inject constructor(
    private val userDao: UserDao,
    private val accountDao: AccountDao,
    private val categoryInitializer: CategoryInitializer
) {
    companion object {
        private const val TAG = "DatabaseInitTask"
        private const val DEFAULT_USER_ID = "current_user_id"
    }
    
    @Volatile
    private var isInitialized = false
    
    // 使用 Mutex 替代 synchronized，支持在协程中使用
    private val initMutex = Mutex()
    
    /**
     * 确保数据库已初始化
     * 使用 Mutex 实现协程安全的延迟初始化
     */
    suspend fun ensureInitialized() {
        // 双重检查锁定：先检查是否已初始化（无锁）
        if (!isInitialized) {
            // 使用 Mutex 确保只有一个协程执行初始化
            initMutex.withLock {
                // 再次检查，防止等待锁期间其他协程已完成初始化
                if (!isInitialized) {
                    initialize()
                    isInitialized = true
                }
            }
        }
    }
    
    /**
     * 执行数据库初始化
     */
    suspend fun initialize() {
        Log.d(TAG, "Starting database initialization")
        
        try {
            // 初始化默认用户
            initializeDefaultUser()
            
            // 初始化默认账户
            initializeDefaultAccount()
            
            // 初始化默认分类
            Log.d(TAG, "Initializing default categories")
            categoryInitializer.initializeDefaultCategories()
            Log.d(TAG, "Default categories initialized")
            
            Log.d(TAG, "Database initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during database initialization", e)
            throw e
        }
    }
    
    private suspend fun initializeDefaultUser() {
        Log.d(TAG, "Checking for existing user: $DEFAULT_USER_ID")
        val existingUser = userDao.getUserById(DEFAULT_USER_ID)
        
        if (existingUser == null) {
            Log.d(TAG, "Creating default user")
            val defaultUser = UserEntity(
                id = DEFAULT_USER_ID,
                email = "default@ccxiaoji.com",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            userDao.insertUser(defaultUser)
            Log.d(TAG, "Default user created")
        } else {
            Log.d(TAG, "Default user already exists")
        }
    }
    
    private suspend fun initializeDefaultAccount() {
        Log.d(TAG, "Checking for default account")
        val defaultAccount = accountDao.getDefaultAccount(DEFAULT_USER_ID)
        
        if (defaultAccount == null) {
            Log.d(TAG, "Creating default account")
            val newAccount = AccountEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = DEFAULT_USER_ID,
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
    }
}
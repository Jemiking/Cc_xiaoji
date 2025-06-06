package com.ccxiaoji.app.data.local

import android.util.Log
import com.ccxiaoji.app.data.local.dao.AccountDao
import com.ccxiaoji.app.data.local.dao.CategoryDao
import com.ccxiaoji.app.data.local.dao.UserDao
import com.ccxiaoji.app.data.local.entity.AccountEntity
import com.ccxiaoji.app.data.local.entity.CategoryEntity
import com.ccxiaoji.app.data.local.entity.UserEntity
import com.ccxiaoji.app.data.sync.SyncStatus
import java.util.UUID
import javax.inject.Inject

/**
 * 数据库初始化器
 * 负责在数据库首次创建时初始化默认数据
 */
class DatabaseInitializer @Inject constructor(
    private val userDao: UserDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) {
    companion object {
        private const val TAG = "DatabaseInitializer"
        private const val DEFAULT_USER_ID = "current_user_id"
        private const val DEFAULT_ACCOUNT_ID = "default_account_id"
    }

    /**
     * 初始化默认数据
     */
    suspend fun initializeDefaultData() {
        try {
            Log.d(TAG, "Starting database initialization...")
            
            // 检查是否已经初始化
            val existingUser = userDao.getUserById(DEFAULT_USER_ID)
            if (existingUser != null) {
                Log.d(TAG, "Database already initialized, skipping...")
                return
            }
            
            // 初始化默认用户
            initializeDefaultUser()
            
            // 初始化默认账户
            initializeDefaultAccount()
            
            // 初始化默认分类
            initializeDefaultCategories()
            
            Log.d(TAG, "Database initialization completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize database", e)
            throw e
        }
    }
    
    /**
     * 创建默认用户
     */
    private suspend fun initializeDefaultUser() {
        val currentTime = System.currentTimeMillis()
        val defaultUser = UserEntity(
            id = DEFAULT_USER_ID,
            email = "default@ccxiaoji.com",
            createdAt = currentTime,
            updatedAt = currentTime,
            isDeleted = false
        )
        userDao.insertUser(defaultUser)
        Log.d(TAG, "Default user created")
    }
    
    /**
     * 创建默认账户
     */
    private suspend fun initializeDefaultAccount() {
        val currentTime = System.currentTimeMillis()
        val defaultAccount = AccountEntity(
            id = DEFAULT_ACCOUNT_ID,
            userId = DEFAULT_USER_ID,
            name = "现金账户",
            type = "CASH",
            balanceCents = 0L,
            currency = "CNY",
            icon = null,
            color = null,
            isDefault = true,
            createdAt = currentTime,
            updatedAt = currentTime,
            isDeleted = false,
            syncStatus = SyncStatus.SYNCED
        )
        accountDao.insertAccount(defaultAccount)
        Log.d(TAG, "Default account created")
    }
    
    /**
     * 创建默认分类
     */
    private suspend fun initializeDefaultCategories() {
        val currentTime = System.currentTimeMillis()
        
        // 支出分类
        val expenseCategories = listOf(
            Triple("餐饮", "🍜", "#FF6B6B"),
            Triple("交通", "🚇", "#4ECDC4"),
            Triple("购物", "🛍️", "#45B7D1"),
            Triple("娱乐", "🎮", "#F7DC6F"),
            Triple("医疗", "🏥", "#E74C3C"),
            Triple("教育", "📚", "#3498DB"),
            Triple("居住", "🏠", "#9B59B6"),
            Triple("水电", "💡", "#1ABC9C"),
            Triple("通讯", "📱", "#34495E"),
            Triple("其他", "📌", "#95A5A6")
        )
        
        // 收入分类
        val incomeCategories = listOf(
            Triple("工资", "💰", "#27AE60"),
            Triple("奖金", "🎁", "#F39C12"),
            Triple("投资", "📈", "#8E44AD"),
            Triple("兼职", "💼", "#2980B9"),
            Triple("其他", "💸", "#16A085")
        )
        
        // 批量插入支出分类
        val expenseEntities = expenseCategories.mapIndexed { index, (name, icon, color) ->
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = DEFAULT_USER_ID,
                name = name,
                type = "EXPENSE",
                icon = icon,
                color = color,
                parentId = null,
                displayOrder = index,
                isSystem = true,
                usageCount = 0,
                createdAt = currentTime,
                updatedAt = currentTime,
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
        }
        categoryDao.insertCategories(expenseEntities)
        Log.d(TAG, "Created ${expenseEntities.size} expense categories")
        
        // 批量插入收入分类
        val incomeEntities = incomeCategories.mapIndexed { index, (name, icon, color) ->
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = DEFAULT_USER_ID,
                name = name,
                type = "INCOME",
                icon = icon,
                color = color,
                parentId = null,
                displayOrder = index,
                isSystem = true,
                usageCount = 0,
                createdAt = currentTime,
                updatedAt = currentTime,
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
        }
        categoryDao.insertCategories(incomeEntities)
        Log.d(TAG, "Created ${incomeEntities.size} income categories")
    }
}
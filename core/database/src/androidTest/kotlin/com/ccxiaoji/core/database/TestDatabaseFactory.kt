package com.ccxiaoji.core.database

import android.content.Context
import androidx.room.Room
import com.ccxiaoji.core.database.entity.*
import com.ccxiaoji.core.database.model.SyncStatus
import kotlinx.coroutines.runBlocking

/**
 * 测试数据库工厂
 * 提供测试用的内存数据库和测试数据
 */
object TestDatabaseFactory {
    
    /**
     * 创建内存数据库
     */
    fun createInMemoryDatabase(context: Context): CcDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            CcDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    
    /**
     * 创建带有测试数据的数据库
     */
    fun createPopulatedDatabase(context: Context): CcDatabase {
        val db = createInMemoryDatabase(context)
        runBlocking {
            populateTestData(db)
        }
        return db
    }
    
    /**
     * 填充测试数据
     */
    private suspend fun populateTestData(db: CcDatabase) {
        // 创建测试用户
        val testUser = UserEntity(
            id = "test_user_id",
            email = "test@ccxiaoji.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        db.userDao().insertUser(testUser)
        
        // 创建测试账户
        val testAccount = AccountEntity(
            id = "test_account_id",
            userId = testUser.id,
            name = "测试账户",
            type = "CASH",
            balanceCents = 100000, // 1000.00
            currency = "CNY",
            isDefault = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        db.accountDao().insertAccount(testAccount)
        
        // 创建测试分类
        val incomeCategory = CategoryEntity(
            id = "income_category_id",
            userId = testUser.id,
            name = "工资",
            type = CategoryType.INCOME,
            icon = "attach_money",
            color = "#4CAF50",
            parentId = null,
            orderIndex = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        val expenseCategory = CategoryEntity(
            id = "expense_category_id",
            userId = testUser.id,
            name = "餐饮",
            type = CategoryType.EXPENSE,
            icon = "restaurant",
            color = "#FF5722",
            parentId = null,
            orderIndex = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        db.categoryDao().insertCategory(incomeCategory)
        db.categoryDao().insertCategory(expenseCategory)
        
        // 创建测试交易
        val incomeTransaction = TransactionEntity(
            id = "income_transaction_id",
            userId = testUser.id,
            accountId = testAccount.id,
            categoryId = incomeCategory.id,
            type = "INCOME",
            amountCents = 1000000, // 10000.00
            note = "测试收入",
            transactionDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        val expenseTransaction = TransactionEntity(
            id = "expense_transaction_id",
            userId = testUser.id,
            accountId = testAccount.id,
            categoryId = expenseCategory.id,
            type = "EXPENSE",
            amountCents = 50000, // 500.00
            note = "测试支出",
            transactionDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        db.transactionDao().insertTransaction(incomeTransaction)
        db.transactionDao().insertTransaction(expenseTransaction)
        
        // 创建测试任务
        val testTask = TaskEntity(
            id = "test_task_id",
            userId = testUser.id,
            title = "测试任务",
            description = "这是一个测试任务",
            isCompleted = false,
            priority = 1,
            dueDate = System.currentTimeMillis() + 86400000, // 明天
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        db.taskDao().insertTask(testTask)
        
        // 创建测试习惯
        val testHabit = HabitEntity(
            id = "test_habit_id",
            userId = testUser.id,
            name = "测试习惯",
            description = "每天测试",
            icon = "fitness_center",
            color = "#2196F3",
            targetDays = 7,
            reminderTime = "09:00",
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        db.habitDao().insertHabit(testHabit)
    }
    
    /**
     * 清理数据库
     */
    fun cleanupDatabase(db: CcDatabase) {
        db.clearAllTables()
        db.close()
    }
}
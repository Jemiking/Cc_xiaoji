package com.ccxiaoji.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ccxiaoji.core.database.CcDatabase
import com.ccxiaoji.core.database.dao.*
import com.ccxiaoji.core.database.entity.CategoryEntity
import com.ccxiaoji.core.database.entity.UserEntity
import com.ccxiaoji.core.database.migrations.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Singleton
import android.util.Log

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val TAG = "CcXiaoJi"
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CcDatabase {
        Log.d(TAG, "Providing CcDatabase instance")
        return Room.databaseBuilder(
            context,
            CcDatabase::class.java,
            CcDatabase.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d(TAG, "Database onCreate callback triggered")
                    // 创建默认用户和默认账户
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d(TAG, "Initializing default data in database")
                        val currentTime = System.currentTimeMillis()
                        
                        // 插入默认用户
                        db.execSQL(
                            "INSERT INTO users (id, email, createdAt, updatedAt, isDeleted) VALUES (?, ?, ?, ?, ?)",
                            arrayOf("current_user_id", "default@ccxiaoji.com", currentTime, currentTime, 0)
                        )
                        
                        // 插入默认账户 - 注意 syncStatus 使用字符串 'SYNCED' 而不是数字
                        db.execSQL(
                            "INSERT INTO accounts (id, userId, name, type, balanceCents, currency, isDefault, creditLimitCents, billingDay, paymentDueDay, gracePeriodDays, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            arrayOf("default_account_id", "current_user_id", "现金账户", "CASH", 0L, "CNY", 1, null, null, null, null, currentTime, currentTime, 0, "SYNCED")
                        )
                        
                        // 创建默认分类
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
                        
                        val incomeCategories = listOf(
                            Triple("工资", "💰", "#27AE60"),
                            Triple("奖金", "🎁", "#F39C12"),
                            Triple("投资", "📈", "#8E44AD"),
                            Triple("兼职", "💼", "#2980B9"),
                            Triple("其他", "💸", "#16A085")
                        )
                        
                        // 插入支出分类 - 使用 displayOrder 而不是 sortOrder，syncStatus 使用字符串
                        expenseCategories.forEachIndexed { index, (name, icon, color) ->
                            db.execSQL(
                                "INSERT INTO categories (id, userId, name, type, icon, color, parentId, displayOrder, isSystem, usageCount, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                arrayOf(UUID.randomUUID().toString(), "current_user_id", name, "EXPENSE", icon, color, null, index, 1, 0, currentTime, currentTime, 0, "SYNCED")
                            )
                        }
                        
                        // 插入收入分类 - 使用 displayOrder 而不是 sortOrder，syncStatus 使用字符串
                        incomeCategories.forEachIndexed { index, (name, icon, color) ->
                            db.execSQL(
                                "INSERT INTO categories (id, userId, name, type, icon, color, parentId, displayOrder, isSystem, usageCount, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                arrayOf(UUID.randomUUID().toString(), "current_user_id", name, "INCOME", icon, color, null, index, 1, 0, currentTime, currentTime, 0, "SYNCED")
                            )
                        }
                        
                        Log.d(TAG, "Default data initialization completed")
                    }
                }
            })
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .build()
            .also { Log.d(TAG, "CcDatabase instance created successfully") }
    }
    
    @Provides
    fun provideUserDao(database: CcDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideAccountDao(database: CcDatabase): AccountDao = database.accountDao()
    
    @Provides
    fun provideTransactionDao(database: CcDatabase): TransactionDao = database.transactionDao()
    
    @Provides
    fun provideTaskDao(database: CcDatabase): TaskDao = database.taskDao()
    
    @Provides
    fun provideHabitDao(database: CcDatabase): HabitDao = database.habitDao()
    
    @Provides
    fun provideCountdownDao(database: CcDatabase): CountdownDao = database.countdownDao()
    
    @Provides
    fun provideChangeLogDao(database: CcDatabase): ChangeLogDao = database.changeLogDao()
    
    @Provides
    fun provideCategoryDao(database: CcDatabase): CategoryDao = database.categoryDao()
    
    @Provides
    fun provideBudgetDao(database: CcDatabase): BudgetDao = database.budgetDao()
    
    @Provides
    fun provideRecurringTransactionDao(database: CcDatabase): RecurringTransactionDao = database.recurringTransactionDao()
    
    @Provides
    fun provideSavingsGoalDao(database: CcDatabase): SavingsGoalDao = database.savingsGoalDao()
    
    @Provides
    fun provideCreditCardPaymentDao(database: CcDatabase): CreditCardPaymentDao = database.creditCardPaymentDao()
    
    @Provides
    fun provideCreditCardBillDao(database: CcDatabase): CreditCardBillDao = database.creditCardBillDao()
}
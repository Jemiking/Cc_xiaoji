package com.ccxiaoji.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ccxiaoji.app.data.local.CcDatabase
import com.ccxiaoji.app.data.local.dao.*
import com.ccxiaoji.app.data.local.entity.CategoryEntity
import com.ccxiaoji.app.data.local.entity.CategoryType
import com.ccxiaoji.app.data.local.entity.UserEntity
import com.ccxiaoji.app.data.local.migrations.DatabaseMigrations
import com.ccxiaoji.app.data.local.migrations.DatabaseMigrations.MIGRATION_1_2
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
            .addMigrations(MIGRATION_1_2)
            // TODO: Add more migrations as needed
            // .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d(TAG, "Database onCreate callback triggered")
                    // 创建默认用户和默认账户
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d(TAG, "Initializing default data in database")
                        val currentTime = System.currentTimeMillis()
                        db.execSQL(
                            "INSERT INTO users (id, email, createdAt, updatedAt) VALUES (?, ?, ?, ?)",
                            arrayOf("current_user_id", "default@ccxiaoji.com", currentTime, currentTime)
                        )
                        db.execSQL(
                            "INSERT INTO accounts (id, userId, name, type, balanceCents, currency, isDefault, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            arrayOf("default_account_id", "current_user_id", "现金账户", "CASH", 0L, "CNY", 1, currentTime, currentTime, 0, 0)
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
                        
                        expenseCategories.forEachIndexed { index, (name, icon, color) ->
                            db.execSQL(
                                "INSERT INTO categories (id, userId, name, icon, color, type, parentId, isSystem, sortOrder, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                arrayOf(UUID.randomUUID().toString(), "current_user_id", name, icon, color, CategoryType.EXPENSE.name, null, 1, index, currentTime, currentTime, 0, 0)
                            )
                        }
                        
                        incomeCategories.forEachIndexed { index, (name, icon, color) ->
                            db.execSQL(
                                "INSERT INTO categories (id, userId, name, icon, color, type, parentId, isSystem, sortOrder, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                arrayOf(UUID.randomUUID().toString(), "current_user_id", name, icon, color, CategoryType.INCOME.name, null, 1, index, currentTime, currentTime, 0, 0)
                            )
                        }
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
}
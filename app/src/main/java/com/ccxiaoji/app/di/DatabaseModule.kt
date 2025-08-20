package com.ccxiaoji.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ccxiaoji.app.data.local.CcDatabase
import com.ccxiaoji.app.data.local.dao.CountdownDao
import com.ccxiaoji.shared.sync.data.local.dao.ChangeLogDao
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetDao
import com.ccxiaoji.feature.ledger.data.local.dao.RecurringTransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.SavingsGoalDao
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardPaymentDao
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardBillDao
import com.ccxiaoji.feature.ledger.data.local.dao.LedgerDao
import com.ccxiaoji.shared.user.data.local.dao.UserDao
import com.ccxiaoji.common.constants.DatabaseConstants
import com.ccxiaoji.core.database.migrations.DatabaseMigrations
import com.ccxiaoji.core.database.DatabaseModuleDebugHelper
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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CcDatabase {
        val databaseName = DatabaseModuleDebugHelper.getDatabaseName(context)
        
        return Room.databaseBuilder(
            context,
            CcDatabase::class.java,
            databaseName
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // 创建默认数据
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentTime = System.currentTimeMillis()
                        
                        // 插入默认用户
                        db.execSQL(
                            "INSERT INTO users (id, email, createdAt, updatedAt, isDeleted) VALUES (?, ?, ?, ?, ?)",
                            arrayOf("current_user_id", "default@ccxiaoji.com", currentTime, currentTime, 0)
                        )
                        
                        // 插入默认账户
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
                        
                        // 插入支出分类
                        expenseCategories.forEachIndexed { index, (name, icon, color) ->
                            db.execSQL(
                                "INSERT INTO categories (id, userId, name, type, icon, color, parentId, displayOrder, isSystem, usageCount, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                arrayOf(UUID.randomUUID().toString(), "current_user_id", name, "EXPENSE", icon, color, null, index, 1, 0, currentTime, currentTime, 0, "SYNCED")
                            )
                        }
                        
                        // 插入收入分类
                        incomeCategories.forEachIndexed { index, (name, icon, color) ->
                            db.execSQL(
                                "INSERT INTO categories (id, userId, name, type, icon, color, parentId, displayOrder, isSystem, usageCount, createdAt, updatedAt, isDeleted, syncStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                arrayOf(UUID.randomUUID().toString(), "current_user_id", name, "INCOME", icon, color, null, index, 1, 0, currentTime, currentTime, 0, "SYNCED")
                            )
                        }
                    }
                }
            })
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .build()
    }
    
    @Provides
    fun provideUserDao(database: CcDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideAccountDao(database: CcDatabase): AccountDao = database.accountDao()
    
    @Provides
    fun provideTransactionDao(database: CcDatabase): TransactionDao = database.transactionDao()
    
    @Provides
    fun provideTaskDao(database: CcDatabase): com.ccxiaoji.feature.todo.data.local.dao.TaskDao = database.taskDao()
    
    @Provides
    fun provideHabitDao(database: CcDatabase): com.ccxiaoji.feature.habit.data.local.dao.HabitDao = database.habitDao()
    
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
    
    @Provides
    fun provideLedgerDao(database: CcDatabase): LedgerDao = database.ledgerDao()
    
    @Provides
    fun provideShiftDao(database: CcDatabase): com.ccxiaoji.feature.schedule.data.local.dao.ShiftDao = database.shiftDao()
    
    @Provides
    fun provideScheduleDao(database: CcDatabase): com.ccxiaoji.feature.schedule.data.local.dao.ScheduleDao = database.scheduleDao()
    
    @Provides
    fun provideExportHistoryDao(database: CcDatabase): com.ccxiaoji.feature.schedule.data.local.dao.ExportHistoryDao = database.exportHistoryDao()
    
    @Provides
    fun providePlanDao(database: CcDatabase): com.ccxiaoji.feature.plan.data.local.dao.PlanDao = database.planDao()
    
    @Provides
    fun provideMilestoneDao(database: CcDatabase): com.ccxiaoji.feature.plan.data.local.dao.MilestoneDao = database.milestoneDao()
    
    @Provides
    fun provideTemplateDao(database: CcDatabase): com.ccxiaoji.feature.plan.data.local.dao.TemplateDao = database.templateDao()
}
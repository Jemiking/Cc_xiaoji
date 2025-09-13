package com.ccxiaoji.app.di

import android.content.Context
import androidx.room.Room
// no onCreate callback here; no SupportSQLiteDatabase needed
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
import com.ccxiaoji.feature.ledger.data.local.dao.CardDao
import com.ccxiaoji.core.database.dao.AutoLedgerDedupDao
import com.ccxiaoji.core.database.dao.AppAutoLedgerConfigDao
import com.ccxiaoji.shared.user.data.local.dao.UserDao
import com.ccxiaoji.core.database.migrations.DatabaseMigrations
import com.ccxiaoji.core.database.DatabaseModuleDebugHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
// removed coroutine and UUID imports since seeding moved to Application
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CcDatabase {
        val databaseName = DatabaseModuleDebugHelper.getDatabaseName(context)
        
        val builder = Room.databaseBuilder(
            context,
            CcDatabase::class.java,
            databaseName
        )
        // 统一初始化路径到 Application，移除这里的 onCreate 预置数据
        // 注册本模块新增的迁移
        builder.addMigrations(com.ccxiaoji.app.data.local.migrations.AppMigrations.MIGRATION_19_20)
        builder.addMigrations(com.ccxiaoji.app.data.local.migrations.AppMigrations.MIGRATION_20_21)
        // 保留已有的迁移集合
        builder.addMigrations(*com.ccxiaoji.core.database.migrations.DatabaseMigrations.getAllMigrations())

        return builder.build()
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
    fun provideLedgerLinkDao(database: CcDatabase): com.ccxiaoji.feature.ledger.data.local.dao.LedgerLinkDao = database.ledgerLinkDao()
    
    @Provides
    fun provideTransactionLedgerRelationDao(database: CcDatabase): com.ccxiaoji.feature.ledger.data.local.dao.TransactionLedgerRelationDao = database.transactionLedgerRelationDao()
    
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
    
    @Provides
    fun provideAutoLedgerDedupDao(database: CcDatabase): AutoLedgerDedupDao = database.autoLedgerDedupDao()
    
    @Provides
    fun provideAppAutoLedgerConfigDao(database: CcDatabase): AppAutoLedgerConfigDao = database.appAutoLedgerConfigDao()

    @Provides
    fun provideCardDao(database: CcDatabase): CardDao = database.cardDao()
}

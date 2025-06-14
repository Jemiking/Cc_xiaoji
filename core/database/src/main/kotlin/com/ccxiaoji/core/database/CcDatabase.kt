package com.ccxiaoji.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ccxiaoji.core.database.dao.*
import com.ccxiaoji.core.database.entity.*

@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitRecordEntity::class,
        CountdownEntity::class,
        ChangeLogEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class,
        CreditCardPaymentEntity::class,
        CreditCardBillEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CcDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun countdownDao(): CountdownDao
    abstract fun changeLogDao(): ChangeLogDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun creditCardPaymentDao(): CreditCardPaymentDao
    abstract fun creditCardBillDao(): CreditCardBillDao
    
    companion object {
        const val DATABASE_NAME = "cc_xiaoji.db"
    }
}
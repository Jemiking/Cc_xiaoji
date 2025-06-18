package com.ccxiaoji.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
// TODO: 暂时注释掉Entity和DAO引用，待Feature模块迁移后恢复
// import com.ccxiaoji.app.data.local.dao.*
// import com.ccxiaoji.app.data.local.entity.*

// TODO: 这是临时解决方案，待所有模块迁移完成后恢复
@Database(
    entities = [PlaceholderEntity::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CcDatabase : RoomDatabase() {
    // TODO: 暂时注释掉DAO方法，待Feature模块迁移后恢复
    // abstract fun userDao(): UserDao
    // abstract fun accountDao(): AccountDao
    // abstract fun categoryDao(): CategoryDao
    // abstract fun transactionDao(): TransactionDao
    // abstract fun taskDao(): TaskDao
    // abstract fun habitDao(): HabitDao
    // abstract fun countdownDao(): CountdownDao
    // abstract fun changeLogDao(): ChangeLogDao
    // abstract fun budgetDao(): BudgetDao
    // abstract fun recurringTransactionDao(): RecurringTransactionDao
    // abstract fun savingsGoalDao(): SavingsGoalDao
    // abstract fun creditCardPaymentDao(): CreditCardPaymentDao
    // abstract fun creditCardBillDao(): CreditCardBillDao
}
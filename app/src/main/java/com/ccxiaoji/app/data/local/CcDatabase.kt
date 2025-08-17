package com.ccxiaoji.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ccxiaoji.app.data.local.dao.CountdownDao
import com.ccxiaoji.shared.sync.data.local.dao.ChangeLogDao
import com.ccxiaoji.app.data.local.entity.CountdownEntity
import com.ccxiaoji.shared.sync.data.local.entity.ChangeLogEntity
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetDao
import com.ccxiaoji.feature.ledger.data.local.dao.RecurringTransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.SavingsGoalDao
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardPaymentDao
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardBillDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import com.ccxiaoji.feature.ledger.data.local.entity.BudgetEntity
import com.ccxiaoji.feature.ledger.data.local.entity.RecurringTransactionEntity
import com.ccxiaoji.feature.ledger.data.local.entity.SavingsGoalEntity
import com.ccxiaoji.feature.ledger.data.local.entity.SavingsContributionEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardPaymentEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardBillEntity
import com.ccxiaoji.core.database.Converters
import com.ccxiaoji.shared.user.data.local.dao.UserDao
import com.ccxiaoji.shared.user.data.local.entity.UserEntity
import com.ccxiaoji.feature.todo.data.local.dao.TaskDao
import com.ccxiaoji.feature.todo.data.local.entity.TaskEntity
import com.ccxiaoji.feature.habit.data.local.dao.HabitDao
import com.ccxiaoji.feature.habit.data.local.entity.HabitEntity
import com.ccxiaoji.feature.habit.data.local.entity.HabitRecordEntity
import com.ccxiaoji.feature.schedule.data.local.dao.ShiftDao
import com.ccxiaoji.feature.schedule.data.local.dao.ScheduleDao
import com.ccxiaoji.feature.schedule.data.local.dao.ExportHistoryDao
import com.ccxiaoji.feature.schedule.data.local.entity.ShiftEntity
import com.ccxiaoji.feature.schedule.data.local.entity.ScheduleEntity
import com.ccxiaoji.feature.schedule.data.local.entity.ExportHistoryEntity
import com.ccxiaoji.feature.schedule.data.local.entity.PatternEntity
import com.ccxiaoji.feature.plan.data.local.dao.PlanDao
import com.ccxiaoji.feature.plan.data.local.dao.MilestoneDao
import com.ccxiaoji.feature.plan.data.local.dao.TemplateDao
import com.ccxiaoji.feature.plan.data.local.entity.PlanEntity
import com.ccxiaoji.feature.plan.data.local.entity.MilestoneEntity
import com.ccxiaoji.feature.plan.data.local.entity.TemplateEntity

// TODO: 这是临时文件，待所有模块迁移完成后删除
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
        CreditCardBillEntity::class,
        ShiftEntity::class,
        ScheduleEntity::class,
        ExportHistoryEntity::class,
        PatternEntity::class,
        PlanEntity::class,
        MilestoneEntity::class,
        TemplateEntity::class
    ],
    version = 9,
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
    abstract fun shiftDao(): ShiftDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun exportHistoryDao(): ExportHistoryDao
    abstract fun planDao(): PlanDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun templateDao(): TemplateDao
}
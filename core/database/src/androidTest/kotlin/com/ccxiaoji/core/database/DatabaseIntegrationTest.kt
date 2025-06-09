package com.ccxiaoji.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ccxiaoji.core.database.entity.*
import com.ccxiaoji.core.database.model.RecurringFrequency
import com.ccxiaoji.core.database.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * 数据库集成测试
 * 测试模块化后数据库的完整功能
 */
@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {
    
    private lateinit var database: CcDatabase
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = TestDatabaseFactory.createInMemoryDatabase(context)
    }
    
    @After
    fun tearDown() {
        TestDatabaseFactory.cleanupDatabase(database)
    }
    
    @Test
    fun testCompleteTransactionFlow() = runBlocking {
        // 1. 创建用户
        val user = UserEntity(
            id = "test_user",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.userDao().insertUser(user)
        
        // 2. 创建账户
        val account = AccountEntity(
            id = "test_account",
            userId = user.id,
            name = "主账户",
            type = "CASH",
            balanceCents = 100000,
            currency = "CNY",
            isDefault = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.accountDao().insertAccount(account)
        
        // 3. 创建分类
        val category = CategoryEntity(
            id = "test_category",
            userId = user.id,
            name = "日常开销",
            type = CategoryType.EXPENSE,
            icon = "shopping_cart",
            color = "#FF5722",
            parentId = null,
            orderIndex = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.categoryDao().insertCategory(category)
        
        // 4. 创建交易
        val transaction = TransactionEntity(
            id = "test_transaction",
            userId = user.id,
            accountId = account.id,
            categoryId = category.id,
            type = "EXPENSE",
            amountCents = 5000,
            note = "测试交易",
            transactionDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.transactionDao().insertTransaction(transaction)
        
        // 5. 验证交易流程
        val transactions = database.transactionDao().getTransactionsByAccount(account.id).first()
        assert(transactions.size == 1)
        assert(transactions[0].id == transaction.id)
        
        // 6. 验证账户余额计算
        val totalExpense = database.transactionDao().getTotalExpenseByAccount(account.id)
        assert(totalExpense == 5000L)
    }
    
    @Test
    fun testCreditCardBillFlow() = runBlocking {
        // 1. 创建用户
        val user = UserEntity(
            id = "test_user",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.userDao().insertUser(user)
        
        // 2. 创建信用卡账户
        val creditCard = AccountEntity(
            id = "credit_card",
            userId = user.id,
            name = "信用卡",
            type = "CREDIT_CARD",
            balanceCents = -50000,
            currency = "CNY",
            isDefault = false,
            billingDay = 25,
            paymentDueDay = 15,
            creditLimitCents = 1000000,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.accountDao().insertAccount(creditCard)
        
        // 3. 创建账单
        val bill = CreditCardBillEntity(
            id = UUID.randomUUID().toString(),
            accountId = creditCard.id,
            billPeriodStart = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000,
            billPeriodEnd = System.currentTimeMillis(),
            billDate = System.currentTimeMillis(),
            paymentDueDate = System.currentTimeMillis() + 20 * 24 * 60 * 60 * 1000,
            previousBalanceCents = 0,
            newChargesCents = 50000,
            paymentsCents = 0,
            adjustmentsCents = 0,
            totalAmountCents = 50000,
            minimumPaymentCents = 5000,
            remainingAmountCents = 50000,
            isPaid = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        database.creditCardBillDao().insertBill(bill)
        
        // 4. 创建还款记录
        val payment = CreditCardPaymentEntity(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            creditCardAccountId = creditCard.id,
            billId = bill.id,
            paymentAccountId = null,
            paymentAmountCents = 5000,
            paymentDate = System.currentTimeMillis(),
            note = "最低还款",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        database.creditCardPaymentDao().insertPayment(payment)
        
        // 5. 验证账单和还款
        val bills = database.creditCardBillDao().getBillsByAccount(creditCard.id).first()
        assert(bills.size == 1)
        
        val payments = database.creditCardPaymentDao().getPaymentsByBill(bill.id).first()
        assert(payments.size == 1)
        assert(payments[0].paymentAmountCents == 5000L)
    }
    
    @Test
    fun testRecurringTransactionFlow() = runBlocking {
        // 1. 创建基础数据
        val user = UserEntity(
            id = "test_user",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.userDao().insertUser(user)
        
        val account = AccountEntity(
            id = "test_account",
            userId = user.id,
            name = "主账户",
            type = "CASH",
            balanceCents = 100000,
            currency = "CNY",
            isDefault = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.accountDao().insertAccount(account)
        
        val category = CategoryEntity(
            id = "test_category",
            userId = user.id,
            name = "房租",
            type = CategoryType.EXPENSE,
            icon = "home",
            color = "#795548",
            parentId = null,
            orderIndex = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.categoryDao().insertCategory(category)
        
        // 2. 创建定期交易
        val recurring = RecurringTransactionEntity(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            accountId = account.id,
            categoryId = category.id,
            type = "EXPENSE",
            amountCents = 300000,
            note = "每月房租",
            frequency = RecurringFrequency.MONTHLY,
            dayOfMonth = 1,
            isEnabled = true,
            nextExecutionDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
            lastExecutionDate = null,
            endDate = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.recurringTransactionDao().insertRecurringTransaction(recurring)
        
        // 3. 验证定期交易
        val recurringTransactions = database.recurringTransactionDao()
            .getAllRecurringTransactions(user.id).first()
        assert(recurringTransactions.size == 1)
        assert(recurringTransactions[0].frequency == RecurringFrequency.MONTHLY)
        assert(recurringTransactions[0].amountCents == 300000L)
    }
    
    @Test
    fun testHabitTrackingFlow() = runBlocking {
        // 1. 创建用户
        val user = UserEntity(
            id = "test_user",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.userDao().insertUser(user)
        
        // 2. 创建习惯
        val habit = HabitEntity(
            id = "test_habit",
            userId = user.id,
            name = "每日运动",
            description = "每天运动30分钟",
            icon = "fitness_center",
            color = "#4CAF50",
            targetDays = 30,
            reminderTime = "19:00",
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.habitDao().insertHabit(habit)
        
        // 3. 记录习惯完成
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val record = HabitRecordEntity(
            id = UUID.randomUUID().toString(),
            habitId = habit.id,
            date = today,
            isCompleted = true,
            note = "完成了30分钟跑步",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        database.habitDao().insertRecord(record)
        
        // 4. 验证习惯记录
        val records = database.habitDao().getRecordsByHabit(habit.id).first()
        assert(records.size == 1)
        assert(records[0].isCompleted)
        
        val todayRecord = database.habitDao().getRecordByDate(habit.id, today)
        assert(todayRecord != null)
        assert(todayRecord?.note == "完成了30分钟跑步")
    }
    
    @Test
    fun testSavingsGoalFlow() = runBlocking {
        // 1. 创建用户
        val user = UserEntity(
            id = "test_user",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.userDao().insertUser(user)
        
        // 2. 创建储蓄目标
        val goal = SavingsGoalEntity(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            name = "旅游基金",
            targetAmountCents = 5000000, // 50000元
            currentAmountCents = 0,
            targetDate = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000,
            icon = "flight",
            color = "#2196F3",
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
        database.savingsGoalDao().insertGoal(goal)
        
        // 3. 添加储蓄记录
        val contribution = SavingsContributionEntity(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            amountCents = 500000, // 5000元
            note = "月度储蓄",
            contributionDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        database.savingsGoalDao().insertContribution(contribution)
        
        // 4. 更新目标进度
        database.savingsGoalDao().updateGoalAmount(goal.id, 500000)
        
        // 5. 验证储蓄目标
        val goals = database.savingsGoalDao().getAllGoals(user.id).first()
        assert(goals.size == 1)
        assert(goals[0].currentAmountCents == 500000L)
        
        val contributions = database.savingsGoalDao().getContributionsByGoal(goal.id).first()
        assert(contributions.size == 1)
        assert(contributions[0].amountCents == 500000L)
    }
}
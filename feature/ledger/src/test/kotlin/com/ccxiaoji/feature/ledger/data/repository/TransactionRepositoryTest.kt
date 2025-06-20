package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import com.ccxiaoji.feature.ledger.domain.model.CategoryDetails
import com.ccxiaoji.shared.sync.data.local.dao.ChangeLogDao
import com.ccxiaoji.shared.user.api.UserApi
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.junit.Before
import org.junit.Test

class TransactionRepositoryTest {

    @MockK
    private lateinit var transactionDao: TransactionDao

    @MockK
    private lateinit var changeLogDao: ChangeLogDao

    @MockK
    private lateinit var userApi: UserApi

    @MockK
    private lateinit var accountDao: AccountDao

    @MockK
    private lateinit var categoryDao: CategoryDao

    @MockK
    private lateinit var gson: Gson

    private lateinit var transactionRepository: TransactionRepository

    private val testUserId = "test-user-123"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { userApi.getCurrentUserId() } returns testUserId
        coEvery { gson.toJson(any()) } returns "{}"
        transactionRepository = TransactionRepository(
            transactionDao, changeLogDao, userApi, accountDao, categoryDao, gson
        )
    }

    @Test
    fun `获取用户的所有交易记录`() = runTest {
        // Given
        val transactionEntities = createTestTransactionEntities()
        val testCategory = createTestCategoryEntity("cat1", "餐饮")
        
        coEvery { transactionDao.getTransactionsByUser(testUserId) } returns flowOf(transactionEntities)
        coEvery { categoryDao.getCategoryById(any()) } returns testCategory

        // When
        val result = transactionRepository.getTransactions().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].amountCents).isEqualTo(5000)
        assertThat(result[1].amountCents).isEqualTo(8000)
        assertThat(result.all { it.categoryDetails != null }).isTrue()
        coVerify(exactly = 1) { transactionDao.getTransactionsByUser(testUserId) }
    }

    @Test
    fun `获取指定日期范围的交易记录`() = runTest {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)
        val transactionEntities = createTestTransactionEntities()
        
        coEvery { 
            transactionDao.getTransactionsByDateRange(testUserId, any(), any()) 
        } returns flowOf(transactionEntities)

        // When
        val result = transactionRepository.getTransactionsByDateRange(startDate, endDate).first()

        // Then
        assertThat(result).hasSize(2)
        coVerify(exactly = 1) { 
            transactionDao.getTransactionsByDateRange(testUserId, any(), any()) 
        }
    }

    @Test
    fun `获取月度总支出`() = runTest {
        // Given
        val year = 2024
        val month = 1
        val totalAmount = 250000
        
        coEvery { 
            transactionDao.getTotalAmountByDateRange(testUserId, any(), any()) 
        } returns totalAmount

        // When
        val result = transactionRepository.getMonthlyTotal(year, month)

        // Then
        assertThat(result).isEqualTo(250000)
        coVerify(exactly = 1) { 
            transactionDao.getTotalAmountByDateRange(testUserId, any(), any()) 
        }
    }

    @Test
    fun `获取月度收入和支出`() = runTest {
        // Given
        val year = 2024
        val month = 1
        val income = 300000
        val expense = 200000
        
        coEvery { 
            transactionDao.getTotalByType(testUserId, any(), any(), "INCOME") 
        } returns income
        coEvery { 
            transactionDao.getTotalByType(testUserId, any(), any(), "EXPENSE") 
        } returns expense

        // When
        val (resultIncome, resultExpense) = transactionRepository.getMonthlyIncomesAndExpenses(year, month)

        // Then
        assertThat(resultIncome).isEqualTo(300000)
        assertThat(resultExpense).isEqualTo(200000)
    }

    @Test
    fun `添加新交易 - 支出`() = runTest {
        // Given
        val amountCents = 5000
        val categoryId = "cat1"
        val note = "午餐"
        val accountId = "acc1"
        
        val defaultAccount = createTestAccountEntity(accountId, "现金账户")
        val expenseCategory = createTestCategoryEntity(categoryId, "餐饮", type = "EXPENSE")
        
        coEvery { accountDao.getDefaultAccount(testUserId) } returns defaultAccount
        coEvery { categoryDao.getCategoryById(categoryId) } returns expenseCategory
        
        val transactionSlot = slot<TransactionEntity>()
        coEvery { transactionDao.insertTransaction(capture(transactionSlot)) } returns Unit
        coEvery { accountDao.updateBalance(any(), any(), any()) } returns Unit
        coEvery { categoryDao.incrementUsageCount(any()) } returns Unit
        coEvery { changeLogDao.insertChange(any()) } returns Unit

        // When
        val result = transactionRepository.addTransaction(
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            accountId = accountId
        )

        // Then
        assertThat(result.amountCents).isEqualTo(amountCents)
        assertThat(result.categoryId).isEqualTo(categoryId)
        assertThat(result.note).isEqualTo(note)
        
        val capturedTransaction = transactionSlot.captured
        assertThat(capturedTransaction.userId).isEqualTo(testUserId)
        assertThat(capturedTransaction.syncStatus).isEqualTo(SyncStatus.PENDING_SYNC)
        
        // 验证账户余额更新（支出为负数）
        coVerify(exactly = 1) { 
            accountDao.updateBalance(accountId, -5000L, any()) 
        }
        coVerify(exactly = 1) { categoryDao.incrementUsageCount(categoryId) }
    }

    @Test
    fun `添加新交易 - 收入`() = runTest {
        // Given
        val amountCents = 10000
        val categoryId = "cat2"
        val note = "工资"
        
        val defaultAccount = createTestAccountEntity("acc1", "工资卡")
        val incomeCategory = createTestCategoryEntity(categoryId, "工资", type = "INCOME")
        
        coEvery { accountDao.getDefaultAccount(testUserId) } returns defaultAccount
        coEvery { categoryDao.getCategoryById(categoryId) } returns incomeCategory
        coEvery { transactionDao.insertTransaction(any()) } returns Unit
        coEvery { accountDao.updateBalance(any(), any(), any()) } returns Unit
        coEvery { categoryDao.incrementUsageCount(any()) } returns Unit
        coEvery { changeLogDao.insertChange(any()) } returns Unit

        // When
        val result = transactionRepository.addTransaction(
            amountCents = amountCents,
            categoryId = categoryId,
            note = note
        )

        // Then
        assertThat(result.amountCents).isEqualTo(amountCents)
        
        // 验证账户余额更新（收入为正数）
        coVerify(exactly = 1) { 
            accountDao.updateBalance("acc1", 10000L, any()) 
        }
    }

    @Test
    fun `搜索交易记录`() = runTest {
        // Given
        val query = "餐饮"
        val searchResults = createTestTransactionEntities()
        
        coEvery { 
            transactionDao.searchTransactions(testUserId, query) 
        } returns flowOf(searchResults)

        // When
        val result = transactionRepository.searchTransactions(query).first()

        // Then
        assertThat(result).hasSize(2)
        coVerify(exactly = 1) { transactionDao.searchTransactions(testUserId, query) }
    }

    @Test
    fun `软删除交易记录`() = runTest {
        // Given
        val transactionId = "trans-123"
        coEvery { transactionDao.softDeleteTransaction(any(), any()) } returns Unit
        coEvery { changeLogDao.insertChange(any()) } returns Unit

        // When
        transactionRepository.deleteTransaction(transactionId)

        // Then
        coVerify(exactly = 1) { transactionDao.softDeleteTransaction(transactionId, any()) }
        coVerify(exactly = 1) { changeLogDao.insertChange(any()) }
    }

    @Test
    fun `计算存款率`() = runTest {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)
        val income = 100000
        val expense = 60000
        
        coEvery { 
            transactionDao.getTotalByType(testUserId, any(), any(), "INCOME") 
        } returns income
        coEvery { 
            transactionDao.getTotalByType(testUserId, any(), any(), "EXPENSE") 
        } returns expense

        // When
        val savingsRate = transactionRepository.calculateSavingsRate(startDate, endDate)

        // Then
        assertThat(savingsRate).isWithin(0.01f).of(40.0f) // 40% 存款率
    }

    @Test
    fun `计算存款率 - 无收入情况`() = runTest {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)
        
        coEvery { 
            transactionDao.getTotalByType(testUserId, any(), any(), "INCOME") 
        } returns 0
        coEvery { 
            transactionDao.getTotalByType(testUserId, any(), any(), "EXPENSE") 
        } returns 5000

        // When
        val savingsRate = transactionRepository.calculateSavingsRate(startDate, endDate)

        // Then
        assertThat(savingsRate).isEqualTo(0f)
    }

    private fun createTestTransactionEntities(): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            TransactionEntity(
                id = "trans1",
                userId = testUserId,
                accountId = "acc1",
                amountCents = 5000,
                categoryId = "cat1",
                note = "午餐",
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.SYNCED,
                isDeleted = false
            ),
            TransactionEntity(
                id = "trans2",
                userId = testUserId,
                accountId = "acc1",
                amountCents = 8000,
                categoryId = "cat1",
                note = "晚餐",
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.SYNCED,
                isDeleted = false
            )
        )
    }

    private fun createTestAccountEntity(id: String, name: String): AccountEntity {
        val now = System.currentTimeMillis()
        return AccountEntity(
            id = id,
            userId = testUserId,
            name = name,
            type = "CASH",
            balanceCents = 100000,
            currency = "CNY",
            icon = "💵",
            color = "#4CAF50",
            isDefault = true,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED,
            isDeleted = false
        )
    }

    private fun createTestCategoryEntity(
        id: String,
        name: String,
        type: String = "EXPENSE"
    ): CategoryEntity {
        val now = System.currentTimeMillis()
        return CategoryEntity(
            id = id,
            userId = testUserId,
            name = name,
            type = type,
            icon = "🍔",
            color = "#FF5722",
            parentId = null,
            displayOrder = 1,
            usageCount = 0,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED,
            isDeleted = false
        )
    }
}
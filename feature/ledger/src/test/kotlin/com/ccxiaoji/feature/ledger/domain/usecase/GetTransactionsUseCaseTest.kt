package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.data.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.junit.Before
import org.junit.Test

class GetTransactionsUseCaseTest {

    @MockK
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var getTransactionsUseCase: GetTransactionsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // 假设存在一个GetTransactionsUseCase类
        getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    }

    @Test
    fun `获取指定日期范围的交易记录`() = runTest {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)
        val now = Clock.System.now()
        
        val account = Account(
            id = "account1",
            name = "现金账户",
            type = "CASH",
            balance = 10000.0,
            currency = "CNY",
            icon = "💵",
            color = "#4CAF50",
            isDefault = true,
            createdAt = now,
            updatedAt = now
        )
        
        val category = Category(
            id = "category1",
            name = "餐饮",
            type = "EXPENSE",
            icon = "🍔",
            color = "#FF5722",
            parentId = null,
            orderIndex = 1,
            createdAt = now,
            updatedAt = now
        )
        
        val transactions = listOf(
            Transaction(
                id = "trans1",
                accountId = "account1",
                categoryId = "category1",
                amountCents = 5000L, // 50元
                type = "EXPENSE",
                note = "午餐",
                transactionDate = startDate.atTime(12, 0).toInstant(TimeZone.currentSystemDefault()),
                createdAt = now,
                updatedAt = now,
                accountDetails = account,
                categoryDetails = category,
                tags = listOf("餐饮", "工作日")
            ),
            Transaction(
                id = "trans2",
                accountId = "account1",
                categoryId = "category1",
                amountCents = 8000L, // 80元
                type = "EXPENSE",
                note = "晚餐",
                transactionDate = startDate.atTime(18, 30).toInstant(TimeZone.currentSystemDefault()),
                createdAt = now,
                updatedAt = now,
                accountDetails = account,
                categoryDetails = category,
                tags = listOf("餐饮", "聚餐")
            )
        )

        coEvery { 
            transactionRepository.getTransactionsByDateRange(startDate, endDate) 
        } returns flowOf(transactions)

        // When
        val result = getTransactionsUseCase.invoke(startDate, endDate).first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].amountCents).isEqualTo(5000L)
        assertThat(result[1].amountCents).isEqualTo(8000L)
        assertThat(result.sumOf { it.amountCents }).isEqualTo(13000L)
        coVerify(exactly = 1) { transactionRepository.getTransactionsByDateRange(startDate, endDate) }
    }

    @Test
    fun `获取最近交易记录`() = runTest {
        // Given
        val limit = 5
        val now = Clock.System.now()
        val recentTransactions = listOf(
            createTestTransaction("trans1", 10000L, "收入", "INCOME"),
            createTestTransaction("trans2", 5000L, "支出", "EXPENSE"),
            createTestTransaction("trans3", 3000L, "支出", "EXPENSE")
        )

        coEvery { transactionRepository.getRecentTransactions(limit) } returns flowOf(recentTransactions)

        // When
        val result = transactionRepository.getRecentTransactions(limit).first()

        // Then
        assertThat(result).hasSize(3)
        assertThat(result[0].amountCents).isEqualTo(10000L)
        assertThat(result[0].type).isEqualTo("INCOME")
        coVerify(exactly = 1) { transactionRepository.getRecentTransactions(limit) }
    }

    @Test
    fun `计算月度总支出`() = runTest {
        // Given
        val year = 2024
        val month = 1
        val monthlyTotal = 250000L // 2500元

        coEvery { transactionRepository.getMonthlyTotal(year, month) } returns monthlyTotal

        // When
        val result = transactionRepository.getMonthlyTotal(year, month)

        // Then
        assertThat(result).isEqualTo(250000L)
        assertThat(result / 100.0).isEqualTo(2500.0) // 转换为元
        coVerify(exactly = 1) { transactionRepository.getMonthlyTotal(year, month) }
    }

    @Test
    fun `按类别获取交易记录`() = runTest {
        // Given
        val categoryId = "food_category"
        val transactions = listOf(
            createTestTransaction("trans1", 5000L, "早餐", "EXPENSE", categoryId),
            createTestTransaction("trans2", 8000L, "午餐", "EXPENSE", categoryId),
            createTestTransaction("trans3", 12000L, "晚餐", "EXPENSE", categoryId)
        )

        coEvery { 
            transactionRepository.getTransactionsByCategory(categoryId) 
        } returns flowOf(transactions)

        // When
        val result = transactionRepository.getTransactionsByCategory(categoryId).first()

        // Then
        assertThat(result).hasSize(3)
        assertThat(result.all { it.categoryId == categoryId }).isTrue()
        assertThat(result.sumOf { it.amountCents }).isEqualTo(25000L) // 总计250元
    }

    // 辅助方法：创建测试用的Transaction对象
    private fun createTestTransaction(
        id: String,
        amountCents: Long,
        note: String,
        type: String,
        categoryId: String = "category1"
    ): Transaction {
        val now = Clock.System.now()
        return Transaction(
            id = id,
            accountId = "account1",
            categoryId = categoryId,
            amountCents = amountCents,
            type = type,
            note = note,
            transactionDate = now,
            createdAt = now,
            updatedAt = now,
            accountDetails = null,
            categoryDetails = null,
            tags = emptyList()
        )
    }
}

// 假设的UseCase类，实际项目中应该存在
class GetTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate) = 
        transactionRepository.getTransactionsByDateRange(startDate, endDate)
}
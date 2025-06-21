package com.ccxiaoji.feature.ledger.integration

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.core.database.dao.AccountDao
import com.ccxiaoji.core.database.dao.CategoryDao
import com.ccxiaoji.core.database.dao.TransactionDao
import com.ccxiaoji.core.database.entity.AccountEntity
import com.ccxiaoji.core.database.entity.CategoryEntity
import com.ccxiaoji.core.database.entity.TransactionEntity
import com.ccxiaoji.core.database.entity.TransactionType
import com.ccxiaoji.core.database.relation.TransactionWithDetails
import com.ccxiaoji.feature.ledger.data.repository.AccountRepositoryImpl
import com.ccxiaoji.feature.ledger.data.repository.CategoryRepositoryImpl
import com.ccxiaoji.feature.ledger.data.repository.TransactionRepositoryImpl
import com.ccxiaoji.feature.ledger.domain.usecase.AddTransactionUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.GetTransactionsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Ledger模块集成测试
 * 测试Repository和UseCase的协同工作
 */
class LedgerIntegrationTest {

    @MockK
    private lateinit var transactionDao: TransactionDao
    
    @MockK
    private lateinit var accountDao: AccountDao
    
    @MockK
    private lateinit var categoryDao: CategoryDao
    
    private lateinit var transactionRepository: TransactionRepositoryImpl
    private lateinit var accountRepository: AccountRepositoryImpl
    private lateinit var categoryRepository: CategoryRepositoryImpl
    
    private lateinit var addTransactionUseCase: AddTransactionUseCase
    private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // 初始化Repository
        transactionRepository = TransactionRepositoryImpl(transactionDao)
        accountRepository = AccountRepositoryImpl(accountDao)
        categoryRepository = CategoryRepositoryImpl(categoryDao)
        
        // 初始化UseCase
        addTransactionUseCase = AddTransactionUseCase(transactionRepository)
        getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    }
    
    @Test
    fun `完整的交易创建和查询流程`() = runTest {
        // Given - 准备测试数据
        val accountEntity = AccountEntity(
            id = 1L,
            userId = 1L,
            name = "现金账户",
            type = "cash",
            balance = 1000.0,
            icon = "💵",
            color = "#4CAF50"
        )
        
        val categoryEntity = CategoryEntity(
            id = 1L,
            userId = 1L,
            name = "餐饮",
            type = TransactionType.EXPENSE,
            icon = "🍔",
            color = "#FF5722",
            parentId = null
        )
        
        val transactionEntity = TransactionEntity(
            id = 1L,
            userId = 1L,
            type = TransactionType.EXPENSE,
            amount = 50.0,
            categoryId = 1L,
            accountId = 1L,
            note = "午餐",
            date = System.currentTimeMillis()
        )
        
        val transactionWithDetails = TransactionWithDetails(
            transaction = transactionEntity,
            account = accountEntity,
            category = categoryEntity
        )
        
        // Mock DAO行为
        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { transactionDao.getTransactionById(1L) } returns transactionWithDetails
        coEvery { transactionDao.getTransactionsWithDetails(1L) } returns flowOf(listOf(transactionWithDetails))
        coEvery { accountDao.updateBalance(1L, 950.0) } returns Unit
        
        // When - 添加交易
        val addResult = addTransactionUseCase(
            type = TransactionType.EXPENSE,
            amount = 50.0,
            categoryId = 1L,
            accountId = 1L,
            note = "午餐",
            date = System.currentTimeMillis()
        )
        
        // Then - 验证添加成功
        assertThat(addResult).isNotNull()
        assertThat(addResult.transaction.amount).isEqualTo(50.0)
        assertThat(addResult.account.name).isEqualTo("现金账户")
        assertThat(addResult.category.name).isEqualTo("餐饮")
        
        // When - 查询交易
        val transactions = getTransactionsUseCase(1L).first()
        
        // Then - 验证查询结果
        assertThat(transactions).hasSize(1)
        assertThat(transactions.first().transaction.note).isEqualTo("午餐")
    }
    
    @Test
    fun `账户余额计算集成测试`() = runTest {
        // Given - 多个交易
        val accountId = 1L
        val transactions = listOf(
            mockTransactionWithDetails(100.0, TransactionType.INCOME),
            mockTransactionWithDetails(50.0, TransactionType.EXPENSE),
            mockTransactionWithDetails(200.0, TransactionType.INCOME),
            mockTransactionWithDetails(75.0, TransactionType.EXPENSE)
        )
        
        coEvery { transactionDao.getTransactionsByAccount(accountId) } returns flowOf(transactions)
        
        // When - 计算账户余额
        val totalIncome = transactions
            .filter { it.transaction.type == TransactionType.INCOME }
            .sumOf { it.transaction.amount }
        val totalExpense = transactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .sumOf { it.transaction.amount }
        val balance = totalIncome - totalExpense
        
        // Then
        assertThat(totalIncome).isEqualTo(300.0)
        assertThat(totalExpense).isEqualTo(125.0)
        assertThat(balance).isEqualTo(175.0)
    }
    
    @Test
    fun `分类统计集成测试`() = runTest {
        // Given - 同一分类的多个交易
        val categoryId = 1L
        val transactions = listOf(
            mockTransactionWithDetails(50.0, TransactionType.EXPENSE, categoryId),
            mockTransactionWithDetails(75.0, TransactionType.EXPENSE, categoryId),
            mockTransactionWithDetails(100.0, TransactionType.EXPENSE, categoryId)
        )
        
        coEvery { transactionDao.getTransactionsByCategory(categoryId) } returns flowOf(transactions)
        
        // When - 计算分类总额
        val categoryTotal = transactions.sumOf { it.transaction.amount }
        
        // Then
        assertThat(categoryTotal).isEqualTo(225.0)
    }
    
    @Test
    fun `错误处理集成测试 - 无效账户ID`() = runTest {
        // Given
        val invalidAccountId = 999L
        coEvery { accountDao.getAccountById(invalidAccountId) } returns null
        coEvery { transactionDao.insertTransaction(any()) } throws Exception("账户不存在")
        
        // When - 尝试添加交易
        try {
            addTransactionUseCase(
                type = TransactionType.EXPENSE,
                amount = 50.0,
                categoryId = 1L,
                accountId = invalidAccountId,
                note = "测试",
                date = System.currentTimeMillis()
            )
            // Should not reach here
            assert(false) { "应该抛出异常" }
        } catch (e: Exception) {
            // Then
            assertThat(e.message).contains("账户不存在")
        }
    }
    
    // 辅助方法
    private fun mockTransactionWithDetails(
        amount: Double,
        type: TransactionType,
        categoryId: Long = 1L
    ): TransactionWithDetails {
        return TransactionWithDetails(
            transaction = TransactionEntity(
                id = System.currentTimeMillis(),
                userId = 1L,
                type = type,
                amount = amount,
                categoryId = categoryId,
                accountId = 1L,
                note = "测试交易",
                date = System.currentTimeMillis()
            ),
            account = mockk(),
            category = mockk()
        )
    }
}
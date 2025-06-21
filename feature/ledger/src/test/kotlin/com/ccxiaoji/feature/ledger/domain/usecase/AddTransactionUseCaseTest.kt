package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.core.database.entity.TransactionType
import com.ccxiaoji.core.database.relation.TransactionWithDetails
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class AddTransactionUseCaseTest {

    @MockK
    private lateinit var transactionRepository: TransactionRepository
    
    private lateinit var addTransactionUseCase: AddTransactionUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        addTransactionUseCase = AddTransactionUseCase(transactionRepository)
    }

    @Test
    fun `invoke should add transaction successfully`() = runTest {
        // Given
        val type = TransactionType.EXPENSE
        val amount = 100.0
        val categoryId = 1L
        val accountId = 1L
        val note = "午餐"
        val date = System.currentTimeMillis()
        
        val expectedTransaction = mockk<TransactionWithDetails>()
        
        coEvery { 
            transactionRepository.addTransaction(
                type, amount, categoryId, accountId, note, date
            ) 
        } returns BaseResult.Success(expectedTransaction)

        // When
        val result = addTransactionUseCase(
            type, amount, categoryId, accountId, note, date
        )

        // Then
        assertThat(result).isEqualTo(expectedTransaction)
        coVerify(exactly = 1) { 
            transactionRepository.addTransaction(
                type, amount, categoryId, accountId, note, date
            )
        }
    }

    @Test
    fun `invoke should throw exception when amount is zero`() = runTest {
        // Given
        val type = TransactionType.EXPENSE
        val amount = 0.0
        val categoryId = 1L
        val accountId = 1L
        val note = "测试"
        val date = System.currentTimeMillis()

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            addTransactionUseCase(
                type, amount, categoryId, accountId, note, date
            )
        }
        coVerify(exactly = 0) { 
            transactionRepository.addTransaction(any(), any(), any(), any(), any(), any()) 
        }
    }

    @Test
    fun `invoke should throw exception when amount is negative`() = runTest {
        // Given
        val type = TransactionType.INCOME
        val amount = -100.0
        val categoryId = 1L
        val accountId = 1L
        val note = "测试"
        val date = System.currentTimeMillis()

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            addTransactionUseCase(
                type, amount, categoryId, accountId, note, date
            )
        }
    }

    @Test
    fun `invoke should throw exception when categoryId is invalid`() = runTest {
        // Given
        val type = TransactionType.EXPENSE
        val amount = 100.0
        val categoryId = 0L
        val accountId = 1L
        val note = "测试"
        val date = System.currentTimeMillis()

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            addTransactionUseCase(
                type, amount, categoryId, accountId, note, date
            )
        }
    }

    @Test
    fun `invoke should throw exception when accountId is invalid`() = runTest {
        // Given
        val type = TransactionType.EXPENSE
        val amount = 100.0
        val categoryId = 1L
        val accountId = 0L
        val note = "测试"
        val date = System.currentTimeMillis()

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            addTransactionUseCase(
                type, amount, categoryId, accountId, note, date
            )
        }
    }

    @Test
    fun `invoke should throw exception when repository fails`() = runTest {
        // Given
        val type = TransactionType.EXPENSE
        val amount = 100.0
        val categoryId = 1L
        val accountId = 1L
        val note = "测试"
        val date = System.currentTimeMillis()
        val exception = Exception("数据库错误")
        
        coEvery { 
            transactionRepository.addTransaction(
                type, amount, categoryId, accountId, note, date
            ) 
        } returns BaseResult.Error(exception)

        // When & Then
        val thrownException = assertFailsWith<Exception> {
            addTransactionUseCase(
                type, amount, categoryId, accountId, note, date
            )
        }
        assertThat(thrownException.message).isEqualTo("数据库错误")
    }
}
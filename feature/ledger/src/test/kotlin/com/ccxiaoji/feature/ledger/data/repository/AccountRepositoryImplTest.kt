package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.PaymentType
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.shared.user.api.UserApi
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AccountRepositoryImplTest {

    @MockK
    private lateinit var accountDao: AccountDao

    @MockK
    private lateinit var transactionDao: TransactionDao

    @MockK
    private lateinit var userApi: UserApi

    private lateinit var accountRepository: AccountRepositoryImpl

    private val testUserId = "test-user-123"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { userApi.getCurrentUserId() } returns testUserId
        accountRepository = AccountRepositoryImpl(accountDao, transactionDao, userApi)
    }

    @Test
    fun `getAccounts返回所有账户`() = runTest {
        // Given
        val accountEntities = createTestAccountEntities()
        coEvery { accountDao.getAccountsByUser(testUserId) } returns flowOf(accountEntities)

        // When
        val result = accountRepository.getAccounts().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("现金账户")
        assertThat(result[1].name).isEqualTo("银行卡")
        coVerify(exactly = 1) { accountDao.getAccountsByUser(testUserId) }
    }

    @Test
    fun `getDefaultAccount返回默认账户`() = runTest {
        // Given
        val defaultAccount = createTestAccountEntity("1", "默认账户", isDefault = true)
        coEvery { accountDao.getDefaultAccountByUser(testUserId) } returns defaultAccount

        // When
        val result = accountRepository.getDefaultAccount()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("默认账户")
        assertThat(result?.isDefault).isTrue()
        coVerify(exactly = 1) { accountDao.getDefaultAccountByUser(testUserId) }
    }

    @Test
    fun `createAccount创建普通账户`() = runTest {
        // Given
        val name = "新账户"
        val type = AccountType.CASH
        val initialBalance = 10000L
        
        val accountEntitySlot = slot<AccountEntity>()
        coEvery { accountDao.insertAccount(capture(accountEntitySlot)) } returns 1L

        // When
        val result = accountRepository.createAccount(
            name = name,
            type = type,
            initialBalanceCents = initialBalance
        )

        // Then
        assertThat(result).isEqualTo(1L)
        
        val capturedEntity = accountEntitySlot.captured
        assertThat(capturedEntity.userId).isEqualTo(testUserId)
        assertThat(capturedEntity.name).isEqualTo(name)
        assertThat(capturedEntity.type).isEqualTo(type.name)
        assertThat(capturedEntity.balanceCents).isEqualTo(initialBalance)
        assertThat(capturedEntity.syncStatus).isEqualTo(SyncStatus.PENDING_SYNC)
        coVerify(exactly = 1) { accountDao.insertAccount(any()) }
    }

    @Test
    fun `createAccount创建信用卡账户`() = runTest {
        // Given
        val name = "信用卡"
        val type = AccountType.CREDIT_CARD
        val initialBalance = -50000L
        val creditLimit = 100000L
        val billingDay = 1
        val paymentDueDay = 20
        val gracePeriodDays = 3
        
        val accountEntitySlot = slot<AccountEntity>()
        coEvery { accountDao.insertAccount(capture(accountEntitySlot)) } returns 2L

        // When
        val result = accountRepository.createAccount(
            name = name,
            type = type,
            initialBalanceCents = initialBalance,
            creditLimitCents = creditLimit,
            billingDay = billingDay,
            paymentDueDay = paymentDueDay,
            gracePeriodDays = gracePeriodDays
        )

        // Then
        assertThat(result).isEqualTo(2L)
        
        val capturedEntity = accountEntitySlot.captured
        assertThat(capturedEntity.type).isEqualTo(AccountType.CREDIT_CARD.name)
        assertThat(capturedEntity.creditLimitCents).isEqualTo(creditLimit)
        assertThat(capturedEntity.billingDay).isEqualTo(billingDay)
        assertThat(capturedEntity.paymentDueDay).isEqualTo(paymentDueDay)
        assertThat(capturedEntity.gracePeriodDays).isEqualTo(gracePeriodDays)
        coVerify(exactly = 1) { accountDao.insertAccount(any()) }
    }

    @Test
    fun `updateBalance更新账户余额`() = runTest {
        // Given
        val accountId = "account-123"
        val changeAmount = 5000L
        coEvery { accountDao.updateBalance(accountId, changeAmount) } returns Unit

        // When
        accountRepository.updateBalance(accountId, changeAmount)

        // Then
        coVerify(exactly = 1) { accountDao.updateBalance(accountId, changeAmount) }
    }

    @Test
    fun `setDefaultAccount设置默认账户`() = runTest {
        // Given
        val accountId = "account-123"
        coEvery { accountDao.clearDefaultAccount(testUserId) } returns Unit
        coEvery { accountDao.setDefaultAccount(accountId) } returns Unit

        // When
        accountRepository.setDefaultAccount(accountId)

        // Then
        coVerify(exactly = 1) { accountDao.clearDefaultAccount(testUserId) }
        coVerify(exactly = 1) { accountDao.setDefaultAccount(accountId) }
    }

    @Test
    fun `deleteAccount删除账户`() = runTest {
        // Given
        val accountId = "account-123"
        coEvery { accountDao.softDeleteAccount(accountId, any()) } returns Unit

        // When
        accountRepository.deleteAccount(accountId)

        // Then
        coVerify(exactly = 1) { accountDao.softDeleteAccount(accountId, any()) }
    }

    @Test
    fun `getAccountById返回指定账户`() = runTest {
        // Given
        val accountId = "account-123"
        val accountEntity = createTestAccountEntity(accountId, "测试账户")
        coEvery { accountDao.getAccountById(accountId) } returns accountEntity

        // When
        val result = accountRepository.getAccountById(accountId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(accountId)
        assertThat(result?.name).isEqualTo("测试账户")
        coVerify(exactly = 1) { accountDao.getAccountById(accountId) }
    }

    @Test
    fun `getTotalBalance计算总余额`() = runTest {
        // Given
        val totalBalanceCents = 150000L
        coEvery { accountDao.getTotalBalanceByUser(testUserId) } returns totalBalanceCents

        // When
        val result = accountRepository.getTotalBalance()

        // Then
        assertThat(result).isEqualTo(1500.0) // 分转换为元
        coVerify(exactly = 1) { accountDao.getTotalBalanceByUser(testUserId) }
    }

    @Test
    fun `transferBetweenAccounts执行账户间转账`() = runTest {
        // Given
        val fromAccountId = "account-1"
        val toAccountId = "account-2"
        val amountCents = 10000L
        val note = "转账测试"
        
        coEvery { accountDao.updateBalance(any(), any()) } returns Unit
        coEvery { transactionDao.insertTransaction(any()) } returns Unit

        // When
        accountRepository.transferBetweenAccounts(fromAccountId, toAccountId, amountCents, note)

        // Then
        // 验证扣款
        coVerify(exactly = 1) { accountDao.updateBalance(fromAccountId, -amountCents) }
        // 验证收款
        coVerify(exactly = 1) { accountDao.updateBalance(toAccountId, amountCents) }
        // 验证记录两笔交易
        coVerify(exactly = 2) { transactionDao.insertTransaction(any()) }
    }

    @Test
    fun `getCreditCardsWithPaymentDueDay返回指定还款日的信用卡`() = runTest {
        // Given
        val dayOfMonth = 15
        val creditCards = listOf(
            createTestAccountEntity("1", "信用卡1", type = AccountType.CREDIT_CARD, paymentDueDay = dayOfMonth),
            createTestAccountEntity("2", "信用卡2", type = AccountType.CREDIT_CARD, paymentDueDay = dayOfMonth)
        )
        coEvery { accountDao.getCreditCardsByPaymentDueDay(testUserId, dayOfMonth) } returns creditCards

        // When
        val result = accountRepository.getCreditCardsWithPaymentDueDay(dayOfMonth)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.all { it.type == AccountType.CREDIT_CARD }).isTrue()
        assertThat(result.all { it.paymentDueDay == dayOfMonth }).isTrue()
        coVerify(exactly = 1) { accountDao.getCreditCardsByPaymentDueDay(testUserId, dayOfMonth) }
    }

    @Test
    fun `getCreditCardsWithDebt返回有欠款的信用卡`() = runTest {
        // Given
        val creditCardsWithDebt = listOf(
            createTestAccountEntity("1", "信用卡1", type = AccountType.CREDIT_CARD, balance = -50000L),
            createTestAccountEntity("2", "信用卡2", type = AccountType.CREDIT_CARD, balance = -30000L)
        )
        coEvery { accountDao.getCreditCardsWithDebt(testUserId) } returns creditCardsWithDebt

        // When
        val result = accountRepository.getCreditCardsWithDebt()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.all { it.balanceCents < 0 }).isTrue()
        coVerify(exactly = 1) { accountDao.getCreditCardsWithDebt(testUserId) }
    }

    private fun createTestAccountEntities(): List<AccountEntity> {
        return listOf(
            createTestAccountEntity("1", "现金账户"),
            createTestAccountEntity("2", "银行卡")
        )
    }

    private fun createTestAccountEntity(
        id: String,
        name: String,
        type: AccountType = AccountType.CASH,
        balance: Long = 10000L,
        isDefault: Boolean = false,
        paymentDueDay: Int? = null
    ): AccountEntity {
        val now = System.currentTimeMillis()
        return AccountEntity(
            id = id,
            userId = testUserId,
            name = name,
            type = type.name,
            balanceCents = balance,
            isDefault = isDefault,
            creditLimitCents = if (type == AccountType.CREDIT_CARD) 100000L else null,
            billingDay = if (type == AccountType.CREDIT_CARD) 1 else null,
            paymentDueDay = paymentDueDay ?: if (type == AccountType.CREDIT_CARD) 20 else null,
            gracePeriodDays = if (type == AccountType.CREDIT_CARD) 3 else null,
            minPaymentPercentage = if (type == AccountType.CREDIT_CARD) 10 else null,
            color = "#3A7AFE",
            icon = "💰",
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED,
            isDeleted = false
        )
    }
}
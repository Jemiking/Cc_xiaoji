package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class GetAccountsUseCaseTest {
    
    private lateinit var accountRepository: AccountRepository
    private lateinit var useCase: GetAccountsUseCase
    
    @Before
    fun setup() {
        accountRepository = mockk()
        useCase = GetAccountsUseCase(accountRepository)
    }
    
    @Test
    fun `获取所有账户成功`() = runTest {
        // Given
        val accounts = listOf(
            Account(
                id = 1,
                name = "现金账户",
                type = AccountType.CASH,
                balance = BigDecimal("1000.00"),
                icon = "cash",
                color = "#FF0000",
                isDefault = true,
                userId = 1
            ),
            Account(
                id = 2,
                name = "银行卡",
                type = AccountType.BANK_CARD,
                balance = BigDecimal("5000.00"),
                icon = "card",
                color = "#00FF00",
                isDefault = false,
                userId = 1
            )
        )
        coEvery { accountRepository.getAllAccounts() } returns flowOf(accounts)
        
        // When
        val result = useCase().first()
        
        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("现金账户")
        assertThat(result[0].balance).isEqualTo(BigDecimal("1000.00"))
        assertThat(result[1].name).isEqualTo("银行卡")
        assertThat(result[1].balance).isEqualTo(BigDecimal("5000.00"))
        coVerify(exactly = 1) { accountRepository.getAllAccounts() }
    }
    
    @Test
    fun `获取所有账户 - 空列表`() = runTest {
        // Given
        val emptyList = emptyList<Account>()
        coEvery { accountRepository.getAllAccounts() } returns flowOf(emptyList)
        
        // When
        val result = useCase().first()
        
        // Then
        assertThat(result).isEmpty()
        coVerify(exactly = 1) { accountRepository.getAllAccounts() }
    }
    
    @Test
    fun `获取所有账户 - 包含不同类型账户`() = runTest {
        // Given
        val accounts = listOf(
            Account(
                id = 1,
                name = "现金",
                type = AccountType.CASH,
                balance = BigDecimal("100.00"),
                icon = "cash",
                color = "#FF0000",
                isDefault = true,
                userId = 1
            ),
            Account(
                id = 2,
                name = "支付宝",
                type = AccountType.ALIPAY,
                balance = BigDecimal("2000.00"),
                icon = "alipay",
                color = "#1677FF",
                isDefault = false,
                userId = 1
            ),
            Account(
                id = 3,
                name = "微信钱包",
                type = AccountType.WECHAT,
                balance = BigDecimal("500.00"),
                icon = "wechat",
                color = "#07C160",
                isDefault = false,
                userId = 1
            ),
            Account(
                id = 4,
                name = "信用卡",
                type = AccountType.CREDIT_CARD,
                balance = BigDecimal("-1500.00"),
                icon = "credit_card",
                color = "#FF6B00",
                isDefault = false,
                userId = 1
            )
        )
        coEvery { accountRepository.getAllAccounts() } returns flowOf(accounts)
        
        // When
        val result = useCase().first()
        
        // Then
        assertThat(result).hasSize(4)
        val accountTypes = result.map { it.type }
        assertThat(accountTypes).containsExactly(
            AccountType.CASH,
            AccountType.ALIPAY,
            AccountType.WECHAT,
            AccountType.CREDIT_CARD
        )
        coVerify(exactly = 1) { accountRepository.getAllAccounts() }
    }
    
    @Test
    fun `获取所有账户 - 验证默认账户`() = runTest {
        // Given
        val accounts = listOf(
            Account(
                id = 1,
                name = "账户1",
                type = AccountType.BANK_CARD,
                balance = BigDecimal.ZERO,
                icon = "card",
                color = "#000000",
                isDefault = false,
                userId = 1
            ),
            Account(
                id = 2,
                name = "默认账户",
                type = AccountType.CASH,
                balance = BigDecimal.ZERO,
                icon = "cash",
                color = "#000000",
                isDefault = true,
                userId = 1
            )
        )
        coEvery { accountRepository.getAllAccounts() } returns flowOf(accounts)
        
        // When
        val result = useCase().first()
        
        // Then
        val defaultAccounts = result.filter { it.isDefault }
        assertThat(defaultAccounts).hasSize(1)
        assertThat(defaultAccounts[0].name).isEqualTo("默认账户")
        coVerify(exactly = 1) { accountRepository.getAllAccounts() }
    }
    
    @Test
    fun `获取所有账户 - 验证余额计算`() = runTest {
        // Given
        val accounts = listOf(
            Account(
                id = 1,
                name = "账户1",
                type = AccountType.CASH,
                balance = BigDecimal("1000.50"),
                icon = "cash",
                color = "#FF0000",
                isDefault = true,
                userId = 1
            ),
            Account(
                id = 2,
                name = "账户2",
                type = AccountType.BANK_CARD,
                balance = BigDecimal("2500.25"),
                icon = "card",
                color = "#00FF00",
                isDefault = false,
                userId = 1
            ),
            Account(
                id = 3,
                name = "信用卡",
                type = AccountType.CREDIT_CARD,
                balance = BigDecimal("-500.00"),
                icon = "credit",
                color = "#0000FF",
                isDefault = false,
                userId = 1
            )
        )
        coEvery { accountRepository.getAllAccounts() } returns flowOf(accounts)
        
        // When
        val result = useCase().first()
        
        // Then
        val totalBalance = result.sumOf { it.balance }
        assertThat(totalBalance).isEqualTo(BigDecimal("3000.75"))
        
        val positiveBalance = result.filter { it.balance > BigDecimal.ZERO }.sumOf { it.balance }
        assertThat(positiveBalance).isEqualTo(BigDecimal("3500.75"))
        
        val negativeBalance = result.filter { it.balance < BigDecimal.ZERO }.sumOf { it.balance }
        assertThat(negativeBalance).isEqualTo(BigDecimal("-500.00"))
        
        coVerify(exactly = 1) { accountRepository.getAllAccounts() }
    }
}
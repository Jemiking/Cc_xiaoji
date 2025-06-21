package com.ccxiaoji.feature.ledger.presentation.viewmodel

import com.ccxiaoji.common.test.util.MainDispatcherRule
import com.ccxiaoji.feature.ledger.data.repository.AccountRepository
import com.ccxiaoji.feature.ledger.data.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.data.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.data.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.shared.user.api.UserApi
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

@ExperimentalCoroutinesApi
class LedgerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mock依赖
    private val mockTransactionRepository = mockk<TransactionRepository>()
    private val mockAccountRepository = mockk<AccountRepository>()
    private val mockCategoryRepository = mockk<CategoryRepository>()
    private val mockBudgetRepository = mockk<BudgetRepository>()
    private val mockUserApi = mockk<UserApi>()
    
    // 被测试的ViewModel
    private lateinit var viewModel: LedgerViewModel

    // 测试数据
    private val testAccounts = listOf(
        Account(
            id = "1",
            name = "现金账户",
            type = "CASH",
            balance = 100000, // 1000元
            isDefault = true,
            color = "#FF5722",
            icon = "💵",
            syncStatus = "synced"
        ),
        Account(
            id = "2",
            name = "银行卡",
            type = "BANK",
            balance = 500000, // 5000元
            isDefault = false,
            color = "#2196F3",
            icon = "💳",
            syncStatus = "synced"
        )
    )

    private val testCategories = listOf(
        Category(
            id = "1",
            name = "工资",
            type = "INCOME",
            icon = "💰",
            color = "#4CAF50",
            parentId = null,
            syncStatus = "synced"
        ),
        Category(
            id = "2",
            name = "餐饮",
            type = "EXPENSE",
            icon = "🍔",
            color = "#FF9800",
            parentId = null,
            syncStatus = "synced"
        ),
        Category(
            id = "3",
            name = "交通",
            type = "EXPENSE",
            icon = "🚗",
            color = "#9C27B0",
            parentId = null,
            syncStatus = "synced"
        )
    )

    private val now = Clock.System.now()
    private val testTransactions = listOf(
        Transaction(
            id = "1",
            amountCents = 500000, // 5000元收入
            categoryId = "1",
            categoryDetails = testCategories[0],
            accountId = "1",
            accountDetails = testAccounts[0],
            note = "本月工资",
            createdAt = now,
            syncStatus = "synced"
        ),
        Transaction(
            id = "2",
            amountCents = 5000, // 50元支出
            categoryId = "2",
            categoryDetails = testCategories[1],
            accountId = "1",
            accountDetails = testAccounts[0],
            note = "午餐",
            createdAt = now.minus(1, DateTimeUnit.DAY),
            syncStatus = "synced"
        ),
        Transaction(
            id = "3",
            amountCents = 10000, // 100元支出
            categoryId = "3",
            categoryDetails = testCategories[2],
            accountId = "2",
            accountDetails = testAccounts[1],
            note = "打车费",
            createdAt = now.minus(2, DateTimeUnit.DAY),
            syncStatus = "synced"
        )
    )

    @Before
    fun setup() {
        // 默认mock设置
        every { mockTransactionRepository.getTransactions() } returns flowOf(testTransactions)
        every { mockTransactionRepository.getTransactionsByAccount(any()) } returns flowOf(testTransactions.filter { it.accountId == "1" })
        every { mockAccountRepository.getAccounts() } returns flowOf(testAccounts)
        every { mockCategoryRepository.getCategories() } returns flowOf(testCategories)
        coEvery { mockTransactionRepository.getMonthlyIncomesAndExpenses(any(), any()) } returns Pair(500000L, 15000L)
        
        // 初始化ViewModel
        viewModel = LedgerViewModel(
            transactionRepository = mockTransactionRepository,
            accountRepository = mockAccountRepository,
            categoryRepository = mockCategoryRepository,
            budgetRepository = mockBudgetRepository,
            userApi = mockUserApi
        )
    }

    @Test
    fun `初始化时应该加载所有数据`() = runTest {
        // When - ViewModel在init中自动加载数据
        
        // Then
        assertThat(viewModel.uiState.value.accounts).hasSize(2)
        assertThat(viewModel.uiState.value.categories).hasSize(3)
        assertThat(viewModel.uiState.value.transactions).hasSize(3)
        assertThat(viewModel.uiState.value.monthlyIncome).isEqualTo(5000.0)
        assertThat(viewModel.uiState.value.monthlyExpense).isEqualTo(150.0)
        assertThat(viewModel.uiState.value.selectedAccount?.isDefault).isTrue()
    }

    @Test
    fun `切换月份应该重新加载数据`() = runTest {
        // Given
        val lastMonth = YearMonth.now().minusMonths(1)
        coEvery { mockTransactionRepository.getMonthlyIncomesAndExpenses(lastMonth.year, lastMonth.monthValue) } returns Pair(300000L, 80000L)
        
        // When
        viewModel.selectMonth(lastMonth)
        
        // Then
        coVerify(exactly = 1) { 
            mockTransactionRepository.getMonthlyIncomesAndExpenses(lastMonth.year, lastMonth.monthValue)
        }
        // 注意：由于mock的交易数据日期是固定的，这里只验证调用
    }

    @Test
    fun `添加交易应该调用repository并检查预算`() = runTest {
        // Given
        coEvery { mockTransactionRepository.addTransaction(any(), any(), any(), any()) } just Runs
        coEvery { mockBudgetRepository.checkBudgetAlert(any(), any(), any()) } returns false
        coEvery { mockBudgetRepository.checkBudgetExceeded(any(), any(), any()) } returns false
        
        // When
        viewModel.addTransaction(
            amountCents = 10000,
            categoryId = "2",
            note = "晚餐",
            accountId = "1"
        )
        
        // Then
        coVerify(exactly = 1) { 
            mockTransactionRepository.addTransaction(
                amountCents = 10000,
                categoryId = "2",
                note = "晚餐",
                accountId = "1"
            )
        }
        coVerify { mockBudgetRepository.checkBudgetAlert(any(), any(), "2") }
        coVerify { mockBudgetRepository.checkBudgetExceeded(any(), any(), "2") }
    }

    @Test
    fun `预算超支时应该显示提醒`() = runTest {
        // Given
        coEvery { mockTransactionRepository.addTransaction(any(), any(), any(), any()) } just Runs
        coEvery { mockBudgetRepository.checkBudgetAlert(any(), any(), any()) } returns false
        coEvery { mockBudgetRepository.checkBudgetExceeded(any(), any(), "2") } returns true
        coEvery { mockBudgetRepository.checkBudgetExceeded(any(), any(), null) } returns false
        
        // When
        viewModel.addTransaction(
            amountCents = 50000,
            categoryId = "2",
            note = "大餐",
            accountId = "1"
        )
        
        // Then
        assertThat(viewModel.uiState.value.budgetAlert).isNotNull()
        assertThat(viewModel.uiState.value.budgetAlert?.message).contains("分类预算已超支")
        assertThat(viewModel.uiState.value.budgetAlert?.isExceeded).isTrue()
    }

    @Test
    fun `删除交易应该调用repository并刷新汇总`() = runTest {
        // Given
        coEvery { mockTransactionRepository.deleteTransaction(any()) } just Runs
        
        // When
        viewModel.deleteTransaction("1")
        
        // Then
        coVerify(exactly = 1) { 
            mockTransactionRepository.deleteTransaction("1")
        }
        coVerify(atLeast = 2) { // 初始化一次 + 删除后一次
            mockTransactionRepository.getMonthlyIncomesAndExpenses(any(), any())
        }
    }

    @Test
    fun `批量选择模式应该正确切换`() = runTest {
        // When - 开启选择模式
        viewModel.toggleSelectionMode()
        
        // Then
        assertThat(viewModel.uiState.value.isSelectionMode).isTrue()
        
        // When - 选择交易
        viewModel.toggleTransactionSelection("1")
        viewModel.toggleTransactionSelection("2")
        
        // Then
        assertThat(viewModel.uiState.value.selectedTransactionIds).containsExactly("1", "2")
        
        // When - 取消选择
        viewModel.toggleTransactionSelection("1")
        
        // Then
        assertThat(viewModel.uiState.value.selectedTransactionIds).containsExactly("2")
        
        // When - 关闭选择模式
        viewModel.toggleSelectionMode()
        
        // Then
        assertThat(viewModel.uiState.value.isSelectionMode).isFalse()
        assertThat(viewModel.uiState.value.selectedTransactionIds).isEmpty()
    }

    @Test
    fun `搜索功能应该正确过滤交易`() = runTest {
        // Given
        val searchResults = listOf(testTransactions[0]) // 只有工资交易
        every { mockTransactionRepository.searchTransactions("工资") } returns flowOf(searchResults)
        
        // When - 开启搜索模式
        viewModel.toggleSearchMode()
        
        // Then
        assertThat(viewModel.uiState.value.isSearchMode).isTrue()
        
        // When - 搜索
        viewModel.updateSearchQuery("工资")
        
        // Then
        verify { mockTransactionRepository.searchTransactions("工资") }
        assertThat(viewModel.uiState.value.filteredTransactions).hasSize(1)
        assertThat(viewModel.uiState.value.filteredTransactions.first().note).contains("工资")
    }

    @Test
    fun `按账户过滤应该只显示该账户交易`() = runTest {
        // When
        viewModel.filterByAccount("2")
        
        // Then
        assertThat(viewModel.uiState.value.activeFilter.accountId).isEqualTo("2")
        // 验证过滤后的交易
        coVerify { mockTransactionRepository.getTransactions() }
    }

    @Test
    fun `设置分组模式应该正确分组交易`() = runTest {
        // When - 按天分组
        viewModel.setGroupingMode(GroupingMode.DAY)
        
        // Then
        assertThat(viewModel.uiState.value.groupingMode).isEqualTo(GroupingMode.DAY)
        assertThat(viewModel.groupedTransactions.value).isNotEmpty()
        
        // When - 按月分组
        viewModel.setGroupingMode(GroupingMode.MONTH)
        
        // Then
        assertThat(viewModel.uiState.value.groupingMode).isEqualTo(GroupingMode.MONTH)
        assertThat(viewModel.groupedTransactions.value).hasSize(1) // 所有交易都在当月
    }

    @Test
    fun `复制交易应该创建新交易`() = runTest {
        // Given
        coEvery { mockTransactionRepository.addTransaction(any(), any(), any(), any()) } just Runs
        
        // When
        viewModel.copyTransaction(testTransactions[0])
        
        // Then
        coVerify(exactly = 1) { 
            mockTransactionRepository.addTransaction(
                amountCents = 500000,
                categoryId = "1",
                note = "本月工资",
                accountId = "1"
            )
        }
    }

    @Test
    fun `更新过滤器应该应用所有过滤条件`() = runTest {
        // Given
        val filter = TransactionFilter(
            transactionType = TransactionType.EXPENSE,
            categoryIds = setOf("2"),
            minAmount = 10.0,
            maxAmount = 100.0
        )
        
        // When
        viewModel.updateFilter(filter)
        
        // Then
        assertThat(viewModel.uiState.value.activeFilter).isEqualTo(filter)
        // 验证过滤逻辑被调用
        coVerify { mockTransactionRepository.getTransactions() }
    }

    // 帮助属性
    private val Transaction.amountYuan: Double
        get() = amountCents / 100.0
}
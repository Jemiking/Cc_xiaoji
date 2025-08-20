package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.data.local.dao.LedgerDao
import com.ccxiaoji.feature.ledger.data.local.entity.LedgerEntity
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * LedgerRepositoryImpl单元测试
 * 重点测试默认记账簿创建逻辑
 */
class LedgerRepositoryImplTest {

    private lateinit var ledgerDao: LedgerDao
    private lateinit var repository: LedgerRepositoryImpl
    
    private val testUserId = "test_user_123"

    @Before
    fun setup() {
        ledgerDao = mockk()
        repository = LedgerRepositoryImpl(ledgerDao)
    }

    @Test
    fun `ensureDefaultLedger - 用户已有默认记账簿时返回现有记账簿`() = runTest {
        // Given: 用户已有默认记账簿
        val existingLedger = createTestLedgerEntity(
            id = "existing_ledger_id",
            name = "总记账簿",
            isDefault = true
        )
        coEvery { ledgerDao.hasDefaultLedger(testUserId) } returns true
        coEvery { ledgerDao.getDefaultLedger(testUserId) } returns existingLedger

        // When: 调用ensureDefaultLedger
        val result = repository.ensureDefaultLedger(testUserId)

        // Then: 返回现有默认记账簿
        assertTrue("应该返回成功结果", result is BaseResult.Success)
        val ledger = (result as BaseResult.Success).data
        assertEquals("应该返回现有记账簿", "existing_ledger_id", ledger.id)
        assertEquals("应该是默认记账簿", true, ledger.isDefault)
        
        // 验证没有尝试创建新记账簿
        coVerify(exactly = 1) { ledgerDao.hasDefaultLedger(testUserId) }
        coVerify(exactly = 1) { ledgerDao.getDefaultLedger(testUserId) }
        coVerify(exactly = 0) { ledgerDao.insertLedger(any()) }
    }

    @Test
    fun `ensureDefaultLedger - 用户无默认记账簿时创建新默认记账簿`() = runTest {
        // Given: 用户没有默认记账簿
        val insertedLedgerSlot = slot<LedgerEntity>()
        coEvery { ledgerDao.hasDefaultLedger(testUserId) } returns false
        coEvery { ledgerDao.clearDefaultLedgers(testUserId, any()) } returns Unit
        coEvery { ledgerDao.getNextDisplayOrder(testUserId) } returns 1
        coEvery { ledgerDao.insertLedger(capture(insertedLedgerSlot)) } returns 1L

        // When: 调用ensureDefaultLedger
        val result = repository.ensureDefaultLedger(testUserId)

        // Then: 创建并返回新的默认记账簿
        assertTrue("应该返回成功结果", result is BaseResult.Success)
        val ledger = (result as BaseResult.Success).data
        assertEquals("用户ID应该正确", testUserId, ledger.userId)
        assertEquals("名称应该是总记账簿", Ledger.DEFAULT_LEDGER_NAME, ledger.name)
        assertEquals("应该是默认记账簿", true, ledger.isDefault)
        assertEquals("应该是激活状态", true, ledger.isActive)
        assertEquals("颜色应该正确", "#3A7AFE", ledger.color)
        assertEquals("图标应该正确", "book", ledger.icon)
        
        // 验证插入的数据正确
        val insertedLedger = insertedLedgerSlot.captured
        assertEquals("插入的用户ID应该正确", testUserId, insertedLedger.userId)
        assertEquals("插入的名称应该正确", Ledger.DEFAULT_LEDGER_NAME, insertedLedger.name)
        assertEquals("插入的描述应该正确", Ledger.DEFAULT_LEDGER_DESCRIPTION, insertedLedger.description)
        assertTrue("插入的应该是默认记账簿", insertedLedger.isDefault)
        
        // 验证调用序列
        coVerify(exactly = 1) { ledgerDao.hasDefaultLedger(testUserId) }
        coVerify(exactly = 1) { ledgerDao.clearDefaultLedgers(testUserId, any()) }
        coVerify(exactly = 1) { ledgerDao.getNextDisplayOrder(testUserId) }
        coVerify(exactly = 1) { ledgerDao.insertLedger(any()) }
    }

    @Test
    fun `ensureDefaultLedger - 多次调用具有幂等性`() = runTest {
        // Given: 设置为第一次调用时创建记账簿，第二次调用时返回已存在的记账簿
        val createdLedger = createTestLedgerEntity(
            id = "created_ledger_id",
            name = Ledger.DEFAULT_LEDGER_NAME,
            isDefault = true
        )
        
        // 第一次调用时没有默认记账簿
        coEvery { ledgerDao.hasDefaultLedger(testUserId) } returnsMany listOf(false, true)
        coEvery { ledgerDao.clearDefaultLedgers(testUserId, any()) } returns Unit
        coEvery { ledgerDao.getNextDisplayOrder(testUserId) } returns 1
        coEvery { ledgerDao.insertLedger(any()) } returns 1L
        
        // 第二次调用时返回已创建的记账簿
        coEvery { ledgerDao.getDefaultLedger(testUserId) } returns createdLedger

        // When: 连续调用两次ensureDefaultLedger
        val result1 = repository.ensureDefaultLedger(testUserId)
        val result2 = repository.ensureDefaultLedger(testUserId)

        // Then: 两次调用都成功，返回相同的记账簿
        assertTrue("第一次调用应该成功", result1 is BaseResult.Success)
        assertTrue("第二次调用应该成功", result2 is BaseResult.Success)
        
        val ledger1 = (result1 as BaseResult.Success).data
        val ledger2 = (result2 as BaseResult.Success).data
        assertEquals("两次调用应该返回相同ID的记账簿", ledger1.id, ledger2.id)
        
        // 验证第一次调用创建了记账簿，第二次调用返回现有记账簿
        coVerify(exactly = 1) { ledgerDao.insertLedger(any()) }
        coVerify(exactly = 1) { ledgerDao.getDefaultLedger(testUserId) }
    }

    @Test
    fun `ensureDefaultLedger - 数据库错误时返回失败结果`() = runTest {
        // Given: 数据库操作抛出异常
        val expectedException = RuntimeException("数据库连接失败")
        coEvery { ledgerDao.hasDefaultLedger(testUserId) } throws expectedException

        // When: 调用ensureDefaultLedger
        val result = repository.ensureDefaultLedger(testUserId)

        // Then: 返回错误结果
        assertTrue("应该返回错误结果", result is BaseResult.Error)
        val error = (result as BaseResult.Error).exception
        assertTrue("错误消息应该包含原因", error.message?.contains("确保默认记账簿失败") == true)
    }

    @Test
    fun `createLedger - 设置为默认时清除其他默认标记`() = runTest {
        // Given: 创建默认记账簿
        coEvery { ledgerDao.clearDefaultLedgers(testUserId, any()) } returns Unit
        coEvery { ledgerDao.getNextDisplayOrder(testUserId) } returns 2
        coEvery { ledgerDao.insertLedger(any()) } returns 1L

        // When: 创建设置为默认的记账簿
        val result = repository.createLedger(
            userId = testUserId,
            name = "新记账簿",
            description = "测试记账簿",
            color = "#FF5722",
            icon = "home",
            isDefault = true
        )

        // Then: 成功创建并清除了其他默认标记
        assertTrue("应该返回成功结果", result is BaseResult.Success)
        coVerify(exactly = 1) { ledgerDao.clearDefaultLedgers(testUserId, any()) }
        coVerify(exactly = 1) { ledgerDao.insertLedger(any()) }
    }

    @Test
    fun `createLedger - 不设置为默认时不清除其他默认标记`() = runTest {
        // Given: 创建非默认记账簿
        coEvery { ledgerDao.getNextDisplayOrder(testUserId) } returns 2
        coEvery { ledgerDao.insertLedger(any()) } returns 1L

        // When: 创建不设置为默认的记账簿
        val result = repository.createLedger(
            userId = testUserId,
            name = "新记账簿",
            description = "测试记账簿",
            color = "#FF5722",
            icon = "home",
            isDefault = false
        )

        // Then: 成功创建但不清除其他默认标记
        assertTrue("应该返回成功结果", result is BaseResult.Success)
        coVerify(exactly = 0) { ledgerDao.clearDefaultLedgers(any(), any()) }
        coVerify(exactly = 1) { ledgerDao.insertLedger(any()) }
    }

    /**
     * 创建测试用的LedgerEntity
     */
    private fun createTestLedgerEntity(
        id: String = UUID.randomUUID().toString(),
        userId: String = testUserId,
        name: String = "测试记账簿",
        description: String? = "测试描述",
        color: String = "#3A7AFE",
        icon: String = "book",
        isDefault: Boolean = false,
        displayOrder: Int = 0,
        isActive: Boolean = true
    ): LedgerEntity {
        val now = Clock.System.now()
        return LedgerEntity(
            id = id,
            userId = userId,
            name = name,
            description = description,
            color = color,
            icon = icon,
            isDefault = isDefault,
            displayOrder = displayOrder,
            isActive = isActive,
            createdAt = now,
            updatedAt = now
        )
    }
}
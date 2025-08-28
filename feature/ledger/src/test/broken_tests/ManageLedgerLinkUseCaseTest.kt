package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.repository.LedgerLinkRepository
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * ManageLedgerLinkUseCase单元测试
 * 测试记账簿联动关系管理的各种场景
 */
class ManageLedgerLinkUseCaseTest {

    private lateinit var ledgerLinkRepository: LedgerLinkRepository
    private lateinit var ledgerRepository: LedgerRepository
    private lateinit var useCase: ManageLedgerLinkUseCase
    
    private val testUserId = "user_001"
    private val parentLedgerId = "parent_ledger_001"
    private val childLedgerId = "child_ledger_001"
    private val linkId = "link_001"
    private val testInstant = Clock.System.now()

    @Before
    fun setup() {
        ledgerLinkRepository = mockk()
        ledgerRepository = mockk()
        
        useCase = ManageLedgerLinkUseCase(
            ledgerLinkRepository,
            ledgerRepository
        )
    }

    @Test
    fun `createLedgerLink - 成功创建联动关系`() = runTest {
        // Given: 正常的父子记账簿
        val parentLedger = createTestLedger(parentLedgerId, "总记账簿", testUserId)
        val childLedger = createTestLedger(childLedgerId, "子记账簿", testUserId)
        val expectedLink = createTestLedgerLink(linkId, parentLedgerId, childLedgerId)
        
        coEvery { ledgerRepository.getLedgerById(parentLedgerId) } returns BaseResult.Success(parentLedger)
        coEvery { ledgerRepository.getLedgerById(childLedgerId) } returns BaseResult.Success(childLedger)
        coEvery { ledgerLinkRepository.hasActiveLinkBetween(parentLedgerId, childLedgerId) } returns 
            BaseResult.Success(false)
        every { ledgerLinkRepository.getChildLinks(childLedgerId) } returns flowOf(emptyList())
        coEvery { ledgerLinkRepository.createLink(parentLedgerId, childLedgerId, SyncMode.BIDIRECTIONAL, true) } returns 
            BaseResult.Success(expectedLink)

        // When: 创建联动关系
        val result = useCase.createLedgerLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId,
            syncMode = SyncMode.BIDIRECTIONAL,
            autoSyncEnabled = true
        )

        // Then: 创建成功
        assertTrue("结果应该是Success", result is BaseResult.Success)
        val link = (result as BaseResult.Success).data
        assertEquals("父记账簿ID正确", parentLedgerId, link.parentLedgerId)
        assertEquals("子记账簿ID正确", childLedgerId, link.childLedgerId)
        assertEquals("同步模式正确", SyncMode.BIDIRECTIONAL, link.syncMode)
        assertTrue("自动同步启用", link.autoSyncEnabled)
        
        // 验证依赖调用
        coVerify(exactly = 1) { ledgerRepository.getLedgerById(parentLedgerId) }
        coVerify(exactly = 1) { ledgerRepository.getLedgerById(childLedgerId) }
        coVerify(exactly = 1) { ledgerLinkRepository.hasActiveLinkBetween(parentLedgerId, childLedgerId) }
        coVerify(exactly = 1) { ledgerLinkRepository.createLink(parentLedgerId, childLedgerId, SyncMode.BIDIRECTIONAL, true) }
    }

    @Test
    fun `createLedgerLink - 参数验证失败时返回错误`() = runTest {
        // When & Then: 记账簿ID相同
        val result1 = useCase.createLedgerLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = parentLedgerId
        )
        assertTrue("相同记账簿应返回错误", result1 is BaseResult.Error)
        assertTrue("错误消息包含自己", (result1 as BaseResult.Error).exception.message!!.contains("不能将记账簿与自己建立联动关系"))

        // When & Then: 父记账簿ID为空
        val result2 = useCase.createLedgerLink(
            parentLedgerId = "",
            childLedgerId = childLedgerId
        )
        assertTrue("空ID应返回错误", result2 is BaseResult.Error)
        assertTrue("错误消息包含ID不能为空", (result2 as BaseResult.Error).exception.message!!.contains("记账簿ID不能为空"))

        // When & Then: 子记账簿ID为空
        val result3 = useCase.createLedgerLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = ""
        )
        assertTrue("空ID应返回错误", result3 is BaseResult.Error)
        assertTrue("错误消息包含ID不能为空", (result3 as BaseResult.Error).exception.message!!.contains("记账簿ID不能为空"))
    }

    @Test
    fun `createLedgerLink - 父记账簿不存在时返回错误`() = runTest {
        // Given: 父记账簿不存在
        coEvery { ledgerRepository.getLedgerById(parentLedgerId) } returns 
            BaseResult.Error(Exception("Record not found"))

        // When: 创建联动关系
        val result = useCase.createLedgerLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId
        )

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含父记账簿不存在", (result as BaseResult.Error).exception.message!!.contains("父记账簿不存在"))
    }

    @Test
    fun `createLedgerLink - 子记账簿不存在时返回错误`() = runTest {
        // Given: 父记账簿存在，子记账簿不存在
        val parentLedger = createTestLedger(parentLedgerId, "总记账簿", testUserId)
        
        coEvery { ledgerRepository.getLedgerById(parentLedgerId) } returns BaseResult.Success(parentLedger)
        coEvery { ledgerRepository.getLedgerById(childLedgerId) } returns 
            BaseResult.Error(Exception("Record not found"))

        // When: 创建联动关系
        val result = useCase.createLedgerLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId
        )

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含子记账簿不存在", (result as BaseResult.Error).exception.message!!.contains("子记账簿不存在"))
    }

    @Test
    fun `createLedgerLink - 记账簿未激活时返回错误`() = runTest {
        // Given: 父记账簿未激活
        val inactiveParentLedger = createTestLedger(parentLedgerId, "总记账簿", testUserId, isActive = false)
        
        coEvery { ledgerRepository.getLedgerById(parentLedgerId) } returns BaseResult.Success(inactiveParentLedger)

        // When: 创建联动关系
        val result = useCase.createLedgerLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId
        )

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含记账簿未激活", (result as BaseResult.Error).exception.message!!.contains("父记账簿未激活"))
    }

    @Test
    fun `createLedgerLink - 不同用户记账簿时返回错误`() = runTest {
        // Given: 不同用户的记账簿
        val parentLedger = createTestLedger(parentLedgerId, "总记账簿", "user_001")
        val childLedger = createTestLedger(childLedgerId, "子记账簿", "user_002")
        
        coEvery { ledgerRepository.getLedgerById(parentLedgerId) } returns BaseResult.Success(parentLedger)
        coEvery { ledgerRepository.getLedgerById(childLedgerId) } returns BaseResult.Success(childLedger)

        // When: 创建联动关系
        val result = useCase.createLedgerLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId
        )

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含不同用户", (result as BaseResult.Error).exception.message!!.contains("不能在不同用户的记账簿间建立联动关系"))
    }

    @Test
    fun `createLedgerLink - 已存在联动关系时返回错误`() = runTest {
        // Given: 已存在联动关系
        val parentLedger = createTestLedger(parentLedgerId, "总记账簿", testUserId)
        val childLedger = createTestLedger(childLedgerId, "子记账簿", testUserId)
        
        coEvery { ledgerRepository.getLedgerById(parentLedgerId) } returns BaseResult.Success(parentLedger)
        coEvery { ledgerRepository.getLedgerById(childLedgerId) } returns BaseResult.Success(childLedger)
        coEvery { ledgerLinkRepository.hasActiveLinkBetween(parentLedgerId, childLedgerId) } returns 
            BaseResult.Success(true)

        // When: 创建联动关系
        val result = useCase.createLedgerLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId
        )

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含已存在", (result as BaseResult.Error).exception.message!!.contains("记账簿之间已存在联动关系"))
    }

    @Test
    fun `updateSyncMode - 成功更新同步模式`() = runTest {
        // Given: 存在的联动关系
        val existingLink = createTestLedgerLink(linkId, parentLedgerId, childLedgerId)
        
        coEvery { ledgerLinkRepository.getLinkById(linkId) } returns BaseResult.Success(existingLink)
        coEvery { ledgerLinkRepository.updateSyncMode(linkId, SyncMode.PARENT_TO_CHILD) } returns BaseResult.Success(Unit)

        // When: 更新同步模式
        val result = useCase.updateSyncMode(linkId, SyncMode.PARENT_TO_CHILD)

        // Then: 更新成功
        assertTrue("结果应该是Success", result is BaseResult.Success)
        
        coVerify(exactly = 1) { ledgerLinkRepository.getLinkById(linkId) }
        coVerify(exactly = 1) { ledgerLinkRepository.updateSyncMode(linkId, SyncMode.PARENT_TO_CHILD) }
    }

    @Test
    fun `updateSyncMode - 联动关系不存在时返回错误`() = runTest {
        // Given: 联动关系不存在
        coEvery { ledgerLinkRepository.getLinkById(linkId) } returns BaseResult.Success(null)

        // When: 更新同步模式
        val result = useCase.updateSyncMode(linkId, SyncMode.PARENT_TO_CHILD)

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含不存在", (result as BaseResult.Error).exception.message!!.contains("联动关系不存在"))
    }

    @Test
    fun `setAutoSyncEnabled - 成功设置自动同步`() = runTest {
        // Given: 存在的联动关系
        val existingLink = createTestLedgerLink(linkId, parentLedgerId, childLedgerId)
        
        coEvery { ledgerLinkRepository.getLinkById(linkId) } returns BaseResult.Success(existingLink)
        coEvery { ledgerLinkRepository.setAutoSyncEnabled(linkId, false) } returns BaseResult.Success(Unit)

        // When: 设置自动同步
        val result = useCase.setAutoSyncEnabled(linkId, false)

        // Then: 设置成功
        assertTrue("结果应该是Success", result is BaseResult.Success)
        
        coVerify(exactly = 1) { ledgerLinkRepository.getLinkById(linkId) }
        coVerify(exactly = 1) { ledgerLinkRepository.setAutoSyncEnabled(linkId, false) }
    }

    @Test
    fun `deleteLedgerLink - 成功删除联动关系`() = runTest {
        // Given: 存在的联动关系
        val existingLink = createTestLedgerLink(linkId, parentLedgerId, childLedgerId)
        
        coEvery { ledgerLinkRepository.getLinkById(linkId) } returns BaseResult.Success(existingLink)
        coEvery { ledgerLinkRepository.deleteLink(linkId) } returns BaseResult.Success(Unit)

        // When: 删除联动关系
        val result = useCase.deleteLedgerLink(linkId)

        // Then: 删除成功
        assertTrue("结果应该是Success", result is BaseResult.Success)
        
        coVerify(exactly = 1) { ledgerLinkRepository.getLinkById(linkId) }
        coVerify(exactly = 1) { ledgerLinkRepository.deleteLink(linkId) }
    }

    @Test
    fun `deleteLedgerLink - ID为空时返回错误`() = runTest {
        // When: 删除空ID联动关系
        val result = useCase.deleteLedgerLink("")

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含ID不能为空", (result as BaseResult.Error).exception.message!!.contains("联动关系ID不能为空"))
    }

    @Test
    fun `deleteAllLinksForLedger - 成功删除记账簿所有联动关系`() = runTest {
        // Given: 存在的记账簿
        val ledger = createTestLedger(parentLedgerId, "总记账簿", testUserId)
        
        coEvery { ledgerRepository.getLedgerById(parentLedgerId) } returns BaseResult.Success(ledger)
        coEvery { ledgerLinkRepository.deleteAllLinksForLedger(parentLedgerId) } returns BaseResult.Success(Unit)

        // When: 删除记账簿所有联动关系
        val result = useCase.deleteAllLinksForLedger(parentLedgerId)

        // Then: 删除成功
        assertTrue("结果应该是Success", result is BaseResult.Success)
        
        coVerify(exactly = 1) { ledgerRepository.getLedgerById(parentLedgerId) }
        coVerify(exactly = 1) { ledgerLinkRepository.deleteAllLinksForLedger(parentLedgerId) }
    }

    @Test
    fun `getLinkBetweenLedgers - 成功获取两个记账簿间的联动关系`() = runTest {
        // Given: 存在的联动关系
        val expectedLink = createTestLedgerLink(linkId, parentLedgerId, childLedgerId)
        
        coEvery { ledgerLinkRepository.getLinkBetweenLedgers(parentLedgerId, childLedgerId) } returns 
            BaseResult.Success(expectedLink)

        // When: 获取联动关系
        val result = useCase.getLinkBetweenLedgers(parentLedgerId, childLedgerId)

        // Then: 获取成功
        assertTrue("结果应该是Success", result is BaseResult.Success)
        val link = (result as BaseResult.Success).data!!
        assertEquals("链接ID正确", linkId, link.id)
        assertEquals("父记账簿ID正确", parentLedgerId, link.parentLedgerId)
        assertEquals("子记账簿ID正确", childLedgerId, link.childLedgerId)
    }

    @Test
    fun `hasLinkBetweenLedgers - 正确检查联动关系存在性`() = runTest {
        // Given: 联动关系存在
        coEvery { ledgerLinkRepository.hasActiveLinkBetween(parentLedgerId, childLedgerId) } returns 
            BaseResult.Success(true)

        // When: 检查联动关系
        val result = useCase.hasLinkBetweenLedgers(parentLedgerId, childLedgerId)

        // Then: 返回存在
        assertTrue("结果应该是Success", result is BaseResult.Success)
        assertTrue("联动关系存在", (result as BaseResult.Success).data)
        
        coVerify(exactly = 1) { ledgerLinkRepository.hasActiveLinkBetween(parentLedgerId, childLedgerId) }
    }

    @Test
    fun `getLedgerLinks - 成功获取记账簿的联动关系`() = runTest {
        // Given: 记账簿的联动关系列表
        val expectedLinks = listOf(
            createTestLedgerLink("link_001", parentLedgerId, "child_001"),
            createTestLedgerLink("link_002", parentLedgerId, "child_002")
        )
        
        every { ledgerLinkRepository.getAllLinksForLedger(parentLedgerId) } returns flowOf(expectedLinks)

        // When: 获取联动关系
        val result = useCase.getLedgerLinks(parentLedgerId).first()

        // Then: 获取成功
        assertEquals("联动关系数量正确", 2, result.size)
        assertEquals("第一个联动关系ID正确", "link_001", result[0].id)
        assertEquals("第二个联动关系ID正确", "link_002", result[1].id)
    }

    @Test
    fun `getRecommendedSyncModes - 返回推荐的同步模式列表`() {
        // When: 获取推荐同步模式
        val result = useCase.getRecommendedSyncModes()

        // Then: 返回正确的推荐列表
        assertEquals("推荐模式数量正确", 3, result.size)
        assertTrue("包含双向同步", result.contains(SyncMode.BIDIRECTIONAL))
        assertTrue("包含父到子", result.contains(SyncMode.PARENT_TO_CHILD))
        assertTrue("包含子到父", result.contains(SyncMode.CHILD_TO_PARENT))
    }

    @Test
    fun `getSyncModeDescription - 返回同步模式描述`() {
        // When & Then: 各种同步模式的描述
        val bidirectionalDesc = useCase.getSyncModeDescription(SyncMode.BIDIRECTIONAL)
        val parentToChildDesc = useCase.getSyncModeDescription(SyncMode.PARENT_TO_CHILD)
        val childToParentDesc = useCase.getSyncModeDescription(SyncMode.CHILD_TO_PARENT)

        assertEquals("双向同步描述正确", SyncMode.BIDIRECTIONAL.description, bidirectionalDesc)
        assertEquals("父到子描述正确", SyncMode.PARENT_TO_CHILD.description, parentToChildDesc)
        assertEquals("子到父描述正确", SyncMode.CHILD_TO_PARENT.description, childToParentDesc)
    }

    // === 测试工具方法 ===

    private fun createTestLedger(
        id: String,
        name: String,
        userId: String,
        isActive: Boolean = true
    ): Ledger {
        return Ledger(
            id = id,
            name = name,
            description = "Test Description",
            color = "#FF5722",
            icon = "default",
            isDefault = false,
            userId = userId,
            createdAt = testInstant,
            updatedAt = testInstant,
            isActive = isActive
        )
    }

    private fun createTestLedgerLink(
        id: String,
        parentLedgerId: String,
        childLedgerId: String,
        syncMode: SyncMode = SyncMode.BIDIRECTIONAL,
        autoSyncEnabled: Boolean = true
    ): LedgerLink {
        return LedgerLink(
            id = id,
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId,
            syncMode = syncMode,
            autoSyncEnabled = autoSyncEnabled,
            createdAt = testInstant,
            updatedAt = testInstant,
            isActive = true
        )
    }
}
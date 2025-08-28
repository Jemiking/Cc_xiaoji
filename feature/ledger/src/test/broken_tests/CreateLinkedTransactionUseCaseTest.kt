package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionLedgerRelationDao
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionLedgerRelationEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.just
import io.mockk.Runs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * CreateLinkedTransactionUseCase单元测试
 * 测试联动交易创建的各种场景和边界情况
 */
class CreateLinkedTransactionUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var ledgerRepository: LedgerRepository
    private lateinit var transactionLedgerRelationDao: TransactionLedgerRelationDao
    private lateinit var syncTransactionUseCase: SyncTransactionUseCase
    private lateinit var useCase: CreateLinkedTransactionUseCase
    
    private val testLedgerId = "ledger_001"
    private val testAccountId = "account_001"
    private val testCategoryId = "category_001"
    private val testUserId = "user_001"
    private val testAmountCents = 10000 // 100.00元
    private val testInstant = Clock.System.now()

    @Before
    fun setup() {
        transactionRepository = mockk()
        ledgerRepository = mockk()
        transactionLedgerRelationDao = mockk()
        syncTransactionUseCase = mockk()
        
        useCase = CreateLinkedTransactionUseCase(
            transactionRepository,
            ledgerRepository,
            transactionLedgerRelationDao,
            syncTransactionUseCase
        )
    }

    @Test
    fun `createLinkedTransaction - 成功创建联动交易`() = runTest {
        // Given: 正常的交易参数和依赖设置
        val testLedger = createTestLedger(testLedgerId, isActive = true)
        val relationSlot = slot<TransactionLedgerRelationEntity>()
        val syncedRelations = listOf(
            createTestTransactionLedgerRelation("trans_001", "child_ledger_001", RelationType.SYNCED_FROM_PARENT)
        )
        
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns BaseResult.Success(testLedger)
        coEvery { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) } returns 
            BaseResult.Success("trans_001")
        coEvery { transactionLedgerRelationDao.insertRelation(capture(relationSlot)) } just Runs
        coEvery { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), any()) } returns 
            BaseResult.Success(syncedRelations)

        // When: 创建联动交易
        val result = useCase.createLinkedTransaction(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = testAmountCents,
            categoryId = testCategoryId,
            note = "Test transaction",
            autoSync = true
        )

        // Then: 交易创建成功，包含PRIMARY关系和同步关系
        assertTrue("结果应该是Success", result is BaseResult.Success)
        val transactionResult = (result as BaseResult.Success).data
        
        assertEquals("交易金额正确", testAmountCents, transactionResult.transaction.amountCents)
        assertEquals("交易账户正确", testAccountId, transactionResult.transaction.accountId)
        assertEquals("交易分类正确", testCategoryId, transactionResult.transaction.categoryId)
        assertEquals("交易备注正确", "Test transaction", transactionResult.transaction.note)
        
        assertEquals("PRIMARY关系类型正确", RelationType.PRIMARY, transactionResult.primaryRelation.relationType)
        assertEquals("PRIMARY关系记账簿正确", testLedgerId, transactionResult.primaryRelation.ledgerId)
        
        assertEquals("同步关系数量正确", 1, transactionResult.syncedRelations.size)
        assertEquals("同步关系记账簿正确", "child_ledger_001", transactionResult.syncedRelations[0].ledgerId)
        
        // 验证依赖调用
        coVerify(exactly = 1) { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { transactionLedgerRelationDao.insertRelation(any()) }
        coVerify(exactly = 1) { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), testLedgerId) }
        
        // 验证关系记录的内容
        val capturedRelation = relationSlot.captured
        assertEquals("关系类型为PRIMARY", "PRIMARY", capturedRelation.relationType)
        assertEquals("关系记账簿正确", testLedgerId, capturedRelation.ledgerId)
    }

    @Test
    fun `createLinkedTransaction - 参数验证失败时返回错误`() = runTest {
        // When & Then: 主记账簿ID为空
        val result1 = useCase.createLinkedTransaction(
            primaryLedgerId = "",
            accountId = testAccountId,
            amountCents = testAmountCents,
            categoryId = testCategoryId
        )
        assertTrue("主记账簿ID为空应返回错误", result1 is BaseResult.Error)
        assertTrue("错误消息包含记账簿ID", (result1 as BaseResult.Error).exception.message!!.contains("主记账簿ID不能为空"))

        // When & Then: 账户ID为空
        val result2 = useCase.createLinkedTransaction(
            primaryLedgerId = testLedgerId,
            accountId = "",
            amountCents = testAmountCents,
            categoryId = testCategoryId
        )
        assertTrue("账户ID为空应返回错误", result2 is BaseResult.Error)
        assertTrue("错误消息包含账户ID", (result2 as BaseResult.Error).exception.message!!.contains("账户ID不能为空"))

        // When & Then: 分类ID为空
        val result3 = useCase.createLinkedTransaction(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = testAmountCents,
            categoryId = ""
        )
        assertTrue("分类ID为空应返回错误", result3 is BaseResult.Error)
        assertTrue("错误消息包含分类ID", (result3 as BaseResult.Error).exception.message!!.contains("分类ID不能为空"))

        // When & Then: 金额为零
        val result4 = useCase.createLinkedTransaction(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = 0,
            categoryId = testCategoryId
        )
        assertTrue("金额为零应返回错误", result4 is BaseResult.Error)
        assertTrue("错误消息包含金额", (result4 as BaseResult.Error).exception.message!!.contains("交易金额不能为零"))
    }

    @Test
    fun `createLinkedTransaction - 记账簿不存在时返回错误`() = runTest {
        // Given: 记账簿不存在
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns 
            BaseResult.Error(Exception("Record not found"))

        // When: 创建联动交易
        val result = useCase.createLinkedTransaction(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = testAmountCents,
            categoryId = testCategoryId
        )

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含记账簿不存在", (result as BaseResult.Error).exception.message!!.contains("主记账簿不存在"))
    }

    @Test
    fun `createLinkedTransaction - 记账簿未激活时返回错误`() = runTest {
        // Given: 记账簿未激活
        val inactiveLedger = createTestLedger(testLedgerId, isActive = false)
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns BaseResult.Success(inactiveLedger)

        // When: 创建联动交易
        val result = useCase.createLinkedTransaction(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = testAmountCents,
            categoryId = testCategoryId
        )

        // Then: 返回错误
        assertTrue("应该返回错误", result is BaseResult.Error)
        assertTrue("错误消息包含记账簿未激活", (result as BaseResult.Error).exception.message!!.contains("主记账簿未激活"))
    }

    @Test
    fun `createLinkedTransaction - 指定目标记账簿列表时同步到指定记账簿`() = runTest {
        // Given: 正常设置 + 指定目标记账簿列表
        val testLedger = createTestLedger(testLedgerId, isActive = true)
        val targetLedgers = listOf("target_001", "target_002")
        val syncedRelation1 = createTestTransactionLedgerRelation("trans_001", "target_001", RelationType.SYNCED_FROM_PARENT)
        val syncedRelation2 = createTestTransactionLedgerRelation("trans_001", "target_002", RelationType.SYNCED_FROM_PARENT)
        
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns BaseResult.Success(testLedger)
        coEvery { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) } returns 
            BaseResult.Success("trans_001")
        coEvery { transactionLedgerRelationDao.insertRelation(any()) } just Runs
        coEvery { syncTransactionUseCase.manualSyncTransaction(any(), any(), "target_001") } returns 
            BaseResult.Success(syncedRelation1)
        coEvery { syncTransactionUseCase.manualSyncTransaction(any(), any(), "target_002") } returns 
            BaseResult.Success(syncedRelation2)

        // When: 创建联动交易，指定目标记账簿
        val result = useCase.createLinkedTransaction(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = testAmountCents,
            categoryId = testCategoryId,
            autoSync = true,
            specificTargetLedgers = targetLedgers
        )

        // Then: 同步到指定的记账簿
        assertTrue("结果应该是Success", result is BaseResult.Success)
        val transactionResult = (result as BaseResult.Success).data
        
        assertEquals("同步关系数量正确", 2, transactionResult.syncedRelations.size)
        assertTrue("包含目标记账簿1", transactionResult.syncedRelations.any { it.ledgerId == "target_001" })
        assertTrue("包含目标记账簿2", transactionResult.syncedRelations.any { it.ledgerId == "target_002" })
        
        // 验证手动同步调用
        coVerify(exactly = 1) { syncTransactionUseCase.manualSyncTransaction(any(), testLedgerId, "target_001") }
        coVerify(exactly = 1) { syncTransactionUseCase.manualSyncTransaction(any(), testLedgerId, "target_002") }
        coVerify(exactly = 0) { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), any()) }
    }

    @Test
    fun `createLinkedTransaction - autoSync为false时不进行同步`() = runTest {
        // Given: 正常设置
        val testLedger = createTestLedger(testLedgerId, isActive = true)
        
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns BaseResult.Success(testLedger)
        coEvery { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) } returns 
            BaseResult.Success("trans_001")
        coEvery { transactionLedgerRelationDao.insertRelation(any()) } just Runs

        // When: 创建联动交易，autoSync为false
        val result = useCase.createLinkedTransaction(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = testAmountCents,
            categoryId = testCategoryId,
            autoSync = false
        )

        // Then: 不进行同步
        assertTrue("结果应该是Success", result is BaseResult.Success)
        val transactionResult = (result as BaseResult.Success).data
        
        assertEquals("没有同步关系", 0, transactionResult.syncedRelations.size)
        
        // 验证不调用同步方法
        coVerify(exactly = 0) { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), any()) }
        coVerify(exactly = 0) { syncTransactionUseCase.manualSyncTransaction(any(), any(), any()) }
    }

    @Test
    fun `batchCreateLinkedTransactions - 成功批量创建交易`() = runTest {
        // Given: 批量交易请求
        val request1 = CreateTransactionRequest(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = 10000,
            categoryId = testCategoryId,
            note = "Transaction 1"
        )
        val request2 = CreateTransactionRequest(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = 20000,
            categoryId = testCategoryId,
            note = "Transaction 2"
        )
        val requests = listOf(request1, request2)
        
        val testLedger = createTestLedger(testLedgerId, isActive = true)
        
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns BaseResult.Success(testLedger)
        coEvery { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) } returns 
            BaseResult.Success("trans_001") andThen BaseResult.Success("trans_002")
        coEvery { transactionLedgerRelationDao.insertRelation(any()) } just Runs
        coEvery { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), any()) } returns 
            BaseResult.Success(emptyList())

        // When: 批量创建交易
        val result = useCase.batchCreateLinkedTransactions(requests)

        // Then: 所有交易创建成功
        assertTrue("结果应该是Success", result is BaseResult.Success)
        val batchResult = (result as BaseResult.Success).data
        
        assertEquals("成功数量正确", 2, batchResult.successCount)
        assertEquals("错误数量正确", 0, batchResult.errorCount)
        assertEquals("总数量正确", 2, batchResult.getTotalCount())
        assertTrue("所有交易成功", batchResult.isAllSuccess())
        assertEquals("成功率100%", 1.0f, batchResult.getSuccessRate())
    }

    @Test
    fun `batchCreateLinkedTransactions - 部分失败时返回混合结果`() = runTest {
        // Given: 批量交易请求，其中一个会失败
        val validRequest = CreateTransactionRequest(
            primaryLedgerId = testLedgerId,
            accountId = testAccountId,
            amountCents = 10000,
            categoryId = testCategoryId
        )
        val invalidRequest = CreateTransactionRequest(
            primaryLedgerId = "", // 无效的记账簿ID
            accountId = testAccountId,
            amountCents = 20000,
            categoryId = testCategoryId
        )
        val requests = listOf(validRequest, invalidRequest)
        
        val testLedger = createTestLedger(testLedgerId, isActive = true)
        
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns BaseResult.Success(testLedger)
        coEvery { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) } returns 
            BaseResult.Success("trans_001")
        coEvery { transactionLedgerRelationDao.insertRelation(any()) } just Runs
        coEvery { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), any()) } returns 
            BaseResult.Success(emptyList())

        // When: 批量创建交易
        val result = useCase.batchCreateLinkedTransactions(requests)

        // Then: 部分成功，部分失败
        assertTrue("结果应该是Success", result is BaseResult.Success)
        val batchResult = (result as BaseResult.Success).data
        
        assertEquals("成功数量正确", 1, batchResult.successCount)
        assertEquals("错误数量正确", 1, batchResult.errorCount)
        assertEquals("总数量正确", 2, batchResult.getTotalCount())
        assertFalse("不是全部成功", batchResult.isAllSuccess())
        assertEquals("成功率50%", 0.5f, batchResult.getSuccessRate())
        
        // 验证错误信息
        assertEquals("错误列表数量正确", 1, batchResult.errors.size)
        val error = batchResult.errors[0]
        assertEquals("错误索引正确", 1, error.index)
        assertEquals("错误请求正确", invalidRequest, error.request)
        assertTrue("错误消息包含记账簿ID", error.error.contains("主记账簿ID不能为空"))
    }

    @Test
    fun `validateTransactionRequest - 成功验证有效请求`() = runTest {
        // Given: 有效的记账簿
        val testLedger = createTestLedger(testLedgerId, isActive = true)
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns BaseResult.Success(testLedger)

        // When: 验证交易请求
        val result = useCase.validateTransactionRequest(testLedgerId, testAccountId, testCategoryId, testAmountCents)

        // Then: 验证通过
        assertTrue("验证应该成功", result is BaseResult.Success)
    }

    @Test
    fun `validateTransactionRequest - 记账簿不存在时验证失败`() = runTest {
        // Given: 记账簿不存在
        coEvery { ledgerRepository.getLedgerById(testLedgerId) } returns 
            BaseResult.Error(Exception("Record not found"))

        // When: 验证交易请求
        val result = useCase.validateTransactionRequest(testLedgerId, testAccountId, testCategoryId, testAmountCents)

        // Then: 验证失败
        assertTrue("验证应该失败", result is BaseResult.Error)
        assertTrue("错误消息包含记账簿不存在", (result as BaseResult.Error).exception.message!!.contains("记账簿不存在"))
    }

    // === 测试工具方法 ===

    private fun createTestLedger(id: String, isActive: Boolean = true): Ledger {
        return Ledger(
            id = id,
            name = "Test Ledger",
            description = "Test Description",
            color = "#FF5722",
            icon = "default",
            isDefault = false,
            userId = testUserId,
            createdAt = testInstant,
            updatedAt = testInstant,
            isActive = isActive
        )
    }

    private fun createTestTransactionLedgerRelation(
        transactionId: String,
        ledgerId: String,
        relationType: RelationType,
        syncSourceLedgerId: String? = null
    ): TransactionLedgerRelation {
        return TransactionLedgerRelation(
            id = "${transactionId}_${ledgerId}_${relationType.name}",
            transactionId = transactionId,
            ledgerId = ledgerId,
            relationType = relationType,
            syncSourceLedgerId = syncSourceLedgerId,
            createdAt = testInstant
        )
    }
}
package com.ccxiaoji.feature.ledger

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.usecase.*
import com.ccxiaoji.feature.ledger.domain.repository.*
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionLedgerRelationDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.just
import io.mockk.Runs
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 记账簿联动功能综合集成测试
 * 测试从联动关系创建到交易同步的完整业务流程
 */
class LedgerLinkingIntegrationTest {

    // UseCase层
    private lateinit var manageLedgerLinkUseCase: ManageLedgerLinkUseCase
    private lateinit var createLinkedTransactionUseCase: CreateLinkedTransactionUseCase
    private lateinit var filterTransactionsByLedgerUseCase: FilterTransactionsByLedgerUseCase
    private lateinit var syncTransactionUseCase: SyncTransactionUseCase
    
    // Repository层（Mocked）
    private lateinit var ledgerLinkRepository: LedgerLinkRepository
    private lateinit var ledgerRepository: LedgerRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var transactionLedgerRelationRepository: TransactionLedgerRelationRepository
    private lateinit var transactionLedgerRelationDao: TransactionLedgerRelationDao
    
    // 测试数据
    private val testUserId = "user_001"
    private val mainLedgerId = "main_ledger"
    private val workLedgerId = "work_ledger"
    private val personalLedgerId = "personal_ledger"
    private val testAccountId = "account_001"
    private val testCategoryId = "category_001"
    private val testInstant = Clock.System.now()

    @Before
    fun setup() {
        // 初始化Repository层（Mock）
        ledgerLinkRepository = mockk()
        ledgerRepository = mockk()
        transactionRepository = mockk()
        transactionLedgerRelationRepository = mockk()
        transactionLedgerRelationDao = mockk()
        syncTransactionUseCase = mockk()
        
        // 初始化UseCase层
        manageLedgerLinkUseCase = ManageLedgerLinkUseCase(
            ledgerLinkRepository,
            ledgerRepository
        )
        
        createLinkedTransactionUseCase = CreateLinkedTransactionUseCase(
            transactionRepository,
            ledgerRepository,
            transactionLedgerRelationDao,
            syncTransactionUseCase
        )
        
        filterTransactionsByLedgerUseCase = FilterTransactionsByLedgerUseCase(
            transactionRepository,
            ledgerLinkRepository,
            transactionLedgerRelationRepository
        )
    }

    @Test
    fun `完整的联动功能集成测试 - 创建联动关系并同步交易`() = runTest {
        
        // ============== Phase 1: 设置记账簿和联动关系 ==============
        
        val mainLedger = createTestLedger(mainLedgerId, "总记账簿", testUserId)
        val workLedger = createTestLedger(workLedgerId, "工作记账簿", testUserId)
        val personalLedger = createTestLedger(personalLedgerId, "个人记账簿", testUserId)
        
        // 创建主记账簿与工作记账簿的联动关系
        val mainWorkLink = createTestLedgerLink(
            "link_main_work", 
            mainLedgerId, 
            workLedgerId, 
            SyncMode.BIDIRECTIONAL
        )
        
        // 创建主记账簿与个人记账簿的联动关系
        val mainPersonalLink = createTestLedgerLink(
            "link_main_personal", 
            mainLedgerId, 
            personalLedgerId, 
            SyncMode.PARENT_TO_CHILD
        )
        
        // Mock记账簿查询
        coEvery { ledgerRepository.getLedgerById(mainLedgerId) } returns BaseResult.Success(mainLedger)
        coEvery { ledgerRepository.getLedgerById(workLedgerId) } returns BaseResult.Success(workLedger)
        coEvery { ledgerRepository.getLedgerById(personalLedgerId) } returns BaseResult.Success(personalLedger)
        
        // Mock联动关系创建
        coEvery { ledgerLinkRepository.hasActiveLinkBetween(mainLedgerId, workLedgerId) } returns BaseResult.Success(false)
        coEvery { ledgerLinkRepository.hasActiveLinkBetween(mainLedgerId, personalLedgerId) } returns BaseResult.Success(false)
        every { ledgerLinkRepository.getChildLinks(any()) } returns flowOf(emptyList())
        
        coEvery { ledgerLinkRepository.createLink(mainLedgerId, workLedgerId, SyncMode.BIDIRECTIONAL, true) } returns 
            BaseResult.Success(mainWorkLink)
        coEvery { ledgerLinkRepository.createLink(mainLedgerId, personalLedgerId, SyncMode.PARENT_TO_CHILD, true) } returns 
            BaseResult.Success(mainPersonalLink)
        
        // Step 1: 创建联动关系
        val linkResult1 = manageLedgerLinkUseCase.createLedgerLink(
            parentLedgerId = mainLedgerId,
            childLedgerId = workLedgerId,
            syncMode = SyncMode.BIDIRECTIONAL
        )
        
        val linkResult2 = manageLedgerLinkUseCase.createLedgerLink(
            parentLedgerId = mainLedgerId,
            childLedgerId = personalLedgerId,
            syncMode = SyncMode.PARENT_TO_CHILD
        )
        
        // 验证联动关系创建成功
        assertTrue("主-工作联动创建成功", linkResult1 is BaseResult.Success)
        assertTrue("主-个人联动创建成功", linkResult2 is BaseResult.Success)
        
        // ============== Phase 2: 在主记账簿中创建交易 ==============
        
        val transactionId = "transaction_001"
        val transaction = createTestTransaction(transactionId, mainLedgerId, 10000) // 100元
        
        // Mock交易创建和同步
        coEvery { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) } returns 
            BaseResult.Success(transactionId)
        coEvery { transactionLedgerRelationDao.insertRelation(any()) } just Runs
        
        // Mock同步到工作记账簿和个人记账簿
        val syncedToWork = createTestTransactionLedgerRelation(
            transactionId, workLedgerId, RelationType.SYNCED_FROM_PARENT, mainLedgerId
        )
        val syncedToPersonal = createTestTransactionLedgerRelation(
            transactionId, personalLedgerId, RelationType.SYNCED_FROM_PARENT, mainLedgerId
        )
        
        coEvery { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), mainLedgerId) } returns 
            BaseResult.Success(listOf(syncedToWork, syncedToPersonal))
        
        // Step 2: 创建联动交易
        val createTransactionResult = createLinkedTransactionUseCase.createLinkedTransaction(
            primaryLedgerId = mainLedgerId,
            accountId = testAccountId,
            amountCents = 10000,
            categoryId = testCategoryId,
            note = "集成测试交易",
            autoSync = true
        )
        
        // 验证交易创建成功
        assertTrue("联动交易创建成功", createTransactionResult is BaseResult.Success)
        val transactionResult = (createTransactionResult as BaseResult.Success).data
        
        assertEquals("交易金额正确", 10000, transactionResult.transaction.amountCents)
        assertEquals("交易记账簿正确", mainLedgerId, transactionResult.transaction.ledgerId)
        assertEquals("PRIMARY关系记账簿正确", mainLedgerId, transactionResult.primaryRelation.ledgerId)
        assertEquals("同步关系数量正确", 2, transactionResult.syncedRelations.size)
        
        // 验证同步关系内容
        val workSyncRelation = transactionResult.syncedRelations.find { it.ledgerId == workLedgerId }
        val personalSyncRelation = transactionResult.syncedRelations.find { it.ledgerId == personalLedgerId }
        
        assertNotNull("工作记账簿同步关系存在", workSyncRelation)
        assertNotNull("个人记账簿同步关系存在", personalSyncRelation)
        assertEquals("工作同步类型正确", RelationType.SYNCED_FROM_PARENT, workSyncRelation!!.relationType)
        assertEquals("个人同步类型正确", RelationType.SYNCED_FROM_PARENT, personalSyncRelation!!.relationType)
        
        // ============== Phase 3: 验证交易过滤和显示逻辑 ==============
        
        // 设置过滤查询Mock数据
        val mainTransactions = listOf(transaction)
        val workTransactions = listOf(transaction) // 同步到工作记账簿
        val personalTransactions = listOf(transaction) // 同步到个人记账簿
        
        val mainRelations = listOf(
            createTestTransactionLedgerRelation(transactionId, mainLedgerId, RelationType.PRIMARY),
            syncedToWork,
            syncedToPersonal
        )
        
        val workRelations = listOf(
            createTestTransactionLedgerRelation(transactionId, workLedgerId, RelationType.SYNCED_FROM_PARENT, mainLedgerId)
        )
        
        val personalRelations = listOf(
            createTestTransactionLedgerRelation(transactionId, personalLedgerId, RelationType.SYNCED_FROM_PARENT, mainLedgerId)
        )
        
        val allLinks = listOf(mainWorkLink, mainPersonalLink)
        
        // Mock过滤查询
        coEvery { transactionRepository.getTransactionsByLedger(mainLedgerId) } returns flowOf(mainTransactions)
        coEvery { transactionRepository.getTransactionsByLedger(workLedgerId) } returns flowOf(workTransactions)
        coEvery { transactionRepository.getTransactionsByLedger(personalLedgerId) } returns flowOf(personalTransactions)
        
        coEvery { transactionLedgerRelationRepository.getRelationsByLedger(mainLedgerId) } returns flowOf(mainRelations)
        coEvery { transactionLedgerRelationRepository.getRelationsByLedger(workLedgerId) } returns flowOf(workRelations)
        coEvery { transactionLedgerRelationRepository.getRelationsByLedger(personalLedgerId) } returns flowOf(personalRelations)
        
        coEvery { ledgerLinkRepository.getAllLinksForLedger(any()) } returns flowOf(allLinks)
        
        // Step 3: 在主记账簿中查看交易（LOCAL_WITH_SYNCED模式）
        val mainLedgerTransactions = filterTransactionsByLedgerUseCase.getFilteredTransactions(
            mainLedgerId, 
            TransactionFilterMode.LOCAL_WITH_SYNCED
        ).first()
        
        assertEquals("主记账簿交易数量", 1, mainLedgerTransactions.size)
        val mainTransaction = mainLedgerTransactions[0]
        assertEquals("主记账簿交易为PRIMARY", TransactionSyncType.PRIMARY, mainTransaction.syncType)
        assertEquals("主记账簿交易源", mainLedgerId, mainTransaction.sourceLedgerId)
        assertEquals("同步目标数量", 2, mainTransaction.targetLedgerIds.size)
        
        // Step 4: 在工作记账簿中查看交易（LOCAL_WITH_SYNCED模式）
        val workLedgerTransactions = filterTransactionsByLedgerUseCase.getFilteredTransactions(
            workLedgerId, 
            TransactionFilterMode.LOCAL_WITH_SYNCED
        ).first()
        
        assertEquals("工作记账簿交易数量", 1, workLedgerTransactions.size)
        val workTransaction = workLedgerTransactions[0]
        assertEquals("工作记账簿交易为SYNCED", TransactionSyncType.SYNCED, workTransaction.syncType)
        assertEquals("工作记账簿交易源", mainLedgerId, workTransaction.sourceLedgerId)
        
        // Step 5: 在工作记账簿中查看仅本地交易（LOCAL_ONLY模式）
        val workLocalOnlyTransactions = filterTransactionsByLedgerUseCase.getFilteredTransactions(
            workLedgerId, 
            TransactionFilterMode.LOCAL_ONLY
        ).first()
        
        assertEquals("工作记账簿本地交易数量", 0, workLocalOnlyTransactions.size) // 没有原始交易
        
        // ============== Phase 4: 验证交易同步状态查询 ==============
        
        coEvery { transactionLedgerRelationRepository.getRelationsByTransaction(transactionId) } returns 
            flowOf(mainRelations)
        
        // Step 6: 查询交易在不同记账簿中的同步状态
        val statusInMain = filterTransactionsByLedgerUseCase.getTransactionSyncStatus(transactionId, mainLedgerId)
        val statusInWork = filterTransactionsByLedgerUseCase.getTransactionSyncStatus(transactionId, workLedgerId)
        val statusInPersonal = filterTransactionsByLedgerUseCase.getTransactionSyncStatus(transactionId, personalLedgerId)
        
        assertTrue("主记账簿状态查询成功", statusInMain is BaseResult.Success)
        assertTrue("工作记账簿状态查询成功", statusInWork is BaseResult.Success)
        assertTrue("个人记账簿状态查询成功", statusInPersonal is BaseResult.Success)
        
        assertEquals("主记账簿为PRIMARY", TransactionSyncType.PRIMARY, (statusInMain as BaseResult.Success).data)
        assertEquals("工作记账簿为SYNCED", TransactionSyncType.SYNCED, (statusInWork as BaseResult.Success).data)
        assertEquals("个人记账簿为SYNCED", TransactionSyncType.SYNCED, (statusInPersonal as BaseResult.Success).data)
        
        // Step 7: 查询交易的同步网络
        val syncNetwork = filterTransactionsByLedgerUseCase.getTransactionSyncNetwork(transactionId)
        
        assertTrue("同步网络查询成功", syncNetwork is BaseResult.Success)
        val networkLedgers = (syncNetwork as BaseResult.Success).data
        assertEquals("网络记账簿数量", 3, networkLedgers.size)
        assertTrue("包含主记账簿", networkLedgers.contains(mainLedgerId))
        assertTrue("包含工作记账簿", networkLedgers.contains(workLedgerId))
        assertTrue("包含个人记账簿", networkLedgers.contains(personalLedgerId))
        
        // ============== Phase 5: 验证依赖调用 ==============
        
        // 验证关键方法被正确调用
        coVerify(exactly = 2) { ledgerLinkRepository.createLink(any(), any(), any(), any()) }
        coVerify(exactly = 1) { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), mainLedgerId) }
        coVerify(exactly = 1) { transactionLedgerRelationDao.insertRelation(any()) }
        
        println("✅ 联动功能集成测试全部通过！")
    }

    @Test
    fun `边界情况测试 - 复杂联动场景处理`() = runTest {
        
        // ============== 测试场景：单向同步模式下的行为 ==============
        
        val parentLedger = createTestLedger("parent", "父记账簿", testUserId)
        val childLedger = createTestLedger("child", "子记账簿", testUserId)
        
        coEvery { ledgerRepository.getLedgerById("parent") } returns BaseResult.Success(parentLedger)
        coEvery { ledgerRepository.getLedgerById("child") } returns BaseResult.Success(childLedger)
        coEvery { ledgerLinkRepository.hasActiveLinkBetween("parent", "child") } returns BaseResult.Success(false)
        every { ledgerLinkRepository.getChildLinks("child") } returns flowOf(emptyList())
        
        val parentToChildLink = createTestLedgerLink(
            "ptc_link", "parent", "child", SyncMode.PARENT_TO_CHILD
        )
        
        coEvery { ledgerLinkRepository.createLink("parent", "child", SyncMode.PARENT_TO_CHILD, true) } returns 
            BaseResult.Success(parentToChildLink)
        
        // 创建单向联动关系
        val linkResult = manageLedgerLinkUseCase.createLedgerLink(
            parentLedgerId = "parent",
            childLedgerId = "child",
            syncMode = SyncMode.PARENT_TO_CHILD
        )
        
        assertTrue("单向联动创建成功", linkResult is BaseResult.Success)
        val link = (linkResult as BaseResult.Success).data
        assertEquals("同步模式为单向", SyncMode.PARENT_TO_CHILD, link.syncMode)
        
        // ============== 测试场景：禁用自动同步的情况 ==============
        
        coEvery { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) } returns 
            BaseResult.Success("trans_002")
        coEvery { transactionLedgerRelationDao.insertRelation(any()) } just Runs
        
        val noSyncResult = createLinkedTransactionUseCase.createLinkedTransaction(
            primaryLedgerId = "parent",
            accountId = testAccountId,
            amountCents = 5000,
            categoryId = testCategoryId,
            autoSync = false // 禁用自动同步
        )
        
        assertTrue("禁用同步交易创建成功", noSyncResult is BaseResult.Success)
        val noSyncTransaction = (noSyncResult as BaseResult.Success).data
        assertEquals("没有同步关系", 0, noSyncTransaction.syncedRelations.size)
        
        // 验证未调用同步方法
        coVerify(exactly = 0) { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), "parent") }
        
        // ============== 测试场景：参数验证和错误处理 ==============
        
        // 测试空记账簿ID
        val emptyIdResult = manageLedgerLinkUseCase.createLedgerLink("", "child")
        assertTrue("空ID应返回错误", emptyIdResult is BaseResult.Error)
        
        // 测试相同记账簿ID
        val sameIdResult = manageLedgerLinkUseCase.createLedgerLink("parent", "parent")
        assertTrue("相同ID应返回错误", sameIdResult is BaseResult.Error)
        
        // 测试无效交易金额
        val zeroAmountResult = createLinkedTransactionUseCase.createLinkedTransaction(
            primaryLedgerId = "parent",
            accountId = testAccountId,
            amountCents = 0,
            categoryId = testCategoryId
        )
        assertTrue("零金额应返回错误", zeroAmountResult is BaseResult.Error)
        
        println("✅ 边界情况测试全部通过！")
    }

    @Test
    fun `性能和批量操作测试`() = runTest {
        
        // ============== 测试批量交易创建 ==============
        
        val parentLedger = createTestLedger("batch_parent", "批量测试记账簿", testUserId)
        coEvery { ledgerRepository.getLedgerById("batch_parent") } returns BaseResult.Success(parentLedger)
        coEvery { transactionRepository.addTransaction(any(), any(), any(), any(), any(), any(), any()) } returns 
            BaseResult.Success("batch_trans_1") andThen
            BaseResult.Success("batch_trans_2") andThen
            BaseResult.Success("batch_trans_3")
        coEvery { transactionLedgerRelationDao.insertRelation(any()) } just Runs
        coEvery { syncTransactionUseCase.syncTransactionToLinkedLedgers(any(), any()) } returns 
            BaseResult.Success(emptyList())
        
        val batchRequests = listOf(
            CreateTransactionRequest("batch_parent", testAccountId, 1000, testCategoryId, "交易1"),
            CreateTransactionRequest("batch_parent", testAccountId, 2000, testCategoryId, "交易2"),
            CreateTransactionRequest("batch_parent", testAccountId, 3000, testCategoryId, "交易3")
        )
        
        val batchResult = createLinkedTransactionUseCase.batchCreateLinkedTransactions(batchRequests)
        
        assertTrue("批量创建成功", batchResult is BaseResult.Success)
        val batch = (batchResult as BaseResult.Success).data
        assertEquals("成功创建3笔交易", 3, batch.successCount)
        assertEquals("无失败交易", 0, batch.errorCount)
        assertEquals("成功率100%", 1.0f, batch.getSuccessRate())
        assertTrue("全部成功", batch.isAllSuccess())
        
        // ============== 测试部分失败的批量操作 ==============
        
        val mixedRequests = listOf(
            CreateTransactionRequest("batch_parent", testAccountId, 1000, testCategoryId, "正常交易"),
            CreateTransactionRequest("", testAccountId, 2000, testCategoryId, "无效交易") // 无效记账簿ID
        )
        
        val mixedResult = createLinkedTransactionUseCase.batchCreateLinkedTransactions(mixedRequests)
        
        assertTrue("混合结果创建成功", mixedResult is BaseResult.Success)
        val mixed = (mixedResult as BaseResult.Success).data
        assertEquals("成功创建1笔交易", 1, mixed.successCount)
        assertEquals("失败1笔交易", 1, mixed.errorCount)
        assertEquals("成功率50%", 0.5f, mixed.getSuccessRate())
        assertFalse("不是全部成功", mixed.isAllSuccess())
        
        println("✅ 性能和批量操作测试全部通过！")
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

    private fun createTestTransaction(
        id: String,
        ledgerId: String,
        amountCents: Int
    ): Transaction {
        return Transaction(
            id = id,
            accountId = testAccountId,
            categoryId = testCategoryId,
            ledgerId = ledgerId,
            amountCents = amountCents,
            note = "Test transaction",
            transactionDate = testInstant,
            isIncome = false,
            userId = testUserId,
            createdAt = testInstant,
            updatedAt = testInstant
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
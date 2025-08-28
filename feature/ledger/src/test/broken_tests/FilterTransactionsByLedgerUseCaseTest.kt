package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.LedgerLinkRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionLedgerRelationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * FilterTransactionsByLedgerUseCase单元测试
 * 测试交易过滤逻辑和联动关系处理
 */
class FilterTransactionsByLedgerUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var ledgerLinkRepository: LedgerLinkRepository
    private lateinit var transactionLedgerRelationRepository: TransactionLedgerRelationRepository
    private lateinit var useCase: FilterTransactionsByLedgerUseCase
    
    private val testLedgerId = "ledger_001"
    private val parentLedgerId = "parent_ledger_001"
    private val childLedgerId = "child_ledger_001"
    private val testTransactionId = "transaction_001"
    private val testInstant = Clock.System.now()

    @Before
    fun setup() {
        transactionRepository = mockk()
        ledgerLinkRepository = mockk()
        transactionLedgerRelationRepository = mockk()
        
        useCase = FilterTransactionsByLedgerUseCase(
            transactionRepository,
            ledgerLinkRepository,
            transactionLedgerRelationRepository
        )
    }

    @Test
    fun `getFilteredTransactions - LOCAL_ONLY模式只返回原始交易`() = runTest {
        // Given: 记账簿有原始交易和同步交易
        val originalTransaction = createTestTransaction("trans_001", 10000)
        val syncedTransaction = createTestTransaction("trans_002", 5000)
        
        val relations = listOf(
            createTestRelation("trans_001", testLedgerId, RelationType.PRIMARY),
            createTestRelation("trans_002", testLedgerId, RelationType.SYNCED_FROM_PARENT, parentLedgerId)
        )
        
        coEvery { transactionRepository.getTransactionsByLedger(testLedgerId) } returns 
            flowOf(listOf(originalTransaction, syncedTransaction))
        coEvery { ledgerLinkRepository.getAllLinksForLedger(testLedgerId) } returns flowOf(emptyList())
        coEvery { transactionLedgerRelationRepository.getRelationsByLedger(testLedgerId) } returns 
            flowOf(relations)

        // When: 使用LOCAL_ONLY模式过滤
        val result = useCase.getFilteredTransactions(
            testLedgerId, 
            TransactionFilterMode.LOCAL_ONLY
        ).first()

        // Then: 只返回原始交易，过滤掉同步交易
        assertEquals(1, result.size)
        assertEquals("trans_001", result[0].transaction.id)
        assertEquals(TransactionSyncType.PRIMARY, result[0].syncType)
        assertEquals(testLedgerId, result[0].sourceLedgerId)
    }

    @Test
    fun `getFilteredTransactions - LOCAL_WITH_SYNCED模式返回所有交易`() = runTest {
        // Given: 记账簿有原始交易和同步交易
        val originalTransaction = createTestTransaction("trans_001", 10000)
        val syncedTransaction = createTestTransaction("trans_002", 5000)
        
        val relations = listOf(
            createTestRelation("trans_001", testLedgerId, RelationType.PRIMARY),
            createTestRelation("trans_002", testLedgerId, RelationType.SYNCED_FROM_PARENT, parentLedgerId)
        )
        
        coEvery { transactionRepository.getTransactionsByLedger(testLedgerId) } returns 
            flowOf(listOf(originalTransaction, syncedTransaction))
        coEvery { ledgerLinkRepository.getAllLinksForLedger(testLedgerId) } returns flowOf(emptyList())
        coEvery { transactionLedgerRelationRepository.getRelationsByLedger(testLedgerId) } returns 
            flowOf(relations)

        // When: 使用LOCAL_WITH_SYNCED模式过滤
        val result = useCase.getFilteredTransactions(
            testLedgerId, 
            TransactionFilterMode.LOCAL_WITH_SYNCED
        ).first()

        // Then: 返回所有交易，正确标识同步状态
        assertEquals(2, result.size)
        
        val primaryTransaction = result.find { it.transaction.id == "trans_001" }!!
        assertEquals(TransactionSyncType.PRIMARY, primaryTransaction.syncType)
        assertEquals(testLedgerId, primaryTransaction.sourceLedgerId)
        
        val syncedTransactionResult = result.find { it.transaction.id == "trans_002" }!!
        assertEquals(TransactionSyncType.SYNCED, syncedTransactionResult.syncType)
        assertEquals(parentLedgerId, syncedTransactionResult.sourceLedgerId)
    }

    @Test
    fun `getTransactionSyncStatus - 正确识别PRIMARY交易状态`() = runTest {
        // Given: 交易在记账簿中有PRIMARY关系
        val relations = listOf(
            createTestRelation(testTransactionId, testLedgerId, RelationType.PRIMARY)
        )
        
        coEvery { transactionLedgerRelationRepository.getRelationsByTransaction(testTransactionId) } returns 
            flowOf(relations)

        // When: 获取同步状态
        val result = useCase.getTransactionSyncStatus(testTransactionId, testLedgerId)

        // Then: 返回PRIMARY状态
        assertTrue(result is BaseResult.Success)
        assertEquals(TransactionSyncType.PRIMARY, (result as BaseResult.Success).data)
    }

    @Test
    fun `getTransactionSyncStatus - 正确识别SYNCED交易状态`() = runTest {
        // Given: 交易在记账簿中有SYNCED关系
        val relations = listOf(
            createTestRelation(testTransactionId, testLedgerId, RelationType.SYNCED_FROM_PARENT, parentLedgerId)
        )
        
        coEvery { transactionLedgerRelationRepository.getRelationsByTransaction(testTransactionId) } returns 
            flowOf(relations)

        // When: 获取同步状态
        val result = useCase.getTransactionSyncStatus(testTransactionId, testLedgerId)

        // Then: 返回SYNCED状态
        assertTrue(result is BaseResult.Success)
        assertEquals(TransactionSyncType.SYNCED, (result as BaseResult.Success).data)
    }

    @Test
    fun `getTransactionSyncStatus - 无关联时返回UNRELATED状态`() = runTest {
        // Given: 交易在记账簿中无关联关系
        coEvery { transactionLedgerRelationRepository.getRelationsByTransaction(testTransactionId) } returns 
            flowOf(emptyList())

        // When: 获取同步状态
        val result = useCase.getTransactionSyncStatus(testTransactionId, testLedgerId)

        // Then: 返回UNRELATED状态
        assertTrue(result is BaseResult.Success)
        assertEquals(TransactionSyncType.UNRELATED, (result as BaseResult.Success).data)
    }

    @Test
    fun `getTransactionSyncNetwork - 正确返回交易关联的记账簿列表`() = runTest {
        // Given: 交易关联到多个记账簿
        val relations = listOf(
            createTestRelation(testTransactionId, "ledger_001", RelationType.PRIMARY),
            createTestRelation(testTransactionId, "ledger_002", RelationType.SYNCED_FROM_PARENT, "ledger_001"),
            createTestRelation(testTransactionId, "ledger_003", RelationType.SYNCED_FROM_PARENT, "ledger_001")
        )
        
        coEvery { transactionLedgerRelationRepository.getRelationsByTransaction(testTransactionId) } returns 
            flowOf(relations)

        // When: 获取同步网络
        val result = useCase.getTransactionSyncNetwork(testTransactionId)

        // Then: 返回所有关联记账簿ID
        assertTrue(result is BaseResult.Success)
        val ledgerIds = (result as BaseResult.Success).data
        assertEquals(3, ledgerIds.size)
        assertTrue(ledgerIds.contains("ledger_001"))
        assertTrue(ledgerIds.contains("ledger_002"))
        assertTrue(ledgerIds.contains("ledger_003"))
    }

    @Test
    fun `getFilteredTransactions - 异常情况下返回错误结果`() = runTest {
        // Given: Repository抛出异常
        coEvery { transactionRepository.getTransactionsByLedger(testLedgerId) } throws 
            RuntimeException("Database error")
        coEvery { ledgerLinkRepository.getAllLinksForLedger(testLedgerId) } returns flowOf(emptyList())
        coEvery { transactionLedgerRelationRepository.getRelationsByLedger(testLedgerId) } returns 
            flowOf(emptyList())

        // When & Then: 应该传播异常（由于使用Flow，异常会在collect时抛出）
        try {
            useCase.getFilteredTransactions(testLedgerId).first()
            assert(false) { "应该抛出异常" }
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }

    @Test
    fun `getTransactionSyncStatus - 异常情况下返回错误结果`() = runTest {
        // Given: Repository抛出异常
        coEvery { transactionLedgerRelationRepository.getRelationsByTransaction(testTransactionId) } throws 
            RuntimeException("Database error")

        // When: 获取同步状态
        val result = useCase.getTransactionSyncStatus(testTransactionId, testLedgerId)

        // Then: 返回错误结果
        assertTrue(result is BaseResult.Error)
        assertTrue((result as BaseResult.Error).exception.message!!.contains("Database error"))
    }

    // === 测试工具方法 ===

    private fun createTestTransaction(id: String, amountCents: Int): Transaction {
        return Transaction(
            id = id,
            accountId = "account_001",
            categoryId = "category_001",
            ledgerId = testLedgerId,
            amountCents = amountCents,
            note = "Test transaction",
            transactionDate = testInstant,
            isIncome = false,
            userId = "user_001",
            createdAt = testInstant,
            updatedAt = testInstant
        )
    }

    private fun createTestRelation(
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

    private fun createTestLedgerLink(
        id: String,
        parentLedgerId: String,
        childLedgerId: String,
        syncMode: SyncMode = SyncMode.BIDIRECTIONAL
    ): LedgerLink {
        return LedgerLink(
            id = id,
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId,
            syncMode = syncMode,
            autoSyncEnabled = true,
            createdAt = testInstant,
            updatedAt = testInstant,
            isActive = true
        )
    }
}
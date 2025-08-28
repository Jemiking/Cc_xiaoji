package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.RelationType
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.TransactionLedgerRelation
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionLedgerRelationDao
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * 创建联动交易用例
 * 
 * 处理在记账簿间创建联动交易的业务逻辑，包括：
 * - 创建原始交易
 * - 创建PRIMARY关联关系
 * - 自动同步到相关记账簿
 */
class CreateLinkedTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ledgerRepository: LedgerRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionLedgerRelationDao: TransactionLedgerRelationDao,
    private val syncTransactionUseCase: SyncTransactionUseCase,
    private val userApi: UserApi
) {
    
    /**
     * 创建联动交易
     * 
     * @param primaryLedgerId 主记账簿ID（交易的原始记账簿）
     * @param accountId 账户ID
     * @param amountCents 金额（分为单位）
     * @param categoryId 分类ID
     * @param note 备注
     * @param transactionDate 交易时间
     * @param location 交易地点
     * @param autoSync 是否自动同步到关联记账簿
     * @param specificTargetLedgers 指定的目标记账簿列表（如果为空，则根据联动规则自动确定）
     */
    suspend fun createLinkedTransaction(
        primaryLedgerId: String,
        accountId: String,
        amountCents: Int,
        categoryId: String,
        note: String? = null,
        transactionDate: Instant? = null,
        location: com.ccxiaoji.feature.ledger.domain.model.LocationData? = null,
        autoSync: Boolean = true,
        specificTargetLedgers: List<String> = emptyList()
    ): BaseResult<CreateLinkedTransactionResult> {
        
        // 参数验证
        if (primaryLedgerId.isBlank()) {
            return BaseResult.Error(Exception("主记账簿ID不能为空"))
        }
        
        if (accountId.isBlank()) {
            return BaseResult.Error(Exception("账户ID不能为空"))
        }
        
        if (categoryId.isBlank()) {
            return BaseResult.Error(Exception("分类ID不能为空"))
        }
        
        if (amountCents == 0) {
            return BaseResult.Error(Exception("交易金额不能为零"))
        }
        
        try {
            println("🔍 [CreateLinkedTransaction] 开始外键存在性验证")
            
            // 1. 验证用户ID
            val currentUserId = userApi.getCurrentUserId()
            println("🔍 [CreateLinkedTransaction] 当前用户ID: '$currentUserId'")
            if (currentUserId.isBlank()) {
                println("❌ [CreateLinkedTransaction] 用户ID为空")
                return BaseResult.Error(Exception("用户未登录或用户ID无效"))
            }
            
            // 2. 验证主记账簿是否存在且有权限
            println("🔍 [CreateLinkedTransaction] 验证记账簿存在性: '$primaryLedgerId'")
            val ledgerResult = ledgerRepository.getLedgerById(primaryLedgerId)
            if (ledgerResult is BaseResult.Error) {
                println("❌ [CreateLinkedTransaction] 记账簿不存在: $primaryLedgerId")
                return BaseResult.Error(Exception("主记账簿不存在: $primaryLedgerId"))
            }
            
            val ledger = (ledgerResult as BaseResult.Success).data
            if (!ledger.isActive) {
                println("❌ [CreateLinkedTransaction] 记账簿未激活: $primaryLedgerId")
                return BaseResult.Error(Exception("主记账簿未激活: $primaryLedgerId"))
            }
            println("✅ [CreateLinkedTransaction] 记账簿验证通过: '${ledger.name}' ($primaryLedgerId)")
            
            // 3. 验证账户是否存在
            println("🔍 [CreateLinkedTransaction] 验证账户存在性: '$accountId'")
            val accounts = accountRepository.getAccounts().first() // 使用first()进行一次性获取
            val accountExists = accounts.any { it.id == accountId }
            if (accountExists) {
                val account = accounts.find { it.id == accountId }
                println("✅ [CreateLinkedTransaction] 账户验证通过: '${account?.name}' ($accountId)")
            } else {
                println("❌ [CreateLinkedTransaction] 账户不存在: $accountId")
                println("🔍 [CreateLinkedTransaction] 可用账户: ${accounts.map { "${it.name}(${it.id})" }}")
                return BaseResult.Error(Exception("账户不存在: $accountId"))
            }
            
            // 4. 验证分类是否存在
            println("🔍 [CreateLinkedTransaction] 验证分类存在性: '$categoryId'")
            val category = categoryRepository.getCategoryById(categoryId)
            if (category == null) {
                println("❌ [CreateLinkedTransaction] 分类不存在: $categoryId")
                return BaseResult.Error(Exception("分类不存在: $categoryId"))
            }
            println("✅ [CreateLinkedTransaction] 分类验证通过: '${category.name}' ($categoryId)")
            
            println("✅ [CreateLinkedTransaction] 所有外键验证通过，开始创建交易")
            
            // 预生成交易ID，确保整个流程使用同一个ID
            val transactionId = UUID.randomUUID().toString()
            val now = Clock.System.now()
            println("🔍 [CreateLinkedTransaction] 预生成交易ID: '$transactionId'")
            
            val transaction = Transaction(
                id = transactionId,
                accountId = accountId,
                amountCents = amountCents,
                categoryId = categoryId,
                note = note?.trim(),
                ledgerId = primaryLedgerId,
                createdAt = now,
                updatedAt = now,
                transactionDate = transactionDate,
                location = location
            )
            
            // 创建交易记录
            println("🔍 [CreateLinkedTransaction] 调用transactionRepository.addTransaction")
            println("🔍 [CreateLinkedTransaction] 参数: amountCents=$amountCents, categoryId='$categoryId', accountId='$accountId', ledgerId='$primaryLedgerId', transactionId='$transactionId'")
            val createResult = transactionRepository.addTransaction(
                amountCents = amountCents,
                categoryId = categoryId,
                note = note,
                accountId = accountId,
                ledgerId = primaryLedgerId,
                transactionDate = transactionDate,
                location = location,
                transactionId = transactionId // 传入预生成的ID
            )
            val actualTransactionId = when (createResult) {
                is BaseResult.Success -> {
                    val returnedId = createResult.data
                    println("✅ [CreateLinkedTransaction] 交易创建成功，返回ID: '$returnedId'")
                    if (returnedId != transactionId) {
                        println("⚠️ [CreateLinkedTransaction] 警告: 返回ID与预期ID不一致")
                    }
                    returnedId
                }
                is BaseResult.Error -> {
                    println("❌ [CreateLinkedTransaction] 创建交易失败: ${createResult.exception.message}")
                    println("❌ [CreateLinkedTransaction] 错误详情: ${createResult.exception}")
                    return BaseResult.Error(Exception("创建交易失败: ${createResult.exception.message}"))
                }
            }
            
            // 创建交易对象用于返回结果
            val createdTransaction = transaction.copy(id = actualTransactionId)
            
            // 创建PRIMARY关联关系
            println("🔍 [CreateLinkedTransaction] 创建PRIMARY关联关系")
            val primaryRelation = TransactionLedgerRelation(
                id = UUID.randomUUID().toString(),
                transactionId = actualTransactionId,
                ledgerId = primaryLedgerId,
                relationType = RelationType.PRIMARY,
                syncSourceLedgerId = null,
                createdAt = now
            )
            println("🔍 [CreateLinkedTransaction] PRIMARY关系: transactionId='$actualTransactionId', ledgerId='$primaryLedgerId'")
            
            try {
                transactionLedgerRelationDao.insertRelation(primaryRelation.toEntity())
                println("✅ [CreateLinkedTransaction] PRIMARY关联关系创建成功")
            } catch (e: Exception) {
                println("❌ [CreateLinkedTransaction] PRIMARY关联关系创建失败: ${e.message}")
                throw e
            }
            
            // 自动同步到相关记账簿
            val syncedRelations = mutableListOf<TransactionLedgerRelation>()
            
            if (autoSync) {
                val syncResult = if (specificTargetLedgers.isNotEmpty()) {
                    // 同步到指定的记账簿
                    syncToSpecificLedgers(createdTransaction, primaryLedgerId, specificTargetLedgers)
                } else {
                    // 根据联动规则自动同步
                    syncTransactionUseCase.syncTransactionToLinkedLedgers(createdTransaction, primaryLedgerId)
                }
                
                if (syncResult is BaseResult.Success) {
                    syncedRelations.addAll(syncResult.data)
                }
                // 注意：这里不返回同步错误，因为主交易已经创建成功
            }
            
            val result = CreateLinkedTransactionResult(
                transaction = createdTransaction,
                primaryRelation = primaryRelation,
                syncedRelations = syncedRelations,
                syncErrors = emptyList() // TODO: 收集同步过程中的错误
            )
            
            return BaseResult.Success(result)
            
        } catch (e: Exception) {
            return BaseResult.Error(Exception("创建联动交易失败: ${e.message}"))
        }
    }
    
    /**
     * 在指定记账簿中创建交易（用于从总记账簿创建到特定子记账簿）
     */
    suspend fun createTransactionInSpecificLedger(
        targetLedgerId: String,
        accountId: String,
        amountCents: Int,
        categoryId: String,
        note: String? = null,
        transactionDate: Instant? = null,
        location: com.ccxiaoji.feature.ledger.domain.model.LocationData? = null,
        linkToParent: Boolean = true
    ): BaseResult<CreateLinkedTransactionResult> {
        
        // 参数验证
        if (targetLedgerId.isBlank()) {
            return BaseResult.Error(Exception("目标记账簿ID不能为空"))
        }
        
        try {
            // 验证目标记账簿
            val ledgerResult = ledgerRepository.getLedgerById(targetLedgerId)
            if (ledgerResult is BaseResult.Error) {
                return BaseResult.Error(Exception("目标记账簿不存在"))
            }
            
            val ledger = (ledgerResult as BaseResult.Success).data
            if (!ledger.isActive) {
                return BaseResult.Error(Exception("目标记账簿未激活"))
            }
            
            // 在目标记账簿中创建交易
            val createResult = createLinkedTransaction(
                primaryLedgerId = targetLedgerId,
                accountId = accountId,
                amountCents = amountCents,
                categoryId = categoryId,
                note = note,
                transactionDate = transactionDate,
                location = location,
                autoSync = linkToParent,
                specificTargetLedgers = emptyList()
            )
            
            return createResult
            
        } catch (e: Exception) {
            return BaseResult.Error(Exception("在指定记账簿中创建交易失败: ${e.message}"))
        }
    }
    
    /**
     * 批量创建联动交易
     */
    suspend fun batchCreateLinkedTransactions(
        transactions: List<CreateTransactionRequest>
    ): BaseResult<BatchCreateLinkedTransactionResult> {
        
        if (transactions.isEmpty()) {
            return BaseResult.Error(Exception("交易列表不能为空"))
        }
        
        val successResults = mutableListOf<CreateLinkedTransactionResult>()
        val errors = mutableListOf<BatchTransactionError>()
        
        transactions.forEachIndexed { index, request ->
            try {
                val result = createLinkedTransaction(
                    primaryLedgerId = request.primaryLedgerId,
                    accountId = request.accountId,
                    amountCents = request.amountCents,
                    categoryId = request.categoryId,
                    note = request.note,
                    transactionDate = request.transactionDate,
                    location = request.location,
                    autoSync = request.autoSync,
                    specificTargetLedgers = request.specificTargetLedgers
                )
                
                when (result) {
                    is BaseResult.Success -> {
                        successResults.add(result.data)
                    }
                    is BaseResult.Error -> {
                        errors.add(
                            BatchTransactionError(
                                index = index,
                                request = request,
                                error = result.exception.message ?: "未知错误"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                errors.add(
                    BatchTransactionError(
                        index = index,
                        request = request,
                        error = "批量创建异常: ${e.message}"
                    )
                )
            }
        }
        
        val batchResult = BatchCreateLinkedTransactionResult(
            successCount = successResults.size,
            errorCount = errors.size,
            successResults = successResults,
            errors = errors
        )
        
        return BaseResult.Success(batchResult)
    }
    
    /**
     * 验证交易创建请求的有效性
     */
    suspend fun validateTransactionRequest(
        primaryLedgerId: String,
        accountId: String,
        categoryId: String,
        amountCents: Int
    ): BaseResult<Unit> {
        
        // 验证记账簿
        val ledgerResult = ledgerRepository.getLedgerById(primaryLedgerId)
        if (ledgerResult is BaseResult.Error) {
            return BaseResult.Error(Exception("记账簿不存在"))
        }
        
        // TODO: 验证账户是否存在
        // TODO: 验证分类是否存在
        // TODO: 验证金额是否在合理范围内
        
        return BaseResult.Success(Unit)
    }
    
    /**
     * 同步到指定的记账簿列表
     */
    private suspend fun syncToSpecificLedgers(
        transaction: Transaction,
        sourceLedgerId: String,
        targetLedgerIds: List<String>
    ): BaseResult<List<TransactionLedgerRelation>> {
        
        val syncedRelations = mutableListOf<TransactionLedgerRelation>()
        
        for (targetLedgerId in targetLedgerIds) {
            if (targetLedgerId != sourceLedgerId) {
                val syncResult = syncTransactionUseCase.manualSyncTransaction(
                    transactionId = transaction.id,
                    sourceLedgerId = sourceLedgerId,
                    targetLedgerId = targetLedgerId
                )
                
                if (syncResult is BaseResult.Success) {
                    syncedRelations.add(syncResult.data)
                }
                // 继续处理其他记账簿，不因单个失败而中断
            }
        }
        
        return BaseResult.Success(syncedRelations)
    }
}

/**
 * 创建联动交易的结果
 */
data class CreateLinkedTransactionResult(
    val transaction: Transaction,
    val primaryRelation: TransactionLedgerRelation,
    val syncedRelations: List<TransactionLedgerRelation>,
    val syncErrors: List<String>
) {
    /**
     * 获取交易存在的记账簿总数
     */
    fun getTotalLedgerCount(): Int = 1 + syncedRelations.size
    
    /**
     * 检查是否有同步错误
     */
    fun hasSyncErrors(): Boolean = syncErrors.isNotEmpty()
    
    /**
     * 获取同步成功的记账簿数量
     */
    fun getSyncSuccessCount(): Int = syncedRelations.size
}

/**
 * 创建交易请求
 */
data class CreateTransactionRequest(
    val primaryLedgerId: String,
    val accountId: String,
    val amountCents: Int,
    val categoryId: String,
    val note: String? = null,
    val transactionDate: Instant? = null,
    val location: com.ccxiaoji.feature.ledger.domain.model.LocationData? = null,
    val autoSync: Boolean = true,
    val specificTargetLedgers: List<String> = emptyList()
)

/**
 * 批量创建联动交易的结果
 */
data class BatchCreateLinkedTransactionResult(
    val successCount: Int,
    val errorCount: Int,
    val successResults: List<CreateLinkedTransactionResult>,
    val errors: List<BatchTransactionError>
) {
    /**
     * 获取总处理数量
     */
    fun getTotalCount(): Int = successCount + errorCount
    
    /**
     * 获取成功率
     */
    fun getSuccessRate(): Float {
        val total = getTotalCount()
        return if (total > 0) successCount.toFloat() / total.toFloat() else 0f
    }
    
    /**
     * 检查是否所有交易都创建成功
     */
    fun isAllSuccess(): Boolean = errorCount == 0
}

/**
 * 批量交易错误信息
 */
data class BatchTransactionError(
    val index: Int,
    val request: CreateTransactionRequest,
    val error: String
)

/**
 * 扩展函数：转换为Entity
 */
private fun TransactionLedgerRelation.toEntity(): com.ccxiaoji.feature.ledger.data.local.entity.TransactionLedgerRelationEntity {
    return com.ccxiaoji.feature.ledger.data.local.entity.TransactionLedgerRelationEntity(
        id = id,
        transactionId = transactionId,
        ledgerId = ledgerId,
        relationType = relationType.name,
        syncSourceLedgerId = syncSourceLedgerId,
        createdAt = createdAt
    )
}
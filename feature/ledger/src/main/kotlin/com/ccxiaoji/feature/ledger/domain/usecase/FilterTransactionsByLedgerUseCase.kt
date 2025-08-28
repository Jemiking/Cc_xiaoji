package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.TransactionWithSyncInfo
import com.ccxiaoji.feature.ledger.domain.model.TransactionSyncType
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.LedgerLinkRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionLedgerRelationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交易过滤规则
 */
enum class TransactionFilterMode {
    /**
     * 仅显示本记账簿的原始交易（不包含同步来的交易）
     */
    LOCAL_ONLY,
    
    /**
     * 显示本记账簿所有交易（包含同步来的交易）
     */
    LOCAL_WITH_SYNCED,
    
    /**
     * 显示所有相关联的交易（本记账簿 + 联动记账簿的交易）
     */
    ALL_RELATED
}

/**
 * 根据记账簿联动关系过滤交易的业务逻辑
 * 
 * 功能：
 * - 根据不同的过滤模式显示交易
 * - 标识交易的同步状态（原始/同步来的）
 * - 处理联动记账簿的交易关系
 */
@Singleton
class FilterTransactionsByLedgerUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ledgerLinkRepository: LedgerLinkRepository,
    private val transactionLedgerRelationRepository: TransactionLedgerRelationRepository
) {
    
    /**
     * 获取指定记账簿的交易列表（支持联动关系过滤）
     * 
     * @param ledgerId 目标记账簿ID
     * @param filterMode 过滤模式
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 带同步信息的交易列表Flow
     */
    fun getFilteredTransactions(
        ledgerId: String,
        filterMode: TransactionFilterMode = TransactionFilterMode.LOCAL_WITH_SYNCED,
        startDate: kotlinx.datetime.Instant? = null,
        endDate: kotlinx.datetime.Instant? = null
    ): Flow<List<TransactionWithSyncInfo>> {
        return combine(
            transactionRepository.getTransactionsByLedger(ledgerId),
            ledgerLinkRepository.getAllLinksForLedger(ledgerId),
            transactionLedgerRelationRepository.getRelationsByLedger(ledgerId)
        ) { baseTransactions, links, relations ->
            
            when (filterMode) {
                TransactionFilterMode.LOCAL_ONLY -> {
                    // 只返回本记账簿的原始交易
                    baseTransactions
                        .filter { transaction ->
                            // 检查是否为原始交易（不在relation表中作为SYNCED出现）
                            relations.none { relation -> 
                                relation.transactionId == transaction.id && 
                                relation.relationType.name == "SYNCED" &&
                                relation.ledgerId == ledgerId
                            }
                        }
                        .map { transaction ->
                            TransactionWithSyncInfo(
                                transaction = transaction,
                                syncType = TransactionSyncType.PRIMARY,
                                sourceLedgerId = ledgerId,
                                targetLedgerIds = getTargetLedgerIds(transaction.id, relations)
                            )
                        }
                }
                
                TransactionFilterMode.LOCAL_WITH_SYNCED -> {
                    // 返回本记账簿所有交易（包含同步来的）
                    baseTransactions.map { transaction ->
                        val syncType = determineSyncType(transaction.id, ledgerId, relations)
                        val sourceLedger = if (syncType == TransactionSyncType.SYNCED) {
                            findSourceLedger(transaction.id, ledgerId, relations)
                        } else {
                            ledgerId
                        }
                        
                        TransactionWithSyncInfo(
                            transaction = transaction,
                            syncType = syncType,
                            sourceLedgerId = sourceLedger,
                            targetLedgerIds = getTargetLedgerIds(transaction.id, relations)
                        )
                    }
                }
                
                TransactionFilterMode.ALL_RELATED -> {
                    // 返回所有相关联记账簿的交易
                    val relatedLedgerIds = links.flatMap { link ->
                        listOf(link.parentLedgerId, link.childLedgerId)
                    }.distinct()
                    
                    // 获取所有相关记账簿的交易ID
                    val allRelatedTransactionIds = relations
                        .filter { relation -> relatedLedgerIds.contains(relation.ledgerId) }
                        .map { it.transactionId }
                        .distinct()
                    
                    // 获取所有相关交易的详细信息
                    // 注意：这里需要Repository支持批量查询，暂时使用现有方法
                    baseTransactions
                        .filter { transaction -> allRelatedTransactionIds.contains(transaction.id) }
                        .map { transaction ->
                            val syncType = determineSyncType(transaction.id, ledgerId, relations)
                            val sourceLedger = if (syncType == TransactionSyncType.SYNCED) {
                                findSourceLedger(transaction.id, ledgerId, relations)
                            } else {
                                ledgerId
                            }
                            
                            TransactionWithSyncInfo(
                                transaction = transaction,
                                syncType = syncType,
                                sourceLedgerId = sourceLedger,
                                targetLedgerIds = getTargetLedgerIds(transaction.id, relations)
                            )
                        }
                }
            }
        }
    }
    
    /**
     * 检查交易在指定记账簿中的同步状态
     */
    suspend fun getTransactionSyncStatus(
        transactionId: String,
        ledgerId: String
    ): BaseResult<TransactionSyncType> {
        return try {
            val relations = transactionLedgerRelationRepository.getRelationsByTransaction(transactionId).first()
            val targetRelation = relations.find { it.ledgerId == ledgerId }
            
            val syncType = when {
                targetRelation == null -> TransactionSyncType.UNRELATED
                targetRelation.relationType.name == "PRIMARY" -> TransactionSyncType.PRIMARY
                targetRelation.relationType.name == "SYNCED" -> TransactionSyncType.SYNCED
                else -> TransactionSyncType.UNRELATED
            }
            
            BaseResult.Success(syncType)
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取交易同步状态失败: ${e.message}"))
        }
    }
    
    /**
     * 获取交易的同步网络信息
     */
    suspend fun getTransactionSyncNetwork(
        transactionId: String
    ): BaseResult<List<String>> {
        return try {
            val relations = transactionLedgerRelationRepository.getRelationsByTransaction(transactionId).first()
            val linkedLedgerIds = relations.map { it.ledgerId }
            BaseResult.Success(linkedLedgerIds)
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取交易同步网络失败: ${e.message}"))
        }
    }
    
    /**
     * 确定交易在特定记账簿中的同步类型
     */
    private fun determineSyncType(
        transactionId: String,
        ledgerId: String,
        relations: List<com.ccxiaoji.feature.ledger.domain.model.TransactionLedgerRelation>
    ): TransactionSyncType {
        val targetRelation = relations.find { 
            it.transactionId == transactionId && it.ledgerId == ledgerId 
        }
        
        return when {
            targetRelation == null -> TransactionSyncType.UNRELATED
            targetRelation.relationType.name == "PRIMARY" -> TransactionSyncType.PRIMARY
            targetRelation.relationType.name == "SYNCED" -> TransactionSyncType.SYNCED
            else -> TransactionSyncType.UNRELATED
        }
    }
    
    /**
     * 查找同步交易的源记账簿
     */
    private fun findSourceLedger(
        transactionId: String,
        currentLedgerId: String,
        relations: List<com.ccxiaoji.feature.ledger.domain.model.TransactionLedgerRelation>
    ): String {
        // 找到PRIMARY关系的记账簿ID
        val primaryRelation = relations.find { 
            it.transactionId == transactionId && it.relationType.name == "PRIMARY"
        }
        return primaryRelation?.ledgerId ?: currentLedgerId
    }
    
    /**
     * 获取交易的目标记账簿ID列表
     */
    private fun getTargetLedgerIds(
        transactionId: String,
        relations: List<com.ccxiaoji.feature.ledger.domain.model.TransactionLedgerRelation>
    ): List<String> {
        return relations
            .filter { it.transactionId == transactionId && it.relationType.name == "SYNCED" }
            .map { it.ledgerId }
    }
}
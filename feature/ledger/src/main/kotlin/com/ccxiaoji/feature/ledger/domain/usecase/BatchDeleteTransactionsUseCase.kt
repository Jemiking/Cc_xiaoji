package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * 批量删除交易
 */
class BatchDeleteTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        transactionIds: Set<String>
    ): BaseResult<Pair<Int, List<String>>> { // 返回成功数量和已删除的ID列表（用于撤销）
        return try {
            var successCount = 0
            val deletedIds = mutableListOf<String>()
            
            transactionIds.forEach { transactionId ->
                val result = repository.deleteTransaction(transactionId)
                if (result is BaseResult.Success) {
                    successCount++
                    deletedIds.add(transactionId)
                }
            }
            
            BaseResult.Success(successCount to deletedIds)
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
}
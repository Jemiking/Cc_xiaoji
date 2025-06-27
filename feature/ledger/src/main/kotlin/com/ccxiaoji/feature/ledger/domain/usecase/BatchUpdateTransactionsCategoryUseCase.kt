package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * 批量更新交易分类
 */
class BatchUpdateTransactionsCategoryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        transactionIds: Set<String>,
        newCategoryId: String
    ): BaseResult<Int> {
        return try {
            var successCount = 0
            transactionIds.forEach { transactionId ->
                val transaction = repository.getTransactionById(transactionId)
                if (transaction != null) {
                    val updated = transaction.copy(categoryId = newCategoryId)
                    val result = repository.updateTransaction(updated)
                    if (result is BaseResult.Success) {
                        successCount++
                    }
                }
            }
            BaseResult.Success(successCount)
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
}
package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * 删除交易记录用例
 */
class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * 删除指定的交易记录
     * @param transactionId 交易ID
     */
    suspend operator fun invoke(transactionId: String) {
        require(transactionId.isNotBlank()) { "交易ID不能为空" }
        repository.deleteTransaction(transactionId).getOrThrow()
    }
}
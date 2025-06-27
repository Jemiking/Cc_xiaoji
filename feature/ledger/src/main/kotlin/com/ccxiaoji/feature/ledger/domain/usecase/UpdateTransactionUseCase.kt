package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import javax.inject.Inject

/**
 * 更新交易记录用例
 */
class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * 更新交易记录
     * @param transaction 要更新的交易记录
     */
    suspend operator fun invoke(transaction: Transaction) {
        require(transaction.id.isNotBlank()) { "交易ID不能为空" }
        require(transaction.amountCents != 0) { "金额不能为0" }
        
        repository.updateTransaction(transaction).getOrThrow()
    }
}
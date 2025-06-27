package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取交易记录用例
 */
class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * 获取所有交易记录
     */
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getTransactions()
    }
    
    /**
     * 根据账户获取交易记录
     * @param accountId 账户ID
     */
    fun getByAccount(accountId: String): Flow<List<Transaction>> {
        return repository.getTransactionsByAccount(accountId)
    }
    
    /**
     * 根据分类获取交易记录
     * @param categoryId 分类ID
     */
    fun getByCategory(categoryId: String): Flow<List<Transaction>> {
        return repository.getTransactionsByCategory(categoryId)
    }
}
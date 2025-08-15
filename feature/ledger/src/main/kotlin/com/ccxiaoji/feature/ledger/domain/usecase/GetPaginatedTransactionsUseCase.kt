package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 获取分页交易记录的UseCase
 * 用于优化大数据量场景下的性能
 */
class GetPaginatedTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    data class PaginatedResult(
        val transactions: List<Transaction>,
        val hasMore: Boolean,
        val totalCount: Int
    )
    
    /**
     * 获取分页的交易记录
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @param accountId 账户ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    operator fun invoke(
        page: Int,
        pageSize: Int = 20,
        accountId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<BaseResult<PaginatedResult>> {
        return transactionRepository.getTransactionsPaginated(
            offset = page * pageSize,
            limit = pageSize,
            accountId = accountId,
            startDate = startDate,
            endDate = endDate
        ).map { result ->
            when (result) {
                is BaseResult.Success -> {
                    val transactions = result.data.first
                    val totalCount = result.data.second
                    BaseResult.Success(
                        PaginatedResult(
                            transactions = transactions,
                            hasMore = (page + 1) * pageSize < totalCount,
                            totalCount = totalCount
                        )
                    )
                }
                is BaseResult.Error -> BaseResult.Error(result.exception)
            }
        }
    }
}
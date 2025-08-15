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
        android.util.Log.e("LEDGER_DEBUG", "========== UseCase 查询交易记录 ==========")
        android.util.Log.e("LEDGER_DEBUG", "分页参数: page=$page, pageSize=$pageSize")
        android.util.Log.e("LEDGER_DEBUG", "账户ID: $accountId")
        android.util.Log.e("LEDGER_DEBUG", "日期范围: $startDate - $endDate")
        if (startDate != null && endDate != null) {
            android.util.Log.e("LEDGER_DEBUG", "日期转换: 开始=${java.util.Date(startDate)}, 结束=${java.util.Date(endDate)}")
        }
        android.util.Log.e("LEDGER_DEBUG", "offset=${page * pageSize}, limit=$pageSize")
        
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
                    android.util.Log.e("LEDGER_DEBUG", "UseCase 查询成功: 返回 ${transactions.size} 条记录, 总计 $totalCount 条")
                    if (transactions.isNotEmpty()) {
                        val first = transactions.first()
                        android.util.Log.e("LEDGER_DEBUG", "第一条: ID=${first.id}, Amount=${first.amountCents}, Date=${first.createdAt}")
                        android.util.Log.e("LEDGER_DEBUG", "第一条时间: ${java.util.Date(first.createdAt.toEpochMilliseconds())}")
                    } else {
                        android.util.Log.e("LEDGER_DEBUG", "UseCase 警告: 返回空列表！")
                    }
                    BaseResult.Success(
                        PaginatedResult(
                            transactions = transactions,
                            hasMore = (page + 1) * pageSize < totalCount,
                            totalCount = totalCount
                        )
                    )
                }
                is BaseResult.Error -> {
                    android.util.Log.e("LEDGER_DEBUG", "UseCase 查询失败: ${result.exception.message}", result.exception)
                    BaseResult.Error(result.exception)
                }
            }
        }
    }
}
package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * 获取月度统计用例
 */
class GetMonthlyStatsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * 获取指定月份的收入和支出
     * @param year 年份
     * @param month 月份
     * @return Pair<收入金额（分）, 支出金额（分）>
     */
    suspend operator fun invoke(year: Int, month: Int): Pair<Int, Int> {
        if (year <= 0) {
            throw DomainException.ValidationException("年份必须大于0")
        }
        if (month !in 1..12) {
            throw DomainException.ValidationException("月份必须在1-12之间")
        }
        
        return repository.getMonthlyIncomesAndExpenses(year, month).getOrThrow()
    }
}
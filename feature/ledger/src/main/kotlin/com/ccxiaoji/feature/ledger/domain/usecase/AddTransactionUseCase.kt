package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * 添加交易记录用例
 * 处理交易创建的业务逻辑
 */
class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    /**
     * 添加新的交易记录
     * @param amountCents 金额（分）
     * @param categoryId 分类ID
     * @param note 备注（可选）
     * @param accountId 账户ID
     * @return 创建的交易ID
     */
    suspend operator fun invoke(
        amountCents: Int,
        categoryId: String,
        note: String? = null,
        accountId: String
    ): Long {
        // 验证输入
        if (amountCents == 0) {
            throw DomainException.ValidationException("金额不能为0")
        }
        if (categoryId.isBlank()) {
            throw DomainException.ValidationException("分类ID不能为空")
        }
        if (accountId.isBlank()) {
            throw DomainException.ValidationException("账户ID不能为空")
        }
        
        return repository.addTransaction(
            amountCents = amountCents,
            categoryId = categoryId,
            note = note?.trim(),
            accountId = accountId
        ).getOrThrow()
    }
}
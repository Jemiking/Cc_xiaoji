package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import javax.inject.Inject

/**
 * 管理账户用例
 * 处理账户的创建、更新和删除
 */
class ManageAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * 创建新账户
     * @param name 账户名称
     * @param type 账户类型
     * @param initialBalanceCents 初始余额（分）
     * @return 创建的账户ID
     */
    suspend fun createAccount(
        name: String,
        type: String,
        initialBalanceCents: Long = 0L
    ): Long {
        require(name.isNotBlank()) { "账户名称不能为空" }
        require(type.isNotBlank()) { "账户类型不能为空" }
        
        val accountType = AccountType.valueOf(type)
        return repository.createAccount(
            name = name,
            type = accountType,
            initialBalanceCents = initialBalanceCents,
            creditLimitCents = null,
            billingDay = null,
            paymentDueDay = null,
            gracePeriodDays = null
        )
    }
    
    /**
     * 更新账户信息
     * @param account 要更新的账户
     */
    suspend fun updateAccount(account: Account) {
        require(account.id.isNotBlank()) { "账户ID不能为空" }
        require(account.name.isNotBlank()) { "账户名称不能为空" }
        
        repository.updateAccount(account)
    }
    
    /**
     * 删除账户
     * @param accountId 账户ID
     */
    suspend fun deleteAccount(accountId: String) {
        require(accountId.isNotBlank()) { "账户ID不能为空" }
        repository.deleteAccount(accountId)
    }
    
    /**
     * 设置默认账户
     * @param accountId 账户ID
     */
    suspend fun setDefaultAccount(accountId: String) {
        require(accountId.isNotBlank()) { "账户ID不能为空" }
        repository.setDefaultAccount(accountId)
    }
}
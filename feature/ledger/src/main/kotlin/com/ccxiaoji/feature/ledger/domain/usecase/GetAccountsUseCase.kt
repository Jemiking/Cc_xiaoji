package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.model.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取账户列表用例
 */
class GetAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * 获取所有账户
     */
    operator fun invoke(): Flow<List<Account>> {
        return repository.getAccounts()
    }
    
    /**
     * 获取默认账户
     */
    suspend fun getDefaultAccount(): Account? {
        return repository.getDefaultAccount()
    }
}
package com.ccxiaoji.feature.ledger.data.migration

import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据迁移工具
 * 用于修复历史数据问题
 */
@Singleton
class DataMigrationTool @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    
    /**
     * 修复导入的孤儿账户数据
     * 将没有正确账户的交易迁移到默认账户
     */
    suspend fun fixOrphanAccountTransactions(userId: String) {
        android.util.Log.e("DATA_MIGRATION", "========== 开始数据修复 ==========")
        android.util.Log.e("DATA_MIGRATION", "用户ID: $userId")
        
        try {
            // 1. 获取或创建默认账户
            val defaultAccountId = "default_account_$userId"
            var defaultAccount = accountDao.getAccountById(defaultAccountId)
            
            if (defaultAccount == null) {
                // 创建默认账户
                defaultAccount = AccountEntity(
                    id = defaultAccountId,
                    userId = userId,
                    name = "默认账户",
                    type = "CASH",
                    balanceCents = 0,
                    currency = "CNY",
                    icon = "💰",
                    color = "#6200EE",
                    isDefault = true,
                    creditLimitCents = null,
                    billingDay = null,
                    paymentDueDay = null,
                    gracePeriodDays = null,
                    annualFeeAmountCents = null,
                    annualFeeWaiverThresholdCents = null,
                    cashAdvanceLimitCents = null,
                    interestRate = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = false,
                    syncStatus = SyncStatus.SYNCED
                )
                accountDao.insert(defaultAccount)
                android.util.Log.e("DATA_MIGRATION", "创建默认账户: $defaultAccountId")
            } else {
                android.util.Log.e("DATA_MIGRATION", "默认账户已存在: $defaultAccountId")
            }
            
            // 2. 查找孤儿账户（钱迹导入时创建的临时账户）
            val orphanAccountId = "eacd3aae-f896-457f-8e41-4cdf20208d9d"
            val orphanAccount = accountDao.getAccountById(orphanAccountId)
            
            if (orphanAccount != null) {
                android.util.Log.e("DATA_MIGRATION", "找到孤儿账户: $orphanAccountId")
                
                // 3. 获取孤儿账户的所有交易
                val orphanTransactions = transactionDao.getTransactionsByUserSync(userId)
                    .filter { it.accountId == orphanAccountId }
                
                android.util.Log.e("DATA_MIGRATION", "孤儿账户交易数: ${orphanTransactions.size}")
                
                if (orphanTransactions.isNotEmpty()) {
                    // 4. 批量更新交易的账户ID
                    orphanTransactions.forEach { transaction ->
                        val updatedTransaction = transaction.copy(
                            accountId = defaultAccountId,
                            updatedAt = System.currentTimeMillis()
                        )
                        transactionDao.updateTransaction(updatedTransaction)
                    }
                    
                    android.util.Log.e("DATA_MIGRATION", "已迁移 ${orphanTransactions.size} 条交易到默认账户")
                }
                
                // 5. 删除孤儿账户
                accountDao.softDeleteAccount(orphanAccountId, System.currentTimeMillis())
                android.util.Log.e("DATA_MIGRATION", "删除孤儿账户: $orphanAccountId")
            } else {
                android.util.Log.e("DATA_MIGRATION", "未找到孤儿账户，可能是新用户或已修复")
            }
            
            // 6. 确保默认账户标记正确
            accountDao.clearDefaultStatus(userId)
            accountDao.updateAccount(defaultAccount.copy(isDefault = true))
            android.util.Log.e("DATA_MIGRATION", "设置默认账户标记")
            
            // 7. 统计修复结果
            val totalTransactions = transactionDao.getUserTransactionsCount(userId)
            val defaultAccountTransactions = transactionDao.getTransactionsByUserSync(userId)
                .filter { it.accountId == defaultAccountId }.size
            
            android.util.Log.e("DATA_MIGRATION", "========== 修复完成 ==========")
            android.util.Log.e("DATA_MIGRATION", "总交易数: $totalTransactions")
            android.util.Log.e("DATA_MIGRATION", "默认账户交易数: $defaultAccountTransactions")
            
        } catch (e: Exception) {
            android.util.Log.e("DATA_MIGRATION", "数据修复失败: ${e.message}", e)
        }
    }
    
    /**
     * 批量更新账户ID的辅助方法
     * 注意：这个方法需要在TransactionDao中添加
     */
    suspend fun updateTransactionsAccountId(
        oldAccountId: String,
        newAccountId: String,
        userId: String
    ): Int {
        val transactions = transactionDao.getTransactionsByUserSync(userId)
            .filter { it.accountId == oldAccountId }
        
        transactions.forEach { transaction ->
            transactionDao.updateTransaction(
                transaction.copy(
                    accountId = newAccountId,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        
        return transactions.size
    }
}
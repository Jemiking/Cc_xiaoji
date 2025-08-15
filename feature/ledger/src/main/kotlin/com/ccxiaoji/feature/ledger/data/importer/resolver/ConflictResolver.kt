package com.ccxiaoji.feature.ledger.data.importer.resolver

import com.ccxiaoji.feature.ledger.data.local.dao.*
import com.ccxiaoji.feature.ledger.data.local.entity.*
import com.ccxiaoji.feature.ledger.domain.importer.ConflictStrategy
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 冲突处理器
 */
class ConflictResolver @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    
    /**
     * 处理账户冲突
     */
    suspend fun resolveAccountConflict(
        account: AccountEntity,
        strategy: ConflictStrategy
    ): ResolveResult<AccountEntity> {
        // 检查是否存在同名账户
        val existingAccounts = accountDao.getAccountsByUserSync(account.userId)
        val existing = existingAccounts.find { it.name == account.name }
        
        return if (existing != null) {
            when (strategy) {
                ConflictStrategy.SKIP -> ResolveResult.Skip("账户已存在: ${account.name}", existing)
                ConflictStrategy.RENAME -> {
                    val newName = generateUniqueName(account.name, existingAccounts.map { it.name })
                    ResolveResult.Modified(account.copy(name = newName))
                }
                ConflictStrategy.MERGE -> {
                    // 合并策略：更新余额
                    val merged = existing.copy(
                        balanceCents = existing.balanceCents + account.balanceCents,
                        updatedAt = System.currentTimeMillis()
                    )
                    ResolveResult.Merge(merged)
                }
                ConflictStrategy.OVERWRITE -> {
                    ResolveResult.Modified(account.copy(id = existing.id))
                }
            }
        } else {
            ResolveResult.NoConflict(account)
        }
    }
    
    /**
     * 处理分类冲突
     */
    suspend fun resolveCategoryConflict(
        category: CategoryEntity,
        strategy: ConflictStrategy
    ): ResolveResult<CategoryEntity> {
        val existingCategories = categoryDao.getCategoriesByUserSync(category.userId)
        val existing = existingCategories.find { 
            it.name == category.name && it.type == category.type 
        }
        
        return if (existing != null) {
            when (strategy) {
                ConflictStrategy.SKIP -> ResolveResult.Skip("分类已存在: ${category.name}", existing)
                ConflictStrategy.RENAME -> {
                    val newName = generateUniqueName(category.name, existingCategories.map { it.name })
                    ResolveResult.Modified(category.copy(name = newName))
                }
                ConflictStrategy.MERGE -> ResolveResult.Merge(existing)
                ConflictStrategy.OVERWRITE -> {
                    ResolveResult.Modified(category.copy(id = existing.id))
                }
            }
        } else {
            ResolveResult.NoConflict(category)
        }
    }
    
    /**
     * 检测交易重复
     */
    suspend fun isTransactionDuplicate(
        transaction: TransactionEntity
    ): Boolean {
        // 基于时间戳、金额和账户检测重复
        val timeWindow = 60000L // 1分钟时间窗口
        val startTime = transaction.createdAt - timeWindow
        val endTime = transaction.createdAt + timeWindow
        
        val transactions = transactionDao.getTransactionsByDateRangeSync(
            transaction.userId,
            startTime,
            endTime
        )
        
        return transactions.any { 
            it.accountId == transaction.accountId &&
            it.amountCents == transaction.amountCents &&
            kotlin.math.abs(it.createdAt - transaction.createdAt) < timeWindow
        }
    }
    
    /**
     * 生成唯一名称
     */
    private fun generateUniqueName(baseName: String, existingNames: List<String>): String {
        var counter = 1
        var newName = "$baseName (导入)"
        
        while (existingNames.contains(newName)) {
            counter++
            newName = "$baseName (导入$counter)"
        }
        
        return newName
    }
}

/**
 * 冲突解决结果
 */
sealed class ResolveResult<T> {
    data class NoConflict<T>(val data: T) : ResolveResult<T>()
    data class Modified<T>(val data: T) : ResolveResult<T>()
    data class Merge<T>(val data: T) : ResolveResult<T>()
    data class Skip<T>(val reason: String, val existingData: T? = null) : ResolveResult<T>()
}
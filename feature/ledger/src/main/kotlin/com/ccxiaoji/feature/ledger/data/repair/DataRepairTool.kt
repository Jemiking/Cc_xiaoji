package com.ccxiaoji.feature.ledger.data.repair

import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import com.ccxiaoji.common.model.SyncStatus
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据修复工具
 * 用于修复导入数据的问题
 */
@Singleton
class DataRepairTool @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    
    /**
     * 执行完整的数据修复
     */
    suspend fun executeRepair(userId: String) {
        android.util.Log.e("DATA_REPAIR", "")
        android.util.Log.e("DATA_REPAIR", "========================================")
        android.util.Log.e("DATA_REPAIR", "         开始执行数据修复")
        android.util.Log.e("DATA_REPAIR", "========================================")
        android.util.Log.e("DATA_REPAIR", "")
        
        // 步骤1：优化默认账户为现金账户
        optimizeDefaultAccount(userId)
        
        // 步骤2：修复转账对象账户
        repairTransferAccounts(userId)
        
        // 步骤3：清理空账户
        cleanupEmptyAccounts(userId)
        
        android.util.Log.e("DATA_REPAIR", "")
        android.util.Log.e("DATA_REPAIR", "========================================")
        android.util.Log.e("DATA_REPAIR", "         数据修复完成")
        android.util.Log.e("DATA_REPAIR", "========================================")
    }
    
    /**
     * 步骤1：优化默认账户为现金账户
     * 将默认账户改名为"现金"，作为无账户标记交易的归属
     */
    private suspend fun optimizeDefaultAccount(userId: String) {
        android.util.Log.e("DATA_REPAIR", "【步骤1】优化默认账户")
        android.util.Log.e("DATA_REPAIR", "----------------------------------------")
        
        val defaultAccountId = "default_account_$userId"
        val defaultAccount = accountDao.getAccountById(defaultAccountId)
        
        if (defaultAccount != null) {
            // 将默认账户改名为"现金"
            val updatedAccount = defaultAccount.copy(
                name = "现金",
                type = "CASH",
                icon = "💵",
                updatedAt = System.currentTimeMillis()
            )
            accountDao.updateAccount(updatedAccount)
            
            android.util.Log.e("DATA_REPAIR", "✓ 已将默认账户改名为'现金'")
            android.util.Log.e("DATA_REPAIR", "  账户ID: $defaultAccountId")
            
            // 统计该账户的交易数
            val transactions = transactionDao.getTransactionsByUserSync(userId)
                .filter { it.accountId == defaultAccountId }
            android.util.Log.e("DATA_REPAIR", "  包含交易: ${transactions.size} 条")
            android.util.Log.e("DATA_REPAIR", "  说明: 这些是钱迹CSV中账户名为空的记录")
        } else {
            android.util.Log.e("DATA_REPAIR", "  未找到默认账户，跳过")
        }
    }
    
    /**
     * 步骤2：修复转账对象账户
     * 将错误创建的转账对象账户的交易迁移到合适的账户
     */
    private suspend fun repairTransferAccounts(userId: String) {
        android.util.Log.e("DATA_REPAIR", "")
        android.util.Log.e("DATA_REPAIR", "【步骤2】修复转账对象账户")
        android.util.Log.e("DATA_REPAIR", "----------------------------------------")
        
        val allAccounts = accountDao.getAccountsByUserSync(userId)
        val allTransactions = transactionDao.getTransactionsByUserSync(userId)
        
        // 识别转账对象账户（以">"开头或符合人名模式）
        val transferAccounts = allAccounts.filter { account ->
            isTransferAccount(account, allTransactions)
        }
        
        android.util.Log.e("DATA_REPAIR", "发现 ${transferAccounts.size} 个转账对象账户需要修复")
        
        // 获取或创建现金账户作为默认迁移目标
        val cashAccountId = getOrCreateCashAccount(userId)
        
        transferAccounts.forEach { account ->
            android.util.Log.e("DATA_REPAIR", "")
            android.util.Log.e("DATA_REPAIR", "处理账户: ${account.name}")
            
            val accountTransactions = allTransactions.filter { it.accountId == account.id }
            android.util.Log.e("DATA_REPAIR", "  涉及交易: ${accountTransactions.size} 条")
            
            accountTransactions.forEach { transaction ->
                // 确定目标账户
                val targetAccountId = determineTargetAccount(
                    transaction = transaction,
                    transferParty = account.name.removePrefix(">"),
                    defaultAccountId = cashAccountId,
                    allAccounts = allAccounts
                )
                
                // 更新交易：移动到目标账户，将转账对象添加到备注
                val updatedNote = buildUpdatedNote(
                    originalNote = transaction.note,
                    transferParty = account.name.removePrefix(">"),
                    isIncome = transaction.amountCents > 0
                )
                
                val updatedTransaction = transaction.copy(
                    accountId = targetAccountId,
                    note = updatedNote,
                    updatedAt = System.currentTimeMillis()
                )
                
                transactionDao.updateTransaction(updatedTransaction)
            }
            
            android.util.Log.e("DATA_REPAIR", "  ✓ 已迁移 ${accountTransactions.size} 条交易")
            
            // 删除错误创建的账户
            accountDao.softDeleteAccount(account.id, System.currentTimeMillis())
            android.util.Log.e("DATA_REPAIR", "  ✓ 已删除账户: ${account.name}")
        }
        
        if (transferAccounts.isEmpty()) {
            android.util.Log.e("DATA_REPAIR", "  未发现需要修复的转账对象账户")
        }
    }
    
    /**
     * 步骤3：清理空账户
     * 删除没有交易的临时账户
     */
    private suspend fun cleanupEmptyAccounts(userId: String) {
        android.util.Log.e("DATA_REPAIR", "")
        android.util.Log.e("DATA_REPAIR", "【步骤3】清理空账户")
        android.util.Log.e("DATA_REPAIR", "----------------------------------------")
        
        val allAccounts = accountDao.getAccountsByUserSync(userId)
        val allTransactions = transactionDao.getTransactionsByUserSync(userId)
        
        val emptyAccounts = allAccounts.filter { account ->
            allTransactions.none { it.accountId == account.id }
        }
        
        android.util.Log.e("DATA_REPAIR", "发现 ${emptyAccounts.size} 个空账户")
        
        emptyAccounts.forEach { account ->
            // 保留特殊账户
            if (account.id == "default_account_id" || account.id.startsWith("default_account_")) {
                android.util.Log.e("DATA_REPAIR", "  跳过: ${account.name} (系统账户)")
                return@forEach
            }
            
            accountDao.softDeleteAccount(account.id, System.currentTimeMillis())
            android.util.Log.e("DATA_REPAIR", "  ✓ 已删除: ${account.name}")
        }
    }
    
    /**
     * 判断是否为转账对象账户
     */
    private fun isTransferAccount(
        account: AccountEntity,
        allTransactions: List<TransactionEntity>
    ): Boolean {
        // 1. 账户名以">"开头
        if (account.name.startsWith(">")) {
            return true
        }
        
        // 2. 符合人名模式且交易特征符合转账
        val isPersonName = account.name.matches(Regex("^[\\u4e00-\\u9fa5]{2,4}$"))
        if (isPersonName) {
            val accountTransactions = allTransactions.filter { it.accountId == account.id }
            // 交易少于5笔且平均金额大于500元
            if (accountTransactions.size in 1..5) {
                val avgAmount = accountTransactions.sumOf { it.amountCents } / accountTransactions.size
                if (avgAmount > 50000) { // 500元
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * 获取或创建现金账户
     */
    private suspend fun getOrCreateCashAccount(userId: String): String {
        // 优先查找名为"现金"的账户
        val cashAccount = accountDao.findByName("现金", userId)
        if (cashAccount != null) {
            return cashAccount.id
        }
        
        // 查找默认账户
        val defaultAccountId = "default_account_$userId"
        val defaultAccount = accountDao.getAccountById(defaultAccountId)
        if (defaultAccount != null) {
            return defaultAccount.id
        }
        
        // 创建新的现金账户
        val newCashAccount = AccountEntity(
            id = "cash_account_$userId",
            userId = userId,
            name = "现金",
            type = "CASH",
            balanceCents = 0,
            currency = "CNY",
            icon = "💵",
            color = "#4CAF50",
            isDefault = false,
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
        
        accountDao.insert(newCashAccount)
        android.util.Log.e("DATA_REPAIR", "  创建新现金账户: ${newCashAccount.id}")
        return newCashAccount.id
    }
    
    /**
     * 确定目标账户
     * 根据交易特征智能选择合适的账户
     */
    private fun determineTargetAccount(
        transaction: TransactionEntity,
        transferParty: String,
        defaultAccountId: String,
        allAccounts: List<AccountEntity>
    ): String {
        // 简单策略：都迁移到现金账户
        // 未来可以根据交易特征（金额、分类、时间）智能匹配
        return defaultAccountId
    }
    
    /**
     * 构建更新后的备注
     */
    private fun buildUpdatedNote(
        originalNote: String?,
        transferParty: String,
        isIncome: Boolean
    ): String {
        val prefix = if (isIncome) "收款人" else "付款对象"
        val transferInfo = "[$prefix: $transferParty]"
        
        return if (originalNote.isNullOrBlank()) {
            transferInfo
        } else {
            "$originalNote $transferInfo"
        }
    }
}
package com.ccxiaoji.feature.ledger.data.diagnostic

import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 记账模块综合诊断工具
 * 用于分析和诊断数据问题
 */
@Singleton
class LedgerDiagnosticTool @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    
    data class AccountDiagnosticInfo(
        val accountId: String,
        val accountName: String,
        val accountType: String,
        val isDefault: Boolean,
        val transactionCount: Int,
        val totalAmountCents: Long,
        val firstTransactionDate: String?,
        val lastTransactionDate: String?,
        val createdAt: String
    )
    
    data class DiagnosticReport(
        val userId: String,
        val totalAccounts: Int,
        val totalTransactions: Int,
        val accounts: List<AccountDiagnosticInfo>,
        val orphanTransactions: Int,  // 没有有效账户的交易
        val dateRange: Pair<String?, String?>,  // 最早和最晚的交易日期
        val monthlyDistribution: Map<String, Int>,  // 按月分布
        val problemAccounts: List<String>  // 有问题的账户ID
    )
    
    /**
     * 运行完整诊断
     */
    suspend fun runFullDiagnostic(userId: String): DiagnosticReport {
        android.util.Log.e("DIAGNOSTIC", "")
        android.util.Log.e("DIAGNOSTIC", "========================================")
        android.util.Log.e("DIAGNOSTIC", "       记账模块综合诊断报告")
        android.util.Log.e("DIAGNOSTIC", "========================================")
        android.util.Log.e("DIAGNOSTIC", "用户ID: $userId")
        android.util.Log.e("DIAGNOSTIC", "诊断时间: ${Clock.System.now()}")
        android.util.Log.e("DIAGNOSTIC", "")
        
        // 1. 获取所有账户
        val allAccounts = accountDao.getAccountsByUserSync(userId)
        android.util.Log.e("DIAGNOSTIC", "【账户分析】")
        android.util.Log.e("DIAGNOSTIC", "账户总数: ${allAccounts.size}")
        android.util.Log.e("DIAGNOSTIC", "----------------------------------------")
        
        // 2. 获取所有交易
        val allTransactions = transactionDao.getTransactionsByUserSync(userId)
        android.util.Log.e("DIAGNOSTIC", "【交易概览】")
        android.util.Log.e("DIAGNOSTIC", "交易总数: ${allTransactions.size}")
        
        // 3. 分析每个账户
        val accountInfoList = mutableListOf<AccountDiagnosticInfo>()
        val problemAccounts = mutableListOf<String>()
        
        android.util.Log.e("DIAGNOSTIC", "")
        android.util.Log.e("DIAGNOSTIC", "【详细账户信息】")
        android.util.Log.e("DIAGNOSTIC", "----------------------------------------")
        
        allAccounts.forEachIndexed { index, account ->
            val accountTransactions = allTransactions.filter { it.accountId == account.id }
            val totalAmount = accountTransactions.sumOf { it.amountCents.toLong() }
            
            val firstTransaction = accountTransactions.minByOrNull { it.createdAt }
            val lastTransaction = accountTransactions.maxByOrNull { it.createdAt }
            
            val info = AccountDiagnosticInfo(
                accountId = account.id,
                accountName = account.name,
                accountType = account.type,
                isDefault = account.isDefault,
                transactionCount = accountTransactions.size,
                totalAmountCents = totalAmount,
                firstTransactionDate = firstTransaction?.let { 
                    Instant.fromEpochMilliseconds(it.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .toString()
                },
                lastTransactionDate = lastTransaction?.let {
                    Instant.fromEpochMilliseconds(it.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .toString()
                },
                createdAt = Instant.fromEpochMilliseconds(account.createdAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toString()
            )
            
            accountInfoList.add(info)
            
            // 输出账户详情
            android.util.Log.e("DIAGNOSTIC", "${index + 1}. 账户: ${account.name}")
            android.util.Log.e("DIAGNOSTIC", "   ID: ${account.id}")
            android.util.Log.e("DIAGNOSTIC", "   类型: ${account.type}")
            android.util.Log.e("DIAGNOSTIC", "   默认: ${if (account.isDefault) "是 ✓" else "否"}")
            android.util.Log.e("DIAGNOSTIC", "   交易数: ${accountTransactions.size} 条")
            android.util.Log.e("DIAGNOSTIC", "   交易额: ${totalAmount / 100.0} 元")
            if (firstTransaction != null) {
                android.util.Log.e("DIAGNOSTIC", "   首笔: ${info.firstTransactionDate}")
                android.util.Log.e("DIAGNOSTIC", "   末笔: ${info.lastTransactionDate}")
            }
            android.util.Log.e("DIAGNOSTIC", "   创建: ${info.createdAt}")
            
            // 识别问题账户
            if (account.id.length == 36 && !account.id.startsWith("default_account_")) {
                problemAccounts.add(account.id)
                android.util.Log.e("DIAGNOSTIC", "   ⚠️ 疑似临时账户")
            }
            
            android.util.Log.e("DIAGNOSTIC", "")
        }
        
        // 4. 查找孤儿交易
        val accountIds = allAccounts.map { it.id }.toSet()
        val orphanTransactions = allTransactions.filter { it.accountId !in accountIds }
        
        if (orphanTransactions.isNotEmpty()) {
            android.util.Log.e("DIAGNOSTIC", "【⚠️ 孤儿交易】")
            android.util.Log.e("DIAGNOSTIC", "发现 ${orphanTransactions.size} 条没有有效账户的交易")
            val orphanAccountIds = orphanTransactions.map { it.accountId }.distinct()
            android.util.Log.e("DIAGNOSTIC", "涉及的无效账户ID: ${orphanAccountIds.joinToString(", ")}")
            android.util.Log.e("DIAGNOSTIC", "")
        }
        
        // 5. 分析时间范围
        val earliestTransaction = allTransactions.minByOrNull { it.createdAt }
        val latestTransaction = allTransactions.maxByOrNull { it.createdAt }
        
        android.util.Log.e("DIAGNOSTIC", "【时间范围分析】")
        android.util.Log.e("DIAGNOSTIC", "----------------------------------------")
        if (earliestTransaction != null && latestTransaction != null) {
            val earliest = Instant.fromEpochMilliseconds(earliestTransaction.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val latest = Instant.fromEpochMilliseconds(latestTransaction.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            
            android.util.Log.e("DIAGNOSTIC", "最早交易: $earliest")
            android.util.Log.e("DIAGNOSTIC", "最新交易: $latest")
        }
        
        // 6. 按月分布统计
        val monthlyDistribution = allTransactions.groupBy { transaction ->
            val instant = Instant.fromEpochMilliseconds(transaction.createdAt)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}"
        }.mapValues { it.value.size }
        
        android.util.Log.e("DIAGNOSTIC", "")
        android.util.Log.e("DIAGNOSTIC", "【月度分布】")
        android.util.Log.e("DIAGNOSTIC", "----------------------------------------")
        monthlyDistribution.toSortedMap().forEach { (month, count) ->
            val bar = "█".repeat((count * 20 / allTransactions.size).coerceAtLeast(1))
            android.util.Log.e("DIAGNOSTIC", "$month: $bar $count 条")
        }
        
        // 7. 问题诊断
        android.util.Log.e("DIAGNOSTIC", "")
        android.util.Log.e("DIAGNOSTIC", "【问题诊断】")
        android.util.Log.e("DIAGNOSTIC", "----------------------------------------")
        
        val defaultAccount = allAccounts.find { it.isDefault }
        if (defaultAccount == null) {
            android.util.Log.e("DIAGNOSTIC", "⚠️ 没有设置默认账户")
        } else {
            android.util.Log.e("DIAGNOSTIC", "✓ 默认账户: ${defaultAccount.name} (${defaultAccount.id})")
        }
        
        if (problemAccounts.isNotEmpty()) {
            android.util.Log.e("DIAGNOSTIC", "⚠️ 发现 ${problemAccounts.size} 个疑似临时账户")
            android.util.Log.e("DIAGNOSTIC", "   建议将这些账户的交易迁移到默认账户")
        }
        
        // 计算默认账户交易占比
        val defaultAccountTransactions = defaultAccount?.let { account ->
            allTransactions.count { it.accountId == account.id }
        } ?: 0
        val percentage = if (allTransactions.isNotEmpty()) {
            (defaultAccountTransactions * 100.0 / allTransactions.size).toInt()
        } else 0
        
        android.util.Log.e("DIAGNOSTIC", "")
        android.util.Log.e("DIAGNOSTIC", "【数据分布】")
        android.util.Log.e("DIAGNOSTIC", "默认账户交易占比: $percentage% ($defaultAccountTransactions/${allTransactions.size})")
        
        if (percentage < 50) {
            android.util.Log.e("DIAGNOSTIC", "⚠️ 大部分交易不在默认账户中，建议执行数据迁移")
        }
        
        android.util.Log.e("DIAGNOSTIC", "")
        android.util.Log.e("DIAGNOSTIC", "========================================")
        android.util.Log.e("DIAGNOSTIC", "          诊断完成")
        android.util.Log.e("DIAGNOSTIC", "========================================")
        android.util.Log.e("DIAGNOSTIC", "")
        
        return DiagnosticReport(
            userId = userId,
            totalAccounts = allAccounts.size,
            totalTransactions = allTransactions.size,
            accounts = accountInfoList,
            orphanTransactions = orphanTransactions.size,
            dateRange = Pair(
                earliestTransaction?.let {
                    Instant.fromEpochMilliseconds(it.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .toString()
                },
                latestTransaction?.let {
                    Instant.fromEpochMilliseconds(it.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .toString()
                }
            ),
            monthlyDistribution = monthlyDistribution,
            problemAccounts = problemAccounts
        )
    }
    
    /**
     * 分析查询逻辑
     */
    suspend fun analyzeQueryLogic(
        userId: String,
        selectedAccountId: String?,
        startDate: Long,
        endDate: Long
    ) {
        android.util.Log.e("DIAGNOSTIC", "")
        android.util.Log.e("DIAGNOSTIC", "【查询逻辑分析】")
        android.util.Log.e("DIAGNOSTIC", "========================================")
        android.util.Log.e("DIAGNOSTIC", "查询参数:")
        android.util.Log.e("DIAGNOSTIC", "  用户ID: $userId")
        android.util.Log.e("DIAGNOSTIC", "  账户ID: ${selectedAccountId ?: "null (查询所有)"}")
        android.util.Log.e("DIAGNOSTIC", "  开始时间: ${Instant.fromEpochMilliseconds(startDate).toLocalDateTime(TimeZone.currentSystemDefault())}")
        android.util.Log.e("DIAGNOSTIC", "  结束时间: ${Instant.fromEpochMilliseconds(endDate).toLocalDateTime(TimeZone.currentSystemDefault())}")
        
        // 测试不同查询条件的结果
        val allUserTransactions = transactionDao.getTransactionsByUserSync(userId)
        android.util.Log.e("DIAGNOSTIC", "")
        android.util.Log.e("DIAGNOSTIC", "查询结果:")
        android.util.Log.e("DIAGNOSTIC", "  1. 用户所有交易: ${allUserTransactions.size} 条")
        
        val dateRangeTransactions = transactionDao.getTransactionsByDateRangeSync(userId, startDate, endDate)
        android.util.Log.e("DIAGNOSTIC", "  2. 时间范围内交易: ${dateRangeTransactions.size} 条")
        
        if (selectedAccountId != null) {
            val accountTransactions = allUserTransactions.filter { it.accountId == selectedAccountId }
            android.util.Log.e("DIAGNOSTIC", "  3. 指定账户交易: ${accountTransactions.size} 条")
            
            val accountDateRangeTransactions = dateRangeTransactions.filter { it.accountId == selectedAccountId }
            android.util.Log.e("DIAGNOSTIC", "  4. 指定账户+时间范围: ${accountDateRangeTransactions.size} 条")
        }
        
        // 使用分页查询
        val paginatedResult = transactionDao.getTransactionsPaginated(
            userId = userId,
            offset = 0,
            limit = 20,
            accountId = selectedAccountId,
            startDateMillis = startDate,
            endDateMillis = endDate
        )
        
        android.util.Log.e("DIAGNOSTIC", "  5. 分页查询结果: ${paginatedResult.first.size} 条 (总计: ${paginatedResult.second})")
        
        if (paginatedResult.first.isEmpty() && dateRangeTransactions.isNotEmpty()) {
            android.util.Log.e("DIAGNOSTIC", "")
            android.util.Log.e("DIAGNOSTIC", "⚠️ 警告: 分页查询返回空，但时间范围内有数据！")
            android.util.Log.e("DIAGNOSTIC", "可能原因: 账户ID过滤导致")
            
            // 分析哪些账户有数据
            val accountsWithData = dateRangeTransactions.groupBy { it.accountId }
                .mapValues { it.value.size }
            
            android.util.Log.e("DIAGNOSTIC", "")
            android.util.Log.e("DIAGNOSTIC", "时间范围内各账户交易数:")
            accountsWithData.forEach { (accountId, count) ->
                val account = accountDao.getAccountById(accountId)
                android.util.Log.e("DIAGNOSTIC", "  ${account?.name ?: "未知"} ($accountId): $count 条")
            }
        }
        
        android.util.Log.e("DIAGNOSTIC", "========================================")
        android.util.Log.e("DIAGNOSTIC", "")
    }
}
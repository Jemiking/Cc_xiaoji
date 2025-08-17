package com.ccxiaoji.feature.ledger.data.diagnostic

import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交易数据流追踪器
 * 用于精准定位数据在哪个环节丢失
 */
@Singleton
class TransactionFlowTracker @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    
    /**
     * 全链路追踪交易查询
     */
    suspend fun trackTransactionFlow(
        userId: String,
        selectedAccountId: String?,
        startDate: Long,
        endDate: Long,
        currentPage: Int,
        pageSize: Int
    ) {
        android.util.Log.e("FLOW_TRACKER", "")
        android.util.Log.e("FLOW_TRACKER", "========================================")
        android.util.Log.e("FLOW_TRACKER", "        交易数据流全链路追踪")
        android.util.Log.e("FLOW_TRACKER", "========================================")
        android.util.Log.e("FLOW_TRACKER", "")
        
        // 1. 输入参数
        android.util.Log.e("FLOW_TRACKER", "【1. 查询参数】")
        android.util.Log.e("FLOW_TRACKER", "----------------------------------------")
        android.util.Log.e("FLOW_TRACKER", "用户ID: $userId")
        android.util.Log.e("FLOW_TRACKER", "选中账户ID: ${selectedAccountId ?: "NULL(查询所有)"}")
        android.util.Log.e("FLOW_TRACKER", "日期范围: $startDate - $endDate")
        android.util.Log.e("FLOW_TRACKER", "日期解析: ${java.util.Date(startDate)} 至 ${java.util.Date(endDate)}")
        android.util.Log.e("FLOW_TRACKER", "分页: 第${currentPage}页, 每页${pageSize}条")
        android.util.Log.e("FLOW_TRACKER", "")
        
        // 2. 账户状态检查
        android.util.Log.e("FLOW_TRACKER", "【2. 账户状态】")
        android.util.Log.e("FLOW_TRACKER", "----------------------------------------")
        
        if (selectedAccountId != null) {
            val account = accountDao.getAccountById(selectedAccountId)
            if (account != null) {
                android.util.Log.e("FLOW_TRACKER", "账户存在: ✅")
                android.util.Log.e("FLOW_TRACKER", "账户名: ${account.name}")
                android.util.Log.e("FLOW_TRACKER", "账户类型: ${account.type}")
                android.util.Log.e("FLOW_TRACKER", "是否默认: ${account.isDefault}")
                android.util.Log.e("FLOW_TRACKER", "是否删除: ${account.isDeleted}")
            } else {
                android.util.Log.e("FLOW_TRACKER", "⚠️ 账户不存在！ID: $selectedAccountId")
            }
        } else {
            android.util.Log.e("FLOW_TRACKER", "未选择特定账户，查询所有账户")
        }
        android.util.Log.e("FLOW_TRACKER", "")
        
        // 3. 数据库实际数据统计
        android.util.Log.e("FLOW_TRACKER", "【3. 数据库实际数据】")
        android.util.Log.e("FLOW_TRACKER", "----------------------------------------")
        
        // 3.1 用户所有交易
        val allUserTransactions = transactionDao.getTransactionsByUserSync(userId)
        android.util.Log.e("FLOW_TRACKER", "用户总交易数: ${allUserTransactions.size}")
        
        // 3.2 按账户分组统计
        val accountGroups = allUserTransactions.groupBy { it.accountId }
        android.util.Log.e("FLOW_TRACKER", "")
        android.util.Log.e("FLOW_TRACKER", "按账户分组:")
        accountGroups.forEach { (accountId, transactions) ->
            val account = accountDao.getAccountById(accountId)
            android.util.Log.e("FLOW_TRACKER", "  ${account?.name ?: accountId}: ${transactions.size}条")
        }
        
        // 3.3 日期范围内的交易
        val inRangeTransactions = allUserTransactions.filter { 
            it.createdAt in startDate until endDate 
        }
        android.util.Log.e("FLOW_TRACKER", "")
        android.util.Log.e("FLOW_TRACKER", "日期范围内交易: ${inRangeTransactions.size}条")
        
        // 3.4 如果选择了账户，过滤账户
        val accountFilteredTransactions = if (selectedAccountId != null) {
            inRangeTransactions.filter { it.accountId == selectedAccountId }
        } else {
            inRangeTransactions
        }
        android.util.Log.e("FLOW_TRACKER", "账户过滤后: ${accountFilteredTransactions.size}条")
        
        // 3.5 未删除的交易
        val activeTransactions = accountFilteredTransactions.filter { !it.isDeleted }
        android.util.Log.e("FLOW_TRACKER", "未删除交易: ${activeTransactions.size}条")
        android.util.Log.e("FLOW_TRACKER", "")
        
        // 4. DAO层查询
        android.util.Log.e("FLOW_TRACKER", "【4. DAO层查询】")
        android.util.Log.e("FLOW_TRACKER", "----------------------------------------")
        
        // 4.1 模拟getPaginatedTransactions的查询
        val offset = currentPage * pageSize
        android.util.Log.e("FLOW_TRACKER", "OFFSET: $offset, LIMIT: $pageSize")
        
        // 根据是否有账户ID选择不同的查询
        val daoResults = if (selectedAccountId != null) {
            android.util.Log.e("FLOW_TRACKER", "执行查询: getTransactionsPaginatedData (with account)")
            android.util.Log.e("FLOW_TRACKER", "SQL参数: accountId=$selectedAccountId, start=$startDate, end=$endDate")
            transactionDao.getTransactionsPaginatedData(
                userId, offset, pageSize, selectedAccountId, startDate, endDate
            )
        } else {
            android.util.Log.e("FLOW_TRACKER", "执行查询: getTransactionsPaginatedData (all accounts)")
            android.util.Log.e("FLOW_TRACKER", "SQL参数: userId=$userId, start=$startDate, end=$endDate")
            transactionDao.getTransactionsPaginatedData(
                userId, offset, pageSize, null, startDate, endDate
            )
        }
        
        android.util.Log.e("FLOW_TRACKER", "DAO返回结果: ${daoResults.size}条")
        if (daoResults.isNotEmpty()) {
            android.util.Log.e("FLOW_TRACKER", "第一条: ${daoResults.first().id}")
            android.util.Log.e("FLOW_TRACKER", "最后一条: ${daoResults.last().id}")
        }
        android.util.Log.e("FLOW_TRACKER", "")
        
        // 5. 月份分析
        android.util.Log.e("FLOW_TRACKER", "【5. 月份数据分布】")
        android.util.Log.e("FLOW_TRACKER", "----------------------------------------")
        
        // 统计各月份交易数
        val monthGroups = activeTransactions.groupBy { transaction ->
            val instant = Instant.fromEpochMilliseconds(transaction.createdAt)
            val datetime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${datetime.year}-${datetime.monthNumber.toString().padStart(2, '0')}"
        }.toSortedMap(reverseOrder())
        
        android.util.Log.e("FLOW_TRACKER", "最近12个月交易分布:")
        monthGroups.entries.take(12).forEach { entry ->
            val (month, transactions) = entry
            android.util.Log.e("FLOW_TRACKER", "  $month: ${transactions.size}条")
        }
        android.util.Log.e("FLOW_TRACKER", "")
        
        // 6. 诊断结论
        android.util.Log.e("FLOW_TRACKER", "【6. 诊断结论】")
        android.util.Log.e("FLOW_TRACKER", "----------------------------------------")
        
        val expectedCount = activeTransactions.size
        val actualCount = daoResults.size
        
        if (actualCount < kotlin.math.min(expectedCount, pageSize)) {
            android.util.Log.e("FLOW_TRACKER", "❌ 数据丢失!")
            android.util.Log.e("FLOW_TRACKER", "   预期: ${kotlin.math.min(expectedCount, pageSize)}条")
            android.util.Log.e("FLOW_TRACKER", "   实际: ${actualCount}条")
            android.util.Log.e("FLOW_TRACKER", "   丢失: ${kotlin.math.min(expectedCount, pageSize) - actualCount}条")
            
            // 分析可能原因
            android.util.Log.e("FLOW_TRACKER", "")
            android.util.Log.e("FLOW_TRACKER", "可能原因:")
            
            if (selectedAccountId?.startsWith("default_account_") == true) {
                android.util.Log.e("FLOW_TRACKER", "   ⚠️ 选择了default_account开头的账户")
                android.util.Log.e("FLOW_TRACKER", "   ⚠️ ViewModel可能有特殊处理逻辑")
            }
            
            if (expectedCount == 0 && allUserTransactions.isNotEmpty()) {
                android.util.Log.e("FLOW_TRACKER", "   ⚠️ 日期范围可能不正确")
                android.util.Log.e("FLOW_TRACKER", "   ⚠️ 检查时区转换是否正确")
            }
            
            if (actualCount == 0 && expectedCount > 0) {
                android.util.Log.e("FLOW_TRACKER", "   ⚠️ SQL查询可能有问题")
                android.util.Log.e("FLOW_TRACKER", "   ⚠️ 检查JOIN条件和WHERE子句")
            }
        } else {
            android.util.Log.e("FLOW_TRACKER", "✅ 数据流正常")
            android.util.Log.e("FLOW_TRACKER", "   查询返回: ${actualCount}条")
        }
        
        android.util.Log.e("FLOW_TRACKER", "")
        android.util.Log.e("FLOW_TRACKER", "========================================")
        android.util.Log.e("FLOW_TRACKER", "           追踪完成")
        android.util.Log.e("FLOW_TRACKER", "========================================")
    }
    
    /**
     * 验证特定月份数据
     */
    suspend fun verifyMonthData(
        userId: String,
        year: Int,
        month: Int
    ) {
        android.util.Log.e("FLOW_TRACKER", "")
        android.util.Log.e("FLOW_TRACKER", "【验证${year}年${month}月数据】")
        android.util.Log.e("FLOW_TRACKER", "----------------------------------------")
        
        // 计算月份起止时间
        val startDate = LocalDate(year, month, 1)
            .atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
        
        val endDate = if (month == 12) {
            LocalDate(year + 1, 1, 1)
        } else {
            LocalDate(year, month + 1, 1)
        }.atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
        
        android.util.Log.e("FLOW_TRACKER", "月份范围: $startDate - $endDate")
        android.util.Log.e("FLOW_TRACKER", "解析: ${java.util.Date(startDate)} 至 ${java.util.Date(endDate)}")
        
        // 获取该月所有交易
        val monthTransactions = transactionDao.getTransactionsByUserSync(userId)
            .filter { it.createdAt in startDate until endDate && !it.isDeleted }
        
        android.util.Log.e("FLOW_TRACKER", "该月总交易: ${monthTransactions.size}条")
        
        // 按账户分组
        val byAccount = monthTransactions.groupBy { it.accountId }
        android.util.Log.e("FLOW_TRACKER", "")
        android.util.Log.e("FLOW_TRACKER", "按账户分布:")
        byAccount.forEach { (accountId, trans) ->
            val account = accountDao.getAccountById(accountId)
            android.util.Log.e("FLOW_TRACKER", "  ${account?.name ?: accountId}: ${trans.size}条")
            
            // 显示前3条样本
            trans.take(3).forEach { t ->
                val instant = Instant.fromEpochMilliseconds(t.createdAt)
                val datetime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                android.util.Log.e("FLOW_TRACKER", "    - ${datetime.date} | ${t.amountCents/100.0}元")
            }
        }
    }
}
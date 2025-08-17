package com.ccxiaoji.feature.ledger.data.diagnostic

import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 默认账户数据分析器
 * 分析默认账户中的交易来源，判断为什么这些交易被放入默认账户
 */
@Singleton
class DefaultAccountAnalyzer @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    
    /**
     * 分析默认账户数据
     */
    suspend fun analyzeDefaultAccount(userId: String) {
        android.util.Log.e("DEFAULT_ANALYZER", "")
        android.util.Log.e("DEFAULT_ANALYZER", "========================================")
        android.util.Log.e("DEFAULT_ANALYZER", "     默认账户数据来源分析")
        android.util.Log.e("DEFAULT_ANALYZER", "========================================")
        android.util.Log.e("DEFAULT_ANALYZER", "")
        
        // 查找默认账户
        val defaultAccountId = "default_account_$userId"
        val defaultAccount = accountDao.getAccountById(defaultAccountId)
        
        if (defaultAccount == null) {
            android.util.Log.e("DEFAULT_ANALYZER", "未找到默认账户")
            return
        }
        
        android.util.Log.e("DEFAULT_ANALYZER", "默认账户ID: $defaultAccountId")
        android.util.Log.e("DEFAULT_ANALYZER", "账户名称: ${defaultAccount.name}")
        android.util.Log.e("DEFAULT_ANALYZER", "")
        
        // 获取默认账户的所有交易
        val transactions = transactionDao.getTransactionsByUserSync(userId)
            .filter { it.accountId == defaultAccountId }
        
        android.util.Log.e("DEFAULT_ANALYZER", "【交易统计】")
        android.util.Log.e("DEFAULT_ANALYZER", "总交易数: ${transactions.size}")
        
        if (transactions.isEmpty()) {
            android.util.Log.e("DEFAULT_ANALYZER", "默认账户无交易")
            return
        }
        
        // 分析备注模式
        android.util.Log.e("DEFAULT_ANALYZER", "")
        android.util.Log.e("DEFAULT_ANALYZER", "【备注分析】")
        android.util.Log.e("DEFAULT_ANALYZER", "----------------------------------------")
        
        val notesWithQianjiId = transactions.filter { 
            it.note?.contains("钱迹ID:") == true 
        }
        android.util.Log.e("DEFAULT_ANALYZER", "包含钱迹ID的交易: ${notesWithQianjiId.size} 条")
        
        val emptyNotes = transactions.filter { it.note.isNullOrEmpty() }
        android.util.Log.e("DEFAULT_ANALYZER", "无备注的交易: ${emptyNotes.size} 条")
        
        // 分析分类分布
        android.util.Log.e("DEFAULT_ANALYZER", "")
        android.util.Log.e("DEFAULT_ANALYZER", "【分类分布】")
        android.util.Log.e("DEFAULT_ANALYZER", "----------------------------------------")
        
        val categoryGroups = transactions.groupBy { it.categoryId }
        val categories = categoryDao.getCategoriesByUserSync(userId)
        
        categoryGroups.entries
            .sortedByDescending { it.value.size }
            .take(10)
            .forEach { (categoryId, trans) ->
                val category = categories.find { it.id == categoryId }
                android.util.Log.e("DEFAULT_ANALYZER", 
                    "${category?.name ?: "未知"}: ${trans.size} 条 (${trans.size * 100 / transactions.size}%)")
            }
        
        // 分析金额分布
        android.util.Log.e("DEFAULT_ANALYZER", "")
        android.util.Log.e("DEFAULT_ANALYZER", "【金额特征】")
        android.util.Log.e("DEFAULT_ANALYZER", "----------------------------------------")
        
        val amounts = transactions.map { it.amountCents }
        val avgAmount = amounts.average() / 100
        val maxAmount = amounts.maxOrNull()?.div(100) ?: 0
        val minAmount = amounts.minOrNull()?.div(100) ?: 0
        
        android.util.Log.e("DEFAULT_ANALYZER", "平均金额: $avgAmount 元")
        android.util.Log.e("DEFAULT_ANALYZER", "最大金额: $maxAmount 元")
        android.util.Log.e("DEFAULT_ANALYZER", "最小金额: $minAmount 元")
        
        // 抽样显示一些交易
        android.util.Log.e("DEFAULT_ANALYZER", "")
        android.util.Log.e("DEFAULT_ANALYZER", "【样本交易（前10条）】")
        android.util.Log.e("DEFAULT_ANALYZER", "----------------------------------------")
        
        transactions.take(10).forEach { trans ->
            val category = categories.find { it.id == trans.categoryId }
            val date = Instant.fromEpochMilliseconds(trans.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            android.util.Log.e("DEFAULT_ANALYZER", 
                "$date | ${category?.name} | ${trans.amountCents / 100.0}元 | ${trans.note ?: "无备注"}")
        }
        
        // 查找可能的原因
        android.util.Log.e("DEFAULT_ANALYZER", "")
        android.util.Log.e("DEFAULT_ANALYZER", "【可能原因分析】")
        android.util.Log.e("DEFAULT_ANALYZER", "----------------------------------------")
        
        if (notesWithQianjiId.size == transactions.size) {
            android.util.Log.e("DEFAULT_ANALYZER", "✓ 所有交易都来自钱迹导入")
        }
        
        // 检查是否是账户名为空的记录
        android.util.Log.e("DEFAULT_ANALYZER", "⚠️ 这些交易可能是：")
        android.util.Log.e("DEFAULT_ANALYZER", "  1. 钱迹CSV中账户名为空的记录")
        android.util.Log.e("DEFAULT_ANALYZER", "  2. 账户名解析失败的记录") 
        android.util.Log.e("DEFAULT_ANALYZER", "  3. 账户名包含特殊字符导致解析失败")
        
        // 检查时间分布
        val oldestTrans = transactions.minByOrNull { it.createdAt }
        val newestTrans = transactions.maxByOrNull { it.createdAt }
        
        if (oldestTrans != null && newestTrans != null) {
            val oldestDate = Instant.fromEpochMilliseconds(oldestTrans.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val newestDate = Instant.fromEpochMilliseconds(newestTrans.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            
            android.util.Log.e("DEFAULT_ANALYZER", "")
            android.util.Log.e("DEFAULT_ANALYZER", "时间跨度: $oldestDate 至 $newestDate")
        }
        
        android.util.Log.e("DEFAULT_ANALYZER", "")
        android.util.Log.e("DEFAULT_ANALYZER", "========================================")
        android.util.Log.e("DEFAULT_ANALYZER", "          分析完成")
        android.util.Log.e("DEFAULT_ANALYZER", "========================================")
    }
}
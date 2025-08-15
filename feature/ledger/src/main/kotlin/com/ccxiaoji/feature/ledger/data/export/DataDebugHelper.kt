package com.ccxiaoji.feature.ledger.data.export

import android.util.Log
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.shared.user.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 数据调试助手 - 用于诊断数据重复问题
 */
class DataDebugHelper @Inject constructor(
    private val transactionDao: TransactionDao,
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "DataDebugHelper"
    }
    
    /**
     * 调试交易记录数据
     * 比较Flow.first()和直接查询的结果差异
     */
    suspend fun debugTransactionData() {
        try {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser == null) {
                Log.e(TAG, "当前用户不存在")
                return
            }
            
            Log.d(TAG, "========== 开始数据调试 ==========")
            Log.d(TAG, "当前用户ID: ${currentUser.id}")
            
            // 方法1: 使用Flow.first() - 这是CSV导出使用的方法
            val transactionsFromFlow = transactionDao.getTransactionsByUser(currentUser.id).first()
            Log.d(TAG, "Flow.first() 返回记录数: ${transactionsFromFlow.size}")
            
            // 分组统计，查找重复
            val groupedByAmount = transactionsFromFlow.groupBy { it.amountCents }
            
            // 找出有重复金额的记录
            groupedByAmount.forEach { (amount, transactions) ->
                if (transactions.size > 1) {
                    Log.w(TAG, "发现重复金额 ${amount/100.0}元，共 ${transactions.size} 条记录:")
                    transactions.forEachIndexed { index, transaction ->
                        Log.w(TAG, "  [${index + 1}] ID=${transaction.id}, " +
                                "账户=${transaction.accountId}, " +
                                "分类=${transaction.categoryId}, " +
                                "备注=${transaction.note}, " +
                                "时间=${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(transaction.createdAt)}, " +
                                "isDeleted=${transaction.isDeleted}")
                    }
                }
            }
            
            // 检查100元的交易
            val hundredYuanTransactions = transactionsFromFlow.filter { it.amountCents == 10000 || it.amountCents == -10000 }
            Log.d(TAG, "100元交易记录数: ${hundredYuanTransactions.size}")
            hundredYuanTransactions.forEachIndexed { index, transaction ->
                Log.d(TAG, "100元交易[${index + 1}]: " +
                        "ID=${transaction.id}, " +
                        "金额=${transaction.amountCents/100.0}, " +
                        "账户=${transaction.accountId}, " +
                        "分类=${transaction.categoryId}, " +
                        "备注=${transaction.note}, " +
                        "时间=${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(transaction.createdAt)}")
            }
            
            // 检查是否有相同ID的记录
            val idMap = mutableMapOf<String, Int>()
            transactionsFromFlow.forEach { transaction ->
                val count = idMap.getOrDefault(transaction.id, 0)
                idMap[transaction.id] = count + 1
            }
            
            val duplicateIds = idMap.filter { it.value > 1 }
            if (duplicateIds.isNotEmpty()) {
                Log.e(TAG, "发现重复ID: $duplicateIds")
            } else {
                Log.d(TAG, "没有发现重复ID")
            }
            
            Log.d(TAG, "========== 数据调试完成 ==========")
            
        } catch (e: Exception) {
            Log.e(TAG, "调试过程出错", e)
        }
    }
    
    /**
     * 获取调试报告
     */
    suspend fun getDebugReport(): String {
        val report = StringBuilder()
        
        try {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser == null) {
                return "错误：当前用户不存在"
            }
            
            val transactions = transactionDao.getTransactionsByUser(currentUser.id).first()
            
            report.appendLine("数据调试报告")
            report.appendLine("=" * 50)
            report.appendLine("用户ID: ${currentUser.id}")
            report.appendLine("总记录数: ${transactions.size}")
            
            // 统计各个金额的记录数
            val amountStats = transactions.groupBy { it.amountCents }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)
            
            report.appendLine("\n重复金额TOP10:")
            amountStats.forEach { (amount, count) ->
                if (count > 1) {
                    report.appendLine("  ${amount/100.0}元: $count 条")
                }
            }
            
            // 检查100元交易
            val hundredYuan = transactions.filter { 
                it.amountCents == 10000 || it.amountCents == -10000 
            }
            
            report.appendLine("\n100元交易详情:")
            if (hundredYuan.isEmpty()) {
                report.appendLine("  无100元交易")
            } else {
                hundredYuan.forEach { t ->
                    report.appendLine("  ID: ${t.id.substring(0, 8)}...")
                    report.appendLine("  金额: ${t.amountCents/100.0}")
                    report.appendLine("  备注: ${t.note ?: "无"}")
                    report.appendLine("  ---")
                }
            }
            
        } catch (e: Exception) {
            report.appendLine("生成报告出错: ${e.message}")
        }
        
        return report.toString()
    }
}

private operator fun String.times(count: Int): String = repeat(count)
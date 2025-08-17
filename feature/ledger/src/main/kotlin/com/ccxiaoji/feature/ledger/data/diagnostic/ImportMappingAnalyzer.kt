package com.ccxiaoji.feature.ledger.data.diagnostic

import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 导入映射错误分析工具
 * 用于精确识别和诊断导入时的字段映射错误
 */
@Singleton
class ImportMappingAnalyzer @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    
    // 账户类型
    enum class AccountClassification {
        PAYMENT_METHOD,     // 支付方式（微信、支付宝等）
        TRANSFER_PARTY,     // 转账对象（人名）
        SUB_CATEGORY,       // 可能是二级分类
        TEMPORARY,          // 临时/未知
        NORMAL              // 正常账户
    }
    
    data class AccountAnalysis(
        val account: AccountEntity,
        val classification: AccountClassification,
        val transactionCount: Int,
        val totalAmountCents: Long,
        val avgAmountCents: Long,
        val dateSpanDays: Long,
        val categoryCount: Int,
        val categories: List<String>,
        val sampleTransactions: List<TransactionEntity>,
        val suspicionReason: String
    )
    
    data class MappingErrorReport(
        val totalAccounts: Int,
        val totalTransactions: Int,
        val transferPartyAccounts: List<AccountAnalysis>,
        val paymentMethodAccounts: List<AccountAnalysis>,
        val suspiciousAccounts: List<AccountAnalysis>,
        val normalAccounts: List<AccountAnalysis>,
        val recommendations: List<String>
    )
    
    /**
     * 运行完整的映射错误分析
     */
    suspend fun analyzeMappingErrors(userId: String): MappingErrorReport {
        android.util.Log.e("MAPPING_ANALYZER", "")
        android.util.Log.e("MAPPING_ANALYZER", "========================================")
        android.util.Log.e("MAPPING_ANALYZER", "     导入映射错误精准分析")
        android.util.Log.e("MAPPING_ANALYZER", "========================================")
        android.util.Log.e("MAPPING_ANALYZER", "分析时间: ${Clock.System.now()}")
        android.util.Log.e("MAPPING_ANALYZER", "")
        
        val allAccounts = accountDao.getAccountsByUserSync(userId)
        val allTransactions = transactionDao.getTransactionsByUserSync(userId)
        val allCategories = categoryDao.getCategoriesByUserSync(userId)
        
        val transferPartyAccounts = mutableListOf<AccountAnalysis>()
        val paymentMethodAccounts = mutableListOf<AccountAnalysis>()
        val suspiciousAccounts = mutableListOf<AccountAnalysis>()
        val normalAccounts = mutableListOf<AccountAnalysis>()
        
        android.util.Log.e("MAPPING_ANALYZER", "【账户分类分析】")
        android.util.Log.e("MAPPING_ANALYZER", "总账户数: ${allAccounts.size}")
        android.util.Log.e("MAPPING_ANALYZER", "总交易数: ${allTransactions.size}")
        android.util.Log.e("MAPPING_ANALYZER", "----------------------------------------")
        
        // 分析每个账户
        allAccounts.forEach { account ->
            val analysis = analyzeAccount(account, allTransactions, allCategories)
            
            android.util.Log.e("MAPPING_ANALYZER", "")
            android.util.Log.e("MAPPING_ANALYZER", "账户: ${account.name}")
            android.util.Log.e("MAPPING_ANALYZER", "  ID: ${account.id}")
            android.util.Log.e("MAPPING_ANALYZER", "  类型识别: ${analysis.classification}")
            android.util.Log.e("MAPPING_ANALYZER", "  交易数: ${analysis.transactionCount}")
            android.util.Log.e("MAPPING_ANALYZER", "  总金额: ${analysis.totalAmountCents / 100.0}元")
            android.util.Log.e("MAPPING_ANALYZER", "  平均金额: ${analysis.avgAmountCents / 100.0}元")
            android.util.Log.e("MAPPING_ANALYZER", "  涉及分类数: ${analysis.categoryCount}")
            android.util.Log.e("MAPPING_ANALYZER", "  时间跨度: ${analysis.dateSpanDays}天")
            android.util.Log.e("MAPPING_ANALYZER", "  判断理由: ${analysis.suspicionReason}")
            
            // 显示样本交易
            if (analysis.sampleTransactions.isNotEmpty()) {
                android.util.Log.e("MAPPING_ANALYZER", "  样本交易:")
                analysis.sampleTransactions.take(3).forEach { trans ->
                    val category = allCategories.find { it.id == trans.categoryId }
                    val date = Instant.fromEpochMilliseconds(trans.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    android.util.Log.e("MAPPING_ANALYZER", "    - ${date.date} | ${category?.name ?: "未知"} | ${trans.amountCents / 100.0}元 | ${trans.note ?: "无备注"}")
                }
            }
            
            // 分类账户
            when (analysis.classification) {
                AccountClassification.TRANSFER_PARTY -> transferPartyAccounts.add(analysis)
                AccountClassification.PAYMENT_METHOD -> paymentMethodAccounts.add(analysis)
                AccountClassification.NORMAL -> normalAccounts.add(analysis)
                else -> suspiciousAccounts.add(analysis)
            }
        }
        
        // 生成诊断报告
        android.util.Log.e("MAPPING_ANALYZER", "")
        android.util.Log.e("MAPPING_ANALYZER", "========================================")
        android.util.Log.e("MAPPING_ANALYZER", "【分析结果汇总】")
        android.util.Log.e("MAPPING_ANALYZER", "========================================")
        
        android.util.Log.e("MAPPING_ANALYZER", "")
        android.util.Log.e("MAPPING_ANALYZER", "🔴 转账对象（错误创建为账户）: ${transferPartyAccounts.size}个")
        transferPartyAccounts.forEach { 
            android.util.Log.e("MAPPING_ANALYZER", "   - ${it.account.name}: ${it.transactionCount}笔, ${it.totalAmountCents / 100.0}元")
        }
        
        android.util.Log.e("MAPPING_ANALYZER", "")
        android.util.Log.e("MAPPING_ANALYZER", "✅ 正常支付方式: ${paymentMethodAccounts.size}个")
        paymentMethodAccounts.forEach { 
            android.util.Log.e("MAPPING_ANALYZER", "   - ${it.account.name}: ${it.transactionCount}笔")
        }
        
        android.util.Log.e("MAPPING_ANALYZER", "")
        android.util.Log.e("MAPPING_ANALYZER", "⚠️ 可疑账户: ${suspiciousAccounts.size}个")
        suspiciousAccounts.forEach { 
            android.util.Log.e("MAPPING_ANALYZER", "   - ${it.account.name}: ${it.suspicionReason}")
        }
        
        // 生成修复建议
        val recommendations = generateRecommendations(
            transferPartyAccounts, 
            paymentMethodAccounts, 
            suspiciousAccounts
        )
        
        android.util.Log.e("MAPPING_ANALYZER", "")
        android.util.Log.e("MAPPING_ANALYZER", "【修复建议】")
        android.util.Log.e("MAPPING_ANALYZER", "========================================")
        recommendations.forEachIndexed { index, rec ->
            android.util.Log.e("MAPPING_ANALYZER", "${index + 1}. $rec")
        }
        
        android.util.Log.e("MAPPING_ANALYZER", "")
        android.util.Log.e("MAPPING_ANALYZER", "========================================")
        android.util.Log.e("MAPPING_ANALYZER", "          分析完成")
        android.util.Log.e("MAPPING_ANALYZER", "========================================")
        
        return MappingErrorReport(
            totalAccounts = allAccounts.size,
            totalTransactions = allTransactions.size,
            transferPartyAccounts = transferPartyAccounts,
            paymentMethodAccounts = paymentMethodAccounts,
            suspiciousAccounts = suspiciousAccounts,
            normalAccounts = normalAccounts,
            recommendations = recommendations
        )
    }
    
    /**
     * 分析单个账户
     */
    private fun analyzeAccount(
        account: AccountEntity,
        allTransactions: List<TransactionEntity>,
        allCategories: List<com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity>
    ): AccountAnalysis {
        val accountTransactions = allTransactions.filter { it.accountId == account.id }
        
        if (accountTransactions.isEmpty()) {
            return AccountAnalysis(
                account = account,
                classification = AccountClassification.TEMPORARY,
                transactionCount = 0,
                totalAmountCents = 0,
                avgAmountCents = 0,
                dateSpanDays = 0,
                categoryCount = 0,
                categories = emptyList(),
                sampleTransactions = emptyList(),
                suspicionReason = "无交易记录"
            )
        }
        
        // 计算统计信息
        val totalAmount = accountTransactions.sumOf { it.amountCents.toLong() }
        val avgAmount = totalAmount / accountTransactions.size
        val categories = accountTransactions.map { it.categoryId }.distinct()
        val categoryNames = categories.mapNotNull { catId ->
            allCategories.find { it.id == catId }?.name
        }
        
        // 计算时间跨度
        val minDate = accountTransactions.minOf { it.createdAt }
        val maxDate = accountTransactions.maxOf { it.createdAt }
        val dateSpanDays = (maxDate - minDate) / (1000 * 60 * 60 * 24)
        
        // 获取样本交易
        val sampleTransactions = accountTransactions.sortedByDescending { it.createdAt }.take(5)
        
        // 分类判断
        val (classification, reason) = classifyAccount(
            account.name,
            accountTransactions.size,
            avgAmount,
            categories.size,
            dateSpanDays,
            categoryNames
        )
        
        return AccountAnalysis(
            account = account,
            classification = classification,
            transactionCount = accountTransactions.size,
            totalAmountCents = totalAmount,
            avgAmountCents = avgAmount,
            dateSpanDays = dateSpanDays,
            categoryCount = categories.size,
            categories = categoryNames,
            sampleTransactions = sampleTransactions,
            suspicionReason = reason
        )
    }
    
    /**
     * 账户分类逻辑（优化版）
     */
    private fun classifyAccount(
        name: String,
        transactionCount: Int,
        avgAmountCents: Long,
        categoryCount: Int,
        dateSpanDays: Long,
        categoryNames: List<String>
    ): Pair<AccountClassification, String> {
        // 1. 识别转账对象（人名）- 最高优先级
        if (name.startsWith(">")) {
            return AccountClassification.TRANSFER_PARTY to "账户名以'>'开头，疑似转账标记"
        }
        
        // 2. 识别支付方式 - 扩展关键词并优先判断
        val paymentKeywords = listOf(
            "微信", "支付宝", "花呗", "白条", "信用卡", 
            "银行", "现金", "零钱", "钱包", "余额",
            "中行", "建行", "工行", "农行", "交行", // 银行简称
            "零钱通", "余额宝", "理财通", // 理财产品
            "京东", "美团" // 其他支付平台
        )
        
        // 特殊处理：包含"行"字的大概率是银行
        if (name.contains("行") && !name.startsWith(">")) {
            return AccountClassification.PAYMENT_METHOD to "包含'行'字，判断为银行账户"
        }
        
        // 包含支付关键词的直接判定为支付方式
        if (paymentKeywords.any { name.contains(it) }) {
            return AccountClassification.PAYMENT_METHOD to "包含支付关键词：${paymentKeywords.first { name.contains(it) }}"
        }
        
        // 3. 检查是否为人名（2-4个汉字，无特殊字符）- 更严格的条件
        val isPersonName = name.matches(Regex("^[\\u4e00-\\u9fa5]{2,4}$"))
        val notCommonAccount = !listOf("现金", "储蓄", "活期", "定期", "工资卡").any { name.contains(it) }
        
        if (isPersonName && notCommonAccount) {
            // 人名模式但需要更多条件判断
            if (transactionCount <= 3 && avgAmountCents > 100000) { // 交易≤3笔且平均>1000元
                return AccountClassification.TRANSFER_PARTY to "符合人名模式，极少交易且金额大"
            }
            if (transactionCount <= 5 && avgAmountCents > 50000 && categoryCount == 1) { // 单一分类
                return AccountClassification.TRANSFER_PARTY to "符合人名模式，少量交易、大额、单一分类"
            }
        }
        
        // 4. 识别可能的二级分类
        if (categoryCount == 1 && transactionCount > 20) {
            return AccountClassification.SUB_CATEGORY to "所有交易都在同一分类下，可能是二级分类"
        }
        
        // 5. 根据交易特征判断
        return when {
            transactionCount > 100 -> AccountClassification.PAYMENT_METHOD to "高频交易（>100笔），判断为支付账户"
            transactionCount > 50 -> AccountClassification.NORMAL to "中频交易（50-100笔），判断为正常账户"
            transactionCount > 10 && categoryCount > 3 -> AccountClassification.NORMAL to "有一定交易量且涉及多个分类"
            transactionCount < 5 && avgAmountCents > 100000 -> AccountClassification.TRANSFER_PARTY to "低频大额（<5笔，>1000元），判断为转账"
            dateSpanDays > 30 && categoryCount > 3 -> AccountClassification.NORMAL to "长期使用，多分类，判断为正常账户"
            transactionCount == 0 -> AccountClassification.TEMPORARY to "无交易记录"
            else -> AccountClassification.TEMPORARY to "无法明确分类"
        }
    }
    
    /**
     * 生成修复建议
     */
    private fun generateRecommendations(
        transferPartyAccounts: List<AccountAnalysis>,
        paymentMethodAccounts: List<AccountAnalysis>,
        suspiciousAccounts: List<AccountAnalysis>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (transferPartyAccounts.isNotEmpty()) {
            recommendations.add("发现${transferPartyAccounts.size}个转账对象被错误创建为账户，建议：")
            recommendations.add("  a) 将这些交易的转账对象信息移到备注字段")
            recommendations.add("  b) 根据交易类型（收入/支出）确定正确的支付账户")
            recommendations.add("  c) 删除这些错误创建的账户")
        }
        
        if (paymentMethodAccounts.isEmpty()) {
            recommendations.add("未找到有效的支付账户，需要检查导入逻辑")
        }
        
        if (suspiciousAccounts.isNotEmpty()) {
            recommendations.add("发现${suspiciousAccounts.size}个可疑账户需要人工确认")
        }
        
        // 检查是否需要查看原始CSV
        val needCheckCSV = transferPartyAccounts.size > 3 || 
                          suspiciousAccounts.size > 5
        if (needCheckCSV) {
            recommendations.add("建议重新检查钱迹CSV文件的列结构，确认字段映射是否正确")
        }
        
        return recommendations
    }
}
package com.ccxiaoji.feature.ledger.data.importer.qianji

import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.data.importer.qianji.QianjiParser.QianjiRecord
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

/**
 * 钱迹数据映射器
 * 负责将钱迹数据映射到CC小记的实体
 */
class QianjiMapper @Inject constructor(
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    
    // 钱迹分类到CC小记分类的映射表
    private val categoryMapping = mapOf(
        "下馆子" to "餐饮",
        "早餐" to "餐饮",
        "买菜" to "餐饮",
        "水果" to "水果零食",
        "饮料" to "饮料酒水",
        "零食" to "水果零食",
        "日用品" to "日用品",
        "交通" to "交通",
        "话费网费" to "通讯",
        "医疗" to "医疗",
        "美妆" to "美妆",
        "衣服" to "服饰",
        "鞋包" to "服饰",
        "学习" to "教育",
        "娱乐" to "娱乐",
        "股票基金" to "投资理财",
        "外快" to "兼职收入",
        "请客送礼" to "人情",
        "工资" to "工资",
        "奖金" to "奖金",
        "红包" to "红包",
        "其它" to "其他"
    )
    
    // 账户类型检测
    private fun detectAccountType(accountName: String): String {
        return when {
            accountName.contains("支付宝") -> "ALIPAY"
            accountName.contains("微信") -> "WECHAT"
            accountName.contains("建行") || accountName.contains("工行") || 
            accountName.contains("农行") || accountName.contains("中行") ||
            accountName.contains("交行") || accountName.contains("招行") -> "BANK_CARD"
            accountName.contains("信用卡") || accountName.contains("花呗") -> "CREDIT_CARD"
            accountName.contains("现金") -> "CASH"
            else -> "OTHER"
        }
    }
    
    // 建议分类图标
    private fun suggestCategoryIcon(category: String): String {
        return when (category) {
            "餐饮" -> "🍔"
            "交通" -> "🚗"
            "购物" -> "🛒"
            "娱乐" -> "🎮"
            "医疗" -> "🏥"
            "教育" -> "📚"
            "日用品" -> "🧻"
            "美妆" -> "💄"
            "服饰" -> "👔"
            "通讯" -> "📱"
            "水果零食" -> "🍎"
            "饮料酒水" -> "☕"
            "人情" -> "🎁"
            "工资" -> "💰"
            "奖金" -> "🏆"
            "红包" -> "🧧"
            "投资理财" -> "📈"
            "兼职收入" -> "💼"
            else -> "📝"
        }
    }
    
    // 建议账户图标
    private fun suggestAccountIcon(accountName: String): String {
        return when {
            accountName.contains("支付宝") -> "💙"
            accountName.contains("微信") -> "💚"
            accountName.contains("银行") || accountName.contains("建行") -> "🏦"
            accountName.contains("信用卡") -> "💳"
            accountName.contains("花呗") -> "🌸"
            accountName.contains("现金") -> "💵"
            else -> "💰"
        }
    }
    
    /**
     * 将钱迹记录映射为交易实体
     */
    suspend fun mapToTransaction(
        record: QianjiRecord,
        userId: String,
        createCategories: Boolean = true,
        createAccounts: Boolean = true,
        mergeSubCategories: Boolean = true
    ): TransactionEntity? {
        android.util.Log.e("QIANJI_DEBUG", "======= 开始映射交易 =======")
        android.util.Log.e("QIANJI_DEBUG", "记录ID: ${record.id}")
        android.util.Log.e("QIANJI_DEBUG", "用户ID: $userId")
        android.util.Log.e("QIANJI_DEBUG", "时间: ${record.datetime}, 类型: ${record.type}, 金额: ${record.amount}")
        android.util.Log.e("QIANJI_DEBUG", "分类: ${record.category}, 账户: ${record.account1}")
        try {
            // 解析日期时间
            val datetime = QianjiParser().parseDateTime(record.datetime)
            val timestamp = datetime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            android.util.Log.e("QIANJI_DEBUG", "解析时间戳: $timestamp")
            
            // 映射分类
            android.util.Log.e("QIANJI_DEBUG", "开始映射分类: ${record.category}, 二级: ${record.subCategory}")
            val categoryId = mapCategory(
                record.category,
                record.subCategory,
                record.type,
                userId,
                createCategories,
                mergeSubCategories
            ) ?: run {
                android.util.Log.e("QIANJI_DEBUG", "分类映射失败: ${record.category}")
                return null
            }
            android.util.Log.e("QIANJI_DEBUG", "分类映射成功: ${record.category} -> $categoryId")
            
            // 映射账户
            android.util.Log.e("QIANJI_DEBUG", "开始映射账户: ${record.account1}")
            val accountId = mapAccount(
                record.account1,
                userId,
                createAccounts
            ) ?: run {
                android.util.Log.e("QIANJI_DEBUG", "账户映射失败: ${record.account1}")
                return null
            }
            android.util.Log.e("QIANJI_DEBUG", "账户映射成功: ${record.account1} -> $accountId")
            
            // 处理金额（转换为分）
            val amount = BigDecimal(record.amount)
            var amountCents = (amount * BigDecimal(100)).toInt()
            
            // 处理退款（使用负数表示）
            if (record.type == "退款") {
                amountCents = -amountCents
            }
            
            // 生成ID（保留原始ID作为参考）
            val transactionId = UUID.randomUUID().toString()
            
            // 构建备注
            val note = buildNote(record)
            
            val transaction = TransactionEntity(
                id = transactionId,
                userId = userId,
                accountId = accountId,
                amountCents = amountCents,
                categoryId = categoryId,
                note = note,
                createdAt = timestamp,
                updatedAt = timestamp,
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
            
            android.util.Log.e("QIANJI_DEBUG", "映射成功！")
            android.util.Log.e("QIANJI_DEBUG", "交易实体: ID=$transactionId, UserID=$userId")
            android.util.Log.e("QIANJI_DEBUG", "金额: $amountCents 分, 时间戳: $timestamp")
            android.util.Log.e("QIANJI_DEBUG", "账户ID: $accountId, 分类ID: $categoryId")
            android.util.Log.e("QIANJI_DEBUG", "======= 映射结束 =======")
            
            return transaction
        } catch (e: Exception) {
            android.util.Log.e("QIANJI_DEBUG", "映射交易失败: ${e.message}", e)
            return null
        }
    }
    
    /**
     * 映射分类
     */
    private suspend fun mapCategory(
        category: String,
        subCategory: String?,
        type: String,
        userId: String,
        createIfNotExists: Boolean,
        mergeSubCategories: Boolean
    ): String? {
        // 映射到CC小记分类名称
        val mappedName = categoryMapping[category] ?: category
        
        // 合并二级分类
        val fullName = if (mergeSubCategories && !subCategory.isNullOrEmpty()) {
            "$mappedName-$subCategory"
        } else {
            mappedName
        }
        
        // 确定分类类型
        val categoryType = when (type) {
            "支出", "退款" -> "EXPENSE"
            "收入" -> "INCOME"
            else -> "EXPENSE"
        }
        
        // 查找现有分类
        val existingCategory = categoryDao.findByNameAndType(fullName, categoryType, userId)
        if (existingCategory != null) {
            return existingCategory.id
        }
        
        // 如果不创建新分类，返回null
        if (!createIfNotExists) {
            return null
        }
        
        // 创建新分类
        val newCategory = CategoryEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = fullName,
            type = categoryType,
            icon = suggestCategoryIcon(mappedName),
            color = "#6200EE",
            parentId = null,
            displayOrder = 0,
            isSystem = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = false,
            syncStatus = SyncStatus.SYNCED
        )
        
        categoryDao.insert(newCategory)
        return newCategory.id
    }
    
    /**
     * 获取或创建默认账户
     */
    private suspend fun getOrCreateDefaultAccount(userId: String): String {
        val DEFAULT_ACCOUNT_ID = "default_account_$userId"
        
        // 查找默认账户
        var account = accountDao.getAccountById(DEFAULT_ACCOUNT_ID)
        if (account == null) {
            // 创建默认账户
            account = AccountEntity(
                id = DEFAULT_ACCOUNT_ID,
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
            accountDao.insert(account)
            android.util.Log.e("QIANJI_DEBUG", "创建默认账户: $DEFAULT_ACCOUNT_ID")
        }
        return DEFAULT_ACCOUNT_ID
    }
    
    /**
     * 映射账户
     */
    private suspend fun mapAccount(
        accountName: String,
        userId: String,
        createIfNotExists: Boolean
    ): String? {
        // 空账户名统一使用默认账户
        if (accountName.isNullOrBlank()) {
            android.util.Log.e("QIANJI_DEBUG", "账户名为空，使用默认账户")
            return getOrCreateDefaultAccount(userId)
        }
        
        // 解析账户名（格式可能是：用户-账户名）
        val parts = accountName.split("-")
        val realAccountName = if (parts.size >= 2) {
            parts.last()  // 取最后一部分作为账户名
        } else {
            accountName
        }
        
        // 查找现有账户
        val existingAccount = accountDao.findByName(realAccountName, userId)
        if (existingAccount != null) {
            return existingAccount.id
        }
        
        // 如果不创建新账户，返回null
        if (!createIfNotExists) {
            return null
        }
        
        // 创建新账户
        val accountType = detectAccountType(realAccountName)
        val newAccount = AccountEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = realAccountName,
            type = accountType,
            balanceCents = 0,
            currency = "CNY",
            icon = suggestAccountIcon(realAccountName),
            color = null,
            isDefault = false,
            creditLimitCents = if (accountType == "CREDIT_CARD") 1000000L else null, // 默认10000元额度
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
        
        accountDao.insert(newAccount)
        return newAccount.id
    }
    
    /**
     * 构建备注
     */
    private fun buildNote(record: QianjiRecord): String? {
        val parts = mutableListOf<String>()
        
        // 添加原始备注
        record.remark?.let { 
            if (it.isNotEmpty()) parts.add(it)
        }
        
        // 添加二级分类信息（如果没有合并）
        record.subCategory?.let {
            if (it.isNotEmpty()) parts.add("[二级分类: $it]")
        }
        
        // 添加标签
        record.tags?.let {
            if (it.isNotEmpty()) parts.add("[标签: $it]")
        }
        
        // 添加钱迹原始ID（用于去重）
        parts.add("[钱迹ID: ${record.id}]")
        
        return if (parts.isNotEmpty()) parts.joinToString(" ") else null
    }
    
    /**
     * 检查交易是否已存在（通过钱迹ID）
     */
    suspend fun isTransactionExists(qianjiId: String, userId: String): Boolean {
        // 通过备注中的钱迹ID来判断是否重复
        val pattern = "%[钱迹ID: $qianjiId]%"
        return transactionDao.existsByNote(pattern, userId)
    }
}
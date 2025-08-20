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
                ledgerId = "default", // 默认记账簿
                createdAt = timestamp,
                updatedAt = timestamp,
                transactionDate = timestamp,
                locationLatitude = null,
                locationLongitude = null,
                locationAddress = null,
                locationPrecision = null,
                locationProvider = null,
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
     * 映射分类 - 支持二级分类结构
     */
    @Suppress("UNUSED_PARAMETER")
    private suspend fun mapCategory(
        category: String,
        subCategory: String?,
        type: String,
        userId: String,
        createIfNotExists: Boolean,
        mergeSubCategories: Boolean
    ): String? {
        // 获取映射的父子分类
        val (parentName, childName) = QianjiCategoryMapping.getMappedCategory(
            category, 
            subCategory, 
            type
        )
        
        // 确定分类类型
        val categoryType = when (type) {
            "支出", "退款" -> "EXPENSE"
            "收入" -> "INCOME"
            else -> "EXPENSE"
        }
        
        // 如果有子分类名，查找或创建二级分类
        if (childName != null) {
            // 先查找父分类
            var parentCategory = categoryDao.findByNameAndType(parentName, categoryType, userId)
            
            // 如果父分类不存在，需要先创建
            if (parentCategory == null) {
                if (!createIfNotExists) {
                    return null
                }
                
                // 创建父分类
                val parentId = UUID.randomUUID().toString()
                parentCategory = CategoryEntity(
                    id = parentId,
                    userId = userId,
                    name = parentName,
                    type = categoryType,
                    icon = QianjiCategoryMapping.suggestCategoryIcon(parentName),
                    color = QianjiCategoryMapping.suggestCategoryColor(parentName),
                    parentId = null,  // 父分类没有parent
                    displayOrder = 0,
                    isSystem = false,
                    usageCount = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = false,
                    syncStatus = SyncStatus.SYNCED
                )
                categoryDao.insert(parentCategory)
                android.util.Log.e("QIANJI_DEBUG", "创建父分类: $parentName (ID: $parentId)")
            }
            
            // 查找子分类
            val childCategory = categoryDao.findByNameAndParent(childName, parentCategory.id, userId)
            if (childCategory != null) {
                return childCategory.id
            }
            
            // 如果不创建新分类，返回null
            if (!createIfNotExists) {
                return null
            }
            
            // 创建子分类
            val childId = UUID.randomUUID().toString()
            val newChildCategory = CategoryEntity(
                id = childId,
                userId = userId,
                name = childName,
                type = categoryType,
                icon = QianjiCategoryMapping.suggestCategoryIcon(parentName, childName),
                color = parentCategory.color,  // 继承父分类颜色
                parentId = parentCategory.id,  // 设置父分类ID
                displayOrder = 0,
                isSystem = false,
                usageCount = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
            categoryDao.insert(newChildCategory)
            android.util.Log.e("QIANJI_DEBUG", "创建子分类: $childName (ID: $childId, 父分类: ${parentCategory.name})")
            return childId
            
        } else {
            // 没有子分类，作为一级分类处理（实际上应该很少出现）
            val existingCategory = categoryDao.findByNameAndType(parentName, categoryType, userId)
            if (existingCategory != null) {
                // 如果是父分类，需要找到或创建一个默认子分类
                val defaultChild = categoryDao.findByNameAndParent("一般", existingCategory.id, userId)
                if (defaultChild != null) {
                    return defaultChild.id
                }
                
                if (!createIfNotExists) {
                    return null
                }
                
                // 创建默认子分类
                val defaultChildId = UUID.randomUUID().toString()
                val newDefaultChild = CategoryEntity(
                    id = defaultChildId,
                    userId = userId,
                    name = "一般",
                    type = categoryType,
                    icon = existingCategory.icon,
                    color = existingCategory.color,
                    parentId = existingCategory.id,
                    displayOrder = 0,
                    isSystem = false,
                    usageCount = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = false,
                    syncStatus = SyncStatus.SYNCED
                )
                categoryDao.insert(newDefaultChild)
                android.util.Log.e("QIANJI_DEBUG", "创建默认子分类: 一般 (父分类: ${existingCategory.name})")
                return defaultChildId
            }
            
            // 如果不创建新分类，返回null
            if (!createIfNotExists) {
                return null
            }
            
            // 创建新的父分类和默认子分类
            val parentId = UUID.randomUUID().toString()
            val newParent = CategoryEntity(
                id = parentId,
                userId = userId,
                name = parentName,
                type = categoryType,
                icon = QianjiCategoryMapping.suggestCategoryIcon(parentName),
                color = QianjiCategoryMapping.suggestCategoryColor(parentName),
                parentId = null,
                displayOrder = 0,
                isSystem = false,
                usageCount = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
            categoryDao.insert(newParent)
            
            // 创建默认子分类
            val defaultChildId = UUID.randomUUID().toString()
            val defaultChild = CategoryEntity(
                id = defaultChildId,
                userId = userId,
                name = "一般",
                type = categoryType,
                icon = newParent.icon,
                color = newParent.color,
                parentId = parentId,
                displayOrder = 0,
                isSystem = false,
                usageCount = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
            categoryDao.insert(defaultChild)
            android.util.Log.e("QIANJI_DEBUG", "创建父分类和默认子分类: $parentName/一般")
            return defaultChildId
        }
    }
    
    /**
     * 获取或创建现金账户（用于承载空账户名的交易）
     */
    private suspend fun getOrCreateCashAccount(userId: String): String {
        val CASH_ACCOUNT_ID = "default_account_$userId"
        
        // 查找现金账户
        var account = accountDao.getAccountById(CASH_ACCOUNT_ID)
        if (account == null) {
            // 创建现金账户
            account = AccountEntity(
                id = CASH_ACCOUNT_ID,
                userId = userId,
                name = "现金",  // 使用"现金"而不是"默认账户"
                type = "CASH",
                balanceCents = 0,
                currency = "CNY",
                icon = "💵",  // 使用现金图标
                color = "#4CAF50",  // 绿色
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
            android.util.Log.e("QIANJI_DEBUG", "创建现金账户: $CASH_ACCOUNT_ID")
        }
        return CASH_ACCOUNT_ID
    }
    
    // 保留原方法名以保持兼容性
    private suspend fun getOrCreateDefaultAccount(userId: String): String {
        return getOrCreateCashAccount(userId)
    }
    
    /**
     * 映射账户
     */
    private suspend fun mapAccount(
        accountName: String,
        userId: String,
        createIfNotExists: Boolean
    ): String? {
        // 空账户名统一使用现金账户
        if (accountName.isNullOrBlank()) {
            android.util.Log.e("QIANJI_DEBUG", "账户名为空，使用现金账户")
            return getOrCreateCashAccount(userId)
        }
        
        // 检查是否为转账对象（以">"开头的不应创建为账户）
        if (accountName.startsWith(">")) {
            android.util.Log.e("QIANJI_DEBUG", "跳过转账对象账户: $accountName，使用现金账户")
            return getOrCreateCashAccount(userId)
        }
        
        // 直接使用原始账户名，不要分割
        val realAccountName = accountName
        
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
        
        // 添加转账对象信息（account2字段）
        record.account2?.let {
            if (it.isNotEmpty()) {
                // 判断是收入还是支出
                val prefix = when (record.type) {
                    "收入" -> "付款方"
                    "支出" -> "收款方"
                    else -> "转账对象"
                }
                parts.add("[$prefix: ${it.removePrefix(">")}]")
            }
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
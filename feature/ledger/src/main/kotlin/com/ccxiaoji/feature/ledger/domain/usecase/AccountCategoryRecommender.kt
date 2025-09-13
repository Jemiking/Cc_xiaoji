package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.model.PaymentDirection
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 账户分类推荐器
 * 
 * 基于支付通知的信息（来源、商户、支付方式等）
 * 智能推荐最合适的账户、分类和记账簿
 */
@Singleton
class AccountCategoryRecommender @Inject constructor(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val ledgerRepository: LedgerRepository,
    private val transactionRepository: TransactionRepository,
    private val userApi: com.ccxiaoji.shared.user.api.UserApi,
    private val manageLedgerUseCase: com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
) {
    
    /**
     * 商户分类映射
     * 基于商户名称推荐分类
     */
    private val merchantCategoryMap = mapOf(
        // 餐饮
        "星巴克" to "FOOD_COFFEE",
        "麦当劳" to "FOOD_FASTFOOD", 
        "肯德基" to "FOOD_FASTFOOD",
        "海底捞" to "FOOD_RESTAURANT",
        "必胜客" to "FOOD_RESTAURANT",
        
        // 交通
        "滴滴" to "TRANSPORT_TAXI",
        "uber" to "TRANSPORT_TAXI",
        "中国石油" to "TRANSPORT_GAS",
        "中国石化" to "TRANSPORT_GAS",
        
        // 购物
        "苹果" to "SHOPPING_ELECTRONICS",
        "华为" to "SHOPPING_ELECTRONICS",
        "小米" to "SHOPPING_ELECTRONICS",
        "优衣库" to "SHOPPING_CLOTHING",
        "zara" to "SHOPPING_CLOTHING",
        
        // 生活服务
        "美团" to "LIFE_SERVICE",
        "大众点评" to "LIFE_SERVICE",
        "饿了么" to "FOOD_DELIVERY",
        
        // 娱乐
        "电影院" to "ENTERTAINMENT_MOVIE",
        "ktv" to "ENTERTAINMENT_KTV",
        
        // 医疗
        "医院" to "MEDICAL_HOSPITAL",
        "药店" to "MEDICAL_PHARMACY"
    )
    
    /**
     * 支付方式账户映射
     */
    private val paymentMethodAccountMap = mapOf(
        "余额宝" to "alipay_yuebao",
        "花呗" to "alipay_huabei", 
        "微信零钱" to "wechat_balance",
        "支付宝余额" to "alipay_balance"
    )
    
    /**
     * 为支付通知推荐账户和分类
     */
    suspend fun recommend(notification: PaymentNotification): AccountCategoryRecommendation =
        withContext(Dispatchers.IO) {
            try {
                val userId = userApi.getCurrentUserId()
                
                // 获取默认记账簿（真实）
                val ledgerId = try {
                    val def = manageLedgerUseCase.getDefaultLedger(userId)
                    if (def is com.ccxiaoji.common.base.BaseResult.Success) def.data.id else ""
                } catch (_: Exception) { "" }
                
                // 1. 推荐账户
                val recommendedAccountId = recommendAccount(notification, userId)
                
                // 2. 推荐分类
                val recommendedCategoryId = recommendCategory(notification, userId)
                
                // 3. 计算推荐置信度
                val confidence = calculateRecommendationConfidence(
                    notification,
                    recommendedAccountId != null,
                    recommendedCategoryId != null
                )
                
                // 4. 生成推荐理由
                val reason = generateRecommendationReason(
                    notification,
                    recommendedAccountId,
                    recommendedCategoryId
                )
                
                AccountCategoryRecommendation(
                    accountId = recommendedAccountId,
                    categoryId = recommendedCategoryId,
                    ledgerId = ledgerId,
                    confidence = confidence,
                    reason = reason
                )
                
            } catch (e: Exception) {
                // 失败时返回默认推荐
                getDefaultRecommendation()
            }
        }
    
    /**
     * 推荐账户
     */
    private suspend fun recommendAccount(
        notification: PaymentNotification,
        userId: String
    ): String? {
        val allAccounts = accountRepository.getAccounts().first()

        // 1) 支付方式关键词 → 账户
        notification.paymentMethod?.let { method ->
            val m = method.lowercase()
            when {
                m.contains("余额宝") || m.contains("yuebao") -> {
                    val hit = allAccounts.firstOrNull { it.type == com.ccxiaoji.feature.ledger.domain.model.AccountType.ALIPAY || it.name.contains("余额宝") || it.name.contains("支付宝") }
                    if (hit != null) return hit.id
                }
                m.contains("微信零钱") || m.contains("零钱") || m.contains("wechat") -> {
                    val hit = allAccounts.firstOrNull { it.type == com.ccxiaoji.feature.ledger.domain.model.AccountType.WECHAT || it.name.contains("微信") }
                    if (hit != null) return hit.id
                }
            }
        }

        // 2) 来源应用 → 账户
        when (notification.sourceType) {
            PaymentSourceType.ALIPAY -> {
                val hit = allAccounts.firstOrNull { it.type == com.ccxiaoji.feature.ledger.domain.model.AccountType.ALIPAY || it.name.contains("支付宝", true) }
                if (hit != null) return hit.id
            }
            PaymentSourceType.WECHAT -> {
                val hit = allAccounts.firstOrNull { it.type == com.ccxiaoji.feature.ledger.domain.model.AccountType.WECHAT || it.name.contains("微信", true) }
                if (hit != null) return hit.id
            }
            PaymentSourceType.UNIONPAY -> {
                val hit = allAccounts.firstOrNull { it.name.contains("云闪付", true) || it.name.contains("UnionPay", true) }
                if (hit != null) return hit.id
            }
            else -> {}
        }

        // 3) 历史最常用（预留，当前返回null）
        if (!notification.normalizedMerchant.isNullOrBlank()) {
            findMostUsedAccountForMerchant(notification.normalizedMerchant, userId)?.let { return it }
        }

        // 4) 默认账户
        accountRepository.getDefaultAccount()?.let { return it.id }
        return allAccounts.firstOrNull()?.id
    }
    
    /**
     * 推荐分类
     */
    private suspend fun recommendCategory(
        notification: PaymentNotification,
        userId: String
    ): String? {
        val targetType = if (notification.direction == PaymentDirection.INCOME)
            com.ccxiaoji.feature.ledger.domain.model.Category.Type.INCOME
        else
            com.ccxiaoji.feature.ledger.domain.model.Category.Type.EXPENSE

        val relevantCategories = try {
            categoryRepository.getLeafCategories(userId, targetType.name)
        } catch (_: Exception) {
            // 回退到按类型的流首帧
            categoryRepository.getCategoriesByType(targetType).first()
        }

        // 1) 商户名 → 模糊匹配关键词到现有分类
        notification.normalizedMerchant?.let { merchant ->
            val fuzzyMatch = findCategoryByFuzzyMatching(merchant, relevantCategories)
            if (fuzzyMatch != null) return fuzzyMatch.id
        }

        // 2) 历史最常用（预留）
        if (!notification.normalizedMerchant.isNullOrBlank()) {
            findMostUsedCategoryForMerchant(notification.normalizedMerchant, userId)?.let { return it }
        }

        // 3) 金额范围启发
        recommendCategoryByAmount(notification.amountCents, relevantCategories)?.let { return it.id }

        // 4) 常用/默认
        try {
            val frequent = categoryRepository.getFrequentCategories(userId, targetType.name, 1)
            if (frequent.isNotEmpty()) return frequent.first().id
        } catch (_: Exception) {}

        return relevantCategories.firstOrNull()?.id
    }
    
    /**
     * 查找商户最常用的账户
     */
    private suspend fun findMostUsedAccountForMerchant(
        merchant: String,
        userId: String
    ): String? {
        return try {
            // 这里应该查询历史交易，找出该商户最常用的账户
            // 简化实现，实际可能需要更复杂的查询
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 查找商户最常用的分类
     */
    private suspend fun findMostUsedCategoryForMerchant(
        merchant: String,
        userId: String
    ): String? {
        return try {
            // 这里应该查询历史交易，找出该商户最常用的分类
            // 简化实现，实际可能需要更复杂的查询
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 通过模糊匹配查找分类
     */
    private fun findCategoryByFuzzyMatching(
        merchant: String,
        categories: List<com.ccxiaoji.feature.ledger.domain.model.Category>
    ): com.ccxiaoji.feature.ledger.domain.model.Category? {
        val merchantLower = merchant.lowercase()
        
        // 餐饮相关关键词
        val foodKeywords = setOf("餐", "食", "饭", "吃", "咖啡", "茶", "酒", "饮", "厅", "店")
        // 交通相关关键词  
        val transportKeywords = setOf("交通", "出行", "车", "油", "停车", "地铁", "公交")
        // 购物相关关键词
        val shoppingKeywords = setOf("购", "买", "商", "店", "超市", "mall")
        
        return when {
            foodKeywords.any { merchantLower.contains(it) } -> {
                categories.find { it.name.contains("餐饮", ignoreCase = true) || 
                                 it.name.contains("食物", ignoreCase = true) }
            }
            transportKeywords.any { merchantLower.contains(it) } -> {
                categories.find { it.name.contains("交通", ignoreCase = true) ||
                                 it.name.contains("出行", ignoreCase = true) }
            }
            shoppingKeywords.any { merchantLower.contains(it) } -> {
                categories.find { it.name.contains("购物", ignoreCase = true) ||
                                 it.name.contains("消费", ignoreCase = true) }
            }
            else -> null
        }
    }
    
    /**
     * 根据金额推荐分类
     */
    private fun recommendCategoryByAmount(
        amountCents: Long,
        categories: List<com.ccxiaoji.feature.ledger.domain.model.Category>
    ): com.ccxiaoji.feature.ledger.domain.model.Category? {
        val amount = amountCents / 100.0
        
        return when {
            amount < 50 -> {
                // 小额：可能是日常消费
                categories.find { it.name.contains("日常", ignoreCase = true) ||
                                 it.name.contains("零食", ignoreCase = true) }
            }
            amount < 200 -> {
                // 中等：可能是餐饮
                categories.find { it.name.contains("餐饮", ignoreCase = true) }
            }
            amount < 1000 -> {
                // 较高：可能是购物
                categories.find { it.name.contains("购物", ignoreCase = true) }
            }
            else -> {
                // 大额：可能是大件商品或服务
                categories.find { it.name.contains("大件", ignoreCase = true) ||
                                 it.name.contains("服务", ignoreCase = true) }
            }
        }
    }
    
    /**
     * 计算推荐置信度
     */
    private fun calculateRecommendationConfidence(
        notification: PaymentNotification,
        hasAccountRecommendation: Boolean,
        hasCategoryRecommendation: Boolean
    ): Double {
        var confidence = 0.0
        
        // 基础分
        confidence += 0.3
        
        // 账户推荐加分
        if (hasAccountRecommendation) confidence += 0.2
        
        // 分类推荐加分
        if (hasCategoryRecommendation) confidence += 0.2
        
        // 商户信息加分
        if (!notification.normalizedMerchant.isNullOrBlank()) confidence += 0.1
        
        // 支付方式加分
        if (!notification.paymentMethod.isNullOrBlank()) confidence += 0.1
        
        // 解析置信度影响
        confidence *= notification.confidence
        
        return minOf(1.0, confidence)
    }
    
    /**
     * 生成推荐理由
     */
    private fun generateRecommendationReason(
        notification: PaymentNotification,
        accountId: String?,
        categoryId: String?
    ): String {
        val reasons = mutableListOf<String>()
        
        if (accountId != null) {
            notification.paymentMethod?.let {
                reasons.add("基于支付方式: $it")
            } ?: run {
                reasons.add("基于来源应用: ${notification.sourceType.displayName}")
            }
        }
        
        if (categoryId != null) {
            notification.normalizedMerchant?.let {
                reasons.add("基于商户: $it")
            } ?: run {
                reasons.add("基于交易类型")
            }
        }
        
        return if (reasons.isNotEmpty()) {
            reasons.joinToString(", ")
        } else {
            "使用默认设置"
        }
    }
    
    /**
     * 获取默认推荐
     */
    private fun getDefaultRecommendation(): AccountCategoryRecommendation {
        return AccountCategoryRecommendation(
            accountId = null,
            categoryId = null,
            ledgerId = "default_ledger",
            confidence = 0.3,
            reason = "使用默认设置"
        )
    }
}

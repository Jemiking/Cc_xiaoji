package com.ccxiaoji.feature.ledger.domain.parser

import android.util.Log
import com.ccxiaoji.feature.ledger.domain.model.PaymentDirection
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent

/**
 * 支付宝通知解析器
 * 
 * 专门处理支付宝支付、转账、收款、退款等场景的通知解析
 */
class AlipayNotificationParser : AbstractNotificationParser() {
    
    companion object {
        private const val TAG = "AutoLedger_Parser"
    }
    
    override val supportedPackages = setOf("com.eg.android.AlipayGphone")
    override val parserName = "AlipayNotificationParser"
    override val version = 1
    
    /**
     * canParse 仅校验包名是否受支持。
     * 具体有效性在 parse 中依据“方向+金额”等规则判定，避免关键词过严导致漏判。
     */
    override fun canParse(event: RawNotificationEvent): Boolean {
        Log.d(TAG, "🔍 开始解析预检查: ${event.packageName}")
        if (event.packageName !in supportedPackages) {
            Log.v(TAG, "⚪ 不支持的包名: ${event.packageName}")
            return false
        }
        Log.d(TAG, "✅ 支付宝包名匹配")
        return true
    }
    
    /**
     * 支付宝特有的交易类型关键词
     */
    private val alipayExpenseKeywords = setOf(
        "向【", "付款", "支付成功", "扫码支付", "刷脸支付", "消费", "支出"
    )
    
    private val alipayIncomeKeywords = setOf(
        "到账", "收钱码", "付钱码", "收款", "余额宝收益"
    )
    
    private val alipayTransferKeywords = setOf(
        "转账给", "转账到", "已转账"
    )
    
    private val alipayRefundKeywords = setOf(
        "退款", "已退回", "撤销", "已撤销"
    )
    
    override fun parse(event: RawNotificationEvent): PaymentNotification? {
        Log.i(TAG, "🚀 开始支付宝通知解析")
        
        val title = event.title.orEmpty()
        val text = event.text.orEmpty()
        val fullText = "$title $text"

        Log.d(TAG, "📋 解析输入 - 标题: '$title', 文本: '$text'")
        
        // 先走快速路径：只要能识别到“方向+金额”，立即判定为有效交易
        val quickDirection = detectDirectionQuick(fullText)
        val quickAmountCents = extractAmount(fullText)
        if (quickDirection != PaymentDirection.UNKNOWN && quickAmountCents != null && quickAmountCents > 0) {
            Log.i(TAG, "✅ 快速解析命中: direction=$quickDirection, amount=${quickAmountCents}分")
            val rawMerchant = extractMerchant(fullText) ?: extractAlipayMerchant(fullText)
            val normalizedMerchant = normalizeMerchant(rawMerchant)
            val paymentMethod = inferAlipayPaymentMethod(fullText)
            return PaymentNotification(
                sourceApp = event.packageName,
                sourceType = PaymentSourceType.ALIPAY,
                direction = quickDirection,
                amountCents = quickAmountCents,
                rawMerchant = rawMerchant,
                normalizedMerchant = normalizedMerchant,
                paymentMethod = paymentMethod,
                postedTime = event.postTime,
                notificationKey = event.notificationKey,
                parserVersion = version,
                confidence = 0.9,
                rawText = null,
                originalTitle = event.title ?: "",
                originalText = event.text ?: "",
                fingerprint = generateFingerprint(event),
                tags = extractAlipayTags(fullText)
            )
        }
        
        // 常规路径：先进行无效过滤，再按原有规则评分
        if (isInvalidAlipayNotification(fullText)) {
            Log.i(TAG, "⚪ 支付宝无效通知过滤: '$fullText'")
            return null
        }
        Log.d(TAG, "✅ 通过无效通知过滤")
        
        // 提取金额
        val amountCents = extractAmount(fullText)
        Log.d(TAG, "💰 金额提取结果: $amountCents 分")
        
        if (amountCents == null || amountCents <= 0) {
            Log.w(TAG, "❌ 金额提取失败或为0，停止解析")
            return null
        }
        
        // 提取商户
        val rawMerchant = extractMerchant(fullText) ?: extractAlipayMerchant(fullText)
        val normalizedMerchant = normalizeMerchant(rawMerchant)
        Log.d(TAG, "🏪 商户信息 - 原始: '$rawMerchant', 标准化: '$normalizedMerchant'")
        
        // 推断交易方向
        val direction = inferAlipayDirection(fullText)
        Log.d(TAG, "🧭 交易方向: $direction")
        
        // 提取支付方式
        val paymentMethod = inferAlipayPaymentMethod(fullText)
        Log.d(TAG, "💳 支付方式: $paymentMethod")
        
        // 提取标签
        val tags = extractAlipayTags(fullText)
        Log.d(TAG, "🏷️ 标签: $tags")
        
        // 计算置信度
        val keywordMatchScore = getKeywordMatchScore(fullText)
        Log.d(TAG, "📊 关键词匹配得分: $keywordMatchScore")
        
        val confidence = calculateAlipayConfidence(
            hasAmount = amountCents > 0,
            hasMerchant = rawMerchant != null,
            hasDirection = direction != PaymentDirection.UNKNOWN,
            hasMethod = paymentMethod != null,
            keywordMatch = keywordMatchScore
        )
        
        Log.i(TAG, "📈 最终置信度: $confidence")
        
        // 低置信度直接返回null
        if (confidence < 0.6) {
            Log.w(TAG, "❌ 置信度过低 ($confidence < 0.6)，停止解析")
            return null
        }
        
        Log.i(TAG, "🎯 解析成功！创建PaymentNotification")
        Log.d(TAG, "💯 最终结果: 金额=${amountCents}分, 商户='$normalizedMerchant', 方向=$direction, 置信度=$confidence")
        
        return PaymentNotification(
            sourceApp = event.packageName,
            sourceType = PaymentSourceType.ALIPAY,
            direction = direction,
            amountCents = amountCents,
            rawMerchant = rawMerchant,
            normalizedMerchant = normalizedMerchant,
            paymentMethod = paymentMethod,
            postedTime = event.postTime,
            notificationKey = event.notificationKey,
            parserVersion = version,
            confidence = confidence,
            rawText = if (confidence < 0.8) fullText else null, // 低置信度保留原文用于调试
            originalTitle = event.title ?: "",
            originalText = event.text ?: "",
            fingerprint = generateFingerprint(event),
            tags = tags
        )
    }
    
    /**
     * 生成通知指纹用于去重
     */
    private fun generateFingerprint(event: RawNotificationEvent): String {
        val content = "${event.title ?: ""}_${event.text ?: ""}"
        return "${event.packageName}_${event.postTime}_${content.hashCode()}"
    }
    
    /**
     * 支付宝专用的无效通知过滤
     * 排除快递、消息、系统通知等非支付相关内容
     */
    private fun isInvalidAlipayNotification(fullText: String): Boolean {
        val lowercaseText = fullText.lowercase()
        
        // 排除关键词 - 明确的非支付场景
        val excludeKeywords = setOf(
            "快递", "包裹", "物流", "配送", "派送", "取件",
            "消息", "聊天", "会话", "好友",
            "余额变动提醒", "账单已生成", "系统维护",
            "活动", "优惠", "券", "积分", "签到",
            "天气", "新闻", "通知设置",
            "更新", "升级", "版本"
        )
        
        // 如果包含排除关键词，视为无效
        if (excludeKeywords.any { lowercaseText.contains(it) }) {
            return true
        }
        
        // 特殊规则：如果没有金额但有"到达"等词，可能是快递通知
        if (!hasAmountPattern(fullText) && 
            (lowercaseText.contains("到达") || lowercaseText.contains("签收"))) {
            return true
        }
        
        return false
    }

    /**
     * 快速识别方向：命中常见方向词即可。
     */
    private fun detectDirectionQuick(text: String): PaymentDirection {
        val t = text
        return when {
            t.contains("收入") || t.contains("收款") || t.contains("已收款") || t.contains("到账") || t.contains("入账") ->
                PaymentDirection.INCOME
            t.contains("支出") || t.contains("付款") || t.contains("扣款") || t.contains("支付成功") || t.contains("已支付") ->
                PaymentDirection.EXPENSE
            else -> PaymentDirection.UNKNOWN
        }
    }
    
    /**
     * 检查文本是否包含金额模式
     */
    private fun hasAmountPattern(text: String): Boolean {
        return extractAmount(text) != null
    }
    
    /**
     * 支付宝特有的商户提取逻辑
     */
    private fun extractAlipayMerchant(text: String): String? {
        // 支付宝常见格式：
        // "向【星巴克咖啡】付款28.50元"
        // "你向星巴克咖啡付款￥28.50"
        // "转账给张三"
        
        // 优先使用通用的【】格式
        extractMerchant(text)?.let { return it }
        
        // 支付宝特殊格式
        val patterns = listOf(
            """你向(.+?)付款""".toRegex(),
            """向(.+?)付款""".toRegex(),
            """转账给([^0-9￥¥]+?)(?:[0-9￥¥]|$)""".toRegex(), // 改进转账模式，匹配到数字或金额符号前
            """收到(.+?)付款""".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        
        return null
    }
    
    /**
     * 推断支付宝交易方向
     */
    private fun inferAlipayDirection(text: String): PaymentDirection {
        val lowercaseText = text.lowercase()
        Log.d(TAG, "🧭 推断交易方向，输入文本: '$text'")
        Log.d(TAG, "🔍 小写文本: '$lowercaseText'")
        
        // 检查退款关键词
        val refundMatches = alipayRefundKeywords.filter { lowercaseText.contains(it) }
        if (refundMatches.isNotEmpty()) {
            Log.i(TAG, "💸 匹配退款关键词: $refundMatches")
            return PaymentDirection.REFUND
        }
        
        // 检查收入关键词
        val incomeMatches = alipayIncomeKeywords.filter { lowercaseText.contains(it) }
        if (incomeMatches.isNotEmpty()) {
            Log.i(TAG, "💰 匹配收入关键词: $incomeMatches")
            return PaymentDirection.INCOME
        }
        
        // 检查转账关键词
        val transferMatches = alipayTransferKeywords.filter { lowercaseText.contains(it) }
        if (transferMatches.isNotEmpty()) {
            Log.i(TAG, "💱 匹配转账关键词: $transferMatches")
            return PaymentDirection.TRANSFER
        }
        
        // 检查支出关键词（重点）
        val expenseMatches = alipayExpenseKeywords.filter { lowercaseText.contains(it) }
        if (expenseMatches.isNotEmpty()) {
            Log.i(TAG, "💳 匹配支出关键词: $expenseMatches (包含'支出')")
            return PaymentDirection.EXPENSE
        }
        
        Log.w(TAG, "❓ 无法匹配任何支付宝专用关键词，使用基类方法")
        Log.d(TAG, "📋 支出关键词列表: $alipayExpenseKeywords")
        
        val fallbackDirection = inferDirection(text, text)
        Log.d(TAG, "🔄 基类方法返回: $fallbackDirection")
        
        return fallbackDirection
    }
    
    /**
     * 推断支付宝支付方式
     */
    private fun inferAlipayPaymentMethod(text: String): String? {
        val lowercaseText = text.lowercase()
        
        return when {
            lowercaseText.contains("余额宝") -> "余额宝"
            lowercaseText.contains("花呗") -> "花呗"
            lowercaseText.contains("借呗") -> "借呗"
            lowercaseText.contains("网商银行") -> "网商银行"
            lowercaseText.contains("余额") -> "支付宝余额"
            else -> inferPaymentMethod(text) // 使用基类方法
        }
    }
    
    /**
     * 提取支付宝特有标签
     */
    private fun extractAlipayTags(text: String): Set<String> {
        val tags = mutableSetOf<String>()
        val lowercaseText = text.lowercase()
        
        when {
            lowercaseText.contains("扫码") -> tags.add("扫码支付")
            lowercaseText.contains("刷脸") -> tags.add("刷脸支付")
            lowercaseText.contains("收钱码") -> tags.add("收钱码")
            lowercaseText.contains("付钱码") -> tags.add("付钱码")
            lowercaseText.contains("转账") -> tags.add("转账")
            lowercaseText.contains("红包") -> tags.add("红包")
            lowercaseText.contains("余额宝收益") -> tags.add("余额宝收益")
        }
        
        return tags
    }
    
    /**
     * 计算支付宝专用置信度
     */
    private fun calculateAlipayConfidence(
        hasAmount: Boolean,
        hasMerchant: Boolean,
        hasDirection: Boolean,
        hasMethod: Boolean,
        keywordMatch: Double
    ): Double {
        var confidence = calculateConfidence(hasAmount, hasMerchant, hasDirection, hasMethod)
        
        // 支付宝专用优化：对于支付宝通知，我们可以更加信任
        if (hasAmount && hasDirection) {
            confidence += 0.15 // 有金额和方向的额外加成
        }
        
        // 支付宝官方应用可信度提升
        confidence += 0.05 // 支付宝官方应用基础信任度加成
        
        // 支付宝特有的置信度加成（提高比例）
        confidence += keywordMatch * 0.3 // 关键词匹配度加成（从0.2提高到0.3）
        
        // 对于支付宝特有关键词给予额外加成
        if (keywordMatch > 0.5) {
            confidence += 0.1 // 高关键词匹配度额外奖励
        }
        
        // 完整信息奖励：如果有金额、商户和方向，给予额外奖励
        if (hasAmount && hasMerchant && hasDirection) {
            confidence += 0.05 // 完整支付信息奖励
        }
        
        return minOf(1.0, confidence)
    }
    
    /**
     * 计算关键词匹配得分
     */
    private fun getKeywordMatchScore(text: String): Double {
        val lowercaseText = text.lowercase()
        val allKeywords = alipayExpenseKeywords + alipayIncomeKeywords + 
                         alipayTransferKeywords + alipayRefundKeywords
        
        val matchedCount = allKeywords.count { lowercaseText.contains(it) }
        return if (allKeywords.isNotEmpty()) {
            matchedCount.toDouble() / allKeywords.size
        } else {
            0.0
        }
    }
}

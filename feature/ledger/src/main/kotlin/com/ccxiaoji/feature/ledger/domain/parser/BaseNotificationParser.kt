package com.ccxiaoji.feature.ledger.domain.parser

import com.ccxiaoji.feature.ledger.domain.model.PaymentDirection
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent

/**
 * 通知解析器基础接口
 * 
 * 定义了将原始通知事件转换为结构化支付信息的契约
 */
interface BaseNotificationParser {
    
    /**
     * 支持的应用包名
     */
    val supportedPackages: Set<String>
    
    /**
     * 解析器名称
     */
    val parserName: String
    
    /**
     * 解析器版本
     */
    val version: Int
    
    /**
     * 检查是否支持该通知
     */
    fun canParse(event: RawNotificationEvent): Boolean
    
    /**
     * 解析通知事件
     * 
     * @param event 原始通知事件
     * @return 解析结果，失败时返回null
     */
    fun parse(event: RawNotificationEvent): PaymentNotification?
}

/**
 * 抽象解析器基类
 * 
 * 提供通用的解析逻辑和工具方法
 */
abstract class AbstractNotificationParser : BaseNotificationParser {
    
    /**
     * 支付关键词模式
     */
    protected val paymentKeywords = setOf(
        "付款", "支付", "收款", "转账", "退款", "到账", "余额", 
        "成功", "失败", "红包", "零钱", "银行卡", "信用卡", "消费"
    )
    
    /**
     * 金额正则表达式
     * 匹配：￥/¥/元、小数/千分位、空格等
     */
    protected val amountRegex = """[￥¥]?\s?([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})|[0-9]+(?:\.[0-9]{1,2}))\s?元?""".toRegex()
    
    /**
     * 商户名正则表达式
     * 匹配：【商户名】或［商户名］
     */
    protected val merchantRegex = """[【\[](.+?)[】\]]""".toRegex()
    
    override fun canParse(event: RawNotificationEvent): Boolean {
        return event.packageName in supportedPackages && 
               containsPaymentKeywords(event.title, event.text)
    }
    
    /**
     * 检查文本是否包含支付关键词
     */
    protected fun containsPaymentKeywords(title: String?, text: String?): Boolean {
        val content = "${title.orEmpty()} ${text.orEmpty()}".lowercase()
        return paymentKeywords.any { keyword -> content.contains(keyword) }
    }
    
    /**
     * 提取金额（返回分为单位）
     */
    protected fun extractAmount(text: String): Long? {
        val match = amountRegex.find(text) ?: return null
        val amountStr = match.groupValues[1].replace(",", "")
        return try {
            (amountStr.toDouble() * 100).toLong()
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    /**
     * 提取商户名称
     */
    protected fun extractMerchant(text: String): String? {
        val match = merchantRegex.find(text)
        return match?.groupValues?.get(1)?.trim()
    }
    
    /**
     * 归一化商户名称
     * 去除表情、广告词等
     */
    protected fun normalizeMerchant(rawMerchant: String?): String? {
        if (rawMerchant.isNullOrBlank()) return null
        
        return rawMerchant
            .replace("\\uD83C[\\uDF00-\\uDFFF]|\\uD83D[\\uDC00-\\uDDFF]".toRegex(), "") // 去除表情
            .replace("[（）()（）]".toRegex(), "") // 去除括号
            .replace("官方|旗舰店|专营店".toRegex(), "") // 去除常见商业词汇
            .trim()
            .takeIf { it.isNotEmpty() }
    }
    
    /**
     * 推断交易方向
     */
    protected fun inferDirection(title: String?, text: String?): PaymentDirection {
        val content = "${title.orEmpty()} ${text.orEmpty()}".lowercase()
        
        return when {
            content.contains("退款") || content.contains("撤销") -> PaymentDirection.REFUND
            content.contains("收款") || content.contains("到账") || 
            content.contains("红包") -> PaymentDirection.INCOME
            content.contains("转账") -> PaymentDirection.TRANSFER
            content.contains("付款") || content.contains("支付") || 
            content.contains("消费") -> PaymentDirection.EXPENSE
            else -> PaymentDirection.UNKNOWN
        }
    }
    
    /**
     * 推断支付方式
     */
    protected fun inferPaymentMethod(text: String): String? {
        val content = text.lowercase()
        
        return when {
            content.contains("余额宝") -> "余额宝"
            content.contains("花呗") -> "花呗"
            content.contains("微信零钱") || content.contains("零钱") -> "微信零钱"
            content.contains("银行卡") -> extractBankCard(text)
            content.contains("信用卡") -> "信用卡"
            else -> null
        }
    }
    
    /**
     * 提取银行卡信息
     */
    private fun extractBankCard(text: String): String? {
        val bankCardRegex = """尾号(\d{4})""".toRegex()
        val match = bankCardRegex.find(text)
        return match?.let { "银行卡尾号${it.groupValues[1]}" }
    }
    
    /**
     * 计算置信度
     */
    protected fun calculateConfidence(
        hasAmount: Boolean,
        hasMerchant: Boolean,
        hasDirection: Boolean,
        hasMethod: Boolean
    ): Double {
        var confidence = 0.0
        if (hasAmount) confidence += 0.4      // 金额最重要
        if (hasMerchant) confidence += 0.3    // 商户次之
        if (hasDirection) confidence += 0.2   // 方向重要
        if (hasMethod) confidence += 0.1      // 方式加分
        return confidence
    }
}
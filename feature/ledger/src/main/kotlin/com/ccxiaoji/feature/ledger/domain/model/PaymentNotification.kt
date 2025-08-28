package com.ccxiaoji.feature.ledger.domain.model

/**
 * 解析后的支付通知数据模型
 * 
 * 将原始通知文本转换为结构化的支付信息
 */
data class PaymentNotification(
    /**
     * 来源应用包名
     */
    val sourceApp: String,
    
    /**
     * 来源类型（alipay/wechat/unionpay）
     */
    val sourceType: PaymentSourceType,
    
    /**
     * 交易方向（expense/income/refund/transfer）
     */
    val direction: PaymentDirection,
    
    /**
     * 金额（分为单位，避免浮点精度问题）
     */
    val amountCents: Long,
    
    /**
     * 货币类型（默认CNY）
     */
    val currency: String = "CNY",
    
    /**
     * 商户名称（原始）
     */
    val rawMerchant: String?,
    
    /**
     * 归一化后的商户名称
     */
    val normalizedMerchant: String?,
    
    /**
     * 支付方式/渠道（余额宝/微信零钱/银行卡末尾等）
     */
    val paymentMethod: String?,
    
    /**
     * 通知发布时间
     */
    val postedTime: Long,
    
    /**
     * 通知唯一标识
     */
    val notificationKey: String?,
    
    /**
     * 解析器版本
     */
    val parserVersion: Int = 1,
    
    /**
     * 解析置信度（0.0-1.0）
     */
    val confidence: Double,
    
    /**
     * 原始通知文本（可选，用于调试）
     */
    val rawText: String? = null,
    
    /**
     * 原始通知标题
     */
    val originalTitle: String,
    
    /**
     * 原始通知内容
     */
    val originalText: String,
    
    /**
     * 去重指纹
     */
    val fingerprint: String,
    
    /**
     * 交易标签（如：红包、转账等）
     */
    val tags: Set<String> = emptySet()
)

/**
 * 支付来源类型
 */
enum class PaymentSourceType(val displayName: String) {
    ALIPAY("支付宝"),
    WECHAT("微信"),
    UNIONPAY("云闪付"),
    UNKNOWN("未知")
}

/**
 * 交易方向
 */
enum class PaymentDirection(val displayName: String) {
    EXPENSE("支出"),    // 向商户付款
    INCOME("收入"),     // 收款到账
    REFUND("退款"),     // 退款成功
    TRANSFER("转账"),   // 转账给他人
    UNKNOWN("未知")
}
package com.ccxiaoji.feature.ledger.domain.model

/**
 * 账户类型映射配置
 * 用于兼容不同记账软件的账户类型命名差异
 */
object AccountTypeMapping {
    
    /**
     * 将各种账户类型字符串映射到标准的 AccountType 枚举
     * 支持多语言、多记账软件的类型名称
     */
    fun mapToAccountType(typeStr: String): AccountType {
        return when (typeStr.uppercase().trim()) {
            // 标准枚举值（直接匹配）
            "CASH" -> AccountType.CASH
            "BANK" -> AccountType.BANK
            "CREDIT_CARD" -> AccountType.CREDIT_CARD
            "ALIPAY" -> AccountType.ALIPAY
            "WECHAT" -> AccountType.WECHAT
            "OTHER" -> AccountType.OTHER
            
            // 银行账户相关
            "BANK_CARD", "银行卡", "储蓄卡", "借记卡", 
            "DEBIT_CARD", "CHECKING", "SAVINGS",
            "储蓄账户", "活期账户", "定期账户" -> AccountType.BANK
            
            // 信用卡相关
            "信用卡", "CREDIT", "花呗", "白条", "信用账户",
            "HUABEI", "BAITIAO" -> AccountType.CREDIT_CARD
            
            // 支付宝相关
            "支付宝", "ALIPAY_WALLET", "支付宝钱包",
            "余额宝", "YUE_BAO", "YUEBAO" -> AccountType.ALIPAY
            
            // 微信相关
            "微信", "微信支付", "WECHAT_PAY", "微信钱包",
            "WECHAT_WALLET", "零钱", "微信零钱" -> AccountType.WECHAT
            
            // 现金相关
            "现金", "CASH_ACCOUNT", "现金账户",
            "钱包", "WALLET" -> AccountType.CASH
            
            // 默认值
            else -> AccountType.OTHER
        }
    }
    
    /**
     * 各记账软件的类型映射配置
     * 可以根据不同软件定制映射规则
     */
    val softwareMappings = mapOf(
        "qianji" to mapOf(
            "银行卡" to AccountType.BANK,
            "信用卡" to AccountType.CREDIT_CARD,
            "支付宝" to AccountType.ALIPAY,
            "微信" to AccountType.WECHAT,
            "现金" to AccountType.CASH,
            "其他" to AccountType.OTHER
        ),
        "suishouji" to mapOf(
            "储蓄卡" to AccountType.BANK,
            "借记卡" to AccountType.BANK,
            "信用卡" to AccountType.CREDIT_CARD,
            "支付账户" to AccountType.ALIPAY,
            "电子钱包" to AccountType.WECHAT,
            "现金" to AccountType.CASH
        ),
        "mint" to mapOf(
            "Checking" to AccountType.BANK,
            "Savings" to AccountType.BANK,
            "Credit Card" to AccountType.CREDIT_CARD,
            "Cash" to AccountType.CASH
        ),
        "ynab" to mapOf(
            "Budget Account" to AccountType.BANK,
            "Tracking Account" to AccountType.BANK,
            "Credit Card" to AccountType.CREDIT_CARD,
            "Cash" to AccountType.CASH
        )
    )
    
    /**
     * 根据特定软件获取映射
     */
    fun mapBySoftware(software: String, typeStr: String): AccountType {
        val mapping = softwareMappings[software.lowercase()]
        return mapping?.get(typeStr) ?: mapToAccountType(typeStr)
    }
    
    /**
     * 安全的 valueOf 替代方法
     * 用于替换所有 AccountType.valueOf() 调用
     */
    fun safeValueOf(typeStr: String): AccountType {
        return try {
            AccountType.valueOf(typeStr)
        } catch (e: IllegalArgumentException) {
            mapToAccountType(typeStr)
        }
    }
}
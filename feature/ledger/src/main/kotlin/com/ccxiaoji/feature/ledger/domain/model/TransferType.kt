package com.ccxiaoji.feature.ledger.domain.model

/**
 * 转账类型枚举
 */
enum class TransferType {
    /**
     * 转出记录 - 从账户转出资金
     */
    TRANSFER_OUT,
    
    /**
     * 转入记录 - 向账户转入资金
     */
    TRANSFER_IN;
    
    /**
     * 获取对应的显示名称
     */
    fun getDisplayName(): String = when (this) {
        TRANSFER_OUT -> "转出"
        TRANSFER_IN -> "转入"
    }
    
    /**
     * 获取对应的图标
     */
    fun getIcon(): String = when (this) {
        TRANSFER_OUT -> "↗️"
        TRANSFER_IN -> "↙️"
    }
    
    /**
     * 判断是否为正数金额
     */
    fun isPositiveAmount(): Boolean = when (this) {
        TRANSFER_OUT -> false  // 转出为负数
        TRANSFER_IN -> true    // 转入为正数
    }
}
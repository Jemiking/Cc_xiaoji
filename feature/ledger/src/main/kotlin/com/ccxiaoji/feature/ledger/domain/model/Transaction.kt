package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

data class Transaction(
    val id: String,
    val accountId: String,
    val amountCents: Int,
    val categoryId: String, // 分类ID
    val categoryDetails: CategoryDetails? = null, // 分类详情
    val note: String?,
    val ledgerId: String, // 所属记账簿ID
    val createdAt: Instant, // 记录创建时间
    val updatedAt: Instant, // 记录修改时间
    val transactionDate: Instant? = null, // 交易实际发生时间
    val location: LocationData? = null, // 交易发生地点
    // 转账相关字段
    val transferId: String? = null, // 转账批次ID
    val transferType: TransferType? = null, // 转账类型
    val relatedTransactionId: String? = null // 关联的另一笔转账记录ID
) {
    val amountYuan: Double
        get() = amountCents / 100.0
    
    /**
     * 获取交易的实际时间，如果没有设置则使用创建时间
     */
    val actualTransactionTime: Instant
        get() = transactionDate ?: createdAt
    
    /**
     * 判断是否为转账交易
     */
    val isTransfer: Boolean
        get() = transferType != null && transferId != null
    
    /**
     * 判断是否为转出交易
     */
    val isTransferOut: Boolean
        get() = transferType == TransferType.TRANSFER_OUT
    
    /**
     * 判断是否为转入交易
     */
    val isTransferIn: Boolean
        get() = transferType == TransferType.TRANSFER_IN
    
    /**
     * 获取转账显示名称
     */
    fun getTransferDisplayName(): String? = transferType?.getDisplayName()
}

data class CategoryDetails(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: String // "INCOME" or "EXPENSE"
)
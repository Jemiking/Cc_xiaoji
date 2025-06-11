package com.ccxiaoji.feature.ledger.domain.model

/**
 * 预算领域模型
 * 表示用户设置的预算信息和实际花费情况
 */
data class Budget(
    val id: String,
    val userId: String,
    val year: Int,
    val month: Int,
    val categoryId: String? = null,
    val budgetAmountCents: Int,
    val spentAmountCents: Int = 0,
    val alertThreshold: Float = 0.8f,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    /**
     * 预算使用百分比
     */
    val usagePercentage: Float
        get() = if (budgetAmountCents > 0) {
            (spentAmountCents.toFloat() / budgetAmountCents.toFloat()) * 100f
        } else {
            0f
        }
    
    /**
     * 是否超支
     */
    val isExceeded: Boolean
        get() = spentAmountCents > budgetAmountCents
    
    /**
     * 是否触发预警
     */
    val isAlert: Boolean
        get() = usagePercentage >= alertThreshold * 100
    
    /**
     * 剩余预算金额（分）
     */
    val remainingAmountCents: Int
        get() = budgetAmountCents - spentAmountCents
    
    /**
     * 预算金额（元）
     */
    val budgetAmountYuan: Double
        get() = budgetAmountCents / 100.0
    
    /**
     * 已花费金额（元）
     */
    val spentAmountYuan: Double
        get() = spentAmountCents / 100.0
    
    /**
     * 剩余金额（元）
     */
    val remainingAmountYuan: Double
        get() = remainingAmountCents / 100.0
    
    /**
     * 是否为总预算（非分类预算）
     */
    val isTotalBudget: Boolean
        get() = categoryId == null
}
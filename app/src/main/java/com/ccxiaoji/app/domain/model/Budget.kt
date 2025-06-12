package com.ccxiaoji.app.domain.model

data class Budget(
    val id: String,
    val userId: String,
    val year: Int,
    val month: Int,
    val categoryId: String? = null,
    val categoryDetails: CategoryDetails? = null,
    val budgetAmountCents: Int,
    val spentAmountCents: Int = 0,
    val alertThreshold: Float = 0.8f,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    val usagePercentage: Float
        get() = if (budgetAmountCents > 0) {
            (spentAmountCents.toFloat() / budgetAmountCents.toFloat()) * 100f
        } else {
            0f
        }
    
    val isExceeded: Boolean
        get() = spentAmountCents > budgetAmountCents
    
    val isAlert: Boolean
        get() = usagePercentage >= alertThreshold * 100
    
    val remainingAmountCents: Int
        get() = budgetAmountCents - spentAmountCents
}
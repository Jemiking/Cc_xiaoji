package com.ccxiaoji.feature.ledger.presentation.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * 货币格式化工具
 */
object CurrencyFormatter {
    private val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
    
    fun formatCurrency(amount: Double): String {
        return formatter.format(amount)
    }
}
package com.ccxiaoji.core.common.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * 货币格式化工具类
 * 提供统一的金额显示格式
 */
object CurrencyFormatter {
    
    private const val CURRENCY_SYMBOL = "¥"
    private const val CURRENCY_PATTERN = "#,##0.00"
    
    private val decimalFormat = DecimalFormat(CURRENCY_PATTERN).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    
    /**
     * 格式化金额（分转元）
     * @param cents 金额（分）
     * @return 格式化后的金额字符串，如：¥1,234.56
     */
    fun formatAmount(cents: Int): String {
        val yuan = cents / 100.0
        return "$CURRENCY_SYMBOL${decimalFormat.format(yuan)}"
    }
    
    /**
     * 格式化金额（元）
     * @param yuan 金额（元）
     * @return 格式化后的金额字符串，如：¥1,234.56
     */
    fun formatAmount(yuan: Double): String {
        return "$CURRENCY_SYMBOL${decimalFormat.format(yuan)}"
    }
    
    /**
     * 格式化金额，不带货币符号
     * @param cents 金额（分）
     * @return 格式化后的金额字符串，如：1,234.56
     */
    fun formatAmountWithoutSymbol(cents: Int): String {
        val yuan = cents / 100.0
        return decimalFormat.format(yuan)
    }
    
    /**
     * 格式化金额，不带货币符号
     * @param yuan 金额（元）
     * @return 格式化后的金额字符串，如：1,234.56
     */
    fun formatAmountWithoutSymbol(yuan: Double): String {
        return decimalFormat.format(yuan)
    }
    
    /**
     * 解析金额字符串为分
     * @param amountString 金额字符串（可能包含货币符号和千分符）
     * @return 金额（分），解析失败返回0
     */
    fun parseAmountToCents(amountString: String): Int {
        return try {
            val cleanedString = amountString
                .replace(CURRENCY_SYMBOL, "")
                .replace(",", "")
                .trim()
            
            (cleanedString.toDouble() * 100).toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }
    
    /**
     * 获取符号（正数返回+，负数返回-，0返回空）
     * @param amount 金额
     * @return 符号字符串
     */
    fun getSign(amount: Double): String {
        return when {
            amount > 0 -> "+"
            amount < 0 -> "-"
            else -> ""
        }
    }
    
    /**
     * 获取符号（正数返回+，负数返回-，0返回空）
     * @param cents 金额（分）
     * @return 符号字符串
     */
    fun getSign(cents: Int): String {
        return when {
            cents > 0 -> "+"
            cents < 0 -> "-"
            else -> ""
        }
    }
}
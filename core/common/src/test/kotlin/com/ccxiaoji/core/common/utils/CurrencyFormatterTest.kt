package com.ccxiaoji.core.common.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * CurrencyFormatter的单元测试
 */
class CurrencyFormatterTest {
    
    @Test
    fun `formatAmount_分转元_正确格式化`() {
        assertEquals("¥12.34", CurrencyFormatter.formatAmount(1234))
        assertEquals("¥1,234.56", CurrencyFormatter.formatAmount(123456))
        assertEquals("¥0.00", CurrencyFormatter.formatAmount(0))
        assertEquals("¥0.01", CurrencyFormatter.formatAmount(1))
    }
    
    @Test
    fun `formatAmount_元_正确格式化`() {
        assertEquals("¥12.34", CurrencyFormatter.formatAmount(12.34))
        assertEquals("¥1,234.56", CurrencyFormatter.formatAmount(1234.56))
        assertEquals("¥0.00", CurrencyFormatter.formatAmount(0.0))
        assertEquals("¥0.01", CurrencyFormatter.formatAmount(0.01))
    }
    
    @Test
    fun `formatAmountWithoutSymbol_分转元_正确格式化`() {
        assertEquals("12.34", CurrencyFormatter.formatAmountWithoutSymbol(1234))
        assertEquals("1,234.56", CurrencyFormatter.formatAmountWithoutSymbol(123456))
        assertEquals("0.00", CurrencyFormatter.formatAmountWithoutSymbol(0))
    }
    
    @Test
    fun `parseAmountToCents_正确解析`() {
        assertEquals(1234, CurrencyFormatter.parseAmountToCents("12.34"))
        assertEquals(1234, CurrencyFormatter.parseAmountToCents("¥12.34"))
        assertEquals(123456, CurrencyFormatter.parseAmountToCents("1,234.56"))
        assertEquals(123456, CurrencyFormatter.parseAmountToCents("¥1,234.56"))
        assertEquals(0, CurrencyFormatter.parseAmountToCents("invalid"))
    }
    
    @Test
    fun `getSign_正确返回符号`() {
        assertEquals("+", CurrencyFormatter.getSign(100))
        assertEquals("-", CurrencyFormatter.getSign(-100))
        assertEquals("", CurrencyFormatter.getSign(0))
        
        assertEquals("+", CurrencyFormatter.getSign(10.0))
        assertEquals("-", CurrencyFormatter.getSign(-10.0))
        assertEquals("", CurrencyFormatter.getSign(0.0))
    }
}
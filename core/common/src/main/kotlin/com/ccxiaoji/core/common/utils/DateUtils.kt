package com.ccxiaoji.core.common.utils

/**
 * 日期相关的通用工具函数
 */
object DateUtils {
    
    /**
     * 判断是否是闰年
     * 闰年规则：
     * 1. 能被4整除且不能被100整除
     * 2. 或者能被400整除
     */
    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
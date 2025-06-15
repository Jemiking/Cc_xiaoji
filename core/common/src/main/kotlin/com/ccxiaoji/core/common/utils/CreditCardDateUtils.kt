package com.ccxiaoji.core.common.utils

import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.min

/**
 * 信用卡日期计算工具类
 * 提供信用卡账单日、还款日等日期计算功能
 */
object CreditCardDateUtils {
    
    /**
     * 计算下一个还款日
     * @param paymentDueDay 还款日（1-28）
     * @param billingDay 账单日（1-28）
     * @param currentDate 当前日期
     * @return 下一个还款日
     */
    fun calculateNextPaymentDate(
        paymentDueDay: Int,
        billingDay: Int,
        currentDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
    ): LocalDate {
        val currentDay = currentDate.dayOfMonth
        val currentMonth = currentDate.month
        val currentYear = currentDate.year
        
        // 计算本月的还款日（处理月末情况）
        val daysInCurrentMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth()
        val actualPaymentDay = min(paymentDueDay, daysInCurrentMonth)
        
        // 判断还款日是否在账单日之后（同月）还是下个月
        var paymentMonth: Month
        var paymentYear: Int
        
        if (billingDay < paymentDueDay) {
            // 账单日和还款日在同一个月
            if (currentDay <= actualPaymentDay) {
                // 本月还款日还未到
                paymentMonth = currentMonth
                paymentYear = currentYear
            } else {
                // 本月还款日已过，计算下个月
                if (currentMonth == Month.DECEMBER) {
                    paymentMonth = Month.JANUARY
                    paymentYear = currentYear + 1
                } else {
                    paymentMonth = currentMonth.plus(1)
                    paymentYear = currentYear
                }
            }
        } else {
            // 账单日和还款日跨月（如账单日25号，还款日15号）
            if (currentDay <= billingDay) {
                // 还在本账单周期内，还款日是本月
                if (currentDay <= actualPaymentDay) {
                    paymentMonth = currentMonth
                    paymentYear = currentYear
                } else {
                    // 本月还款日已过，下个还款日是下下个月
                    val nextNextMonth = currentMonth.plus(2)
                    paymentMonth = if (currentMonth == Month.NOVEMBER) {
                        Month.JANUARY
                    } else if (currentMonth == Month.DECEMBER) {
                        Month.FEBRUARY
                    } else {
                        nextNextMonth
                    }
                    paymentYear = if (currentMonth.value >= 11) currentYear + 1 else currentYear
                }
            } else {
                // 已过本月账单日，还款日在下个月
                val nextMonth = if (currentMonth == Month.DECEMBER) {
                    Month.JANUARY to currentYear + 1
                } else {
                    currentMonth.plus(1) to currentYear
                }
                paymentMonth = nextMonth.first
                paymentYear = nextMonth.second
            }
        }
        
        // 计算实际的还款日（处理月末情况）
        val daysInPaymentMonth = YearMonth.of(paymentYear, paymentMonth).lengthOfMonth()
        val actualPaymentDayInMonth = min(paymentDueDay, daysInPaymentMonth)
        
        return LocalDate.of(paymentYear, paymentMonth, actualPaymentDayInMonth)
    }
    
    /**
     * 计算距离还款日的天数
     * @param paymentDueDay 还款日（1-28）
     * @param billingDay 账单日（1-28）
     * @param currentDate 当前日期
     * @return 剩余天数
     */
    fun calculateDaysUntilPayment(
        paymentDueDay: Int,
        billingDay: Int,
        currentDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
    ): Int {
        val nextPaymentDate = calculateNextPaymentDate(paymentDueDay, billingDay, currentDate)
        return currentDate.until(nextPaymentDate, ChronoUnit.DAYS).toInt()
    }
    
    /**
     * 计算当前账单周期
     * @param billingDay 账单日（1-28）
     * @param currentDate 当前日期
     * @return Pair<开始日期, 结束日期>
     */
    fun calculateCurrentBillingCycle(
        billingDay: Int,
        currentDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
    ): Pair<LocalDate, LocalDate> {
        val currentDay = currentDate.dayOfMonth
        val currentMonth = currentDate.month
        val currentYear = currentDate.year
        
        val cycleStartDate: LocalDate
        val cycleEndDate: LocalDate
        
        if (currentDay <= billingDay) {
            // 还未到本月账单日，当前周期是上个月账单日到本月账单日
            val previousMonth = if (currentMonth == Month.JANUARY) {
                Month.DECEMBER to currentYear - 1
            } else {
                currentMonth.minus(1) to currentYear
            }
            
            val daysInPreviousMonth = YearMonth.of(previousMonth.second, previousMonth.first).lengthOfMonth()
            val actualBillingDayPrevMonth = min(billingDay, daysInPreviousMonth)
            
            cycleStartDate = LocalDate.of(previousMonth.second, previousMonth.first, actualBillingDayPrevMonth)
            
            val daysInCurrentMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth()
            val actualBillingDayCurrentMonth = min(billingDay, daysInCurrentMonth)
            cycleEndDate = LocalDate.of(currentYear, currentMonth, actualBillingDayCurrentMonth)
        } else {
            // 已过本月账单日，当前周期是本月账单日到下月账单日
            val daysInCurrentMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth()
            val actualBillingDayCurrentMonth = min(billingDay, daysInCurrentMonth)
            cycleStartDate = LocalDate.of(currentYear, currentMonth, actualBillingDayCurrentMonth)
            
            val nextMonth = if (currentMonth == Month.DECEMBER) {
                Month.JANUARY to currentYear + 1
            } else {
                currentMonth.plus(1) to currentYear
            }
            
            val daysInNextMonth = YearMonth.of(nextMonth.second, nextMonth.first).lengthOfMonth()
            val actualBillingDayNextMonth = min(billingDay, daysInNextMonth)
            cycleEndDate = LocalDate.of(nextMonth.second, nextMonth.first, actualBillingDayNextMonth)
        }
        
        return cycleStartDate to cycleEndDate
    }
    
    /**
     * 判断某个日期是否在当前账单周期内
     * @param date 要判断的日期
     * @param billingDay 账单日
     * @return 是否在当前账单周期内
     */
    fun isInCurrentBillingCycle(
        date: LocalDate,
        billingDay: Int
    ): Boolean {
        val (cycleStart, cycleEnd) = calculateCurrentBillingCycle(billingDay, date)
        return !date.isBefore(cycleStart) && !date.isAfter(cycleEnd)
    }
}
package com.ccxiaoji.core.common.utils

import com.ccxiaoji.common.utils.CreditCardDateUtils
import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.*
import org.junit.Test

class DateUtilsTest {

    @Test
    fun `计算下一个还款日_账单日在还款日之前`() {
        // Given
        val paymentDueDay = 20
        val billingDay = 10
        val currentDate = LocalDate(2024, 1, 15) // 1月15日，已过账单日但未到还款日

        // When
        val nextPaymentDate = CreditCardDateUtils.calculateNextPaymentDate(
            paymentDueDay = paymentDueDay,
            billingDay = billingDay,
            currentDate = currentDate
        )

        // Then
        assertThat(nextPaymentDate).isEqualTo(LocalDate(2024, 1, 20))
    }

    @Test
    fun `计算下一个还款日_账单日在还款日之后_跨月情况`() {
        // Given
        val paymentDueDay = 5
        val billingDay = 25
        val currentDate = LocalDate(2024, 1, 26) // 1月26日，已过账单日

        // When
        val nextPaymentDate = CreditCardDateUtils.calculateNextPaymentDate(
            paymentDueDay = paymentDueDay,
            billingDay = billingDay,
            currentDate = currentDate
        )

        // Then
        assertThat(nextPaymentDate).isEqualTo(LocalDate(2024, 2, 5))
    }

    @Test
    fun `计算距离还款日的天数`() {
        // Given
        val paymentDueDay = 20
        val billingDay = 10
        val currentDate = LocalDate(2024, 1, 15)

        // When
        val daysUntilPayment = CreditCardDateUtils.calculateDaysUntilPayment(
            paymentDueDay = paymentDueDay,
            billingDay = billingDay,
            currentDate = currentDate
        )

        // Then
        assertThat(daysUntilPayment).isEqualTo(5)
    }

    @Test
    fun `计算当前账单周期_未到账单日`() {
        // Given
        val billingDay = 20
        val currentDate = LocalDate(2024, 1, 15) // 1月15日，未到账单日

        // When
        val (cycleStart, cycleEnd) = CreditCardDateUtils.calculateCurrentBillingCycle(
            billingDay = billingDay,
            currentDate = currentDate
        )

        // Then
        assertThat(cycleStart).isEqualTo(LocalDate(2023, 12, 20))
        assertThat(cycleEnd).isEqualTo(LocalDate(2024, 1, 20))
    }

    @Test
    fun `判断日期是否在当前账单周期内`() {
        // Given
        val billingDay = 20
        val dateToCheck = LocalDate(2024, 1, 15)
        val dateOutsideCycle = LocalDate(2024, 2, 1)

        // When
        val isInCycle = CreditCardDateUtils.isInCurrentBillingCycle(dateToCheck, billingDay)
        val isOutsideCycle = CreditCardDateUtils.isInCurrentBillingCycle(dateOutsideCycle, billingDay)

        // Then
        assertThat(isInCycle).isTrue()
        assertThat(isOutsideCycle).isFalse()
    }

    @Test
    fun `处理月末特殊情况_2月份`() {
        // Given
        val paymentDueDay = 31
        val billingDay = 25
        val currentDate = LocalDate(2024, 1, 26) // 1月26日

        // When
        val nextPaymentDate = CreditCardDateUtils.calculateNextPaymentDate(
            paymentDueDay = paymentDueDay,
            billingDay = billingDay,
            currentDate = currentDate
        )

        // Then
        // 2月只有29天（2024是闰年），所以还款日应该是2月29日
        assertThat(nextPaymentDate).isEqualTo(LocalDate(2024, 2, 29))
    }
}
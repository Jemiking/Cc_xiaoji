package com.ccxiaoji.core.database

import com.ccxiaoji.common.model.CategoryType
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.common.model.SyncStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    @Test
    fun `LocalDate转换测试`() {
        // Given
        val date = LocalDate.of(2024, 1, 15)

        // When
        val dateString = converters.fromLocalDate(date)
        val convertedDate = converters.toLocalDate(dateString)

        // Then
        assertThat(dateString).isEqualTo("2024-01-15")
        assertThat(convertedDate).isEqualTo(date)
    }

    @Test
    fun `LocalDate空值处理`() {
        // When
        val nullString = converters.fromLocalDate(null)
        val nullDate = converters.toLocalDate(null)

        // Then
        assertThat(nullString).isNull()
        assertThat(nullDate).isNull()
    }

    @Test
    fun `LocalDateTime转换测试`() {
        // Given
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45)

        // When
        val dateTimeString = converters.fromLocalDateTime(dateTime)
        val convertedDateTime = converters.toLocalDateTime(dateTimeString)

        // Then
        assertThat(dateTimeString).isEqualTo("2024-01-15T14:30:45")
        assertThat(convertedDateTime).isEqualTo(dateTime)
    }

    @Test
    fun `Instant转换测试`() {
        // Given
        val instant = Instant.fromEpochMilliseconds(1705320000000L) // 2024-01-15 12:00:00 UTC

        // When
        val timestamp = converters.fromInstant(instant)
        val convertedInstant = converters.toInstant(timestamp)

        // Then
        assertThat(timestamp).isEqualTo(1705320000000L)
        assertThat(convertedInstant).isEqualTo(instant)
    }

    @Test
    fun `CategoryType枚举转换测试`() {
        // Given
        val incomeType = CategoryType.INCOME
        val expenseType = CategoryType.EXPENSE

        // When
        val incomeString = converters.fromCategoryType(incomeType)
        val expenseString = converters.fromCategoryType(expenseType)
        val convertedIncome = converters.toCategoryType(incomeString)
        val convertedExpense = converters.toCategoryType(expenseString)

        // Then
        assertThat(incomeString).isEqualTo("INCOME")
        assertThat(expenseString).isEqualTo("EXPENSE")
        assertThat(convertedIncome).isEqualTo(incomeType)
        assertThat(convertedExpense).isEqualTo(expenseType)
    }

    @Test
    fun `SyncStatus枚举转换测试`() {
        // Given
        val pendingStatus = SyncStatus.PENDING
        val syncedStatus = SyncStatus.SYNCED

        // When
        val pendingString = converters.fromSyncStatus(pendingStatus)
        val syncedString = converters.fromSyncStatus(syncedStatus)
        val convertedPending = converters.toSyncStatus(pendingString)
        val convertedSynced = converters.toSyncStatus(syncedString)

        // Then
        assertThat(pendingString).isEqualTo("PENDING")
        assertThat(syncedString).isEqualTo("SYNCED")
        assertThat(convertedPending).isEqualTo(pendingStatus)
        assertThat(convertedSynced).isEqualTo(syncedStatus)
    }

    @Test
    fun `RecurringFrequency枚举转换测试`() {
        // Given
        val dailyFreq = RecurringFrequency.DAILY
        val monthlyFreq = RecurringFrequency.MONTHLY

        // When
        val dailyString = converters.fromRecurringFrequency(dailyFreq)
        val monthlyString = converters.fromRecurringFrequency(monthlyFreq)
        val convertedDaily = converters.toRecurringFrequency(dailyString)
        val convertedMonthly = converters.toRecurringFrequency(monthlyString)

        // Then
        assertThat(dailyString).isEqualTo("DAILY")
        assertThat(monthlyString).isEqualTo("MONTHLY")
        assertThat(convertedDaily).isEqualTo(dailyFreq)
        assertThat(convertedMonthly).isEqualTo(monthlyFreq)
    }

    @Test
    fun `List String转换测试`() {
        // Given
        val list = listOf("tag1", "tag2", "tag3")
        val emptyList = emptyList<String>()

        // When
        val jsonString = converters.fromStringList(list)
        val emptyJsonString = converters.fromStringList(emptyList)
        val convertedList = converters.toStringList(jsonString)
        val convertedEmptyList = converters.toStringList(emptyJsonString)

        // Then
        assertThat(convertedList).containsExactly("tag1", "tag2", "tag3")
        assertThat(convertedEmptyList).isEmpty()
    }
}
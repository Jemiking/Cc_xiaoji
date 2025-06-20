package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import org.junit.Before
import org.junit.Test

class GetMonthScheduleUseCaseTest {

    @MockK
    private lateinit var scheduleRepository: ScheduleRepository

    private lateinit var getMonthScheduleUseCase: GetMonthScheduleUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // 假设GetMonthScheduleUseCase存在
        getMonthScheduleUseCase = GetMonthScheduleUseCase(scheduleRepository)
    }

    @Test
    fun `获取指定月份的所有排班`() = runTest {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        val shift = Shift(
            id = 1L,
            name = "早班",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
        
        val schedules = listOf(
            Schedule(id = 1L, date = LocalDate.of(2024, 1, 1), shift = shift),
            Schedule(id = 2L, date = LocalDate.of(2024, 1, 15), shift = shift),
            Schedule(id = 3L, date = LocalDate.of(2024, 1, 31), shift = shift)
        )

        coEvery { scheduleRepository.getSchedulesByMonth(yearMonth) } returns flowOf(schedules)

        // When
        val result = getMonthScheduleUseCase.invoke(yearMonth).first()

        // Then
        assertThat(result).hasSize(3)
        assertThat(result.all { it.date.year == 2024 && it.date.monthValue == 1 }).isTrue()
        coVerify(exactly = 1) { scheduleRepository.getSchedulesByMonth(yearMonth) }
    }

    @Test
    fun `获取空月份排班返回空列表`() = runTest {
        // Given
        val yearMonth = YearMonth.of(2024, 2)
        coEvery { scheduleRepository.getSchedulesByMonth(yearMonth) } returns flowOf(emptyList())

        // When
        val result = getMonthScheduleUseCase.invoke(yearMonth).first()

        // Then
        assertThat(result).isEmpty()
        coVerify(exactly = 1) { scheduleRepository.getSchedulesByMonth(yearMonth) }
    }

    @Test
    fun `获取包含多种班次的月份排班`() = runTest {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        val earlyShift = createTestShift(1L, "早班", LocalTime.of(8, 0), LocalTime.of(16, 0))
        val lateShift = createTestShift(2L, "晚班", LocalTime.of(16, 0), LocalTime.of(0, 0))
        val nightShift = createTestShift(3L, "夜班", LocalTime.of(0, 0), LocalTime.of(8, 0))
        
        val schedules = listOf(
            Schedule(id = 1L, date = LocalDate.of(2024, 1, 1), shift = earlyShift),
            Schedule(id = 2L, date = LocalDate.of(2024, 1, 2), shift = lateShift),
            Schedule(id = 3L, date = LocalDate.of(2024, 1, 3), shift = nightShift),
            Schedule(id = 4L, date = LocalDate.of(2024, 1, 4), shift = earlyShift)
        )

        coEvery { scheduleRepository.getSchedulesByMonth(yearMonth) } returns flowOf(schedules)

        // When
        val result = getMonthScheduleUseCase.invoke(yearMonth).first()

        // Then
        assertThat(result).hasSize(4)
        assertThat(result.filter { it.shift?.name == "早班" }).hasSize(2)
        assertThat(result.filter { it.shift?.name == "晚班" }).hasSize(1)
        assertThat(result.filter { it.shift?.name == "夜班" }).hasSize(1)
    }

    private fun createTestShift(id: Long, name: String, startTime: LocalTime, endTime: LocalTime): Shift {
        return Shift(
            id = id,
            name = name,
            color = 0xFFFF5722.toInt(),
            startTime = startTime,
            endTime = endTime,
            isActive = true
        )
    }
}

// 假设的UseCase类
class GetMonthScheduleUseCase(
    private val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(yearMonth: YearMonth) = 
        scheduleRepository.getSchedulesByMonth(yearMonth)
}
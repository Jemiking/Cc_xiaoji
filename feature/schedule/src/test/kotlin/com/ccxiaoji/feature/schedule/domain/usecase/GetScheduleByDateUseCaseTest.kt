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
import org.junit.Before
import org.junit.Test

class GetScheduleByDateUseCaseTest {

    @MockK
    private lateinit var scheduleRepository: ScheduleRepository

    private lateinit var getScheduleByDateUseCase: GetScheduleByDateUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getScheduleByDateUseCase = GetScheduleByDateUseCase(scheduleRepository)
    }

    @Test
    fun `获取指定日期的排班信息`() = runTest {
        // Given
        val date = LocalDate.of(2024, 1, 1)
        val shift = Shift(
            id = 1L,
            name = "早班",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
        val schedule = Schedule(
            id = 1L,
            date = date,
            shift = shift
        )

        coEvery { scheduleRepository.getScheduleByDate(date) } returns flowOf(schedule)

        // When
        val result = getScheduleByDateUseCase.invoke(date).first()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.date).isEqualTo(date)
        assertThat(result?.shift?.name).isEqualTo("早班")
        coVerify(exactly = 1) { scheduleRepository.getScheduleByDate(date) }
    }

    @Test
    fun `获取指定日期无排班时返回null`() = runTest {
        // Given
        val date = LocalDate.of(2024, 1, 1)
        coEvery { scheduleRepository.getScheduleByDate(date) } returns flowOf(null)

        // When
        val result = getScheduleByDateUseCase.invoke(date).first()

        // Then
        assertThat(result).isNull()
        coVerify(exactly = 1) { scheduleRepository.getScheduleByDate(date) }
    }

    @Test
    fun `获取夜班排班信息`() = runTest {
        // Given
        val date = LocalDate.of(2024, 1, 1)
        val nightShift = Shift(
            id = 2L,
            name = "夜班",
            color = 0xFF3F51B5.toInt(),
            startTime = LocalTime.of(20, 0),
            endTime = LocalTime.of(8, 0), // 跨天
            isActive = true
        )
        val schedule = Schedule(
            id = 2L,
            date = date,
            shift = nightShift
        )

        coEvery { scheduleRepository.getScheduleByDate(date) } returns flowOf(schedule)

        // When
        val result = getScheduleByDateUseCase.invoke(date).first()

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.shift?.name).isEqualTo("夜班")
        assertThat(result?.shift?.startTime).isEqualTo(LocalTime.of(20, 0))
        assertThat(result?.shift?.endTime).isEqualTo(LocalTime.of(8, 0))
        coVerify(exactly = 1) { scheduleRepository.getScheduleByDate(date) }
    }
}
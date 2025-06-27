package com.ccxiaoji.feature.schedule.domain.usecase

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
import java.time.LocalTime
import org.junit.Before
import org.junit.Test

class GetShiftsUseCaseTest {

    @MockK
    private lateinit var scheduleRepository: ScheduleRepository

    private lateinit var getShiftsUseCase: GetShiftsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getShiftsUseCase = GetShiftsUseCase(scheduleRepository)
    }

    @Test
    fun `获取所有活跃班次`() = runTest {
        // Given
        val shifts = listOf(
            Shift(
                id = 1L,
                name = "早班",
                color = 0xFFFF5722.toInt(),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(16, 0),
                isActive = true
            ),
            Shift(
                id = 2L,
                name = "晚班",
                color = 0xFF3F51B5.toInt(),
                startTime = LocalTime.of(16, 0),
                endTime = LocalTime.of(0, 0),
                isActive = true
            )
        )

        coEvery { scheduleRepository.getAllShifts() } returns flowOf(shifts)

        // When
        val result = getShiftsUseCase.invoke().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("早班")
        assertThat(result[1].name).isEqualTo("晚班")
        coVerify(exactly = 1) { scheduleRepository.getAllShifts() }
    }

    @Test
    fun `根据ID获取单个班次`() = runTest {
        // Given
        val shiftId = 1L
        val shift = Shift(
            id = shiftId,
            name = "早班",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )

        coEvery { scheduleRepository.getShiftById(shiftId) } returns shift

        // When
        val result = getShiftsUseCase.getShiftById(shiftId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(shiftId)
        assertThat(result?.name).isEqualTo("早班")
        coVerify(exactly = 1) { scheduleRepository.getShiftById(shiftId) }
    }

    @Test
    fun `根据ID获取不存在的班次返回null`() = runTest {
        // Given
        val shiftId = 999L
        coEvery { scheduleRepository.getShiftById(shiftId) } returns null

        // When
        val result = getShiftsUseCase.getShiftById(shiftId)

        // Then
        assertThat(result).isNull()
        coVerify(exactly = 1) { scheduleRepository.getShiftById(shiftId) }
    }

    @Test
    fun `获取空的班次列表`() = runTest {
        // Given
        coEvery { scheduleRepository.getAllShifts() } returns flowOf(emptyList())

        // When
        val result = getShiftsUseCase.invoke().first()

        // Then
        assertThat(result).isEmpty()
        coVerify(exactly = 1) { scheduleRepository.getAllShifts() }
    }
}
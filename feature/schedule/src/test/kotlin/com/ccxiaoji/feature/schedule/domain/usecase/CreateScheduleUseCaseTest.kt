package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.SchedulePattern
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Before
import org.junit.Test

class CreateScheduleUseCaseTest {

    @MockK
    private lateinit var scheduleRepository: ScheduleRepository

    private lateinit var createScheduleUseCase: CreateScheduleUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        createScheduleUseCase = CreateScheduleUseCase(scheduleRepository)
    }

    @Test
    fun `创建或更新单个排班`() = runTest {
        // Given
        val schedule = Schedule(
            id = 1L,
            date = LocalDate.of(2024, 1, 1),
            shift = Shift(
                id = 1L,
                name = "早班",
                color = 0xFFFF5722.toInt(),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(16, 0),
                isActive = true
            )
        )
        coEvery { scheduleRepository.saveSchedule(any()) } returns 1L

        // When
        val result = createScheduleUseCase.createOrUpdateSchedule(schedule)

        // Then
        assertThat(result).isEqualTo(1L)
        coVerify(exactly = 1) { scheduleRepository.saveSchedule(schedule) }
    }

    @Test
    fun `删除指定日期的排班`() = runTest {
        // Given
        val date = LocalDate.of(2024, 1, 1)
        coEvery { scheduleRepository.deleteScheduleByDate(date) } returns Unit

        // When
        createScheduleUseCase.deleteSchedule(date)

        // Then
        coVerify(exactly = 1) { scheduleRepository.deleteScheduleByDate(date) }
    }

    @Test
    fun `处理单个排班模式`() = runTest {
        // Given
        val shift = Shift(
            id = 1L,
            name = "早班",
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
        val pattern = SchedulePattern.Single(
            date = LocalDate.of(2024, 1, 1),
            shiftId = 1L
        )
        
        coEvery { scheduleRepository.getShiftById(1L) } returns shift
        coEvery { scheduleRepository.saveSchedule(any()) } returns 1L

        // When
        createScheduleUseCase.createSchedulesByPattern(pattern)

        // Then
        coVerify(exactly = 1) { scheduleRepository.getShiftById(1L) }
        coVerify(exactly = 1) { scheduleRepository.saveSchedule(any()) }
    }

    @Test
    fun `处理循环排班模式`() = runTest {
        // Given
        val shift1 = createTestShift(1L, "早班")
        val shift2 = createTestShift(2L, "晚班")
        val pattern = SchedulePattern.Cycle(
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 7),
            cycleDays = 3,
            cyclePattern = mapOf(0 to 1L, 1 to 2L) // 早班-晚班（跳过第3天表示休息）
        )
        
        coEvery { scheduleRepository.getShiftById(1L) } returns shift1
        coEvery { scheduleRepository.getShiftById(2L) } returns shift2
        
        val schedulesSlot = slot<List<Schedule>>()
        coEvery { scheduleRepository.saveSchedules(capture(schedulesSlot)) } returns Unit

        // When
        createScheduleUseCase.createSchedulesByPattern(pattern)

        // Then
        val savedSchedules = schedulesSlot.captured
        assertThat(savedSchedules).hasSize(5) // 7天内，3个早班+2个晚班
        assertThat(savedSchedules.count { it.shift.id == 1L }).isEqualTo(3)
        assertThat(savedSchedules.count { it.shift.id == 2L }).isEqualTo(2)
    }

    @Test
    fun `处理轮转排班模式`() = runTest {
        // Given
        val shift1 = createTestShift(1L, "早班")
        val shift2 = createTestShift(2L, "晚班")
        val pattern = SchedulePattern.Rotation(
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 10),
            shiftIds = listOf(1L, 2L),
            restDays = 1
        )
        
        coEvery { scheduleRepository.getShiftById(1L) } returns shift1
        coEvery { scheduleRepository.getShiftById(2L) } returns shift2
        
        val schedulesSlot = slot<List<Schedule>>()
        coEvery { scheduleRepository.saveSchedules(capture(schedulesSlot)) } returns Unit

        // When
        createScheduleUseCase.createSchedulesByPattern(pattern)

        // Then
        val savedSchedules = schedulesSlot.captured
        // 10天内：早班-晚班-休息-早班-晚班-休息-早班-晚班-休息-早班
        assertThat(savedSchedules).hasSize(7) // 工作日数
    }

    @Test
    fun `清除日期范围内的排班`() = runTest {
        // Given
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        coEvery { scheduleRepository.clearSchedules(startDate, endDate) } returns Unit

        // When
        createScheduleUseCase.clearSchedules(startDate, endDate)

        // Then
        coVerify(exactly = 1) { scheduleRepository.clearSchedules(startDate, endDate) }
    }

    @Test
    fun `处理无效循环天数抛出异常`() = runTest {
        // Given
        val pattern = SchedulePattern.Cycle(
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 7),
            cycleDays = 366, // 超过365天
            cyclePattern = mapOf(0 to 1L)
        )

        // When & Then
        try {
            createScheduleUseCase.createSchedulesByPattern(pattern)
            assertThat(false).isTrue() // 不应该到达这里
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("循环天数必须在2-365之间")
        }
    }

    private fun createTestShift(id: Long, name: String): Shift {
        return Shift(
            id = id,
            name = name,
            color = 0xFFFF5722.toInt(),
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 0),
            isActive = true
        )
    }
}
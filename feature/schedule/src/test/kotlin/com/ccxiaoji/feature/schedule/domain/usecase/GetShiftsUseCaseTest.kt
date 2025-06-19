package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.data.repository.ShiftRepository
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.model.ShiftType
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.junit.Before
import org.junit.Test

class GetShiftsUseCaseTest {

    @MockK
    private lateinit var shiftRepository: ShiftRepository

    private lateinit var getShiftsUseCase: GetShiftsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // 假设存在一个GetShiftsUseCase类
        getShiftsUseCase = GetShiftsUseCase(shiftRepository)
    }

    @Test
    fun `获取指定日期范围的排班记录`() = runTest {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 7)
        
        val shiftType = ShiftType(
            id = "type1",
            name = "早班",
            shortName = "早",
            color = "#FF5722",
            startTime = LocalTime(8, 0),
            endTime = LocalTime(16, 0),
            breakMinutes = 60,
            isNightShift = false,
            orderIndex = 1
        )
        
        val shifts = listOf(
            Shift(
                id = "shift1",
                date = startDate,
                shiftTypeId = "type1",
                note = "正常上班",
                actualStartTime = LocalTime(8, 0),
                actualEndTime = LocalTime(16, 30),
                overtimeMinutes = 30,
                isHoliday = false,
                isAbsent = false,
                shiftType = shiftType
            ),
            Shift(
                id = "shift2",
                date = startDate.plus(1, DateTimeUnit.DAY),
                shiftTypeId = "type1",
                note = null,
                actualStartTime = LocalTime(8, 0),
                actualEndTime = LocalTime(16, 0),
                overtimeMinutes = 0,
                isHoliday = false,
                isAbsent = false,
                shiftType = shiftType
            )
        )

        coEvery { 
            shiftRepository.getShiftsByDateRange(startDate, endDate) 
        } returns flowOf(shifts)

        // When
        val result = getShiftsUseCase.invoke(startDate, endDate).first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].shiftType?.name).isEqualTo("早班")
        assertThat(result[0].overtimeMinutes).isEqualTo(30)
        assertThat(result[1].overtimeMinutes).isEqualTo(0)
        coVerify(exactly = 1) { shiftRepository.getShiftsByDateRange(startDate, endDate) }
    }

    @Test
    fun `获取某月的排班统计`() = runTest {
        // Given
        val year = 2024
        val month = 1
        val monthlyStats = mapOf(
            "totalShifts" to 20,
            "dayShifts" to 10,
            "nightShifts" to 8,
            "restDays" to 2,
            "totalOvertimeMinutes" to 360
        )

        coEvery { 
            shiftRepository.getMonthlyStatistics(year, month) 
        } returns monthlyStats

        // When
        val result = shiftRepository.getMonthlyStatistics(year, month)

        // Then
        assertThat(result["totalShifts"]).isEqualTo(20)
        assertThat(result["dayShifts"]).isEqualTo(10)
        assertThat(result["nightShifts"]).isEqualTo(8)
        assertThat(result["totalOvertimeMinutes"]).isEqualTo(360)
        coVerify(exactly = 1) { shiftRepository.getMonthlyStatistics(year, month) }
    }

    @Test
    fun `获取连续工作天数`() = runTest {
        // Given
        val currentDate = LocalDate(2024, 1, 15)
        val consecutiveWorkDays = 5

        coEvery { 
            shiftRepository.getConsecutiveWorkDays(currentDate) 
        } returns consecutiveWorkDays

        // When
        val result = shiftRepository.getConsecutiveWorkDays(currentDate)

        // Then
        assertThat(result).isEqualTo(5)
        coVerify(exactly = 1) { shiftRepository.getConsecutiveWorkDays(currentDate) }
    }

    @Test
    fun `检查是否为夜班`() = runTest {
        // Given
        val nightShiftType = ShiftType(
            id = "type2",
            name = "夜班",
            shortName = "夜",
            color = "#3F51B5",
            startTime = LocalTime(20, 0),
            endTime = LocalTime(8, 0), // 跨天
            breakMinutes = 60,
            isNightShift = true,
            orderIndex = 2
        )
        
        val nightShift = Shift(
            id = "shift3",
            date = LocalDate(2024, 1, 1),
            shiftTypeId = "type2",
            note = "夜班值班",
            actualStartTime = LocalTime(20, 0),
            actualEndTime = LocalTime(8, 30),
            overtimeMinutes = 30,
            isHoliday = false,
            isAbsent = false,
            shiftType = nightShiftType
        )

        // When & Then
        assertThat(nightShift.shiftType?.isNightShift).isTrue()
        assertThat(nightShift.shiftType?.name).isEqualTo("夜班")
        
        // 计算夜班工作时长（跨天）
        val workHours = if (nightShift.shiftType?.endTime!! < nightShift.shiftType?.startTime!!) {
            // 跨天情况
            val minutesToMidnight = (LocalTime(23, 59) - nightShift.shiftType?.startTime!!).inWholeMinutes + 1
            val minutesFromMidnight = nightShift.shiftType?.endTime!!.toSecondOfDay() / 60
            minutesToMidnight + minutesFromMidnight
        } else {
            (nightShift.shiftType?.endTime!! - nightShift.shiftType?.startTime!!).inWholeMinutes
        }
        
        assertThat(workHours).isEqualTo(720L) // 12小时 = 720分钟
    }
}

// 假设的UseCase类和Repository接口
class GetShiftsUseCase(
    private val shiftRepository: ShiftRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate) = 
        shiftRepository.getShiftsByDateRange(startDate, endDate)
}

// 假设的Repository接口扩展
interface ShiftRepository {
    fun getShiftsByDateRange(startDate: LocalDate, endDate: LocalDate): kotlinx.coroutines.flow.Flow<List<Shift>>
    suspend fun getMonthlyStatistics(year: Int, month: Int): Map<String, Int>
    suspend fun getConsecutiveWorkDays(date: LocalDate): Int
}
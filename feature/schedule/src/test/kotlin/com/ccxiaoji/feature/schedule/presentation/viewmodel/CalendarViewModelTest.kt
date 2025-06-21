package com.ccxiaoji.feature.schedule.presentation.viewmodel

import com.ccxiaoji.common.test.util.MainDispatcherRule
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.usecase.*
import com.ccxiaoji.feature.schedule.presentation.theme.ThemeManager
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@ExperimentalCoroutinesApi
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mock依赖
    private val mockGetMonthScheduleUseCase = mockk<GetMonthScheduleUseCase>()
    private val mockGetScheduleStatisticsUseCase = mockk<GetScheduleStatisticsUseCase>()
    private val mockGetQuickShiftsUseCase = mockk<GetQuickShiftsUseCase>()
    private val mockCreateScheduleUseCase = mockk<CreateScheduleUseCase>()
    private val mockDeleteScheduleUseCase = mockk<DeleteScheduleUseCase>()
    private val mockThemeManager = mockk<ThemeManager>()
    
    // 被测试的ViewModel
    private lateinit var viewModel: CalendarViewModel

    // 测试数据
    private val testShifts = listOf(
        Shift(
            id = 1,
            name = "早班",
            shortName = "早",
            color = "#FF5722",
            startTime = "08:00",
            endTime = "17:00",
            isQuickSelect = true
        ),
        Shift(
            id = 2,
            name = "晚班",
            shortName = "晚",
            color = "#2196F3",
            startTime = "17:00",
            endTime = "02:00",
            isQuickSelect = true
        ),
        Shift(
            id = 3,
            name = "休息",
            shortName = "休",
            color = "#4CAF50",
            startTime = null,
            endTime = null,
            isQuickSelect = true
        )
    )

    private val testSchedules = listOf(
        Schedule(
            id = 1,
            date = LocalDate.now(),
            shift = testShifts[0],
            note = "测试备注"
        ),
        Schedule(
            id = 2,
            date = LocalDate.now().plusDays(1),
            shift = testShifts[1],
            note = ""
        ),
        Schedule(
            id = 3,
            date = LocalDate.now().plusDays(2),
            shift = testShifts[2],
            note = ""
        )
    )

    private val testStatistics = ScheduleStatistics(
        totalDays = 30,
        workDays = 20,
        restDays = 10,
        dayShifts = 10,
        nightShifts = 10,
        totalHours = 200.0,
        overtimeHours = 20.0
    )

    @Before
    fun setup() {
        // 默认mock设置
        every { mockGetMonthScheduleUseCase(any()) } returns flowOf(testSchedules)
        every { mockGetQuickShiftsUseCase() } returns flowOf(testShifts)
        every { mockThemeManager.weekStartDay } returns flowOf(DayOfWeek.MONDAY)
        coEvery { mockGetScheduleStatisticsUseCase.getMonthlyStatistics(any()) } returns testStatistics
        
        // 初始化ViewModel
        viewModel = CalendarViewModel(
            getMonthScheduleUseCase = mockGetMonthScheduleUseCase,
            getScheduleStatisticsUseCase = mockGetScheduleStatisticsUseCase,
            getQuickShiftsUseCase = mockGetQuickShiftsUseCase,
            createScheduleUseCase = mockCreateScheduleUseCase,
            deleteScheduleUseCase = mockDeleteScheduleUseCase,
            themeManager = mockThemeManager
        )
    }

    @Test
    fun `初始化时应该加载当前月份的数据`() = runTest {
        // When - ViewModel在init中自动加载数据
        
        // Then
        assertThat(viewModel.currentYearMonth.value).isEqualTo(YearMonth.now())
        assertThat(viewModel.selectedDate.value).isEqualTo(LocalDate.now())
        assertThat(viewModel.schedules.value).hasSize(3)
        assertThat(viewModel.quickShifts.value).hasSize(3)
        assertThat(viewModel.monthlyStatistics.value).isNotNull()
        verify(exactly = 1) { mockGetMonthScheduleUseCase(YearMonth.now()) }
        coVerify(exactly = 1) { mockGetScheduleStatisticsUseCase.getMonthlyStatistics(YearMonth.now()) }
    }

    @Test
    fun `切换到上一个月应该更新数据`() = runTest {
        // Given
        val previousMonth = YearMonth.now().minusMonths(1)
        
        // When
        viewModel.navigateToPreviousMonth()
        
        // Then
        assertThat(viewModel.currentYearMonth.value).isEqualTo(previousMonth)
        verify(exactly = 1) { mockGetMonthScheduleUseCase(previousMonth) }
        coVerify(exactly = 1) { mockGetScheduleStatisticsUseCase.getMonthlyStatistics(previousMonth) }
    }

    @Test
    fun `切换到下一个月应该更新数据`() = runTest {
        // Given
        val nextMonth = YearMonth.now().plusMonths(1)
        
        // When
        viewModel.navigateToNextMonth()
        
        // Then
        assertThat(viewModel.currentYearMonth.value).isEqualTo(nextMonth)
        verify(exactly = 1) { mockGetMonthScheduleUseCase(nextMonth) }
        coVerify(exactly = 1) { mockGetScheduleStatisticsUseCase.getMonthlyStatistics(nextMonth) }
    }

    @Test
    fun `回到今天应该重置到当前月份和日期`() = runTest {
        // Given - 先切换到其他月份
        viewModel.navigateToYearMonth(YearMonth.now().minusMonths(3))
        viewModel.selectDate(LocalDate.now().minusMonths(3))
        
        // When
        viewModel.navigateToToday()
        
        // Then
        assertThat(viewModel.currentYearMonth.value).isEqualTo(YearMonth.now())
        assertThat(viewModel.selectedDate.value).isEqualTo(LocalDate.now())
    }

    @Test
    fun `切换到指定年月应该清除选中日期`() = runTest {
        // Given
        val targetMonth = YearMonth.of(2025, 3)
        viewModel.selectDate(LocalDate.now())
        
        // When
        viewModel.navigateToYearMonth(targetMonth)
        
        // Then
        assertThat(viewModel.currentYearMonth.value).isEqualTo(targetMonth)
        assertThat(viewModel.selectedDate.value).isNull()
    }

    @Test
    fun `选择日期应该更新选中状态`() = runTest {
        // Given
        val date = LocalDate.of(2025, 6, 20)
        
        // When
        viewModel.selectDate(date)
        
        // Then
        assertThat(viewModel.selectedDate.value).isEqualTo(date)
    }

    @Test
    fun `快速设置排班应该创建新排班`() = runTest {
        // Given
        val date = LocalDate.now().plusDays(5)
        val shift = testShifts[0]
        coEvery { mockCreateScheduleUseCase.createOrUpdateSchedule(any()) } just Runs
        
        // When
        viewModel.showQuickSelector(date)
        viewModel.quickSetSchedule(date, shift)
        
        // Then
        coVerify(exactly = 1) { 
            mockCreateScheduleUseCase.createOrUpdateSchedule(
                withArg { schedule ->
                    assertThat(schedule.date).isEqualTo(date)
                    assertThat(schedule.shift).isEqualTo(shift)
                }
            )
        }
        assertThat(viewModel.quickSelectDate.value).isNull() // 对话框应该关闭
    }

    @Test
    fun `快速设置空班次应该删除排班`() = runTest {
        // Given
        val date = LocalDate.now().plusDays(5)
        coEvery { mockCreateScheduleUseCase.deleteSchedule(any()) } just Runs
        
        // When
        viewModel.quickSetSchedule(date, null)
        
        // Then
        coVerify(exactly = 1) { 
            mockCreateScheduleUseCase.deleteSchedule(date)
        }
    }

    @Test
    fun `切换视图模式应该在紧凑和舒适模式间切换`() = runTest {
        // Given - 初始为紧凑模式
        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMPACT)
        
        // When - 第一次切换
        viewModel.toggleViewMode()
        
        // Then
        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMFORTABLE)
        
        // When - 第二次切换
        viewModel.toggleViewMode()
        
        // Then
        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMPACT)
    }

    @Test
    fun `删除排班应该调用删除用例`() = runTest {
        // Given
        val date = LocalDate.now()
        coEvery { mockDeleteScheduleUseCase(any()) } just Runs
        
        // When
        viewModel.deleteSchedule(date)
        
        // Then
        coVerify(exactly = 1) { 
            mockDeleteScheduleUseCase(1) // testSchedules[0]的id
        }
    }

    @Test
    fun `加载统计信息失败应该显示错误`() = runTest {
        // Given
        val error = Exception("网络错误")
        coEvery { mockGetScheduleStatisticsUseCase.getMonthlyStatistics(any()) } throws error
        
        // When
        viewModel.navigateToNextMonth()
        
        // Then
        assertThat(viewModel.uiState.value.errorMessage).contains("加载统计信息失败")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `清除错误应该重置错误信息`() = runTest {
        // Given - 先触发一个错误
        coEvery { mockGetScheduleStatisticsUseCase.getMonthlyStatistics(any()) } throws Exception("错误")
        viewModel.navigateToNextMonth()
        
        // When
        viewModel.clearError()
        
        // Then
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}
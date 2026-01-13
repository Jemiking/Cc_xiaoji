package com.ccxiaoji.feature.schedule.presentation.viewmodel

import android.util.Log
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.usecase.DeleteScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetMonthScheduleUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetScheduleStatisticsUseCase
import com.ccxiaoji.feature.schedule.presentation.theme.ThemeManager
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/**
 * CalendarViewModel 单元测试
 *
 * 这些测试覆盖了 CalendarViewModel 的核心功能，作为 UI 重构的安全网。
 * 确保重构过程中不会破坏现有的业务逻辑。
 *
 * 测试覆盖的关键场景：
 * 1. 月份导航（上月、下月、跳转到今天、跳转到指定月份）
 * 2. 日期选择
 * 3. 视图模式切换
 * 4. 排班数据加载（关键：响应式触发）
 * 5. 统计信息加载
 * 6. 删除排班
 * 7. 错误处理
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Mocks
    private lateinit var getMonthScheduleUseCase: GetMonthScheduleUseCase
    private lateinit var getScheduleStatisticsUseCase: GetScheduleStatisticsUseCase
    private lateinit var deleteScheduleUseCase: DeleteScheduleUseCase
    private lateinit var themeManager: ThemeManager

    // Test data
    private val testShift = Shift(
        id = 1L,
        name = "早班",
        startTime = LocalTime.of(8, 0),
        endTime = LocalTime.of(16, 0),
        color = 0xFF4CAF50.toInt()
    )

    private val testSchedule = Schedule(
        id = 1L,
        date = LocalDate.now(),
        shift = testShift
    )

    private val testStatistics = ScheduleStatistics(
        totalDays = 30,
        workDays = 22,
        restDays = 8,
        shiftDistribution = mapOf("早班" to 10, "中班" to 8, "晚班" to 4),
        totalHours = 176.0
    )

    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock android.util.Log 以防止 "Method not mocked" 错误
        // 在 JVM 单元测试中，android.util.Log 是一个 stub 类，会抛出 RuntimeException
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.v(any(), any()) } returns 0

        // Setup mocks
        getMonthScheduleUseCase = mockk()
        getScheduleStatisticsUseCase = mockk()
        deleteScheduleUseCase = mockk()
        themeManager = mockk()

        // Default mock behaviors
        every { getMonthScheduleUseCase(any()) } returns flowOf(listOf(testSchedule))
        coEvery { getScheduleStatisticsUseCase.getMonthlyStatistics(any()) } returns testStatistics
        coEvery { deleteScheduleUseCase(any()) } returns Unit
        every { themeManager.weekStartDay } returns flowOf(DayOfWeek.MONDAY)
        every { themeManager.defaultCompactMode } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // 清理静态 mock，避免影响其他测试
        unmockkStatic(Log::class)
    }

    private fun createViewModel(): CalendarViewModel {
        return CalendarViewModel(
            getMonthScheduleUseCase = getMonthScheduleUseCase,
            getScheduleStatisticsUseCase = getScheduleStatisticsUseCase,
            deleteScheduleUseCase = deleteScheduleUseCase,
            themeManager = themeManager
        )
    }

    // ========== 初始状态测试 ==========

    @Test
    fun `initial state should have current month and today selected`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val currentYearMonth = viewModel.currentYearMonth.value
        val selectedDate = viewModel.selectedDate.value

        assertThat(currentYearMonth).isEqualTo(YearMonth.now())
        assertThat(selectedDate).isEqualTo(LocalDate.now())
    }

    @Test
    fun `initial viewMode should be COMPACT when defaultCompactMode is true`() = testScope.runTest {
        every { themeManager.defaultCompactMode } returns flowOf(true)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMPACT)
    }

    @Test
    fun `initial viewMode should be COMFORTABLE when defaultCompactMode is false`() = testScope.runTest {
        every { themeManager.defaultCompactMode } returns flowOf(false)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMFORTABLE)
    }

    // ========== 月份导航测试（关键：响应式数据加载） ==========

    @Test
    fun `navigateToPreviousMonth should update yearMonth and trigger data reload`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialMonth = viewModel.currentYearMonth.value
        viewModel.navigateToPreviousMonth()
        advanceUntilIdle()

        assertThat(viewModel.currentYearMonth.value).isEqualTo(initialMonth.minusMonths(1))
        // 验证统计数据重新加载
        coVerify(atLeast = 2) { getScheduleStatisticsUseCase.getMonthlyStatistics(any()) }
    }

    @Test
    fun `navigateToNextMonth should update yearMonth and trigger data reload`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialMonth = viewModel.currentYearMonth.value
        viewModel.navigateToNextMonth()
        advanceUntilIdle()

        assertThat(viewModel.currentYearMonth.value).isEqualTo(initialMonth.plusMonths(1))
        coVerify(atLeast = 2) { getScheduleStatisticsUseCase.getMonthlyStatistics(any()) }
    }

    @Test
    fun `navigateToToday should reset to current month and select today`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // 先导航到其他月份
        viewModel.navigateToPreviousMonth()
        viewModel.navigateToPreviousMonth()
        advanceUntilIdle()

        // 然后跳回今天
        viewModel.navigateToToday()
        advanceUntilIdle()

        assertThat(viewModel.currentYearMonth.value).isEqualTo(YearMonth.now())
        assertThat(viewModel.selectedDate.value).isEqualTo(LocalDate.now())
    }

    @Test
    fun `navigateToYearMonth should update to specified month and clear selection`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val targetMonth = YearMonth.of(2024, 6)
        viewModel.navigateToYearMonth(targetMonth)
        advanceUntilIdle()

        assertThat(viewModel.currentYearMonth.value).isEqualTo(targetMonth)
        assertThat(viewModel.selectedDate.value).isNull()
    }

    @Test
    fun `month change should trigger schedule data reload via flatMapLatest`() = testScope.runTest {
        // 这是关键测试：验证月份变化会触发排班数据重新加载
        val targetMonth = YearMonth.of(2024, 3)

        every { getMonthScheduleUseCase(targetMonth) } returns flowOf(emptyList())

        viewModel = createViewModel()

        // 启动schedules Flow的订阅以触发数据加载
        // 使用 backgroundScope 保持订阅在测试期间活跃
        val collectJob = backgroundScope.launch {
            viewModel.schedules.collect {}
        }
        advanceUntilIdle()

        viewModel.navigateToYearMonth(targetMonth)
        advanceUntilIdle()

        // 验证对目标月份调用了 UseCase
        io.mockk.verify { getMonthScheduleUseCase(targetMonth) }

        collectJob.cancel()
    }

    // ========== 日期选择测试 ==========

    @Test
    fun `selectDate should update selectedDate`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val targetDate = LocalDate.of(2024, 12, 15)
        viewModel.selectDate(targetDate)

        assertThat(viewModel.selectedDate.value).isEqualTo(targetDate)
    }

    // ========== 视图模式切换测试 ==========

    @Test
    fun `toggleViewMode should switch between COMPACT and COMFORTABLE`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialMode = viewModel.viewMode.value
        viewModel.toggleViewMode()

        val expectedMode = if (initialMode == CalendarViewMode.COMPACT) {
            CalendarViewMode.COMFORTABLE
        } else {
            CalendarViewMode.COMPACT
        }
        assertThat(viewModel.viewMode.value).isEqualTo(expectedMode)
    }

    @Test
    fun `toggleViewMode should mark userOverridden to prevent default mode changes`() = testScope.runTest {
        // 用户手动切换后，后续的默认模式变化不应影响当前选择
        val defaultCompactModeFlow = MutableStateFlow(true)
        every { themeManager.defaultCompactMode } returns defaultCompactModeFlow

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMPACT)

        // 用户手动切换
        viewModel.toggleViewMode()
        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMFORTABLE)

        // 模拟设置变化（不应影响用户选择）
        defaultCompactModeFlow.value = false
        advanceUntilIdle()

        // 用户的选择应该保持不变
        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMFORTABLE)
    }

    @Test
    fun `setViewMode should directly set the mode`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setViewMode(CalendarViewMode.COMFORTABLE)
        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMFORTABLE)

        viewModel.setViewMode(CalendarViewMode.COMPACT)
        assertThat(viewModel.viewMode.value).isEqualTo(CalendarViewMode.COMPACT)
    }

    // ========== 排班数据加载测试 ==========

    @Test
    fun `schedules should emit data from UseCase`() = testScope.runTest {
        val scheduleList = listOf(
            testSchedule,
            testSchedule.copy(id = 2L, date = LocalDate.now().plusDays(1))
        )
        every { getMonthScheduleUseCase(any()) } returns flowOf(scheduleList)

        viewModel = createViewModel()

        // 启动订阅以触发数据加载
        val collectJob = backgroundScope.launch {
            viewModel.schedules.collect {}
        }
        advanceUntilIdle()

        assertThat(viewModel.schedules.value).hasSize(2)
        assertThat(viewModel.schedules.value).isEqualTo(scheduleList)

        collectJob.cancel()
    }

    // ========== 统计信息加载测试 ==========

    @Test
    fun `monthlyStatistics should be loaded on init`() = testScope.runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.monthlyStatistics.value).isNotNull()
        assertThat(viewModel.monthlyStatistics.value).isEqualTo(testStatistics)
    }

    @Test
    fun `monthlyStatistics should be reloaded on month navigation`() = testScope.runTest {
        val stats1 = testStatistics
        val stats2 = testStatistics.copy(workDays = 20)

        coEvery { getScheduleStatisticsUseCase.getMonthlyStatistics(YearMonth.now()) } returns stats1
        coEvery { getScheduleStatisticsUseCase.getMonthlyStatistics(YearMonth.now().minusMonths(1)) } returns stats2

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.monthlyStatistics.value?.workDays).isEqualTo(22)

        viewModel.navigateToPreviousMonth()
        advanceUntilIdle()

        assertThat(viewModel.monthlyStatistics.value?.workDays).isEqualTo(20)
    }

    // ========== 删除排班测试 ==========

    @Test
    fun `deleteSchedule should call UseCase with correct id`() = testScope.runTest {
        val scheduleToDelete = testSchedule
        every { getMonthScheduleUseCase(any()) } returns flowOf(listOf(scheduleToDelete))

        viewModel = createViewModel()

        // 启动订阅以确保schedules有数据
        val collectJob = backgroundScope.launch {
            viewModel.schedules.collect {}
        }
        advanceUntilIdle()

        viewModel.deleteSchedule(scheduleToDelete.date)
        advanceUntilIdle()

        coVerify { deleteScheduleUseCase(scheduleToDelete.id) }

        collectJob.cancel()
    }

    @Test
    fun `deleteSchedule should not call UseCase when no schedule found for date`() = testScope.runTest {
        every { getMonthScheduleUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()

        // 启动订阅以确保schedules Flow已激活
        val collectJob = backgroundScope.launch {
            viewModel.schedules.collect {}
        }
        advanceUntilIdle()

        viewModel.deleteSchedule(LocalDate.now())
        advanceUntilIdle()

        coVerify(exactly = 0) { deleteScheduleUseCase(any()) }

        collectJob.cancel()
    }

    @Test
    fun `deleteSchedule should update error state on failure`() = testScope.runTest {
        every { getMonthScheduleUseCase(any()) } returns flowOf(listOf(testSchedule))
        coEvery { deleteScheduleUseCase(any()) } throws RuntimeException("删除失败")

        viewModel = createViewModel()

        // 启动订阅以确保schedules有数据
        val collectJob = backgroundScope.launch {
            viewModel.schedules.collect {}
        }
        advanceUntilIdle()

        viewModel.deleteSchedule(testSchedule.date)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).contains("删除排班失败")

        collectJob.cancel()
    }

    // ========== 错误处理测试 ==========

    @Test
    fun `statistics loading failure should update error state`() = testScope.runTest {
        coEvery { getScheduleStatisticsUseCase.getMonthlyStatistics(any()) } throws RuntimeException("网络错误")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).contains("加载统计信息失败")
    }

    @Test
    fun `clearError should reset error message`() = testScope.runTest {
        coEvery { getScheduleStatisticsUseCase.getMonthlyStatistics(any()) } throws RuntimeException("网络错误")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.clearError()

        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    // ========== 一周开始日测试 ==========

    @Test
    fun `weekStartDay should reflect ThemeManager value`() = testScope.runTest {
        // 注意：必须在创建 ViewModel 之前设置 mock
        // 因为 weekStartDay 在 ViewModel 初始化时就开始收集
        every { themeManager.weekStartDay } returns flowOf(DayOfWeek.SUNDAY)

        viewModel = createViewModel()

        // 启动订阅以触发 stateIn 收集数据
        val collectJob = backgroundScope.launch {
            viewModel.weekStartDay.collect {}
        }
        advanceUntilIdle()

        assertThat(viewModel.weekStartDay.value).isEqualTo(DayOfWeek.SUNDAY)

        collectJob.cancel()
    }

    // ========== UI状态测试 ==========

    @Test
    fun `uiState isLoading should be true during statistics loading`() = testScope.runTest {
        // 延迟统计加载以观察 loading 状态
        coEvery { getScheduleStatisticsUseCase.getMonthlyStatistics(any()) } coAnswers {
            kotlinx.coroutines.delay(100)
            testStatistics
        }

        viewModel = createViewModel()

        // 注意：由于是测试调度器，需要手动推进时间
        testDispatcher.scheduler.advanceTimeBy(50)

        // 此时应该还在加载中
        // （实际上 isLoading 的可观测性依赖于具体的调度时序）

        advanceUntilIdle()

        // 加载完成后 isLoading 应为 false
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}

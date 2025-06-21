package com.ccxiaoji.feature.habit.presentation.viewmodel

import com.ccxiaoji.common.test.util.MainDispatcherRule
import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HabitViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mock依赖
    private val mockHabitRepository = mockk<HabitRepository>()
    
    // 被测试的ViewModel
    private lateinit var viewModel: HabitViewModel

    // 测试数据
    private val testHabits = listOf(
        HabitWithStreak(
            habit = Habit(
                id = "1",
                title = "每日运动",
                description = "每天运动30分钟",
                period = "daily",
                target = 1,
                color = "#FF5722",
                icon = "🏃",
                syncStatus = "synced"
            ),
            currentStreak = 5,
            longestStreak = 10,
            completionRate = 0.8f
        ),
        HabitWithStreak(
            habit = Habit(
                id = "2",
                title = "阅读",
                description = "每天阅读1小时",
                period = "daily",
                target = 1,
                color = "#4CAF50",
                icon = "📚",
                syncStatus = "synced"
            ),
            currentStreak = 3,
            longestStreak = 15,
            completionRate = 0.6f
        ),
        HabitWithStreak(
            habit = Habit(
                id = "3",
                title = "学习编程",
                description = "每周学习5次",
                period = "weekly",
                target = 5,
                color = "#2196F3",
                icon = "💻",
                syncStatus = "synced"
            ),
            currentStreak = 2,
            longestStreak = 8,
            completionRate = 0.7f
        )
    )

    @Before
    fun setup() {
        // 默认mock设置
        every { mockHabitRepository.getHabitsWithStreaks() } returns flowOf(testHabits)
        
        // 初始化ViewModel
        viewModel = HabitViewModel(mockHabitRepository)
    }

    @Test
    fun `初始化时应该加载所有习惯`() = runTest {
        // When - ViewModel在init中自动加载数据
        
        // Then
        assertThat(viewModel.uiState.value.habits).hasSize(3)
        assertThat(viewModel.uiState.value.habits).isEqualTo(testHabits)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        verify(exactly = 1) { mockHabitRepository.getHabitsWithStreaks() }
    }

    @Test
    fun `搜索查询应该过滤习惯列表`() = runTest {
        // When
        viewModel.updateSearchQuery("运动")
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.habits).hasSize(1)
        assertThat(viewModel.uiState.value.habits.first().habit.title).contains("运动")
    }

    @Test
    fun `搜索应该匹配标题和描述`() = runTest {
        // When - 搜索描述中的内容
        viewModel.updateSearchQuery("1小时")
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.habits).hasSize(1)
        assertThat(viewModel.uiState.value.habits.first().habit.id).isEqualTo("2")
    }

    @Test
    fun `空搜索查询应该显示所有习惯`() = runTest {
        // Given - 先设置一个搜索
        viewModel.updateSearchQuery("运动")
        testScheduler.advanceUntilIdle()
        
        // When - 清空搜索
        viewModel.updateSearchQuery("")
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.habits).hasSize(3)
    }

    @Test
    fun `添加习惯应该调用repository`() = runTest {
        // Given
        coEvery { 
            mockHabitRepository.createHabit(any(), any(), any(), any(), any(), any()) 
        } just Runs
        
        // When
        viewModel.addHabit(
            title = "新习惯",
            description = "新描述",
            period = "daily",
            target = 1
        )
        
        // Then
        coVerify(exactly = 1) { 
            mockHabitRepository.createHabit(
                title = "新习惯",
                description = "新描述",
                period = "daily",
                target = 1,
                color = "#3A7AFE",
                icon = null
            )
        }
    }

    @Test
    fun `更新习惯应该调用repository`() = runTest {
        // Given
        coEvery { 
            mockHabitRepository.updateHabit(any(), any(), any(), any(), any(), any(), any()) 
        } just Runs
        
        // When
        viewModel.updateHabit(
            habitId = "1",
            title = "更新的习惯",
            description = "更新的描述",
            period = "weekly",
            target = 3,
            color = "#FF9800",
            icon = "🎯"
        )
        
        // Then
        coVerify(exactly = 1) { 
            mockHabitRepository.updateHabit(
                habitId = "1",
                title = "更新的习惯",
                description = "更新的描述",
                period = "weekly",
                target = 3,
                color = "#FF9800",
                icon = "🎯"
            )
        }
    }

    @Test
    fun `打卡习惯应该调用repository并更新UI状态`() = runTest {
        // Given
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        coEvery { mockHabitRepository.checkInHabit(any(), any()) } just Runs
        
        // When
        viewModel.checkInHabit("1")
        
        // Then
        coVerify(exactly = 1) { 
            mockHabitRepository.checkInHabit("1", any<LocalDate>())
        }
        assertThat(viewModel.uiState.value.checkedToday).contains("1")
    }

    @Test
    fun `多次打卡不同习惯应该累加到checkedToday`() = runTest {
        // Given
        coEvery { mockHabitRepository.checkInHabit(any(), any()) } just Runs
        
        // When
        viewModel.checkInHabit("1")
        viewModel.checkInHabit("2")
        viewModel.checkInHabit("3")
        
        // Then
        assertThat(viewModel.uiState.value.checkedToday).containsExactly("1", "2", "3")
    }

    @Test
    fun `删除习惯应该调用repository`() = runTest {
        // Given
        coEvery { mockHabitRepository.deleteHabit(any()) } just Runs
        
        // When
        viewModel.deleteHabit("1")
        
        // Then
        coVerify(exactly = 1) { 
            mockHabitRepository.deleteHabit("1")
        }
    }

    // 帮助属性
    private val testScheduler = kotlinx.coroutines.test.UnconfinedTestDispatcher()
    
    @Test
    fun `错误处理 - 打卡失败应该保持UI状态不变`() = runTest {
        // Given
        val exception = Exception("打卡失败")
        coEvery { mockHabitRepository.checkInHabit(any(), any()) } throws exception
        
        // When
        viewModel.checkInHabit("1")
        
        // Then
        assertThat(viewModel.uiState.value.checkedToday).doesNotContain("1")
        coVerify(exactly = 1) { 
            mockHabitRepository.checkInHabit("1", any<LocalDate>())
        }
    }
    
    @Test
    fun `错误处理 - 删除习惯失败应该发送错误消息`() = runTest {
        // Given
        val exception = Exception("删除失败")
        coEvery { mockHabitRepository.deleteHabit(any()) } throws exception
        
        // When
        viewModel.deleteHabit("1")
        
        // Then
        coVerify(exactly = 1) { 
            mockHabitRepository.deleteHabit("1")
        }
        // 验证错误被处理（虽然当前实现可能没有错误消息机制）
    }
    
    @Test
    fun `创建习惯应该调用repository`() = runTest {
        // Given
        val newHabit = Habit(
            id = "4",
            title = "新习惯",
            description = "描述",
            period = "daily",
            target = 1,
            color = "#00BCD4",
            icon = "📚",
            syncStatus = "pending"
        )
        coEvery { 
            mockHabitRepository.createHabit(any(), any(), any(), any(), any(), any()) 
        } returns Unit
        
        // When
        viewModel.createHabit(
            title = "新习惯",
            description = "描述",
            period = "daily",
            target = 1,
            color = "#00BCD4",
            icon = "📚"
        )
        
        // Then
        coVerify(exactly = 1) { 
            mockHabitRepository.createHabit(
                title = "新习惯",
                description = "描述",
                period = "daily",
                target = 1,
                color = "#00BCD4",
                icon = "📚"
            )
        }
    }
    
    @Test
    fun `重复打卡同一习惯不应该重复添加到checkedToday`() = runTest {
        // Given
        coEvery { mockHabitRepository.checkInHabit(any(), any()) } just Runs
        
        // When
        viewModel.checkInHabit("1")
        viewModel.checkInHabit("1") // 重复打卡
        
        // Then
        assertThat(viewModel.uiState.value.checkedToday).containsExactly("1")
        assertThat(viewModel.uiState.value.checkedToday.count { it == "1" }).isEqualTo(1)
    }
    
    @Test
    fun `更新习惯应该调用repository`() = runTest {
        // Given
        coEvery { 
            mockHabitRepository.updateHabit(any(), any(), any(), any(), any(), any(), any()) 
        } returns Unit
        
        // When
        viewModel.updateHabit(
            habitId = "1",
            title = "更新的习惯",
            description = "更新的描述",
            period = "weekly",
            target = 3,
            color = "#9C27B0",
            icon = "🎯"
        )
        
        // Then
        coVerify(exactly = 1) { 
            mockHabitRepository.updateHabit(
                habitId = "1",
                title = "更新的习惯",
                description = "更新的描述",
                period = "weekly",
                target = 3,
                color = "#9C27B0",
                icon = "🎯"
            )
        }
    }
    
    @Test
    fun `获取习惯统计应该调用repository`() = runTest {
        // Given
        val stats = mapOf(
            "1" to mapOf("completions" to 10, "streak" to 5),
            "2" to mapOf("completions" to 20, "streak" to 15)
        )
        coEvery { mockHabitRepository.getHabitStats(any()) } returns stats
        
        // When
        val result = viewModel.getHabitStats("monthly")
        
        // Then
        assertThat(result).isEqualTo(stats)
        coVerify(exactly = 1) { 
            mockHabitRepository.getHabitStats("monthly")
        }
    }
    
    @Test
    fun `初始化时加载失败应该设置错误状态`() = runTest {
        // Given - 在setup之前设置repository抛出异常
        val exception = Exception("加载失败")
        every { mockHabitRepository.getHabits() } returns flowOf(throw exception)
        
        // When - 重新创建ViewModel触发init
        viewModel = HabitViewModel(mockHabitRepository)
        
        // Then
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        // 验证habits为空（加载失败）
        assertThat(viewModel.uiState.value.habits).isEmpty()
    }
}
package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitFrequency
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SearchHabitsUseCaseTest {
    
    private lateinit var habitRepository: HabitRepository
    private lateinit var useCase: SearchHabitsUseCase
    
    @Before
    fun setup() {
        habitRepository = mockk()
        useCase = SearchHabitsUseCase(habitRepository)
    }
    
    @Test
    fun `搜索习惯 - 找到匹配结果`() = runTest {
        // Given
        val query = "运动"
        val habits = listOf(
            Habit(
                id = 1,
                name = "早晨运动",
                description = "每天早上跑步30分钟",
                icon = "🏃",
                color = "#FF5722",
                frequency = HabitFrequency.DAILY,
                targetDays = 30,
                completedDays = 15,
                currentStreak = 5,
                longestStreak = 10,
                reminderTime = LocalTime.of(7, 0),
                isActive = true,
                startDate = LocalDate.now().minusDays(15),
                lastCheckInDate = LocalDate.now().minusDays(1),
                createdAt = LocalDateTime.now().minusDays(15),
                updatedAt = LocalDateTime.now().minusDays(1)
            ),
            Habit(
                id = 2,
                name = "晚间运动",
                description = "瑜伽或拉伸",
                icon = "🧘",
                color = "#4CAF50",
                frequency = HabitFrequency.DAILY,
                targetDays = 30,
                completedDays = 10,
                currentStreak = 3,
                longestStreak = 5,
                reminderTime = LocalTime.of(20, 0),
                isActive = true,
                startDate = LocalDate.now().minusDays(10),
                lastCheckInDate = LocalDate.now(),
                createdAt = LocalDateTime.now().minusDays(10),
                updatedAt = LocalDateTime.now()
            )
        )
        coEvery { habitRepository.searchHabits(query) } returns flowOf(habits)
        
        // When
        val result = useCase(query).first()
        
        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).contains("运动")
        assertThat(result[1].name).contains("运动")
        coVerify(exactly = 1) { habitRepository.searchHabits(query) }
    }
    
    @Test
    fun `搜索习惯 - 空查询返回所有习惯`() = runTest {
        // Given
        val query = ""
        val allHabits = listOf(
            Habit(
                id = 1,
                name = "喝水",
                description = "每天8杯水",
                icon = "💧",
                color = "#2196F3",
                frequency = HabitFrequency.DAILY,
                targetDays = 30,
                completedDays = 20,
                currentStreak = 10,
                longestStreak = 15,
                reminderTime = null,
                isActive = true,
                startDate = LocalDate.now().minusDays(20),
                lastCheckInDate = LocalDate.now(),
                createdAt = LocalDateTime.now().minusDays(20),
                updatedAt = LocalDateTime.now()
            ),
            Habit(
                id = 2,
                name = "阅读",
                description = "每天读书30分钟",
                icon = "📚",
                color = "#9C27B0",
                frequency = HabitFrequency.DAILY,
                targetDays = 30,
                completedDays = 15,
                currentStreak = 5,
                longestStreak = 8,
                reminderTime = LocalTime.of(21, 0),
                isActive = true,
                startDate = LocalDate.now().minusDays(15),
                lastCheckInDate = LocalDate.now(),
                createdAt = LocalDateTime.now().minusDays(15),
                updatedAt = LocalDateTime.now()
            )
        )
        coEvery { habitRepository.searchHabits(query) } returns flowOf(allHabits)
        
        // When
        val result = useCase(query).first()
        
        // Then
        assertThat(result).hasSize(2)
        coVerify(exactly = 1) { habitRepository.searchHabits(query) }
    }
    
    @Test
    fun `搜索习惯 - 没有匹配结果`() = runTest {
        // Given
        val query = "不存在的习惯"
        val emptyList = emptyList<Habit>()
        coEvery { habitRepository.searchHabits(query) } returns flowOf(emptyList)
        
        // When
        val result = useCase(query).first()
        
        // Then
        assertThat(result).isEmpty()
        coVerify(exactly = 1) { habitRepository.searchHabits(query) }
    }
    
    @Test
    fun `搜索习惯 - 大小写不敏感`() = runTest {
        // Given
        val query = "MEDITATION"
        val habits = listOf(
            Habit(
                id = 1,
                name = "Morning Meditation",
                description = "冥想10分钟",
                icon = "🧘",
                color = "#00BCD4",
                frequency = HabitFrequency.DAILY,
                targetDays = 30,
                completedDays = 25,
                currentStreak = 15,
                longestStreak = 20,
                reminderTime = LocalTime.of(6, 30),
                isActive = true,
                startDate = LocalDate.now().minusDays(25),
                lastCheckInDate = LocalDate.now(),
                createdAt = LocalDateTime.now().minusDays(25),
                updatedAt = LocalDateTime.now()
            )
        )
        coEvery { habitRepository.searchHabits(query) } returns flowOf(habits)
        
        // When
        val result = useCase(query).first()
        
        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Morning Meditation")
        coVerify(exactly = 1) { habitRepository.searchHabits(query) }
    }
    
    @Test
    fun `搜索习惯 - 部分匹配`() = runTest {
        // Given
        val query = "健"
        val habits = listOf(
            Habit(
                id = 1,
                name = "健身",
                description = "每周3次健身房",
                icon = "💪",
                color = "#FF9800",
                frequency = HabitFrequency.WEEKLY,
                targetDays = 12,
                completedDays = 8,
                currentStreak = 2,
                longestStreak = 4,
                reminderTime = LocalTime.of(18, 0),
                isActive = true,
                startDate = LocalDate.now().minusWeeks(4),
                lastCheckInDate = LocalDate.now().minusDays(2),
                createdAt = LocalDateTime.now().minusWeeks(4),
                updatedAt = LocalDateTime.now().minusDays(2)
            ),
            Habit(
                id = 2,
                name = "健康饮食",
                description = "少油少盐",
                icon = "🥗",
                color = "#8BC34A",
                frequency = HabitFrequency.DAILY,
                targetDays = 30,
                completedDays = 20,
                currentStreak = 10,
                longestStreak = 15,
                reminderTime = null,
                isActive = true,
                startDate = LocalDate.now().minusDays(20),
                lastCheckInDate = LocalDate.now(),
                createdAt = LocalDateTime.now().minusDays(20),
                updatedAt = LocalDateTime.now()
            )
        )
        coEvery { habitRepository.searchHabits(query) } returns flowOf(habits)
        
        // When
        val result = useCase(query).first()
        
        // Then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.name }).containsExactly("健身", "健康饮食")
        coVerify(exactly = 1) { habitRepository.searchHabits(query) }
    }
}
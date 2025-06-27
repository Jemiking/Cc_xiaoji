package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class GetHabitsUseCaseTest {

    @MockK
    private lateinit var habitRepository: HabitRepository

    private lateinit var getHabitsUseCase: GetHabitsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // 假设存在一个GetHabitsUseCase类
        getHabitsUseCase = GetHabitsUseCase(habitRepository)
    }

    @Test
    fun `获取所有习惯列表`() = runTest {
        // Given
        val now = Clock.System.now()
        val habits = listOf(
            Habit(
                id = "habit1",
                title = "每日运动",
                description = "保持健康的生活方式",
                period = "daily",
                target = 1,
                color = "#FF5722",
                icon = "🏃",
                createdAt = now,
                updatedAt = now
            ),
            Habit(
                id = "habit2",
                title = "阅读",
                description = "每天阅读30分钟",
                period = "daily",
                target = 30,
                color = "#4CAF50",
                icon = "📚",
                createdAt = now,
                updatedAt = now
            )
        )

        coEvery { habitRepository.getHabits() } returns flowOf(habits)

        // When
        val result = getHabitsUseCase.invoke().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].title).isEqualTo("每日运动")
        assertThat(result[1].title).isEqualTo("阅读")
        coVerify(exactly = 1) { habitRepository.getHabits() }
    }

    @Test
    fun `获取习惯和连续天数信息`() = runTest {
        // Given
        val now = Clock.System.now()
        val habit = Habit(
            id = "habit1",
            title = "冥想",
            description = "每日冥想10分钟",
            period = "daily",
            target = 1,
            color = "#9C27B0",
            icon = "🧘",
            createdAt = now,
            updatedAt = now
        )
        
        val habitsWithStreak = listOf(
            HabitWithStreak(
                habit = habit,
                currentStreak = 7,
                completedCount = 25,
                longestStreak = 15
            )
        )

        coEvery { habitRepository.getHabitsWithStreaks() } returns flowOf(habitsWithStreak)

        // When
        val result = getHabitsUseCase.getHabitsWithStreaks().first()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].habit.title).isEqualTo("冥想")
        assertThat(result[0].currentStreak).isEqualTo(7)
        assertThat(result[0].completedCount).isEqualTo(25)
        assertThat(result[0].longestStreak).isEqualTo(15)
        coVerify(exactly = 1) { habitRepository.getHabitsWithStreaks() }
    }

    @Test
    fun `搜索习惯功能`() = runTest {
        // Given
        val searchQuery = "运动"
        val now = Clock.System.now()
        val searchResults = listOf(
            Habit(
                id = "habit1",
                title = "每日运动",
                description = "健身30分钟",
                period = "daily",
                target = 1,
                color = "#FF5722",
                icon = "🏃",
                createdAt = now,
                updatedAt = now
            )
        )

        coEvery { habitRepository.searchHabits(searchQuery) } returns flowOf(searchResults)

        // When
        val result = getHabitsUseCase.searchHabits(searchQuery).first()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].title).contains("运动")
        coVerify(exactly = 1) { habitRepository.searchHabits(searchQuery) }
    }

    @Test
    fun `获取今日已打卡习惯数量`() = runTest {
        // Given
        val checkedCount = 3
        coEvery { habitRepository.getTodayCheckedHabitsCount() } returns flowOf(checkedCount)

        // When
        val result = getHabitsUseCase.getTodayCheckedCount().first()

        // Then
        assertThat(result).isEqualTo(3)
        coVerify(exactly = 1) { habitRepository.getTodayCheckedHabitsCount() }
    }

    @Test
    fun `获取活跃习惯数量`() = runTest {
        // Given
        val activeCount = 5
        coEvery { habitRepository.getActiveHabitsCount() } returns flowOf(activeCount)

        // When
        val result = getHabitsUseCase.getActiveHabitsCount().first()

        // Then
        assertThat(result).isEqualTo(5)
        coVerify(exactly = 1) { habitRepository.getActiveHabitsCount() }
    }
}
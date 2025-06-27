package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class CreateHabitUseCaseTest {

    @MockK
    private lateinit var habitRepository: HabitRepository
    
    private lateinit var createHabitUseCase: CreateHabitUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        createHabitUseCase = CreateHabitUseCase(habitRepository)
    }

    @Test
    fun `invoke should create habit successfully with valid inputs`() = runTest {
        // Given
        val title = "晨跑"
        val description = "每天早上跑步30分钟"
        val period = "daily"
        val target = 1
        val color = "#FF5722"
        val icon = "🏃"
        
        val now = Clock.System.now()
        val expectedHabit = Habit(
            id = "123",
            title = title,
            description = description,
            period = period,
            target = target,
            color = color,
            icon = icon,
            createdAt = now,
            updatedAt = now,
            completedCount = 0,
            lastCompletedAt = null,
            currentStreak = 0,
            longestStreak = 0
        )
        
        coEvery { 
            habitRepository.createHabit(
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            ) 
        } returns BaseResult.Success(expectedHabit)

        // When
        val result = createHabitUseCase(
            title = title,
            description = description,
            period = period,
            target = target,
            color = color,
            icon = icon
        )

        // Then
        assertThat(result).isEqualTo(expectedHabit)
        assertThat(result.title).isEqualTo(title)
        assertThat(result.period).isEqualTo(period)
        coVerify(exactly = 1) { 
            habitRepository.createHabit(
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            )
        }
    }

    @Test
    fun `invoke should throw exception when title is blank`() = runTest {
        // Given
        val title = "   "
        val description = "描述"
        val period = "daily"
        val target = 1
        val color = "#FF5722"
        val icon = "🏃"

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            createHabitUseCase(
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            )
        }
        coVerify(exactly = 0) { 
            habitRepository.createHabit(any(), any(), any(), any(), any(), any()) 
        }
    }

    @Test
    fun `invoke should throw exception when period is invalid`() = runTest {
        // Given
        val title = "晨跑"
        val description = "每天早上跑步30分钟"
        val period = "invalid_period" // Invalid
        val target = 1
        val color = "#FF5722"
        val icon = "🏃"

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            createHabitUseCase(
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            )
        }
    }

    @Test
    fun `invoke should throw exception when target is zero or negative`() = runTest {
        // Given
        val title = "晨跑"
        val description = "每天早上跑步30分钟"
        val period = "daily"
        val target = 0 // Invalid: should be > 0
        val color = "#FF5722"
        val icon = "🏃"

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            createHabitUseCase(
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            )
        }
    }

    @Test
    fun `invoke should throw exception when repository fails`() = runTest {
        // Given
        val title = "晨跑"
        val description = "每天早上跑步30分钟"
        val period = "daily"
        val target = 1
        val color = "#FF5722"
        val icon = "🏃"
        
        val exception = Exception("数据库错误")
        coEvery { 
            habitRepository.createHabit(
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            ) 
        } returns BaseResult.Error(exception)

        // When & Then
        val thrownException = assertFailsWith<Exception> {
            createHabitUseCase(
                title = title,
                description = description,
                period = period,
                target = target,
                color = color,
                icon = icon
            )
        }
        assertThat(thrownException.message).isEqualTo("数据库错误")
    }
}
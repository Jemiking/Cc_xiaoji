package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.habit.domain.model.CheckInRecord
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

class CheckInHabitUseCaseTest {

    @MockK
    private lateinit var habitRepository: HabitRepository
    
    private lateinit var checkInHabitUseCase: CheckInHabitUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        checkInHabitUseCase = CheckInHabitUseCase(habitRepository)
    }

    @Test
    fun `invoke should check in habit successfully`() = runTest {
        // Given
        val habitId = "habit123"
        val note = "早上6点起床跑步"
        
        val now = Clock.System.now()
        val expectedRecord = CheckInRecord(
            id = "record123",
            habitId = habitId,
            checkInAt = now,
            note = note
        )
        
        coEvery { 
            habitRepository.checkInHabit(habitId, note) 
        } returns BaseResult.Success(expectedRecord)

        // When
        val result = checkInHabitUseCase(habitId, note)

        // Then
        assertThat(result).isEqualTo(expectedRecord)
        assertThat(result.habitId).isEqualTo(habitId)
        assertThat(result.note).isEqualTo(note)
        coVerify(exactly = 1) { 
            habitRepository.checkInHabit(habitId, note)
        }
    }

    @Test
    fun `invoke should throw exception when habitId is blank`() = runTest {
        // Given
        val habitId = ""
        val note = "笔记"

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            checkInHabitUseCase(habitId, note)
        }
        coVerify(exactly = 0) { 
            habitRepository.checkInHabit(any(), any()) 
        }
    }

    @Test
    fun `invoke should check in without note`() = runTest {
        // Given
        val habitId = "habit123"
        val note: String? = null
        
        val now = Clock.System.now()
        val expectedRecord = CheckInRecord(
            id = "record123",
            habitId = habitId,
            checkInAt = now,
            note = null
        )
        
        coEvery { 
            habitRepository.checkInHabit(habitId, note) 
        } returns BaseResult.Success(expectedRecord)

        // When
        val result = checkInHabitUseCase(habitId, note)

        // Then
        assertThat(result).isEqualTo(expectedRecord)
        assertThat(result.note).isNull()
        coVerify(exactly = 1) { 
            habitRepository.checkInHabit(habitId, note)
        }
    }

    @Test
    fun `invoke should throw exception when repository fails`() = runTest {
        // Given
        val habitId = "habit123"
        val note = "笔记"
        val exception = Exception("数据库错误")
        
        coEvery { 
            habitRepository.checkInHabit(habitId, note) 
        } returns BaseResult.Error(exception)

        // When & Then
        val thrownException = assertFailsWith<Exception> {
            checkInHabitUseCase(habitId, note)
        }
        assertThat(thrownException.message).isEqualTo("数据库错误")
    }

    @Test
    fun `invoke should throw exception when habit not found`() = runTest {
        // Given
        val habitId = "nonexistent"
        val note = "笔记"
        val exception = DomainException.BusinessException("习惯不存在")
        
        coEvery { 
            habitRepository.checkInHabit(habitId, note) 
        } returns BaseResult.Error(exception)

        // When & Then
        val thrownException = assertFailsWith<DomainException.BusinessException> {
            checkInHabitUseCase(habitId, note)
        }
        assertThat(thrownException.message).isEqualTo("习惯不存在")
    }
}
package com.ccxiaoji.feature.habit.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class DeleteHabitUseCaseTest {

    @MockK
    private lateinit var habitRepository: HabitRepository
    
    private lateinit var deleteHabitUseCase: DeleteHabitUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        deleteHabitUseCase = DeleteHabitUseCase(habitRepository)
    }

    @Test
    fun `invoke should delete habit successfully`() = runTest {
        // Given
        val habitId = "habit123"
        
        coEvery { 
            habitRepository.deleteHabit(habitId) 
        } returns BaseResult.Success(Unit)

        // When
        deleteHabitUseCase(habitId)

        // Then
        coVerify(exactly = 1) { 
            habitRepository.deleteHabit(habitId)
        }
    }

    @Test
    fun `invoke should throw exception when habitId is blank`() = runTest {
        // Given
        val habitId = "   "

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            deleteHabitUseCase(habitId)
        }
        coVerify(exactly = 0) { 
            habitRepository.deleteHabit(any()) 
        }
    }

    @Test
    fun `invoke should throw exception when habit not found`() = runTest {
        // Given
        val habitId = "nonexistent"
        val exception = DomainException.BusinessException("习惯不存在")
        
        coEvery { 
            habitRepository.deleteHabit(habitId) 
        } returns BaseResult.Error(exception)

        // When & Then
        val thrownException = assertFailsWith<DomainException.BusinessException> {
            deleteHabitUseCase(habitId)
        }
        assertThat(thrownException.message).isEqualTo("习惯不存在")
    }

    @Test
    fun `invoke should throw exception when repository fails`() = runTest {
        // Given
        val habitId = "habit123"
        val exception = Exception("数据库错误")
        
        coEvery { 
            habitRepository.deleteHabit(habitId) 
        } returns BaseResult.Error(exception)

        // When & Then
        val thrownException = assertFailsWith<Exception> {
            deleteHabitUseCase(habitId)
        }
        assertThat(thrownException.message).isEqualTo("数据库错误")
    }

    @Test
    fun `invoke should throw exception when habit has check-in records`() = runTest {
        // Given
        val habitId = "habit123"
        val exception = DomainException.BusinessException("无法删除有打卡记录的习惯")
        
        coEvery { 
            habitRepository.deleteHabit(habitId) 
        } returns BaseResult.Error(exception)

        // When & Then
        val thrownException = assertFailsWith<DomainException.BusinessException> {
            deleteHabitUseCase(habitId)
        }
        assertThat(thrownException.message).isEqualTo("无法删除有打卡记录的习惯")
    }
}
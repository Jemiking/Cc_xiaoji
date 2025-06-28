package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.model.TaskPriority
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class ToggleTodoCompletionUseCaseTest {
    
    private lateinit var todoRepository: TodoRepository
    private lateinit var useCase: ToggleTodoCompletionUseCase
    
    @Before
    fun setup() {
        todoRepository = mockk()
        useCase = ToggleTodoCompletionUseCase(todoRepository)
    }
    
    @Test
    fun `切换待办事项完成状态 - 从未完成到完成`() = runTest {
        // Given
        val taskId = 1L
        val isCompleted = true
        coEvery { todoRepository.updateTaskCompletion(taskId, isCompleted) } returns BaseResult.Success(Unit)
        
        // When
        val result = useCase(taskId, isCompleted)
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        coVerify(exactly = 1) { todoRepository.updateTaskCompletion(taskId, isCompleted) }
    }
    
    @Test
    fun `切换待办事项完成状态 - 从完成到未完成`() = runTest {
        // Given
        val taskId = 1L
        val isCompleted = false
        coEvery { todoRepository.updateTaskCompletion(taskId, isCompleted) } returns BaseResult.Success(Unit)
        
        // When
        val result = useCase(taskId, isCompleted)
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        coVerify(exactly = 1) { todoRepository.updateTaskCompletion(taskId, isCompleted) }
    }
    
    @Test
    fun `切换待办事项完成状态失败 - 任务不存在`() = runTest {
        // Given
        val taskId = 999L
        val isCompleted = true
        val errorMessage = "任务不存在"
        coEvery { todoRepository.updateTaskCompletion(taskId, isCompleted) } returns BaseResult.Error(Exception(errorMessage))
        
        // When
        val result = useCase(taskId, isCompleted)
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Error::class.java)
        val errorResult = result as BaseResult.Error
        assertThat(errorResult.exception.message).isEqualTo(errorMessage)
        coVerify(exactly = 1) { todoRepository.updateTaskCompletion(taskId, isCompleted) }
    }
    
    @Test
    fun `批量切换待办事项完成状态`() = runTest {
        // Given
        val taskStatuses = mapOf(
            1L to true,
            2L to false,
            3L to true
        )
        
        taskStatuses.forEach { (id, status) ->
            coEvery { todoRepository.updateTaskCompletion(id, status) } returns BaseResult.Success(Unit)
        }
        
        // When
        val results = taskStatuses.map { (id, status) -> 
            useCase(id, status)
        }
        
        // Then
        results.forEach { result ->
            assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        }
        taskStatuses.forEach { (id, status) ->
            coVerify(exactly = 1) { todoRepository.updateTaskCompletion(id, status) }
        }
    }
    
    @Test
    fun `切换待办事项完成状态 - 验证参数传递`() = runTest {
        // Given
        val testCases = listOf(
            1L to true,
            2L to false,
            100L to true,
            999L to false
        )
        
        testCases.forEach { (id, status) ->
            coEvery { todoRepository.updateTaskCompletion(id, status) } returns BaseResult.Success(Unit)
        }
        
        // When & Then
        testCases.forEach { (id, status) ->
            val result = useCase(id, status)
            assertThat(result).isInstanceOf(BaseResult.Success::class.java)
            coVerify(exactly = 1) { todoRepository.updateTaskCompletion(id, status) }
        }
    }
}
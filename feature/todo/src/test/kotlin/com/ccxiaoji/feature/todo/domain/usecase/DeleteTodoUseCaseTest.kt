package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteTodoUseCaseTest {
    
    private lateinit var todoRepository: TodoRepository
    private lateinit var useCase: DeleteTodoUseCase
    
    @Before
    fun setup() {
        todoRepository = mockk()
        useCase = DeleteTodoUseCase(todoRepository)
    }
    
    @Test
    fun `删除待办事项成功`() = runTest {
        // Given
        val taskId = 1L
        coEvery { todoRepository.deleteTask(taskId) } returns BaseResult.Success(Unit)
        
        // When
        val result = useCase(taskId)
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        coVerify(exactly = 1) { todoRepository.deleteTask(taskId) }
    }
    
    @Test
    fun `删除待办事项失败 - 任务不存在`() = runTest {
        // Given
        val taskId = 999L
        val errorMessage = "任务不存在"
        coEvery { todoRepository.deleteTask(taskId) } returns BaseResult.Error(Exception(errorMessage))
        
        // When
        val result = useCase(taskId)
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Error::class.java)
        val errorResult = result as BaseResult.Error
        assertThat(errorResult.exception.message).isEqualTo(errorMessage)
        coVerify(exactly = 1) { todoRepository.deleteTask(taskId) }
    }
    
    @Test
    fun `删除待办事项失败 - 数据库错误`() = runTest {
        // Given
        val taskId = 1L
        val errorMessage = "数据库连接失败"
        coEvery { todoRepository.deleteTask(taskId) } returns BaseResult.Error(Exception(errorMessage))
        
        // When
        val result = useCase(taskId)
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Error::class.java)
        val errorResult = result as BaseResult.Error
        assertThat(errorResult.exception.message).isEqualTo(errorMessage)
        coVerify(exactly = 1) { todoRepository.deleteTask(taskId) }
    }
    
    @Test
    fun `删除多个待办事项`() = runTest {
        // Given
        val taskIds = listOf(1L, 2L, 3L)
        taskIds.forEach { id ->
            coEvery { todoRepository.deleteTask(id) } returns BaseResult.Success(Unit)
        }
        
        // When
        val results = taskIds.map { id -> useCase(id) }
        
        // Then
        results.forEach { result ->
            assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        }
        taskIds.forEach { id ->
            coVerify(exactly = 1) { todoRepository.deleteTask(id) }
        }
    }
}
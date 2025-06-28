package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.model.TaskPriority
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class GetAllTodosUseCaseTest {
    
    private lateinit var todoRepository: TodoRepository
    private lateinit var useCase: GetAllTodosUseCase
    
    @Before
    fun setup() {
        todoRepository = mockk()
        useCase = GetAllTodosUseCase(todoRepository)
    }
    
    @Test
    fun `获取所有待办事项成功`() = runTest {
        // Given
        val tasks = listOf(
            Task(
                id = 1,
                title = "任务1",
                description = "描述1",
                completed = false,
                priority = TaskPriority.HIGH,
                dueAt = LocalDateTime.now().plusDays(1),
                createdAt = LocalDateTime.now()
            ),
            Task(
                id = 2,
                title = "任务2",
                description = "描述2",
                completed = true,
                priority = TaskPriority.MEDIUM,
                dueAt = null,
                createdAt = LocalDateTime.now()
            )
        )
        coEvery { todoRepository.getAllTasks() } returns flowOf(BaseResult.Success(tasks))
        
        // When
        val result = useCase().first()
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        val successResult = result as BaseResult.Success
        assertThat(successResult.data).hasSize(2)
        assertThat(successResult.data[0].title).isEqualTo("任务1")
        assertThat(successResult.data[1].title).isEqualTo("任务2")
        coVerify(exactly = 1) { todoRepository.getAllTasks() }
    }
    
    @Test
    fun `获取所有待办事项 - 空列表`() = runTest {
        // Given
        val emptyList = emptyList<Task>()
        coEvery { todoRepository.getAllTasks() } returns flowOf(BaseResult.Success(emptyList))
        
        // When
        val result = useCase().first()
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        val successResult = result as BaseResult.Success
        assertThat(successResult.data).isEmpty()
        coVerify(exactly = 1) { todoRepository.getAllTasks() }
    }
    
    @Test
    fun `获取所有待办事项失败`() = runTest {
        // Given
        val errorMessage = "数据库错误"
        coEvery { todoRepository.getAllTasks() } returns flowOf(BaseResult.Error(Exception(errorMessage)))
        
        // When
        val result = useCase().first()
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Error::class.java)
        val errorResult = result as BaseResult.Error
        assertThat(errorResult.exception.message).isEqualTo(errorMessage)
        coVerify(exactly = 1) { todoRepository.getAllTasks() }
    }
    
    @Test
    fun `获取所有待办事项 - 按创建时间排序`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val tasks = listOf(
            Task(
                id = 1,
                title = "新任务",
                description = "",
                completed = false,
                priority = TaskPriority.LOW,
                dueAt = null,
                createdAt = now
            ),
            Task(
                id = 2,
                title = "旧任务",
                description = "",
                completed = false,
                priority = TaskPriority.LOW,
                dueAt = null,
                createdAt = now.minusDays(1)
            )
        )
        // 假设repository返回的是按创建时间排序的结果
        coEvery { todoRepository.getAllTasks() } returns flowOf(BaseResult.Success(tasks))
        
        // When
        val result = useCase().first()
        
        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        val successResult = result as BaseResult.Success
        assertThat(successResult.data).hasSize(2)
        assertThat(successResult.data[0].title).isEqualTo("新任务")
        assertThat(successResult.data[1].title).isEqualTo("旧任务")
    }
}
package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class AddTodoUseCaseTest {

    @MockK
    private lateinit var todoRepository: TodoRepository
    
    private lateinit var addTodoUseCase: AddTodoUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        addTodoUseCase = AddTodoUseCase(todoRepository)
    }

    @Test
    fun `invoke should add todo successfully with all fields`() = runTest {
        // Given
        val title = "完成项目报告"
        val description = "编写季度项目总结报告"
        val dueAt = Clock.System.now()
        val priority = 2
        
        val expectedTask = Task(
            id = "task123",
            title = title,
            description = description,
            dueAt = dueAt,
            priority = priority,
            completed = false,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        coEvery { 
            todoRepository.addTodo(title, description, dueAt, priority) 
        } returns BaseResult.Success(expectedTask)

        // When
        val result = addTodoUseCase(title, description, dueAt, priority)

        // Then
        assertThat(result).isEqualTo(expectedTask)
        assertThat(result.title).isEqualTo(title)
        assertThat(result.priority).isEqualTo(priority)
        coVerify(exactly = 1) { 
            todoRepository.addTodo(title, description, dueAt, priority)
        }
    }

    @Test
    fun `invoke should add todo with minimal fields`() = runTest {
        // Given
        val title = "买牛奶"
        val description: String? = null
        val dueAt: Instant? = null
        val priority = 0
        
        val expectedTask = Task(
            id = "task123",
            title = title,
            description = null,
            dueAt = null,
            priority = priority,
            completed = false,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        coEvery { 
            todoRepository.addTodo(title, description, dueAt, priority) 
        } returns BaseResult.Success(expectedTask)

        // When
        val result = addTodoUseCase(title, description, dueAt, priority)

        // Then
        assertThat(result).isEqualTo(expectedTask)
        assertThat(result.description).isNull()
        assertThat(result.dueAt).isNull()
        coVerify(exactly = 1) { 
            todoRepository.addTodo(title, description, dueAt, priority)
        }
    }

    @Test
    fun `invoke should throw exception when title is blank`() = runTest {
        // Given
        val title = "   "
        val description = "描述"
        val dueAt = Clock.System.now()
        val priority = 1

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            addTodoUseCase(title, description, dueAt, priority)
        }
        coVerify(exactly = 0) { 
            todoRepository.addTodo(any(), any(), any(), any()) 
        }
    }

    @Test
    fun `invoke should throw exception when priority is negative`() = runTest {
        // Given
        val title = "任务"
        val description = "描述"
        val dueAt = Clock.System.now()
        val priority = -1

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            addTodoUseCase(title, description, dueAt, priority)
        }
    }

    @Test
    fun `invoke should throw exception when priority is too high`() = runTest {
        // Given
        val title = "任务"
        val description = "描述"
        val dueAt = Clock.System.now()
        val priority = 4

        // When & Then
        assertFailsWith<DomainException.ValidationException> {
            addTodoUseCase(title, description, dueAt, priority)
        }
    }

    @Test
    fun `invoke should throw exception when repository fails`() = runTest {
        // Given
        val title = "任务"
        val description = "描述"
        val dueAt = Clock.System.now()
        val priority = 1
        val exception = Exception("数据库错误")
        
        coEvery { 
            todoRepository.addTodo(title, description, dueAt, priority) 
        } returns BaseResult.Error(exception)

        // When & Then
        val thrownException = assertFailsWith<Exception> {
            addTodoUseCase(title, description, dueAt, priority)
        }
        assertThat(thrownException.message).isEqualTo("数据库错误")
    }
}
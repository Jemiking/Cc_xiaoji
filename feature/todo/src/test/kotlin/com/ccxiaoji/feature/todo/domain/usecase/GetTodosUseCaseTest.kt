package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class GetTodosUseCaseTest {

    @MockK
    private lateinit var todoRepository: TodoRepository

    private lateinit var getTodosUseCase: GetTodosUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getTodosUseCase = GetTodosUseCase(todoRepository)
    }

    @Test
    fun `execute returns todos from repository`() = runTest {
        // Given
        val now = Clock.System.now()
        val expectedTodos = listOf(
            Task(
                id = "1",
                title = "测试任务1",
                description = "这是一个测试任务",
                dueAt = now.plus(Instant.fromEpochMilliseconds(86400000)),
                priority = 1,
                completed = false,
                completedAt = null,
                createdAt = now,
                updatedAt = now
            ),
            Task(
                id = "2",
                title = "测试任务2",
                description = null,
                dueAt = null,
                priority = 0,
                completed = true,
                completedAt = now,
                createdAt = now.minus(Instant.fromEpochMilliseconds(172800000)),
                updatedAt = now
            )
        )
        coEvery { todoRepository.getAllTodos() } returns flowOf(expectedTodos)

        // When
        val result = getTodosUseCase().toList()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(expectedTodos)
        assertThat(result[0]).hasSize(2)
        assertThat(result[0][0].title).isEqualTo("测试任务1")
        assertThat(result[0][1].completed).isTrue()
        coVerify(exactly = 1) { todoRepository.getAllTodos() }
    }

    @Test
    fun `execute returns empty list when repository is empty`() = runTest {
        // Given
        coEvery { todoRepository.getAllTodos() } returns flowOf(emptyList())

        // When
        val result = getTodosUseCase().toList()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEmpty()
        coVerify(exactly = 1) { todoRepository.getAllTodos() }
    }
    
    @Test
    fun `getTodayTodos returns today tasks from repository`() = runTest {
        // Given
        val now = Clock.System.now()
        val todayTask = Task(
            id = "3",
            title = "今日任务",
            description = "今天要完成的任务",
            dueAt = now,
            priority = 2,
            completed = false,
            completedAt = null,
            createdAt = now,
            updatedAt = now
        )
        coEvery { todoRepository.getTodayTodos() } returns flowOf(listOf(todayTask))

        // When
        val result = getTodosUseCase.getTodayTodos().toList()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0]).hasSize(1)
        assertThat(result[0][0].title).isEqualTo("今日任务")
        assertThat(result[0][0].priority).isEqualTo(2)
        coVerify(exactly = 1) { todoRepository.getTodayTodos() }
    }
    
    @Test
    fun `getIncompleteTodos returns incomplete tasks from repository`() = runTest {
        // Given
        val now = Clock.System.now()
        val incompleteTasks = listOf(
            Task(
                id = "4",
                title = "未完成任务",
                description = null,
                dueAt = null,
                priority = 1,
                completed = false,
                completedAt = null,
                createdAt = now,
                updatedAt = now
            )
        )
        coEvery { todoRepository.getIncompleteTodos() } returns flowOf(incompleteTasks)

        // When
        val result = getTodosUseCase.getIncompleteTodos().toList()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0]).hasSize(1)
        assertThat(result[0][0].completed).isFalse()
        coVerify(exactly = 1) { todoRepository.getIncompleteTodos() }
    }
    
    @Test
    fun `searchTodos returns matching tasks from repository`() = runTest {
        // Given
        val query = "测试"
        val now = Clock.System.now()
        val matchingTasks = listOf(
            Task(
                id = "5",
                title = "测试搜索功能",
                description = "包含测试关键词的任务",
                dueAt = null,
                priority = 0,
                completed = false,
                completedAt = null,
                createdAt = now,
                updatedAt = now
            )
        )
        coEvery { todoRepository.searchTodos(query) } returns flowOf(matchingTasks)

        // When
        val result = getTodosUseCase.searchTodos(query).toList()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0]).hasSize(1)
        assertThat(result[0][0].title).contains(query)
        coVerify(exactly = 1) { todoRepository.searchTodos(query) }
    }
}
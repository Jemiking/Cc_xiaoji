package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
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
        val expectedTodos = listOf(
            // 这里应该创建测试用的Todo对象
        )
        coEvery { todoRepository.getAllTodos() } returns flowOf(expectedTodos)

        // When
        val result = getTodosUseCase().toList()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(expectedTodos)
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
}
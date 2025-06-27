package com.ccxiaoji.feature.todo.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.core.database.dao.TaskDao
import com.ccxiaoji.core.database.entity.TaskEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class TodoRepositoryImplTest {

    @MockK
    private lateinit var taskDao: TaskDao
    
    private lateinit var todoRepository: TodoRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        todoRepository = TodoRepositoryImpl(taskDao)
    }

    @Test
    fun `getAllTodos should return all tasks from dao`() = runTest {
        // Given
        val taskEntities = listOf(
            mockk<TaskEntity>(),
            mockk<TaskEntity>()
        )
        coEvery { taskDao.getAllTasks() } returns flowOf(taskEntities)

        // When
        val result = todoRepository.getAllTodos().first()

        // Then
        assertThat(result).hasSize(2)
        coVerify(exactly = 1) { taskDao.getAllTasks() }
    }

    @Test
    fun `getIncompleteTodos should return incomplete tasks`() = runTest {
        // Given
        val taskEntities = listOf(
            mockk<TaskEntity>()
        )
        coEvery { taskDao.getIncompleteTasks() } returns flowOf(taskEntities)

        // When
        val result = todoRepository.getIncompleteTodos().first()

        // Then
        assertThat(result).hasSize(1)
        coVerify(exactly = 1) { taskDao.getIncompleteTasks() }
    }

    @Test
    fun `searchTodos should return search results`() = runTest {
        // Given
        val query = "测试"
        val taskEntities = listOf(
            mockk<TaskEntity>(),
            mockk<TaskEntity>()
        )
        coEvery { taskDao.searchTasks("%$query%") } returns flowOf(taskEntities)

        // When
        val result = todoRepository.searchTodos(query).first()

        // Then
        assertThat(result).hasSize(2)
        coVerify(exactly = 1) { taskDao.searchTasks("%$query%") }
    }

    @Test
    fun `addTodo should insert task and return success`() = runTest {
        // Given
        val title = "买牛奶"
        val description = "去超市买牛奶"
        val dueAt = Clock.System.now()
        val priority = 1
        
        coEvery { taskDao.insertTask(any()) } returns 1L

        // When
        val result = todoRepository.addTodo(title, description, dueAt, priority)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        val task = (result as BaseResult.Success).data
        assertThat(task.title).isEqualTo(title)
        assertThat(task.description).isEqualTo(description)
        assertThat(task.priority).isEqualTo(priority)
        coVerify(exactly = 1) { taskDao.insertTask(any()) }
    }

    @Test
    fun `addTodo should return error when dao fails`() = runTest {
        // Given
        val title = "买牛奶"
        val exception = Exception("数据库错误")
        
        coEvery { taskDao.insertTask(any()) } throws exception

        // When
        val result = todoRepository.addTodo(title)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Error::class.java)
        val error = (result as BaseResult.Error).exception
        assertThat(error.message).contains("数据库错误")
    }

    @Test
    fun `updateTodoCompletion should update task completion`() = runTest {
        // Given
        val todoId = "task123"
        val completed = true
        
        coEvery { taskDao.updateTaskCompletion(todoId, completed) } returns Unit

        // When
        val result = todoRepository.updateTodoCompletion(todoId, completed)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        coVerify(exactly = 1) { taskDao.updateTaskCompletion(todoId, completed) }
    }

    @Test
    fun `deleteTodo should delete task from dao`() = runTest {
        // Given
        val todoId = "task123"
        
        coEvery { taskDao.deleteTask(todoId) } returns Unit

        // When
        val result = todoRepository.deleteTodo(todoId)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        coVerify(exactly = 1) { taskDao.deleteTask(todoId) }
    }

    @Test
    fun `getTodayTodos should return today's tasks`() = runTest {
        // Given
        val taskEntities = listOf(
            mockk<TaskEntity>()
        )
        coEvery { taskDao.getTodayTasks(any(), any()) } returns flowOf(taskEntities)

        // When
        val result = todoRepository.getTodayTodos().first()

        // Then
        assertThat(result).hasSize(1)
        coVerify(exactly = 1) { taskDao.getTodayTasks(any(), any()) }
    }

    @Test
    fun `getTodoById should return task when exists`() = runTest {
        // Given
        val todoId = "task123"
        val taskEntity = mockk<TaskEntity>()
        
        coEvery { taskDao.getTaskById(todoId) } returns taskEntity

        // When
        val result = todoRepository.getTodoById(todoId)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        val task = (result as BaseResult.Success).data
        assertThat(task).isNotNull()
        coVerify(exactly = 1) { taskDao.getTaskById(todoId) }
    }

    @Test
    fun `getTodoById should return null when not exists`() = runTest {
        // Given
        val todoId = "nonexistent"
        
        coEvery { taskDao.getTaskById(todoId) } returns null

        // When
        val result = todoRepository.getTodoById(todoId)

        // Then
        assertThat(result).isInstanceOf(BaseResult.Success::class.java)
        val task = (result as BaseResult.Success).data
        assertThat(task).isNull()
        coVerify(exactly = 1) { taskDao.getTaskById(todoId) }
    }
}
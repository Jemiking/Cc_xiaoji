package com.ccxiaoji.feature.todo.data.repository

import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.todo.data.local.dao.TaskDao
import com.ccxiaoji.feature.todo.data.local.entity.TaskEntity
import com.ccxiaoji.shared.user.api.UserApi
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class TaskRepositoryTest {

    @MockK
    private lateinit var taskDao: TaskDao

    @MockK
    private lateinit var userApi: UserApi

    private lateinit var taskRepository: TaskRepository

    private val testUserId = "test-user-123"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { userApi.getCurrentUserId() } returns testUserId
        taskRepository = TaskRepository(taskDao, userApi)
    }

    @Test
    fun `获取用户的所有任务`() = runTest {
        // Given
        val taskEntities = createTestTaskEntities()
        coEvery { taskDao.getTasksByUser(testUserId) } returns flowOf(taskEntities)

        // When
        val result = taskRepository.getTasks().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].title).isEqualTo("任务1")
        assertThat(result[1].title).isEqualTo("任务2")
        coVerify(exactly = 1) { taskDao.getTasksByUser(testUserId) }
    }

    @Test
    fun `获取未完成的任务`() = runTest {
        // Given
        val incompleteTaskEntities = listOf(
            createTestTaskEntity("1", "未完成任务1", completed = false),
            createTestTaskEntity("2", "未完成任务2", completed = false)
        )
        coEvery { taskDao.getIncompleteTasks(testUserId) } returns flowOf(incompleteTaskEntities)

        // When
        val result = taskRepository.getIncompleteTasks().first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.all { !it.completed }).isTrue()
        coVerify(exactly = 1) { taskDao.getIncompleteTasks(testUserId) }
    }

    @Test
    fun `搜索任务`() = runTest {
        // Given
        val query = "重要"
        val searchResults = listOf(
            createTestTaskEntity("1", "重要任务1"),
            createTestTaskEntity("2", "重要任务2")
        )
        coEvery { taskDao.searchTasks(testUserId, "%$query%") } returns flowOf(searchResults)

        // When
        val result = taskRepository.searchTasks(query).first()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.all { it.title.contains(query) }).isTrue()
        coVerify(exactly = 1) { taskDao.searchTasks(testUserId, "%$query%") }
    }

    @Test
    fun `获取今日任务`() = runTest {
        // Given
        val todayTasks = listOf(
            createTestTaskEntity("1", "今日任务1"),
            createTestTaskEntity("2", "今日任务2")
        )
        coEvery { taskDao.getTasksByDateRange(any(), any(), any()) } returns flowOf(todayTasks)

        // When
        val result = taskRepository.getTodayTasks().first()

        // Then
        assertThat(result).hasSize(2)
        coVerify(exactly = 1) { taskDao.getTasksByDateRange(testUserId, any(), any()) }
    }

    @Test
    fun `获取今日任务数量`() = runTest {
        // Given
        val todayTasks = listOf(
            createTestTaskEntity("1", "今日任务1"),
            createTestTaskEntity("2", "今日任务2"),
            createTestTaskEntity("3", "今日任务3")
        )
        coEvery { taskDao.getTasksByDateRange(any(), any(), any()) } returns flowOf(todayTasks)

        // When
        val result = taskRepository.getTodayTasksCount().first()

        // Then
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `添加新任务`() = runTest {
        // Given
        val title = "新任务"
        val description = "任务描述"
        val dueAt = Clock.System.now()
        val priority = 1
        
        val taskEntitySlot = slot<TaskEntity>()
        coEvery { taskDao.insertTask(capture(taskEntitySlot)) } returns Unit

        // When
        val result = taskRepository.addTask(title, description, dueAt, priority)

        // Then
        assertThat(result.title).isEqualTo(title)
        assertThat(result.description).isEqualTo(description)
        assertThat(result.priority).isEqualTo(priority)
        
        val capturedEntity = taskEntitySlot.captured
        assertThat(capturedEntity.userId).isEqualTo(testUserId)
        assertThat(capturedEntity.syncStatus).isEqualTo(SyncStatus.PENDING_SYNC)
        coVerify(exactly = 1) { taskDao.insertTask(any()) }
    }

    @Test
    fun `更新任务完成状态`() = runTest {
        // Given
        val taskId = "task-123"
        val completed = true
        coEvery { taskDao.updateTaskCompletion(any(), any(), any(), any()) } returns Unit

        // When
        taskRepository.updateTaskCompletion(taskId, completed)

        // Then
        coVerify(exactly = 1) { 
            taskDao.updateTaskCompletion(
                taskId, 
                completed, 
                any(), // completedAt
                any()  // updatedAt
            ) 
        }
    }

    @Test
    fun `更新任务信息`() = runTest {
        // Given
        val taskId = "task-123"
        val existingTask = createTestTaskEntity(taskId, "原标题")
        val newTitle = "新标题"
        val newDescription = "新描述"
        
        coEvery { taskDao.getTaskById(taskId) } returns existingTask
        coEvery { taskDao.updateTask(any()) } returns Unit

        // When
        taskRepository.updateTask(taskId, newTitle, newDescription)

        // Then
        coVerify(exactly = 1) { taskDao.getTaskById(taskId) }
        coVerify(exactly = 1) { taskDao.updateTask(any()) }
    }

    @Test
    fun `软删除任务`() = runTest {
        // Given
        val taskId = "task-123"
        coEvery { taskDao.softDeleteTask(any(), any()) } returns Unit

        // When
        taskRepository.deleteTask(taskId)

        // Then
        coVerify(exactly = 1) { taskDao.softDeleteTask(taskId, any()) }
    }

    @Test
    fun `根据ID获取任务`() = runTest {
        // Given
        val taskId = "task-123"
        val taskEntity = createTestTaskEntity(taskId, "测试任务")
        coEvery { taskDao.getTaskById(taskId) } returns taskEntity

        // When
        val result = taskRepository.getTaskById(taskId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(taskId)
        assertThat(result?.title).isEqualTo("测试任务")
        coVerify(exactly = 1) { taskDao.getTaskById(taskId) }
    }

    @Test
    fun `根据ID获取不存在的任务返回null`() = runTest {
        // Given
        val taskId = "non-existent"
        coEvery { taskDao.getTaskById(taskId) } returns null

        // When
        val result = taskRepository.getTaskById(taskId)

        // Then
        assertThat(result).isNull()
        coVerify(exactly = 1) { taskDao.getTaskById(taskId) }
    }

    private fun createTestTaskEntities(): List<TaskEntity> {
        return listOf(
            createTestTaskEntity("1", "任务1"),
            createTestTaskEntity("2", "任务2")
        )
    }

    private fun createTestTaskEntity(
        id: String,
        title: String,
        completed: Boolean = false
    ): TaskEntity {
        val now = System.currentTimeMillis()
        return TaskEntity(
            id = id,
            userId = testUserId,
            title = title,
            description = null,
            dueAt = null,
            priority = 0,
            completed = completed,
            completedAt = if (completed) now else null,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.SYNCED,
            isDeleted = false
        )
    }
}
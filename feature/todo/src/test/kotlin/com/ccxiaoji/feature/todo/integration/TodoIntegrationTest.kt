package com.ccxiaoji.feature.todo.integration

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.core.database.dao.TaskDao
import com.ccxiaoji.core.database.entity.TaskEntity
import com.ccxiaoji.feature.todo.data.repository.TodoRepositoryImpl
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.usecase.*
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Todo模块集成测试
 * 测试Repository和UseCase的协同工作
 */
class TodoIntegrationTest {

    @MockK
    private lateinit var taskDao: TaskDao
    
    private lateinit var todoRepository: TodoRepositoryImpl
    
    private lateinit var addTodoUseCase: AddTodoUseCase
    private lateinit var updateTodoUseCase: UpdateTodoUseCase
    private lateinit var deleteTodoUseCase: DeleteTodoUseCase
    private lateinit var toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase
    private lateinit var getAllTodosUseCase: GetAllTodosUseCase
    private lateinit var getTodayTodosUseCase: GetTodayTodosUseCase
    private lateinit var searchTodosUseCase: SearchTodosUseCase
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // 初始化Repository
        todoRepository = TodoRepositoryImpl(taskDao)
        
        // 初始化UseCases
        addTodoUseCase = AddTodoUseCase(todoRepository)
        updateTodoUseCase = UpdateTodoUseCase(todoRepository)
        deleteTodoUseCase = DeleteTodoUseCase(todoRepository)
        toggleTodoCompletionUseCase = ToggleTodoCompletionUseCase(todoRepository)
        getAllTodosUseCase = GetAllTodosUseCase(todoRepository)
        getTodayTodosUseCase = GetTodayTodosUseCase(todoRepository)
        searchTodosUseCase = SearchTodosUseCase(todoRepository)
    }
    
    @Test
    fun `完整的任务生命周期测试`() = runTest {
        // Given - 准备测试数据
        val taskId = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val taskEntity = TaskEntity(
            id = taskId,
            userId = 1L,
            title = "完成项目报告",
            description = "编写季度项目总结报告",
            dueAt = now.plus(1, DateTimeUnit.DAY),
            priority = 2,
            completed = false,
            createdAt = now,
            updatedAt = now
        )
        
        // Mock DAO行为
        coEvery { taskDao.insertTask(any()) } returns 1L
        coEvery { taskDao.getAllTasks() } returns flowOf(listOf(taskEntity))
        coEvery { taskDao.updateTaskCompletion(taskId, true) } returns Unit
        coEvery { taskDao.deleteTask(taskId) } returns Unit
        
        // When - 添加任务
        val addedTask = addTodoUseCase(
            title = "完成项目报告",
            description = "编写季度项目总结报告",
            dueAt = now.plus(1, DateTimeUnit.DAY),
            priority = 2
        )
        
        // Then - 验证添加成功
        assertThat(addedTask.title).isEqualTo("完成项目报告")
        assertThat(addedTask.priority).isEqualTo(2)
        assertThat(addedTask.completed).isFalse()
        
        // When - 查询所有任务
        val allTasks = getAllTodosUseCase().first()
        
        // Then - 验证查询结果
        assertThat(allTasks).hasSize(1)
        assertThat(allTasks.first().id).isEqualTo(taskId)
        
        // When - 标记任务完成
        toggleTodoCompletionUseCase(taskId, true)
        
        // Then - 验证更新调用
        coVerify(exactly = 1) { taskDao.updateTaskCompletion(taskId, true) }
        
        // When - 删除任务
        deleteTodoUseCase(taskId)
        
        // Then - 验证删除调用
        coVerify(exactly = 1) { taskDao.deleteTask(taskId) }
    }
    
    @Test
    fun `搜索功能集成测试`() = runTest {
        // Given - 多个任务
        val tasks = listOf(
            createTaskEntity("1", "买牛奶", "去超市买牛奶"),
            createTaskEntity("2", "写代码", "完成新功能开发"),
            createTaskEntity("3", "买面包", "去面包店买面包")
        )
        
        coEvery { taskDao.searchTasks("%买%") } returns flowOf(
            tasks.filter { it.title.contains("买") }
        )
        
        // When - 搜索包含"买"的任务
        val searchResults = searchTodosUseCase("买").first()
        
        // Then
        assertThat(searchResults).hasSize(2)
        assertThat(searchResults.map { it.title }).containsExactly("买牛奶", "买面包")
    }
    
    @Test
    fun `今日任务过滤集成测试`() = runTest {
        // Given - 不同日期的任务
        val now = Clock.System.now()
        val tasks = listOf(
            createTaskEntity("1", "今天的任务", dueAt = now),
            createTaskEntity("2", "明天的任务", dueAt = now.plus(1, DateTimeUnit.DAY)),
            createTaskEntity("3", "昨天的任务", dueAt = now.plus(-1, DateTimeUnit.DAY))
        )
        
        coEvery { taskDao.getTodayTasks(any(), any()) } returns flowOf(
            listOf(tasks[0]) // 只返回今天的任务
        )
        
        // When - 获取今日任务
        val todayTasks = getTodayTodosUseCase().first()
        
        // Then
        assertThat(todayTasks).hasSize(1)
        assertThat(todayTasks.first().title).isEqualTo("今天的任务")
    }
    
    @Test
    fun `优先级验证集成测试`() = runTest {
        // Given - 不同优先级的任务
        val tasks = listOf(
            createTaskEntity("1", "低优先级", priority = 0),
            createTaskEntity("2", "中优先级", priority = 1),
            createTaskEntity("3", "高优先级", priority = 2),
            createTaskEntity("4", "紧急", priority = 3)
        )
        
        coEvery { taskDao.getAllTasks() } returns flowOf(tasks)
        
        // When - 获取所有任务
        val allTasks = getAllTodosUseCase().first()
        
        // Then - 验证优先级范围
        assertThat(allTasks).hasSize(4)
        allTasks.forEach { task ->
            assertThat(task.priority).isAtLeast(0)
            assertThat(task.priority).isAtMost(3)
        }
    }
    
    @Test
    fun `错误处理集成测试 - 添加空标题任务`() = runTest {
        // When & Then - 尝试添加空标题任务
        try {
            addTodoUseCase(
                title = "   ",
                description = "描述",
                dueAt = Clock.System.now(),
                priority = 1
            )
            assert(false) { "应该抛出验证异常" }
        } catch (e: DomainException.ValidationException) {
            assertThat(e.message).contains("标题不能为空")
        }
    }
    
    @Test
    fun `错误处理集成测试 - 更新不存在的任务`() = runTest {
        // Given
        val nonExistentId = "non-existent"
        coEvery { taskDao.getTaskById(nonExistentId) } returns null
        coEvery { taskDao.updateTask(any()) } throws Exception("任务不存在")
        
        // When & Then
        try {
            updateTodoUseCase(
                todoId = nonExistentId,
                title = "更新的标题",
                description = "更新的描述",
                dueAt = Clock.System.now(),
                priority = 1
            )
            assert(false) { "应该抛出异常" }
        } catch (e: Exception) {
            assertThat(e.message).contains("任务不存在")
        }
    }
    
    // 辅助方法
    private fun createTaskEntity(
        id: String,
        title: String,
        description: String? = null,
        dueAt: kotlinx.datetime.Instant? = null,
        priority: Int = 0,
        completed: Boolean = false
    ): TaskEntity {
        val now = Clock.System.now()
        return TaskEntity(
            id = id,
            userId = 1L,
            title = title,
            description = description,
            dueAt = dueAt,
            priority = priority,
            completed = completed,
            createdAt = now,
            updatedAt = now
        )
    }
}
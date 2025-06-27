package com.ccxiaoji.feature.todo.presentation.viewmodel

import com.ccxiaoji.common.test.util.MainDispatcherRule
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TodoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mock依赖
    private val mockTodoRepository = mockk<TodoRepository>()
    
    // 被测试的ViewModel
    private lateinit var viewModel: TodoViewModel

    // 测试数据
    private val testTasks = listOf(
        Task(
            id = "1",
            title = "测试任务1",
            description = "描述1",
            completed = false,
            priority = 0,
            dueAt = Clock.System.now(),
            syncStatus = "synced"
        ),
        Task(
            id = "2",
            title = "重要任务2",
            description = "重要描述2",
            completed = false,
            priority = 1,
            dueAt = Clock.System.now().plus(1, DateTimeUnit.DAY),
            syncStatus = "synced"
        ),
        Task(
            id = "3",
            title = "已完成任务3",
            description = null,
            completed = true,
            priority = 2,
            dueAt = null,
            syncStatus = "synced"
        ),
        Task(
            id = "4",
            title = "过期任务4",
            description = "已过期",
            completed = false,
            priority = 0,
            dueAt = Clock.System.now().minus(2, DateTimeUnit.DAY),
            syncStatus = "synced"
        )
    )

    @Before
    fun setup() {
        // 默认mock设置
        every { mockTodoRepository.getAllTodos() } returns flowOf(testTasks)
        every { mockTodoRepository.getIncompleteTodos() } returns flowOf(testTasks.filter { !it.completed })
        
        // 初始化ViewModel
        viewModel = TodoViewModel(mockTodoRepository)
    }

    @Test
    fun `初始化时应该加载未完成的任务`() = runTest {
        // When - ViewModel在init中自动加载数据
        
        // Then
        assertThat(viewModel.uiState.value.tasks).hasSize(3) // 只有未完成的任务
        assertThat(viewModel.uiState.value.tasks.map { it.id }).containsExactly("1", "2", "4")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        verify(exactly = 1) { mockTodoRepository.getIncompleteTodos() }
    }

    @Test
    fun `搜索查询应该过滤任务列表`() = runTest {
        // When
        viewModel.updateSearchQuery("重要")
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.tasks).hasSize(1)
        assertThat(viewModel.uiState.value.tasks.first().title).contains("重要")
    }

    @Test
    fun `切换显示已完成任务时应该显示所有任务`() = runTest {
        // When
        val newFilterOptions = viewModel.uiState.value.filterOptions.copy(showCompleted = true)
        viewModel.updateFilterOptions(newFilterOptions)
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.tasks).hasSize(4) // 所有任务
        verify(exactly = 1) { mockTodoRepository.getAllTodos() }
    }

    @Test
    fun `按优先级过滤应该只显示选中优先级的任务`() = runTest {
        // When - 只显示高优先级任务
        val newFilterOptions = viewModel.uiState.value.filterOptions.copy(
            selectedPriorities = setOf(1)
        )
        viewModel.updateFilterOptions(newFilterOptions)
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.tasks).hasSize(1)
        assertThat(viewModel.uiState.value.tasks.first().priority).isEqualTo(1)
    }

    @Test
    fun `按今天过滤应该只显示今天到期的任务`() = runTest {
        // When
        val newFilterOptions = viewModel.uiState.value.filterOptions.copy(
            dateFilter = DateFilter.TODAY
        )
        viewModel.updateFilterOptions(newFilterOptions)
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.tasks).hasSize(1)
        assertThat(viewModel.uiState.value.tasks.first().id).isEqualTo("1")
    }

    @Test
    fun `按过期过滤应该只显示过期的任务`() = runTest {
        // When
        val newFilterOptions = viewModel.uiState.value.filterOptions.copy(
            dateFilter = DateFilter.OVERDUE
        )
        viewModel.updateFilterOptions(newFilterOptions)
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.tasks).hasSize(1)
        assertThat(viewModel.uiState.value.tasks.first().id).isEqualTo("4")
    }

    @Test
    fun `添加任务应该调用repository并发送事件`() = runTest {
        // Given
        val newTask = Task(
            id = "5",
            title = "新任务",
            description = "新描述",
            completed = false,
            priority = 1,
            dueAt = Clock.System.now(),
            syncStatus = "pending"
        )
        coEvery { mockTodoRepository.addTodo(any(), any(), any(), any()) } returns newTask
        
        // Collect events
        val events = mutableListOf<TaskEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.taskEvent.toList(events)
        }
        
        // When
        viewModel.addTask(
            title = "新任务",
            description = "新描述",
            dueAt = Clock.System.now(),
            priority = 1
        )
        
        // Then
        coVerify(exactly = 1) { 
            mockTodoRepository.addTodo(
                title = "新任务",
                description = "新描述",
                dueAt = any(),
                priority = 1
            )
        }
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(TaskEvent.TaskAdded::class.java)
        
        job.cancel()
    }

    @Test
    fun `更新任务应该调用repository并发送事件`() = runTest {
        // Given
        coEvery { mockTodoRepository.updateTodo(any(), any(), any(), any(), any()) } just Runs
        
        // Collect events
        val events = mutableListOf<TaskEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.taskEvent.toList(events)
        }
        
        // When
        viewModel.updateTask(
            taskId = "1",
            title = "更新的任务",
            description = "更新的描述",
            dueAt = Clock.System.now(),
            priority = 2
        )
        
        // Then
        coVerify(exactly = 1) { 
            mockTodoRepository.updateTodo(
                todoId = "1",
                title = "更新的任务",
                description = "更新的描述",
                dueAt = any(),
                priority = 2
            )
        }
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(TaskEvent.TaskUpdated::class.java)
        
        job.cancel()
    }

    @Test
    fun `切换任务完成状态应该调用repository`() = runTest {
        // Given
        coEvery { mockTodoRepository.updateTodoCompletion(any(), any()) } just Runs
        
        // When
        viewModel.toggleTaskCompletion("1", true)
        
        // Then
        coVerify(exactly = 1) { 
            mockTodoRepository.updateTodoCompletion("1", true)
        }
    }

    @Test
    fun `删除任务应该调用repository并发送事件`() = runTest {
        // Given
        coEvery { mockTodoRepository.deleteTodo(any()) } just Runs
        
        // Collect events
        val events = mutableListOf<TaskEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.taskEvent.toList(events)
        }
        
        // When
        viewModel.deleteTask("1")
        
        // Then
        coVerify(exactly = 1) { 
            mockTodoRepository.deleteTodo("1")
        }
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(TaskEvent.TaskDeleted::class.java)
        assertThat((events.first() as TaskEvent.TaskDeleted).taskId).isEqualTo("1")
        
        job.cancel()
    }

    // 帮助属性，用于等待状态更新
    private val testScheduler = UnconfinedTestDispatcher()
    
    @Test
    fun `排序选项改变应该重新排序任务`() = runTest {
        // When - 按优先级排序
        viewModel.updateSortOption(TaskSortOption.PRIORITY)
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        val tasks = viewModel.uiState.value.tasks
        assertThat(tasks).hasSize(3)
        // 验证按优先级降序排列
        assertThat(tasks[0].priority).isGreaterThan(tasks[1].priority)
        assertThat(tasks[1].priority).isGreaterThan(tasks[2].priority)
    }
    
    @Test
    fun `按截止日期排序应该正确排序任务`() = runTest {
        // When
        viewModel.updateSortOption(TaskSortOption.DUE_DATE)
        
        // Wait for state update
        testScheduler.advanceUntilIdle()
        
        // Then
        val tasks = viewModel.uiState.value.tasks
        assertThat(tasks).hasSize(3)
        // 验证过期任务在前，然后是今天，然后是明天
        assertThat(tasks[0].id).isEqualTo("4") // 过期
        assertThat(tasks[1].id).isEqualTo("1") // 今天
        assertThat(tasks[2].id).isEqualTo("2") // 明天
    }
    
    @Test
    fun `错误处理 - 添加任务失败应该发送错误事件`() = runTest {
        // Given
        val exception = Exception("添加失败")
        coEvery { mockTodoRepository.addTodo(any(), any(), any(), any()) } throws exception
        
        // Collect events
        val events = mutableListOf<TaskEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.taskEvent.toList(events)
        }
        
        // When
        viewModel.addTask(
            title = "新任务",
            description = "新描述",
            dueAt = Clock.System.now(),
            priority = 1
        )
        
        // Then
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(TaskEvent.Error::class.java)
        assertThat((events.first() as TaskEvent.Error).message).contains("添加失败")
        
        job.cancel()
    }
    
    @Test
    fun `错误处理 - 更新任务失败应该发送错误事件`() = runTest {
        // Given
        val exception = Exception("更新失败")
        coEvery { mockTodoRepository.updateTodo(any(), any(), any(), any(), any()) } throws exception
        
        // Collect events
        val events = mutableListOf<TaskEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.taskEvent.toList(events)
        }
        
        // When
        viewModel.updateTask(
            taskId = "1",
            title = "更新的任务",
            description = "更新的描述",
            dueAt = Clock.System.now(),
            priority = 2
        )
        
        // Then
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(TaskEvent.Error::class.java)
        assertThat((events.first() as TaskEvent.Error).message).contains("更新失败")
        
        job.cancel()
    }
    
    @Test
    fun `错误处理 - 删除任务失败应该发送错误事件`() = runTest {
        // Given
        val exception = Exception("删除失败")
        coEvery { mockTodoRepository.deleteTodo(any()) } throws exception
        
        // Collect events
        val events = mutableListOf<TaskEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.taskEvent.toList(events)
        }
        
        // When
        viewModel.deleteTask("1")
        
        // Then
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(TaskEvent.Error::class.java)
        assertThat((events.first() as TaskEvent.Error).message).contains("删除失败")
        
        job.cancel()
    }
    
    @Test
    fun `错误处理 - 切换完成状态失败应该发送错误事件`() = runTest {
        // Given
        val exception = Exception("更新状态失败")
        coEvery { mockTodoRepository.updateTodoCompletion(any(), any()) } throws exception
        
        // Collect events
        val events = mutableListOf<TaskEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.taskEvent.toList(events)
        }
        
        // When
        viewModel.toggleTaskCompletion("1", true)
        
        // Then
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(TaskEvent.Error::class.java)
        assertThat((events.first() as TaskEvent.Error).message).contains("更新状态失败")
        
        job.cancel()
    }
    
    @Test
    fun `空搜索查询应该显示所有任务`() = runTest {
        // Given - 先设置一个搜索查询
        viewModel.updateSearchQuery("测试")
        testScheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.tasks).hasSize(1)
        
        // When - 清空搜索查询
        viewModel.updateSearchQuery("")
        testScheduler.advanceUntilIdle()
        
        // Then
        assertThat(viewModel.uiState.value.tasks).hasSize(3) // 恢复显示所有未完成任务
    }
    
    @Test
    fun `组合过滤条件应该正确过滤任务`() = runTest {
        // When - 设置多个过滤条件：高优先级且今天到期
        val newFilterOptions = viewModel.uiState.value.filterOptions.copy(
            selectedPriorities = setOf(0, 1),
            dateFilter = DateFilter.TODAY
        )
        viewModel.updateFilterOptions(newFilterOptions)
        
        testScheduler.advanceUntilIdle()
        
        // Then - 只有任务1符合条件（优先级0且今天到期）
        assertThat(viewModel.uiState.value.tasks).hasSize(1)
        assertThat(viewModel.uiState.value.tasks.first().id).isEqualTo("1")
    }
}
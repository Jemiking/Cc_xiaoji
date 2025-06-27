package com.ccxiaoji.feature.todo.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseViewModel
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.todo.domain.usecase.*
import com.ccxiaoji.feature.todo.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val getAllTodosUseCase: GetAllTodosUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase,
    private val searchTodosUseCase: SearchTodosUseCase,
    private val filterTodosUseCase: FilterTodosUseCase
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 任务事件，用于通知外部处理（如通知调度）
    private val _taskEvent = MutableSharedFlow<TaskEvent>()
    val taskEvent: SharedFlow<TaskEvent> = _taskEvent.asSharedFlow()
    
    init {
        observeTasksWithFilters()
    }
    
    private fun observeTasksWithFilters() {
        viewModelScope.launch {
            combine(
                searchQuery,
                _uiState.map { it.filterOptions },
                _uiState.map { it.filterOptions.showCompleted }
                    .distinctUntilChanged()
                    .flatMapLatest { showCompleted ->
                        getAllTodosUseCase(showCompleted)
                    }
            ) { query, filterOptions, allTasks ->
                filterTodosUseCase(allTasks, query, filterOptions)
            }.collect { filteredTasks ->
                _uiState.update { it.copy(tasks = filteredTasks) }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateFilterOptions(filterOptions: TaskFilterOptions) {
        _uiState.update { it.copy(filterOptions = filterOptions) }
    }
    
    fun addTask(
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ) {
        launchWithErrorHandling {
            val task = addTodoUseCase(
                title = title,
                description = description,
                dueAt = dueAt,
                priority = priority
            )
            
            // 发送事件，让外部处理通知调度
            if (dueAt != null) {
                _taskEvent.emit(TaskEvent.TaskAdded(task))
            }
            
            showSuccess("Task added successfully")
        }
    }
    
    fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ) {
        launchWithErrorHandling {
            updateTodoUseCase(
                todoId = taskId,
                title = title,
                description = description,
                dueAt = dueAt,
                priority = priority
            )
            
            // 发送事件，让外部处理通知调度
            _taskEvent.emit(TaskEvent.TaskUpdated(taskId, title, dueAt))
            
            showSuccess("Task updated successfully")
        }
    }
    
    fun toggleTaskCompletion(taskId: String, completed: Boolean) {
        launchWithErrorHandling {
            toggleTodoCompletionUseCase(taskId, completed)
        }
    }
    
    fun deleteTask(taskId: String) {
        launchWithErrorHandling {
            deleteTodoUseCase(taskId)
            // 发送事件，让外部处理通知调度
            _taskEvent.emit(TaskEvent.TaskDeleted(taskId))
            
            showSuccess("Task deleted successfully")
        }
    }
}

data class TodoUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val filterOptions: TaskFilterOptions = TaskFilterOptions()
)

data class TaskFilterOptions(
    val showCompleted: Boolean = false,
    val selectedPriorities: Set<Int> = setOf(0, 1, 2),
    val dateFilter: DateFilter = DateFilter.ALL
)

enum class DateFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    OVERDUE
}

// 任务事件，用于通知外部
sealed class TaskEvent {
    data class TaskAdded(val task: Task) : TaskEvent()
    data class TaskUpdated(val taskId: String, val title: String, val dueAt: Instant?) : TaskEvent()
    data class TaskDeleted(val taskId: String) : TaskEvent()
}
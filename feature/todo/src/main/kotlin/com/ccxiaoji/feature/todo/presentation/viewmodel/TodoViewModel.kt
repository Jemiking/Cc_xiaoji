package com.ccxiaoji.feature.todo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.todo.data.repository.TaskRepository
import com.ccxiaoji.feature.todo.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
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
                        if (showCompleted) {
                            taskRepository.getTasks()
                        } else {
                            taskRepository.getIncompleteTasks()
                        }
                    }
            ) { query, filterOptions, allTasks ->
                applyFilters(allTasks, query, filterOptions)
            }.collect { filteredTasks ->
                _uiState.update { it.copy(tasks = filteredTasks) }
            }
        }
    }
    
    private fun applyFilters(
        tasks: List<Task>,
        query: String,
        filterOptions: TaskFilterOptions
    ): List<Task> {
        var filteredTasks = tasks
        
        // Apply search filter
        if (query.isNotBlank()) {
            filteredTasks = filteredTasks.filter { task ->
                task.title.contains(query, ignoreCase = true) || 
                task.description?.contains(query, ignoreCase = true) == true
            }
        }
        
        // Apply priority filter
        filteredTasks = filteredTasks.filter { task ->
            task.priority in filterOptions.selectedPriorities
        }
        
        // Apply date filter
        filteredTasks = when (filterOptions.dateFilter) {
            DateFilter.TODAY -> {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                filteredTasks.filter { task ->
                    task.dueAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == today
                }
            }
            DateFilter.THIS_WEEK -> {
                val now = Clock.System.now()
                val weekStart = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    .let { it.minus(it.dayOfWeek.ordinal, DateTimeUnit.DAY) }
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                val weekEnd = weekStart.plus(DateTimePeriod(days = 7), TimeZone.currentSystemDefault())
                filteredTasks.filter { task ->
                    task.dueAt?.let { it >= weekStart && it < weekEnd } ?: false
                }
            }
            DateFilter.OVERDUE -> {
                val now = Clock.System.now()
                filteredTasks.filter { task ->
                    task.dueAt?.let { it < now && !task.completed } ?: false
                }
            }
            DateFilter.ALL -> filteredTasks
        }
        
        return filteredTasks
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
        viewModelScope.launch {
            val task = taskRepository.addTask(
                title = title,
                description = description,
                dueAt = dueAt,
                priority = priority
            )
            
            // 发送事件，让外部处理通知调度
            if (dueAt != null) {
                _taskEvent.emit(TaskEvent.TaskAdded(task))
            }
        }
    }
    
    fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ) {
        viewModelScope.launch {
            taskRepository.updateTask(
                taskId = taskId,
                title = title,
                description = description,
                dueAt = dueAt,
                priority = priority
            )
            
            // 发送事件，让外部处理通知调度
            _taskEvent.emit(TaskEvent.TaskUpdated(taskId, title, dueAt))
        }
    }
    
    fun toggleTaskCompletion(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskCompletion(taskId, completed)
        }
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            // 发送事件，让外部处理通知调度
            _taskEvent.emit(TaskEvent.TaskDeleted(taskId))
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
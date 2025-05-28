package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.data.repository.TaskRepository
import com.ccxiaoji.app.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.getIncompleteTasks().collect { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
            }
        }
    }
    
    fun addTask(
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ) {
        viewModelScope.launch {
            taskRepository.addTask(
                title = title,
                description = description,
                dueAt = dueAt,
                priority = priority
            )
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
        }
    }
}

data class TodoUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false
)
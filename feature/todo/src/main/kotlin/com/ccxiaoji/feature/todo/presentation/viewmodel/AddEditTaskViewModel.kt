package com.ccxiaoji.feature.todo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.todo.domain.model.Priority
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.usecase.AddTodoUseCase
import com.ccxiaoji.feature.todo.domain.usecase.UpdateTodoUseCase
import com.ccxiaoji.feature.todo.domain.usecase.GetTaskByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val addTodoUseCase: AddTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase
) : ViewModel() {
    
    data class AddEditTaskUiState(
        val isLoading: Boolean = false,
        val title: String = "",
        val description: String = "",
        val priority: Priority = Priority.MEDIUM,
        val dueAt: Instant? = null,
        val titleError: String? = null,
        val isSaved: Boolean = false,
        val taskId: String? = null
    )
    
    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()
    
    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val task = getTaskByIdUseCase(taskId)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = task.title,
                        description = task.description ?: "",
                        priority = task.priorityLevel,
                        dueAt = task.dueAt,
                        taskId = taskId
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = if (title.isEmpty()) "任务标题不能为空" else null
        )
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun updatePriority(priority: Priority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }
    
    fun updateDueDate(epochMillis: Long) {
        _uiState.value = _uiState.value.copy(
            dueAt = Instant.fromEpochMilliseconds(epochMillis)
        )
    }
    
    fun clearDueDate() {
        _uiState.value = _uiState.value.copy(dueAt = null)
    }
    
    fun saveTask() {
        val state = _uiState.value
        
        if (state.title.isEmpty()) {
            _uiState.value = state.copy(titleError = "任务标题不能为空")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                if (state.taskId != null) {
                    // 编辑模式
                    updateTodoUseCase(
                        todoId = state.taskId,
                        title = state.title,
                        description = state.description.ifEmpty { null },
                        dueAt = state.dueAt,
                        priority = state.priority.ordinal
                    )
                } else {
                    // 添加模式
                    addTodoUseCase(
                        title = state.title,
                        description = state.description.ifEmpty { null },
                        dueAt = state.dueAt,
                        priority = state.priority.ordinal
                    )
                }
                
                _uiState.value = state.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    titleError = "保存失败：${e.message}"
                )
            }
        }
    }
}
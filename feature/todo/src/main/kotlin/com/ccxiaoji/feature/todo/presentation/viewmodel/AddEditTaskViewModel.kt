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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val addTodoUseCase: AddTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val todoRepository: com.ccxiaoji.feature.todo.domain.repository.TodoRepository
) : ViewModel() {

    data class AddEditTaskUiState(
        val isLoading: Boolean = false,
        val title: String = "",
        val description: String = "",
        val priority: Priority = Priority.MEDIUM,
        val dueAt: Instant? = null,
        val titleError: String? = null,
        val isSaved: Boolean = false,
        val taskId: String? = null,

        // ===== 提醒设置（Phase 3 - 混合模式）=====
        val reminderEnabled: Boolean? = null,  // null=使用全局配置
        val reminderMinutesBefore: Int? = null,  // 相对时间：提前N分钟
        val reminderTime: String? = null  // 固定时间："HH:mm"格式
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
                        taskId = taskId,
                        reminderEnabled = task.reminderEnabled,
                        reminderMinutesBefore = task.reminderMinutesBefore,
                        reminderTime = task.reminderTime
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

    fun updateReminderEnabled(enabled: Boolean?) {
        _uiState.value = _uiState.value.copy(reminderEnabled = enabled)
    }

    fun updateReminderMinutesBefore(minutes: Int?) {
        _uiState.value = _uiState.value.copy(reminderMinutesBefore = minutes)
    }

    fun updateReminderTime(time: String?) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
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
                val savedTaskId: String
                if (state.taskId != null) {
                    // 编辑模式
                    updateTodoUseCase(
                        todoId = state.taskId,
                        title = state.title,
                        description = state.description.ifEmpty { null },
                        dueAt = state.dueAt,
                        priority = state.priority.ordinal
                    )
                    savedTaskId = state.taskId
                } else {
                    // 添加模式
                    val newTask = addTodoUseCase(
                        title = state.title,
                        description = state.description.ifEmpty { null },
                        dueAt = state.dueAt,
                        priority = state.priority.ordinal
                    )
                    savedTaskId = newTask.id
                }

                // 保存提醒设置（如果有自定义配置）
                if (state.reminderEnabled != null || state.reminderMinutesBefore != null || state.reminderTime != null) {
                    // 计算reminderAt：如果用户设置了固定时间，需要转换为Instant
                    val reminderAt: Instant? = if (state.reminderTime != null && state.dueAt != null) {
                        calculateFixedReminderTime(state.reminderTime, state.dueAt)
                    } else {
                        null
                    }

                    todoRepository.updateTaskReminder(
                        todoId = savedTaskId,
                        reminderEnabled = state.reminderEnabled,
                        reminderAt = reminderAt,  // 直接传递Instant?类型
                        reminderMinutesBefore = state.reminderMinutesBefore,
                        reminderTime = state.reminderTime
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

    /**
     * 计算固定时间提醒的绝对时间
     * @param timeString 时间字符串 "HH:mm"
     * @param dueAt 任务截止时间
     * @return 提醒时间的Instant
     */
    private fun calculateFixedReminderTime(timeString: String, dueAt: Instant): Instant? {
        return try {
            val parts = timeString.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            // 获取截止日期的日期部分（系统时区）
            val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
            val dueDateTime = dueAt.toLocalDateTime(timeZone)
            val dueDate = dueDateTime.date

            // 创建提醒时间：在截止日期当天的指定时刻
            val reminderDateTime = kotlinx.datetime.LocalDateTime(
                year = dueDate.year,
                monthNumber = dueDate.monthNumber,
                dayOfMonth = dueDate.dayOfMonth,
                hour = hour,
                minute = minute,
                second = 0
            )

            // 转换为Instant
            reminderDateTime.toInstant(timeZone)
        } catch (e: Exception) {
            null
        }
    }
}
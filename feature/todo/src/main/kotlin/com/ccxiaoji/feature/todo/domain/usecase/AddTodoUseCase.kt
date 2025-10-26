package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * 添加待办事项用例
 * 处理待办事项的创建逻辑
 */
class AddTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    /**
     * 添加新的待办事项
     * @param title 标题
     * @param description 描述（可选）
     * @param dueAt 截止时间（可选）
     * @param priority 优先级（0-2）
     * @return 创建的待办事项
     */
    suspend operator fun invoke(
        title: String,
        description: String? = null,
        dueAt: Instant? = null,
        priority: Int = 0,
        // ===== 提醒相关（可选）=====
        reminderEnabled: Boolean? = null,
        reminderMinutesBefore: Int? = null,
        reminderTime: String? = null
    ): Task {
        // 验证输入
        if (title.isBlank()) {
            throw DomainException.ValidationException("标题不能为空")
        }
        if (priority !in 0..2) {
            throw DomainException.ValidationException("优先级必须在0-2之间")
        }
        
        val result = repository.addTodo(
            title = title.trim(),
            description = description?.trim(),
            dueAt = dueAt,
            priority = priority
        )
        val created = result.getOrThrow()

        // 如果携带了提醒字段，先持久化提醒配置（不直接写 reminderAt，由上层按策略计算并调度）
        if (reminderEnabled != null || reminderMinutesBefore != null || reminderTime != null) {
            repository.updateTaskReminder(
                todoId = created.id,
                reminderEnabled = reminderEnabled,
                reminderAt = null,
                reminderMinutesBefore = reminderMinutesBefore,
                reminderTime = reminderTime
            )
        }

        return created
    }
}

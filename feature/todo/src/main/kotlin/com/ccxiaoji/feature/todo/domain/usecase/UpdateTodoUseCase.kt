package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * 更新待办事项用例
 * 处理待办事项的更新逻辑
 */
class UpdateTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    /**
     * 更新待办事项
     * @param todoId 待办事项ID
     * @param title 标题
     * @param description 描述（可选）
     * @param dueAt 截止时间（可选）
     * @param priority 优先级（0-2）
     */
    suspend operator fun invoke(
        todoId: String,
        title: String,
        description: String? = null,
        dueAt: Instant? = null,
        priority: Int = 0
    ) {
        // 验证输入
        if (todoId.isBlank()) {
            throw DomainException.ValidationException("待办事项ID不能为空")
        }
        if (title.isBlank()) {
            throw DomainException.ValidationException("标题不能为空")
        }
        if (priority !in 0..2) {
            throw DomainException.ValidationException("优先级必须在0-2之间")
        }
        
        repository.updateTodo(
            todoId = todoId,
            title = title.trim(),
            description = description?.trim(),
            dueAt = dueAt,
            priority = priority
        ).getOrThrow()
    }
}
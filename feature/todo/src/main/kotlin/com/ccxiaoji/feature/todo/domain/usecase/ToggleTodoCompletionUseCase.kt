package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import javax.inject.Inject

/**
 * 切换待办事项完成状态用例
 */
class ToggleTodoCompletionUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    /**
     * 切换待办事项的完成状态
     * @param todoId 待办事项ID
     * @param completed 是否完成
     */
    suspend operator fun invoke(todoId: String, completed: Boolean) {
        if (todoId.isBlank()) {
            throw DomainException.ValidationException("待办事项ID不能为空")
        }
        repository.updateTodoCompletion(todoId, completed).getOrThrow()
    }
}
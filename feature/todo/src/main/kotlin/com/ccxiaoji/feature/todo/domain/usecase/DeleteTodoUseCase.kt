package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import javax.inject.Inject

/**
 * 删除待办事项用例
 */
class DeleteTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    /**
     * 删除指定的待办事项
     * @param todoId 待办事项ID
     */
    suspend operator fun invoke(todoId: String) {
        if (todoId.isBlank()) {
            throw DomainException.ValidationException("待办事项ID不能为空")
        }
        repository.deleteTodo(todoId).getOrThrow()
    }
}
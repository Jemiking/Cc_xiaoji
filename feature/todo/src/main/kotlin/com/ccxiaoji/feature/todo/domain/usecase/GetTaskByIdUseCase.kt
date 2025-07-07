package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import javax.inject.Inject

/**
 * 根据ID获取任务用例
 */
class GetTaskByIdUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(taskId: String): Task? {
        val result = repository.getTodoById(taskId)
        return if (result is com.ccxiaoji.common.base.BaseResult.Success) {
            result.data
        } else {
            null
        }
    }
}
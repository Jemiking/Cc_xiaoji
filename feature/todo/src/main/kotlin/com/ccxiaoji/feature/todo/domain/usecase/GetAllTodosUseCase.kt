package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有待办事项用例
 */
class GetAllTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    /**
     * 获取所有待办事项
     * @param showCompleted 是否显示已完成的任务
     */
    operator fun invoke(showCompleted: Boolean = false): Flow<List<Task>> {
        return if (showCompleted) {
            repository.getAllTodos()
        } else {
            repository.getIncompleteTodos()
        }
    }
}
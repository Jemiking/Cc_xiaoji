package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取今日待办事项用例
 */
class GetTodayTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    /**
     * 获取今日的待办事项
     * @return 今日待办事项列表
     */
    operator fun invoke(): Flow<List<Task>> {
        return repository.getTodayTodos()
    }
    
    /**
     * 获取今日待办事项数量
     * @return 今日待办事项数量
     */
    fun getTodayCount(): Flow<Int> {
        return repository.getTodayTodosCount()
    }
}
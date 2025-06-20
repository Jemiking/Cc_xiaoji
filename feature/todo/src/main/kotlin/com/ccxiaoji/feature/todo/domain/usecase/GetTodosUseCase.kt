package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.ccxiaoji.feature.todo.domain.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有待办事项的用例
 */
class GetTodosUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    /**
     * 执行获取所有待办事项
     * @return 待办事项列表的Flow
     */
    operator fun invoke(): Flow<List<Task>> {
        return todoRepository.getAllTodos()
    }
    
    /**
     * 获取今日待办事项
     * @return 今日待办事项列表的Flow
     */
    fun getTodayTodos(): Flow<List<Task>> {
        return todoRepository.getTodayTodos()
    }
    
    /**
     * 获取未完成的待办事项
     * @return 未完成待办事项列表的Flow
     */
    fun getIncompleteTodos(): Flow<List<Task>> {
        return todoRepository.getIncompleteTodos()
    }
    
    /**
     * 搜索待办事项
     * @param query 搜索关键词
     * @return 匹配的待办事项列表的Flow
     */
    fun searchTodos(query: String): Flow<List<Task>> {
        return todoRepository.searchTodos(query)
    }
}
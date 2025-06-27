package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 搜索待办事项用例
 */
class SearchTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    /**
     * 根据关键词搜索待办事项
     * @param query 搜索关键词
     * @return 匹配的待办事项列表
     */
    operator fun invoke(query: String): Flow<List<Task>> {
        return if (query.isBlank()) {
            repository.getAllTodos()
        } else {
            repository.searchTodos(query.trim())
        }
    }
}
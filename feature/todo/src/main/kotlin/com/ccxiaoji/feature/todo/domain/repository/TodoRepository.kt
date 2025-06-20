package com.ccxiaoji.feature.todo.domain.repository

import com.ccxiaoji.feature.todo.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Todo领域层的仓库接口
 * 定义了所有待办事项相关的数据操作
 */
interface TodoRepository {
    /**
     * 获取所有待办事项
     */
    fun getAllTodos(): Flow<List<Task>>
    
    /**
     * 获取未完成的待办事项
     */
    fun getIncompleteTodos(): Flow<List<Task>>
    
    /**
     * 搜索待办事项
     */
    fun searchTodos(query: String): Flow<List<Task>>
    
    /**
     * 获取今日待办事项
     */
    fun getTodayTodos(): Flow<List<Task>>
    
    /**
     * 获取今日待办事项数量
     */
    fun getTodayTodosCount(): Flow<Int>
    
    /**
     * 添加待办事项
     */
    suspend fun addTodo(
        title: String,
        description: String? = null,
        dueAt: Instant? = null,
        priority: Int = 0
    ): Task
    
    /**
     * 更新待办事项完成状态
     */
    suspend fun updateTodoCompletion(todoId: String, completed: Boolean)
    
    /**
     * 更新待办事项
     */
    suspend fun updateTodo(
        todoId: String,
        title: String,
        description: String? = null,
        dueAt: Instant? = null,
        priority: Int = 0
    )
    
    /**
     * 删除待办事项
     */
    suspend fun deleteTodo(todoId: String)
    
    /**
     * 根据ID获取待办事项
     */
    suspend fun getTodoById(todoId: String): Task?
}
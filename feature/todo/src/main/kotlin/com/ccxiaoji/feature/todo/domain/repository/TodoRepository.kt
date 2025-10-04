package com.ccxiaoji.feature.todo.domain.repository

import com.ccxiaoji.common.base.BaseResult
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
     * @return Flow<List<Task>> 所有待办事项的实时数据流
     */
    fun getAllTodos(): Flow<List<Task>>
    
    /**
     * 获取未完成的待办事项
     * @return Flow<List<Task>> 未完成任务的实时数据流
     */
    fun getIncompleteTodos(): Flow<List<Task>>
    
    /**
     * 搜索待办事项
     * @param query 搜索关键词，将在标题和描述中进行模糊匹配
     * @return Flow<List<Task>> 匹配的任务列表数据流
     */
    fun searchTodos(query: String): Flow<List<Task>>
    
    /**
     * 获取今日待办事项
     * @return Flow<List<Task>> 今日到期的任务列表数据流
     */
    fun getTodayTodos(): Flow<List<Task>>
    
    /**
     * 获取今日待办事项数量
     * @return Flow<Int> 今日任务数量的实时数据流
     */
    fun getTodayTodosCount(): Flow<Int>
    
    /**
     * 添加待办事项
     * @param title 任务标题，不能为空
     * @param description 任务描述，可选
     * @param dueAt 截止时间，可选
     * @param priority 优先级（0=低，1=中，2=高），默认为0
     * @return BaseResult<Task> 创建成功返回Task对象，失败返回错误信息
     * @throws DomainException.ValidationException 当参数验证失败时抛出
     */
    suspend fun addTodo(
        title: String,
        description: String? = null,
        dueAt: Instant? = null,
        priority: Int = 0
    ): BaseResult<Task>
    
    /**
     * 更新待办事项完成状态
     * @param todoId 任务ID
     * @param completed 是否完成
     * @return BaseResult<Unit> 成功返回Unit，失败返回错误信息
     */
    suspend fun updateTodoCompletion(todoId: String, completed: Boolean): BaseResult<Unit>
    
    /**
     * 更新待办事项
     * @param todoId 任务ID
     * @param title 新的标题
     * @param description 新的描述
     * @param dueAt 新的截止时间
     * @param priority 新的优先级
     * @return BaseResult<Unit> 成功返回Unit，失败返回错误信息
     */
    suspend fun updateTodo(
        todoId: String,
        title: String,
        description: String? = null,
        dueAt: Instant? = null,
        priority: Int = 0
    ): BaseResult<Unit>
    
    /**
     * 删除待办事项
     * @param todoId 要删除的任务ID
     * @return BaseResult<Unit> 成功返回Unit，失败返回错误信息
     */
    suspend fun deleteTodo(todoId: String): BaseResult<Unit>
    
    /**
     * 根据ID获取待办事项
     * @param todoId 任务ID
     * @return BaseResult<Task?> 成功返回Task对象或null（如果不存在），失败返回错误信息
     */
    suspend fun getTodoById(todoId: String): BaseResult<Task?>

    /**
     * 更新任务提醒配置（Phase 3 - 支持固定时间）
     *
     * @param todoId 任务ID
     * @param reminderEnabled 是否启用提醒（null=继承全局配置，true=强制启用，false=强制禁用）
     * @param reminderAt 绝对提醒时间（null=使用相对时间，非null=使用绝对时间）
     * @param reminderMinutesBefore 提前提醒分钟数（null=使用全局配置，非null=使用单任务配置）
     * @param reminderTime 固定时间提醒（null=使用相对时间，非null=使用固定时间，格式："HH:mm"）
     * @return BaseResult<Unit> 成功返回Unit，失败返回错误信息
     *
     * 优先级策略：reminderTime（固定时间） > reminderAt > reminderMinutesBefore > 全局配置
     */
    suspend fun updateTaskReminder(
        todoId: String,
        reminderEnabled: Boolean? = null,
        reminderAt: Instant? = null,
        reminderMinutesBefore: Int? = null,
        reminderTime: String? = null
    ): BaseResult<Unit>

}
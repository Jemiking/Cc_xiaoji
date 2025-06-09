package com.ccxiaoji.feature.todo.api

/**
 * 导航接口，需要在app模块中实现
 */
interface TodoNavigator {
    fun navigateToTodoList()
    fun navigateToAddTask()
}
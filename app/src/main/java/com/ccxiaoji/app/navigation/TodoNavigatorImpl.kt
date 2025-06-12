package com.ccxiaoji.app.navigation

import androidx.navigation.NavController
import com.ccxiaoji.feature.todo.api.TodoNavigator
import com.ccxiaoji.feature.todo.presentation.navigation.TodoRoute
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TodoNavigator的实现
 * 在app模块中实现feature-todo模块的导航接口
 */
@Singleton
class TodoNavigatorImpl @Inject constructor() : TodoNavigator {
    
    private var navController: NavController? = null
    
    fun setNavController(navController: NavController) {
        this.navController = navController
    }
    
    override fun navigateToTodoList() {
        navController?.navigate(TodoRoute.TODO_LIST)
    }
    
    override fun navigateToAddTask() {
        navController?.navigate(TodoRoute.ADD_TASK)
    }
}
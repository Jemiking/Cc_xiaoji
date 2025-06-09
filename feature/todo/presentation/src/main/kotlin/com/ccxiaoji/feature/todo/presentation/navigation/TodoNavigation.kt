package com.ccxiaoji.feature.todo.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ccxiaoji.feature.todo.presentation.ui.TodoScreen

/**
 * Todo模块的导航路由
 */
object TodoRoute {
    const val TODO_LIST = "todo_list"
    const val ADD_TASK = "add_task"
}

/**
 * Todo模块的导航图构建
 */
fun NavGraphBuilder.todoGraph(
    navController: NavController
) {
    composable(TodoRoute.TODO_LIST) {
        TodoScreen()
    }
}
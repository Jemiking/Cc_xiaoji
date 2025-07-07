package com.ccxiaoji.feature.todo.presentation.navigation

// 定义待办模块的导航路由
object TodoNavigation {
    const val AddEditTaskRoute = "add_edit_task?taskId={taskId}"
    const val DatePickerRoute = "date_picker?initialMillis={initialMillis}"
    
    fun addEditTaskRoute(taskId: String? = null) = if (taskId != null) {
        "add_edit_task?taskId=$taskId"
    } else {
        "add_edit_task"
    }
    
    fun datePickerRoute(initialMillis: Long? = null) = if (initialMillis != null) {
        "date_picker?initialMillis=$initialMillis"
    } else {
        "date_picker"
    }
}
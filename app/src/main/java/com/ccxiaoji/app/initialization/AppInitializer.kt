package com.ccxiaoji.app.initialization

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用初始化管理器
 * 
 * 负责管理应用启动时的各种初始化任务，支持：
 * - 延迟初始化
 * - 异步执行
 * - 优先级管理
 */
@Singleton
class AppInitializer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AppInitializer"
    }
    
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val initTasks = mutableListOf<InitTask>()
    
    /**
     * 初始化任务定义
     */
    data class InitTask(
        val name: String,
        val priority: Priority,
        val runOnMainThread: Boolean = false,
        val delayMillis: Long = 0,
        val task: suspend () -> Unit
    )
    
    /**
     * 初始化优先级
     */
    enum class Priority {
        CRITICAL,    // 关键任务，必须立即执行
        HIGH,        // 高优先级，尽快执行
        NORMAL,      // 普通优先级
        LOW          // 低优先级，可延迟执行
    }
    
    /**
     * 注册初始化任务
     */
    fun registerTask(task: InitTask) {
        initTasks.add(task)
    }
    
    /**
     * 执行所有初始化任务
     */
    fun initialize() {
        Log.d(TAG, "Starting app initialization")
        
        // 按优先级分组执行
        val tasksByPriority = initTasks.groupBy { it.priority }
        
        // 立即执行关键任务
        tasksByPriority[Priority.CRITICAL]?.forEach { task ->
            executeTask(task, immediately = true)
        }
        
        // 异步执行其他任务
        initScope.launch {
            // 高优先级任务
            tasksByPriority[Priority.HIGH]?.forEach { task ->
                executeTask(task)
            }
            
            // 普通优先级任务（延迟100ms）
            kotlinx.coroutines.delay(100)
            tasksByPriority[Priority.NORMAL]?.forEach { task ->
                executeTask(task)
            }
            
            // 低优先级任务（延迟500ms）
            kotlinx.coroutines.delay(400)
            tasksByPriority[Priority.LOW]?.forEach { task ->
                executeTask(task)
            }
        }
    }
    
    private fun executeTask(task: InitTask, immediately: Boolean = false) {
        Log.d(TAG, "Executing task: ${task.name} (priority: ${task.priority})")
        
        if (immediately && task.runOnMainThread) {
            // 主线程立即执行
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    if (task.delayMillis > 0) {
                        kotlinx.coroutines.delay(task.delayMillis)
                    }
                    task.task()
                    Log.d(TAG, "Task completed: ${task.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Task failed: ${task.name}", e)
                }
            }
        } else {
            // 异步执行
            initScope.launch(if (task.runOnMainThread) Dispatchers.Main else Dispatchers.IO) {
                try {
                    if (task.delayMillis > 0) {
                        kotlinx.coroutines.delay(task.delayMillis)
                    }
                    task.task()
                    Log.d(TAG, "Task completed: ${task.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Task failed: ${task.name}", e)
                }
            }
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        // 取消所有未完成的任务
        cancelAllChildren()
    }
    
    private fun cancelAllChildren() {
        // Cancel all children jobs in the scope
        initScope.coroutineContext[kotlinx.coroutines.Job]?.children?.forEach { it.cancel() }
    }
}
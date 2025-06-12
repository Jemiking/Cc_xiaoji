package com.ccxiaoji.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ccxiaoji.app.initialization.AppInitializer
import com.ccxiaoji.app.initialization.DatabaseInitTask
import com.ccxiaoji.app.initialization.WorkerInitTask
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import android.util.Log

@HiltAndroidApp
class CcXiaoJiApplication : Application(), Configuration.Provider {
    
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var appInitializer: AppInitializer
    
    @Inject
    lateinit var databaseInitTask: DatabaseInitTask
    
    @Inject
    lateinit var workerInitTask: WorkerInitTask
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate started")
        
        try {
            // 使用AppInitializer管理所有初始化任务
            setupInitializationTasks()
            
            // 启动初始化流程
            appInitializer.initialize()
            
            Log.d(TAG, "Application onCreate completed (initialization tasks scheduled)")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in Application onCreate", e)
            throw e
        }
    }
    
    /**
     * 配置所有初始化任务
     * 按照优先级分配任务，实现懒加载
     */
    private fun setupInitializationTasks() {
        // 关键任务：暂时没有必须立即执行的任务
        
        // 高优先级任务：注册重要的Worker
        appInitializer.registerTask(
            AppInitializer.InitTask(
                name = "RegisterHighPriorityWorkers",
                priority = AppInitializer.Priority.HIGH,
                delayMillis = 100 // 延迟100ms，让UI先加载
            ) {
                workerInitTask.registerHighPriorityWorkers()
            }
        )
        
        // 普通优先级任务：数据库初始化（改为延迟初始化，仅在首次访问时执行）
        // 注意：数据库初始化已经通过LazyInitRepositoryWrapper实现延迟加载
        // 这里只是预热，可以进一步延迟
        appInitializer.registerTask(
            AppInitializer.InitTask(
                name = "DatabaseWarmup",
                priority = AppInitializer.Priority.NORMAL,
                delayMillis = 2000 // 延迟2秒，等应用完全启动后再预热
            ) {
                // 预热数据库连接，但不强制初始化
                Log.d(TAG, "Database warmup scheduled")
            }
        )
        
        // 普通优先级任务：注册普通Worker
        appInitializer.registerTask(
            AppInitializer.InitTask(
                name = "RegisterNormalPriorityWorkers",
                priority = AppInitializer.Priority.NORMAL,
                delayMillis = 500 // 延迟500ms
            ) {
                workerInitTask.registerNormalPriorityWorkers()
            }
        )
        
        // 低优先级任务：可以大幅延迟的初始化
        appInitializer.registerTask(
            AppInitializer.InitTask(
                name = "RegisterLowPriorityWorkers",
                priority = AppInitializer.Priority.LOW,
                delayMillis = 5000 // 延迟5秒
            ) {
                workerInitTask.registerLowPriorityWorkers()
            }
        )
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
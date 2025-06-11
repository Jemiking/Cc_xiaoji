package com.ccxiaoji.app.initialization

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository延迟初始化包装器
 * 
 * 确保在首次访问数据库时自动完成初始化
 */
@Singleton
class LazyInitRepositoryWrapper @Inject constructor(
    private val databaseInitTask: DatabaseInitTask
) {
    /**
     * 包装suspend函数，确保数据库已初始化
     */
    suspend fun <T> ensureInitAndExecute(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            // 确保数据库已初始化
            databaseInitTask.ensureInitialized()
            // 执行实际操作
            block()
        }
    }
    
    /**
     * 包装Flow，确保数据库已初始化
     */
    suspend fun <T> ensureInitAndFlow(flowProvider: suspend () -> kotlinx.coroutines.flow.Flow<T>): kotlinx.coroutines.flow.Flow<T> {
        return withContext(Dispatchers.IO) {
            // 确保数据库已初始化
            databaseInitTask.ensureInitialized()
            // 返回Flow
            flowProvider()
        }
    }
}
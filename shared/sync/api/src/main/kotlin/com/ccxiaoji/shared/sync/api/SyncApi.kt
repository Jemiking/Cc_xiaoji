package com.ccxiaoji.shared.sync.api

import kotlinx.coroutines.flow.Flow

/**
 * 同步功能的公共API接口
 * 提供同步管理、状态查询等功能
 */
interface SyncApi {
    
    /**
     * 启动定期同步
     * 使用WorkManager定期执行同步任务
     */
    suspend fun startPeriodicSync()
    
    /**
     * 立即执行一次同步
     */
    suspend fun syncNow()
    
    /**
     * 取消同步任务
     */
    suspend fun cancelSync()
    
    /**
     * 获取同步状态流
     * @return 同步状态的Flow
     */
    fun getSyncStatus(): Flow<SyncState>
    
    /**
     * 获取上次成功同步的时间
     * @return 时间戳，如果从未同步过返回0
     */
    suspend fun getLastSyncTime(): Long
    
    /**
     * 检查是否需要同步
     * @return true表示需要同步，false表示不需要
     */
    suspend fun needsSync(): Boolean
    
    /**
     * 获取待同步的变更数量
     * @return 待同步的变更记录数
     */
    suspend fun getPendingChangesCount(): Int
}

/**
 * 同步状态枚举
 */
enum class SyncState {
    /** 空闲状态，没有正在进行的同步 */
    IDLE,
    
    /** 正在同步 */
    SYNCING,
    
    /** 同步成功 */
    SUCCESS,
    
    /** 同步失败 */
    ERROR
}
package com.ccxiaoji.shared.sync.data

import com.ccxiaoji.core.database.CcDatabase
import com.ccxiaoji.core.database.model.SyncStatus
import com.ccxiaoji.shared.sync.api.SyncApi
import com.ccxiaoji.shared.sync.api.SyncState
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncApi接口的实现类
 * 整合SyncManager功能并提供额外的同步相关功能
 */
@Singleton
class SyncApiImpl @Inject constructor(
    private val syncManager: SyncManager,
    private val userApi: UserApi,
    private val database: CcDatabase
) : SyncApi {
    
    override suspend fun startPeriodicSync() {
        syncManager.startPeriodicSync()
    }
    
    override suspend fun syncNow() {
        syncManager.syncNow()
    }
    
    override suspend fun cancelSync() {
        syncManager.cancelSync()
    }
    
    override fun getSyncStatus(): Flow<SyncState> {
        return syncManager.getSyncStatus()
    }
    
    override suspend fun getLastSyncTime(): Long {
        return userApi.getLastSyncTime()
    }
    
    override suspend fun needsSync(): Boolean {
        // 检查是否有待同步的变更
        val pendingCount = getPendingChangesCount()
        if (pendingCount > 0) return true
        
        // 检查是否超过同步间隔（例如：15分钟）
        val lastSyncTime = getLastSyncTime()
        val currentTime = System.currentTimeMillis()
        val syncInterval = 15 * 60 * 1000L // 15分钟
        
        return (currentTime - lastSyncTime) > syncInterval
    }
    
    override suspend fun getPendingChangesCount(): Int {
        val changeLogDao = database.changeLogDao()
        return changeLogDao.getPendingChangesCount()
    }
}
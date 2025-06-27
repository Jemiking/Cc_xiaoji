package com.ccxiaoji.feature.plan.domain.model

/**
 * 同步状态枚举
 * 用于标识数据的同步状态，为未来集成到CC小记主项目做准备
 */
enum class SyncStatus {
    /**
     * 本地数据，尚未同步
     */
    LOCAL,
    
    /**
     * 等待同步
     */
    PENDING_SYNC,
    
    /**
     * 正在同步
     */
    SYNCING,
    
    /**
     * 已同步
     */
    SYNCED,
    
    /**
     * 同步失败
     */
    SYNC_FAILED
}
package com.ccxiaoji.shared.sync.api

/**
 * 同步状态
 */
enum class SyncState {
    IDLE,      // 空闲
    SYNCING,   // 同步中
    SUCCESS,   // 成功
    ERROR      // 错误
}
package com.ccxiaoji.shared.sync.domain.model

/**
 * 同步上传项
 * 用于将本地变更上传到服务器
 */
data class SyncUploadItem(
    val table: String,
    val rowId: String,
    val operation: String,
    val payload: String,
    val timestamp: Long
)

/**
 * 同步变更
 * 表示从服务器下载的变更数据
 */
data class SyncChange(
    val table: String,
    val rows: List<Map<String, Any>>
)

/**
 * 同步上传响应
 * 服务器返回的上传结果
 */
data class SyncUploadResponse(
    val serverTime: Long,
    val processedCount: Int,
    val conflicts: List<ConflictItem>?
)

/**
 * 冲突项
 * 表示同步过程中发生的数据冲突
 */
data class ConflictItem(
    val table: String,
    val rowId: String,
    val resolution: String
)
package com.ccxiaoji.shared.sync.data.remote.dto

data class SyncUploadItem(
    val table: String,
    val rowId: String,
    val operation: String,
    val payload: String,
    val timestamp: Long
)

data class SyncChange(
    val table: String,
    val rows: List<Map<String, Any>>
)

data class SyncUploadResponse(
    val serverTime: Long,
    val processedCount: Int,
    val conflicts: List<ConflictItem>?
)

data class ConflictItem(
    val table: String,
    val rowId: String,
    val resolution: String
)
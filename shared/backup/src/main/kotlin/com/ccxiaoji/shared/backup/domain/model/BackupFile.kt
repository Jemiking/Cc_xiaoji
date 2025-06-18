package com.ccxiaoji.shared.backup.domain.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * 备份文件信息
 */
data class BackupFile(
    val path: String,
    val name: String,
    val size: Long,
    val createdAt: Long
) {
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
    
    fun getFormattedDate(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(createdAt))
    }
}
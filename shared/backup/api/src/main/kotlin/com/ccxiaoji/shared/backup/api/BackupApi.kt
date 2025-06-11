package com.ccxiaoji.shared.backup.api

/**
 * 备份管理API接口
 * 提供数据库备份和恢复功能
 */
interface BackupApi {
    /**
     * 创建数据库备份
     * @return 备份文件路径，失败返回null
     */
    suspend fun createBackup(): String?
    
    /**
     * 在迁移前自动创建备份
     * @return 备份文件路径，失败返回null
     */
    suspend fun createMigrationBackup(): String?
    
    /**
     * 恢复数据库备份
     * @param backupPath 备份文件路径
     * @return 是否恢复成功
     */
    suspend fun restoreBackup(backupPath: String): Boolean
    
    /**
     * 获取所有备份文件
     * @return 备份文件列表，按时间倒序排列
     */
    suspend fun getBackupFiles(): List<BackupFile>
}

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
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(createdAt))
    }
}
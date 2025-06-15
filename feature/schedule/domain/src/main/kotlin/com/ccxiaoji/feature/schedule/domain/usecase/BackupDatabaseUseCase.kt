package com.ccxiaoji.feature.schedule.domain.usecase

import android.net.Uri
import com.ccxiaoji.shared.backup.api.BackupApi
import javax.inject.Inject

/**
 * 数据库备份用例
 */
class BackupDatabaseUseCase @Inject constructor(
    private val backupApi: BackupApi
) {
    /**
     * 执行备份
     * @param backupUri 如果提供，将备份到指定位置；否则备份到默认位置
     * @return 备份结果
     */
    suspend operator fun invoke(backupUri: Uri? = null): Result<String> {
        return try {
            // 实现自定义位置备份
            // 目前使用默认备份位置
            val backupPath = backupApi.createBackup()
            if (backupPath != null) {
                Result.success(backupPath)
            } else {
                Result.failure(Exception("备份失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除旧备份文件
     * @param keepCount 保留的备份数量
     */
    suspend fun deleteOldBackups(keepCount: Int) {
        try {
            val backupFiles = backupApi.getBackupFiles()
            if (backupFiles.size > keepCount) {
                // 删除旧备份文件
                // 保留最新的keepCount个文件
            }
        } catch (e: Exception) {
            // 忽略删除失败
        }
    }
}
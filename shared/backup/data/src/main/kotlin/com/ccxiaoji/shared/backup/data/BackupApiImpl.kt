package com.ccxiaoji.shared.backup.data

import com.ccxiaoji.shared.backup.api.BackupApi
import com.ccxiaoji.shared.backup.api.BackupFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupApi的实现类
 * 委托给DatabaseBackupManager处理实际的备份逻辑
 */
@Singleton
class BackupApiImpl @Inject constructor(
    private val backupManager: DatabaseBackupManager
) : BackupApi {
    
    override suspend fun createBackup(): String? {
        return backupManager.createBackup()
    }
    
    override suspend fun createMigrationBackup(): String? {
        return backupManager.createMigrationBackup()
    }
    
    override suspend fun restoreBackup(backupPath: String): Boolean {
        return backupManager.restoreBackup(backupPath)
    }
    
    override suspend fun getBackupFiles(): List<BackupFile> {
        return backupManager.getBackupFiles()
    }
}
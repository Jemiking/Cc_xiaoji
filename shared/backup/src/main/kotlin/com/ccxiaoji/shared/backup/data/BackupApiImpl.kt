package com.ccxiaoji.shared.backup.data

import com.ccxiaoji.shared.backup.api.BackupApi
import com.ccxiaoji.shared.backup.data.manager.DatabaseBackupManager
import com.ccxiaoji.shared.backup.domain.model.BackupFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupApi的实现类
 */
@Singleton
class BackupApiImpl @Inject constructor(
    private val databaseBackupManager: DatabaseBackupManager
) : BackupApi {
    
    override suspend fun createBackup(): String? {
        return databaseBackupManager.createBackup()
    }
    
    override suspend fun restoreBackup(backupPath: String): Boolean {
        return databaseBackupManager.restoreBackup(backupPath)
    }
    
    override suspend fun getBackupFiles(): List<BackupFile> {
        return databaseBackupManager.getBackupFiles()
    }
    
    override suspend fun createMigrationBackup(): String? {
        return databaseBackupManager.createMigrationBackup()
    }
}
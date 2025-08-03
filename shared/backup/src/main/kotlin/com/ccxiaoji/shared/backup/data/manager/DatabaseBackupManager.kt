package com.ccxiaoji.shared.backup.data.manager

import android.content.Context
import android.os.Environment
import com.ccxiaoji.common.constants.DatabaseConstants
// import com.ccxiaoji.common.data.import.BackupFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * 数据库备份管理器
 * 提供数据库的备份和恢复功能，确保用户数据安全
 */
@Singleton
class DatabaseBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val BACKUP_DIR_NAME = "CcXiaojiBackup"
        private const val BACKUP_FILE_PREFIX = "cc_xiaoji_backup_"
        private const val BACKUP_FILE_EXTENSION = ".db"
        private const val MAX_BACKUP_COUNT = 5 // 最多保留5个备份
    }
    
    /**
     * 创建数据库备份
     * @return 备份文件路径，如果失败返回null
     */
    suspend fun createBackup(): String? = withContext(Dispatchers.IO) {
        try {
            // 1. 确保备份目录存在
            val backupDir = getBackupDirectory() ?: return@withContext null
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            // 2. 生成备份文件名
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
            val backupFile = File(backupDir, backupFileName)
            
            // 3. 获取当前数据库文件
            val currentDbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
            if (!currentDbFile.exists()) {
                return@withContext null
            }
            
            // 4. 复制数据库文件
            currentDbFile.copyTo(backupFile, overwrite = true)
            
            // 5. 同时备份wal和shm文件（如果存在）
            val walFile = File(currentDbFile.parent, "${DatabaseConstants.DATABASE_NAME}-wal")
            val shmFile = File(currentDbFile.parent, "${DatabaseConstants.DATABASE_NAME}-shm")
            
            if (walFile.exists()) {
                walFile.copyTo(File(backupDir, "$backupFileName-wal"), overwrite = true)
            }
            if (shmFile.exists()) {
                shmFile.copyTo(File(backupDir, "$backupFileName-shm"), overwrite = true)
            }
            
            // 6. 清理旧备份
            cleanOldBackups(backupDir)
            
            backupFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 恢复数据库备份
     * @param backupPath 备份文件路径
     * @return 是否恢复成功
     */
    suspend fun restoreBackup(backupPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupPath)
            if (!backupFile.exists()) {
                return@withContext false
            }
            
            // 1. 获取目标数据库文件
            val targetDbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
            
            // 2. 创建当前数据库的临时备份（以防恢复失败）
            val tempBackup = File(targetDbFile.parent, "${DatabaseConstants.DATABASE_NAME}.temp")
            if (targetDbFile.exists()) {
                targetDbFile.copyTo(tempBackup, overwrite = true)
            }
            
            try {
                // 3. 恢复备份文件
                backupFile.copyTo(targetDbFile, overwrite = true)
                
                // 4. 恢复wal和shm文件（如果存在）
                val walBackup = File(backupFile.parent, "${backupFile.name}-wal")
                val shmBackup = File(backupFile.parent, "${backupFile.name}-shm")
                
                if (walBackup.exists()) {
                    walBackup.copyTo(File(targetDbFile.parent, "${DatabaseConstants.DATABASE_NAME}-wal"), overwrite = true)
                }
                if (shmBackup.exists()) {
                    shmBackup.copyTo(File(targetDbFile.parent, "${DatabaseConstants.DATABASE_NAME}-shm"), overwrite = true)
                }
                
                // 5. 删除临时备份
                tempBackup.delete()
                
                true
            } catch (e: Exception) {
                // 恢复失败，还原临时备份
                if (tempBackup.exists()) {
                    tempBackup.copyTo(targetDbFile, overwrite = true)
                    tempBackup.delete()
                }
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // 以下方法暂时注释，等待重构完成
    /*
    /**
     * 获取所有备份文件
     * @return 备份文件列表，按时间倒序排列
     */
    suspend fun getBackupFiles(): List<BackupFile> = withContext(Dispatchers.IO) {
        try {
            val backupDir = getBackupDirectory() ?: return@withContext emptyList()
            if (!backupDir.exists()) {
                return@withContext emptyList()
            }
            
            backupDir.listFiles { file ->
                file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
            }?.map { file ->
                BackupFile(
                    path = file.absolutePath,
                    name = file.name,
                    size = file.length(),
                    createdAt = file.lastModified()
                )
            }?.sortedByDescending { it.createdAt } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    */
    
    /*
    /**
     * 在迁移前自动创建备份
     */
    suspend fun createMigrationBackup(): String? = withContext(Dispatchers.IO) {
        try {
            val backupDir = getBackupDirectory() ?: return@withContext null
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "${BACKUP_FILE_PREFIX}migration_$timestamp$BACKUP_FILE_EXTENSION"
            val backupFile = File(backupDir, backupFileName)
            
            val currentDbFile = context.getDatabasePath(DatabaseConstants.DATABASE_NAME)
            if (!currentDbFile.exists()) {
                return@withContext null
            }
            
            currentDbFile.copyTo(backupFile, overwrite = true)
            backupFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    */
    
    /**
     * 清理旧备份文件
     */
    private fun cleanOldBackups(backupDir: File) {
        try {
            val backupFiles = backupDir.listFiles { file ->
                file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
            }?.sortedByDescending { it.lastModified() } ?: return
            
            // 保留最新的MAX_BACKUP_COUNT个备份
            if (backupFiles.size > MAX_BACKUP_COUNT) {
                backupFiles.drop(MAX_BACKUP_COUNT).forEach { file ->
                    file.delete()
                    // 同时删除相关的wal和shm文件
                    File(file.parent, "${file.name}-wal").delete()
                    File(file.parent, "${file.name}-shm").delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取备份目录
     */
    private fun getBackupDirectory(): File? {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            // 优先使用外部存储
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR_NAME)
        } else {
            // 使用内部存储
            File(context.filesDir, BACKUP_DIR_NAME)
        }
    }
}
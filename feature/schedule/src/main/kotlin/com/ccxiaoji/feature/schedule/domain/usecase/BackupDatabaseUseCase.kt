package com.ccxiaoji.feature.schedule.domain.usecase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 备份数据库用例
 * 
 * 负责将数据库文件备份到指定位置
 */
class BackupDatabaseUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * 执行数据库备份
     * 
     * @param backupUri 备份文件的URI（如果为null，则备份到应用内部存储）
     * @return 备份文件路径
     */
    suspend operator fun invoke(backupUri: Uri? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 获取数据库文件路径
            val dbFile = context.getDatabasePath("schedule_database")
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("数据库文件不存在"))
            }
            
            // 确保数据库写入完成
            context.deleteDatabase("schedule_database-wal")
            context.deleteDatabase("schedule_database-shm")
            
            if (backupUri != null) {
                // 备份到用户指定的位置
                context.contentResolver.openOutputStream(backupUri)?.use { outputStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: return@withContext Result.failure(Exception("无法打开输出流"))
                
                Result.success(backupUri.toString())
            } else {
                // 备份到应用内部存储
                val backupDir = File(context.filesDir, "backups").apply { 
                    if (!exists()) mkdirs() 
                }
                
                val timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                )
                val backupFile = File(backupDir, "schedule_backup_$timestamp.db")
                
                FileInputStream(dbFile).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                Result.success(backupFile.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取备份文件列表
     * 
     * @return 备份文件列表
     */
    suspend fun getBackupFiles(): List<File> = withContext(Dispatchers.IO) {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return@withContext emptyList()
        
        backupDir.listFiles { file ->
            file.name.startsWith("schedule_backup_") && file.name.endsWith(".db")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * 删除旧备份文件（保留最近的N个）
     * 
     * @param keepCount 保留的备份数量
     */
    suspend fun deleteOldBackups(keepCount: Int = 5) = withContext(Dispatchers.IO) {
        val backupFiles = getBackupFiles()
        if (backupFiles.size > keepCount) {
            backupFiles.drop(keepCount).forEach { file ->
                file.delete()
            }
        }
    }
}
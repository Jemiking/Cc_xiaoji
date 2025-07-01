package com.ccxiaoji.shared.backup.api

import android.net.Uri
import com.ccxiaoji.common.data.import.BackupFile
import com.ccxiaoji.common.data.import.ImportConfig
import com.ccxiaoji.common.data.import.ImportResult
import com.ccxiaoji.common.data.import.ImportValidation

/**
 * 备份模块对外API接口
 */
interface BackupApi {
    /**
     * 创建数据库备份
     * @return 备份文件路径，如果失败返回null
     */
    suspend fun createBackup(): String?
    
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
    
    /**
     * 在迁移前自动创建备份
     * @return 备份文件路径，如果失败返回null
     */
    suspend fun createMigrationBackup(): String?
    
    /**
     * 验证导入文件
     * @param uri 文件URI
     * @return 验证结果
     */
    suspend fun validateImportFile(uri: Uri): ImportValidation
    
    /**
     * 导入JSON数据文件
     * @param uri 文件URI
     * @param config 导入配置
     * @return 导入结果
     */
    suspend fun importJsonFile(uri: Uri, config: ImportConfig = ImportConfig()): ImportResult
}
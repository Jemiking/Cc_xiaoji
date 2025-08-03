package com.ccxiaoji.app.data.backup.manager

import com.ccxiaoji.app.data.backup.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import android.net.Uri

/**
 * 备份管理器接口
 * 提供数据导入导出的统一入口
 */
interface BackupManager {
    /**
     * 导出数据到ZIP文件
     * @param outputFile 输出文件
     * @param dateRange 数据日期范围，null表示导出所有数据
     * @return 备份结果
     */
    suspend fun exportData(
        outputFile: File,
        dateRange: DataRange? = null
    ): BackupResult
    
    /**
     * 从ZIP文件导入数据
     * @param inputUri 输入文件URI
     * @param config 导入配置
     * @return 导入结果
     */
    suspend fun importData(
        inputUri: Uri,
        config: ImportConfig = ImportConfig()
    ): ImportResult
    
    /**
     * 验证备份文件
     * @param inputUri 输入文件URI
     * @return 验证结果，包含文件信息和潜在问题
     */
    suspend fun validateBackupFile(inputUri: Uri): ValidationResult
    
    /**
     * 获取导出进度
     * @return 进度流，0-100
     */
    fun getExportProgress(): Flow<Int>
    
    /**
     * 获取导入进度
     * @return 进度流，0-100
     */
    fun getImportProgress(): Flow<Int>
}

/**
 * 验证结果
 */
data class ValidationResult(
    val isValid: Boolean,
    val metadata: BackupMetadata? = null,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)
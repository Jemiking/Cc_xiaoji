package com.ccxiaoji.shared.backup.data

import android.net.Uri
import android.content.Context
import com.ccxiaoji.shared.backup.api.BackupApi
import com.ccxiaoji.shared.backup.data.manager.DatabaseBackupManager
import com.ccxiaoji.common.data.import.BackupFile
import com.ccxiaoji.common.data.import.ImportConfig
import com.ccxiaoji.common.data.import.ImportResult
import com.ccxiaoji.common.data.import.ImportValidation
import com.ccxiaoji.common.data.import.ImportError
import com.ccxiaoji.common.data.import.ImportData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupApi的实现类
 */
@Singleton
class BackupApiImpl @Inject constructor(
    @ApplicationContext private val context: Context,
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
    
    override suspend fun validateImportFile(uri: Uri): ImportValidation {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return ImportValidation(
                    isValid = false,
                    fileSize = 0,
                    dataModules = emptyList(),
                    errors = listOf("无法打开文件")
                )
            }
            
            val fileSize = inputStream.available().toLong()
            val jsonContent = inputStream.bufferedReader().use { it.readText() }
            
            // 尝试解析JSON
            val gson = Gson()
            val importData = gson.fromJson(jsonContent, ImportData::class.java)
            
            // 检查数据模块
            val dataModules = mutableListOf<com.ccxiaoji.common.data.import.DataModule>()
            importData.ledger?.accounts?.takeIf { it.isNotEmpty() }?.let { 
                dataModules.add(com.ccxiaoji.common.data.import.DataModule.ACCOUNTS) 
            }
            importData.ledger?.categories?.takeIf { it.isNotEmpty() }?.let { 
                dataModules.add(com.ccxiaoji.common.data.import.DataModule.CATEGORIES) 
            }
            importData.ledger?.transactions?.takeIf { it.isNotEmpty() }?.let { 
                dataModules.add(com.ccxiaoji.common.data.import.DataModule.TRANSACTIONS) 
            }
            importData.ledger?.budgets?.takeIf { it.isNotEmpty() }?.let { 
                dataModules.add(com.ccxiaoji.common.data.import.DataModule.BUDGETS) 
            }
            importData.tasks?.takeIf { it.isNotEmpty() }?.let { 
                dataModules.add(com.ccxiaoji.common.data.import.DataModule.TASKS) 
            }
            importData.habits?.habits?.takeIf { it.isNotEmpty() }?.let { 
                dataModules.add(com.ccxiaoji.common.data.import.DataModule.HABITS) 
            }
            
            ImportValidation(
                isValid = true,
                fileSize = fileSize,
                dataModules = dataModules,
                errors = emptyList()
            )
        } catch (e: Exception) {
            ImportValidation(
                isValid = false,
                fileSize = 0,
                dataModules = emptyList(),
                errors = listOf("文件解析失败: ${e.message}")
            )
        }
    }
    
    override suspend fun importJsonFile(uri: Uri, config: ImportConfig): ImportResult {
        // 注意：实际的导入功能应该由app模块的ImportManager处理
        // 这里只是提供一个占位实现，真正的导入逻辑在app模块中
        return ImportResult(
            success = false,
            totalItems = 0,
            importedItems = 0,
            skippedItems = 0,
            errors = listOf(ImportError("请使用app模块的ImportManager进行导入", "REDIRECT_TO_APP_MODULE")),
            moduleResults = emptyMap()
        )
    }
}
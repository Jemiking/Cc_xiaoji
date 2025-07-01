package com.ccxiaoji.app.data.importer

import android.content.Context
import android.net.Uri
import com.ccxiaoji.common.data.import.*
import com.ccxiaoji.shared.backup.api.BackupApi
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据导入服务 - App模块的完整导入解决方案
 * 整合文件验证和数据导入功能
 */
@Singleton
class ImportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importManager: ImportManager,
    private val backupApi: BackupApi
) {
    
    /**
     * 从URI导入JSON数据文件
     * @param uri 文件URI
     * @param config 导入配置
     * @return 导入结果
     */
    suspend fun importFromUri(
        uri: Uri, 
        config: ImportConfig = ImportConfig()
    ): ImportResult = withContext(Dispatchers.IO) {
        
        try {
            // 1. 验证文件
            val validation = backupApi.validateImportFile(uri)
            if (!validation.isValid) {
                return@withContext ImportResult(
                    success = false,
                    totalItems = 0,
                    importedItems = 0,
                    skippedItems = 0,
                    errors = validation.errors.map { ImportError(it, "") },
                    moduleResults = emptyMap()
                )
            }
            
            // 2. 读取并解析JSON文件
            val importData = parseJsonFile(uri)
            if (importData == null) {
                return@withContext ImportResult(
                    success = false,
                    totalItems = 0,
                    importedItems = 0,
                    skippedItems = 0,
                    errors = listOf(ImportError("文件解析失败", "")),
                    moduleResults = emptyMap()
                )
            }
            
            // 3. 执行导入
            return@withContext importManager.importData(importData, config.skipExisting)
            
        } catch (e: Exception) {
            return@withContext ImportResult(
                success = false,
                totalItems = 0,
                importedItems = 0,
                skippedItems = 0,
                errors = listOf(ImportError("导入服务异常: ${e.message}", "")),
                moduleResults = emptyMap()
            )
        }
    }
    
    /**
     * 验证导入文件
     * @param uri 文件URI
     * @return 验证结果
     */
    suspend fun validateFile(uri: Uri): ImportValidation {
        return backupApi.validateImportFile(uri)
    }
    
    /**
     * 解析JSON文件
     * @param uri 文件URI
     * @return 解析后的导入数据，失败返回null
     */
    private suspend fun parseJsonFile(uri: Uri): ImportData? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return@withContext null
            }
            
            val jsonContent = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            return@withContext gson.fromJson(jsonContent, ImportData::class.java)
        } catch (e: Exception) {
            return@withContext null
        }
    }
    
    /**
     * 获取支持的数据模块列表
     * @return 支持导入的数据模块
     */
    fun getSupportedModules(): List<DataModule> {
        return listOf(
            DataModule.ACCOUNTS,
            DataModule.CATEGORIES,
            DataModule.TRANSACTIONS,
            DataModule.BUDGETS,
            DataModule.TASKS,
            DataModule.HABITS
        )
    }
}
package com.ccxiaoji.app.data.backup.importer

import com.ccxiaoji.app.data.backup.export.ModuleType
import com.ccxiaoji.app.data.backup.model.ImportMode
import com.google.gson.JsonObject
import java.io.InputStream

/**
 * JSON导入器接口
 */
interface JsonImporter {
    /**
     * 从JSON导入指定模块的数据
     * @param moduleType 模块类型
     * @param inputStream 输入流
     * @param mode 导入模式
     * @return 导入统计信息
     */
    suspend fun importModule(
        moduleType: ModuleType,
        inputStream: InputStream,
        mode: ImportMode = ImportMode.APPEND
    ): JsonImportStats
    
    /**
     * 从JSON对象导入数据
     * @param moduleType 模块类型
     * @param jsonObject JSON对象
     * @param mode 导入模式
     * @return 导入统计信息
     */
    suspend fun importFromJson(
        moduleType: ModuleType,
        jsonObject: JsonObject,
        mode: ImportMode = ImportMode.APPEND
    ): JsonImportStats
}

/**
 * JSON导入统计信息
 */
data class JsonImportStats(
    val module: ModuleType,
    val totalItems: Int,
    val importedItems: Int,
    val skippedItems: Int = 0,
    val errors: List<String> = emptyList()
)
package com.ccxiaoji.app.data.backup.importer

import com.ccxiaoji.app.data.backup.export.ModuleType
import com.ccxiaoji.app.data.backup.model.ImportMode
import com.google.gson.JsonObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JSON导入器实现
 */
@Singleton
class JsonImporterImpl @Inject constructor() : JsonImporter {
    
    override suspend fun importModule(
        moduleType: ModuleType,
        inputStream: InputStream,
        mode: ImportMode
    ): JsonImportStats {
        // TODO: 实现JSON导入逻辑
        return JsonImportStats(
            module = moduleType,
            totalItems = 0,
            importedItems = 0
        )
    }
    
    override suspend fun importFromJson(
        moduleType: ModuleType,
        jsonObject: JsonObject,
        mode: ImportMode
    ): JsonImportStats {
        // TODO: 实现从JSON对象导入的逻辑
        return JsonImportStats(
            module = moduleType,
            totalItems = 0,
            importedItems = 0
        )
    }
}
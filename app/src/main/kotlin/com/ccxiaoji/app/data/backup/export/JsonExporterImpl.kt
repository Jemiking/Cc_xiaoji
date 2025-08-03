package com.ccxiaoji.app.data.backup.export

import com.ccxiaoji.app.data.backup.model.DataRange
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JSON导出器实现
 */
@Singleton
class JsonExporterImpl @Inject constructor() : JsonExporter {
    
    override suspend fun exportModule(
        moduleType: ModuleType,
        dateRange: DataRange?
    ): JsonObject {
        // TODO: 实现JSON导出逻辑
        return JsonObject()
    }
}
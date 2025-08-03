package com.ccxiaoji.app.data.backup.export

import com.ccxiaoji.app.data.backup.model.DataRange
import com.google.gson.JsonObject

/**
 * JSON导出器接口
 */
interface JsonExporter {
    /**
     * 导出指定模块的数据到JSON
     * @param moduleType 模块类型
     * @param dateRange 数据日期范围
     * @return JSON对象
     */
    suspend fun exportModule(
        moduleType: ModuleType,
        dateRange: DataRange? = null
    ): JsonObject
}

/**
 * 模块类型枚举
 */
enum class ModuleType(val fileName: String) {
    LEDGER_MASTER("ledger_master.json"),
    TODO("todo.json"),
    HABIT("habit.json"),
    SCHEDULE("schedule.json"),
    PLAN("plan.json"),
    OTHERS("others.json")
}
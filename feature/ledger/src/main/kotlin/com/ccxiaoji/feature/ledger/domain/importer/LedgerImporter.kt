package com.ccxiaoji.feature.ledger.domain.importer

import java.io.File

/**
 * 记账数据导入器接口
 * 支持多种格式的数据导入
 */
interface LedgerImporter {
    /**
     * 导入数据
     * @param file 要导入的文件
     * @param config 导入配置
     * @return 导入结果
     */
    suspend fun importData(file: File, config: ImportConfig = ImportConfig()): ImportResult
    
    /**
     * 预览导入数据
     * @param file 要导入的文件
     * @return 导入预览信息
     */
    suspend fun previewImport(file: File): ImportPreview
    
    /**
     * 验证文件格式
     * @param file 要验证的文件
     * @return 是否为支持的格式
     */
    suspend fun validateFile(file: File): Boolean
}
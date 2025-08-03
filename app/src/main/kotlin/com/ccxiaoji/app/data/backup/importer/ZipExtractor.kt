package com.ccxiaoji.app.data.backup.importer

import java.io.File
import java.io.InputStream

/**
 * ZIP解压器接口
 */
interface ZipExtractor {
    /**
     * 从ZIP文件中解压文件
     * @param inputStream ZIP输入流
     * @param outputDir 输出目录
     * @param onProgress 进度回调
     * @return 解压的文件列表
     */
    suspend fun extractFiles(
        inputStream: InputStream,
        outputDir: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): List<File>
    
    /**
     * 获取ZIP文件中的文件列表
     * @param inputStream ZIP输入流
     * @return 文件名列表
     */
    suspend fun listFiles(inputStream: InputStream): List<String>
    
    /**
     * 从ZIP中读取指定文件
     * @param inputStream ZIP输入流
     * @param fileName 文件名
     * @return 文件内容的输入流
     */
    suspend fun readFile(
        inputStream: InputStream,
        fileName: String
    ): InputStream?
}
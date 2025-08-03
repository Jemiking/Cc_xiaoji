package com.ccxiaoji.app.data.backup.export

import java.io.File
import java.io.OutputStream

/**
 * ZIP打包器接口
 */
interface ZipPackager {
    /**
     * 将多个文件打包成ZIP
     * @param files 要打包的文件列表
     * @param outputStream 输出流
     * @param onProgress 进度回调
     */
    suspend fun packageFiles(
        files: List<File>,
        outputStream: OutputStream,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    )
    
    /**
     * 添加文件到ZIP
     * @param fileName 文件名
     * @param content 文件内容
     * @param outputStream ZIP输出流
     */
    suspend fun addFileToZip(
        fileName: String,
        content: ByteArray,
        outputStream: OutputStream
    )
}
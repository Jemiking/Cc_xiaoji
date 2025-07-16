package com.ccxiaoji.app.data.excel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Xml
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 简化的XLSX读取器
 * 作为Apache POI失败时的降级方案
 * 
 * 原理：
 * - XLSX文件本质上是包含XML文件的ZIP压缩包
 * - 使用Android原生的ZIP和XML解析器
 * - 只实现基础功能，不支持复杂格式
 */
@Singleton
class SimpleXlsxReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "SimpleXlsxReader"
        private const val SHEET_PATH_PREFIX = "xl/worksheets/sheet"
        private const val SHARED_STRINGS_PATH = "xl/sharedStrings.xml"
        private const val WORKBOOK_PATH = "xl/workbook.xml"
    }
    
    /**
     * 判断是否可以处理该文件
     */
    fun canHandle(uri: Uri): Boolean {
        return try {
            val fileName = getFileName(uri).lowercase()
            fileName.endsWith(".xlsx")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 读取XLSX文件的基础数据
     * @return 工作表名称到数据的映射
     */
    suspend fun readBasicData(uri: Uri): Result<Map<String, List<List<String>>>> = withContext(Dispatchers.IO) {
        try {
            val result = mutableMapOf<String, List<List<String>>>()
            val sharedStrings = mutableListOf<String>()
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    
                    // 第一遍：读取共享字符串
                    while (entry != null) {
                        if (entry.name == SHARED_STRINGS_PATH) {
                            parseSharedStrings(zip, sharedStrings)
                            break
                        }
                        entry = zip.nextEntry
                    }
                    
                    // 重新打开流读取工作表
                    context.contentResolver.openInputStream(uri)?.use { newInputStream ->
                        ZipInputStream(newInputStream).use { newZip ->
                            var newEntry = newZip.nextEntry
                            var sheetIndex = 1
                            
                            while (newEntry != null) {
                                if (newEntry.name.startsWith(SHEET_PATH_PREFIX) && newEntry.name.endsWith(".xml")) {
                                    val sheetName = "Sheet$sheetIndex"
                                    val sheetData = parseSheet(newZip, sharedStrings)
                                    if (sheetData.isNotEmpty()) {
                                        result[sheetName] = sheetData
                                        sheetIndex++
                                    }
                                }
                                newEntry = newZip.nextEntry
                            }
                        }
                    }
                }
            } ?: return@withContext Result.failure(Exception("无法打开文件"))
            
            if (result.isEmpty()) {
                Result.failure(Exception("文件中没有找到有效的工作表"))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取XLSX失败", e)
            Result.failure(Exception("读取Excel文件失败: ${e.message}"))
        }
    }
    
    /**
     * 解析共享字符串
     */
    private fun parseSharedStrings(input: InputStream, sharedStrings: MutableList<String>) {
        try {
            val parser = Xml.newPullParser()
            parser.setInput(input, "UTF-8")
            
            var eventType = parser.eventType
            var insideSi = false
            val currentString = StringBuilder()
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "si" -> {
                                insideSi = true
                                currentString.clear()
                            }
                            "t" -> {
                                if (insideSi) {
                                    currentString.append(parser.nextText())
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "si" && insideSi) {
                            sharedStrings.add(currentString.toString())
                            insideSi = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析共享字符串失败", e)
        }
    }
    
    /**
     * 解析工作表数据
     */
    private fun parseSheet(input: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<MutableList<String>>()
        
        try {
            val parser = Xml.newPullParser()
            parser.setInput(input, "UTF-8")
            
            var eventType = parser.eventType
            var currentRow: MutableList<String>? = null
            var currentCellValue: String? = null
            var currentCellType: String? = null
            var currentRowIndex = -1
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "row" -> {
                                val rowNum = parser.getAttributeValue(null, "r")?.toIntOrNull() ?: 0
                                currentRowIndex = rowNum - 1
                                currentRow = mutableListOf()
                            }
                            "c" -> {
                                currentCellType = parser.getAttributeValue(null, "t")
                            }
                            "v" -> {
                                val value = parser.nextText()
                                currentCellValue = when (currentCellType) {
                                    "s" -> {
                                        // 共享字符串索引
                                        val index = value.toIntOrNull() ?: 0
                                        sharedStrings.getOrNull(index) ?: ""
                                    }
                                    "str" -> value // 内联字符串
                                    else -> value // 数字或其他
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "c" -> {
                                currentRow?.add(currentCellValue ?: "")
                                currentCellValue = null
                                currentCellType = null
                            }
                            "row" -> {
                                currentRow?.let { row ->
                                    // 确保行索引正确
                                    while (rows.size <= currentRowIndex) {
                                        rows.add(mutableListOf())
                                    }
                                    if (currentRowIndex >= 0) {
                                        rows[currentRowIndex] = row
                                    }
                                }
                                currentRow = null
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析工作表失败", e)
        }
        
        // 转换为不可变列表
        return rows.map { it.toList() }
    }
    
    /**
     * 获取文件名
     */
    private fun getFileName(uri: Uri): String {
        var fileName = "未知文件"
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) ?: fileName
            }
        }
        
        return fileName
    }
    
    /**
     * 读取简单的Excel文件结构信息
     */
    suspend fun analyzeSimpleStructure(uri: Uri): Result<ExcelFileStructure> = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(uri)
            val sheets = mutableListOf<SheetInfo>()
            var totalRows = 0
            
            // 读取基础数据
            val data = readBasicData(uri).getOrThrow()
            
            data.forEach { (sheetName, rows) ->
                val rowCount = rows.size
                val columnCount = rows.maxOfOrNull { it.size } ?: 0
                val headers = rows.firstOrNull() ?: emptyList()
                val dataPreview = rows.drop(1).take(10)
                
                sheets.add(
                    SheetInfo(
                        name = sheetName,
                        rowCount = rowCount,
                        columnCount = columnCount,
                        headers = headers,
                        dataPreview = dataPreview
                    )
                )
                
                totalRows += rowCount
            }
            
            Result.success(
                ExcelFileStructure(
                    fileName = fileName,
                    sheets = sheets,
                    totalRows = totalRows,
                    fileSize = 0 // 简化版不计算文件大小
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
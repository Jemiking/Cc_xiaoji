package com.ccxiaoji.app.data.excel

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import com.ccxiaoji.app.data.importer.ImportExportError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.BufferedInputStream
import java.io.InputStream
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class ExcelFileStructure(
    val fileName: String,
    val sheets: List<SheetInfo>,
    val totalRows: Int,
    val fileSize: Long = 0
)

data class SheetInfo(
    val name: String,
    val rowCount: Int,
    val columnCount: Int,
    val headers: List<String> = emptyList(),
    val dataPreview: List<List<String>> = emptyList()
)

data class ExcelCell(
    val value: Any?,
    val type: Int, // POI 3.17 使用int类型的CellType
    val formattedValue: String
)

@Singleton
class ExcelReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val PREVIEW_ROW_COUNT = 10
        const val MAX_COLUMN_COUNT = 50
    }
    
    suspend fun analyzeExcelFile(uri: Uri): Result<ExcelFileStructure> = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(uri)
            var totalRows = 0
            val sheets = mutableListOf<SheetInfo>()
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = createWorkbook(inputStream)
                
                for (i in 0 until workbook.numberOfSheets) {
                    val sheet = workbook.getSheetAt(i)
                    val sheetInfo = analyzeSheet(sheet)
                    sheets.add(sheetInfo)
                    totalRows += sheetInfo.rowCount
                }
                
                workbook.close()
            } ?: return@withContext Result.failure(
                Exception("无法打开文件")
            )
            
            val fileSize = getFileSize(uri)
            
            Result.success(
                ExcelFileStructure(
                    fileName = fileName,
                    sheets = sheets,
                    totalRows = totalRows,
                    fileSize = fileSize
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun readSheet(
        uri: Uri, 
        sheetName: String,
        rowOffset: Int = 0,
        rowLimit: Int = Int.MAX_VALUE
    ): Result<SheetData> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = createWorkbook(inputStream)
                val sheet = workbook.getSheet(sheetName)
                    ?: return@withContext Result.failure(
                        Exception("找不到工作表: $sheetName")
                    )
                
                val sheetData = readSheetData(sheet, rowOffset, rowLimit)
                workbook.close()
                
                Result.success(sheetData)
            } ?: Result.failure(Exception("无法打开文件"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun readAllSheets(uri: Uri): Result<Map<String, SheetData>> = withContext(Dispatchers.IO) {
        try {
            val allSheets = mutableMapOf<String, SheetData>()
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = createWorkbook(inputStream)
                
                for (i in 0 until workbook.numberOfSheets) {
                    val sheet = workbook.getSheetAt(i)
                    val sheetData = readSheetData(sheet)
                    allSheets[sheet.sheetName] = sheetData
                }
                
                workbook.close()
            } ?: return@withContext Result.failure(
                Exception("无法打开文件")
            )
            
            Result.success(allSheets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createWorkbook(inputStream: InputStream): Workbook {
        // POI 3.17需要BufferedInputStream来支持mark/reset
        val bufferedInput = BufferedInputStream(inputStream, 8192)
        
        return try {
            // 尝试使用WorkbookFactory（POI 3.17也支持）
            WorkbookFactory.create(bufferedInput)
        } catch (e: Exception) {
            Log.e("ExcelReader", "WorkbookFactory失败，尝试手动检测格式", e)
            // 降级方案：手动检测文件格式
            bufferedInput.reset()
            
            if (POIFSFileSystem.hasPOIFSHeader(bufferedInput)) {
                // Excel 97-2003格式
                HSSFWorkbook(bufferedInput)
            } else {
                // Excel 2007+格式
                XSSFWorkbook(bufferedInput)
            }
        }
    }
    
    private fun analyzeSheet(sheet: Sheet): SheetInfo {
        val rowCount = sheet.lastRowNum + 1
        val firstRow = sheet.getRow(0)
        val columnCount = firstRow?.lastCellNum?.toInt() ?: 0
        
        val headers = if (firstRow != null) {
            readRowAsStrings(firstRow).take(MAX_COLUMN_COUNT)
        } else {
            emptyList()
        }
        
        val dataPreview = mutableListOf<List<String>>()
        val previewEndRow = minOf(PREVIEW_ROW_COUNT, rowCount - 1)
        
        for (rowNum in 1..previewEndRow) {
            sheet.getRow(rowNum)?.let { row ->
                dataPreview.add(readRowAsStrings(row).take(MAX_COLUMN_COUNT))
            }
        }
        
        return SheetInfo(
            name = sheet.sheetName,
            rowCount = rowCount,
            columnCount = columnCount,
            headers = headers,
            dataPreview = dataPreview
        )
    }
    
    private fun readSheetData(
        sheet: Sheet,
        rowOffset: Int = 0,
        rowLimit: Int = Int.MAX_VALUE
    ): SheetData {
        val headers = sheet.getRow(0)?.let { readRowAsStrings(it) } ?: emptyList()
        val rows = mutableListOf<List<ExcelCell>>()
        
        val startRow = maxOf(1, rowOffset)
        val endRow = minOf(sheet.lastRowNum, startRow + rowLimit - 1)
        
        for (rowNum in startRow..endRow) {
            sheet.getRow(rowNum)?.let { row ->
                rows.add(readRowCells(row))
            }
        }
        
        return SheetData(
            sheetName = sheet.sheetName,
            headers = headers,
            rows = rows,
            totalRows = sheet.lastRowNum + 1
        )
    }
    
    private fun readRowAsStrings(row: Row): List<String> {
        val cells = mutableListOf<String>()
        val cellCount = row.lastCellNum
        
        for (i in 0 until cellCount) {
            val cell = row.getCell(i)
            cells.add(getCellValueAsString(cell))
        }
        
        return cells
    }
    
    private fun readRowCells(row: Row): List<ExcelCell> {
        val cells = mutableListOf<ExcelCell>()
        val cellCount = row.lastCellNum
        
        for (i in 0 until cellCount) {
            val cell = row.getCell(i)
            cells.add(readCell(cell))
        }
        
        return cells
    }
    
    private fun readCell(cell: Cell?): ExcelCell {
        if (cell == null) {
            return ExcelCell(
                value = null,
                type = Cell.CELL_TYPE_BLANK,
                formattedValue = ""
            )
        }
        
        val cellType = cell.cellType
        val value = when (cellType) {
            Cell.CELL_TYPE_STRING -> cell.stringCellValue
            Cell.CELL_TYPE_NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue
                } else {
                    cell.numericCellValue
                }
            }
            Cell.CELL_TYPE_BOOLEAN -> cell.booleanCellValue
            Cell.CELL_TYPE_FORMULA -> {
                try {
                    when (cell.cachedFormulaResultType) {
                        Cell.CELL_TYPE_STRING -> cell.stringCellValue
                        Cell.CELL_TYPE_NUMERIC -> cell.numericCellValue
                        Cell.CELL_TYPE_BOOLEAN -> cell.booleanCellValue
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
        
        return ExcelCell(
            value = value,
            type = cellType,
            formattedValue = getCellValueAsString(cell)
        )
    }
    
    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        
        return when (cell.cellType) {
            Cell.CELL_TYPE_STRING -> cell.stringCellValue
            Cell.CELL_TYPE_NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(cell.dateCellValue)
                } else {
                    val value = cell.numericCellValue
                    if (value == value.toLong().toDouble()) {
                        value.toLong().toString()
                    } else {
                        String.format(Locale.getDefault(), "%.2f", value)
                    }
                }
            }
            Cell.CELL_TYPE_BOOLEAN -> cell.booleanCellValue.toString()
            Cell.CELL_TYPE_FORMULA -> {
                try {
                    when (cell.cachedFormulaResultType) {
                        Cell.CELL_TYPE_STRING -> cell.stringCellValue
                        Cell.CELL_TYPE_NUMERIC -> {
                            val value = cell.numericCellValue
                            if (value == value.toLong().toDouble()) {
                                value.toLong().toString()
                            } else {
                                String.format(Locale.getDefault(), "%.2f", value)
                            }
                        }
                        Cell.CELL_TYPE_BOOLEAN -> cell.booleanCellValue.toString()
                        else -> ""
                    }
                } catch (e: Exception) {
                    ""
                }
            }
            else -> ""
        }
    }
    
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
    
    private fun getFileSize(uri: Uri): Long {
        var size = 0L
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                size = cursor.getLong(sizeIndex)
            }
        }
        
        return size
    }
}

data class SheetData(
    val sheetName: String,
    val headers: List<String>,
    val rows: List<List<ExcelCell>>,
    val totalRows: Int
)
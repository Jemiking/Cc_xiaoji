package com.ccxiaoji.app.data.excel

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
 * Excel样式管理器
 * 用于统一管理Excel导出时的样式
 */
class ExcelStyleManager(private val workbook: XSSFWorkbook) {
    
    /**
     * 创建标题行样式
     */
    fun createHeaderStyle(): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        
        // 背景色
        style.fillForegroundColor = IndexedColors.LIGHT_BLUE.index
        style.fillPattern = CellStyle.SOLID_FOREGROUND
        
        // 字体
        val font = workbook.createFont()
        font.boldweight = Font.BOLDWEIGHT_BOLD
        font.color = IndexedColors.WHITE.index
        font.fontHeightInPoints = 12
        style.setFont(font)
        
        // 对齐
        style.alignment = CellStyle.ALIGN_CENTER
        style.verticalAlignment = CellStyle.VERTICAL_CENTER
        
        // 边框
        style.borderBottom = CellStyle.BORDER_THIN
        style.borderLeft = CellStyle.BORDER_THIN
        style.borderRight = CellStyle.BORDER_THIN
        style.borderTop = CellStyle.BORDER_THIN
        
        // 自动换行
        style.wrapText = true
        
        return style
    }
    
    /**
     * 创建数据单元格样式
     */
    fun createDataStyle(): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        
        // 边框
        style.borderBottom = CellStyle.BORDER_THIN
        style.borderLeft = CellStyle.BORDER_THIN
        style.borderRight = CellStyle.BORDER_THIN
        style.borderTop = CellStyle.BORDER_THIN
        
        // 对齐
        style.verticalAlignment = CellStyle.VERTICAL_CENTER
        
        return style
    }
    
    /**
     * 创建数字单元格样式
     */
    fun createNumberStyle(): XSSFCellStyle {
        val style = createDataStyle()
        // 数字格式，保留两位小数
        style.dataFormat = workbook.createDataFormat().getFormat("#,##0.00")
        style.alignment = CellStyle.ALIGN_RIGHT
        return style
    }
    
    /**
     * 创建日期单元格样式
     */
    fun createDateStyle(): XSSFCellStyle {
        val style = createDataStyle()
        style.dataFormat = workbook.createDataFormat().getFormat("yyyy-MM-dd")
        style.alignment = CellStyle.ALIGN_CENTER
        return style
    }
    
    /**
     * 创建时间单元格样式
     */
    fun createTimeStyle(): XSSFCellStyle {
        val style = createDataStyle()
        style.dataFormat = workbook.createDataFormat().getFormat("HH:mm:ss")
        style.alignment = CellStyle.ALIGN_CENTER
        return style
    }
    
    /**
     * 创建期初余额行样式
     */
    fun createInitialBalanceStyle(): XSSFCellStyle {
        val style = createDataStyle()
        
        // 淡灰色背景
        style.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        style.fillPattern = CellStyle.SOLID_FOREGROUND
        
        // 斜体字
        val font = workbook.createFont()
        font.italic = true
        style.setFont(font)
        
        return style
    }
    
    /**
     * 创建收入样式（绿色文字）
     */
    fun createIncomeStyle(): XSSFCellStyle {
        val style = createNumberStyle()
        val font = workbook.createFont()
        font.color = IndexedColors.GREEN.index
        style.setFont(font)
        return style
    }
    
    /**
     * 创建支出样式（红色文字）
     */
    fun createExpenseStyle(): XSSFCellStyle {
        val style = createNumberStyle()
        val font = workbook.createFont()
        font.color = IndexedColors.RED.index
        style.setFont(font)
        return style
    }
    
    /**
     * 创建总计行样式
     */
    fun createTotalStyle(): XSSFCellStyle {
        val style = createDataStyle()
        
        // 背景色
        style.fillForegroundColor = IndexedColors.LIGHT_YELLOW.index
        style.fillPattern = CellStyle.SOLID_FOREGROUND
        
        // 加粗字体
        val font = workbook.createFont()
        font.boldweight = Font.BOLDWEIGHT_BOLD
        style.setFont(font)
        
        return style
    }
    
    /**
     * 应用列宽设置
     */
    fun applyColumnWidths(sheet: Sheet, vararg widths: Int) {
        widths.forEachIndexed { index, width ->
            sheet.setColumnWidth(index, width * 256) // POI使用1/256字符宽度为单位
        }
    }
    
    /**
     * 冻结窗格（固定标题行）
     */
    fun freezeHeader(sheet: Sheet, rowCount: Int = 1) {
        sheet.createFreezePane(0, rowCount)
    }
}
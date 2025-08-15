package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.export.ExportConfig
import com.ccxiaoji.feature.ledger.domain.export.ExportFormat
import com.ccxiaoji.feature.ledger.domain.export.LedgerExporter
import com.ccxiaoji.feature.ledger.data.export.DataDebugHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ledgerExporter: LedgerExporter,
    private val dataDebugHelper: DataDebugHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()
    
    /**
     * 设置日期范围
     */
    fun setDateRange(startDate: LocalDate?, endDate: LocalDate?) {
        _uiState.update { state ->
            state.copy(
                startDate = startDate,
                endDate = endDate
            )
        }
    }
    
    /**
     * 切换数据类型选择
     */
    fun toggleDataType(dataType: DataType) {
        _uiState.update { state ->
            val newSelectedTypes = if (dataType in state.selectedDataTypes) {
                state.selectedDataTypes - dataType
            } else {
                state.selectedDataTypes + dataType
            }
            state.copy(selectedDataTypes = newSelectedTypes)
        }
    }
    
    /**
     * 设置导出格式
     */
    fun setExportFormat(format: ExportFormat) {
        _uiState.update { state ->
            state.copy(exportFormat = format)
        }
    }
    
    /**
     * 执行导出
     */
    fun export() {
        if (_uiState.value.selectedDataTypes.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请至少选择一种数据类型") }
            return
        }
        
        _uiState.update { it.copy(isExporting = true, errorMessage = null) }
        
        viewModelScope.launch {
            try {
                // 调试：在导出前先检查数据
                dataDebugHelper.debugTransactionData()
                val state = _uiState.value
                val config = ExportConfig(
                    includeTransactions = DataType.TRANSACTIONS in state.selectedDataTypes,
                    includeAccounts = DataType.ACCOUNTS in state.selectedDataTypes,
                    includeCategories = DataType.CATEGORIES in state.selectedDataTypes,
                    includeBudgets = DataType.BUDGETS in state.selectedDataTypes,
                    includeRecurringTransactions = DataType.RECURRING in state.selectedDataTypes,
                    includeSavingsGoals = DataType.SAVINGS in state.selectedDataTypes,
                    startDate = state.startDate?.toEpochMillis(),
                    endDate = state.endDate?.toEpochMillis(),
                    format = state.exportFormat
                )
                
                // 使用新的单文件CSV格式导出
                val file = ledgerExporter.exportAll(config)
                
                _uiState.update { 
                    it.copy(
                        isExporting = false,
                        exportedFile = file,
                        successMessage = "导出成功"
                    )
                }
                
                // 自动分享文件
                shareFile(file)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isExporting = false,
                        errorMessage = "导出失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 分享导出的文件
     */
    private fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when {
                    file.extension == "zip" -> "application/zip"
                    file.extension == "csv" -> "text/csv"
                    file.extension == "json" -> "application/json"
                    else -> "*/*"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooserIntent = Intent.createChooser(intent, "分享导出文件")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(errorMessage = "分享失败：${e.message}")
            }
        }
    }
    
    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}

/**
 * 导出界面状态
 */
data class ExportUiState(
    val selectedDataTypes: Set<DataType> = setOf(DataType.TRANSACTIONS),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val exportFormat: ExportFormat = ExportFormat.CSV,
    val isExporting: Boolean = false,
    val exportedFile: File? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * 数据类型
 */
enum class DataType(val displayName: String) {
    TRANSACTIONS("交易记录"),
    ACCOUNTS("账户信息"),
    CATEGORIES("分类信息"),
    BUDGETS("预算信息"),
    RECURRING("定期交易"),
    SAVINGS("储蓄目标"),
    CREDITBILLS("信用卡账单")
}

/**
 * LocalDate扩展函数：转换为时间戳
 */
private fun LocalDate.toEpochMillis(): Long {
    return this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}
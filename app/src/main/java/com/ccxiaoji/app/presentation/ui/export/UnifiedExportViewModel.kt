package com.ccxiaoji.app.presentation.ui.export

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.presentation.ui.export.adapter.LedgerExportAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 统一数据导出视图模型
 */
@HiltViewModel
class UnifiedExportViewModel @Inject constructor(
    private val ledgerAdapter: LedgerExportAdapter
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UnifiedExportUiState())
    val uiState: StateFlow<UnifiedExportUiState> = _uiState.asStateFlow()
    
    init {
        loadModuleStats()
    }
    
    /**
     * 加载各模块统计信息
     */
    fun loadModuleStats() {
        viewModelScope.launch {
            try {
                // 加载记账模块统计
                val ledgerStats = ledgerAdapter.getStatistics()
                
                // 更新UI状态
                _uiState.update { state ->
                    state.copy(
                        moduleStats = mapOf(
                            ExportModule.LEDGER to ledgerStats,
                            ExportModule.TODO to ModuleStats(0, null, "-"),
                            ExportModule.HABIT to ModuleStats(0, null, "-"),
                            ExportModule.SCHEDULE to ModuleStats(0, null, "-"),
                            ExportModule.PLAN to ModuleStats(0, null, "-")
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "加载统计信息失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 切换模块选择状态
     */
    fun toggleModule(module: ExportModule) {
        if (!module.isAvailable) {
            _uiState.update { it.copy(error = "${module.displayName}功能正在开发中") }
            return
        }
        
        _uiState.update { state ->
            val newSelectedModules = if (module in state.selectedModules) {
                state.selectedModules - module
            } else {
                state.selectedModules + module
            }
            state.copy(
                selectedModules = newSelectedModules,
                error = null
            )
        }
    }
    
    /**
     * 设置日期范围
     */
    fun setDateRange(dateRange: DateRange) {
        _uiState.update { it.copy(dateRange = dateRange) }
    }
    
    /**
     * 设置导出格式
     */
    fun setExportFormat(format: ExportFormat) {
        _uiState.update { it.copy(exportFormat = format) }
    }
    
    /**
     * 执行导出
     */
    fun exportData() {
        val selectedModules = _uiState.value.selectedModules
        
        if (selectedModules.isEmpty()) {
            _uiState.update { it.copy(error = "请至少选择一个模块") }
            return
        }
        
        _uiState.update { it.copy(isExporting = true, error = null) }
        
        viewModelScope.launch {
            try {
                var exportedFile: File? = null
                
                // 目前只处理记账模块
                if (ExportModule.LEDGER in selectedModules) {
                    updateProgress(0f, "正在导出记账数据...")
                    
                    exportedFile = ledgerAdapter.exportData(
                        _uiState.value.dateRange,
                        _uiState.value.exportFormat
                    )
                    
                    updateProgress(1f, "记账数据导出完成")
                }
                
                // 其他模块暂不处理
                if (selectedModules.any { it != ExportModule.LEDGER }) {
                    // 如果选择了其他模块，可以在这里提示
                }
                
                if (exportedFile != null) {
                    _uiState.update { 
                        it.copy(
                            isExporting = false,
                            exportProgress = 0f,
                            currentExportingModule = null,
                            exportResult = ExportResult(
                                success = true,
                                file = exportedFile,
                                message = "导出成功"
                            )
                        )
                    }
                } else {
                    throw IllegalStateException("没有可导出的数据")
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isExporting = false,
                        exportProgress = 0f,
                        currentExportingModule = null,
                        error = "导出失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 更新导出进度
     */
    private fun updateProgress(progress: Float, module: String) {
        _uiState.update { 
            it.copy(
                exportProgress = progress,
                currentExportingModule = module
            )
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 清除导出结果
     */
    fun clearExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }
}

/**
 * 导出模块枚举
 */
enum class ExportModule(
    val displayName: String,
    val icon: ImageVector,
    val isAvailable: Boolean,
    val description: String
) {
    LEDGER("记账", Icons.Outlined.AccountBalance, true, "账户、交易、预算等"),
    TODO("待办", Icons.Outlined.Task, false, "任务、分组、标签"),
    HABIT("习惯", Icons.Outlined.Loop, false, "习惯、打卡记录"),
    SCHEDULE("排班", Icons.Outlined.Schedule, false, "班次、排班记录"),
    PLAN("计划", Icons.AutoMirrored.Outlined.Assignment, false, "计划、里程碑")
}

/**
 * 日期范围枚举
 */
enum class DateRange(val displayName: String) {
    ALL("全部数据"),
    THIS_MONTH("本月"),
    THIS_YEAR("今年"),
    CUSTOM("自定义范围")
}

/**
 * 导出格式枚举
 */
enum class ExportFormat(val displayName: String, val extension: String) {
    CSV("CSV文件", "csv"),
    JSON("JSON文件", "json"),
    EXCEL("Excel文件", "xlsx")
}

/**
 * 统一导出界面状态
 */
data class UnifiedExportUiState(
    val selectedModules: Set<ExportModule> = emptySet(),
    val moduleStats: Map<ExportModule, ModuleStats> = emptyMap(),
    val dateRange: DateRange = DateRange.ALL,
    val exportFormat: ExportFormat = ExportFormat.CSV,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val currentExportingModule: String? = null,
    val exportResult: ExportResult? = null,
    val error: String? = null
)

/**
 * 模块统计信息
 */
data class ModuleStats(
    val totalRecords: Int,
    val lastModified: Long?,
    val estimatedSize: String
)

/**
 * 导出结果
 */
data class ExportResult(
    val success: Boolean,
    val file: File?,
    val message: String
)
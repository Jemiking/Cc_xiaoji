package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.importer.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 导入功能ViewModel
 */
@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ledgerImporter: LedgerImporter
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()
    
    // 导入配置
    private val _importConfig = MutableStateFlow(ImportConfig())
    val importConfig: StateFlow<ImportConfig> = _importConfig.asStateFlow()
    
    /**
     * 选择文件
     */
    fun selectFile(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )
                
                // 复制文件到临时目录
                val file = copyUriToFile(uri)
                
                // 验证文件
                if (!ledgerImporter.validateFile(file)) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "不支持的文件格式。请选择CSV文件（扩展名为.csv）或确保文件内容为逗号分隔格式"
                    )
                    return@launch
                }
                
                // 预览文件
                val preview = ledgerImporter.previewImport(file)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedFile = file,
                    importPreview = preview,
                    currentStep = ImportStep.PREVIEW
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "文件读取失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 更新冲突策略
     */
    fun updateConflictStrategy(strategy: ConflictStrategy) {
        _importConfig.value = _importConfig.value.copy(
            conflictStrategy = strategy
        )
    }
    
    /**
     * 更新数据类型选择
     */
    fun updateDataTypeSelection(
        includeTransactions: Boolean? = null,
        includeAccounts: Boolean? = null,
        includeCategories: Boolean? = null,
        includeBudgets: Boolean? = null,
        includeRecurringTransactions: Boolean? = null,
        includeSavingsGoals: Boolean? = null,
        includeCreditCardBills: Boolean? = null
    ) {
        _importConfig.value = _importConfig.value.copy(
            includeTransactions = includeTransactions ?: _importConfig.value.includeTransactions,
            includeAccounts = includeAccounts ?: _importConfig.value.includeAccounts,
            includeCategories = includeCategories ?: _importConfig.value.includeCategories,
            includeBudgets = includeBudgets ?: _importConfig.value.includeBudgets,
            includeRecurringTransactions = includeRecurringTransactions ?: _importConfig.value.includeRecurringTransactions,
            includeSavingsGoals = includeSavingsGoals ?: _importConfig.value.includeSavingsGoals,
            includeCreditCardBills = includeCreditCardBills ?: _importConfig.value.includeCreditCardBills
        )
    }
    
    /**
     * 进入配置步骤
     */
    fun goToConfigureStep() {
        _uiState.value = _uiState.value.copy(
            currentStep = ImportStep.CONFIGURE
        )
    }
    
    /**
     * 开始导入
     */
    fun startImport() {
        val file = _uiState.value.selectedFile ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                currentStep = ImportStep.IMPORTING,
                error = null
            )
            
            try {
                // 执行导入
                val result = ledgerImporter.importData(file, _importConfig.value)
                
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = result,
                    currentStep = ImportStep.RESULT
                )
                
                // 清理临时文件
                file.delete()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "导入失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 更新部分导入设置
     */
    fun updateAllowPartialImport(allow: Boolean) {
        _importConfig.value = _importConfig.value.copy(
            allowPartialImport = allow
        )
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 重置状态
     */
    fun reset() {
        _uiState.value = ImportUiState()
        _importConfig.value = ImportConfig()
    }
    
    /**
     * 复制Uri到临时文件
     */
    private fun copyUriToFile(uri: Uri): File {
        val tempDir = File(context.cacheDir, "imports")
        if (!tempDir.exists()) tempDir.mkdirs()
        
        val tempFile = File(tempDir, "import_${System.currentTimeMillis()}.csv")
        
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return tempFile
    }
}

/**
 * 导入UI状态
 */
data class ImportUiState(
    val isLoading: Boolean = false,
    val isImporting: Boolean = false,
    val currentStep: ImportStep = ImportStep.SELECT_FILE,
    val selectedFile: File? = null,
    val importPreview: ImportPreview? = null,
    val importResult: ImportResult? = null,
    val error: String? = null
)

/**
 * 导入步骤
 */
enum class ImportStep {
    SELECT_FILE,    // 选择文件
    PREVIEW,        // 预览数据
    CONFIGURE,      // 配置选项
    IMPORTING,      // 导入中
    RESULT          // 导入结果
}
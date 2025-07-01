package com.ccxiaoji.app.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.data.importer.ImportService
import com.ccxiaoji.common.data.import.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 数据导入ViewModel
 */
@HiltViewModel
class DataImportViewModel @Inject constructor(
    private val importService: ImportService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DataImportUiState())
    val uiState: StateFlow<DataImportUiState> = _uiState.asStateFlow()
    
    /**
     * 选择文件进行验证
     */
    fun selectAndValidateFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, error = null) }
            
            try {
                val validation = importService.validateFile(uri)
                
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        selectedFileUri = uri,
                        validation = validation,
                        selectedModules = if (validation.isValid) validation.dataModules.toSet() else emptySet(),
                        importStep = if (validation.isValid) ImportStep.PREVIEW else ImportStep.SELECT_FILE
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        error = "文件验证失败: ${e.message}",
                        importStep = ImportStep.SELECT_FILE
                    )
                }
            }
        }
    }
    
    /**
     * 切换模块选择
     */
    fun toggleModuleSelection(module: DataModule) {
        _uiState.update { state ->
            val newSelection = if (state.selectedModules.contains(module)) {
                state.selectedModules - module
            } else {
                state.selectedModules + module
            }
            state.copy(selectedModules = newSelection)
        }
    }
    
    /**
     * 全选/取消全选模块
     */
    fun toggleSelectAll() {
        _uiState.update { state ->
            val validation = state.validation ?: return@update state
            val availableModules = validation.dataModules.toSet()
            val newSelection = if (state.selectedModules.size == availableModules.size) {
                emptySet()
            } else {
                availableModules
            }
            state.copy(selectedModules = newSelection)
        }
    }
    
    /**
     * 更新导入配置
     */
    fun updateImportConfig(
        skipExisting: Boolean? = null,
        createBackup: Boolean? = null
    ) {
        _uiState.update { state ->
            state.copy(
                importConfig = state.importConfig.copy(
                    skipExisting = skipExisting ?: state.importConfig.skipExisting,
                    createBackup = createBackup ?: state.importConfig.createBackup
                )
            )
        }
    }
    
    /**
     * 开始导入
     */
    fun startImport() {
        val fileUri = _uiState.value.selectedFileUri ?: return
        val selectedModules = _uiState.value.selectedModules
        if (selectedModules.isEmpty()) {
            _uiState.update { it.copy(error = "请至少选择一个模块") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isImporting = true,
                    error = null,
                    importStep = ImportStep.IMPORTING
                )
            }
            
            try {
                val config = _uiState.value.importConfig
                val result = importService.importFromUri(fileUri, config)
                
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importResult = result,
                        importStep = ImportStep.RESULT
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = "导入失败: ${e.message}",
                        importStep = ImportStep.PREVIEW
                    )
                }
            }
        }
    }
    
    /**
     * 重置导入状态
     */
    fun reset() {
        _uiState.value = DataImportUiState()
    }
    
    /**
     * 返回上一步
     */
    fun goBack() {
        _uiState.update { state ->
            val newStep = when (state.importStep) {
                ImportStep.SELECT_FILE -> ImportStep.SELECT_FILE
                ImportStep.PREVIEW -> ImportStep.SELECT_FILE
                ImportStep.IMPORTING -> ImportStep.PREVIEW
                ImportStep.RESULT -> ImportStep.SELECT_FILE
            }
            state.copy(importStep = newStep)
        }
    }
}

/**
 * 导入UI状态
 */
data class DataImportUiState(
    val isValidating: Boolean = false,
    val isImporting: Boolean = false,
    val selectedFileUri: Uri? = null,
    val validation: ImportValidation? = null,
    val selectedModules: Set<DataModule> = emptySet(),
    val importConfig: ImportConfig = ImportConfig(),
    val importResult: ImportResult? = null,
    val importStep: ImportStep = ImportStep.SELECT_FILE,
    val error: String? = null
)

/**
 * 导入步骤
 */
enum class ImportStep {
    SELECT_FILE,    // 选择文件
    PREVIEW,        // 预览和配置
    IMPORTING,      // 导入中
    RESULT          // 导入结果
}
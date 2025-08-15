package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.importer.qianji.QianjiImporter
import com.ccxiaoji.feature.ledger.data.importer.qianji.QianjiParser
import com.ccxiaoji.shared.user.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * 钱迹数据导入ViewModel
 */
@HiltViewModel
class QianjiImportViewModel @Inject constructor(
    private val importer: QianjiImporter,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    /**
     * 导入选项
     */
    data class ImportOptions(
        val skipDuplicates: Boolean = true,
        val createCategories: Boolean = true,
        val createAccounts: Boolean = true,
        val mergeSubCategories: Boolean = true,
        val handleRefunds: Boolean = true
    )
    
    /**
     * UI状态
     */
    data class UiState(
        val isLoading: Boolean = false,
        val isImporting: Boolean = false,
        val selectedFile: File? = null,
        val previewData: List<QianjiParser.QianjiRecord> = emptyList(),
        val importOptions: ImportOptions = ImportOptions(),
        val importProgress: Float = 0f,
        val progressMessage: String = "",
        val importComplete: Boolean = false,
        val resultMessage: String = "",
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        // 监听导入进度
        viewModelScope.launch {
            importer.progress.collect { progress ->
                val progressValue = if (progress.total > 0) {
                    progress.current.toFloat() / progress.total
                } else {
                    0f
                }
                
                _uiState.update {
                    it.copy(
                        importProgress = progressValue,
                        progressMessage = progress.message
                    )
                }
            }
        }
    }
    
    /**
     * 选择文件
     */
    fun selectFile(uri: Uri) {
        android.util.Log.d("QianjiImport", "selectFile called with uri: $uri")
        android.util.Log.d("QianjiImport", "URI scheme: ${uri.scheme}, authority: ${uri.authority}")
        android.util.Log.d("QianjiImport", "URI path: ${uri.path}, lastPathSegment: ${uri.lastPathSegment}")
        
        viewModelScope.launch {
            try {
                android.util.Log.d("QianjiImport", "Starting file selection process")
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // 在IO线程中将Uri转换为File
                android.util.Log.d("QianjiImport", "Converting URI to File...")
                val startTime = System.currentTimeMillis()
                val file = withContext(Dispatchers.IO) {
                    uriToFile(uri)
                }
                val elapsedTime = System.currentTimeMillis() - startTime
                android.util.Log.d("QianjiImport", "File conversion completed in ${elapsedTime}ms, file: ${file.absolutePath}")
                
                // 验证文件格式
                android.util.Log.d("QianjiImport", "Validating file format...")
                android.util.Log.d("QianjiImport", "File exists: ${file.exists()}, size: ${file.length()} bytes")
                
                if (!importer.validateFile(file)) {
                    android.util.Log.e("QianjiImport", "File validation failed")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "文件格式错误，请选择钱迹导出的CSV文件"
                        )
                    }
                    return@launch
                }
                android.util.Log.d("QianjiImport", "File validation passed")
                
                // 预览数据
                android.util.Log.d("QianjiImport", "Starting data preview...")
                val previewData = importer.preview(file, 100)
                android.util.Log.d("QianjiImport", "Preview data loaded: ${previewData.size} items")
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedFile = file,
                        previewData = previewData,
                        error = null
                    )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("QianjiImport", "Error in selectFile: ${e.message}", e)
                android.util.Log.e("QianjiImport", "Exception type: ${e.javaClass.simpleName}")
                e.stackTrace.take(5).forEach { 
                    android.util.Log.e("QianjiImport", "  at $it")
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "文件读取失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 开始导入
     */
    fun startImport() {
        android.util.Log.e("QIANJI_DEBUG", "========== startImport 开始 ==========")
        val file = _uiState.value.selectedFile ?: run {
            android.util.Log.e("QIANJI_DEBUG", "错误：没有选择文件")
            return
        }
        android.util.Log.e("QIANJI_DEBUG", "文件路径: ${file.absolutePath}")
        
        viewModelScope.launch {
            try {
                android.util.Log.e("QIANJI_DEBUG", "开始导入流程...")
                _uiState.update {
                    it.copy(
                        isImporting = true,
                        error = null,
                        importProgress = 0f
                    )
                }
                
                // 获取当前用户ID
                val userId = userRepository.getCurrentUserId()
                android.util.Log.e("QIANJI_DEBUG", "当前用户ID: $userId")
                
                // 执行导入
                android.util.Log.e("QIANJI_DEBUG", "创建导入选项...")
                val options = QianjiImporter.ImportOptions(
                    skipDuplicates = _uiState.value.importOptions.skipDuplicates,
                    createCategories = _uiState.value.importOptions.createCategories,
                    createAccounts = _uiState.value.importOptions.createAccounts,
                    mergeSubCategories = _uiState.value.importOptions.mergeSubCategories,
                    handleRefunds = _uiState.value.importOptions.handleRefunds
                )
                android.util.Log.e("QIANJI_DEBUG", "导入选项: $options")
                
                android.util.Log.e("QIANJI_DEBUG", "调用 importer.import()...")
                val result = importer.import(file, userId, options)
                android.util.Log.e("QIANJI_DEBUG", "importer.import() 返回: $result")
                
                when (result) {
                    is QianjiImporter.ImportResult.Success -> {
                        android.util.Log.e("QIANJI_DEBUG", "导入成功！详细信息:")
                        android.util.Log.e("QIANJI_DEBUG", "  - 总数: ${result.total}")
                        android.util.Log.e("QIANJI_DEBUG", "  - 导入: ${result.imported}")
                        android.util.Log.e("QIANJI_DEBUG", "  - 跳过: ${result.skipped}")
                        android.util.Log.e("QIANJI_DEBUG", "  - 失败: ${result.failed}")
                        val message = buildString {
                            append("导入成功！\n")
                            append("共 ${result.total} 条记录\n")
                            append("成功导入 ${result.imported} 条\n")
                            if (result.skipped > 0) {
                                append("跳过重复 ${result.skipped} 条\n")
                            }
                            if (result.failed > 0) {
                                append("失败 ${result.failed} 条")
                            }
                        }
                        
                        _uiState.update {
                            it.copy(
                                isImporting = false,
                                importComplete = true,
                                resultMessage = message,
                                importProgress = 1f
                            )
                        }
                    }
                    
                    is QianjiImporter.ImportResult.Error -> {
                        android.util.Log.e("QIANJI_DEBUG", "导入失败: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isImporting = false,
                                error = result.message
                            )
                        }
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("QIANJI_DEBUG", "导入异常: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = "导入失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 更新导入选项
     */
    fun updateOptions(options: ImportOptions) {
        _uiState.update { it.copy(importOptions = options) }
    }
    
    /**
     * 重置状态
     */
    fun reset() {
        _uiState.value = UiState()
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 将Uri转换为File
     */
    private fun uriToFile(uri: Uri): File {
        android.util.Log.d("QianjiImport", "uriToFile: Starting conversion for URI: $uri")
        
        // 检查URI权限
        try {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            android.util.Log.d("QianjiImport", "Successfully took persistable URI permission")
        } catch (e: Exception) {
            android.util.Log.w("QianjiImport", "Could not take persistable permission: ${e.message}")
        }
        
        android.util.Log.d("QianjiImport", "Opening input stream...")
        val startStreamTime = System.currentTimeMillis()
        val inputStream = try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            android.util.Log.e("QianjiImport", "Failed to open input stream: ${e.message}", e)
            throw e
        }
        val streamOpenTime = System.currentTimeMillis() - startStreamTime
        android.util.Log.d("QianjiImport", "Input stream opened in ${streamOpenTime}ms")
        
        if (inputStream == null) {
            android.util.Log.e("QianjiImport", "Input stream is null")
            throw IllegalArgumentException("无法打开文件")
        }
        
        // 创建临时文件
        android.util.Log.d("QianjiImport", "Creating temp file in: ${context.cacheDir}")
        val tempFile = File.createTempFile("qianji_import_", ".csv", context.cacheDir)
        android.util.Log.d("QianjiImport", "Temp file created: ${tempFile.absolutePath}")
        
        android.util.Log.d("QianjiImport", "Starting file copy...")
        val copyStartTime = System.currentTimeMillis()
        var bytesCopied = 0L
        
        inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                bytesCopied = input.copyTo(output)
            }
        }
        
        val copyTime = System.currentTimeMillis() - copyStartTime
        android.util.Log.d("QianjiImport", "File copy completed: ${bytesCopied} bytes in ${copyTime}ms")
        android.util.Log.d("QianjiImport", "Temp file size: ${tempFile.length()} bytes")
        
        return tempFile
    }
}
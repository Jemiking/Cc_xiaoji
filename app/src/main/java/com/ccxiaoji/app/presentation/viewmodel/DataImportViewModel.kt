package com.ccxiaoji.app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.domain.usecase.ImportDataUseCase
import com.ccxiaoji.app.domain.usecase.ValidateImportDataUseCase
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.ImportLedgerResult
import com.ccxiaoji.app.data.repository.CountdownRepository
import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.todo.api.ImportTasksResult
import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.habit.api.ImportHabitsResult
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

// TODO: 编译验证 - 需要执行 ./gradlew :app:compileDebugKotlin
// TODO: 编译验证 - 使用新的UseCase实现

@HiltViewModel
class DataImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val validateImportDataUseCase: ValidateImportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val ledgerApi: LedgerApi,
    private val countdownRepository: CountdownRepository,
    private val gson: Gson
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DataImportUiState())
    val uiState: StateFlow<DataImportUiState> = _uiState.asStateFlow()
    
    fun selectFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                selectedFileUri = uri,
                selectedFileName = getFileName(uri),
                validationResult = null,
                importResult = null
            ) }
            
            // 自动验证文件
            validateFile()
        }
    }
    
    fun clearFile() {
        _uiState.update { it.copy(
            selectedFileUri = null,
            selectedFileName = null,
            fileContent = null,
            validationResult = null,
            importResult = null
        ) }
    }
    
    fun toggleModule(module: ImportModule) {
        _uiState.update { state ->
            val selectedModules = state.selectedModules.toMutableSet()
            if (module in selectedModules) {
                selectedModules.remove(module)
            } else {
                selectedModules.add(module)
            }
            state.copy(selectedModules = selectedModules)
        }
    }
    
    fun setConflictResolution(resolution: ConflictResolution) {
        _uiState.update { it.copy(conflictResolution = resolution) }
    }
    
    private suspend fun validateFile() = withContext(Dispatchers.IO) {
        val uri = _uiState.value.selectedFileUri ?: return@withContext
        
        _uiState.update { it.copy(isValidating = true) }
        
        try {
            // 读取文件内容
            val content = readFile(uri)
            _uiState.update { it.copy(fileContent = content) }
            
            // 使用ValidateImportDataUseCase验证文件
            when (val result = validateImportDataUseCase(content)) {
                is ValidateImportDataUseCase.ValidationResult.Success -> {
                    val errors = mutableListOf<ValidationError>()
                    val warnings = mutableListOf<ValidationWarning>()
                    val dataPreview = DataPreview()
                    
                    // 映射数据预览
                    result.ledgerItemDetails?.let { details ->
                        dataPreview.transactionCount = details.transactionCount
                        dataPreview.accountCount = details.accountCount
                        dataPreview.categoryCount = details.categoryCount
                        dataPreview.budgetCount = details.budgetCount
                        dataPreview.savingsGoalCount = details.savingsGoalCount
                    }
                    dataPreview.taskCount = result.todoItemCount
                    dataPreview.habitCount = result.habitItemCount
                    
                    // 添加警告信息
                    if (result.totalItemCount > 10000) {
                        warnings.add(ValidationWarning("general", "数据量较大(${result.totalItemCount}条)，导入可能需要较长时间"))
                    }
                    
                    when (result.dataType) {
                        ValidateImportDataUseCase.DataType.FULL_BACKUP -> {
                            warnings.add(ValidationWarning("info", "检测到完整备份数据"))
                        }
                        ValidateImportDataUseCase.DataType.MULTI_MODULE -> {
                            warnings.add(ValidationWarning("info", "检测到多模块数据"))
                        }
                        ValidateImportDataUseCase.DataType.SINGLE_MODULE -> {
                            warnings.add(ValidationWarning("info", "检测到单模块数据"))
                        }
                    }
                    
                    val validationResult = ValidationResult(
                        isValid = true,
                        errors = errors,
                        warnings = warnings,
                        dataPreview = dataPreview
                    )
                    
                    _uiState.update { it.copy(
                        isValidating = false,
                        validationResult = validationResult
                    ) }
                }
                is ValidateImportDataUseCase.ValidationResult.Error -> {
                    val errors = listOf(
                        ValidationError("validation", result.message)
                    )
                    
                    _uiState.update { it.copy(
                        isValidating = false,
                        validationResult = ValidationResult(
                            isValid = false,
                            errors = errors,
                            warnings = emptyList(),
                            dataPreview = DataPreview()
                        )
                    ) }
                }
            }
            
        } catch (e: Exception) {
            val errors = listOf(
                ValidationError("file", "读取文件失败: ${e.message}")
            )
            
            _uiState.update { it.copy(
                isValidating = false,
                validationResult = ValidationResult(
                    isValid = false,
                    errors = errors,
                    warnings = emptyList(),
                    dataPreview = DataPreview()
                )
            ) }
        }
    }
    
    fun startImport() {
        viewModelScope.launch {
            importData()
        }
    }
    
    private suspend fun importData() = withContext(Dispatchers.IO) {
        val content = _uiState.value.fileContent ?: return@withContext
        val selectedModules = _uiState.value.selectedModules
        
        _uiState.update { it.copy(isImporting = true, importProgress = ImportProgress()) }
        
        // 修改文件内容以适应新的数据格式
        val modifiedContent = prepareImportContent(content, selectedModules)
        
        // 使用ImportDataUseCase进行导入
        when (val result = importDataUseCase(
            jsonContent = modifiedContent,
            conflictResolution = _uiState.value.conflictResolution.name,
            onProgress = { progress ->
                when (progress) {
                    is ImportDataUseCase.ImportResult.Progress -> {
                        _uiState.update { state ->
                            state.copy(
                                importProgress = ImportProgress(
                                    currentModule = progress.currentModule,
                                    totalItems = state.importProgress?.totalItems ?: 0,
                                    processedItems = (progress.progress * 100).toInt(),
                                    successCount = state.importProgress?.successCount ?: 0,
                                    errorCount = state.importProgress?.errorCount ?: 0,
                                    skipCount = state.importProgress?.skipCount ?: 0,
                                    modulesCompleted = state.importProgress?.modulesCompleted ?: mutableListOf()
                                )
                            )
                        }
                    }
                    else -> {}
                }
            }
        )) {
            is ImportDataUseCase.ImportResult.Success -> {
                _uiState.update { it.copy(
                    isImporting = false,
                    importResult = ImportResult(
                        success = result.totalFailed == 0,
                        totalItems = result.totalImported + result.totalSkipped + result.totalFailed,
                        successCount = result.totalImported,
                        errorCount = result.totalFailed,
                        skipCount = result.totalSkipped,
                        message = result.messages.firstOrNull() ?: "数据导入完成"
                    )
                ) }
            }
            is ImportDataUseCase.ImportResult.Error -> {
                _uiState.update { it.copy(
                    isImporting = false,
                    importResult = ImportResult(
                        success = false,
                        totalItems = 0,
                        successCount = 0,
                        errorCount = 0,
                        skipCount = 0,
                        message = result.message
                    )
                ) }
            }
            else -> {
                // 不应该发生
            }
        }
    }
    
    // 准备导入内容，适配新的数据格式
    private fun prepareImportContent(content: String, selectedModules: Set<ImportModule>): String {
        try {
            val originalJson = gson.fromJson(content, JsonObject::class.java)
            val newJson = JsonObject()
            
            // 添加版本信息
            newJson.addProperty("version", "1.0")
            
            // 创建data对象
            val dataObject = JsonObject()
            
            // 根据选择的模块添加数据
            if (ImportModule.LEDGER in selectedModules) {
                if (originalJson.has("ledger")) {
                    dataObject.add("ledger", originalJson.getAsJsonObject("ledger"))
                }
                
                // 处理其他数据中的记账相关数据
                if (ImportModule.OTHERS in selectedModules && originalJson.has("others")) {
                    val others = originalJson.getAsJsonObject("others")
                    val ledgerData = dataObject.getAsJsonObject("ledger") ?: JsonObject().also { dataObject.add("ledger", it) }
                    
                    if (others.has("budgets")) {
                        ledgerData.add("budgets", others.get("budgets"))
                    }
                    if (others.has("savingsGoals")) {
                        ledgerData.add("savingsGoals", others.get("savingsGoals"))
                    }
                }
            }
            
            if (ImportModule.TODO in selectedModules && originalJson.has("tasks")) {
                val todoObject = JsonObject()
                todoObject.add("tasks", originalJson.get("tasks"))
                dataObject.add("todo", todoObject)
            }
            
            if (ImportModule.HABIT in selectedModules && originalJson.has("habits")) {
                val habitObject = JsonObject()
                habitObject.add("habits", originalJson.get("habits"))
                dataObject.add("habit", habitObject)
            }
            
            newJson.add("data", dataObject)
            
            return gson.toJson(newJson)
        } catch (e: Exception) {
            // 如果转换失败，返回原始内容
            return content
        }
    }
    
    private suspend fun readFile(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("无法打开文件")
        
        inputStream.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.readText()
            }
        }
    }
    
    private fun getFileName(uri: Uri): String {
        // 尝试从URI获取文件名
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    it.getString(nameIndex)
                } else {
                    "未知文件"
                }
            } else {
                "未知文件"
            }
        } ?: "未知文件"
    }
}

// 数据类定义
data class DataImportUiState(
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val fileContent: String? = null,
    val selectedModules: Set<ImportModule> = setOf(
        ImportModule.LEDGER,
        ImportModule.TODO,
        ImportModule.HABIT,
        ImportModule.OTHERS
    ),
    val conflictResolution: ConflictResolution = ConflictResolution.SKIP,
    val isValidating: Boolean = false,
    val validationResult: ValidationResult? = null,
    val isImporting: Boolean = false,
    val importProgress: ImportProgress? = null,
    val importResult: ImportResult? = null
) {
    val canImport: Boolean
        get() = validationResult?.isValid == true && selectedModules.isNotEmpty()
}

enum class ImportModule {
    LEDGER, TODO, HABIT, OTHERS
}

enum class ConflictResolution {
    SKIP,                  // 跳过冲突数据
    REPLACE,               // 替换现有数据
    MERGE,                 // 合并数据
    CREATE_NEW            // 创建新记录
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError>,
    val warnings: List<ValidationWarning>,
    val dataPreview: DataPreview
)

data class ValidationError(
    val module: String,
    val message: String
)

data class ValidationWarning(
    val module: String,
    val message: String
)

data class DataPreview(
    var transactionCount: Int = 0,
    var accountCount: Int = 0,
    var categoryCount: Int = 0,
    var taskCount: Int = 0,
    var habitCount: Int = 0,
    var budgetCount: Int = 0,
    var savingsGoalCount: Int = 0,
    var countdownCount: Int = 0
)

data class ImportProgress(
    var currentModule: String = "",
    var totalItems: Int = 0,
    var processedItems: Int = 0,
    var successCount: Int = 0,
    var errorCount: Int = 0,
    var skipCount: Int = 0,
    val modulesCompleted: MutableList<String> = mutableListOf()
) {
    val progress: Float
        get() = if (totalItems > 0) processedItems.toFloat() / totalItems else 0f
}

data class ImportResult(
    val success: Boolean,
    val totalItems: Int,
    val successCount: Int,
    val errorCount: Int,
    val skipCount: Int,
    val message: String
)
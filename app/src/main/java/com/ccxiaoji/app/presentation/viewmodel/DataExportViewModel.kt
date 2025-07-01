package com.ccxiaoji.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.data.repository.CountdownRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.app.presentation.ui.profile.DateRange
import com.ccxiaoji.app.presentation.ui.profile.ExportFormat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import android.provider.OpenableColumns
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DataExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val countdownRepository: CountdownRepository,
    private val gson: Gson
) : ViewModel() {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("export_history", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(DataExportUiState())
    val uiState: StateFlow<DataExportUiState> = _uiState.asStateFlow()
    
    init {
        // 加载持久化的导出历史
        loadExportHistory()
    }
    
    fun selectFormat(format: ExportFormat) {
        _uiState.update { it.copy(selectedFormat = format) }
    }
    
    fun selectDateRange(range: DateRange) {
        _uiState.update { it.copy(dateRange = range) }
    }
    
    fun toggleLedger() {
        _uiState.update { it.copy(exportLedger = !it.exportLedger) }
    }
    
    fun toggleTodo() {
        _uiState.update { it.copy(exportTodo = !it.exportTodo) }
    }
    
    fun toggleHabit() {
        _uiState.update { it.copy(exportHabit = !it.exportHabit) }
    }
    
    fun toggleOthers() {
        _uiState.update { it.copy(exportOthers = !it.exportOthers) }
    }
    
    // 选择模式相关方法
    fun enterSelectionMode(initialItem: ExportHistory? = null) {
        _uiState.update { 
            it.copy(
                isSelectionMode = true,
                selectedItems = initialItem?.let { item -> setOf(item) } ?: emptySet()
            )
        }
    }
    
    fun exitSelectionMode() {
        _uiState.update { 
            it.copy(
                isSelectionMode = false,
                selectedItems = emptySet()
            )
        }
    }
    
    fun toggleItemSelection(item: ExportHistory) {
        _uiState.update { state ->
            val newSelectedItems = if (state.selectedItems.contains(item)) {
                state.selectedItems - item
            } else {
                state.selectedItems + item
            }
            
            // 如果没有选中任何项目，自动退出选择模式
            if (newSelectedItems.isEmpty()) {
                state.copy(
                    isSelectionMode = false,
                    selectedItems = emptySet()
                )
            } else {
                state.copy(selectedItems = newSelectedItems)
            }
        }
    }
    
    fun selectAllItems() {
        _uiState.update { 
            it.copy(selectedItems = it.exportHistory.toSet())
        }
    }
    
    fun clearSelection() {
        _uiState.update { 
            it.copy(selectedItems = emptySet())
        }
    }
    
    /**
     * 删除选中的历史记录
     * @param deleteFiles 是否同时删除文件（仅对本地文件有效）
     */
    suspend fun deleteSelectedItems(deleteFiles: Boolean = false): DeleteResult = withContext(Dispatchers.IO) {
        try {
            _uiState.update { it.copy(isDeletingItems = true) }
            
            val selectedItems = _uiState.value.selectedItems.toList()
            val deletedFiles = mutableListOf<String>()
            val failedFiles = mutableListOf<String>()
            
            // 处理文件删除
            if (deleteFiles) {
                selectedItems.forEach { history ->
                    if (!history.isFromSAF) {
                        // 只删除本地文件
                        try {
                            val file = File(history.filePath)
                            if (file.exists() && file.delete()) {
                                deletedFiles.add(history.fileName)
                            } else {
                                failedFiles.add(history.fileName)
                            }
                        } catch (e: Exception) {
                            failedFiles.add(history.fileName)
                        }
                    }
                }
            }
            
            // 从历史记录中移除选中项目
            val newHistory = _uiState.value.exportHistory.filterNot { history ->
                selectedItems.contains(history)
            }
            
            // 保存更新后的历史记录
            saveExportHistory(newHistory)
            
            // 更新UI状态
            _uiState.update { 
                it.copy(
                    exportHistory = newHistory,
                    isSelectionMode = false,
                    selectedItems = emptySet(),
                    isDeletingItems = false
                )
            }
            
            return@withContext DeleteResult(
                deletedRecords = selectedItems.size,
                deletedFiles = deletedFiles.size,
                failedFiles = failedFiles.size,
                failedFileNames = failedFiles
            )
            
        } catch (e: Exception) {
            _uiState.update { it.copy(isDeletingItems = false) }
            return@withContext DeleteResult(
                deletedRecords = 0,
                deletedFiles = 0,
                failedFiles = 0,
                failedFileNames = emptyList(),
                error = e.message
            )
        }
    }
    
    suspend fun exportData(): Boolean = withContext(Dispatchers.IO) {
        try {
            _uiState.update { it.copy(isExporting = true) }
            
            val dateRange = getDateRange()
            val exportData = mutableMapOf<String, Any>()
            
            // 导出记账数据
            if (_uiState.value.exportLedger) {
                val transactions = transactionRepository.getTransactionsByDateRange(
                    dateRange.first,
                    dateRange.second
                ).first()
                val accounts = accountRepository.getAccounts().first()
                val categories = categoryRepository.getCategories().first()
                
                exportData["ledger"] = mapOf(
                    "transactions" to transactions,
                    "accounts" to accounts,
                    "categories" to categories
                )
            }
            
            // 导出待办数据
            if (_uiState.value.exportTodo) {
                val tasks = todoApi.getTasks().first()
                exportData["tasks"] = tasks
            }
            
            // 导出习惯数据
            if (_uiState.value.exportHabit) {
                val habits = habitApi.getHabits().first()
                exportData["habits"] = habits
            }
            
            // 导出其他数据
            if (_uiState.value.exportOthers) {
                val budgets = budgetRepository.getBudgets().first()
                val savingsGoals = savingsGoalRepository.getAllSavingsGoals().first()
                val countdowns = countdownRepository.getCountdowns().first()
                
                exportData["others"] = mapOf(
                    "budgets" to budgets,
                    "savingsGoals" to savingsGoals,
                    "countdowns" to countdowns
                )
            }
            
            // 添加元数据
            exportData["metadata"] = mapOf(
                "exportDate" to Clock.System.now().toEpochMilliseconds(),
                "appVersion" to "1.0.0",
                "dataVersion" to "1"
            )
            
            // 生成文件
            val fileName = generateFileName()
            val file = when (_uiState.value.selectedFormat) {
                ExportFormat.JSON -> exportAsJson(exportData, fileName)
                ExportFormat.CSV -> exportAsCsv(exportData, fileName)
                ExportFormat.EXCEL -> exportAsExcel(exportData, fileName)
            }
            
            // 更新导出历史
            if (file != null) {
                addToExportHistory(file)
                _uiState.update { it.copy(isExporting = false) }
                return@withContext true
            }
            
            _uiState.update { it.copy(isExporting = false) }
            false
        } catch (e: Exception) {
            _uiState.update { it.copy(isExporting = false) }
            false
        }
    }

    /**
     * 准备导出数据，返回要保存的内容和建议的文件名
     * 用于SAF文件选择器
     */
    suspend fun prepareExportData(): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            _uiState.update { it.copy(isExporting = true) }
            
            val dateRange = getDateRange()
            val exportData = mutableMapOf<String, Any>()
            
            // 导出记账数据
            if (_uiState.value.exportLedger) {
                val transactions = transactionRepository.getTransactionsByDateRange(
                    dateRange.first,
                    dateRange.second
                ).first()
                val accounts = accountRepository.getAccounts().first()
                val categories = categoryRepository.getCategories().first()
                
                exportData["ledger"] = mapOf(
                    "transactions" to transactions,
                    "accounts" to accounts,
                    "categories" to categories
                )
            }
            
            // 导出待办数据
            if (_uiState.value.exportTodo) {
                val tasks = todoApi.getTasks().first()
                exportData["tasks"] = tasks
            }
            
            // 导出习惯数据
            if (_uiState.value.exportHabit) {
                val habits = habitApi.getHabits().first()
                exportData["habits"] = habits
            }
            
            // 导出其他数据
            if (_uiState.value.exportOthers) {
                val budgets = budgetRepository.getBudgets().first()
                val savingsGoals = savingsGoalRepository.getAllSavingsGoals().first()
                val countdowns = countdownRepository.getCountdowns().first()
                
                exportData["others"] = mapOf(
                    "budgets" to budgets,
                    "savingsGoals" to savingsGoals,
                    "countdowns" to countdowns
                )
            }
            
            // 添加元数据
            exportData["metadata"] = mapOf(
                "exportDate" to Clock.System.now().toEpochMilliseconds(),
                "appVersion" to "1.0.0",
                "dataVersion" to "1"
            )
            
            // 准备内容和文件名
            val fileName = generateFileName()
            val content = when (_uiState.value.selectedFormat) {
                ExportFormat.JSON -> {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    gson.toJson(exportData)
                }
                ExportFormat.CSV -> {
                    // CSV内容生成（简化版）
                    generateCsvContent(exportData)
                }
                ExportFormat.EXCEL -> {
                    // Excel暂不支持
                    null
                }
            }
            
            _uiState.update { it.copy(isExporting = false) }
            
            if (content != null) {
                val fileExtension = when (_uiState.value.selectedFormat) {
                    ExportFormat.JSON -> ".json"
                    ExportFormat.CSV -> ".csv"
                    ExportFormat.EXCEL -> ".xlsx"
                }
                return@withContext content to "$fileName$fileExtension"
            }
            
            return@withContext null
        } catch (e: Exception) {
            _uiState.update { it.copy(isExporting = false) }
            return@withContext null
        }
    }

    /**
     * 保存导出数据到用户选择的位置
     */
    suspend fun saveExportDataToUri(uri: Uri, content: String, suggestedFileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                outputStream.flush()
            }
            
            // 获取实际的文件名
            val actualFileName = getActualFileNameFromUri(uri, suggestedFileName)
            val history = ExportHistory(
                fileName = actualFileName,
                filePath = uri.toString(),
                dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                size = formatFileSize(content.toByteArray().size.toLong()),
                isFromSAF = true // 标记这是SAF导出的文件
            )
            
            val newHistory = listOf(history) + _uiState.value.exportHistory.take(9)
            
            _uiState.update { state ->
                state.copy(exportHistory = newHistory)
            }
            
            // 持久化保存历史记录
            saveExportHistory(newHistory)
            
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        }
    }

    /**
     * 生成CSV内容
     */
    private fun generateCsvContent(data: Map<String, Any>): String {
        val sb = StringBuilder()
        
        // 处理交易数据
        val ledger = data["ledger"] as? Map<*, *>
        val transactions = ledger?.get("transactions") as? List<*>
        
        if (transactions != null) {
            sb.append("类型,日期,金额,分类,账户,备注\n")
            sb.append("交易数据导出\n")
            // 这里可以根据实际Transaction结构来处理
            transactions.forEach { transaction ->
                sb.append("交易记录数据\n")
            }
        }
        
        return sb.toString()
    }
    
    private fun exportAsJson(data: Map<String, Any>, fileName: String): File? {
        val file = File(context.getExternalFilesDir(null), "$fileName.json")
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        file.writeText(gson.toJson(data))
        return file
    }
    
    private fun exportAsCsv(data: Map<String, Any>, fileName: String): File? {
        // CSV导出逻辑（简化版本，只导出交易记录）
        val file = File(context.getExternalFilesDir(null), "$fileName.csv")
        val transactions = (data["ledger"] as? Map<*, *>)?.get("transactions") as? List<*>
        
        if (transactions != null) {
            file.bufferedWriter().use { writer ->
                // 写入标题行
                writer.write("日期,金额,分类,账户,备注\n")
                
                // 写入数据行
                transactions.forEach { transaction ->
                    // 这里需要根据实际的Transaction类结构来处理
                    writer.write("...\n")
                }
            }
        }
        
        return file
    }
    
    private fun exportAsExcel(data: Map<String, Any>, fileName: String): File? {
        // Excel导出需要额外的库支持，这里暂时返回null
        // 实际实现需要使用Apache POI或类似的库
        return null
    }
    
    private fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "ccxiaoji_export_${dateFormat.format(Date())}"
    }
    
    private fun getDateRange(): Pair<LocalDate, LocalDate> {
        val today = Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
        return when (_uiState.value.dateRange) {
            DateRange.ALL -> LocalDate(2020, 1, 1) to today
            DateRange.THIS_YEAR -> LocalDate(today.year, 1, 1) to today
            DateRange.THIS_MONTH -> LocalDate(today.year, today.monthNumber, 1) to today
        }
    }
    
    private fun addToExportHistory(file: File) {
        val history = ExportHistory(
            fileName = file.name,
            filePath = file.absolutePath,
            dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            size = formatFileSize(file.length()),
            isFromSAF = false // 传统文件导出
        )
        
        val newHistory = listOf(history) + _uiState.value.exportHistory.take(9)
        
        _uiState.update { state ->
            state.copy(exportHistory = newHistory)
        }
        
        // 持久化保存历史记录
        saveExportHistory(newHistory)
    }
    
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "%.1f KB".format(size / 1024.0)
            else -> "%.1f MB".format(size / (1024.0 * 1024.0))
        }
    }
    
    fun shareFile(exportHistory: ExportHistory) {
        try {
            if (exportHistory.isFromSAF) {
                // SAF导出的文件，使用Uri直接分享
                val uri = Uri.parse(exportHistory.filePath)
                shareUri(uri, exportHistory.fileName)
            } else {
                // 传统文件导出，使用File路径
                val file = File(exportHistory.filePath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    shareUri(uri, exportHistory.fileName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 可以在这里显示错误提示
        }
    }
    
    private fun shareUri(uri: Uri, fileName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        context.startActivity(Intent.createChooser(intent, "分享导出文件").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
    
    private fun getCurrentUserId(): String = "current_user_id"
    
    /**
     * 从Uri获取实际的文件名
     */
    private fun getActualFileNameFromUri(uri: Uri, fallbackName: String): String {
        return try {
            // 尝试从ContentResolver获取显示名称
            val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val displayName = cursor.getString(nameIndex)
                    if (!displayName.isNullOrBlank()) {
                        return@use displayName
                    }
                }
                return@use null
            }
            
            if (!displayName.isNullOrBlank()) {
                return displayName
            }
            
            // 如果无法获取显示名称，尝试从Uri路径中提取
            val path = uri.path
            if (!path.isNullOrBlank()) {
                val fileName = path.substringAfterLast('/')
                if (fileName.isNotBlank() && fileName.contains('.')) {
                    return fileName
                }
            }
            
            // 最后使用fallback name
            fallbackName
        } catch (e: Exception) {
            fallbackName
        }
    }
    
    /**
     * 加载持久化的导出历史
     */
    private fun loadExportHistory() {
        try {
            val historyJson = sharedPreferences.getString("export_history_list", null)
            if (historyJson != null) {
                val historyList = gson.fromJson<List<ExportHistory>>(
                    historyJson,
                    object : com.google.gson.reflect.TypeToken<List<ExportHistory>>() {}.type
                )
                _uiState.update { it.copy(exportHistory = historyList) }
            }
        } catch (e: Exception) {
            // 如果加载失败（可能是格式不兼容），清空历史记录
            e.printStackTrace()
            // 清空SharedPreferences中的旧数据
            sharedPreferences.edit().remove("export_history_list").apply()
            // 保持空列表状态
        }
    }
    
    /**
     * 保存导出历史到持久化存储
     */
    private fun saveExportHistory(history: List<ExportHistory>) {
        try {
            val historyJson = gson.toJson(history)
            sharedPreferences.edit()
                .putString("export_history_list", historyJson)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class DataExportUiState(
    val selectedFormat: ExportFormat = ExportFormat.JSON,
    val dateRange: DateRange = DateRange.ALL,
    val exportLedger: Boolean = true,
    val exportTodo: Boolean = true,
    val exportHabit: Boolean = true,
    val exportOthers: Boolean = true,
    val isExporting: Boolean = false,
    val exportHistory: List<ExportHistory> = emptyList(),
    // 选择模式相关状态
    val isSelectionMode: Boolean = false,
    val selectedItems: Set<ExportHistory> = emptySet(),
    val isDeletingItems: Boolean = false
) {
    val canExport: Boolean
        get() = exportLedger || exportTodo || exportHabit || exportOthers
        
    val selectedCount: Int
        get() = selectedItems.size
        
    val allItemsSelected: Boolean
        get() = exportHistory.isNotEmpty() && selectedItems.size == exportHistory.size
}

data class ExportHistory(
    val fileName: String,
    val filePath: String,
    val dateTime: String,
    val size: String,
    val isFromSAF: Boolean = false // 标记是否来自SAF导出
)

data class DeleteResult(
    val deletedRecords: Int,
    val deletedFiles: Int,
    val failedFiles: Int,
    val failedFileNames: List<String>,
    val error: String? = null
) {
    val isSuccess: Boolean
        get() = error == null
        
    val hasFailures: Boolean
        get() = failedFiles > 0
}
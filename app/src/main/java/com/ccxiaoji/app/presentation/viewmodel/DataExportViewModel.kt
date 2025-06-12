package com.ccxiaoji.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.app.data.repository.CountdownRepository
import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.app.presentation.ui.profile.DateRange
import com.ccxiaoji.app.presentation.ui.profile.ExportFormat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DataExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val ledgerApi: LedgerApi,
    private val countdownRepository: CountdownRepository,
    private val gson: Gson
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DataExportUiState())
    val uiState: StateFlow<DataExportUiState> = _uiState.asStateFlow()
    
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
    
    suspend fun exportData(): Boolean = withContext(Dispatchers.IO) {
        try {
            _uiState.update { it.copy(isExporting = true) }
            
            val dateRange = getDateRange()
            val exportData = mutableMapOf<String, Any>()
            
            // 导出记账数据
            if (_uiState.value.exportLedger) {
                // 获取日期范围内的所有月份
                val startDate = dateRange.first
                val endDate = dateRange.second
                val transactions = mutableListOf<Any>()
                
                // 逐月获取交易记录
                var currentDate = startDate
                while (currentDate <= endDate) {
                    val monthTransactions = ledgerApi.getTransactionsByMonth(
                        currentDate.year,
                        currentDate.monthNumber
                    )
                    transactions.addAll(monthTransactions.filter { transaction ->
                        transaction.date >= startDate && transaction.date <= endDate
                    })
                    
                    // 移到下个月
                    currentDate = LocalDate(
                        if (currentDate.monthNumber == 12) currentDate.year + 1 else currentDate.year,
                        if (currentDate.monthNumber == 12) 1 else currentDate.monthNumber + 1,
                        1
                    )
                }
                
                val accounts = ledgerApi.getAccounts()
                val categories = ledgerApi.getAllCategories()
                
                exportData["ledger"] = mapOf(
                    "transactions" to transactions,
                    "accounts" to accounts,
                    "categories" to categories
                )
            }
            
            // 导出待办数据
            if (_uiState.value.exportTodo) {
                val tasks = todoApi.getAllTasks()
                exportData["tasks"] = tasks
            }
            
            // 导出习惯数据
            if (_uiState.value.exportHabit) {
                val habits = habitApi.getAllHabits()
                exportData["habits"] = habits
            }
            
            // 导出其他数据
            if (_uiState.value.exportOthers) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val budgets = ledgerApi.getBudgetsWithSpent(now.year, now.monthNumber).first()
                val savingsGoals = ledgerApi.getSavingsGoals()
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
            size = formatFileSize(file.length())
        )
        
        _uiState.update { state ->
            state.copy(
                exportHistory = listOf(history) + state.exportHistory.take(9)
            )
        }
    }
    
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "%.1f KB".format(size / 1024.0)
            else -> "%.1f MB".format(size / (1024.0 * 1024.0))
        }
    }
    
    fun shareFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(intent, "分享导出文件").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
    
    private fun getCurrentUserId(): String = "current_user_id"
}

data class DataExportUiState(
    val selectedFormat: ExportFormat = ExportFormat.JSON,
    val dateRange: DateRange = DateRange.ALL,
    val exportLedger: Boolean = true,
    val exportTodo: Boolean = true,
    val exportHabit: Boolean = true,
    val exportOthers: Boolean = true,
    val isExporting: Boolean = false,
    val exportHistory: List<ExportHistory> = emptyList()
) {
    val canExport: Boolean
        get() = exportLedger || exportTodo || exportHabit || exportOthers
}

data class ExportHistory(
    val fileName: String,
    val filePath: String,
    val dateTime: String,
    val size: String
)
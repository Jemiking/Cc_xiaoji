package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.AutoLedgerDebugRecord
import com.ccxiaoji.feature.ledger.domain.repository.AutoLedgerDebugRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.ccxiaoji.shared.notification.api.NotificationEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 自动记账调试面板ViewModel
 */
@HiltViewModel
class AutoLedgerDebugViewModel @Inject constructor(
    private val debugRepository: AutoLedgerDebugRepository,
    private val notificationEventRepository: NotificationEventRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DebugUiState())
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()
    
    init {
        loadDebugData()

        // 监听诊断统计
        viewModelScope.launch {
            notificationEventRepository.diagnostics().collect { d ->
                _uiState.value = _uiState.value.copy(
                    listenerDiagnostics = ListenerDiagnostics(
                        totalEmitted = d.totalEmitted,
                        emittedByKeywords = d.emittedByKeywords,
                        emittedWithoutKeywords = d.emittedWithoutKeywords,
                        skippedGroupSummary = d.skippedGroupSummary,
                        skippedUnsupportedPackage = d.skippedUnsupportedPackage,
                        skippedNoKeywordsByConfig = d.skippedNoKeywordsByConfig
                    )
                )
            }
        }

        // 监听监听层配置
        viewModelScope.launch {
            val KEY1 = booleanPreferencesKey("auto_ledger_emit_without_keywords")
            val KEY2 = booleanPreferencesKey("auto_ledger_emit_group_summary")
            dataStore.data.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    emitWithoutKeywords = prefs[KEY1] ?: true,
                    emitGroupSummary = prefs[KEY2] ?: false
                )
            }
        }
    }
    
    /**
     * 加载调试数据
     */
    fun loadDebugData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 加载调试记录
                when (val recordsResult = debugRepository.getRecentDebugRecords(100)) {
                    is BaseResult.Success -> {
                        // 加载统计信息
                        when (val statsResult = debugRepository.getDebugStatistics()) {
                            is BaseResult.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    debugRecords = recordsResult.data,
                                    statistics = DebugStatistics(
                                        totalRecords = statsResult.data.totalRecords,
                                        last24HoursRecords = statsResult.data.last24HoursRecords,
                                        successCount = statsResult.data.successCount,
                                        failureCount = statsResult.data.failureCount,
                                        duplicateCount = statsResult.data.duplicateCount,
                                        averageProcessingTime = statsResult.data.averageProcessingTime,
                                        averageConfidence = statsResult.data.averageConfidence
                                    )
                                )
                            }
                            is BaseResult.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    debugRecords = recordsResult.data,
                                    statistics = null
                                )
                            }
                        }
                    }
                    is BaseResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = recordsResult.exception.message ?: "加载失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "未知错误"
                )
            }
        }
    }
    
    /**
     * 刷新调试数据
     */
    fun refreshDebugData() {
        loadDebugData()
    }
    
    /**
     * 切换脱敏模式
     */
    fun toggleMaskingMode() {
        _uiState.value = _uiState.value.copy(
            maskSensitiveData = !_uiState.value.maskSensitiveData
        )
    }
    
    /**
     * 导出调试数据
     */
    fun exportDebugData() {
        viewModelScope.launch {
            try {
                when (val result = debugRepository.exportDebugRecords(
                    masked = _uiState.value.maskSensitiveData,
                    limit = 1000
                )) {
                    is BaseResult.Success -> {
                        // TODO: 保存文件到设备存储
                        _uiState.value = _uiState.value.copy(
                            exportSuccess = "调试数据已导出 (${result.data.length} 字符)"
                        )
                    }
                    is BaseResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "导出失败: ${result.exception.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "导出异常: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除导出成功状态
     */
    fun clearExportSuccess() {
        _uiState.value = _uiState.value.copy(exportSuccess = null)
    }
    
    /**
     * 根据状态过滤记录
     */
    fun filterByStatus(statuses: List<AutoLedgerDebugRecord.ProcessingStatus>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = debugRepository.getDebugRecordsByStatus(statuses, 100)) {
                is BaseResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        debugRecords = result.data,
                        currentFilter = statuses
                    )
                }
                is BaseResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "过滤失败"
                    )
                }
            }
        }
    }
    
    /**
     * 清除过滤器，显示所有记录
     */
    fun clearFilter() {
        _uiState.value = _uiState.value.copy(currentFilter = null)
        loadDebugData()
    }
    
    /**
     * UI状态
     */
    data class DebugUiState(
        val isLoading: Boolean = false,
        val debugRecords: List<AutoLedgerDebugRecord> = emptyList(),
        val statistics: DebugStatistics? = null,
        val maskSensitiveData: Boolean = true,
        val error: String? = null,
        val exportSuccess: String? = null,
        val currentFilter: List<AutoLedgerDebugRecord.ProcessingStatus>? = null,
        val listenerDiagnostics: ListenerDiagnostics? = null,
        val emitWithoutKeywords: Boolean = true,
        val emitGroupSummary: Boolean = false
    )
    
    /**
     * 调试统计信息
     */
    data class DebugStatistics(
        val totalRecords: Int,
        val last24HoursRecords: Int,
        val successCount: Int,
        val failureCount: Int,
        val duplicateCount: Int,
        val averageProcessingTime: Double,
        val averageConfidence: Double
    )

    data class ListenerDiagnostics(
        val totalEmitted: Int,
        val emittedByKeywords: Int,
        val emittedWithoutKeywords: Int,
        val skippedGroupSummary: Int,
        val skippedUnsupportedPackage: Int,
        val skippedNoKeywordsByConfig: Int
    )
}

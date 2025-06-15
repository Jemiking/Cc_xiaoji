package com.ccxiaoji.feature.schedule.presentation.viewmodel

import android.net.Uri
import com.ccxiaoji.core.common.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.schedule.domain.usecase.BackupDatabaseUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.ClearAllDataUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.RestoreDatabaseUseCase
import com.ccxiaoji.feature.schedule.presentation.ui.settings.DarkModeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 设置界面的ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupDatabaseUseCase: BackupDatabaseUseCase,
    private val restoreDatabaseUseCase: RestoreDatabaseUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase
) : BaseViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * 更新通知开关
     */
    fun updateNotificationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationEnabled = enabled) }
        saveSettings()
    }
    
    /**
     * 更新通知时间
     */
    fun updateNotificationTime(time: String) {
        _uiState.update { it.copy(notificationTime = time) }
        saveSettings()
    }
    
    /**
     * 更新自动备份开关
     */
    fun updateAutoBackupEnabled(enabled: Boolean) {
        _uiState.update { it.copy(autoBackupEnabled = enabled) }
        saveSettings()
    }
    
    /**
     * 执行备份
     */
    fun performBackup(backupUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            backupDatabaseUseCase(backupUri)
                .onSuccess { backupPath ->
                    val backupTime = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    )
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastBackupTime = backupTime,
                            successMessage = if (backupUri != null) {
                                "备份成功"
                            } else {
                                "备份成功: $backupPath"
                            }
                        )
                    }
                    
                    // 清理旧备份（保留最近5个）
                    if (backupUri == null) {
                        backupDatabaseUseCase.deleteOldBackups(5)
                    }
                    
                    saveSettings()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "备份失败: ${error.message}"
                        )
                    }
                }
        }
    }
    
    /**
     * 执行数据恢复
     */
    fun restoreDatabase(backupUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            restoreDatabaseUseCase(backupUri)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "恢复成功，请重启应用生效"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "恢复失败: ${error.message}"
                        )
                    }
                }
        }
    }
    
    /**
     * 清除所有数据
     */
    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 先获取数据统计
            val statistics = clearAllDataUseCase.getDataStatistics()
            
            clearAllDataUseCase()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "已清除所有数据：${statistics.shiftCount}个班次，${statistics.scheduleCount}条排班记录"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "清除失败: ${error.message}"
                        )
                    }
                }
        }
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 更新深色模式选项
     */
    fun updateDarkMode(option: DarkModeOption) {
        _uiState.update { it.copy(darkMode = option) }
        saveSettings()
    }
    
    /**
     * 设置一周开始日
     */
    fun setWeekStartDay(dayOfWeek: DayOfWeek) {
        val weekStartDayText = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> "星期日"
            DayOfWeek.MONDAY -> "星期一"
            else -> "星期一"
        }
        _uiState.update { 
            it.copy(
                weekStartDay = weekStartDayText,
                weekStartDayValue = dayOfWeek
            )
        }
        saveSettings()
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        // 从SharedPreferences或数据库加载设置
        _uiState.update {
            it.copy(
                notificationEnabled = true,
                notificationTime = "08:00",
                weekStartDay = "星期一",
                autoBackupEnabled = false,
                lastBackupTime = null,
                darkMode = DarkModeOption.SYSTEM,
                appVersion = "1.0"
            )
        }
    }
    
    /**
     * 保存设置
     */
    private fun saveSettings() {
        // 保存到 SharedPreferences 或数据库
        // 功能待实现
    }
}

/**
 * 设置界面UI状态
 */
data class SettingsUiState(
    // 通知设置
    val notificationEnabled: Boolean = true,
    val notificationTime: String = "08:00",
    
    // 通用设置
    val weekStartDay: String = "星期一",
    val weekStartDayValue: DayOfWeek = DayOfWeek.MONDAY,
    
    // 数据管理
    val autoBackupEnabled: Boolean = false,
    val lastBackupTime: String? = null,
    
    // 外观
    val darkMode: DarkModeOption = DarkModeOption.SYSTEM,
    val isDarkMode: Boolean = false,
    
    // 应用信息
    val appVersion: String = "1.0",
    
    // 状态
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
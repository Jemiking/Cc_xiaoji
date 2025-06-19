package com.ccxiaoji.feature.schedule.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.schedule.domain.usecase.BackupDatabaseUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.ClearAllDataUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.RestoreDatabaseUseCase
import com.ccxiaoji.feature.schedule.notification.ScheduleNotificationScheduler
import com.ccxiaoji.feature.schedule.presentation.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val notificationScheduler: ScheduleNotificationScheduler,
    private val themeManager: ThemeManager,
    private val backupDatabaseUseCase: BackupDatabaseUseCase,
    private val restoreDatabaseUseCase: RestoreDatabaseUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
        
        // 监听主题变化
        themeManager.isDarkMode
            .onEach { isDarkMode ->
                _uiState.update { it.copy(isDarkMode = isDarkMode) }
            }
            .launchIn(viewModelScope)
            
        // 监听一周开始日变化
        themeManager.weekStartDay
            .onEach { weekStartDay ->
                val weekStartDayText = when (weekStartDay) {
                    DayOfWeek.SUNDAY -> "星期日"
                    DayOfWeek.MONDAY -> "星期一"
                    else -> "星期一"
                }
                _uiState.update { it.copy(weekStartDay = weekStartDayText, weekStartDayValue = weekStartDay) }
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * 更新通知开关
     */
    fun updateNotificationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationEnabled = enabled) }
        notificationScheduler.scheduleDailyReminder(enabled, _uiState.value.notificationTime)
    }
    
    /**
     * 更新通知时间
     */
    fun updateNotificationTime(time: String) {
        _uiState.update { it.copy(notificationTime = time) }
        if (_uiState.value.notificationEnabled) {
            notificationScheduler.scheduleDailyReminder(true, time)
        }
    }
    
    /**
     * 发送测试通知
     */
    fun sendTestNotification() {
        notificationScheduler.sendTestNotification()
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
     * 切换深色模式
     */
    fun toggleDarkMode() {
        viewModelScope.launch {
            themeManager.toggleDarkMode()
        }
    }
    
    /**
     * 设置深色模式
     */
    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(isDarkMode)
        }
    }
    
    /**
     * 设置一周开始日
     */
    fun setWeekStartDay(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            themeManager.setWeekStartDay(dayOfWeek)
        }
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        // 从NotificationScheduler获取通知设置
        val (notificationEnabled, notificationTime) = notificationScheduler.getNotificationSettings()
        
        _uiState.update {
            it.copy(
                notificationEnabled = notificationEnabled,
                notificationTime = notificationTime,
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
        // TODO: 保存到 SharedPreferences 或数据库
        // 暂时不实现
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
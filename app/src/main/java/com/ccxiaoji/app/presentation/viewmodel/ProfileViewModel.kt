package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.app.data.sync.SyncManager
import com.ccxiaoji.shared.sync.api.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userApi: UserApi,
    private val syncManager: SyncManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserInfo()
        observeSyncStatus()
    }
    
    private fun loadUserInfo() {
        viewModelScope.launch {
            userApi.getCurrentUserFlow().collect { user ->
                _uiState.update { 
                    it.copy(
                        userEmail = user?.email ?: "未登录"
                    )
                }
            }
        }
        
        viewModelScope.launch {
            val lastSync = userApi.getLastSyncTime()
            if (lastSync > 0) {
                val dateTime = Instant.fromEpochMilliseconds(lastSync)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                
                _uiState.update { 
                    it.copy(lastSyncTime = dateTime)
                }
            }
        }
    }
    
    private fun observeSyncStatus() {
        viewModelScope.launch {
            syncManager.getSyncStatus().collect { status ->
                _uiState.update { 
                    it.copy(
                        isSyncing = status == SyncState.SYNCING
                    )
                }
            }
        }
    }
    
    fun syncNow() {
        syncManager.syncNow()
    }
    
    fun syncData() {
        syncNow()
    }
    
    fun logout() {
        viewModelScope.launch {
            userApi.logout()
            // Navigation to login screen would be handled by navigation component
        }
    }
    
    fun backupData() {
        viewModelScope.launch {
            // TODO: Implement data backup
            syncNow()
        }
    }
    
    fun restoreData() {
        viewModelScope.launch {
            // TODO: Implement data restoration
            syncNow()
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            // TODO: Implement cache clearing
            _uiState.update { it.copy(cacheSize = "0 MB") }
        }
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            // TODO: Implement update checking
        }
    }
}

data class ProfileUiState(
    val userEmail: String = "",
    val username: String? = null,
    val signature: String? = null,
    val lastSyncTime: String? = null,
    val isSyncing: Boolean = false,
    val cacheSize: String = "0 MB",
    val currentTheme: String = "跟随系统",
    val fontSize: String = "标准",
    val isAppLockEnabled: Boolean = false,
    val appVersion: String = "1.0.0"
)
package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.shared.notification.api.NotificationEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 权限引导页面的ViewModel
 * 
 * 管理自动记账功能所需的各项权限状态
 */
@HiltViewModel
class PermissionGuideViewModel @Inject constructor(
    private val notificationEventRepository: NotificationEventRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(PermissionGuideState())
    val state: StateFlow<PermissionGuideState> = _state.asStateFlow()
    
    /**
     * 更新权限状态
     */
    fun updatePermissionStatus(context: Context) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                notificationListenerEnabled = isNotificationListenerEnabled(context),
                postNotificationsGranted = isPostNotificationGranted(context),
                canDrawOverlays = canDrawOverlays(context),
                ignoreBatteryOptimizations = isIgnoringBatteryOptimizations(context)
            )
        }
    }
    
    /**
     * 请求通知发布权限（Android 13+）
     */
    fun requestPostNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATIONS
                )
            }
        }
    }
    
    /**
     * 检查通知监听权限是否已开启
     */
    private fun isNotificationListenerEnabled(context: Context): Boolean {
        return try {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            enabledListeners?.contains(context.packageName) == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查通知发布权限（Android 13+）
     */
    private fun isPostNotificationGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13以下不需要此权限
        }
    }
    
    /**
     * 检查悬浮窗权限
     */
    private fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // Android 6.0以下不需要此权限
        }
    }
    
    /**
     * 检查电池优化白名单
     */
    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Android 6.0以下不需要此设置
        }
    }
    
    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }
}

/**
 * 权限引导页面状态
 */
data class PermissionGuideState(
    val notificationListenerEnabled: Boolean = false,
    val postNotificationsGranted: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val ignoreBatteryOptimizations: Boolean = false
) {
    /**
     * 检查所有必需权限是否已授予
     */
    val allRequiredPermissionsGranted: Boolean
        get() = notificationListenerEnabled
    
    /**
     * 检查所有推荐权限是否已授予
     */
    val allRecommendedPermissionsGranted: Boolean
        get() = allRequiredPermissionsGranted && postNotificationsGranted && ignoreBatteryOptimizations
}
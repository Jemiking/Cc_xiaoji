package com.ccxiaoji.feature.ledger.presentation.component

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.LocationData
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun LocationPicker(
    selectedLocation: LocationData?,
    onLocationSelected: (LocationData?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    
    // 位置权限请求启动器
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (fineLocationGranted || coarseLocationGranted) {
            // 权限获得，获取当前位置
            scope.launch {
                getCurrentLocation(
                    context = context,
                    onLocationReceived = { location ->
                        onLocationSelected(location)
                        isLoadingLocation = false
                        locationError = null
                    },
                    onError = { error ->
                        locationError = error
                        isLoadingLocation = false
                    }
                )
            }
        } else {
            locationError = "需要位置权限才能获取当前位置"
            isLoadingLocation = false
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        Text(
            text = "位置信息",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 位置显示卡片
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                // 当前位置信息
                if (selectedLocation != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "位置",
                                tint = DesignTokens.BrandColors.Ledger
                            )
                            Column {
                                selectedLocation.getDisplayAddress()?.let { address ->
                                    Text(
                                        text = address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (selectedLocation.hasValidCoordinates()) {
                                    Text(
                                        text = "精度: ${if (selectedLocation.hasGoodPrecision()) "高" else "低"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // 清除位置按钮
                        IconButton(
                            onClick = { onLocationSelected(null) }
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "清除位置",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // 无位置信息时的提示
                    Text(
                        text = "未设置位置信息",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 错误信息
                locationError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // 获取当前位置按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    Button(
                        onClick = {
                            isLoadingLocation = true
                            locationError = null
                            
                            // 检查权限
                            val hasLocationPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (hasLocationPermission) {
                                scope.launch {
                                    getCurrentLocation(
                                        context = context,
                                        onLocationReceived = { location ->
                                            onLocationSelected(location)
                                            isLoadingLocation = false
                                            locationError = null
                                        },
                                        onError = { error ->
                                            locationError = error
                                            isLoadingLocation = false
                                        }
                                    )
                                }
                            } else {
                                // 请求权限
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !isLoadingLocation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignTokens.BrandColors.Ledger
                        )
                    ) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "获取当前位置"
                            )
                        }
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                        Text(
                            text = if (isLoadingLocation) "定位中..." else "获取当前位置"
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取当前位置 - 使用Android原生LocationManager
 * 兼容所有Android设备，不依赖Google Play Services
 */
private suspend fun getCurrentLocation(
    context: Context,
    onLocationReceived: (LocationData) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // 检查位置服务是否可用
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && 
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            onError("请在设置中开启位置服务")
            return
        }
        
        // 先尝试获取最后已知位置
        val lastKnownLocation = getLastKnownLocation(locationManager)
        if (lastKnownLocation != null) {
            val locationData = convertToLocationData(lastKnownLocation, context)
            onLocationReceived(locationData)
            return
        }
        
        // 如果没有最后已知位置，请求一次性位置更新
        val location = withTimeoutOrNull(15000) { // 15秒超时
            requestSingleLocationUpdate(locationManager)
        }
        
        if (location != null) {
            val locationData = convertToLocationData(location, context)
            onLocationReceived(locationData)
        } else {
            onError("位置获取超时，请确保在开阔地带并开启位置服务")
        }
    } catch (e: SecurityException) {
        onError("位置权限被拒绝")
    } catch (e: Exception) {
        onError("位置获取失败: ${e.message}")
    }
}

/**
 * 获取最后已知位置
 */
private fun getLastKnownLocation(locationManager: LocationManager): Location? {
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    
    return providers.mapNotNull { provider ->
        try {
            if (locationManager.isProviderEnabled(provider)) {
                locationManager.getLastKnownLocation(provider)
            } else null
        } catch (e: SecurityException) {
            null
        }
    }.maxByOrNull { it.time } // 选择最新的位置
}

/**
 * 请求单次位置更新
 */
private suspend fun requestSingleLocationUpdate(locationManager: LocationManager): Location? {
    return suspendCancellableCoroutine { continuation ->
        val providers = mutableListOf<String>()
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            providers.add(LocationManager.GPS_PROVIDER)
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            providers.add(LocationManager.NETWORK_PROVIDER)
        }
        
        if (providers.isEmpty()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 移除监听器
                try {
                    locationManager.removeUpdates(this)
                } catch (e: SecurityException) {
                    // 忽略
                }
                continuation.resume(location)
            }
            
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        
        // 尝试从多个provider获取位置
        var hasActiveRequest = false
        providers.forEach { provider ->
            try {
                locationManager.requestSingleUpdate(provider, locationListener, null)
                hasActiveRequest = true
            } catch (e: SecurityException) {
                // 忽略这个provider
            }
        }
        
        if (!hasActiveRequest) {
            continuation.resume(null)
        }
        
        // 清理资源
        continuation.invokeOnCancellation {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: SecurityException) {
                // 忽略
            }
        }
    }
}

/**
 * 转换Location为LocationData
 */
private suspend fun convertToLocationData(location: Location, context: Context): LocationData {
    // 地理编码获取地址
    val address = try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        addresses?.firstOrNull()?.getAddressLine(0)
    } catch (e: Exception) {
        null
    }
    
    return LocationData(
        latitude = location.latitude,
        longitude = location.longitude,
        address = address,
        precision = if (location.hasAccuracy()) location.accuracy else null,
        provider = location.provider
    )
}
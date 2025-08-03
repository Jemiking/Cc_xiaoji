package com.ccxiaoji.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一的文件选择管理器
 * 实现SAF权限持久化和兼容性处理
 */
@Singleton
class FilePickerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FilePickerManager"
    }

    /**
     * 创建文件选择器
     * 返回一个ActivityResultLauncher用于启动文件选择
     */
    fun createFilePicker(): ActivityResultLauncher<Array<String>> {
        throw UnsupportedOperationException("此方法需要在Activity或Fragment中调用")
    }

    /**
     * 处理选择的URI
     * 自动处理权限持久化
     */
    fun handleUri(uri: Uri): Result<PersistableUri> {
        return try {
            // Android 10+ Scoped Storage处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d(TAG, "成功获取持久权限: $uri")
                } catch (e: SecurityException) {
                    Log.w(TAG, "无法获取持久权限，使用临时权限", e)
                }
            }
            
            // 验证权限
            val canRead = context.contentResolver.query(uri, null, null, null, null)?.use {
                it.moveToFirst()
            } ?: false
            
            if (canRead) {
                Result.success(PersistableUri(uri, System.currentTimeMillis(), true))
            } else {
                // 尝试打开输入流作为第二次验证
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    inputStream.close()
                    Result.success(PersistableUri(uri, System.currentTimeMillis(), false))
                } else {
                    Result.failure(SecurityException("无法读取文件"))
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "权限错误", e)
            // 降级到临时权限
            Result.success(PersistableUri(uri, System.currentTimeMillis(), false))
        } catch (e: Exception) {
            Log.e(TAG, "处理URI时出错", e)
            Result.failure(e)
        }
    }

    /**
     * 释放URI权限
     */
    fun releaseUriPermission(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                context.contentResolver.releasePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Log.d(TAG, "释放URI权限: $uri")
            } catch (e: Exception) {
                Log.w(TAG, "释放权限失败", e)
            }
        }
    }

    /**
     * 检查是否为MIUI设备
     */
    private fun isMiuiDevice(): Boolean {
        return try {
            val prop = Class.forName("android.os.SystemProperties")
            val get = prop.getMethod("get", String::class.java)
            val miuiVersion = get.invoke(null, "ro.miui.ui.version.name") as? String
            miuiVersion?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 持久化URI
 */
data class PersistableUri(
    val uri: Uri,
    val timestamp: Long,
    val isPersistent: Boolean
) : Serializable


/**
 * 文件选择器扩展函数
 * 在Activity或Fragment中使用
 */
fun androidx.activity.ComponentActivity.registerFilePickerWithManager(
    filePickerManager: FilePickerManager,
    onFileSelected: (Result<PersistableUri>) -> Unit,
    onSelectionCancelled: () -> Unit = {}
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = filePickerManager.handleUri(uri)
            onFileSelected(result)
        } else {
            onSelectionCancelled()
        }
    }
}

/**
 * 备选方案：GetContent
 */
fun androidx.activity.ComponentActivity.registerFilePickerFallback(
    filePickerManager: FilePickerManager,
    onFileSelected: (Result<PersistableUri>) -> Unit,
    onSelectionCancelled: () -> Unit = {}
): ActivityResultLauncher<String> {
    return registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = filePickerManager.handleUri(uri)
            onFileSelected(result)
        } else {
            onSelectionCancelled()
        }
    }
}
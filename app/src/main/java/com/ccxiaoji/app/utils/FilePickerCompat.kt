package com.ccxiaoji.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/**
 * 文件选择器兼容性工具类
 * 用于处理MIUI等定制ROM的兼容性问题
 */
object FilePickerCompat {
    private const val TAG = "FilePickerCompat"
    
    /**
     * 注册一个安全的文件选择器
     * 这个方法会自动处理各种兼容性问题，包括MIUI的NPE问题
     */
    fun ComponentActivity.registerSafeFilePicker(
        onFileSelected: (Uri) -> Unit,
        onSelectionCancelled: () -> Unit = {},
        onError: (String) -> Unit = {}
    ): ActivityResultLauncher<String> {
        // 注册一个特殊的launcher，它会拦截并处理异常
        return registerForActivityResult(
            object : ActivityResultContracts.GetContent() {
                override fun createIntent(context: Context, input: String): Intent {
                    val intent = super.createIntent(context, input)
                    // 添加额外的标志以提高兼容性
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    // MIUI特定优化
                    if (isMiuiDevice()) {
                        intent.putExtra("android.provider.extra.INITIAL_URI", "")
                        intent.putExtra("android.provider.extra.SHOW_ADVANCED", false)
                    }
                    return intent
                }
            }
        ) { uri: Uri? ->
            try {
                if (uri != null) {
                    // 验证URI是否有效
                    this.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            onFileSelected(uri)
                        } else {
                            onError("选择的文件无法访问")
                        }
                    } ?: run {
                        // 如果查询失败，仍然尝试使用URI
                        Log.w(TAG, "无法查询文件信息，但将尝试使用URI")
                        onFileSelected(uri)
                    }
                } else {
                    onSelectionCancelled()
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理文件选择结果时出错", e)
                onError("文件选择失败: ${e.message}")
            }
        }
    }
    
    /**
     * 创建一个安全的文件选择Intent
     * 用于传统的startActivityForResult方式
     */
    fun createSafeFilePickerIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            // 添加常见的MIME类型
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/json",
                "text/csv",
                "text/plain"
            ))
            
            // MIUI优化
            if (isMiuiDevice()) {
                putExtra("android.provider.extra.SHOW_ADVANCED", true)
                putExtra("android.content.extra.SHOW_ADVANCED", true)
            }
        }
    }
    
    /**
     * 安全地处理Activity结果
     * 防止NPE和其他异常
     */
    fun handleActivityResultSafely(
        resultCode: Int,
        data: Intent?,
        onFileSelected: (Uri) -> Unit,
        onSelectionCancelled: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // 首先尝试获取data URI
                data.data?.let { uri ->
                    onFileSelected(uri)
                    return
                }
                
                // 如果data为空，尝试从extras获取
                try {
                    val extras = data.extras
                    if (extras != null && extras.size() > 0) {
                        // 尝试各种可能的key
                        val possibleKeys = listOf(
                            "output", "result", "uri", "path", 
                            "file_uri", "selected_uri", "android.intent.extra.STREAM"
                        )
                        
                        for (key in possibleKeys) {
                            when (val value = extras.get(key)) {
                                is Uri -> {
                                    onFileSelected(value)
                                    return
                                }
                                is String -> {
                                    try {
                                        val uri = Uri.parse(value)
                                        onFileSelected(uri)
                                        return
                                    } catch (e: Exception) {
                                        // 继续尝试其他key
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "无法访问Intent extras", e)
                }
                
                onError("无法获取选择的文件")
            } else {
                onSelectionCancelled()
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理Activity结果时出现异常", e)
            onError("处理文件选择结果失败: ${e.message}")
        }
    }
    
    /**
     * 检测是否为MIUI设备
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
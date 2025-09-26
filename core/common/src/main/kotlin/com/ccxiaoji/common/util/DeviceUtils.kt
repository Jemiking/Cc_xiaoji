package com.ccxiaoji.common.util

import android.content.Context
import android.os.Build

object DeviceUtils {
    private fun getSystemProperty(key: String): String? {
        return try {
            val cl = Class.forName("android.os.SystemProperties")
            val get = cl.getMethod("get", String::class.java)
            val value = get.invoke(null, key) as? String
            if (value.isNullOrBlank()) null else value
        } catch (_: Throwable) {
            null
        }
    }

    fun isHyperOSOrMIUI(): Boolean {
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val brand = Build.BRAND?.lowercase() ?: ""
        val isXiaomiFamily = listOf(manufacturer, brand).any { it.contains("xiaomi") || it.contains("redmi") || it.contains("poco") }
        val miuiProp = getSystemProperty("ro.miui.ui.version.name")
        val hyperOSProp = getSystemProperty("ro.hyperos.version") ?: getSystemProperty("ro.miui.version.code_time")
        return isXiaomiFamily || miuiProp != null || hyperOSProp != null
    }

    fun isLongShotInteropRecommended(context: Context): Boolean {
        // 目前策略：在小米/Redmi/POCO/HyperOS/MIUI 设备上启用互操作兼容
        return isHyperOSOrMIUI()
    }
}


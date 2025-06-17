package com.ccxiaoji.app.di

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * 数据库调试辅助类
 * 用于在开发环境中安全地判断是否启用特殊配置
 */
object DatabaseModuleDebugHelper {
    
    /**
     * 判断当前是否为调试模式
     * 使用 ApplicationInfo 而不是 BuildConfig，避免编译依赖问题
     */
    fun isDebugMode(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    
    /**
     * 获取数据库名称
     * 可以在调试模式下使用不同的数据库文件
     */
    fun getDatabaseName(context: Context): String {
        return if (isDebugMode(context)) {
            "cc_xiaoji_debug.db"  // 开发环境使用独立数据库
        } else {
            "cc_xiaoji.db"       // 生产环境数据库
        }
    }
}
package com.ccxiaoji.feature.plan.util.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 安全的数据库调用包装器
 * 确保数据库操作在IO线程执行，并处理异常
 */
suspend inline fun <T> safeDbCall(
    crossinline action: suspend () -> T
): T {
    return withContext(Dispatchers.IO) {
        try {
            action()
        } catch (e: Exception) {
            throw e
        }
    }
}
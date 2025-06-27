package com.ccxiaoji.feature.plan.util.core

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.UnknownHostException

/**
 * 错误处理扩展函数
 */

/**
 * 安全执行数据库操作
 */
suspend fun <T> safeDbCall(
    errorMessage: String = "数据库操作失败",
    call: suspend () -> T
): Result<T> {
    return try {
        Result.success(call())
    } catch (e: SQLiteException) {
        Result.failure(DatabaseException("$errorMessage: 数据库错误", e))
    } catch (e: CancellationException) {
        throw e // 不要捕获协程取消异常
    } catch (e: Exception) {
        Result.failure(DatabaseException(errorMessage, e))
    }
}

/**
 * 安全执行网络操作
 */
suspend fun <T> safeNetworkCall(
    errorMessage: String = "网络请求失败",
    call: suspend () -> T
): Result<T> {
    return try {
        Result.success(call())
    } catch (e: UnknownHostException) {
        Result.failure(NetworkException("无法连接到服务器", e))
    } catch (e: IOException) {
        Result.failure(NetworkException("$errorMessage: 网络错误", e))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(NetworkException(errorMessage, e))
    }
}

/**
 * 安全执行任意操作
 */
suspend fun <T> safeCall(
    errorMessage: String = "操作失败",
    call: suspend () -> T
): Result<T> {
    return try {
        Result.success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(AppException(errorMessage, e))
    }
}

/**
 * 自定义异常类基类
 */
open class AppException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)

/**
 * 数据库异常
 */
class DatabaseException(
    message: String,
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * 网络异常
 */
class NetworkException(
    message: String,
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * 业务逻辑异常
 */
class BusinessException(
    message: String,
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * 验证异常
 */
class ValidationException(
    message: String,
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * 获取用户友好的错误消息
 */
fun Throwable.getUserMessage(): String {
    return when (this) {
        is ValidationException -> message
        is BusinessException -> message
        is DatabaseException -> "数据库错误：${message}"
        is NetworkException -> "网络错误：${message}"
        is SQLiteException -> "数据库操作失败"
        is UnknownHostException -> "无法连接到服务器"
        is IOException -> "网络连接失败"
        else -> message ?: "未知错误"
    }
}
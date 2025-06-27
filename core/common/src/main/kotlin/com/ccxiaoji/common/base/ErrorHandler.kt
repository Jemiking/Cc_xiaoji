package com.ccxiaoji.common.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * 通用错误类型
 */
sealed class AppError : Exception() {
    data class NetworkError(override val message: String = "网络连接失败") : AppError()
    data class DatabaseError(override val message: String = "数据库操作失败") : AppError()
    data class ValidationError(override val message: String) : AppError()
    data class UnknownError(override val message: String = "未知错误") : AppError()
}

/**
 * Flow的错误处理扩展
 */
fun <T> Flow<T>.handleErrors(): Flow<BaseResult<T>> = this
    .map<T, BaseResult<T>> { BaseResult.Success(it) }
    .catch { e ->
        val error = when (e) {
            is AppError -> e
            else -> AppError.UnknownError(e.message ?: "发生未知错误")
        }
        emit(BaseResult.Error(error))
    }

/**
 * 将错误转换为用户友好的消息
 */
fun Exception.toUserMessage(): String = when (this) {
    is AppError.NetworkError -> message
    is AppError.DatabaseError -> message
    is AppError.ValidationError -> message
    is AppError.UnknownError -> message
    else -> "操作失败，请稍后重试"
}
package com.ccxiaoji.feature.plan.utils

/**
 * 通用结果包装类
 * 用于处理操作的成功或失败状态
 */
sealed class Result<out T> {
    /**
     * 操作成功
     * @param data 成功返回的数据
     */
    data class Success<out T>(val data: T) : Result<T>()
    
    /**
     * 操作失败
     * @param message 错误信息
     * @param exception 异常（可选）
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : Result<Nothing>()
    
    /**
     * 获取成功的数据，如果失败则返回null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * 获取成功的数据，如果失败则返回默认值
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default
    }
    
    /**
     * 如果成功则执行给定的操作
     */
    inline fun onSuccess(action: (value: T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    /**
     * 如果失败则执行给定的操作
     */
    inline fun onError(action: (message: String) -> Unit): Result<T> {
        if (this is Error) {
            action(message)
        }
        return this
    }
    
    /**
     * 映射成功的结果
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    /**
     * 映射失败的结果
     */
    inline fun mapError(transform: (String) -> String): Result<T> = when (this) {
        is Success -> this
        is Error -> Error(transform(message), exception)
    }
}
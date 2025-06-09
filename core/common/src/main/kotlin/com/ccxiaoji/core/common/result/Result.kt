package com.ccxiaoji.core.common.result

/**
 * 通用结果包装类，用于处理成功和失败的情况
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
    
    val isSuccess: Boolean
        get() = this is Success
    
    val isError: Boolean
        get() = this is Error
    
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * 获取成功的数据，如果不是Success状态则返回null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * 获取成功的数据，如果不是Success状态则返回默认值
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }
    
    /**
     * 获取成功的数据，如果不是Success状态则抛出异常
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("结果还在加载中")
    }
    
    /**
     * 映射成功的结果
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception)
        is Loading -> Loading
    }
    
    /**
     * 当结果成功时执行操作
     */
    inline fun onSuccess(action: (value: T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    /**
     * 当结果失败时执行操作
     */
    inline fun onError(action: (exception: Throwable) -> Unit): Result<T> {
        if (this is Error) {
            action(exception)
        }
        return this
    }
    
    /**
     * 当结果正在加载时执行操作
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) {
            action()
        }
        return this
    }
}

/**
 * 将任意值包装为Success结果
 */
fun <T> T.asSuccess(): Result<T> = Result.Success(this)

/**
 * 将异常包装为Error结果
 */
fun Throwable.asError(): Result<Nothing> = Result.Error(this)
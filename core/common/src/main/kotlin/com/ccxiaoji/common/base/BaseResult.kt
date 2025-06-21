package com.ccxiaoji.common.base

/**
 * 统一的结果封装类，用于处理成功和失败情况
 * 
 * 这个密封类提供了一种类型安全的方式来处理操作结果，避免了异常的隐式传播。
 * 通过使用BaseResult，可以强制调用者显式处理成功和失败两种情况。
 * 
 * @param T 成功时返回的数据类型
 * 
 * @sample
 * ```kotlin
 * val result = repository.getData()
 * result.onSuccess { data ->
 *     // 处理成功情况
 * }.onError { exception ->
 *     // 处理错误情况
 * }
 * ```
 */
sealed class BaseResult<out T> {
    /**
     * 表示操作成功的结果
     * @property data 成功时返回的数据
     */
    data class Success<T>(val data: T) : BaseResult<T>()
    
    /**
     * 表示操作失败的结果
     * @property exception 失败时的异常信息
     */
    data class Error(val exception: Exception) : BaseResult<Nothing>()
    
    /**
     * 转换成功结果的数据类型
     * @param transform 转换函数
     * @return 转换后的新Result对象
     */
    inline fun <R> map(transform: (T) -> R): BaseResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    /**
     * 在成功时执行操作
     * @param action 成功时要执行的操作
     * @return 返回自身以支持链式调用
     */
    inline fun onSuccess(action: (T) -> Unit): BaseResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * 在失败时执行操作
     * @param action 失败时要执行的操作
     * @return 返回自身以支持链式调用
     */
    inline fun onError(action: (Exception) -> Unit): BaseResult<T> {
        if (this is Error) action(exception)
        return this
    }
    
    /**
     * 安全获取数据，失败时返回null
     * @return 成功时返回数据，失败时返回null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * 获取数据或抛出异常
     * @return 成功时返回数据
     * @throws Exception 失败时抛出包含的异常
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }
}

/**
 * 安全执行代码块并返回Result
 * 
 * 这个函数会捕获代码块中抛出的所有异常，并将其包装为BaseResult.Error返回。
 * 如果代码块成功执行，则返回BaseResult.Success包装的结果。
 * 
 * @param T 代码块返回值类型
 * @param action 要执行的代码块
 * @return BaseResult<T> 包装后的执行结果
 * 
 * @sample
 * ```kotlin
 * val result = safeCall {
 *     // 可能抛出异常的代码
 *     riskyOperation()
 * }
 * ```
 */
inline fun <T> safeCall(action: () -> T): BaseResult<T> {
    return try {
        BaseResult.Success(action())
    } catch (e: Exception) {
        BaseResult.Error(e)
    }
}

/**
 * 安全执行suspend函数并返回Result
 * 
 * 这个函数是safeCall的挂起版本，用于在协程中安全执行可能抛出异常的挂起函数。
 * 它会捕获所有异常并包装为BaseResult.Error返回。
 * 
 * @param T 挂起函数返回值类型
 * @param action 要执行的挂起函数
 * @return BaseResult<T> 包装后的执行结果
 * 
 * @sample
 * ```kotlin
 * val result = safeSuspendCall {
 *     // 可能抛出异常的挂起函数
 *     networkRepository.fetchData()
 * }
 * ```
 */
suspend inline fun <T> safeSuspendCall(crossinline action: suspend () -> T): BaseResult<T> {
    return try {
        BaseResult.Success(action())
    } catch (e: Exception) {
        BaseResult.Error(e)
    }
}
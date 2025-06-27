package com.ccxiaoji.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel基类，提供统一的错误处理机制
 */
abstract class BaseViewModel : ViewModel() {
    
    // 错误状态
    private val _errorState = MutableStateFlow<ErrorState?>(null)
    val errorState: StateFlow<ErrorState?> = _errorState.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 全局异常处理器
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleError(exception)
    }
    
    /**
     * 带错误处理的协程启动方法
     */
    protected fun launchWithErrorHandling(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            try {
                _isLoading.value = true
                block()
            } catch (e: Exception) {
                onError?.invoke(e) ?: handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 处理错误的方法，子类可以重写以自定义错误处理逻辑
     */
    protected open fun handleError(throwable: Throwable) {
        val errorState = when (throwable) {
            is DomainException.NetworkException -> ErrorState(
                message = throwable.message ?: "网络连接失败",
                type = ErrorType.NETWORK
            )
            is DomainException.DataException -> ErrorState(
                message = throwable.message ?: "数据处理失败",
                type = ErrorType.DATA
            )
            is DomainException.ValidationException -> ErrorState(
                message = throwable.message ?: "数据验证失败",
                type = ErrorType.VALIDATION
            )
            else -> ErrorState(
                message = throwable.message ?: "未知错误",
                type = ErrorType.UNKNOWN
            )
        }
        _errorState.value = errorState
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _errorState.value = null
    }
    
    /**
     * 显示自定义错误消息
     */
    protected fun showError(message: String, type: ErrorType = ErrorType.UNKNOWN) {
        _errorState.value = ErrorState(message, type)
    }
    
    /**
     * 显示成功消息
     */
    protected fun showSuccess(message: String) {
        _errorState.value = ErrorState(message, ErrorType.SUCCESS)
    }
}

/**
 * 错误状态数据类
 */
data class ErrorState(
    val message: String,
    val type: ErrorType,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 错误类型枚举
 */
enum class ErrorType {
    NETWORK,      // 网络错误
    DATA,         // 数据错误
    VALIDATION,   // 验证错误
    PERMISSION,   // 权限错误
    UNKNOWN,      // 未知错误
    SUCCESS       // 成功消息（用于显示操作成功的提示）
}

/**
 * 统一的领域异常定义
 */
sealed class DomainException(message: String) : Exception(message) {
    class NetworkException(message: String = "网络连接失败") : DomainException(message)
    class DataException(message: String = "数据处理失败") : DomainException(message)
    class ValidationException(message: String = "数据验证失败") : DomainException(message)
    class PermissionException(message: String = "权限不足") : DomainException(message)
    class BusinessException(message: String) : DomainException(message)
}
package com.ccxiaoji.core.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 基础ViewModel，提供统一的错误处理和协程作用域
 */
abstract class BaseViewModel : ViewModel() {
    
    /**
     * 通用错误处理器
     */
    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        handleError(exception)
    }
    
    /**
     * 在主线程中执行协程，自动处理错误
     */
    protected fun launchSafely(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(errorHandler) {
            try {
                this.block()
            } catch (e: Exception) {
                onError?.invoke(e) ?: handleError(e)
            }
        }
    }
    
    /**
     * 处理错误，子类可以重写此方法以实现自定义错误处理
     */
    protected open fun handleError(throwable: Throwable) {
        // 默认错误处理逻辑
        throwable.printStackTrace()
    }
}
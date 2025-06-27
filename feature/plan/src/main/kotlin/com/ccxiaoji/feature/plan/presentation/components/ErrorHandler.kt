package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 全局错误消息类型
 */
data class ErrorMessage(
    val message: String,
    val action: ErrorAction? = null
)

/**
 * 错误操作
 */
data class ErrorAction(
    val label: String,
    val action: suspend () -> Unit
)

/**
 * 全局错误处理器
 */
object GlobalErrorHandler {
    private val _errorFlow = MutableSharedFlow<ErrorMessage>()
    val errorFlow: SharedFlow<ErrorMessage> = _errorFlow.asSharedFlow()
    
    /**
     * 显示错误消息
     */
    suspend fun showError(message: String, action: ErrorAction? = null) {
        _errorFlow.emit(ErrorMessage(message, action))
    }
    
    /**
     * 显示错误消息（非挂起版本）
     */
    fun showErrorAsync(message: String, action: ErrorAction? = null) {
        kotlinx.coroutines.GlobalScope.launch {
            showError(message, action)
        }
    }
}

/**
 * 错误处理Composable
 */
@Composable
fun ErrorHandler() {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // 监听错误消息
    LaunchedEffect(Unit) {
        GlobalErrorHandler.errorFlow.collect { errorMessage ->
            val result = snackbarHostState.showSnackbar(
                message = errorMessage.message,
                actionLabel = errorMessage.action?.label,
                duration = if (errorMessage.action != null) {
                    SnackbarDuration.Long
                } else {
                    SnackbarDuration.Short
                }
            )
            
            if (result == SnackbarResult.ActionPerformed) {
                errorMessage.action?.let { action ->
                    coroutineScope.launch {
                        action.action()
                    }
                }
            }
        }
    }
    
    SnackbarHost(hostState = snackbarHostState)
}

/**
 * Toast错误提示
 */
@Composable
fun ToastErrorHandler() {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        GlobalErrorHandler.errorFlow.collect { errorMessage ->
            Toast.makeText(context, errorMessage.message, Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * 扩展函数：Result错误处理
 */
fun <T> Result<T>.onError(
    onError: (Throwable) -> Unit = { throwable ->
        GlobalErrorHandler.showErrorAsync(
            throwable.message ?: "操作失败"
        )
    }
): Result<T> {
    if (isFailure) {
        exceptionOrNull()?.let(onError)
    }
    return this
}

/**
 * 扩展函数：带重试的错误处理
 */
fun <T> Result<T>.onErrorWithRetry(
    retryLabel: String = "重试",
    retry: suspend () -> Unit,
    onError: (Throwable) -> Unit = { throwable ->
        GlobalErrorHandler.showErrorAsync(
            message = throwable.message ?: "操作失败",
            action = ErrorAction(retryLabel, retry)
        )
    }
): Result<T> {
    if (isFailure) {
        exceptionOrNull()?.let(onError)
    }
    return this
}
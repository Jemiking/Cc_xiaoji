package com.ccxiaoji.feature.schedule.presentation.settings.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.components.FlatAlertDialog

/**
 * 错误消息对话框 - 扁平化设计
 *
 * @deprecated 请使用 [com.ccxiaoji.feature.schedule.presentation.screen.ErrorScreen] 替代。
 * 新的全屏页面提供更好的用户体验和一致的 UI Kit 风格。
 */
@Deprecated(
    message = "Use ErrorScreen instead",
    replaceWith = ReplaceWith(
        "ErrorScreen(errorMessage, navController)",
        "com.ccxiaoji.feature.schedule.presentation.screen.ErrorScreen"
    )
)
@Composable
fun ErrorDialog(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    errorMessage?.let { message ->
        FlatAlertDialog(
            onDismissRequest = onDismiss,
            onConfirmation = onDismiss,
            dialogTitle = stringResource(R.string.schedule_settings_error_title),
            dialogText = message,
            confirmText = stringResource(R.string.schedule_confirm),
            dismissText = ""
        )
    }
}
package com.ccxiaoji.feature.schedule.presentation.settings.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.components.FlatAlertDialog

/**
 * 错误消息对话框 - 扁平化设计
 */
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
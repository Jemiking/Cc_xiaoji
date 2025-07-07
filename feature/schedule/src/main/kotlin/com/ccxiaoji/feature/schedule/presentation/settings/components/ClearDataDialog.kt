package com.ccxiaoji.feature.schedule.presentation.settings.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.components.FlatAlertDialog

/**
 * 清除数据确认对话框 - 扁平化设计
 */
@Composable
fun ClearDataDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        FlatAlertDialog(
            onDismissRequest = onDismiss,
            onConfirmation = {
                onConfirm()
                onDismiss()
            },
            dialogTitle = stringResource(R.string.schedule_settings_confirm_clear_title),
            dialogText = stringResource(R.string.schedule_settings_confirm_clear_message),
            confirmText = stringResource(R.string.schedule_confirm),
            dismissText = stringResource(R.string.schedule_cancel)
        )
    }
}
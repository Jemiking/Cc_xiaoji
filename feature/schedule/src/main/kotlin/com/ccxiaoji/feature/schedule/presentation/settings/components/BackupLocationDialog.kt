package com.ccxiaoji.feature.schedule.presentation.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 备份位置选择对话框 - 扁平化设计
 */
@Composable
fun BackupLocationDialog(
    showDialog: Boolean,
    onExternalBackup: () -> Unit,
    onInternalBackup: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        FlatDialog(
            onDismissRequest = onDismiss,
            title = stringResource(R.string.schedule_settings_backup_location_dialog_title),
            confirmButton = {
                TextButton(
                    onClick = {
                        onExternalBackup()
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.schedule_settings_backup_external))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onInternalBackup()
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.schedule_settings_backup_internal))
                }
            }
        ) {
            Text(
                text = stringResource(R.string.schedule_settings_backup_location_dialog_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
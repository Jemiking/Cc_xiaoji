package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.ledger.R

/**
 * 丢弃未保存变更的确认对话框
 *
 * 当用户尝试离开有未保存变更的页面时显示，
 * 确保用户不会意外丢失数据。
 */
@Composable
fun DiscardChangesDialog(
    onConfirmDiscard: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.discard_changes_title),
    text: String = stringResource(R.string.discard_changes_message),
    confirmText: String = stringResource(R.string.discard),
    dismissText: String = stringResource(R.string.continue_editing)
) {
    AlertDialog(
        modifier = modifier
            .testTag(EditorTestTags.DISCARD_DIALOG),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDiscard
            ) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
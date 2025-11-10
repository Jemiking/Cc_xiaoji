package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.ui.components.FlatButton

/**
 * 通用删除确认弹窗组件
 *
 * 用于替代全屏删除确认页面，提供一致的删除确认体验。
 * 支持单个和批量删除场景，可自定义标题、消息和附加内容。
 *
 * @param visible 是否显示弹窗
 * @param itemCount 要删除的项目数量，默认为1（单个删除）
 * @param title 弹窗标题，为null时使用默认标题
 * @param message 确认消息，为null时根据itemCount自动生成
 * @param confirmText 确认按钮文本，为null时使用默认文本
 * @param cancelText 取消按钮文本，为null时使用默认文本
 * @param isDeleting 是否正在删除中，用于显示进度和禁用按钮
 * @param onConfirm 确认删除的回调
 * @param onDismiss 取消/关闭弹窗的回调
 * @param content 可选的附加内容，如交易详情预览、额外警告等
 */
@Composable
fun DeleteConfirmDialog(
    visible: Boolean,
    itemCount: Int = 1,
    title: String? = null,
    message: String? = null,
    confirmText: String? = null,
    cancelText: String? = null,
    isDeleting: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    if (!visible) return

    // 解析按钮文本
    val confirmButtonText = confirmText ?: stringResource(R.string.delete)
    val cancelButtonText = cancelText ?: stringResource(R.string.cancel)

    // 根据itemCount和传入参数确定标题和消息
    val dialogTitle = title ?: if (itemCount > 1) {
        stringResource(R.string.batch_delete_confirm_title)
    } else {
        stringResource(R.string.confirm_delete)
    }

    val dialogMessage = message ?: if (itemCount > 1) {
        stringResource(R.string.batch_delete_confirm_message, itemCount)
    } else {
        // 单个删除的通用消息
        stringResource(R.string.delete_confirm_message)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = dialogTitle,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = dialogMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )

                // 附加内容（如交易预览、额外警告等）
                if (content != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }

                // 删除进度指示器
                if (isDeleting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            FlatButton(
                text = confirmButtonText,
                onClick = onConfirm,
                enabled = !isDeleting,
                backgroundColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(
                    text = cancelButtonText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
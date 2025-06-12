package com.ccxiaoji.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ccxiaoji.core.ui.theme.CcXiaoJiTheme

/**
 * 错误对话框组件
 * @param title 标题，默认为"错误"
 * @param message 错误信息
 * @param onDismiss 关闭对话框的回调
 * @param confirmButtonText 确认按钮文本，默认为"确定"
 * @param onConfirm 确认按钮点击回调，默认调用onDismiss
 * @param dismissButtonText 取消按钮文本，如果为null则不显示取消按钮
 * @param modifier 修饰符
 */
@Composable
fun ErrorDialog(
    title: String = "错误",
    message: String,
    onDismiss: () -> Unit,
    confirmButtonText: String = "确定",
    onConfirm: () -> Unit = onDismiss,
    dismissButtonText: String? = null,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "错误图标"
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = if (dismissButtonText != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
            }
        } else null,
        modifier = modifier
    )
}

@Preview
@Composable
private fun ErrorDialogPreview() {
    CcXiaoJiTheme {
        ErrorDialog(
            message = "这是一个错误信息示例",
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun ErrorDialogWithDismissButtonPreview() {
    CcXiaoJiTheme {
        ErrorDialog(
            title = "操作失败",
            message = "无法完成操作，请稍后重试",
            onDismiss = {},
            confirmButtonText = "重试",
            dismissButtonText = "取消"
        )
    }
}
package com.ccxiaoji.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.ccxiaoji.core.ui.theme.CcXiaoJiTheme

/**
 * 确认对话框组件
 * @param title 标题
 * @param message 确认信息
 * @param onConfirm 确认回调
 * @param onDismiss 取消回调
 * @param confirmText 确认按钮文本，默认为"确定"
 * @param dismissText 取消按钮文本，默认为"取消"
 * @param icon 图标，如果为null则不显示图标
 * @param modifier 修饰符
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "确定",
    dismissText: String = "取消",
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            }
        } else null,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    CcXiaoJiTheme {
        ConfirmDialog(
            title = "确认删除",
            message = "确定要删除这条记录吗？此操作不可撤销。",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun ConfirmDialogWithIconPreview() {
    CcXiaoJiTheme {
        ConfirmDialog(
            title = "警告",
            message = "此操作将清空所有数据，是否继续？",
            onConfirm = {},
            onDismiss = {},
            confirmText = "继续",
            dismissText = "取消",
            icon = Icons.Default.Warning
        )
    }
}
package com.ccxiaoji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化对话框组件
 * 遵循极简扁平化设计（方案A）
 * 
 * @param onDismissRequest 关闭对话框的回调
 * @param title 对话框标题
 * @param content 对话框内容
 * @param confirmButton 确认按钮
 * @param dismissButton 取消按钮（可选）
 * @param properties 对话框属性
 * @param containerColor 容器颜色
 * @param modifier 修饰符
 */
@Composable
fun FlatDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.large),
            shape = RoundedCornerShape(DesignTokens.BorderRadius.large),
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.Spacing.large)
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 内容
                Column {
                    content()
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton?.invoke()
                    
                    if (dismissButton != null) {
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    }
                    
                    confirmButton()
                }
            }
        }
    }
}

/**
 * 扁平化警告对话框
 * 简化版本，用于显示简单的警告信息
 */
@Composable
fun FlatAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    confirmText: String = "确定",
    dismissText: String = "取消"
) {
    FlatDialog(
        onDismissRequest = onDismissRequest,
        title = dialogTitle,
        confirmButton = {
            FlatButton(
                text = confirmText,
                onClick = onConfirmation,
                backgroundColor = MaterialTheme.colorScheme.primary
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Text(
            text = dialogText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
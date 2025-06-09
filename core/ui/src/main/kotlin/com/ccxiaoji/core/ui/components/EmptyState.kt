package com.ccxiaoji.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.core.ui.theme.CcXiaoJiTheme

/**
 * 空状态组件
 * @param icon 图标，默认为收件箱图标
 * @param title 标题
 * @param message 描述信息
 * @param actionText 操作按钮文本，如果为null则不显示按钮
 * @param onAction 操作按钮点击回调
 * @param modifier 修饰符
 */
@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Inbox,
    title: String,
    message: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = actionText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    CcXiaoJiTheme {
        EmptyState(
            title = "暂无数据",
            message = "还没有任何记录，点击下方按钮创建第一条"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateWithActionPreview() {
    CcXiaoJiTheme {
        EmptyState(
            title = "暂无交易记录",
            message = "开始记录你的第一笔交易吧",
            actionText = "添加交易",
            onAction = {}
        )
    }
}
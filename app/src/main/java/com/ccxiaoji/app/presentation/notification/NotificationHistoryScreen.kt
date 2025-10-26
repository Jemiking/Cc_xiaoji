package com.ccxiaoji.app.presentation.notification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.core.database.entity.NotificationQueueEntity
import com.ccxiaoji.core.database.entity.NotificationStatus
import com.ccxiaoji.core.database.entity.NotificationType

@Composable
fun NotificationHistoryScreen(
    viewModel: NotificationHistoryViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    NotificationHistoryContent(items)
}

@Composable
private fun NotificationHistoryContent(items: List<NotificationQueueEntity>) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(items) { item ->
                val title = when (item.type) {
                    NotificationType.TASK -> "任务: ${item.title}"
                    NotificationType.HABIT -> "习惯: ${item.title}"
                    NotificationType.BUDGET -> "预算: ${item.title}"
                    NotificationType.CREDIT_CARD -> "信用卡: ${item.title}"
                    NotificationType.SCHEDULE -> "排班: ${item.title}"
                    NotificationType.GENERAL -> item.title
                }
                val status = when (item.status) {
                    NotificationStatus.PENDING -> "待发送"
                    NotificationStatus.PROCESSING -> "发送中"
                    NotificationStatus.SENT -> "已发送"
                    NotificationStatus.FAILED -> "失败"
                    NotificationStatus.CANCELLED -> "已取消"
                }
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = "状态: $status  计划时间: ${item.scheduledAt}", style = MaterialTheme.typography.bodySmall)
                if (item.message.isNotBlank()) {
                    Text(text = item.message, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}


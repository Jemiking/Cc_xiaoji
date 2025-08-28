package com.ccxiaoji.feature.ledger.presentation.screen.ledgerbook.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccxiaoji.feature.ledger.domain.model.SyncMode
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 同步模式选择对话框
 * 提供不同联动同步模式的选择界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncModeSelectionDialog(
    currentSyncMode: SyncMode,
    onDismiss: () -> Unit,
    onConfirm: (SyncMode) -> Unit
) {
    var selectedSyncMode by remember { mutableStateOf(currentSyncMode) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.Spacing.medium)
            ) {
                // 标题
                Text(
                    text = "选择同步模式",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 同步模式列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    items(SyncMode.values()) { syncMode ->
                        SyncModeItem(
                            syncMode = syncMode,
                            isSelected = selectedSyncMode == syncMode,
                            onSelect = { selectedSyncMode = syncMode }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 按钮栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    
                    Button(
                        onClick = { onConfirm(selectedSyncMode) }
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

/**
 * 同步模式选择项
 */
@Composable
private fun SyncModeItem(
    syncMode: SyncMode,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        },
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 同步模式图标
            Icon(
                imageVector = getSyncModeIcon(syncMode),
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
            
            // 同步模式信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = syncMode.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = syncMode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
            
            // 选中状态指示器
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 获取同步模式对应的图标
 */
private fun getSyncModeIcon(syncMode: SyncMode): androidx.compose.ui.graphics.vector.ImageVector {
    return when (syncMode) {
        SyncMode.BIDIRECTIONAL -> Icons.Default.SyncAlt
        SyncMode.PARENT_TO_CHILD -> Icons.Default.CallMade
        SyncMode.CHILD_TO_PARENT -> Icons.Default.CallReceived
    }
}
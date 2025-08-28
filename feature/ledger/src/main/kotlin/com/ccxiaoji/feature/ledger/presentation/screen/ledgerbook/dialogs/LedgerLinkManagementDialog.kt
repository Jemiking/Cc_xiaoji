package com.ccxiaoji.feature.ledger.presentation.screen.ledgerbook.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.LedgerLink
import com.ccxiaoji.feature.ledger.domain.model.SyncMode
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 记账簿联动关系管理对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerLinkManagementDialog(
    currentLedger: Ledger,
    availableLedgers: List<Ledger>,
    existingLinks: List<LedgerLink>,
    onDismiss: () -> Unit,
    onCreateLink: (String, SyncMode) -> Unit,
    onDeleteLink: (String) -> Unit,
    onUpdateSyncMode: (String, SyncMode) -> Unit,
    onToggleAutoSync: (String, Boolean) -> Unit
) {
    var showAddLinkDialog by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.Spacing.medium)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "「${currentLedger.name}」联动设置",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 添加联动按钮
                OutlinedButton(
                    onClick = { showAddLinkDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    Text("添加联动关系")
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 现有联动关系列表
                if (existingLinks.isEmpty()) {
                    // 空状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                            Text(
                                text = "暂无联动关系",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                            Text(
                                text = "点击上方按钮添加联动关系",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    // 联动关系列表
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        items(existingLinks) { link ->
                            LedgerLinkItem(
                                link = link,
                                currentLedger = currentLedger,
                                availableLedgers = availableLedgers,
                                onDeleteLink = { onDeleteLink(link.id) },
                                onUpdateSyncMode = { syncMode -> onUpdateSyncMode(link.id, syncMode) },
                                onToggleAutoSync = { enabled -> onToggleAutoSync(link.id, enabled) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 添加联动关系对话框
    if (showAddLinkDialog) {
        AddLedgerLinkDialog(
            currentLedger = currentLedger,
            availableLedgers = availableLedgers.filter { ledger ->
                ledger.id != currentLedger.id && 
                !existingLinks.any { link -> 
                    link.parentLedgerId == ledger.id || link.childLedgerId == ledger.id 
                }
            },
            onDismiss = { showAddLinkDialog = false },
            onConfirm = { targetLedgerId, syncMode ->
                onCreateLink(targetLedgerId, syncMode)
                showAddLinkDialog = false
            }
        )
    }
}

/**
 * 联动关系列表项
 */
@Composable
private fun LedgerLinkItem(
    link: LedgerLink,
    currentLedger: Ledger,
    availableLedgers: List<Ledger>,
    onDeleteLink: () -> Unit,
    onUpdateSyncMode: (SyncMode) -> Unit,
    onToggleAutoSync: (Boolean) -> Unit
) {
    val linkedLedger = availableLedgers.find { ledger ->
        ledger.id == link.getOtherLedgerId(currentLedger.id)
    }
    
    var showSyncModeDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 联动记账簿信息
                if (linkedLedger != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(linkedLedger.color))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = linkedLedger.name.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = linkedLedger.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = getLinkTypeDescription(link, currentLedger.id),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        text = "未知记账簿",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 操作按钮
                Row {
                    // 同步模式按钮
                    OutlinedButton(
                        onClick = { showSyncModeDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = link.syncMode.displayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    
                    // 删除按钮
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除联动",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 自动同步开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (link.autoSyncEnabled) Icons.Default.Sync else Icons.Default.SyncDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (link.autoSyncEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                
                Text(
                    text = "自动同步",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = link.autoSyncEnabled,
                    onCheckedChange = onToggleAutoSync
                )
            }
        }
    }
    
    // 同步模式选择对话框
    if (showSyncModeDialog) {
        SyncModeSelectionDialog(
            currentSyncMode = link.syncMode,
            onDismiss = { showSyncModeDialog = false },
            onConfirm = { syncMode ->
                onUpdateSyncMode(syncMode)
                showSyncModeDialog = false
            }
        )
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { 
                Text("确定要删除这个联动关系吗？删除后两个记账簿之间将不再同步交易。") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLink()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = "删除",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 获取联动类型描述
 */
private fun getLinkTypeDescription(link: LedgerLink, currentLedgerId: String): String {
    return when {
        link.isParentLedger(currentLedgerId) -> "父记账簿 → 子记账簿"
        link.isChildLedger(currentLedgerId) -> "子记账簿 ← 父记账簿"
        else -> "联动关系"
    }
}
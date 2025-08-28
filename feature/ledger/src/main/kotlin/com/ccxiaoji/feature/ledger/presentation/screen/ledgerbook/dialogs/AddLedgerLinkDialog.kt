package com.ccxiaoji.feature.ledger.presentation.screen.ledgerbook.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.SyncMode
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 添加联动关系对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLedgerLinkDialog(
    currentLedger: Ledger,
    availableLedgers: List<Ledger>,
    onDismiss: () -> Unit,
    onConfirm: (String, SyncMode) -> Unit
) {
    var selectedLedgerId by remember { mutableStateOf<String?>(null) }
    var selectedSyncMode by remember { mutableStateOf(SyncMode.BIDIRECTIONAL) }
    var showSyncModeDialog by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.Spacing.medium)
            ) {
                // 标题
                Text(
                    text = "添加联动关系",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 当前记账簿信息
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    try {
                                        Color(android.graphics.Color.parseColor(currentLedger.color))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentLedger.name.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
                        
                        Column {
                            Text(
                                text = currentLedger.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "当前记账簿",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 选择目标记账簿
                Text(
                    text = "选择要联动的记账簿",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                if (availableLedgers.isEmpty()) {
                    // 无可选记账簿
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.large),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                            Text(
                                text = "没有可联动的记账簿",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    // 记账簿列表
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        items(availableLedgers) { ledger ->
                            LedgerSelectionItem(
                                ledger = ledger,
                                isSelected = selectedLedgerId == ledger.id,
                                onSelect = { selectedLedgerId = ledger.id }
                            )
                        }
                    }
                }
                
                if (availableLedgers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                    
                    // 同步模式选择
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSyncModeDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null
                            )
                            
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "同步模式",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = selectedSyncMode.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                            onClick = {
                                selectedLedgerId?.let { ledgerId ->
                                    onConfirm(ledgerId, selectedSyncMode)
                                }
                            },
                            enabled = selectedLedgerId != null
                        ) {
                            Text("创建联动")
                        }
                    }
                }
            }
        }
    }
    
    // 同步模式选择对话框
    if (showSyncModeDialog) {
        SyncModeSelectionDialog(
            currentSyncMode = selectedSyncMode,
            onDismiss = { showSyncModeDialog = false },
            onConfirm = { syncMode ->
                selectedSyncMode = syncMode
                showSyncModeDialog = false
            }
        )
    }
}

/**
 * 记账簿选择项
 */
@Composable
private fun LedgerSelectionItem(
    ledger: Ledger,
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(ledger.color))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ledger.name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ledger.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (!ledger.description.isNullOrBlank()) {
                    Text(
                        text = ledger.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }
            
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
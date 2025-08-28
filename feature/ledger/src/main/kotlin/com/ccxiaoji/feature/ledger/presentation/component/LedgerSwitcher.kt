package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ccxiaoji.feature.ledger.domain.model.Ledger

/**
 * 记账簿切换器组件
 * 
 * 用于在侧边栏显示当前记账簿，并提供切换功能
 */
@Composable
fun LedgerSwitcher(
    currentLedger: Ledger?,
    ledgers: List<Ledger>,
    isLoading: Boolean = false,
    onLedgerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLedgerSelector by remember { mutableStateOf(false) }
    
    // 主要的记账簿切换按钮
    LedgerSwitcherButton(
        currentLedger = currentLedger,
        isLoading = isLoading,
        onClick = { showLedgerSelector = true },
        modifier = modifier
    )
    
    // 记账簿选择弹窗
    if (showLedgerSelector) {
        LedgerSelectorDialog(
            currentLedger = currentLedger,
            ledgers = ledgers,
            onLedgerSelected = { ledgerId ->
                onLedgerSelected(ledgerId)
                showLedgerSelector = false
            },
            onDismiss = { showLedgerSelector = false }
        )
    }
}

/**
 * 记账簿切换按钮
 */
@Composable
private fun LedgerSwitcherButton(
    currentLedger: Ledger?,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColor = remember(currentLedger?.color) {
        try {
            Color(android.graphics.Color.parseColor(currentLedger?.color ?: "#3A7AFE"))
        } catch (e: Exception) {
            Color(0xFF3A7AFE)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 记账簿图标
            LedgerIcon(
                icon = currentLedger?.icon ?: "book",
                color = ledgerColor,
                modifier = Modifier.size(32.dp)
            )
            
            // 记账簿信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                } else {
                    Text(
                        text = currentLedger?.name ?: "未选择记账簿",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (currentLedger?.description != null) {
                        Text(
                            text = currentLedger.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // 展开图标
            val rotationAngle by animateFloatAsState(
                targetValue = if (isLoading) 360f else 0f,
                label = "rotation"
            )
            
            Icon(
                imageVector = if (isLoading) Icons.Default.Refresh else Icons.Default.KeyboardArrowDown,
                contentDescription = "切换记账簿",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotationAngle)
            )
        }
    }
}

/**
 * 记账簿选择弹窗
 */
@Composable
private fun LedgerSelectorDialog(
    currentLedger: Ledger?,
    ledgers: List<Ledger>,
    onLedgerSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择记账簿",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 记账簿列表
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(ledgers) { ledger ->
                        LedgerItem(
                            ledger = ledger,
                            isSelected = ledger.id == currentLedger?.id,
                            onClick = { onLedgerSelected(ledger.id) }
                        )
                    }
                }
                
                if (ledgers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "暂无记账簿",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 记账簿列表项
 */
@Composable
private fun LedgerItem(
    ledger: Ledger,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColor = remember(ledger.color) {
        try {
            Color(android.graphics.Color.parseColor(ledger.color))
        } catch (e: Exception) {
            Color(0xFF3A7AFE)
        }
    }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "backgroundColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        label = "borderColor"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 记账簿图标
            LedgerIcon(
                icon = ledger.icon,
                color = ledgerColor,
                modifier = Modifier.size(28.dp)
            )
            
            // 记账簿信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = ledger.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (ledger.isDefault) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "默认",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                
                if (ledger.description != null) {
                    Text(
                        text = ledger.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 选中标记
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已选中",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 记账簿图标组件
 */
@Composable
private fun LedgerIcon(
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val iconVector = remember(icon) {
        when (icon) {
            "home" -> Icons.Default.Home
            "baby" -> Icons.Default.ChildCare
            "school" -> Icons.Default.School
            "work" -> Icons.Default.Work
            "travel" -> Icons.Default.Flight
            "car" -> Icons.Default.DirectionsCar
            "health" -> Icons.Default.LocalHospital
            "shopping" -> Icons.Default.ShoppingCart
            "food" -> Icons.Default.Restaurant
            else -> Icons.Default.MenuBook // 默认图标
        }
    }
    
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * 简化版记账簿切换器
 * 用于较小的空间
 */
@Composable
fun CompactLedgerSwitcher(
    currentLedger: Ledger?,
    ledgers: List<Ledger>,
    onLedgerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSelector by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .clickable { showSelector = true }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 简化图标
        currentLedger?.let { ledger ->
            val ledgerColor = remember(ledger.color) {
                try {
                    Color(android.graphics.Color.parseColor(ledger.color))
                } catch (e: Exception) {
                    Color(0xFF3A7AFE)
                }
            }
            
            LedgerIcon(
                icon = ledger.icon,
                color = ledgerColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // 记账簿名称
        Text(
            text = currentLedger?.name ?: "记账簿",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        
        // 下拉图标
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "切换",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    // 选择弹窗
    if (showSelector) {
        LedgerSelectorDialog(
            currentLedger = currentLedger,
            ledgers = ledgers,
            onLedgerSelected = { ledgerId ->
                onLedgerSelected(ledgerId)
                showSelector = false
            },
            onDismiss = { showSelector = false }
        )
    }
}
package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 记账簿选择器组件
 */
@Composable
fun LedgerSelector(
    selectedLedger: Ledger?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 记账簿图标
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        selectedLedger?.let { 
                            try {
                                Color(android.graphics.Color.parseColor(it.color))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                        } ?: MaterialTheme.colorScheme.primary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getLedgerIcon(selectedLedger?.icon ?: "book"),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // 记账簿信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = selectedLedger?.name ?: "选择记账簿",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (selectedLedger?.description?.isNotBlank() == true) {
                    Text(
                        text = selectedLedger.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 下拉箭头
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 记账簿选择对话框
 */
@Composable
fun LedgerSelectorDialog(
    isVisible: Boolean,
    ledgers: List<Ledger>,
    selectedLedgerId: String?,
    onLedgerSelected: (Ledger) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 标题栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "选择记账簿",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Divider()
                    
                    // 记账簿列表
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(ledgers) { ledger ->
                            LedgerItem(
                                ledger = ledger,
                                isSelected = ledger.id == selectedLedgerId,
                                onSelect = { onLedgerSelected(ledger) }
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
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 记账簿图标
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
            Icon(
                imageVector = getLedgerIcon(ledger.icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // 记账簿信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = ledger.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // 默认标识
                if (ledger.isDefault) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "默认",
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignTokens.BrandColors.Ledger,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            if (ledger.description?.isNotBlank() == true) {
                Text(
                    text = ledger.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // 选中指示器
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = DesignTokens.BrandColors.Ledger,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 根据图标键获取对应的Material图标
 */
private fun getLedgerIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "book" -> Icons.Default.MenuBook
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        "school" -> Icons.Default.School
        "family" -> Icons.Default.FamilyRestroom
        "car" -> Icons.Default.DirectionsCar
        "health" -> Icons.Default.LocalHospital
        "travel" -> Icons.Default.Flight
        "shopping" -> Icons.Default.ShoppingCart
        "food" -> Icons.Default.Restaurant
        "entertainment" -> Icons.Default.Movie
        "investment" -> Icons.Default.TrendingUp
        else -> Icons.Default.MenuBook
    }
}
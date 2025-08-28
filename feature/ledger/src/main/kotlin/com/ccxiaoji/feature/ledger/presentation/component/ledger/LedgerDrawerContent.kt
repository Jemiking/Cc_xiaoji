package com.ccxiaoji.feature.ledger.presentation.component.ledger

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.presentation.component.LedgerSwitcher

@Composable
fun LedgerDrawerContent(
    currentLedger: Ledger?,
    allLedgers: List<Ledger>,
    onLedgerSelected: (Ledger) -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToUnifiedAccountAsset: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToRecurringTransaction: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToSavingsGoal: () -> Unit,
    onNavigateToLedgerSettings: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    // 账户管理现在是直接菜单项，不需要展开状态
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 头部区域
        DrawerHeader(
            currentLedger = currentLedger,
            allLedgers = allLedgers,
            onLedgerSelected = { ledgerId ->
                // 找到对应的记账簿对象
                val selectedLedger = allLedgers.find { it.id == ledgerId }
                selectedLedger?.let { onLedgerSelected(it) }
                onCloseDrawer() // 选择记账簿后自动关闭抽屉
            }
        )
        
        HorizontalDivider()
        
        // 数据查看
        DrawerSectionTitle(title = "数据查看")
        DrawerMenuItem(
            icon = Icons.Default.BarChart,
            text = "统计分析",
            onClick = {
                onNavigateToStatistics()
                onCloseDrawer()
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // 账户资产 (统一页面)
        DrawerSectionTitle(title = "账户资产")
        DrawerMenuItem(
            icon = Icons.Default.AccountBalance,
            text = "账户与资产",
            onClick = {
                onNavigateToUnifiedAccountAsset()
                onCloseDrawer()
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // 记账设置
        DrawerSectionTitle(title = "记账设置")
        DrawerMenuItem(
            icon = Icons.Default.Folder,
            text = "分类管理",
            onClick = {
                onNavigateToCategoryManagement()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            icon = Icons.Default.Refresh,
            text = "定期交易",
            onClick = {
                onNavigateToRecurringTransaction()
                onCloseDrawer()
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // 财务规划
        DrawerSectionTitle(title = "财务规划")
        DrawerMenuItem(
            icon = Icons.Default.TrendingUp,
            text = "预算管理",
            onClick = {
                onNavigateToBudget()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            icon = Icons.Default.TrackChanges,
            text = "储蓄目标",
            onClick = {
                onNavigateToSavingsGoal()
                onCloseDrawer()
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // 系统设置 (重命名)
        DrawerSectionTitle(title = "系统设置")
        DrawerMenuItem(
            icon = Icons.Default.Settings,
            text = "记账设置",
            onClick = {
                onNavigateToLedgerSettings()
                onCloseDrawer()
            }
        )
    }
}

@Composable
private fun DrawerHeader(
    currentLedger: Ledger?,
    allLedgers: List<Ledger>,
    onLedgerSelected: (String) -> Unit
) {
    
    // 如果没有记账簿，显示提示信息
    if (allLedgers.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "暂无记账簿",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "当前记账簿",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 默认标识
            if (currentLedger?.isDefault == true) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "默认",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        // 记账簿切换器（新组件）
        LedgerSwitcher(
            currentLedger = currentLedger,
            ledgers = allLedgers,
            isLoading = false,
            onLedgerSelected = onLedgerSelected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DrawerSectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFF4CAF50), // 记账模块主题色
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DrawerExpandableMenuItem(
    icon: ImageVector,
    text: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onParentClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onParentClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFF4CAF50), // 记账模块主题色
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            // 展开/折叠指示器
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onToggleExpanded() }
            )
        }
        
        // 子菜单内容
        if (isExpanded) {
            content()
        }
    }
}

@Composable
private fun DrawerSubMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 52.dp, end = 16.dp, top = 8.dp, bottom = 8.dp), // 增加左边距以显示层次
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, // 子项使用稍浅的颜色
            modifier = Modifier.size(20.dp) // 子项图标稍小
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium, // 子项文本稍小
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
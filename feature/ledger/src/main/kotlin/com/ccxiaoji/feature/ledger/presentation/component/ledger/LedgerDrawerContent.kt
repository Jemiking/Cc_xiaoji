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
import com.ccxiaoji.feature.ledger.presentation.component.LedgerSelector
import com.ccxiaoji.feature.ledger.presentation.component.LedgerSelectorDialog

@Composable
fun LedgerDrawerContent(
    currentLedger: Ledger?,
    allLedgers: List<Ledger>,
    onLedgerSelected: (Ledger) -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToAssetOverview: () -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToRecurringTransaction: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToSavingsGoal: () -> Unit,
    onNavigateToCreditCard: () -> Unit,
    onNavigateToLedgerSettings: () -> Unit,
    onCloseDrawer: () -> Unit
) {
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
            onLedgerSelected = { ledger ->
                onLedgerSelected(ledger)
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
        DrawerMenuItem(
            icon = Icons.Default.AccountBalance,
            text = "资产总览",
            onClick = {
                onNavigateToAssetOverview()
                onCloseDrawer()
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // 账务管理
        DrawerSectionTitle(title = "账务管理")
        DrawerMenuItem(
            icon = Icons.Default.AccountBalanceWallet,
            text = "账户管理",
            onClick = {
                onNavigateToAccountManagement()
                onCloseDrawer()
            }
        )
        DrawerMenuItem(
            icon = Icons.Default.CreditCard,
            text = "信用卡管理",
            onClick = {
                onNavigateToCreditCard()
                onCloseDrawer()
            }
        )
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
        
        // 设置
        DrawerSectionTitle(title = "设置")
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
    onLedgerSelected: (Ledger) -> Unit
) {
    var showLedgerSelector by remember { mutableStateOf(false) }
    
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
        
        // 记账簿选择器
        LedgerSelector(
            selectedLedger = currentLedger,
            onClick = { 
                if (allLedgers.size > 1) {
                    showLedgerSelector = true 
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
    
    // 记账簿选择对话框
    if (allLedgers.size > 1) {
        LedgerSelectorDialog(
            isVisible = showLedgerSelector,
            ledgers = allLedgers,
            selectedLedgerId = currentLedger?.id,
            onLedgerSelected = { ledger ->
                onLedgerSelected(ledger)
                showLedgerSelector = false
            },
            onDismiss = { showLedgerSelector = false }
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
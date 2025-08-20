package com.ccxiaoji.feature.ledger.presentation.component.ledger

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LedgerDrawerContent(
    currentAccountName: String,
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
            currentAccountName = currentAccountName
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
    currentAccountName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 用户头像占位
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "用户头像",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column {
            Text(
                text = "当前账户",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currentAccountName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
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
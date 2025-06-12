package com.ccxiaoji.feature.ledger.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.api.LedgerNavigator

@Composable
fun LedgerDrawerContent(
    navigator: LedgerNavigator,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题
            Text(
                text = "记账功能",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // 功能菜单项
            DrawerMenuItem(
                icon = Icons.Default.BarChart,
                title = "统计分析",
                onClick = {
                    navigator.navigateToStatistics()
                    onCloseDrawer()
                }
            )
            
            DrawerMenuItem(
                icon = Icons.Default.AccountBalance,
                title = "账户管理",
                onClick = {
                    navigator.navigateToAccounts()
                    onCloseDrawer()
                }
            )
            
            DrawerMenuItem(
                icon = Icons.Default.Category,
                title = "分类管理",
                onClick = {
                    navigator.navigateToCategories()
                    onCloseDrawer()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            DrawerMenuItem(
                icon = Icons.Default.CreditCard,
                title = "信用卡管理",
                onClick = {
                    navigator.navigateToCreditCards()
                    onCloseDrawer()
                }
            )
            
            DrawerMenuItem(
                icon = Icons.Default.AttachMoney,
                title = "预算管理",
                onClick = {
                    navigator.navigateToBudget()
                    onCloseDrawer()
                }
            )
            
            DrawerMenuItem(
                icon = Icons.Default.Refresh,
                title = "定期交易",
                onClick = {
                    navigator.navigateToRecurringTransactions()
                    onCloseDrawer()
                }
            )
            
            DrawerMenuItem(
                icon = Icons.Default.Savings,
                title = "存钱目标",
                onClick = {
                    navigator.navigateToSavingsGoals()
                    onCloseDrawer()
                }
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(title) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
package com.ccxiaoji.feature.ledger.presentation.screen.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.AdvancedSettings
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 高级设置部分
 */
@Composable
fun AdvancedSettingsSection(
    advancedSettings: AdvancedSettings,
    onUpdateAdvancedSettings: (AdvancedSettings) -> Unit,
    onNavigateToLedgerBookManagement: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    onNavigateToBudgetManagement: () -> Unit,
    onNavigateToDesignDemo: () -> Unit = {}
) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.medium, vertical = DesignTokens.Spacing.small),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.medium)) {
            // 记账簿管理
            SettingItem(
                icon = Icons.Default.MenuBook,
                title = "记账簿管理",
                subtitle = "管理多个记账簿",
                onClick = onNavigateToLedgerBookManagement
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 分类管理
            SettingItem(
                icon = Icons.Default.Category,
                title = "分类管理",
                subtitle = "管理收支分类",
                onClick = onNavigateToCategoryManagement
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 账户管理
            SettingItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = "账户管理",
                subtitle = "管理账户信息",
                onClick = onNavigateToAccountManagement
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 预算管理
            SettingItem(
                icon = Icons.Default.Savings,
                title = "预算管理",
                subtitle = "设置和管理预算",
                onClick = onNavigateToBudgetManagement
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 设计方案Demo
            SettingItem(
                icon = Icons.Default.Palette,
                title = "界面设计Demo",
                subtitle = "预览不同的界面设计方案",
                onClick = onNavigateToDesignDemo
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 小数位数设置
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Pin,
                        contentDescription = null,
                        modifier = Modifier.padding(end = DesignTokens.Spacing.medium),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "小数位数",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${advancedSettings.decimalPlaces} 位",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (advancedSettings.decimalPlaces > 0) {
                                onUpdateAdvancedSettings(
                                    advancedSettings.copy(decimalPlaces = advancedSettings.decimalPlaces - 1)
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "减少",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = advancedSettings.decimalPlaces.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(
                        onClick = {
                            if (advancedSettings.decimalPlaces < 4) {
                                onUpdateAdvancedSettings(
                                    advancedSettings.copy(decimalPlaces = advancedSettings.decimalPlaces + 1)
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "增加",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 显示图标开关
            SwitchSettingItem(
                icon = Icons.Default.Image,
                title = "显示分类图标",
                checked = advancedSettings.enableCategoryIcons,
                onCheckedChange = { checked ->
                    onUpdateAdvancedSettings(advancedSettings.copy(enableCategoryIcons = checked))
                }
            )
            
            SwitchSettingItem(
                icon = Icons.Default.AccountCircle,
                title = "显示账户图标",
                checked = advancedSettings.enableAccountIcons,
                onCheckedChange = { checked ->
                    onUpdateAdvancedSettings(advancedSettings.copy(enableAccountIcons = checked))
                }
            )
        }
    }
}
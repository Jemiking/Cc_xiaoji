package com.ccxiaoji.feature.ledger.presentation.screen.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 基础设置部分
 */
@Composable
fun BasicSettingsSection(
    basicSettings: BasicSettings,
    accounts: List<Account>,
    onUpdateBasicSettings: (BasicSettings) -> Unit,
    onNavigateToCurrencySelection: () -> Unit = {},
    onNavigateToAccountSelection: () -> Unit = {},
    onNavigateToReminderSettings: () -> Unit = {},
    onNavigateToHomeDisplaySettings: () -> Unit = {},
    onNavigateToUIStyleSettings: () -> Unit = {}
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
            // 默认账户选择
            val selectedAccount = accounts.find { it.id == basicSettings.defaultAccountId?.toString() }
            
            SettingItem(
                icon = Icons.Default.AccountBalance,
                title = "默认账户",
                subtitle = selectedAccount?.name ?: "未选择",
                onClick = onNavigateToAccountSelection
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 默认币种
            SettingItem(
                icon = Icons.Default.AttachMoney,
                title = "默认币种",
                subtitle = basicSettings.defaultCurrency,
                onClick = onNavigateToCurrencySelection
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 首页显示设置
            SettingItem(
                icon = Icons.Default.Dashboard,
                title = "首页显示设置",
                subtitle = "自定义首页显示内容",
                onClick = onNavigateToHomeDisplaySettings
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 界面风格设置
            SettingItem(
                icon = Icons.Default.Palette,
                title = "界面风格",
                subtitle = "选择记账页面的显示风格",
                onClick = onNavigateToUIStyleSettings
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 时间记录开关
            SwitchSettingItem(
                icon = Icons.Default.AccessTime,
                title = "记录交易时间",
                subtitle = "开启后可以记录交易的具体时分",
                checked = basicSettings.enableTimeRecording,
                onCheckedChange = { enabled ->
                    onUpdateBasicSettings(basicSettings.copy(enableTimeRecording = enabled))
                }
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 记账提醒
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "记账提醒",
                subtitle = if (basicSettings.reminderSettings.enableDailyReminder) 
                    "每日 ${basicSettings.reminderSettings.dailyReminderTime}" else "已关闭",
                onClick = onNavigateToReminderSettings
            )
        }
    }
}
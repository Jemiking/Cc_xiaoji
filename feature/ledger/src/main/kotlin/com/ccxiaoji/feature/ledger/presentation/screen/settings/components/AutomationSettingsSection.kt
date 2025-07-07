package com.ccxiaoji.feature.ledger.presentation.screen.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.AutomationSettings
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 自动化设置部分
 */
@Composable
fun AutomationSettingsSection(
    automationSettings: AutomationSettings,
    onUpdateAutomationSettings: (AutomationSettings) -> Unit,
    onNavigateToRecurringTransactions: () -> Unit
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
            // 定期交易管理
            SettingItem(
                icon = Icons.Default.Repeat,
                title = "定期交易管理",
                subtitle = "管理自动记账规则",
                onClick = onNavigateToRecurringTransactions
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 智能分类开关
            SwitchSettingItem(
                icon = Icons.Default.AutoAwesome,
                title = "智能分类",
                subtitle = "根据描述自动选择分类",
                checked = automationSettings.enableSmartCategorization,
                onCheckedChange = { checked ->
                    onUpdateAutomationSettings(automationSettings.copy(enableSmartCategorization = checked))
                }
            )
            
            // 智能记账建议开关
            SwitchSettingItem(
                icon = Icons.Default.Lightbulb,
                title = "智能记账建议",
                subtitle = "基于历史记录提供建议",
                checked = automationSettings.enableSmartSuggestions,
                onCheckedChange = { checked ->
                    onUpdateAutomationSettings(automationSettings.copy(enableSmartSuggestions = checked))
                }
            )
            
            // 自动创建定期交易开关
            SwitchSettingItem(
                icon = Icons.Default.Schedule,
                title = "自动创建定期交易",
                subtitle = "到期自动创建定期交易",
                checked = automationSettings.enableAutoRecurring,
                onCheckedChange = { checked ->
                    onUpdateAutomationSettings(automationSettings.copy(enableAutoRecurring = checked))
                }
            )
        }
    }
}
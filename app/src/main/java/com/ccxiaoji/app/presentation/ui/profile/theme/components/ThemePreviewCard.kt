package com.ccxiaoji.app.presentation.ui.profile.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.components.FlatChip
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 主题预览卡片 - 扁平化设计
 */
@Composable
fun ThemePreviewCard(
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.medium, vertical = DesignTokens.Spacing.small),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = "预览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            Text(
                text = "这是当前主题的预览效果",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                FlatChip(
                    label = "芯片",
                    selected = true,
                    onClick = { }
                )
                
                FlatButton(
                    onClick = { },
                    modifier = Modifier.padding(
                        horizontal = DesignTokens.Spacing.medium,
                        vertical = DesignTokens.Spacing.small
                    )
                ) {
                    Text("按钮")
                }
            }
        }
    }
}
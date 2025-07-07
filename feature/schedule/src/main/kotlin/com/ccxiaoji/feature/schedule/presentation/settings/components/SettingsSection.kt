package com.ccxiaoji.feature.schedule.presentation.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 设置区域 - 扁平化设计
 */
@Composable
fun SettingsSection(
    title: String,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DesignTokens.Spacing.small),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp
            )
        }
        
        SettingsCategoryHeader(title)
        
        content()
    }
}
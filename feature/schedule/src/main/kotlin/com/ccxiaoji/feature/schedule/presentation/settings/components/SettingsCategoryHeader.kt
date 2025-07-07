package com.ccxiaoji.feature.schedule.presentation.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 设置分类标题 - 扁平化设计
 */
@Composable
fun SettingsCategoryHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            horizontal = DesignTokens.Spacing.medium,
            vertical = DesignTokens.Spacing.small
        )
    )
}
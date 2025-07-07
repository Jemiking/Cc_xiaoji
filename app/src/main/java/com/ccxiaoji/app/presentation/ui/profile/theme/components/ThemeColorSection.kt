package com.ccxiaoji.app.presentation.ui.profile.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.app.presentation.ui.profile.ThemeColor
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 主题颜色选择区域 - 扁平化设计
 */
@Composable
fun ThemeColorSection(
    selectedColor: ThemeColor,
    onColorSelect: (ThemeColor) -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.medium, vertical = DesignTokens.Spacing.small),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = "主题颜色",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                items(ThemeColor.values()) { color ->
                    ColorOption(
                        color = color,
                        selected = selectedColor == color,
                        onClick = { onColorSelect(color) }
                    )
                }
            }
        }
    }
}
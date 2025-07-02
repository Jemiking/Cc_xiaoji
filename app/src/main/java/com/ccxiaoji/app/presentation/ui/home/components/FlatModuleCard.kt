package com.ccxiaoji.app.presentation.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化模块卡片组件
 * 极简设计，无渐变效果
 */
@Composable
fun FlatModuleCard(
    title: String,
    icon: ImageVector,
    moduleColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = moduleColor.copy(alpha = 0.2f)
    ) {
        // 简洁头部
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = moduleColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        // 内容区域
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}
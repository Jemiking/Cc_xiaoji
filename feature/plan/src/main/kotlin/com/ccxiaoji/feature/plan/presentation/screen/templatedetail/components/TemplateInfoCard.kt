package com.ccxiaoji.feature.plan.presentation.screen.templatedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.components.FlatChip
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 模板基本信息卡片 - 扁平化设计
 */
@Composable
fun TemplateInfoCard(
    template: Template,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 标题和图标
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(DesignTokens.BorderRadius.medium))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(template.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FlatChip(
                        label = getCategoryName(template.category),
                        onClick = { },
                        selected = false,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                }
            }
            
            // 描述
            if (template.description.isNotEmpty()) {
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 标签
            if (template.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    template.tags.forEach { tag ->
                        FlatChip(
                            label = tag,
                            onClick = { },
                            selected = false,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取分类图标
 */
private fun getCategoryIcon(category: TemplateCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        TemplateCategory.WORK -> Icons.Default.AccountCircle
        TemplateCategory.STUDY -> Icons.Default.Edit
        TemplateCategory.FITNESS -> Icons.Default.PlayArrow
        TemplateCategory.LIFE -> Icons.Default.Home
        TemplateCategory.HEALTH -> Icons.Default.Favorite
        TemplateCategory.SKILL -> Icons.Default.Build
        TemplateCategory.PROJECT -> Icons.Default.Share
        TemplateCategory.OTHER -> Icons.Default.MoreVert
    }
}

/**
 * 获取分类名称
 */
private fun getCategoryName(category: TemplateCategory): String {
    return category.displayName
}
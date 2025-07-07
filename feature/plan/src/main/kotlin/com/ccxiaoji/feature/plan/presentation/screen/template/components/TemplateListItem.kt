package com.ccxiaoji.feature.plan.presentation.screen.template.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 模板列表项组件 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListItem(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            // 标题和图标
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getCategoryIcon(template.category),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                // 类别标签
                CategoryBadge(template.category)
            }
            
            // 描述
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // 标签
            if (template.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    template.tags.take(3).forEach { tag ->
                        TagChip(tag = tag)
                    }
                    if (template.tags.size > 3) {
                        Text(
                            text = "+${template.tags.size - 3}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
            
            // 评分和使用次数
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 评分
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "评分",
                        modifier = Modifier.size(16.dp),
                        tint = DesignTokens.BrandColors.Warning
                    )
                    Text(
                        text = String.format("%.1f", template.rating),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 使用次数
                Text(
                    text = "使用 ${template.useCount} 次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 分类标签
 */
@Composable
private fun CategoryBadge(category: TemplateCategory) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = getCategoryColor(category).copy(alpha = 0.1f),
        contentColor = getCategoryColor(category)
    ) {
        Text(
            text = getCategoryName(category),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(
                horizontal = DesignTokens.Spacing.small,
                vertical = DesignTokens.Spacing.xs
            )
        )
    }
}

/**
 * 获取分类图标
 */
private fun getCategoryIcon(category: TemplateCategory): String {
    return when (category) {
        TemplateCategory.WORK -> "📋"
        TemplateCategory.STUDY -> "📚"
        TemplateCategory.FITNESS -> "💪"
        TemplateCategory.LIFE -> "🎯"
        TemplateCategory.HEALTH -> "🏥"
        TemplateCategory.SKILL -> "🎨"
        TemplateCategory.PROJECT -> "🚀"
        TemplateCategory.OTHER -> "✨"
    }
}

/**
 * 获取分类名称
 */
@Composable
private fun getCategoryName(category: TemplateCategory) = when (category) {
    TemplateCategory.WORK -> "工作"
    TemplateCategory.STUDY -> "学习"
    TemplateCategory.FITNESS -> "健身"
    TemplateCategory.LIFE -> "生活"
    TemplateCategory.HEALTH -> "健康"
    TemplateCategory.SKILL -> "技能"
    TemplateCategory.PROJECT -> "项目"
    TemplateCategory.OTHER -> "其他"
}

/**
 * 获取分类颜色
 */
@Composable
private fun getCategoryColor(category: TemplateCategory) = when (category) {
    TemplateCategory.WORK -> MaterialTheme.colorScheme.primary
    TemplateCategory.STUDY -> DesignTokens.BrandColors.Info
    TemplateCategory.FITNESS -> DesignTokens.BrandColors.Success
    TemplateCategory.LIFE -> MaterialTheme.colorScheme.tertiary
    TemplateCategory.HEALTH -> DesignTokens.BrandColors.Warning
    TemplateCategory.SKILL -> MaterialTheme.colorScheme.secondary
    TemplateCategory.PROJECT -> MaterialTheme.colorScheme.primary
    TemplateCategory.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
}
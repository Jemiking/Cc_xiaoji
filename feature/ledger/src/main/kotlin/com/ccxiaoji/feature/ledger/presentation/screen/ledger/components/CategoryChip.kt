package com.ccxiaoji.feature.ledger.presentation.screen.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.ui.theme.DesignTokens

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 使用预定义的语义化颜色，而不是解析category.color
    val categoryColor = when {
        category.type == Category.Type.INCOME -> DesignTokens.BrandColors.Success
        category.type == Category.Type.EXPENSE -> DesignTokens.BrandColors.Error
        category.name.contains("餐饮") || category.name.contains("食") -> DesignTokens.BrandColors.Warning
        category.name.contains("交通") || category.name.contains("车") -> DesignTokens.BrandColors.Info
        category.name.contains("购物") || category.name.contains("买") -> DesignTokens.BrandColors.Habit
        category.name.contains("娱乐") || category.name.contains("玩") -> DesignTokens.BrandColors.Schedule
        else -> MaterialTheme.colorScheme.primary
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier.size(72.dp),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
        color = if (isSelected) {
            categoryColor.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (isSelected) {
            BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.3f))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignTokens.Spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    categoryColor
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 分类路径显示组件
 * 显示完整的分类路径，例如："餐饮 / 早餐"
 */
@Composable
fun CategoryPathDisplay(
    icon: String,
    categoryName: String,
    parentName: String? = null,
    modifier: Modifier = Modifier,
    showBackground: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
        color = if (showBackground) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignTokens.Spacing.small,
                    vertical = DesignTokens.Spacing.xs
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Text(
                text = icon,
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
            
            // 分类路径
            if (parentName != null) {
                // 父分类名称
                Text(
                    text = parentName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 分隔符
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 子分类名称
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // 只有一级分类
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 紧凑的分类路径显示组件
 * 用于列表项等空间有限的场景
 */
@Composable
fun CompactCategoryPath(
    icon: String,
    fullPath: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类图标
        Text(
            text = icon,
            style = MaterialTheme.typography.labelMedium
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // 完整路径
        Text(
            text = fullPath,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 分类标签组件
 * 用于显示可点击的分类标签
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTag(
    icon: String,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
package com.ccxiaoji.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 统一的分组标题组件
 * 遵循极简扁平化设计（方案A）
 * 
 * @param title 标题文本
 * @param modifier 修饰符
 * @param subtitle 副标题
 * @param action 操作文本
 * @param onActionClick 操作点击事件
 * @param leadingIcon 前置图标
 * @param trailingContent 尾部内容
 * @param titleColor 标题颜色
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: String? = null,
    onActionClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    titleColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧内容
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 前置图标
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = titleColor
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
            }
            
            // 标题和副标题
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // 右侧内容
        when {
            trailingContent != null -> trailingContent()
            action != null -> {
                TextButton(
                    onClick = { onActionClick?.invoke() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = action,
                        style = MaterialTheme.typography.labelMedium,
                        color = titleColor
                    )
                    if (onActionClick != null) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = titleColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简化版分组标题
 * 仅包含标题和计数
 */
@Composable
fun SimpleSectionHeader(
    title: String,
    count: Int? = null,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = titleColor
        )
        
        count?.let {
            Text(
                text = "$it 项",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 可折叠分组标题
 */
@Composable
fun CollapsibleSectionHeader(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    count: Int? = null,
    titleColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        onClick = { onExpandedChange(!expanded) },
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignTokens.Spacing.medium,
                    vertical = DesignTokens.Spacing.small
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧内容
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 展开/收起图标
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.ChevronRight
                    },
                    contentDescription = if (expanded) "收起" else "展开",
                    modifier = Modifier.size(20.dp),
                    tint = titleColor
                )
                
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                
                // 标题和副标题
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = titleColor
                        )
                        count?.let {
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                            Text(
                                text = "($it)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
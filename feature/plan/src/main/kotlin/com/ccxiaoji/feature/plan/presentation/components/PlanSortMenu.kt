package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.PlanSortBy

/**
 * 计划排序菜单
 * @param showMenu 是否显示菜单
 * @param currentSortBy 当前排序方式
 * @param onSortBySelected 排序方式选择回调
 * @param onDismiss 关闭菜单回调
 */
@Composable
fun PlanSortMenu(
    showMenu: Boolean,
    currentSortBy: PlanSortBy,
    onSortBySelected: (PlanSortBy) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismiss
    ) {
        // 按名称排序
        Text(
            text = "按名称",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        SortMenuItem(
            text = "升序 (A-Z)",
            isSelected = currentSortBy == PlanSortBy.NAME_ASC,
            onClick = {
                onSortBySelected(PlanSortBy.NAME_ASC)
                onDismiss()
            }
        )
        SortMenuItem(
            text = "降序 (Z-A)",
            isSelected = currentSortBy == PlanSortBy.NAME_DESC,
            onClick = {
                onSortBySelected(PlanSortBy.NAME_DESC)
                onDismiss()
            }
        )
        
        Divider(modifier = Modifier.padding(vertical = 4.dp))
        
        // 按时间排序
        Text(
            text = "按时间",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        SortMenuItem(
            text = "更新时间（最新）",
            isSelected = currentSortBy == PlanSortBy.UPDATE_TIME_DESC,
            onClick = {
                onSortBySelected(PlanSortBy.UPDATE_TIME_DESC)
                onDismiss()
            }
        )
        SortMenuItem(
            text = "更新时间（最早）",
            isSelected = currentSortBy == PlanSortBy.UPDATE_TIME_ASC,
            onClick = {
                onSortBySelected(PlanSortBy.UPDATE_TIME_ASC)
                onDismiss()
            }
        )
        SortMenuItem(
            text = "创建时间（最新）",
            isSelected = currentSortBy == PlanSortBy.CREATE_TIME_DESC,
            onClick = {
                onSortBySelected(PlanSortBy.CREATE_TIME_DESC)
                onDismiss()
            }
        )
        SortMenuItem(
            text = "创建时间（最早）",
            isSelected = currentSortBy == PlanSortBy.CREATE_TIME_ASC,
            onClick = {
                onSortBySelected(PlanSortBy.CREATE_TIME_ASC)
                onDismiss()
            }
        )
        
        Divider(modifier = Modifier.padding(vertical = 4.dp))
        
        // 按日期排序
        Text(
            text = "按日期",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        SortMenuItem(
            text = "开始日期（最近）",
            isSelected = currentSortBy == PlanSortBy.START_DATE_ASC,
            onClick = {
                onSortBySelected(PlanSortBy.START_DATE_ASC)
                onDismiss()
            }
        )
        SortMenuItem(
            text = "开始日期（最远）",
            isSelected = currentSortBy == PlanSortBy.START_DATE_DESC,
            onClick = {
                onSortBySelected(PlanSortBy.START_DATE_DESC)
                onDismiss()
            }
        )
        SortMenuItem(
            text = "结束日期（最近）",
            isSelected = currentSortBy == PlanSortBy.END_DATE_ASC,
            onClick = {
                onSortBySelected(PlanSortBy.END_DATE_ASC)
                onDismiss()
            }
        )
        SortMenuItem(
            text = "结束日期（最远）",
            isSelected = currentSortBy == PlanSortBy.END_DATE_DESC,
            onClick = {
                onSortBySelected(PlanSortBy.END_DATE_DESC)
                onDismiss()
            }
        )
        
        Divider(modifier = Modifier.padding(vertical = 4.dp))
        
        // 按进度排序
        Text(
            text = "按进度",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        SortMenuItem(
            text = "进度（从低到高）",
            isSelected = currentSortBy == PlanSortBy.PROGRESS_ASC,
            onClick = {
                onSortBySelected(PlanSortBy.PROGRESS_ASC)
                onDismiss()
            }
        )
        SortMenuItem(
            text = "进度（从高到低）",
            isSelected = currentSortBy == PlanSortBy.PROGRESS_DESC,
            onClick = {
                onSortBySelected(PlanSortBy.PROGRESS_DESC)
                onDismiss()
            }
        )
    }
}

/**
 * 排序菜单项
 */
@Composable
private fun SortMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { 
            Text(
                text = text,
                style = if (isSelected) {
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        },
        onClick = onClick,
        leadingIcon = {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
    )
}
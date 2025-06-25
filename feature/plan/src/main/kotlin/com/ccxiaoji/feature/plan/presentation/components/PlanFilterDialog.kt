package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.DateRange
import com.ccxiaoji.feature.plan.domain.model.PlanFilter
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import kotlinx.datetime.LocalDate

/**
 * 计划筛选对话框
 * @param isOpen 是否显示对话框
 * @param currentFilter 当前筛选条件
 * @param availableTags 可用的标签列表
 * @param onDismiss 关闭对话框回调
 * @param onConfirm 确认筛选条件回调
 */
@Composable
fun PlanFilterDialog(
    isOpen: Boolean,
    currentFilter: PlanFilter,
    availableTags: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (PlanFilter) -> Unit
) {
    if (!isOpen) return
    
    var selectedStatuses by remember(currentFilter) { 
        mutableStateOf(currentFilter.statuses) 
    }
    var selectedTags by remember(currentFilter) { 
        mutableStateOf(currentFilter.tags) 
    }
    var hasChildren by remember(currentFilter) { 
        mutableStateOf(currentFilter.hasChildren) 
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("筛选条件")
                
                // 清除所有筛选
                if (currentFilter.isActive) {
                    TextButton(
                        onClick = {
                            selectedStatuses = emptySet()
                            selectedTags = emptySet()
                            hasChildren = null
                        }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "清除",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("清除")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 状态筛选
                FilterSection(title = "计划状态") {
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        PlanStatus.values().forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .selectable(
                                        selected = status in selectedStatuses,
                                        onClick = {
                                            selectedStatuses = if (status in selectedStatuses) {
                                                selectedStatuses - status
                                            } else {
                                                selectedStatuses + status
                                            }
                                        },
                                        role = Role.Checkbox
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = status in selectedStatuses,
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when(status) {
                                        PlanStatus.NOT_STARTED -> "未开始"
                                        PlanStatus.IN_PROGRESS -> "进行中"
                                        PlanStatus.COMPLETED -> "已完成"
                                        PlanStatus.CANCELLED -> "已取消"
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 标签筛选
                if (availableTags.isNotEmpty()) {
                    FilterSection(title = "标签") {
                        Column(
                            modifier = Modifier.selectableGroup()
                        ) {
                            availableTags.forEach { tag ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .selectable(
                                            selected = tag in selectedTags,
                                            onClick = {
                                                selectedTags = if (tag in selectedTags) {
                                                    selectedTags - tag
                                                } else {
                                                    selectedTags + tag
                                                }
                                            },
                                            role = Role.Checkbox
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = tag in selectedTags,
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = tag)
                                }
                            }
                        }
                    }
                }
                
                // 是否有子计划
                FilterSection(title = "计划类型") {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = hasChildren == null,
                                onClick = { hasChildren = null }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("全部")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = hasChildren == true,
                                onClick = { hasChildren = true }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("父计划（有子计划）")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = hasChildren == false,
                                onClick = { hasChildren = false }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("独立计划（无子计划）")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        PlanFilter(
                            statuses = selectedStatuses,
                            tags = selectedTags,
                            hasChildren = hasChildren
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 筛选区域组件
 */
@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
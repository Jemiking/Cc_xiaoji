package com.ccxiaoji.feature.plan.presentation.screen.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.plan.domain.model.PlanFilter
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.presentation.viewmodel.PlanFilterViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 计划筛选页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanFilterScreen(
    navController: NavController,
    viewModel: PlanFilterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("筛选条件") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 清除所有筛选
                    if (uiState.hasActiveFilters) {
                        TextButton(onClick = viewModel::clearAllFilters) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "清除",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "清除",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.large)
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
                                .height(48.dp)
                                .selectable(
                                    selected = status in uiState.selectedStatuses,
                                    onClick = { viewModel.toggleStatus(status) },
                                    role = Role.Checkbox
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = status in uiState.selectedStatuses,
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                            Text(
                                text = when(status) {
                                    PlanStatus.NOT_STARTED -> "未开始"
                                    PlanStatus.IN_PROGRESS -> "进行中"
                                    PlanStatus.COMPLETED -> "已完成"
                                    PlanStatus.CANCELLED -> "已取消"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            // 标签筛选
            if (uiState.availableTags.isNotEmpty()) {
                FilterSection(title = "标签") {
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        uiState.availableTags.forEach { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = tag in uiState.selectedTags,
                                        onClick = { viewModel.toggleTag(tag) },
                                        role = Role.Checkbox
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = tag in uiState.selectedTags,
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            // 是否有子计划
            FilterSection(title = "计划类型") {
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    RadioButtonOption(
                        text = "全部",
                        selected = uiState.hasChildren == null,
                        onClick = { viewModel.updateHasChildren(null) }
                    )
                    RadioButtonOption(
                        text = "父计划（有子计划）",
                        selected = uiState.hasChildren == true,
                        onClick = { viewModel.updateHasChildren(true) }
                    )
                    RadioButtonOption(
                        text = "独立计划（无子计划）",
                        selected = uiState.hasChildren == false,
                        onClick = { viewModel.updateHasChildren(false) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        val currentState = uiState
                        
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set("filter_applied", true)
                            set("filter_statuses", currentState.selectedStatuses.map { it.name }.toTypedArray())
                            set("filter_tags", currentState.selectedTags.toTypedArray())
                            set("filter_hasChildren", currentState.hasChildren)
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("确定")
                }
            }
        }
    }
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
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.padding(DesignTokens.Spacing.medium)
            ) {
                content()
            }
        }
    }
}

/**
 * 单选按钮选项
 */
@Composable
private fun RadioButtonOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
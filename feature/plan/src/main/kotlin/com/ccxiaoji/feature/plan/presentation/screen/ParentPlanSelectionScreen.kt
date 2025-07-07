package com.ccxiaoji.feature.plan.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.presentation.viewmodel.ParentPlanSelectionViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 父计划选择页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPlanSelectionScreen(
    navController: NavController,
    currentPlanId: String? = null,
    viewModel: ParentPlanSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 初始化
    LaunchedEffect(currentPlanId) {
        viewModel.loadPlans(currentPlanId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择父计划") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = DesignTokens.Spacing.small)
        ) {
            // 无父计划选项
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.Spacing.medium)
                        .clickable {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_parent_id", "")
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_parent_title", "")
                            navController.popBackStack()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "无父计划（作为顶级计划）",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (uiState.selectedId == null) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "已选择",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.medium),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            }
            
            // 计划列表
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.large),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                items(uiState.plans) { plan ->
                    PlanTreeSelectItem(
                        plan = plan,
                        level = 0,
                        selectedId = uiState.selectedId,
                        expandedIds = uiState.expandedIds,
                        onToggleExpand = viewModel::toggleExpand,
                        onSelect = { planId, planTitle ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_parent_id", planId)
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_parent_title", planTitle)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 计划树形选择项
 */
@Composable
private fun PlanTreeSelectItem(
    plan: Plan,
    level: Int,
    selectedId: String?,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit,
    onSelect: (String, String) -> Unit
) {
    val isExpanded = expandedIds.contains(plan.id)
    val isSelected = plan.id == selectedId
    
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = DesignTokens.Spacing.medium + (level * 24).dp,
                    end = DesignTokens.Spacing.medium,
                    top = 4.dp,
                    bottom = 4.dp
                )
                .clickable { onSelect(plan.id, plan.title) },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 展开/折叠按钮
                if (plan.hasChildren) {
                    IconButton(
                        onClick = { onToggleExpand(plan.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Default.KeyboardArrowDown
                            } else {
                                Icons.Default.KeyboardArrowRight
                            },
                            contentDescription = if (isExpanded) "折叠" else "展开",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }
                
                // 状态指示器
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color(android.graphics.Color.parseColor(plan.color)))
                )
                
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                
                // 计划标题
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!plan.description.isNullOrBlank()) {
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // 选中标记
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已选择",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // 子计划
        if (isExpanded && plan.hasChildren) {
            plan.children.forEach { childPlan ->
                PlanTreeSelectItem(
                    plan = childPlan,
                    level = level + 1,
                    selectedId = selectedId,
                    expandedIds = expandedIds,
                    onToggleExpand = onToggleExpand,
                    onSelect = onSelect
                )
            }
        }
    }
}
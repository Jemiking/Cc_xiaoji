package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Plan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 父计划选择器组件
 */
@Composable
fun ParentPlanSelector(
    selectedParentId: String? = null,
    currentPlanId: String? = null, // 当前计划ID，用于编辑时排除自己和子计划
    onParentSelected: (parentId: String?, parentTitle: String?) -> Unit,
    viewModel: ParentPlanSelectorViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    
    // 初始化
    LaunchedEffect(currentPlanId) {
        viewModel.loadPlans(currentPlanId)
    }
    
    // 选中的父计划信息
    val selectedParent = uiState.plans.find { it.id == selectedParentId }
    
    // 触发器
    OutlinedTextField(
        value = selectedParent?.title ?: "",
        onValueChange = { },
        label = { Text("父计划（可选）") },
        placeholder = { Text("点击选择父计划") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            if (selectedParentId != null) {
                TextButton(
                    onClick = { onParentSelected(null, null) }
                ) {
                    Text("清除")
                }
            }
        },
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            showDialog = true
                        }
                    }
                }
            }
    )
    
    // 选择对话框
    if (showDialog) {
        ParentPlanSelectorDialog(
            plans = uiState.plans,
            selectedId = selectedParentId,
            expandedIds = uiState.expandedIds,
            onToggleExpand = viewModel::toggleExpand,
            onSelect = { planId, planTitle ->
                onParentSelected(planId, planTitle)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * 父计划选择对话框
 */
@Composable
private fun ParentPlanSelectorDialog(
    plans: List<Plan>,
    selectedId: String?,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit,
    onSelect: (String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择父计划",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                }
                
                Divider()
                
                // 无父计划选项
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(null, null) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "无父计划（作为顶级计划）",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (selectedId == null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已选择",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Divider()
                
                // 计划列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(plans) { plan ->
                        PlanTreeSelectItem(
                            plan = plan,
                            level = 0,
                            selectedId = selectedId,
                            expandedIds = expandedIds,
                            onToggleExpand = onToggleExpand,
                            onSelect = onSelect
                        )
                    }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(plan.id, plan.title) }
                .padding(
                    start = (16 + level * 24).dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 展开/折叠按钮
            if (plan.hasChildren) {
                IconButton(
                    onClick = { onToggleExpand(plan.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowRight
                        },
                        contentDescription = if (isExpanded) "折叠" else "展开",
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }
            
            // 状态指示器
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(android.graphics.Color.parseColor(plan.color)))
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 计划标题
            Text(
                text = plan.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
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

/**
 * 父计划选择器ViewModel
 */
@HiltViewModel
class ParentPlanSelectorViewModel @Inject constructor(
    private val planRepository: com.ccxiaoji.feature.plan.domain.repository.PlanRepository
) : androidx.lifecycle.ViewModel() {
    
    private val _uiState = MutableStateFlow(ParentPlanSelectorUiState())
    val uiState: StateFlow<ParentPlanSelectorUiState> = _uiState.asStateFlow()
    
    /**
     * 加载计划列表
     */
    fun loadPlans(excludePlanId: String? = null) {
        viewModelScope.launch {
            planRepository.getAllPlansTree().collect { allPlans ->
                val filteredPlans = if (excludePlanId != null) {
                    // 排除当前计划及其子计划
                    filterOutPlanAndChildren(allPlans, excludePlanId)
                } else {
                    allPlans
                }
                
                _uiState.value = _uiState.value.copy(
                    plans = filteredPlans,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 切换展开/折叠状态
     */
    fun toggleExpand(planId: String) {
        _uiState.value = _uiState.value.let { state ->
            val newExpandedIds = if (state.expandedIds.contains(planId)) {
                state.expandedIds - planId
            } else {
                state.expandedIds + planId
            }
            state.copy(expandedIds = newExpandedIds)
        }
    }
    
    /**
     * 过滤掉指定计划及其所有子计划
     */
    private fun filterOutPlanAndChildren(plans: List<Plan>, excludeId: String): List<Plan> {
        return plans.mapNotNull { plan ->
            when {
                plan.id == excludeId -> null
                plan.hasChildren -> {
                    val filteredChildren = filterOutPlanAndChildren(plan.children, excludeId)
                    // 如果过滤后没有子计划了，更新hasChildren状态
                    plan.copy(children = filteredChildren)
                }
                else -> plan
            }
        }
    }
}

/**
 * 父计划选择器UI状态
 */
data class ParentPlanSelectorUiState(
    val plans: List<Plan> = emptyList(),
    val expandedIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)
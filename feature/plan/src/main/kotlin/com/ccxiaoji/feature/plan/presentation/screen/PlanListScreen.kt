package com.ccxiaoji.feature.plan.presentation.plan.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.presentation.plan.components.PlanTreeItem
import com.ccxiaoji.feature.plan.presentation.components.SearchBar
import com.ccxiaoji.feature.plan.presentation.components.PlanFilterDialog
import com.ccxiaoji.feature.plan.presentation.components.PlanSortMenu
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.style.TextAlign
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * 计划列表页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanListScreen(
    onNavigateToPlanDetail: (String) -> Unit,
    onNavigateToCreatePlan: (String?) -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var fabExpanded by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // 使用derivedStateOf优化计划列表的不可变性
    val immutablePlans by remember(uiState.plans) {
        derivedStateOf { uiState.plans.toImmutableList() }
    }
    
    // 使用derivedStateOf优化展开状态集合
    val immutableExpandedPlanIds by remember(uiState.expandedPlanIds) {
        derivedStateOf { uiState.expandedPlanIds.toSet() }
    }
    
    // 显示错误信息
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("CC小记计划书") },
                actions = {
                    IconButton(onClick = onNavigateToAnalysis) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "进度分析"
                        )
                    }
                    
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多选项"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("设置") },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // 子菜单项
                AnimatedVisibility(visible = fabExpanded) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        // 从模板创建
                        ExtendedFloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                onNavigateToTemplates()
                            },
                            modifier = Modifier.padding(bottom = 8.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "从模板创建",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("从模板创建")
                        }
                        
                        // 创建新计划
                        ExtendedFloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                onNavigateToCreatePlan(null)
                            },
                            modifier = Modifier.padding(bottom = 16.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "创建新计划",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("创建新计划")
                        }
                    }
                }
                
                // 主FAB按钮
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (fabExpanded) "关闭" else "创建选项"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            Box(modifier = Modifier.padding(16.dp)) {
                SearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onFilterClick = { showFilterDialog = true },
                    onSortClick = { showSortMenu = true },
                    hasActiveFilters = uiState.filter.isActive
                )
                
                // 排序菜单
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    PlanSortMenu(
                        showMenu = showSortMenu,
                        currentSortBy = uiState.sortBy,
                        onSortBySelected = viewModel::updateSortBy,
                        onDismiss = { showSortMenu = false }
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                immutablePlans.isEmpty() -> {
                    Text(
                        text = "暂无计划\n点击右下角按钮创建第一个计划",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    OptimizedPlanList(
                        plans = immutablePlans,
                        expandedPlanIds = immutableExpandedPlanIds,
                        onToggleExpand = viewModel::togglePlanExpanded,
                        onPlanClick = onNavigateToPlanDetail,
                        onProgressUpdate = viewModel::updatePlanProgress,
                        onDeleteClick = viewModel::deletePlan,
                        onCreateSubPlan = onNavigateToCreatePlan
                    )
                    }
                }
            }
        }
    }
    
    // 筛选对话框
    PlanFilterDialog(
        isOpen = showFilterDialog,
        currentFilter = uiState.filter,
        availableTags = emptySet(), // TODO: 从ViewModel获取可用标签
        onDismiss = { showFilterDialog = false },
        onConfirm = viewModel::updateFilter
    )
    
    // 性能测试对话框
    uiState.performanceTest?.let { testState ->
        PerformanceTestDialog(
            state = testState,
            onDismiss = viewModel::clearPerformanceTestState
        )
    }
}

/**
 * 优化后的计划列表组件
 */
@Composable
private fun OptimizedPlanList(
    plans: ImmutableList<com.ccxiaoji.feature.plan.domain.model.Plan>,
    expandedPlanIds: Set<String>,
    onToggleExpand: (String) -> Unit,
    onPlanClick: (String) -> Unit,
    onProgressUpdate: (String, Float) -> Unit,
    onDeleteClick: (String) -> Unit,
    onCreateSubPlan: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = plans,
            key = { it.id },
            contentType = { 
                // 为不同类型的item提供contentType，优化复用
                when {
                    it.hasChildren -> "parent_plan"
                    it.isRootPlan -> "root_plan"
                    else -> "child_plan"
                }
            }
        ) { plan ->
            // 使用remember缓存回调函数，避免重组时重新创建
            val onToggle = remember(plan.id) { { onToggleExpand(plan.id) } }
            val onClick = remember(plan.id) { { onPlanClick(plan.id) } }
            val onProgress = remember(plan.id) { { progress: Float -> onProgressUpdate(plan.id, progress) } }
            val onDelete = remember(plan.id) { { onDeleteClick(plan.id) } }
            val onCreateSub = remember(plan.id) { { onCreateSubPlan(plan.id) } }
            
            PlanTreeItem(
                plan = plan,
                isExpanded = expandedPlanIds.contains(plan.id),
                onToggleExpand = onToggle,
                onPlanClick = onClick,
                onProgressUpdate = onProgress,
                onDeleteClick = onDelete,
                onCreateSubPlan = onCreateSub,
                level = 0
            )
        }
    }
}

/**
 * 性能测试对话框
 */
@Composable
private fun PerformanceTestDialog(
    state: PerformanceTestState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!state.isRunning) onDismiss() },
        title = { Text("性能测试") },
        text = {
            Column {
                Text(state.message)
                
                if (state.isRunning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                state.result?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "测试结果",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("生成数量: ${result.planCount} 条")
                            Text("耗时: ${result.duration / 1000.0} 秒")
                            Text("平均速度: %.2f 条/秒".format(result.averageSpeed))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!state.isRunning) {
                TextButton(onClick = onDismiss) {
                    Text("确定")
                }
            }
        }
    )
}
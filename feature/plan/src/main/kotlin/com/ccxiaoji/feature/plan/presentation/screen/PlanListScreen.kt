package com.ccxiaoji.feature.plan.presentation.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Observer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.presentation.components.OptimizedPlanTreeItem
import com.ccxiaoji.feature.plan.presentation.components.ExpandableFAB
import com.ccxiaoji.feature.plan.presentation.components.EmptyPlanState
import com.ccxiaoji.feature.plan.presentation.components.SearchBar
import com.ccxiaoji.feature.plan.presentation.components.PlanSortMenu
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.feature.plan.presentation.viewmodel.PlanListViewModel
import com.ccxiaoji.feature.plan.presentation.viewmodel.PerformanceTestState
import com.ccxiaoji.feature.plan.domain.model.PlanFilter
import com.ccxiaoji.feature.plan.domain.model.PlanStatus

/**
 * 计划列表页面 - 扁平化设计优化版
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanListScreen(
    onNavigateToPlanDetail: (String) -> Unit,
    onNavigateToCreatePlan: (String?) -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFilter: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PlanListViewModel = hiltViewModel(),
    navController: androidx.navigation.NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var fabExpanded by remember { mutableStateOf(false) }
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
                title = { 
                    Text(
                        text = "计划书",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = onNavigateToAnalysis) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "进度分析",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多选项",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            ExpandableFAB(
                expanded = fabExpanded,
                onExpandedChange = { fabExpanded = it },
                onCreateNewPlan = { onNavigateToCreatePlan(null) },
                onCreateFromTemplate = onNavigateToTemplates
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            Box(modifier = Modifier.padding(DesignTokens.Spacing.medium)) {
                SearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onFilterClick = onNavigateToFilter,
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
                        CircularProgressIndicator(
                            color = DesignTokens.BrandColors.Plan
                        )
                    }
                    immutablePlans.isEmpty() -> {
                        EmptyPlanState(
                            onCreatePlan = { onNavigateToCreatePlan(null) }
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
    
    // 处理导航返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = Observer<Boolean> { filterApplied ->
                if (filterApplied == true) {
                    // 获取筛选条件
                    val statuses = savedStateHandle.get<Array<String>>("filter_statuses")
                        ?.map { PlanStatus.valueOf(it) }
                        ?.toSet() ?: emptySet()
                    val tags = savedStateHandle.get<Array<String>>("filter_tags")
                        ?.toSet() ?: emptySet()
                    val hasChildren = savedStateHandle.get<Boolean?>("filter_hasChildren")
                    
                    // 应用筛选条件
                    val filter = PlanFilter(
                        statuses = statuses,
                        tags = tags,
                        hasChildren = hasChildren
                    )
                    viewModel.updateFilter(filter)
                    
                    // 清除返回数据
                    savedStateHandle.remove<Boolean>("filter_applied")
                    savedStateHandle.remove<Array<String>>("filter_statuses")
                    savedStateHandle.remove<Array<String>>("filter_tags")
                    savedStateHandle.remove<Boolean?>("filter_hasChildren")
                }
            }
            savedStateHandle.getLiveData<Boolean>("filter_applied").observe(lifecycleOwner, observer)
            
            onDispose {
                savedStateHandle.getLiveData<Boolean>("filter_applied").removeObserver(observer)
            }
        }
    }
    
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
            
            OptimizedPlanTreeItem(
                plan = plan,
                isExpanded = expandedPlanIds.contains(plan.id),
                onToggleExpand = onToggle,
                onPlanClick = onClick,
                onProgressUpdate = onProgress,
                onDeleteClick = onDelete,
                onCreateSubPlan = onCreateSub,
                level = 0,
                isLastChild = plans.indexOf(plan) == plans.size - 1
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
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = DesignTokens.BrandColors.Plan
                    )
                }
                
                state.result?.let { result ->
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                    ModernCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        borderColor = DesignTokens.BrandColors.Plan.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignTokens.Spacing.medium)
                        ) {
                            Text(
                                text = "测试结果",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
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
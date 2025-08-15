package com.ccxiaoji.feature.plan.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.ccxiaoji.feature.plan.presentation.viewmodel.PlanDetailViewModel
import com.ccxiaoji.feature.plan.presentation.components.DeleteMilestoneDialog
import com.ccxiaoji.feature.plan.presentation.screen.detail.components.*
import com.ccxiaoji.ui.components.FlatFAB
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 计划详情页面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onCreateSubPlan: (String) -> Unit,
    onNavigateToPlanDetail: (String) -> Unit,
    onNavigateToUpdateProgress: (String) -> Unit = {},
    onNavigateToAddEditMilestone: (planId: String, milestoneId: String?) -> Unit = { _, _ -> },
    onNavigateToCreateTemplate: (String) -> Unit = {},
    viewModel: PlanDetailViewModel = hiltViewModel(),
    navController: androidx.navigation.NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 加载计划数据
    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
    }
    
    // 处理导航返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val milestoneObserver = androidx.lifecycle.Observer<Boolean> { updated ->
                if (updated == true) {
                    viewModel.loadPlan(planId)
                    savedStateHandle.remove<Boolean>("milestone_updated")
                }
            }
            val templateObserver = androidx.lifecycle.Observer<Boolean> { created ->
                if (created == true) {
                    // 模板创建成功，可以显示提示或刷新界面
                    savedStateHandle.remove<Boolean>("template_created")
                }
            }
            val deleteObserver = androidx.lifecycle.Observer<Boolean> { deleted ->
                if (deleted == true) {
                    savedStateHandle.remove<Boolean>("plan_deleted")
                    onBackClick()
                }
            }
            val milestoneDeletedObserver = androidx.lifecycle.Observer<Boolean> { deleted ->
                if (deleted == true) {
                    viewModel.loadPlan(planId)
                    savedStateHandle.remove<Boolean>("milestone_deleted")
                }
            }
            
            savedStateHandle.getLiveData<Boolean>("milestone_updated").observe(lifecycleOwner, milestoneObserver)
            savedStateHandle.getLiveData<Boolean>("template_created").observe(lifecycleOwner, templateObserver)
            savedStateHandle.getLiveData<Boolean>("plan_deleted").observe(lifecycleOwner, deleteObserver)
            savedStateHandle.getLiveData<Boolean>("milestone_deleted").observe(lifecycleOwner, milestoneDeletedObserver)
            
            onDispose {
                savedStateHandle.getLiveData<Boolean>("milestone_updated").removeObserver(milestoneObserver)
                savedStateHandle.getLiveData<Boolean>("template_created").removeObserver(templateObserver)
                savedStateHandle.getLiveData<Boolean>("plan_deleted").removeObserver(deleteObserver)
                savedStateHandle.getLiveData<Boolean>("milestone_deleted").removeObserver(milestoneDeletedObserver)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("计划详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 编辑按钮
                    IconButton(onClick = { onEditClick(planId) }) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 更多操作
                    var showMenu by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("更新进度") },
                            onClick = {
                                showMenu = false
                                onNavigateToUpdateProgress(planId)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("创建模板") },
                            onClick = {
                                showMenu = false
                                onNavigateToCreateTemplate(planId)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        HorizontalDivider()
                        
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "删除计划",
                                    color = MaterialTheme.colorScheme.error
                                ) 
                            },
                            onClick = {
                                showMenu = false
                                navController?.navigate("delete_plan/$planId")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FlatFAB(
                onClick = { uiState.plan?.let { onCreateSubPlan(it.id) } },
                containerColor = DesignTokens.BrandColors.Plan
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "添加子计划"
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            uiState.plan != null -> {
                val plan = uiState.plan!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 基本信息卡片
                    item {
                        PlanInfoCard(
                            plan = plan,
                            onStatusChange = viewModel::updateStatus,
                            onProgressChange = viewModel::updateProgress,
                            onEditProgress = { onNavigateToUpdateProgress(planId) }
                        )
                    }
                    
                    // 里程碑卡片
                    item {
                        MilestonesCard(
                            milestones = plan.milestones,
                            onMilestoneToggle = viewModel::toggleMilestone,
                            onAddMilestone = { onNavigateToAddEditMilestone(planId, null) },
                            viewModel = viewModel,
                            onEditMilestone = { milestoneId ->
                                onNavigateToAddEditMilestone(planId, milestoneId)
                            },
                            onDeleteMilestone = { milestoneId ->
                                navController?.navigate("delete_milestone/$planId/$milestoneId")
                            }
                        )
                    }
                    
                    // 子计划列表
                    if (plan.hasChildren) {
                        item {
                            SubPlansCard(
                                subPlans = plan.children,
                                onNavigateToPlan = onNavigateToPlanDetail,
                                onCreateSubPlan = { onCreateSubPlan(plan.id) }
                            )
                        }
                    }
                }
            }
            
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "计划不存在", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    
    // 错误提示
    uiState.error?.let { error ->
        FlatDialog(
            onDismissRequest = { viewModel.clearError() },
            title = "错误",
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // 处理里程碑导航
    LaunchedEffect(uiState.showMilestoneDialog) {
        if (uiState.showMilestoneDialog) {
            onNavigateToAddEditMilestone(planId, uiState.editingMilestone?.id)
            viewModel.hideMilestoneDialog()
        }
    }
    
}
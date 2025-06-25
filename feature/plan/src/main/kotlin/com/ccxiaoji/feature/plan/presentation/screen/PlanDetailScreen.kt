package com.ccxiaoji.feature.plan.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.presentation.components.MilestoneDialog
import com.ccxiaoji.feature.plan.presentation.components.DeleteMilestoneDialog
import com.ccxiaoji.feature.plan.presentation.components.ProgressUpdateDialog
import com.ccxiaoji.feature.plan.presentation.components.CreateTemplateDialog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/**
 * 计划详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onCreateSubPlan: (String) -> Unit,
    onNavigateToPlanDetail: (String) -> Unit,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 加载计划数据
    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("计划详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 编辑按钮
                    IconButton(onClick = { onEditClick(planId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    
                    // 更多操作
                    var showMenu by remember { mutableStateOf(false) }
                    var showProgressDialog by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("更新进度") },
                            onClick = {
                                showMenu = false
                                showProgressDialog = true
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
                                viewModel.showCreateTemplateDialog()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        Divider()
                        
                        DropdownMenuItem(
                            text = { Text("删除计划") },
                            onClick = {
                                showMenu = false
                                viewModel.showDeleteConfirmation()
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
                    
                    // 进度更新对话框
                    if (showProgressDialog && uiState.plan != null) {
                        ProgressUpdateDialog(
                            currentProgress = uiState.plan!!.progress.toInt(),
                            hasChildren = uiState.plan!!.hasChildren,
                            onProgressUpdate = { newProgress ->
                                viewModel.updateProgress(newProgress.toFloat())
                            },
                            onDismiss = { showProgressDialog = false }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { uiState.plan?.let { onCreateSubPlan(it.id) } }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加子计划")
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
                    CircularProgressIndicator()
                }
            }
            
            uiState.plan != null -> {
                val plan = uiState.plan!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 基本信息卡片
                    item {
                        PlanInfoCard(
                            plan = plan,
                            onStatusChange = viewModel::updateStatus,
                            onProgressChange = viewModel::updateProgress
                        )
                    }
                    
                    // 里程碑卡片
                    item {
                        MilestonesCard(
                            milestones = plan.milestones,
                            onMilestoneToggle = viewModel::toggleMilestone,
                            onAddMilestone = { viewModel.showAddMilestoneDialog() },
                            viewModel = viewModel
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
                    Text("计划不存在", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
    
    // 删除确认对话框
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { Text("删除计划") },
            text = { 
                Text(
                    if (uiState.plan?.hasChildren == true) {
                        "此计划包含子计划，删除后所有子计划也将被删除。确定要删除吗？"
                    } else {
                        "确定要删除这个计划吗？"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlan()
                        onBackClick()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 错误提示
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        )
    }
    
    // 里程碑编辑对话框
    if (uiState.showMilestoneDialog) {
        MilestoneDialog(
            milestone = uiState.editingMilestone,
            planId = planId,
            onDismiss = { viewModel.hideMilestoneDialog() },
            onConfirm = { milestone ->
                viewModel.saveMilestone(milestone)
            }
        )
    }
    
    // 删除里程碑确认对话框
    uiState.deletingMilestone?.let { milestone ->
        DeleteMilestoneDialog(
            milestone = milestone,
            onDismiss = { viewModel.hideDeleteMilestoneDialog() },
            onConfirm = { viewModel.deleteMilestone() }
        )
    }
    
    // 创建模板对话框
    if (uiState.showCreateTemplateDialog) {
        CreateTemplateDialog(
            planTitle = uiState.plan?.title ?: "",
            onDismiss = { viewModel.hideCreateTemplateDialog() },
            onConfirm = { title, description, category, tags, isPublic ->
                viewModel.createTemplateFromPlan(
                    title = title,
                    description = description,
                    category = category,
                    tags = tags,
                    isPublic = isPublic
                )
            }
        )
    }
}

/**
 * 计划基本信息卡片
 */
@Composable
private fun PlanInfoCard(
    plan: Plan,
    onStatusChange: (PlanStatus) -> Unit,
    onProgressChange: (Float) -> Unit
) {
    var showProgressDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (plan.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 状态指示器
                StatusChip(
                    status = plan.status,
                    onClick = {
                        // 显示状态选择菜单
                    }
                )
            }
            
            // 日期范围
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${formatDate(plan.startDate)} - ${formatDate(plan.endDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 进度条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "进度",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${plan.progress.toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (!plan.hasChildren) {
                            IconButton(
                                onClick = { showProgressDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "编辑进度",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                LinearProgressIndicator(
                    progress = { plan.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(android.graphics.Color.parseColor(plan.color)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                if (plan.hasChildren) {
                    Text(
                        text = "进度由子计划自动计算",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 标签
            if (plan.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    plan.tags.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
    }
    
    // 进度更新对话框
    if (showProgressDialog) {
        ProgressUpdateDialog(
            currentProgress = plan.progress.toInt(),
            hasChildren = plan.hasChildren,
            onProgressUpdate = { newProgress ->
                onProgressChange(newProgress.toFloat())
            },
            onDismiss = { showProgressDialog = false }
        )
    }
}

/**
 * 里程碑卡片
 */
@Composable
private fun MilestonesCard(
    milestones: List<Milestone>,
    onMilestoneToggle: (String) -> Unit,
    onAddMilestone: () -> Unit,
    viewModel: PlanDetailViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "里程碑",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onAddMilestone) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加")
                }
            }
            
            if (milestones.isEmpty()) {
                Text(
                    text = "暂无里程碑",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                milestones.forEach { milestone ->
                    MilestoneItem(
                        milestone = milestone,
                        onToggle = { onMilestoneToggle(milestone.id) },
                        onEdit = { viewModel.showEditMilestoneDialog(milestone) },
                        onDelete = { viewModel.showDeleteMilestoneDialog(milestone) }
                    )
                }
            }
        }
    }
}

/**
 * 里程碑项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MilestoneItem(
    milestone: Milestone,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = milestone.isCompleted,
                onCheckedChange = { onToggle() }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (milestone.isCompleted) FontWeight.Normal else FontWeight.Medium
                )
                
                Text(
                    text = if (milestone.isCompleted && milestone.completedDate != null) {
                        "已完成于 ${formatDate(milestone.completedDate)}"
                    } else {
                        "目标日期：${formatDate(milestone.targetDate)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (milestone.description.isNotBlank()) {
                    Text(
                        text = milestone.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 更多操作菜单
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "更多",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            showMenu = false
                            onDelete()
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
            }
        }
    }
}

/**
 * 子计划卡片
 */
@Composable
private fun SubPlansCard(
    subPlans: List<Plan>,
    onNavigateToPlan: (String) -> Unit,
    onCreateSubPlan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "子计划 (${subPlans.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onCreateSubPlan) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            subPlans.forEach { subPlan ->
                SubPlanItem(
                    plan = subPlan,
                    onClick = { onNavigateToPlan(subPlan.id) }
                )
            }
        }
    }
}

/**
 * 子计划项
 */
@Composable
private fun SubPlanItem(
    plan: Plan,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示器
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (plan.status) {
                            PlanStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
                            PlanStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                            PlanStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                            PlanStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 标题和进度
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LinearProgressIndicator(
                    progress = { plan.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(android.graphics.Color.parseColor(plan.color)),
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 进度百分比
            Text(
                text = "${plan.progress.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 导航箭头
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 状态芯片
 */
@Composable
private fun StatusChip(
    status: PlanStatus,
    onClick: () -> Unit
) {
    val (text, containerColor, contentColor) = when (status) {
        PlanStatus.NOT_STARTED -> Triple(
            "未开始",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        PlanStatus.IN_PROGRESS -> Triple(
            "进行中",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        PlanStatus.COMPLETED -> Triple(
            "已完成",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        PlanStatus.CANCELLED -> Triple(
            "已取消",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }
    
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        )
    )
}

/**
 * 格式化日期
 */
private fun formatDate(date: LocalDate): String {
    return date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
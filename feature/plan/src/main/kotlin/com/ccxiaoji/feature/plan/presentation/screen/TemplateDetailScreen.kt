package com.ccxiaoji.feature.plan.presentation.template.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.model.MilestoneTemplate
import com.ccxiaoji.feature.plan.presentation.template.ApplyTemplateDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: String,
    onBack: () -> Unit,
    onNavigateToPlanDetail: (String) -> Unit,
    viewModel: TemplateDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showApplyDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(templateId) {
        viewModel.loadTemplate(templateId)
    }
    
    // 处理应用模板成功后的导航
    LaunchedEffect(uiState.appliedPlanId) {
        uiState.appliedPlanId?.let { planId ->
            onNavigateToPlanDetail(planId)
            viewModel.resetAppliedPlanId()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模板详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.template != null) {
                ExtendedFloatingActionButton(
                    onClick = { showApplyDialog = true },
                    text = { Text("应用模板") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
            }
        }
    ) { paddingValues ->
        val currentState = uiState
        when {
            currentState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            currentState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadTemplate(templateId) }) {
                        Text("重试")
                    }
                }
            }
            currentState.template != null -> {
                TemplateDetailContent(
                    template = currentState.template,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
    
    // 应用模板对话框
    val template = uiState.template
    if (showApplyDialog && template != null) {
        ApplyTemplateDialog(
            template = template,
            onDismiss = { showApplyDialog = false },
            onConfirm = { title, startDate, parentId ->
                viewModel.applyTemplate(
                    templateId = template.id,
                    title = title,
                    startDate = startDate,
                    parentId = parentId
                )
                showApplyDialog = false
            },
            isLoading = uiState.isApplying
        )
    }
}

@Composable
private fun TemplateDetailContent(
    template: Template,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 模板基本信息卡片
        item {
            TemplateInfoCard(template)
        }
        
        // 模板结构预览
        item {
            TemplateStructureCard(template)
        }
        
        // 使用统计卡片
        item {
            TemplateStatisticsCard(template)
        }
    }
}

@Composable
private fun TemplateInfoCard(template: Template) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和图标
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(template.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(getCategoryName(template.category)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            
            // 描述
            if (template.description.isNotEmpty()) {
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 标签
            if (template.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    template.tags.forEach { tag ->
                        ElevatedAssistChip(
                            onClick = { },
                            label = { Text(tag) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateStructureCard(template: Template) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "模板结构",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 计划时长
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "预计时长：${template.duration}天",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Divider()
            
            // 子计划结构预览
            if (template.structure.subPlans.isNotEmpty()) {
                Text(
                    text = "包含的子计划",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                template.structure.subPlans.forEach { subPlan ->
                    SubPlanItem(
                        title = subPlan.title,
                        description = subPlan.description,
                        duration = subPlan.duration,
                        milestones = subPlan.milestones
                    )
                }
            }
        }
    }
}

@Composable
private fun SubPlanItem(
    title: String,
    description: String,
    duration: Int,
    milestones: List<MilestoneTemplate>
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${duration}天",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "收起" else "展开"
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (milestones.isNotEmpty()) {
                    Text(
                        text = "里程碑：",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    milestones.forEach { milestone ->
                        Row(
                            modifier = Modifier.padding(start = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${milestone.title} (第${milestone.dayOffset}天)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateStatisticsCard(template: Template) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                icon = Icons.Default.Person,
                label = "使用次数",
                value = template.useCount.toString()
            )
            
            StatisticItem(
                icon = Icons.Default.Star,
                label = "平均评分",
                value = if (template.rating > 0) String.format("%.1f", template.rating) else "暂无"
            )
            
            StatisticItem(
                icon = Icons.Default.CheckCircle,
                label = "完成率",
                value = "暂无数据"
            )
        }
    }
}

@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getCategoryIcon(category: com.ccxiaoji.feature.plan.domain.model.TemplateCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        com.ccxiaoji.feature.plan.domain.model.TemplateCategory.WORK -> Icons.Default.AccountCircle
        com.ccxiaoji.feature.plan.domain.model.TemplateCategory.STUDY -> Icons.Default.Edit
        com.ccxiaoji.feature.plan.domain.model.TemplateCategory.FITNESS -> Icons.Default.PlayArrow
        com.ccxiaoji.feature.plan.domain.model.TemplateCategory.LIFE -> Icons.Default.Home
        com.ccxiaoji.feature.plan.domain.model.TemplateCategory.HEALTH -> Icons.Default.Favorite
        com.ccxiaoji.feature.plan.domain.model.TemplateCategory.SKILL -> Icons.Default.Build
        com.ccxiaoji.feature.plan.domain.model.TemplateCategory.PROJECT -> Icons.Default.Share
        com.ccxiaoji.feature.plan.domain.model.TemplateCategory.OTHER -> Icons.Default.MoreVert
    }
}

private fun getCategoryName(category: com.ccxiaoji.feature.plan.domain.model.TemplateCategory): String {
    return category.displayName
}
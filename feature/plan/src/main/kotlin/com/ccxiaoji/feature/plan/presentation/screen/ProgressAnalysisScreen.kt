package com.ccxiaoji.feature.plan.presentation.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.domain.model.PlanStatistics

/**
 * 进度分析页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressAnalysisScreen(
    onBackClick: () -> Unit,
    viewModel: ProgressAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("进度分析") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("导出报告") },
                            onClick = {
                                showMenu = false
                                // TODO: 实现导出功能
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.statistics != null -> {
                    val statistics = uiState.statistics
                    if (statistics != null) {
                        StatisticsContent(
                            statistics = statistics,
                            selectedChartType = uiState.selectedChartType,
                            onChartTypeChange = viewModel::toggleChartType
                        )
                    }
                }
                
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
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
}

/**
 * 统计内容
 */
@Composable
private fun StatisticsContent(
    statistics: PlanStatistics,
    selectedChartType: ChartType,
    onChartTypeChange: (ChartType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 总体统计卡片
        OverallStatisticsCard(statistics)
        
        // 图表类型选择
        ChartTypeSelector(selectedChartType, onChartTypeChange)
        
        // 图表展示
        when (selectedChartType) {
            ChartType.STATUS_PIE -> StatusDistributionCard(statistics)
            ChartType.PROGRESS_BAR -> ProgressDistributionCard(statistics)
            ChartType.MONTHLY_TREND -> MonthlyTrendCard(statistics)
            ChartType.TAG_ANALYSIS -> TagAnalysisCard(statistics)
        }
        
        // 其他统计信息
        DeadlineStatisticsCard(statistics)
    }
}

/**
 * 总体统计卡片
 */
@Composable
private fun OverallStatisticsCard(statistics: PlanStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "总体统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "总计划数",
                    value = statistics.totalPlans.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatisticItem(
                    label = "完成率",
                    value = "${statistics.completionRate.toInt()}%",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatisticItem(
                    label = "平均进度",
                    value = "${statistics.averageProgress.toInt()}%",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 图表类型选择器
 */
@Composable
private fun ChartTypeSelector(
    selectedType: ChartType,
    onTypeChange: (ChartType) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = ChartType.values().indexOf(selectedType),
        edgePadding = 0.dp
    ) {
        ChartType.values().forEach { type ->
            Tab(
                selected = selectedType == type,
                onClick = { onTypeChange(type) },
                text = {
                    Text(
                        text = when (type) {
                            ChartType.STATUS_PIE -> "状态分布"
                            ChartType.PROGRESS_BAR -> "进度分布"
                            ChartType.MONTHLY_TREND -> "月度趋势"
                            ChartType.TAG_ANALYSIS -> "标签分析"
                        }
                    )
                }
            )
        }
    }
}

/**
 * 状态分布卡片
 */
@Composable
private fun StatusDistributionCard(statistics: PlanStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "状态分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 简单的文字展示，后续可以替换为饼图
            StatusRow("未开始", statistics.notStartedPlans, statistics.totalPlans, MaterialTheme.colorScheme.onSurfaceVariant)
            StatusRow("进行中", statistics.inProgressPlans, statistics.totalPlans, MaterialTheme.colorScheme.primary)
            StatusRow("已完成", statistics.completedPlans, statistics.totalPlans, MaterialTheme.colorScheme.tertiary)
            StatusRow("已取消", statistics.cancelledPlans, statistics.totalPlans, MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * 状态行
 */
@Composable
private fun StatusRow(
    status: String,
    count: Int,
    total: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, shape = MaterialTheme.shapes.small)
            )
            Text(text = status)
        }
        Text(
            text = "$count (${if (total > 0) (count * 100 / total) else 0}%)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 进度分布卡片
 */
@Composable
private fun ProgressDistributionCard(statistics: PlanStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "进度分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            statistics.progressDistribution.forEach { range ->
                ProgressRangeRow(range)
            }
        }
    }
}

/**
 * 进度区间行
 */
@Composable
private fun ProgressRangeRow(range: com.ccxiaoji.feature.plan.domain.model.ProgressRange) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = range.range)
            Text(
                text = "${range.count}个 (${range.percentage.toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { range.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * 月度趋势卡片
 */
@Composable
private fun MonthlyTrendCard(statistics: PlanStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "月度趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 简单的文字展示，后续可以替换为折线图
            statistics.monthlyStats.forEach { monthly ->
                MonthlyStatRow(monthly)
            }
        }
    }
}

/**
 * 月度统计行
 */
@Composable
private fun MonthlyStatRow(monthly: com.ccxiaoji.feature.plan.domain.model.MonthlyStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = monthly.yearMonth,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "创建: ${monthly.created}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "完成: ${monthly.completed}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

/**
 * 标签分析卡片
 */
@Composable
private fun TagAnalysisCard(statistics: PlanStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "标签分析",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (statistics.tagStats.isEmpty()) {
                Text(
                    text = "暂无标签数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                statistics.tagStats.take(10).forEach { tagStat ->
                    TagStatRow(tagStat)
                }
            }
        }
    }
}

/**
 * 标签统计行
 */
@Composable
private fun TagStatRow(tagStat: com.ccxiaoji.feature.plan.domain.model.TagStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = { },
            label = { Text(tagStat.tag) },
            modifier = Modifier.height(28.dp)
        )
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${tagStat.count}个计划",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "平均进度: ${tagStat.avgProgress.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 截止日期统计卡片
 */
@Composable
private fun DeadlineStatisticsCard(statistics: PlanStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statistics.overdueePlans.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "已逾期",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Divider(
                modifier = Modifier
                    .height(48.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statistics.upcomingDeadlines.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "即将到期",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
package com.ccxiaoji.feature.plan.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.domain.model.ChartType
import com.ccxiaoji.feature.plan.domain.model.PlanStatistics
import com.ccxiaoji.feature.plan.presentation.viewmodel.ProgressAnalysisViewModel
import com.ccxiaoji.feature.plan.presentation.screen.analysis.components.*
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 进度分析页面 - 扁平化设计
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多选项",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                uiState.statistics != null -> {
                    StatisticsContent(
                        statistics = uiState.statistics!!,
                        selectedChartType = uiState.selectedChartType,
                        onChartTypeChange = viewModel::toggleChartType
                    )
                }
                
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
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
            .padding(DesignTokens.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
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


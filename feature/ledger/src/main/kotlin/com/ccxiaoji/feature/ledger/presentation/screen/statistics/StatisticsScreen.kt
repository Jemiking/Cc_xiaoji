package com.ccxiaoji.feature.ledger.presentation.screen.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log
import com.ccxiaoji.feature.ledger.presentation.component.charts.BarChart
import com.ccxiaoji.feature.ledger.presentation.component.charts.LineChart
import com.ccxiaoji.feature.ledger.presentation.component.charts.PieChart
import com.ccxiaoji.feature.ledger.presentation.viewmodel.StatisticsViewModel
import com.ccxiaoji.feature.ledger.presentation.screen.statistics.components.*
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 🔍 监控UI状态变化
    LaunchedEffect(uiState.selectedPeriod, uiState.showDateRangePicker) {
        Log.d("StatisticsScreen", "📱 Screen状态变化 - selectedPeriod: ${uiState.selectedPeriod}, showDateRangePicker: ${uiState.showDateRangePicker}")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "统计分析",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .heightIn(min = 400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StatisticsLoadingState()
                }
            }
            uiState.totalIncome == 0 && uiState.totalExpense == 0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .heightIn(min = 400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StatisticsEmptyState(
                        onRefresh = viewModel::refreshStatistics
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 时间段选择器
                    item {
                        Log.d("StatisticsScreen", "🎯 渲染TimePeriodSelector - selectedPeriod: ${uiState.selectedPeriod}")
                        TimePeriodSelector(
                            selectedPeriod = uiState.selectedPeriod,
                            onPeriodSelected = { period ->
                                Log.d("StatisticsScreen", "🎯 TimePeriodSelector回调触发 - period: $period")
                                viewModel.selectTimePeriod(period)
                            }
                        )
                    }
                    
                    // 统计摘要
                    item {
                        SummaryRow(
                            totalIncome = uiState.totalIncome,
                            totalExpense = uiState.totalExpense,
                            balance = uiState.balance,
                            savingsRate = uiState.savingsRate
                        )
                    }
                    
                    // 收支趋势图表
                    item {
                        ChartCard(title = "收支趋势") {
                            LineChart(
                                data = uiState.dailyTotals,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // 支出分类饼图
                    if (uiState.expenseCategories.isNotEmpty()) {
                        item {
                            ChartCard(title = "支出分类") {
                                PieChart(
                                    data = uiState.expenseCategories,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    
                    // 收入分类饼图
                    if (uiState.incomeCategories.isNotEmpty()) {
                        item {
                            ChartCard(title = "收入分类") {
                                PieChart(
                                    data = uiState.incomeCategories,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    
                    // 支出排行榜
                    if (uiState.topExpenses.isNotEmpty()) {
                        item {
                            ChartCard(title = "支出排行榜") {
                                BarChart(
                                    transactions = uiState.topExpenses.take(5),
                                    isExpense = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    
                    // 收入排行榜
                    if (uiState.topIncomes.isNotEmpty()) {
                        item {
                            ChartCard(title = "收入排行榜") {
                                BarChart(
                                    transactions = uiState.topIncomes.take(5),
                                    isExpense = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    
                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xxl))
                    }
                }
            }
        }
    }
    
    // 🆕 日期范围选择器对话框
    if (uiState.showDateRangePicker) {
        Log.d("StatisticsScreen", "📅 正在显示日期选择器对话框")
        DateRangePickerDialog(
            onDismiss = {
                Log.d("StatisticsScreen", "📅 用户关闭日期选择器")
                viewModel.hideDateRangePicker()
            },
            onConfirm = { startDate, endDate ->
                Log.d("StatisticsScreen", "📅 用户确认日期选择: $startDate 到 $endDate")
                viewModel.setCustomDateRange(startDate, endDate)
            },
            initialStartDate = uiState.customStartDate,
            initialEndDate = uiState.customEndDate
        )
    } else {
        Log.d("StatisticsScreen", "📅 日期选择器未显示 - showDateRangePicker: ${uiState.showDateRangePicker}")
    }
}
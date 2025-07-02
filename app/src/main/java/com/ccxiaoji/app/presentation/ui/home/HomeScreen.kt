package com.ccxiaoji.app.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.R
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import com.ccxiaoji.app.presentation.ui.components.MiniSavingsGoalCard
import com.ccxiaoji.app.presentation.ui.home.components.*
import com.ccxiaoji.app.presentation.viewmodel.HomeViewModel
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToLedger: () -> Unit = {},
    onNavigateToTodo: () -> Unit = {},
    onNavigateToHabit: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onQuickAddTransaction: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToSavingsGoal: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = { /* TODO: 实现通知功能 */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "通知",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset { IntOffset(x = 2.dp.roundToPx(), y = (-4).dp.roundToPx()) }
                        ) {
                            Text("3", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small
            ),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 今日概览卡片
            item {
                TodayOverviewCard(
                    todayIncome = uiState.todayIncome,
                    todayExpense = uiState.todayExpense,
                    totalBalance = uiState.totalAccountBalance
                )
            }
            
            // 模块卡片网格（响应式布局）
            item {
                Text(
                    text = "功能模块",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = DesignTokens.Spacing.small)
                )
            }
            
            // 记账模块卡片
            item {
                LedgerModuleCard(
                    todayIncome = uiState.todayIncome,
                    todayExpense = uiState.todayExpense,
                    budgetUsagePercentage = uiState.budgetUsagePercentage,
                    onCardClick = onNavigateToLedger,
                    onQuickAdd = onQuickAddTransaction
                )
            }
            
            // 待办模块卡片
            item {
                TodoModuleCard(
                    todayTodoCount = uiState.todayTasks,
                    completedCount = uiState.todayCompletedTasks,
                    onCardClick = onNavigateToTodo,
                    onViewTodos = onNavigateToTodo
                )
            }
            
            // 习惯模块卡片
            item {
                HabitModuleCard(
                    todayCheckedCount = uiState.todayCheckedHabits,
                    totalHabitCount = uiState.activeHabits,
                    longestStreak = uiState.longestHabitStreak,
                    onCardClick = onNavigateToHabit,
                    onCheckIn = onNavigateToHabit
                )
            }
            
            // 计划模块卡片
            item {
                PlanModuleCard(
                    activePlansCount = uiState.activePlansCount,
                    todayPlansCount = uiState.todayPlansCount,
                    averageProgress = uiState.planAverageProgress,
                    onCardClick = onNavigateToPlan,
                    onViewPlans = onNavigateToPlan
                )
            }
            
            // 底部间距
            item {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            }
        }
    }
}


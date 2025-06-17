package com.ccxiaoji.app.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.R
import com.ccxiaoji.app.domain.model.SavingsGoal
import com.ccxiaoji.app.presentation.ui.components.MiniSavingsGoalCard
import com.ccxiaoji.app.presentation.ui.home.components.*
import com.ccxiaoji.app.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToLedger: () -> Unit = {},
    onNavigateToTodo: () -> Unit = {},
    onNavigateToHabit: () -> Unit = {},
    onQuickAddTransaction: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToSavingsGoal: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: 实现通知功能 */ },
                        enabled = false
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "通知",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            
            // 今日概览卡片
            TodayOverviewCard(
                todayIncome = uiState.todayIncome,
                todayExpense = uiState.todayExpense,
                totalBalance = uiState.totalAccountBalance,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // 记账模块卡片
            LedgerModuleCard(
                todayIncome = uiState.todayIncome,
                todayExpense = uiState.todayExpense,
                budgetUsagePercentage = uiState.budgetUsagePercentage,
                onCardClick = onNavigateToLedger,
                onQuickAdd = onQuickAddTransaction,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // 待办模块卡片
            TodoModuleCard(
                todayTodoCount = uiState.todayTasks,
                completedCount = uiState.todayCompletedTasks,
                onCardClick = onNavigateToTodo,
                onViewTodos = onNavigateToTodo,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // 习惯模块卡片
            HabitModuleCard(
                todayCheckedCount = uiState.todayCheckedHabits,
                totalHabitCount = uiState.activeHabits,
                longestStreak = uiState.longestHabitStreak,
                onCardClick = onNavigateToHabit,
                onCheckIn = onNavigateToHabit,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


package com.ccxiaoji.feature.ledger.presentation.screen.savings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.presentation.screen.savings.components.*
import com.ccxiaoji.feature.ledger.presentation.viewmodel.SavingsGoalViewModel
import com.ccxiaoji.ui.components.FlatExtendedFAB
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddGoal: () -> Unit,
    viewModel: SavingsGoalViewModel = hiltViewModel()
) {
    val goals by viewModel.activeSavingsGoals.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    
    // 处理消息显示
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessage()
            }
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearMessage()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "储蓄目标",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (goals.isNotEmpty()) {
                FlatExtendedFAB(
                    text = { Text("新建目标") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = onNavigateToAddGoal,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (goals.isEmpty()) {
                EmptySavingsState(
                    onAddClick = onNavigateToAddGoal
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 储蓄概览卡片
                    item {
                        SavingsSummaryCard(goals = goals)
                    }
                    
                    // 目标列表
                    items(goals) { goal ->
                        SavingsGoalItem(
                            goal = goal,
                            onClick = { onNavigateToDetail(goal.id) }
                        )
                    }
                    
                    // 为FAB添加间距
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
            
            // 加载指示器
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
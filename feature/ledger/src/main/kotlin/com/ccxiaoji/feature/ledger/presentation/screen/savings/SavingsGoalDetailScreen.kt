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
import com.ccxiaoji.feature.ledger.domain.model.SavingsContribution
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import com.ccxiaoji.feature.ledger.presentation.screen.savings.components.*
import com.ccxiaoji.feature.ledger.presentation.screen.savings.dialogs.FlatDeleteContributionDialog
import com.ccxiaoji.feature.ledger.presentation.navigation.DeleteGoalRoute
import com.ccxiaoji.feature.ledger.presentation.viewmodel.SavingsGoalViewModel
import com.ccxiaoji.ui.components.FlatExtendedFAB
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch

/**
 * 储蓄目标详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalDetailScreen(
    goalId: Long,
    navController: androidx.navigation.NavController,
    onNavigateBack: () -> Unit,
    onNavigateToEditGoal: (Long) -> Unit,
    onNavigateToContribution: (Long) -> Unit,
    viewModel: SavingsGoalViewModel = hiltViewModel()
) {
    val goal by viewModel.selectedGoal.collectAsStateWithLifecycle()
    val contributions by viewModel.contributions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var contributionToDelete by remember { mutableStateOf<SavingsContribution?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(goalId) {
        viewModel.selectGoal(goalId)
    }
    
    LaunchedEffect(uiState.message, uiState.error) {
        val message = uiState.message ?: uiState.error
        if (message != null) {
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            viewModel.clearMessage()
        }
    }
    
    goal?.let { currentGoal ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = currentGoal.name,
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
                    actions = {
                        IconButton(onClick = { onNavigateToEditGoal(goalId) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "编辑",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                if (!currentGoal.isCompleted) {
                    FlatExtendedFAB(
                        onClick = { onNavigateToContribution(goalId) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("记录存款") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 目标概览卡片
                    item {
                        GoalOverviewCard(goal = currentGoal)
                    }
                    
                    // 进度详情
                    item {
                        ProgressDetailsCard(goal = currentGoal)
                    }
                    
                    // 快速操作
                    if (!currentGoal.isCompleted) {
                        item {
                            QuickActionsCard(
                                onDeposit = { onNavigateToContribution(goalId) },
                                onWithdraw = { onNavigateToContribution(goalId) }
                            )
                        }
                    }
                    
                    // 贡献记录标题
                    item {
                        Text(
                            text = "存款记录",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                top = DesignTokens.Spacing.small,
                                bottom = DesignTokens.Spacing.small
                            )
                        )
                    }
                    
                    // 贡献记录列表
                    if (contributions.isEmpty()) {
                        item {
                            EmptyContributionState()
                        }
                    } else {
                        items(
                            items = contributions,
                            key = { it.id }
                        ) { contribution ->
                            ContributionItem(
                                contribution = contribution,
                                onDelete = {
                                    contributionToDelete = contribution
                                }
                            )
                        }
                    }
                    
                    // FAB间距
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
        
        // 删除确认导航
        if (showDeleteDialog) {
            LaunchedEffect(Unit) {
                onNavigateBack() // 先返回
                // 延迟导航到删除确认页面，避免导航冲突
                kotlinx.coroutines.delay(100)
                navController.navigate(
                    DeleteGoalRoute.createRoute(
                        goalId = currentGoal.id,
                        goalName = currentGoal.name
                    )
                )
                showDeleteDialog = false
            }
        }
        
        // 处理删除确认返回结果
        LaunchedEffect(Unit) {
            navController.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
                savedStateHandle.getLiveData<Boolean>("delete_goal_confirmed").observeForever { confirmed ->
                    if (confirmed == true) {
                        viewModel.deleteSavingsGoal(currentGoal)
                        savedStateHandle.remove<Boolean>("delete_goal_confirmed")
                    }
                }
            }
        }
        
        contributionToDelete?.let { contribution ->
            FlatDeleteContributionDialog(
                isDeposit = contribution.isDeposit,
                onConfirm = {
                    viewModel.deleteContribution(contribution)
                    contributionToDelete = null
                },
                onDismiss = { contributionToDelete = null }
            )
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
package com.ccxiaoji.feature.ledger.presentation.screen.savings

import android.util.Log
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
    val TAG = "SavingsGoalScreen"
    val goals by viewModel.activeSavingsGoals.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 调试初始化信息
    LaunchedEffect(Unit) {
        Log.d(TAG, "SavingsGoalScreen初始化")
    }
    
    // 调试储蓄目标数据变化
    LaunchedEffect(goals) {
        Log.d(TAG, "储蓄目标数据更新：${goals.size}个目标")
        goals.forEachIndexed { index, goal ->
            Log.d(TAG, "目标$index: ${goal.name}, 进度: ${goal.progressPercentage}%")
        }
    }
    
    // 调试UI状态变化
    LaunchedEffect(uiState) {
        Log.d(TAG, "UI状态更新 - 加载中: ${uiState.isLoading}")
        if (uiState.message != null) {
            Log.d(TAG, "成功消息: ${uiState.message}")
        }
        if (uiState.error != null) {
            Log.e(TAG, "错误消息: ${uiState.error}")
        }
    }
    
    // 处理消息显示
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            Log.d(TAG, "显示成功消息: $message")
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessage()
            }
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Log.e(TAG, "显示错误消息: $error")
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
                    onClick = {
                        Log.d(TAG, "点击新建目标FAB按钮")
                        onNavigateToAddGoal()
                    },
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
                Log.d(TAG, "显示空状态页面")
                EmptySavingsState(
                    onAddClick = {
                        Log.d(TAG, "点击空状态页面的创建储蓄目标按钮")
                        onNavigateToAddGoal()
                    }
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
                            onClick = { 
                                Log.d(TAG, "点击储蓄目标：${goal.name}, ID: ${goal.id}")
                                onNavigateToDetail(goal.id) 
                            }
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
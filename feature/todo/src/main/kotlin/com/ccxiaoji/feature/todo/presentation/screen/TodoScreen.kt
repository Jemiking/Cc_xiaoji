package com.ccxiaoji.feature.todo.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.todo.R
import com.ccxiaoji.feature.todo.presentation.component.TodoSearchBar
import com.ccxiaoji.feature.todo.presentation.component.TodoFilterBar
import com.ccxiaoji.feature.todo.presentation.component.GroupedTaskList
import com.ccxiaoji.feature.todo.presentation.viewmodel.TodoViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 待办事项主屏幕
 * 使用模块化组件设计，将复杂UI拆分为独立组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = hiltViewModel(),
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToEditTask: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示错误/成功消息
    LaunchedEffect(errorState) {
        errorState?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = if (showTopBar) {
            {
                TopAppBar(
                    title = { 
                        Text(
                            text = stringResource(R.string.todo_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                )
            }
        } else {
            {} // 空的Composable
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = DesignTokens.BrandColors.Todo,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = stringResource(R.string.todo_add_task),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            TodoSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery
            )
            
            // 过滤器栏
            TodoFilterBar(
                filterOptions = uiState.filterOptions,
                onFilterOptionsChange = viewModel::updateFilterOptions
            )
            
            // 任务列表
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = DesignTokens.BrandColors.Todo
                    )
                }
            } else {
                GroupedTaskList(
                    tasks = uiState.tasks,
                    onToggleComplete = { task ->
                        viewModel.toggleTaskCompletion(task.id, !task.completed)
                    },
                    onEditTask = { task ->
                        onNavigateToEditTask(task.id)
                    },
                    onDeleteTask = { task ->
                        viewModel.deleteTask(task.id)
                    },
                    onAddTask = onNavigateToAddTask
                )
            }
        }
    }
}
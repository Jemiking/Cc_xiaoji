package com.ccxiaoji.feature.todo.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.todo.R
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.presentation.component.*
import com.ccxiaoji.feature.todo.presentation.viewmodel.TodoViewModel

/**
 * 待办事项主屏幕
 * 使用模块化组件设计，将复杂UI拆分为独立组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    
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
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.todo_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = stringResource(R.string.todo_add_task)
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
                    CircularProgressIndicator()
                }
            } else {
                TaskList(
                    tasks = uiState.tasks,
                    onToggleComplete = { task ->
                        viewModel.toggleTaskCompletion(task.id, !task.completed)
                    },
                    onEditTask = { task ->
                        editingTask = task
                    },
                    onDeleteTask = { task ->
                        viewModel.deleteTask(task.id)
                    }
                )
            }
        }
    }
    
    // 添加/编辑任务对话框
    if (showAddDialog || editingTask != null) {
        AddTaskDialog(
            onDismiss = { 
                showAddDialog = false 
                editingTask = null
            },
            onConfirm = { title, description, dueAt, priority ->
                if (editingTask != null) {
                    viewModel.updateTask(
                        editingTask!!.id, 
                        title, 
                        description, 
                        dueAt, 
                        priority
                    )
                } else {
                    viewModel.addTask(
                        title, 
                        description, 
                        dueAt, 
                        priority
                    )
                }
                showAddDialog = false
                editingTask = null
            },
            task = editingTask
        )
    }
}
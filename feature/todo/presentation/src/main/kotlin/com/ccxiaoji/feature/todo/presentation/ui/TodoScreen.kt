package com.ccxiaoji.feature.todo.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.presentation.viewmodel.TodoViewModel
import com.ccxiaoji.feature.todo.presentation.viewmodel.TaskFilterOptions
import com.ccxiaoji.feature.todo.presentation.viewmodel.DateFilter
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "待办事项",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("搜索任务...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            
            // Filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show completed toggle
                item {
                    FilterChip(
                        selected = uiState.filterOptions.showCompleted,
                        onClick = {
                            viewModel.updateFilterOptions(
                                uiState.filterOptions.copy(showCompleted = !uiState.filterOptions.showCompleted)
                            )
                        },
                        label = { Text("显示已完成") }
                    )
                }
                
                // Date filters
                item {
                    FilterChip(
                        selected = uiState.filterOptions.dateFilter == DateFilter.TODAY,
                        onClick = {
                            viewModel.updateFilterOptions(
                                uiState.filterOptions.copy(
                                    dateFilter = if (uiState.filterOptions.dateFilter == DateFilter.TODAY) 
                                        DateFilter.ALL else DateFilter.TODAY
                                )
                            )
                        },
                        label = { Text("今天") }
                    )
                }
                
                item {
                    FilterChip(
                        selected = uiState.filterOptions.dateFilter == DateFilter.THIS_WEEK,
                        onClick = {
                            viewModel.updateFilterOptions(
                                uiState.filterOptions.copy(
                                    dateFilter = if (uiState.filterOptions.dateFilter == DateFilter.THIS_WEEK) 
                                        DateFilter.ALL else DateFilter.THIS_WEEK
                                )
                            )
                        },
                        label = { Text("本周") }
                    )
                }
                
                item {
                    FilterChip(
                        selected = uiState.filterOptions.dateFilter == DateFilter.OVERDUE,
                        onClick = {
                            viewModel.updateFilterOptions(
                                uiState.filterOptions.copy(
                                    dateFilter = if (uiState.filterOptions.dateFilter == DateFilter.OVERDUE) 
                                        DateFilter.ALL else DateFilter.OVERDUE
                                )
                            )
                        },
                        label = { Text("逾期") }
                    )
                }
                
                // Priority filters
                item {
                    FilterChip(
                        selected = uiState.filterOptions.selectedPriorities.size < 3,
                        onClick = {
                            viewModel.updateFilterOptions(
                                uiState.filterOptions.copy(
                                    selectedPriorities = if (uiState.filterOptions.selectedPriorities.size < 3)
                                        setOf(0, 1, 2) else setOf()
                                )
                            )
                        },
                        label = { Text("优先级筛选") },
                        leadingIcon = if (uiState.filterOptions.selectedPriorities.size < 3) {
                            { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
            
            if (uiState.tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty() || uiState.filterOptions != TaskFilterOptions()) {
                            "没有找到匹配的任务"
                        } else {
                            "暂无任务"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tasks) { task ->
                        TaskItem(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task.id, !task.completed) },
                            onEdit = { editingTask = task },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog || editingTask != null) {
        AddTaskDialog(
            onDismiss = { 
                showAddDialog = false 
                editingTask = null
            },
            onConfirm = { title, description, dueAt, priority ->
                if (editingTask != null) {
                    viewModel.updateTask(editingTask!!.id, title, description, dueAt, priority)
                } else {
                    viewModel.addTask(title, description, dueAt, priority)
                }
                showAddDialog = false
                editingTask = null
            },
            task = editingTask
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggleComplete() }
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
                
                task.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority chip
                    AssistChip(
                        onClick = { },
                        label = { Text(task.priorityLevel.displayName) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    
                    // Due date
                    task.dueAt?.let { dueAt ->
                        Text(
                            text = dueAt.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                                .toJavaLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑"
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, dueAt: kotlinx.datetime.Instant?, priority: Int) -> Unit,
    task: Task? = null
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: 0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "添加任务" else "编辑任务") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务标题") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Priority selection
                Text(
                    text = "优先级",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = priority == 0,
                        onClick = { priority = 0 },
                        label = { Text("低") }
                    )
                    FilterChip(
                        selected = priority == 1,
                        onClick = { priority = 1 },
                        label = { Text("中") }
                    )
                    FilterChip(
                        selected = priority == 2,
                        onClick = { priority = 2 },
                        label = { Text("高") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title, description.ifEmpty { null }, null, priority)
                },
                enabled = title.isNotEmpty()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
package com.ccxiaoji.feature.todo.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.todo.R
import com.ccxiaoji.feature.todo.domain.model.Priority
import com.ccxiaoji.feature.todo.presentation.viewmodel.AddEditTaskViewModel
import com.ccxiaoji.feature.todo.presentation.component.TaskReminderSettingSection
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: String? = null,
    navController: NavController,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 处理日期选择器返回结果
    navController.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
        androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<Long> { selectedMillis ->
                selectedMillis?.let { millis ->
                    if (millis == 0L) {
                        // 清除日期
                        viewModel.clearDueDate()
                    } else {
                        viewModel.updateDueDate(millis)
                    }
                    savedStateHandle.remove<Long>("selected_date_millis")
                }
            }
            savedStateHandle.getLiveData<Long>("selected_date_millis").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<Long>("selected_date_millis").removeObserver(observer)
            }
        }
    }
    
    LaunchedEffect(taskId) {
        taskId?.let {
            viewModel.loadTask(it)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (taskId == null) 
                            stringResource(R.string.todo_add_task)
                        else 
                            stringResource(R.string.todo_edit_task),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveTask()
                        },
                        enabled = uiState.title.isNotEmpty() && !uiState.isLoading
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 任务标题输入
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text(stringResource(R.string.task_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.BrandColors.Todo,
                        focusedLabelColor = DesignTokens.BrandColors.Todo
                    ),
                    isError = uiState.titleError != null,
                    supportingText = {
                        uiState.titleError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                
                // 任务描述输入
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text(stringResource(R.string.task_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.BrandColors.Todo,
                        focusedLabelColor = DesignTokens.BrandColors.Todo
                    )
                )
                
                // 优先级选择
                Column {
                    Text(
                        text = stringResource(R.string.priority),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    PrioritySelector(
                        selectedPriority = uiState.priority,
                        onPriorityChange = { viewModel.updatePriority(it) }
                    )
                }
                
                // 截止日期选择
                Column {
                    Text(
                        text = stringResource(R.string.due_date),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        // 日期选择按钮
                        OutlinedButton(
                            onClick = { 
                                val initialMillis = uiState.dueAt?.toEpochMilliseconds()
                                navController.navigate(
                                    if (initialMillis != null) {
                                        "todo_date_picker?initialMillis=$initialMillis"
                                    } else {
                                        "todo_date_picker"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DesignTokens.BrandColors.Todo
                            ),
                            border = BorderStroke(
                                1.dp,
                                DesignTokens.BrandColors.Todo.copy(alpha = 0.2f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                            Text(
                                text = uiState.dueAt?.let {
                                    it.toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date.toString()
                                } ?: stringResource(R.string.select_date)
                            )
                        }
                        
                        // 清除按钮
                        if (uiState.dueAt != null) {
                            TextButton(
                                onClick = { viewModel.clearDueDate() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(stringResource(R.string.clear))
                            }
                        }
                    }
                }

                // 提醒设置（Phase 3 - 混合模式）
                if (uiState.dueAt != null) {
                    TaskReminderSettingSection(
                        reminderEnabled = uiState.reminderEnabled,
                        reminderMinutesBefore = uiState.reminderMinutesBefore,
                        reminderTime = uiState.reminderTime,
                        onReminderEnabledChange = { viewModel.updateReminderEnabled(it) },
                        onReminderMinutesChange = { viewModel.updateReminderMinutesBefore(it) },
                        onReminderTimeChange = { viewModel.updateReminderTime(it) }
                    )
                }

                // 保存结果处理
                LaunchedEffect(uiState.isSaved) {
                    if (uiState.isSaved) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("task_updated", true)
                        navController.navigateUp()
                    }
                }
            }
        }
    }
}

/**
 * 优先级选择器组件
 */
@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPriorityChange: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
    ) {
        Priority.values().forEach { priority ->
            val isSelected = selectedPriority == priority
            val backgroundColor = when (priority) {
                Priority.HIGH -> DesignTokens.BrandColors.Error
                Priority.MEDIUM -> DesignTokens.BrandColors.Warning
                Priority.LOW -> DesignTokens.BrandColors.Success
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(DesignTokens.BorderRadius.medium))
                    .background(
                        if (isSelected) backgroundColor.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) backgroundColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
                    )
                    .clickable { onPriorityChange(priority) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = priority.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) backgroundColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
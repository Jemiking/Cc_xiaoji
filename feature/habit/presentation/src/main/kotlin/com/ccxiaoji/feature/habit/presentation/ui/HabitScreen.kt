package com.ccxiaoji.feature.habit.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.feature.habit.presentation.viewmodel.HabitViewModel
import com.ccxiaoji.feature.habit.presentation.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitWithStreak?>(null) }
    var showStatistics by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "习惯",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = { showStatistics = !showStatistics }) {
                        Icon(
                            imageVector = if (showStatistics) Icons.Default.List else Icons.Default.BarChart,
                            contentDescription = if (showStatistics) "显示列表" else "显示统计"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showStatistics) {
                FloatingActionButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加习惯")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showStatistics) {
                // 统计视图
                HabitStatisticsView(
                    habits = uiState.habits,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 列表视图
                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("搜索习惯...") },
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
                
                if (uiState.habits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "没有找到匹配的习惯"
                            } else {
                                "暂无习惯"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.habits) { habitWithStreak ->
                            HabitCard(
                                habitWithStreak = habitWithStreak,
                                isCheckedToday = uiState.checkedToday.contains(habitWithStreak.habit.id),
                                onCheckIn = { viewModel.checkInHabit(habitWithStreak.habit.id) },
                                onEdit = { editingHabit = habitWithStreak },
                                onDelete = { viewModel.deleteHabit(habitWithStreak.habit.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog || editingHabit != null) {
        AddHabitDialog(
            onDismiss = { 
                showAddDialog = false 
                editingHabit = null
            },
            onConfirm = { title, description, period, target ->
                if (editingHabit != null) {
                    viewModel.updateHabit(
                        editingHabit!!.habit.id, 
                        title, 
                        description, 
                        period, 
                        target,
                        editingHabit!!.habit.color,
                        editingHabit!!.habit.icon
                    )
                } else {
                    viewModel.addHabit(title, description, period, target)
                }
                showAddDialog = false
                editingHabit = null
            },
            habitWithStreak = editingHabit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitWithStreak: HabitWithStreak,
    isCheckedToday: Boolean,
    onCheckIn: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = habitWithStreak.habit
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        habit.icon?.let { icon ->
                            Text(
                                text = icon,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    habit.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Streak display
                    if (habitWithStreak.currentStreak > 0) {
                        Text(
                            text = "🔥 连续 ${habitWithStreak.currentStreak} 天",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Check-in button
                    IconButton(
                        onClick = onCheckIn,
                        enabled = !isCheckedToday
                    ) {
                        Icon(
                            imageVector = if (isCheckedToday) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Outlined.CheckCircleOutline
                            },
                            contentDescription = "打卡",
                            tint = if (isCheckedToday) {
                                Color(habit.color.removePrefix("#").toLong(16) or 0xFF000000)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Edit button
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Delete button
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Period info
            Text(
                text = when (habit.period) {
                    "daily" -> "每日"
                    "weekly" -> "每周"
                    "monthly" -> "每月"
                    else -> habit.period
                } + " · 目标 ${habit.target} 次",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, period: String, target: Int) -> Unit,
    habitWithStreak: HabitWithStreak? = null
) {
    val habit = habitWithStreak?.habit
    var title by remember { mutableStateOf(habit?.title ?: "") }
    var description by remember { mutableStateOf(habit?.description ?: "") }
    var period by remember { mutableStateOf(habit?.period ?: "daily") }
    var target by remember { mutableStateOf((habit?.target ?: 1).toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (habit == null) "添加习惯" else "编辑习惯") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("习惯名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Period selection
                Text(
                    text = "周期",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = period == "daily",
                        onClick = { period = "daily" },
                        label = { Text("每日") }
                    )
                    FilterChip(
                        selected = period == "weekly",
                        onClick = { period = "weekly" },
                        label = { Text("每周") }
                    )
                    FilterChip(
                        selected = period == "monthly",
                        onClick = { period = "monthly" },
                        label = { Text("每月") }
                    )
                }
                
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter { char -> char.isDigit() } },
                    label = { Text("目标次数") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        title,
                        description.ifEmpty { null },
                        period,
                        target.toIntOrNull() ?: 1
                    )
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
package com.ccxiaoji.app.presentation.ui.habit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.R
import com.ccxiaoji.app.data.repository.HabitWithStreak
import com.ccxiaoji.app.presentation.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.nav_habit),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_habit))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.habits) { habitWithStreak ->
                HabitCard(
                    habitWithStreak = habitWithStreak,
                    isCheckedToday = uiState.checkedToday.contains(habitWithStreak.habit.id),
                    onCheckIn = { viewModel.checkInHabit(habitWithStreak.habit.id) },
                    onDelete = { viewModel.deleteHabit(habitWithStreak.habit.id) }
                )
            }
        }
    }
    
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, period, target ->
                viewModel.addHabit(title, description, period, target)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitWithStreak: HabitWithStreak,
    isCheckedToday: Boolean,
    onCheckIn: () -> Unit,
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
                            text = "🔥 ${stringResource(R.string.streak_days, habitWithStreak.currentStreak)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
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
                        contentDescription = stringResource(R.string.check_in),
                        tint = if (isCheckedToday) {
                            Color(habit.color.removePrefix("#").toLong(16) or 0xFF000000)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Period info
            Text(
                text = when (habit.period) {
                    "daily" -> stringResource(R.string.period_daily)
                    "weekly" -> stringResource(R.string.period_weekly)
                    "monthly" -> stringResource(R.string.period_monthly)
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
    onConfirm: (title: String, description: String?, period: String, target: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("daily") }
    var target by remember { mutableStateOf("1") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_habit)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.habit_name_hint)) },
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
                    text = stringResource(R.string.habit_period),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = period == "daily",
                        onClick = { period = "daily" },
                        label = { Text(stringResource(R.string.period_daily)) }
                    )
                    FilterChip(
                        selected = period == "weekly",
                        onClick = { period = "weekly" },
                        label = { Text(stringResource(R.string.period_weekly)) }
                    )
                    FilterChip(
                        selected = period == "monthly",
                        onClick = { period = "monthly" },
                        label = { Text(stringResource(R.string.period_monthly)) }
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
package com.ccxiaoji.feature.habit.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import com.ccxiaoji.feature.habit.R
import com.ccxiaoji.feature.habit.presentation.viewmodel.AddEditHabitViewModel
import com.ccxiaoji.feature.habit.presentation.component.HabitReminderSettingSection
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    habitId: String? = null,
    navController: NavController,
    viewModel: AddEditHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    LaunchedEffect(habitId) {
        habitId?.let {
            viewModel.loadHabit(it)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (habitId == null) 
                            stringResource(R.string.add_habit)
                        else 
                            stringResource(R.string.edit_habit),
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
                            viewModel.saveHabit()
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
                    .verticalScroll(scrollState)
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 图标和颜色选择器（水平排列）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 当前选中的图标和颜色预览
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                uiState.availableColors.find { it.first == uiState.selectedColor }?.second?.copy(alpha = 0.1f)
                                    ?: DesignTokens.BrandColors.Success.copy(alpha = 0.1f)
                            )
                            .border(
                                width = 2.dp,
                                color = uiState.availableColors.find { it.first == uiState.selectedColor }?.second
                                    ?: DesignTokens.BrandColors.Success,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.selectedIcon,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    
                    // 习惯名称输入
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text(stringResource(R.string.habit_name_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DesignTokens.BrandColors.Habit,
                            focusedLabelColor = DesignTokens.BrandColors.Habit
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
                }
                
                // 图标选择器
                Column {
                    Text(
                        text = stringResource(R.string.select_icon),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(DesignTokens.BorderRadius.medium))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(DesignTokens.Spacing.small)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                        ) {
                            uiState.availableIcons.take(8).forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (icon == uiState.selectedIcon) {
                                                DesignTokens.BrandColors.Habit.copy(alpha = 0.2f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .clickable { viewModel.updateIcon(icon) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = icon)
                                }
                            }
                            
                            // 随机图标按钮
                            IconButton(
                                onClick = { viewModel.randomizeIcon() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.random_icon),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 颜色选择器
                Column {
                    Text(
                        text = stringResource(R.string.select_color),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        uiState.availableColors.forEach { (colorString, color) ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (colorString == uiState.selectedColor) 2.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.updateColor(colorString) }
                            )
                        }
                    }
                }
                
                // 习惯描述输入
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text(stringResource(R.string.description_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.BrandColors.Habit,
                        focusedLabelColor = DesignTokens.BrandColors.Habit
                    )
                )
                
                // 周期选择
                Column {
                    Text(
                        text = stringResource(R.string.habit_period),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        listOf(
                            "daily" to stringResource(R.string.period_daily),
                            "weekly" to stringResource(R.string.period_weekly),
                            "monthly" to stringResource(R.string.period_monthly)
                        ).forEach { (value, label) ->
                            val isSelected = uiState.period == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(DesignTokens.BorderRadius.medium))
                                    .background(
                                        if (isSelected) DesignTokens.BrandColors.Habit.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) DesignTokens.BrandColors.Habit 
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
                                    )
                                    .clickable { viewModel.updatePeriod(value) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isSelected) DesignTokens.BrandColors.Habit 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 目标次数输入
                OutlinedTextField(
                    value = uiState.target,
                    onValueChange = { viewModel.updateTarget(it) },
                    label = { Text(stringResource(R.string.target_count)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.BrandColors.Habit,
                        focusedLabelColor = DesignTokens.BrandColors.Habit
                    ),
                    isError = uiState.targetError != null,
                    supportingText = {
                        uiState.targetError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // 提醒设置（Phase 3）
                HabitReminderSettingSection(
                    reminderEnabled = uiState.reminderEnabled,
                    reminderTime = uiState.reminderTime,
                    onReminderEnabledChange = { viewModel.updateReminderEnabled(it) },
                    onReminderTimeChange = { viewModel.updateReminderTime(it) }
                )

                // 保存结果处理
                LaunchedEffect(uiState.isSaved) {
                    if (uiState.isSaved) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("habit_updated", true)
                        navController.navigateUp()
                    }
                }
            }
        }
    }
}
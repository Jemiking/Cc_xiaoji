package com.ccxiaoji.feature.habit.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccxiaoji.feature.habit.R
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化设计的习惯对话框
 * 用于添加或编辑习惯
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, period: String, target: Int, color: String, icon: String?) -> Unit,
    habitWithStreak: HabitWithStreak? = null
) {
    val habit = habitWithStreak?.habit
    var title by remember { mutableStateOf(habit?.title ?: "") }
    var description by remember { mutableStateOf(habit?.description ?: "") }
    var period by remember { mutableStateOf(habit?.period ?: "daily") }
    var target by remember { mutableStateOf((habit?.target ?: 1).toString()) }
    var selectedIcon by remember { mutableStateOf(habit?.icon ?: "💪") }
    var selectedColor by remember { mutableStateOf(habit?.color ?: "#4CAF50") }
    
    // 预定义的图标列表
    val availableIcons = listOf(
        "💪", "📚", "🏃", "🧘", "💤", "💧", "🎯", "✍️",
        "🎨", "🎵", "🌱", "🧠", "🏋️", "🚴", "🏊", "🤸",
        "📖", "💻", "🎮", "🍎", "🥗", "🚭", "🙏", "⏰"
    )
    
    // 预定义的颜色列表
    val availableColors = listOf(
        "#4CAF50" to DesignTokens.BrandColors.Success,
        "#2196F3" to DesignTokens.BrandColors.Primary,
        "#FF9800" to DesignTokens.BrandColors.Warning,
        "#F44336" to DesignTokens.BrandColors.Error,
        "#9C27B0" to DesignTokens.BrandColors.Todo,
        "#00BCD4" to DesignTokens.BrandColors.Info,
        "#795548" to DesignTokens.BrandColors.Plan,
        "#607D8B" to DesignTokens.BrandColors.Schedule
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(DesignTokens.BorderRadius.large),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.Spacing.large)
            ) {
                // 标题
                Text(
                    text = if (habit == null) stringResource(R.string.add_habit) else "编辑习惯",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Habit
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
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
                                availableColors.find { it.first == selectedColor }?.second?.copy(alpha = 0.1f)
                                    ?: DesignTokens.BrandColors.Success.copy(alpha = 0.1f)
                            )
                            .border(
                                width = 2.dp,
                                color = availableColors.find { it.first == selectedColor }?.second
                                    ?: DesignTokens.BrandColors.Success,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedIcon,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    
                    // 习惯名称输入
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.habit_name_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DesignTokens.BrandColors.Habit,
                            focusedLabelColor = DesignTokens.BrandColors.Habit
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 图标选择器
                Column {
                    Text(
                        text = "选择图标",
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
                            availableIcons.take(8).forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (icon == selectedIcon) {
                                                DesignTokens.BrandColors.Habit.copy(alpha = 0.2f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .clickable { selectedIcon = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = icon)
                                }
                            }
                            
                            // 随机图标按钮
                            IconButton(
                                onClick = { selectedIcon = availableIcons.random() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "随机图标",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 颜色选择器
                Column {
                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        availableColors.forEach { (colorString, color) ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (colorString == selectedColor) 2.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorString }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 习惯描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.BrandColors.Habit,
                        focusedLabelColor = DesignTokens.BrandColors.Habit
                    )
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
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
                            val isSelected = period == value
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
                                    .clickable { period = value },
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
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 目标次数输入
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter { char -> char.isDigit() } },
                    label = { Text("目标次数") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.BrandColors.Habit,
                        focusedLabelColor = DesignTokens.BrandColors.Habit
                    )
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    
                    // 确认按钮
                    FlatButton(
                        onClick = {
                            onConfirm(
                                title,
                                description.ifEmpty { null },
                                period,
                                target.toIntOrNull() ?: 1,
                                selectedColor,
                                selectedIcon
                            )
                        },
                        enabled = title.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        backgroundColor = DesignTokens.BrandColors.Habit
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}
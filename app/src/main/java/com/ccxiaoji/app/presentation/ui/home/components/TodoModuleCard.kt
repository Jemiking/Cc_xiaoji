package com.ccxiaoji.app.presentation.ui.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

@Composable
fun TodoModuleCard(
    todayTodoCount: Int,
    completedCount: Int,
    onCardClick: () -> Unit,
    onViewTodos: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 计算完成率
    val completionRate = if (todayTodoCount > 0) {
        (completedCount.toFloat() / todayTodoCount * 100).toInt()
    } else 0
    
    // 动画效果
    var animatedCompletion by remember { mutableStateOf(0f) }
    LaunchedEffect(completionRate) {
        animate(
            initialValue = animatedCompletion,
            targetValue = completionRate.toFloat(),
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedCompletion = value
        }
    }
    
    FlatModuleCard(
        title = "待办",
        icon = Icons.Default.TaskAlt,
        moduleColor = DesignTokens.BrandColors.Todo,
        onClick = onCardClick,
        modifier = modifier
    ) {
        // 今日待办统计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.Spacing.small),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 待办总数
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.BrandColors.Todo.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = DesignTokens.BrandColors.Todo,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                Text(
                    text = "今日待办",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$todayTodoCount 项",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.BrandColors.Todo
                )
            }
            
            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            // 已完成
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.BrandColors.Success.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DesignTokens.BrandColors.Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                Text(
                    text = "已完成",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$completedCount 项",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.BrandColors.Success
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        // 完成进度
        if (todayTodoCount > 0) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "完成进度",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${animatedCompletion.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignTokens.BrandColors.Success
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                // 圆形进度条效果
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((animatedCompletion / 100f))
                            .background(
                                brush = DesignTokens.BrandGradients.Success,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        }
        
        // 快速操作按钮
        FlatButton(
            onClick = onViewTodos,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DesignTokens.BrandColors.Todo,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.FormatListBulleted,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
            Text(
                text = "查看待办",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
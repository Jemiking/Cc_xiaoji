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
fun PlanModuleCard(
    activePlansCount: Int,
    todayPlansCount: Int,
    averageProgress: Int,
    onCardClick: () -> Unit,
    onViewPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 动画效果
    var animatedProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(averageProgress) {
        animate(
            initialValue = animatedProgress,
            targetValue = averageProgress.toFloat(),
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedProgress = value
        }
    }
    
    FlatModuleCard(
        title = "计划",
        icon = Icons.Default.Assignment,
        moduleColor = DesignTokens.BrandColors.Plan,
        onClick = onCardClick,
        modifier = modifier
    ) {
        // 计划统计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.Spacing.small),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 进行中计划
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.BrandColors.Plan.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = DesignTokens.BrandColors.Plan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "进行中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$activePlansCount 个",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DesignTokens.BrandColors.Plan
                    )
                }
            }
            
            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            // 今日相关
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.BrandColors.Info.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        tint = DesignTokens.BrandColors.Info,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "今日",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$todayPlansCount 个",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DesignTokens.BrandColors.Info
                    )
                }
            }
        }
        
        // 平均进度
        if (activePlansCount > 0) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
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
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "平均进度",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${animatedProgress.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignTokens.BrandColors.Plan
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                // 进度条
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
                            .fillMaxWidth((animatedProgress / 100f).coerceIn(0f, 1f))
                            .background(
                                brush = DesignTokens.BrandGradients.ModulePlan,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        // 快速操作按钮
        FlatButton(
            onClick = onViewPlans,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DesignTokens.BrandColors.Plan,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.ViewList,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
            Text(
                text = "查看计划",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
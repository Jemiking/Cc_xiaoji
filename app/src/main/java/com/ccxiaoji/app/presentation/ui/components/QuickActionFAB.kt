package com.ccxiaoji.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.app.presentation.ui.navigation.*

@Composable
fun QuickActionFAB(
    navController: NavController
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd  // 改为右下角对齐
    ) {
        // 背景遮罩
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isExpanded = false }
            )
        }
        
        // FAB和快捷操作项
        Column(
            horizontalAlignment = Alignment.End,  // 改为右对齐
            modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)  // 调整边距
        ) {
            // 快捷操作列表
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200)
                ) + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    QuickActionItem(
                        icon = Icons.Default.Add,
                        label = "记一笔",
                        backgroundColor = Color(0xFF66BB6A), // 使用项目标准柔和绿色
                        onClick = {
                            navController.navigate(AddTransactionRoute.createRoute())
                            isExpanded = false
                        }
                    )
                    
                    QuickActionItem(
                        icon = Icons.Default.Task,
                        label = "新待办",
                        backgroundColor = Color(0xFF5E7CE0), // 使用项目标准柔和蓝色
                        onClick = {
                            navController.navigate(AddEditTaskRoute.createRoute())
                            isExpanded = false
                        }
                    )
                    
                    QuickActionItem(
                        icon = Icons.Default.CheckCircle,
                        label = "习惯打卡",
                        backgroundColor = Color(0xFFAB47BC), // 使用项目标准柔和紫色
                        onClick = {
                            navController.navigate(Screen.Habit.route)
                            isExpanded = false
                        }
                    )
                    
                    QuickActionItem(
                        icon = Icons.Default.Timeline,
                        label = "新计划",
                        backgroundColor = Color(0xFF8D6E63), // 使用项目标准柔和棕色
                        onClick = {
                            // TODO: 导航到创建计划
                            navController.navigate(PlanRoute.route)
                            isExpanded = false
                        }
                    )
                }
            }
            
            // 主FAB按钮
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp,
                    hoveredElevation = 10.dp,
                    focusedElevation = 10.dp
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isExpanded) "关闭" else "快捷操作",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // 标签
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 小FAB
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = backgroundColor,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
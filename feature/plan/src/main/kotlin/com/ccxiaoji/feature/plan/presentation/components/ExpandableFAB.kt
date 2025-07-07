package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 可展开的FAB组件 - 扁平化设计
 * 包含背景遮罩和动画效果
 */
@Composable
fun ExpandableFAB(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCreateNewPlan: () -> Unit,
    onCreateFromTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // 背景遮罩
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onExpandedChange(false)
                    }
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // 子菜单项
            AnimatedVisibility(
                visible = expanded,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200)
                ) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // 从模板创建
                    ExtendedFloatingActionButton(
                        onClick = {
                            onExpandedChange(false)
                            onCreateFromTemplate()
                        },
                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.small),
                        containerColor = DesignTokens.BrandColors.Plan.copy(alpha = 0.9f),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 1.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "从模板创建",
                            modifier = Modifier.padding(end = DesignTokens.Spacing.small)
                        )
                        Text("从模板创建")
                    }
                    
                    // 创建新计划
                    ExtendedFloatingActionButton(
                        onClick = {
                            onExpandedChange(false)
                            onCreateNewPlan()
                        },
                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.medium),
                        containerColor = DesignTokens.BrandColors.Plan.copy(alpha = 0.9f),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 1.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "创建新计划",
                            modifier = Modifier.padding(end = DesignTokens.Spacing.small)
                        )
                        Text("创建新计划")
                    }
                }
            }
            
            // 主FAB按钮
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 45f else 0f,
                animationSpec = tween(200),
                label = "fab_rotation"
            )
            
            FloatingActionButton(
                onClick = { onExpandedChange(!expanded) },
                containerColor = DesignTokens.BrandColors.Plan,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (expanded) "关闭" else "创建选项",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}
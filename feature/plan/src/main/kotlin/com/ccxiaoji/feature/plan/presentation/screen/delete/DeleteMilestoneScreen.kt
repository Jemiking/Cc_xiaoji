package com.ccxiaoji.feature.plan.presentation.screen.delete

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.feature.plan.presentation.viewmodel.DeleteMilestoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteMilestoneScreen(
    planId: String,
    milestoneId: String,
    navController: NavController,
    viewModel: DeleteMilestoneViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(milestoneId) {
        viewModel.loadMilestone(milestoneId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("确认删除") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 警告图标
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = DesignTokens.BrandColors.Error
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            
            // 标题
            Text(
                text = "删除里程碑",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 里程碑信息
            uiState.milestone?.let { milestone: com.ccxiaoji.feature.plan.domain.model.Milestone ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.Spacing.medium)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (milestone.isCompleted) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = DesignTokens.BrandColors.Success.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text(
                                        text = "已完成",
                                        modifier = Modifier.padding(
                                            horizontal = DesignTokens.Spacing.small,
                                            vertical = DesignTokens.Spacing.xs
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DesignTokens.BrandColors.Success
                                    )
                                }
                            }
                        }
                        
                        if (!milestone.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                            Text(
                                text = milestone.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        milestone.targetDate?.let { date: kotlinx.datetime.LocalDate ->
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                            Text(
                                text = "目标日期：$date",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            
            // 警告信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "此操作无法撤销，里程碑将被永久删除。",
                    modifier = Modifier.padding(DesignTokens.Spacing.medium),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                FlatButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text("取消")
                }
                
                FlatButton(
                    onClick = { 
                        viewModel.deleteMilestone()
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("milestone_deleted", true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = DesignTokens.BrandColors.Error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text("删除")
                }
            }
        }
    }
}
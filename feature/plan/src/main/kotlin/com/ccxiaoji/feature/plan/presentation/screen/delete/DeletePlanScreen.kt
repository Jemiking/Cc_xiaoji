package com.ccxiaoji.feature.plan.presentation.screen.delete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.feature.plan.presentation.viewmodel.DeletePlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletePlanScreen(
    planId: String,
    navController: NavController,
    viewModel: DeletePlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
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
                text = "删除计划",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 计划信息
            uiState.plan?.let { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.Spacing.medium)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 计划颜色标识
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = Color(android.graphics.Color.parseColor(plan.color)),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                            
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        if (!plan.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                            Text(
                                text = plan.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (plan.hasChildren) {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = "⚠️ 此计划包含 ${plan.children.size} 个子计划",
                                    modifier = Modifier.padding(DesignTokens.Spacing.small),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
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
                    text = if (uiState.plan?.hasChildren == true) {
                        "此计划包含子计划，删除后所有子计划也将被删除。此操作无法撤销。"
                    } else {
                        "删除后计划及其所有相关数据将永久丢失，此操作无法撤销。"
                    },
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
                        viewModel.deletePlan()
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("plan_deleted", true)
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
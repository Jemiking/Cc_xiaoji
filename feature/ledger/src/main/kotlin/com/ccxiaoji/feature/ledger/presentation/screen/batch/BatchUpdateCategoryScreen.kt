package com.ccxiaoji.feature.ledger.presentation.screen.batch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.BatchUpdateCategoryViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 批量修改分类页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchUpdateCategoryScreen(
    navController: NavController,
    viewModel: BatchUpdateCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批量修改分类") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.selectedCategoryId?.let { categoryId ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selected_category_id", categoryId)
                                navController.popBackStack()
                            }
                        },
                        enabled = viewModel.selectedCategoryId != null
                    ) {
                        Text("确定")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 顶部提示信息
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.medium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                ) {
                    Text(
                        text = "批量修改提示",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                    Text(
                        text = "将为 ${uiState.selectedCount} 笔交易修改分类",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 分类选择列表
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = DesignTokens.Spacing.medium)
                ) {
                    // 支出分类
                    if (uiState.expenseCategories.isNotEmpty()) {
                        item {
                            Text(
                                text = "支出分类",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(
                                    horizontal = DesignTokens.Spacing.medium,
                                    vertical = DesignTokens.Spacing.small
                                )
                            )
                        }
                        
                        items(uiState.expenseCategories) { category ->
                            ListItem(
                                headlineContent = { 
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    ) 
                                },
                                leadingContent = {
                                    // 分类图标
                                    if (category.icon != null) {
                                        Text(
                                            text = category.icon,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = viewModel.selectedCategoryId == category.id,
                                        onClick = { viewModel.selectCategory(category.id) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                },
                                modifier = Modifier.clickable {
                                    viewModel.selectCategory(category.id)
                                }
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.medium),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        }
                    }
                    
                    // 收入分类
                    if (uiState.incomeCategories.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                            Text(
                                text = "收入分类",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(
                                    horizontal = DesignTokens.Spacing.medium,
                                    vertical = DesignTokens.Spacing.small
                                )
                            )
                        }
                        
                        items(uiState.incomeCategories) { category ->
                            ListItem(
                                headlineContent = { 
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    ) 
                                },
                                leadingContent = {
                                    // 分类图标
                                    if (category.icon != null) {
                                        Text(
                                            text = category.icon,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = viewModel.selectedCategoryId == category.id,
                                        onClick = { viewModel.selectCategory(category.id) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                },
                                modifier = Modifier.clickable {
                                    viewModel.selectCategory(category.id)
                                }
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.medium),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
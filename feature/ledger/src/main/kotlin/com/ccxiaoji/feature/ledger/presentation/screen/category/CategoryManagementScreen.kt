package com.ccxiaoji.feature.ledger.presentation.screen.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CategoryTab
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CategoryViewModel
import com.ccxiaoji.feature.ledger.presentation.screen.category.components.*
import com.ccxiaoji.ui.components.FlatFAB
import com.ccxiaoji.ui.components.FlatAlertDialog
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 处理导航返回结果
    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.let { savedStateHandle ->
                savedStateHandle.get<Boolean>("category_added")?.let { added ->
                    if (added) {
                        savedStateHandle.remove<Boolean>("category_added")
                        // 刷新分类列表
                        viewModel.loadCategories()
                    }
                }
                savedStateHandle.get<Boolean>("category_updated")?.let { updated ->
                    if (updated) {
                        savedStateHandle.remove<Boolean>("category_updated")
                        // 刷新分类列表
                        viewModel.loadCategories()
                    }
                }
            }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "分类管理",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FlatFAB(
                onClick = { 
                    val categoryType = if (uiState.selectedTab == CategoryTab.EXPENSE) {
                        "EXPENSE"
                    } else {
                        "INCOME"
                    }
                    navController.navigate(LedgerNavigation.addCategoryRoute(categoryType))
                },
                containerColor = DesignTokens.BrandColors.Ledger
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加分类")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab选择器
            CategoryTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::setSelectedTab
            )
            
            // 分类列表
            val categories = if (uiState.selectedTab == CategoryTab.EXPENSE) {
                uiState.expenseCategories
            } else {
                uiState.incomeCategories
            }
            
            if (categories.isEmpty()) {
                EmptyCategoryState(
                    message = if (uiState.selectedTab == CategoryTab.EXPENSE) {
                        "暂无支出分类"
                    } else {
                        "暂无收入分类"
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    items(categories.size) { index ->
                        val categoryWithStats = categories[index]
                        CategoryItem(
                            categoryWithStats = categoryWithStats,
                            onEdit = { 
                                navController.navigate(
                                    LedgerNavigation.editCategoryRoute(categoryWithStats.category.id)
                                )
                            },
                            onDelete = { viewModel.deleteCategory(categoryWithStats.category.id) }
                        )
                    }
                    
                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xxl))
                    }
                }
            }
        }
    }
    
    // 添加分类对话框已改为全屏页面
    // 原对话框功能已通过导航到AddCategoryScreen实现
    
    // 编辑分类对话框已改为全屏页面
    // 原对话框功能已通过导航到EditCategoryScreen实现
    
    // 错误提示对话框
    uiState.errorMessage?.let { message ->
        FlatAlertDialog(
            onDismissRequest = { viewModel.clearError() },
            onConfirmation = { viewModel.clearError() },
            dialogTitle = "提示",
            dialogText = message
        )
    }
}
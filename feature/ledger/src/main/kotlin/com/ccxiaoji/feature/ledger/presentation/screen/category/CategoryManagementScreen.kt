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
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CategoryManagementViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.DialogMode
import com.ccxiaoji.feature.ledger.presentation.screen.category.components.*
import com.ccxiaoji.ui.components.FlatFAB
import com.ccxiaoji.ui.components.FlatAlertDialog
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    navController: NavController,
    viewModel: CategoryManagementViewModel = hiltViewModel()
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
                        Category.Type.EXPENSE
                    } else {
                        Category.Type.INCOME
                    }
                    viewModel.showAddParentDialog(categoryType)
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
                onTabSelected = viewModel::selectTab
            )
            
            // 分类列表（使用分类树结构）
            val categoryGroups = if (uiState.selectedTab == CategoryTab.EXPENSE) {
                uiState.expenseGroups
            } else {
                uiState.incomeGroups
            }
            
            if (categoryGroups.isEmpty()) {
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
                    // 遍历分类组（父分类及其子分类）
                    categoryGroups.forEach { group ->
                        // 显示父分类
                        item(key = group.parent.id) {
                            CategoryGroupItem(
                                categoryGroup = group,
                                isExpanded = uiState.expandedGroups[group.parent.id] ?: true,
                                onToggleExpand = { viewModel.toggleGroupExpansion(group.parent.id) },
                                onEditParent = { 
                                    viewModel.showEditDialog(group.parent)
                                },
                                onDeleteParent = { 
                                    viewModel.deleteCategory(group.parent.id) 
                                },
                                onAddChild = {
                                    viewModel.showAddChildDialog(group)
                                },
                                onEditChild = { child ->
                                    viewModel.showEditDialog(child, group.parent.name)
                                },
                                onDeleteChild = { childId ->
                                    viewModel.deleteCategory(childId)
                                }
                            )
                        }
                    }
                    
                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xxl))
                    }
                }
            }
        }
    }
    
    // 分类编辑对话框
    CategoryEditDialog(
        isVisible = uiState.showAddDialog,
        title = when (uiState.dialogMode) {
            DialogMode.ADD_PARENT -> "添加父分类"
            DialogMode.ADD_CHILD -> "添加子分类" 
            DialogMode.EDIT_PARENT -> "编辑父分类"
            DialogMode.EDIT_CHILD -> "编辑子分类"
        },
        categoryName = uiState.dialogName,
        categoryIcon = uiState.dialogIcon.ifEmpty { "📝" },
        categoryColor = if (uiState.dialogMode == DialogMode.ADD_PARENT || 
                           uiState.dialogMode == DialogMode.EDIT_PARENT) {
            uiState.dialogColor.ifEmpty { "#6200EE" }
        } else {
            uiState.dialogColor.ifEmpty { null }
        },
        parentName = uiState.dialogParentName,
        onNameChange = { viewModel.updateDialogInput(name = it) },
        onIconChange = { viewModel.updateDialogInput(icon = it) },
        onColorChange = { viewModel.updateDialogInput(color = it) },
        onConfirm = { viewModel.saveCategory() },
        onDismiss = { viewModel.closeDialog() },
        error = uiState.dialogError
    )
    
    // 错误提示对话框
    uiState.error?.let { message ->
        FlatAlertDialog(
            onDismissRequest = { viewModel.clearError() },
            onConfirmation = { viewModel.clearError() },
            dialogTitle = "提示",
            dialogText = message
        )
    }
}
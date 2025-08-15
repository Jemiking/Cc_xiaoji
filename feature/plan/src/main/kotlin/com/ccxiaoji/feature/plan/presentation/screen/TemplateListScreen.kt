package com.ccxiaoji.feature.plan.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.presentation.viewmodel.TemplateListViewModel
import com.ccxiaoji.feature.plan.presentation.screen.template.components.*
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 模板列表页面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlanDetail: (String) -> Unit,
    onNavigateToTemplateDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TemplateListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示错误信息
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    // 处理应用成功后的导航
    LaunchedEffect(uiState.appliedPlanId) {
        uiState.appliedPlanId?.let { planId ->
            onNavigateToPlanDetail(planId)
            viewModel.clearAppliedPlanId()
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("模板中心") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 分类Tab
            CategoryTabs(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory,
                modifier = Modifier.padding(
                    horizontal = DesignTokens.Spacing.medium,
                    vertical = DesignTokens.Spacing.small
                )
            )
            
            // 搜索栏
            TemplateSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::searchTemplates,
                modifier = Modifier.padding(
                    horizontal = DesignTokens.Spacing.medium,
                    vertical = DesignTokens.Spacing.small
                )
            )
            
            // 模板列表
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.filteredTemplates.isEmpty() -> {
                        EmptyTemplateState(
                            isSearching = uiState.searchQuery.isNotEmpty()
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = DesignTokens.Spacing.medium,
                                vertical = DesignTokens.Spacing.small
                            ),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                        ) {
                            items(
                                items = uiState.filteredTemplates,
                                key = { it.id }
                            ) { template ->
                                TemplateListItem(
                                    template = template,
                                    onClick = { onNavigateToTemplateDetail(template.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


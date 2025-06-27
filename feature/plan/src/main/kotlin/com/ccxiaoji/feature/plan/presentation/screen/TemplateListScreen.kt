package com.ccxiaoji.feature.plan.presentation.template

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory

/**
 * 模板列表页面
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
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
                onCategorySelected = viewModel::selectCategory
            )
            
            // 搜索栏
            SearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::searchTemplates,
                modifier = Modifier.padding(16.dp)
            )
            
            // 模板列表
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator()
                    }
                    uiState.filteredTemplates.isEmpty() -> {
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) {
                                "未找到匹配的模板"
                            } else {
                                "暂无模板"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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

/**
 * 分类Tab组件
 */
@Composable
private fun CategoryTabs(
    selectedCategory: TemplateCategory?,
    onCategorySelected: (TemplateCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = when (selectedCategory) {
            null -> 0
            TemplateCategory.WORK -> 1
            TemplateCategory.STUDY -> 2
            TemplateCategory.FITNESS -> 3
            TemplateCategory.LIFE -> 4
            TemplateCategory.HEALTH -> 5
            TemplateCategory.SKILL -> 6
            TemplateCategory.PROJECT -> 7
            TemplateCategory.OTHER -> 8
        },
        modifier = modifier,
        edgePadding = 16.dp
    ) {
        Tab(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            text = { Text("全部") }
        )
        Tab(
            selected = selectedCategory == TemplateCategory.WORK,
            onClick = { onCategorySelected(TemplateCategory.WORK) },
            text = { Text("工作") }
        )
        Tab(
            selected = selectedCategory == TemplateCategory.STUDY,
            onClick = { onCategorySelected(TemplateCategory.STUDY) },
            text = { Text("学习") }
        )
        Tab(
            selected = selectedCategory == TemplateCategory.FITNESS,
            onClick = { onCategorySelected(TemplateCategory.FITNESS) },
            text = { Text("健身") }
        )
        Tab(
            selected = selectedCategory == TemplateCategory.LIFE,
            onClick = { onCategorySelected(TemplateCategory.LIFE) },
            text = { Text("生活") }
        )
        Tab(
            selected = selectedCategory == TemplateCategory.HEALTH,
            onClick = { onCategorySelected(TemplateCategory.HEALTH) },
            text = { Text("健康") }
        )
    }
}

/**
 * 搜索栏组件
 */
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("搜索模板...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索"
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * 模板列表项组件
 */
@Composable
private fun TemplateListItem(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题和图标
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getCategoryIcon(template.category),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 描述
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // 标签
            if (template.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    template.tags.take(3).forEach { tag ->
                        TagChip(tag = tag)
                    }
                    if (template.tags.size > 3) {
                        Text(
                            text = "+${template.tags.size - 3}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 评分和使用次数
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 评分
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "评分",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format("%.1f", template.rating),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // 使用次数
                Text(
                    text = "使用 ${template.useCount} 次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 标签Chip组件
 */
@Composable
private fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * 获取分类图标
 */
private fun getCategoryIcon(category: TemplateCategory): String {
    return when (category) {
        TemplateCategory.WORK -> "📋"
        TemplateCategory.STUDY -> "📚"
        TemplateCategory.FITNESS -> "💪"
        TemplateCategory.LIFE -> "🎯"
        TemplateCategory.HEALTH -> "🏥"
        TemplateCategory.SKILL -> "🎨"
        TemplateCategory.PROJECT -> "🚀"
        TemplateCategory.OTHER -> "✨"
    }
}
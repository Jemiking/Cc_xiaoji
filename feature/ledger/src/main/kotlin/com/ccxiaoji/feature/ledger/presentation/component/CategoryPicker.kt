package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 分类选择器组件
 * 支持二级分类的选择，以树形结构展示
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPicker(
    isVisible: Boolean,
    categoryGroups: List<CategoryGroup>,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    onDismiss: () -> Unit,
    title: String = "选择分类"
) {
    // 获取图标显示模式
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.large),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column {
                    // 标题栏
                    TopAppBar(
                        title = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "关闭"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    // 搜索栏
                    var searchQuery by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索分类...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "搜索"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "清除"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DesignTokens.Spacing.medium)
                            .padding(bottom = DesignTokens.Spacing.small),
                        singleLine = true,
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
                    )
                    
                    HorizontalDivider()
                    
                    // 分类列表
                    val filteredGroups = if (searchQuery.isEmpty()) {
                        categoryGroups
                    } else {
                        categoryGroups.mapNotNull { group ->
                            val matchingChildren = group.children.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }
                            when {
                                group.parent.name.contains(searchQuery, ignoreCase = true) -> group
                                matchingChildren.isNotEmpty() -> group.copy(children = matchingChildren)
                                else -> null
                            }
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = DesignTokens.Spacing.medium,
                            vertical = DesignTokens.Spacing.small
                        ),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        items(filteredGroups) { group ->
                            CategoryGroupPickerItem(
                                categoryGroup = group,
                                selectedCategoryId = selectedCategoryId,
                                onCategorySelected = onCategorySelected,
                                searchQuery = searchQuery,
                                iconDisplayMode = uiPreferences.iconDisplayMode
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 分类组选择项
 * 显示一个父分类及其子分类，支持展开/折叠
 */
@Composable
private fun CategoryGroupPickerItem(
    categoryGroup: CategoryGroup,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    searchQuery: String = "",
    iconDisplayMode: IconDisplayMode
) {
    var isExpanded by remember(categoryGroup.parent.id, searchQuery) { 
        mutableStateOf(searchQuery.isNotEmpty() || categoryGroup.children.any { it.id == selectedCategoryId })
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // 父分类行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (categoryGroup.children.isEmpty()) {
                            // 如果没有子分类，直接选择父分类
                            onCategorySelected(categoryGroup.parent)
                        } else {
                            // 有子分类则展开/折叠
                            isExpanded = !isExpanded
                        }
                    }
                    .padding(DesignTokens.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 展开/折叠图标（仅在有子分类时显示）
                if (categoryGroup.children.isNotEmpty()) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "折叠" else "展开",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                }
                
                // 分类图标
                DynamicCategoryIcon(
                    category = categoryGroup.parent,
                    iconDisplayMode = iconDisplayMode,
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                
                // 分类名称
                Text(
                    text = categoryGroup.parent.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (categoryGroup.parent.id == selectedCategoryId) FontWeight.Bold else FontWeight.Normal,
                    color = if (categoryGroup.parent.id == selectedCategoryId) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // 子分类数量标签
                if (categoryGroup.children.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${categoryGroup.children.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // 选中标记（仅在没有子分类时显示）
                if (categoryGroup.children.isEmpty() && categoryGroup.parent.id == selectedCategoryId) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "已选中",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // 子分类列表
            AnimatedVisibility(
                visible = isExpanded && categoryGroup.children.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = DesignTokens.Spacing.large)
                ) {
                    categoryGroup.children.forEach { child ->
                        CategoryChildPickerItem(
                            category = child,
                            parentName = categoryGroup.parent.name,
                            isSelected = child.id == selectedCategoryId,
                            onCategorySelected = onCategorySelected,
                            iconDisplayMode = iconDisplayMode
                        )
                    }
                }
            }
        }
    }
}

/**
 * 子分类选择项
 */
@Composable
private fun CategoryChildPickerItem(
    category: Category,
    parentName: String,
    isSelected: Boolean,
    onCategorySelected: (Category) -> Unit,
    iconDisplayMode: IconDisplayMode
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategorySelected(category) }
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 连接线
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        
        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
        
        // 分类图标
        DynamicCategoryIcon(
            category = category,
            iconDisplayMode = iconDisplayMode,
            size = 16.dp,
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
        
        // 分类名称
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            // 显示完整路径
            Text(
                text = "$parentName / ${category.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 选中标记
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "已选中",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
package com.ccxiaoji.feature.ledger.presentation.screen.category.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 分类组项目组件
 * 显示一个父分类及其所有子分类
 */
@Composable
fun CategoryGroupItem(
    categoryGroup: CategoryGroup,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditParent: () -> Unit,
    onDeleteParent: () -> Unit,
    onAddChild: () -> Unit,
    onEditChild: (Category) -> Unit,
    onDeleteChild: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 获取图标显示模式状态
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    // 分类组显示逻辑
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column {
            // 父分类行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(DesignTokens.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 展开/折叠图标
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "折叠" else "展开",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                
                // 分类图标
                DynamicCategoryIcon(
                    category = categoryGroup.parent,
                    iconDisplayMode = uiPreferences.iconDisplayMode,
                    size = 24.dp,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                
                // 分类名称
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = categoryGroup.parent.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${categoryGroup.children.size} 个子分类",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 操作按钮
                Row {
                    // 添加子分类
                    IconButton(
                        onClick = { 
                            onAddChild()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加子分类",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // 编辑父分类
                    IconButton(
                        onClick = { 
                            onEditParent()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 删除父分类（如果没有子分类且不是系统分类）
                    if (categoryGroup.children.isEmpty() && !categoryGroup.parent.isSystem) {
                        IconButton(
                            onClick = { 
                                onDeleteParent()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // 子分类列表（展开时显示）
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider()
                    
                    if (categoryGroup.children.isEmpty()) {
                        // 没有子分类时的提示
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无子分类，点击 + 添加",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // 显示子分类
                        categoryGroup.children.forEach { child ->
                            CategoryChildItem(
                                category = child,
                                iconDisplayMode = uiPreferences.iconDisplayMode,
                                onEdit = { onEditChild(child) },
                                onDelete = { onDeleteChild(child.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 子分类项目组件
 */
@Composable
private fun CategoryChildItem(
    category: Category,
    iconDisplayMode: IconDisplayMode,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = DesignTokens.Spacing.xl,
                end = DesignTokens.Spacing.medium,
                top = DesignTokens.Spacing.small,
                bottom = DesignTokens.Spacing.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类图标
        DynamicCategoryIcon(
            category = category,
            iconDisplayMode = iconDisplayMode,
            size = 20.dp,
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
        
        // 分类名称
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        // 操作按钮
        Row {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!category.isSystem) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
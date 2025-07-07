package com.ccxiaoji.feature.plan.presentation.screen.template.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 分类Tab组件 - 扁平化设计
 */
@Composable
fun CategoryTabs(
    selectedCategory: TemplateCategory?,
    onCategorySelected: (TemplateCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
    ) {
        ScrollableTabRow(
            selectedTabIndex = getCategoryIndex(selectedCategory),
            edgePadding = DesignTokens.Spacing.medium,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.0f),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[getCategoryIndex(selectedCategory)]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                text = { 
                    Text(
                        "全部",
                        color = if (selectedCategory == null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ) 
                }
            )
            TemplateCategory.values().forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    text = { 
                        Text(
                            getCategoryName(category),
                            color = if (selectedCategory == category) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ) 
                    }
                )
            }
        }
    }
}

/**
 * 获取分类索引
 */
private fun getCategoryIndex(category: TemplateCategory?): Int {
    return when (category) {
        null -> 0
        else -> TemplateCategory.values().indexOf(category) + 1
    }
}

/**
 * 获取分类名称
 */
@Composable
private fun getCategoryName(category: TemplateCategory) = when (category) {
    TemplateCategory.WORK -> "工作"
    TemplateCategory.STUDY -> "学习"
    TemplateCategory.FITNESS -> "健身"
    TemplateCategory.LIFE -> "生活"
    TemplateCategory.HEALTH -> "健康"
    TemplateCategory.SKILL -> "技能"
    TemplateCategory.PROJECT -> "项目"
    TemplateCategory.OTHER -> "其他"
}
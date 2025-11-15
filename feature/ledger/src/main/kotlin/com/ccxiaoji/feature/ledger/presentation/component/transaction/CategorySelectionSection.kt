package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo

/**
 * 分类选择区域组件
 *
 * 展示当前选中的分类信息，支持父子分类层级显示，点击后触发分类选择流程。
 *
 * @param selectedCategoryInfo 当前选中的分类信息，null表示未选择
 * @param onCategoryClick 点击分类卡片的回调函数
 * @param modifier 修饰符，用于自定义组件布局
 * @param placeholderText 未选择分类时显示的占位文本
 * @param trailingIcon 卡片右侧显示的图标
 *
 * @sample CategorySelectionSectionPreview
 */
@Composable
fun CategorySelectionSection(
    selectedCategoryInfo: SelectedCategoryInfo?,
    onCategoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "选择分类",
    trailingIcon: ImageVector = Icons.Default.ArrowForward
) {
    Card(
        onClick = onCategoryClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = buildString {
                    if (selectedCategoryInfo == null) {
                        append(placeholderText)
                    } else {
                        // 优先显示全路径，如果没有则根据父分类组合
                        val displayPath = selectedCategoryInfo.fullPath
                            ?: if (selectedCategoryInfo.parentName != null) {
                                "${selectedCategoryInfo.parentName} / ${selectedCategoryInfo.categoryName}"
                            } else {
                                selectedCategoryInfo.categoryName
                            }
                        append(displayPath)
                    }
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "No Category Selected")
@Composable
private fun CategorySelectionSectionEmptyPreview() {
    CategorySelectionSection(
        selectedCategoryInfo = null,
        onCategoryClick = {}
    )
}

@Preview(showBackground = true, name = "Single Category")
@Composable
private fun CategorySelectionSectionSinglePreview() {
    val category = SelectedCategoryInfo(
        categoryId = "food",
        categoryName = "餐饮",
        parentId = null,
        parentName = null,
        fullPath = "餐饮",
        icon = "restaurant",
        color = "#FF5722"
    )
    CategorySelectionSection(
        selectedCategoryInfo = category,
        onCategoryClick = {}
    )
}

@Preview(showBackground = true, name = "Nested Category")
@Composable
private fun CategorySelectionSectionNestedPreview() {
    val category = SelectedCategoryInfo(
        categoryId = "breakfast",
        categoryName = "早餐",
        parentId = "food",
        parentName = "餐饮",
        fullPath = "餐饮 / 早餐",
        icon = "breakfast_dining",
        color = "#FF9800"
    )
    CategorySelectionSection(
        selectedCategoryInfo = category,
        onCategoryClick = {}
    )
}

@Preview(showBackground = true, name = "Custom Placeholder")
@Composable
private fun CategorySelectionSectionCustomPreview() {
    CategorySelectionSection(
        selectedCategoryInfo = null,
        onCategoryClick = {},
        placeholderText = "请选择交易分类"
    )
}
package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 分类网格选择器
 *
 * 显示分类图标网格，支持选择和自定义显示模式
 *
 * @param categories 分类列表
 * @param selectedCategoryId 当前选中的分类ID
 * @param onCategorySelect 分类选择回调
 * @param modifier 修饰符
 * @param columns 网格列数
 * @param iconDisplayMode 图标显示模式
 * @param showCategoryName 是否显示分类名称
 * @param enabled 是否启用选择
 */
@Composable
fun CategoryGridPicker(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelect: (Category) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 6,
    iconDisplayMode: IconDisplayMode = IconDisplayMode.EMOJI,
    showCategoryName: Boolean = true,
    enabled: Boolean = true
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = categories,
            key = { it.id }
        ) { category ->
            CategoryGridItem(
                category = category,
                isSelected = category.id == selectedCategoryId,
                onClick = {
                    if (enabled) {
                        onCategorySelect(category)
                    }
                },
                iconDisplayMode = iconDisplayMode,
                showName = showCategoryName,
                enabled = enabled
            )
        }
    }
}

/**
 * 分类网格项
 */
@Composable
private fun CategoryGridItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconDisplayMode: IconDisplayMode,
    showName: Boolean,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> DesignTokens.BrandColors.Ledger.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 图标显示
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            when (iconDisplayMode) {
                IconDisplayMode.EMOJI -> {
                    Text(
                        text = category.icon,
                        fontSize = 24.sp
                    )
                }
                IconDisplayMode.MATERIAL -> {
                    DynamicCategoryIcon(
                        category = category,
                        iconDisplayMode = IconDisplayMode.MATERIAL,
                        size = 28.dp
                    )
                }
            }
        }

        // 分类名称
        if (showName) {
            Text(
                text = category.name,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    isSelected -> DesignTokens.BrandColors.Ledger
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * 分类网格配置
 */
data class CategoryGridConfig(
    val itemSize: Dp = 56.dp,
    val iconSize: Dp = 32.dp,
    val spacing: Dp = 8.dp,
    val nameTextSize: Dp = 11.dp,
    val cornerRadius: Dp = 8.dp,
    val selectedAlpha: Float = 0.12f
)
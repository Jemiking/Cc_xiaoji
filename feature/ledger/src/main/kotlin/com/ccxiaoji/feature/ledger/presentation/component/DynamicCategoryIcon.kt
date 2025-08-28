package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.IconDisplayMode

/**
 * 动态分类图标组件
 * 根据用户偏好显示emoji图标或Material Design图标
 */
@Composable
fun DynamicCategoryIcon(
    category: Category,
    iconDisplayMode: IconDisplayMode,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    // 使用remember来缓存计算结果，避免不必要的重组
    val iconContent = remember(category.id, category.icon, iconDisplayMode) {
        when (iconDisplayMode) {
            IconDisplayMode.EMOJI -> "emoji"
            IconDisplayMode.MATERIAL -> "material"
        }
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (iconDisplayMode) {
            IconDisplayMode.EMOJI -> {
                EmojiCategoryIcon(
                    emoji = category.icon,
                    size = size,
                    modifier = Modifier
                )
            }
            IconDisplayMode.MATERIAL -> {
                MaterialCategoryIcon(
                    category = category,
                    size = size,
                    tint = tint,
                    modifier = Modifier
                )
            }
        }
    }
}

/**
 * Emoji分类图标组件
 */
@Composable
private fun EmojiCategoryIcon(
    emoji: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Text(
        text = emoji,
        fontSize = (size.value * 0.8).sp,
        modifier = modifier,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Normal
    )
}

/**
 * Material Design分类图标组件
 */
@Composable
private fun MaterialCategoryIcon(
    category: Category,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier
) {
    // 缓存图标计算结果，避免重复计算
    val materialIcon = remember(category.icon, category.type) {
        CategoryIconMapper.getMaterialIconByEmoji(
            emojiIcon = category.icon,
            isIncome = category.type == Category.Type.INCOME
        )
    }
    
    val iconToDisplay = remember(materialIcon, category.type) {
        materialIcon ?: CategoryIconMapper.getDefaultIcon(category.type == Category.Type.INCOME)
    }
    
    Icon(
        imageVector = iconToDisplay,
        contentDescription = category.name,
        modifier = modifier.size(size),
        tint = tint
    )
}

/**
 * 简化版动态分类图标，用于只有分类名称的场景
 */
@Composable
fun DynamicCategoryIconByName(
    categoryName: String,
    isIncome: Boolean,
    iconDisplayMode: IconDisplayMode,
    fallbackEmoji: String = "📝",
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (iconDisplayMode) {
            IconDisplayMode.EMOJI -> {
                EmojiCategoryIcon(
                    emoji = fallbackEmoji,
                    size = size,
                    modifier = Modifier
                )
            }
            IconDisplayMode.MATERIAL -> {
                val materialIcon = CategoryIconMapper.getMaterialIconFuzzy(
                    categoryName = categoryName,
                    isIncome = isIncome
                )
                
                if (materialIcon != null) {
                    Icon(
                        imageVector = materialIcon,
                        contentDescription = categoryName,
                        modifier = Modifier.size(size),
                        tint = tint
                    )
                } else {
                    // 如果没有找到Material图标，使用默认图标
                    Icon(
                        imageVector = CategoryIconMapper.getDefaultIcon(isIncome),
                        contentDescription = categoryName,
                        modifier = Modifier.size(size),
                        tint = tint
                    )
                }
            }
        }
    }
}

/**
 * 检查分类是否有对应的Material图标
 * 修复：基于emoji图标而不是分类名称进行检查
 */
fun Category.hasMaterialIcon(): Boolean {
    return CategoryIconMapper.getMaterialIconByEmoji(
        emojiIcon = this.icon,
        isIncome = this.type == Category.Type.INCOME
    ) != null
}

/**
 * 获取分类对应的Material图标（如果有的话）
 * 修复：基于emoji图标而不是分类名称进行映射
 */
fun Category.getMaterialIcon() = CategoryIconMapper.getMaterialIconByEmoji(
    emojiIcon = this.icon,
    isIncome = this.type == Category.Type.INCOME
)
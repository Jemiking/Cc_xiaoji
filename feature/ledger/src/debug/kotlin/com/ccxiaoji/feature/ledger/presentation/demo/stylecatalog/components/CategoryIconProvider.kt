package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 分类图标资源提供器
 * 使用Material Icons Outlined (细线条图标)
 */
object CategoryIconProvider {

    /**
     * 获取分类对应的图标
     * 使用Material Icons Outlined系列，符合iOS细线条风格
     */
    fun getIcon(categoryId: String): ImageVector {
        return when (categoryId) {
            // 支出类别
            "grocery" -> Icons.Outlined.ShoppingCart
            "breakfast", "dining" -> Icons.Outlined.Restaurant
            "beverage" -> Icons.Outlined.LocalCafe
            "transport" -> Icons.Outlined.DirectionsCar
            "travel" -> Icons.Outlined.Flight
            "housing", "home", "utilities" -> Icons.Outlined.Home
            "medical" -> Icons.Outlined.LocalHospital
            "school", "study" -> Icons.Outlined.School
            "phone_bill" -> Icons.Outlined.Phone
            "clothing" -> Icons.Outlined.Checkroom
            "fruit" -> Icons.Outlined.LocalFlorist
            "snack" -> Icons.Outlined.Fastfood
            "condiments" -> Icons.Outlined.Kitchen
            "tobacco" -> Icons.Outlined.SmokingRooms
            "daily" -> Icons.Outlined.CleaningServices
            "beauty" -> Icons.Outlined.Face
            "red_packet" -> Icons.Outlined.CardGiftcard
            "entertainment" -> Icons.Outlined.SportsEsports
            "gift" -> Icons.Outlined.Cake
            "electronics" -> Icons.Outlined.Devices
            "custom_1" -> Icons.Outlined.PersonOutline
            "supermarket" -> Icons.Outlined.Store
            "other" -> Icons.Outlined.MoreHoriz

            // 收入类别
            "salary" -> Icons.Outlined.AccountBalance
            "bonus" -> Icons.Outlined.EmojiEvents
            "investment" -> Icons.Outlined.TrendingUp
            "part_time" -> Icons.Outlined.Work
            "red_packet_in" -> Icons.Outlined.Redeem

            // 默认图标
            else -> Icons.Outlined.Category
        }
    }
}

/**
 * 分类图标组件
 * 显示iOS风格的细线条图标
 */
@Composable
fun CategoryIcon(
    categoryId: String,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF8E8E93), // iOS标准灰色
    selected: Boolean = false
) {
    val iconColor = if (selected) {
        Color(0xFF007AFF) // iOS蓝色
    } else {
        tint
    }

    Icon(
        imageVector = CategoryIconProvider.getIcon(categoryId),
        contentDescription = null,
        modifier = modifier.size(28.dp),
        tint = iconColor
    )
}

/**
 * iOS风格的图标尺寸和颜色定义
 */
object IOSIconStyle {
    val ICON_SIZE = 28.dp
    val ICON_SIZE_SMALL = 20.dp
    val ICON_SIZE_LARGE = 32.dp

    val COLOR_DEFAULT = Color(0xFF8E8E93)     // 默认灰色
    val COLOR_SELECTED = Color(0xFF007AFF)    // 选中蓝色
    val COLOR_DESTRUCTIVE = Color(0xFFFF3B30) // 删除红色
    val COLOR_SUCCESS = Color(0xFF34C759)     // 成功绿色
}


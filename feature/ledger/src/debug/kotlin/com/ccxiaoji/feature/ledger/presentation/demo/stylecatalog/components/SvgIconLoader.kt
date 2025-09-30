package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*

/**
 * SVG图标加载器 - 映射本地SVG文件到分类
 */
@Composable
fun CategorySvgIcon(
    categoryId: String,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF8E8E93) // iOS标准灰色
) {
    val painter = getCategoryPainter(categoryId)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint)
    )
}

/**
 * 根据分类ID获取对应的图标
 * 优先使用细线条图标
 */
@Composable
private fun getCategoryPainter(categoryId: String): Painter {
    // 映射分类ID到Material Icons（作为临时方案）
    // 后续可替换为本地SVG资源
    val icon = when (categoryId) {
        // 支出类别
        "grocery" -> Icons.Outlined.ShoppingCart
        "breakfast" -> Icons.Outlined.Restaurant
        "dining" -> Icons.Outlined.RestaurantMenu
        "condiments" -> Icons.Outlined.Kitchen
        "fruit" -> Icons.Outlined.Spa // 临时替代
        "snack" -> Icons.Outlined.Cookie
        "beverage" -> Icons.Outlined.LocalCafe
        "clothing" -> Icons.Outlined.Checkroom
        "transport" -> Icons.Outlined.DirectionsCar
        "travel" -> Icons.Outlined.Flight
        "phone_bill" -> Icons.Outlined.Phone
        "tobacco" -> Icons.Outlined.SmokingRooms
        "study" -> Icons.Outlined.School
        "daily" -> Icons.Outlined.CleaningServices
        "housing" -> Icons.Outlined.Home
        "beauty" -> Icons.Outlined.Face
        "medical" -> Icons.Outlined.LocalHospital
        "red_packet" -> Icons.Outlined.CardGiftcard
        "entertainment" -> Icons.Outlined.SportsEsports
        "gift" -> Icons.Outlined.Cake
        "electronics" -> Icons.Outlined.Devices
        "utilities" -> Icons.Outlined.Bolt
        "other" -> Icons.Outlined.MoreHoriz
        "custom_1" -> Icons.Outlined.PersonOutline
        "supermarket" -> Icons.Outlined.Store

        // 收入类别
        "salary" -> Icons.Outlined.AccountBalance
        "bonus" -> Icons.Outlined.EmojiEvents
        "investment" -> Icons.Outlined.ShowChart
        "part_time" -> Icons.Outlined.Work
        "red_packet_in" -> Icons.Outlined.Redeem

        // 默认
        else -> Icons.Outlined.Category
    }

    return rememberVectorPainter(icon)
}

/**
 * 自定义细线条图标样式
 */
object ThinIconStyle {
    const val STROKE_WIDTH = 1.0f // 细线条宽度
    val ICON_SIZE = 28 // dp
    val COLOR_DEFAULT = Color(0xFF8E8E93) // iOS灰色
    val COLOR_SELECTED = Color(0xFF007AFF) // iOS蓝色
}

/**
 * 图标分类数据
 */
data class IconCategory(
    val id: String,
    val name: String,
    val svgPath: String? = null // 可选的SVG路径
)

/**
 * 预定义的分类列表
 */
object CategoryIcons {
    val expenseCategories = listOf(
        IconCategory("grocery", "买菜", "localGroceryStore.svg"),
        IconCategory("breakfast", "早餐", "restaurant.svg"),
        IconCategory("dining", "下馆子", "localCafe.svg"),
        IconCategory("condiments", "柴米油盐", "home.svg"),
        IconCategory("fruit", "水果", null), // 需要自定义
        IconCategory("snack", "零食", "localBar.svg"),
        IconCategory("beverage", "饮料", "localCafe.svg"),
        IconCategory("clothing", "衣服", null), // 需要自定义
        IconCategory("transport", "交通", "directionsCar.svg"),
        IconCategory("travel", "旅行", "flight.svg"),
        IconCategory("phone_bill", "话费网费", null), // 需要自定义
        IconCategory("tobacco", "烟酒", "localBar.svg"),
        IconCategory("study", "学习", "school.svg"),
        IconCategory("daily", "日用品", "home.svg"),
        IconCategory("housing", "住房", "home.svg"),
        IconCategory("beauty", "美妆", null), // 需要自定义
        IconCategory("medical", "医疗", "localHospital.svg"),
        IconCategory("red_packet", "发红包", "attachMoney.svg"),
        IconCategory("entertainment", "娱乐", "movie.svg"),
        IconCategory("gift", "请客送礼", null), // 需要自定义
        IconCategory("electronics", "电器数码", null), // 需要自定义
        IconCategory("utilities", "水电煤", "home.svg"),
        IconCategory("other", "其它", "moreHoriz.svg"),
        IconCategory("custom_1", "崔芳榕专用", "person.svg"),
        IconCategory("supermarket", "超市", "store.svg")
    )

    val incomeCategories = listOf(
        IconCategory("salary", "工资", "accountBalance.svg"),
        IconCategory("bonus", "奖金", "star.svg"),
        IconCategory("investment", "投资收益", "trendingUp.svg"),
        IconCategory("part_time", "兼职", "businessCenter.svg"),
        IconCategory("red_packet_in", "收红包", "cardGiftcard.svg")
    )
}
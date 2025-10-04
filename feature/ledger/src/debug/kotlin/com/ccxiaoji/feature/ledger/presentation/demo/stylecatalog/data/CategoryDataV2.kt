package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.data

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 分类数据V2 - 1:1复刻版本
 * 包含精确的颜色值和图标映射
 */
object CategoryDataV2 {

    /**
     * 分类数据类
     */
    data class Category(
        val id: String,
        val name: String,
        val backgroundColor: Color,
        val icon: ImageVector  // 暂时使用ImageVector，后续可改为drawable资源
    )

    /**
     * 支出分类列表 - 精确颜色值
     */
    val EXPENSE_CATEGORIES = listOf(
        // 第一行
        Category("grocery", "买菜", Color(0xFF4ECDC4), Icons.Filled.ShoppingCart),
        Category("breakfast", "早餐", Color(0xFFFF9F1C), Icons.Filled.Restaurant),
        Category("dining", "下馆子", Color(0xFFE91E63), Icons.Filled.RestaurantMenu),
        Category("condiments", "柴米油盐", Color(0xFFFF6B6B), Icons.Filled.Kitchen),
        Category("fruit", "水果", Color(0xFF4CAF50), Icons.Filled.Eco),

        // 第二行
        Category("snack", "零食", Color(0xFFFFC107), Icons.Filled.Fastfood),
        Category("beverage", "饮料", Color(0xFF2196F3), Icons.Filled.LocalCafe),
        Category("clothing", "衣服", Color(0xFF9C27B0), Icons.Filled.Checkroom),
        Category("transport", "交通", Color(0xFF03A9F4), Icons.Filled.DirectionsBus),
        Category("travel", "旅行", Color(0xFF3F51B5), Icons.Filled.Flight),

        // 第三行
        Category("phone_bill", "话费网费", Color(0xFF8BC34A), Icons.Filled.Phone),
        Category("tobacco", "烟酒", Color(0xFF795548), Icons.Filled.LocalBar),
        Category("study", "学习", Color(0xFFF44336), Icons.Filled.School),
        Category("daily", "日用品", Color(0xFF00BCD4), Icons.Filled.CleaningServices),
        Category("housing", "住房", Color(0xFFFF9800), Icons.Filled.Home),

        // 第四行
        Category("beauty", "美妆", Color(0xFFE91E63), Icons.Filled.Face),
        Category("medical", "医疗", Color(0xFFF44336), Icons.Filled.LocalHospital),
        Category("red_packet", "发红包", Color(0xFFFF5252), Icons.Filled.CardGiftcard),
        Category("entertainment", "娱乐", Color(0xFF673AB7), Icons.Filled.SportsEsports),
        Category("gift", "请客送礼", Color(0xFFFF4081), Icons.Filled.Cake),

        // 第五行
        Category("electronics", "电器数码", Color(0xFF607D8B), Icons.Filled.Devices),
        Category("utilities", "水电煤", Color(0xFF2196F3), Icons.Filled.Bolt),
        Category("other", "其它", Color(0xFF9E9E9E), Icons.Filled.MoreHoriz),
        Category("custom_1", "崔芳榕专用", Color(0xFF7B1FA2), Icons.Filled.Favorite),
        Category("supermarket", "超市", Color(0xFF009688), Icons.Filled.Store)
    )

    /**
     * 收入分类列表
     */
    val INCOME_CATEGORIES = listOf(
        Category("salary", "工资", Color(0xFF4CAF50), Icons.Filled.AccountBalance),
        Category("bonus", "奖金", Color(0xFFFFC107), Icons.Filled.EmojiEvents),
        Category("investment", "投资", Color(0xFF2196F3), Icons.Filled.TrendingUp),
        Category("part_time", "兼职", Color(0xFF9C27B0), Icons.Filled.Work),
        Category("red_packet_in", "收红包", Color(0xFFFF5252), Icons.Filled.Redeem),
        Category("other_income", "其他", Color(0xFF9E9E9E), Icons.Filled.AttachMoney)
    )

    /**
     * 获取分类的背景颜色
     */
    fun getCategoryColor(categoryId: String): Color {
        return EXPENSE_CATEGORIES.find { it.id == categoryId }?.backgroundColor
            ?: INCOME_CATEGORIES.find { it.id == categoryId }?.backgroundColor
            ?: Color(0xFF9E9E9E)
    }

    /**
     * 获取分类的图标
     */
    fun getCategoryIcon(categoryId: String): ImageVector {
        return EXPENSE_CATEGORIES.find { it.id == categoryId }?.icon
            ?: INCOME_CATEGORIES.find { it.id == categoryId }?.icon
            ?: Icons.Filled.Category
    }
}
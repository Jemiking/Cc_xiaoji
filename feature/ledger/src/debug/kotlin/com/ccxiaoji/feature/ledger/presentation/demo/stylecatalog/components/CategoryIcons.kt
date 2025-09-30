package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.BillTab
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.Category

/**
 * 分类图标数据源
 */
object CategoryData {

    /**
     * 支出分类列表（25个）
     */
    val EXPENSE_CATEGORIES = listOf(
        // 第一行
        Category("grocery", "买菜", Icons.Outlined.ShoppingBasket, BillTab.EXPENSE),
        Category("breakfast", "早餐", Icons.Outlined.Restaurant, BillTab.EXPENSE),
        Category("dining", "下馆子", Icons.Outlined.RestaurantMenu, BillTab.EXPENSE),
        Category("condiments", "柴米油盐", Icons.Outlined.Kitchen, BillTab.EXPENSE),
        Category("fruit", "水果", Icons.Outlined.LocalFlorist, BillTab.EXPENSE),

        // 第二行
        Category("snack", "零食", Icons.Outlined.Fastfood, BillTab.EXPENSE),
        Category("beverage", "饮料", Icons.Outlined.LocalCafe, BillTab.EXPENSE),
        Category("clothing", "衣服", Icons.Outlined.Checkroom, BillTab.EXPENSE),
        Category("transport", "交通", Icons.Outlined.DirectionsCar, BillTab.EXPENSE),
        Category("travel", "旅行", Icons.Outlined.Flight, BillTab.EXPENSE),

        // 第三行
        Category("phone_bill", "话费网费", Icons.Outlined.Phone, BillTab.EXPENSE),
        Category("tobacco", "烟酒", Icons.Outlined.SmokingRooms, BillTab.EXPENSE),
        Category("study", "学习", Icons.Outlined.MenuBook, BillTab.EXPENSE),
        Category("daily", "日用品", Icons.Outlined.CleaningServices, BillTab.EXPENSE),
        Category("housing", "住房", Icons.Outlined.Home, BillTab.EXPENSE),

        // 第四行
        Category("beauty", "美妆", Icons.Outlined.Face, BillTab.EXPENSE),
        Category("medical", "医疗", Icons.Outlined.LocalHospital, BillTab.EXPENSE),
        Category("red_packet", "发红包", Icons.Outlined.CardGiftcard, BillTab.EXPENSE),
        Category("entertainment", "娱乐", Icons.Outlined.SportsEsports, BillTab.EXPENSE),
        Category("gift", "请客送礼", Icons.Outlined.Cake, BillTab.EXPENSE),

        // 第五行
        Category("electronics", "电器数码", Icons.Outlined.Devices, BillTab.EXPENSE),
        Category("utilities", "水电煤", Icons.Outlined.Bolt, BillTab.EXPENSE),
        Category("other", "其它", Icons.Outlined.MoreHoriz, BillTab.EXPENSE),
        Category("custom_1", "崔芳榕专用", Icons.Outlined.PersonOutline, BillTab.EXPENSE),
        Category("supermarket", "超市", Icons.Outlined.ShoppingCart, BillTab.EXPENSE)
    )

    /**
     * 收入分类列表
     */
    val INCOME_CATEGORIES = listOf(
        Category("salary", "工资", Icons.Outlined.AccountBalance, BillTab.INCOME),
        Category("bonus", "奖金", Icons.Outlined.EmojiEvents, BillTab.INCOME),
        Category("investment", "投资收益", Icons.Outlined.TrendingUp, BillTab.INCOME),
        Category("part_time", "兼职", Icons.Outlined.Work, BillTab.INCOME),
        Category("red_packet_in", "收红包", Icons.Outlined.Redeem, BillTab.INCOME),

        Category("refund", "退款", Icons.Outlined.CurrencyExchange, BillTab.INCOME),
        Category("reimbursement", "报销", Icons.Outlined.Receipt, BillTab.INCOME),
        Category("gift_in", "礼金", Icons.Outlined.CardGiftcard, BillTab.INCOME),
        Category("transfer_in", "转账收入", Icons.Outlined.AccountBalanceWallet, BillTab.INCOME),
        Category("other_income", "其他收入", Icons.Outlined.Add, BillTab.INCOME)
    )

    /**
     * 根据Tab获取对应分类
     */
    fun getCategoriesByTab(tab: BillTab): List<Category> {
        return when (tab) {
            BillTab.EXPENSE -> EXPENSE_CATEGORIES
            BillTab.INCOME -> INCOME_CATEGORIES
            BillTab.TRANSFER -> emptyList() // 转账不需要分类
        }
    }
}
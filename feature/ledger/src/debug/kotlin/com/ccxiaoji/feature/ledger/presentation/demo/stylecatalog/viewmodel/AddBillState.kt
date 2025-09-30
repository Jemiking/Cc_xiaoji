package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 记账页面状态数据类
 */
data class AddBillState(
    val selectedTab: BillTab = BillTab.EXPENSE,
    val selectedCategory: Category? = null,
    val amount: String = "0.0",
    val note: String = "",
    val account: String = "骆一微信零钱",
    val dateTime: String = "今天 17:11",
    val isReimbursement: Boolean = false,
    val hasImage: Boolean = false
)

/**
 * 账单类型Tab
 */
enum class BillTab(val displayName: String) {
    EXPENSE("支出"),
    INCOME("收入"),
    TRANSFER("转账")
}

/**
 * 分类数据类
 */
data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val type: BillTab
)
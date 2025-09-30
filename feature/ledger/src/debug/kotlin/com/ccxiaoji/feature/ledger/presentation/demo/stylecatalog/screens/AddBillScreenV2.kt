package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.AddBillState
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.BillTab

/**
 * 记账界面V2 - 1:1复刻iOS风格
 * 使用Popup代替Dialog，避免遮罩问题
 */
@Composable
fun AddBillScreenV2(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: (AddBillState) -> Unit
) {
    // 动画状态
    val density = LocalDensity.current
    val transition = updateTransition(visible, label = "AddBillTransition")

    val offsetY by transition.animateDp(
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            } else {
                spring(stiffness = Spring.StiffnessMedium)
            }
        },
        label = "offsetY"
    ) { isVisible ->
        if (isVisible) 0.dp else with(density) { 1000.dp }
    }

    if (visible) {
        // 使用Popup替代Dialog，避免默认遮罩
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            // 手动添加半透明背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {} // 拦截点击但不处理
            ) {
                // 主内容区域 - 纯白背景
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = offsetY),
                    color = Color.White
                ) {
                    AddBillContent(
                        onDismiss = onDismiss,
                        onSave = onSave
                    )
                }
            }
        }
    }
}

/**
 * 记账界面主内容
 */
@Composable
private fun AddBillContent(
    onDismiss: () -> Unit,
    onSave: (AddBillState) -> Unit
) {
    var state by remember { mutableStateOf(AddBillState()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding() // 处理状态栏
    ) {
        // 1. 顶部栏
        IOSStyleTopBar(
            selectedTab = state.selectedTab,
            onTabSelected = { tab ->
                state = state.copy(selectedTab = tab)
            },
            onClose = onDismiss,
            onAdd = { /* TODO */ }
        )

        // 2. 分类网格（可滚动）
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            IOSStyleCategoryGrid(
                tab = state.selectedTab,
                selectedCategory = state.selectedCategory,
                onCategorySelected = { category ->
                    state = state.copy(selectedCategory = category)
                }
            )
        }

        // 3. 输入区域
        IOSStyleInputSection(
            amount = state.amount,
            note = state.note,
            onNoteChange = { state = state.copy(note = it) }
        )

        // 4. 数字键盘
        IOSStyleNumberKeyboard(
            onNumberInput = { number ->
                state = state.copy(amount = updateAmount(state.amount, number))
            },
            onDelete = {
                state = state.copy(amount = deleteLastChar(state.amount))
            },
            onOperator = { op ->
                state = state.copy(amount = applyOperator(state.amount, op))
            },
            onSave = {
                onSave(state)
                onDismiss()
            },
            onSaveAndNew = {
                onSave(state)
                state = AddBillState(selectedTab = state.selectedTab)
            }
        )
    }
}

/**
 * iOS风格顶部栏
 */
@Composable
private fun IOSStyleTopBar(
    selectedTab: BillTab,
    onTabSelected: (BillTab) -> Unit,
    onClose: () -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.White,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 关闭按钮 (X)
            Text(
                text = "✕",
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.clickable(onClick = onClose)
            )

            // Tab组
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                TabItem("支出", selectedTab == BillTab.EXPENSE) {
                    onTabSelected(BillTab.EXPENSE)
                }
                TabItem("收入", selectedTab == BillTab.INCOME) {
                    onTabSelected(BillTab.INCOME)
                }
                TabItem("转账", selectedTab == BillTab.TRANSFER) {
                    onTabSelected(BillTab.TRANSFER)
                }
            }

            // 添加按钮 (+)
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(onClick = onAdd),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color.Black
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) Color.Black else Color(0xFF999999)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(24.dp)
                    .height(2.dp)
                    .background(Color.Black)
            )
        }
    }
}

/**
 * iOS风格分类网格
 */
@Composable
private fun IOSStyleCategoryGrid(
    tab: BillTab,
    selectedCategory: Any?,
    onCategorySelected: (Any) -> Unit
) {
    // 临时分类数据
    val categories = when (tab) {
        BillTab.EXPENSE -> expenseCategories
        BillTab.INCOME -> incomeCategories
        BillTab.TRANSFER -> emptyList()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(categories) { category ->
            IOSCategoryItem(
                category = category,
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun IOSCategoryItem(
    category: TempCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        // 图标占位符
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (selected) Color(0xFFE8E8E8) else Color(0xFFF5F5F5),
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.name,
            fontSize = 11.sp,
            color = Color(0xFF333333),
            maxLines = 1
        )
    }
}

/**
 * iOS风格输入区域
 */
@Composable
private fun IOSStyleInputSection(
    amount: String,
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 分割线
        Divider(color = Color(0xFFEEEEEE))

        // 备注输入
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (note.isEmpty()) {
                Text(
                    text = "点此输入备注...",
                    color = Color(0xFF999999),
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = note,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }

        Divider(color = Color(0xFFEEEEEE))

        // 金额显示
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "$amount CNY >",
                fontSize = 28.sp,
                color = Color(0xFFFF3B30),
                fontWeight = FontWeight.Normal
            )
        }

        Divider(color = Color(0xFFEEEEEE))

        // 快捷按钮行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickButton("骆一微信零钱")
            QuickButton("今天 17:11")
            QuickButton("报销")
            QuickButton("图片")
        }

        Divider(color = Color(0xFFEEEEEE))
    }
}

@Composable
private fun QuickButton(text: String) {
    Surface(
        modifier = Modifier.height(28.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFFDDDDDD)
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

/**
 * iOS风格数字键盘
 */
@Composable
private fun IOSStyleNumberKeyboard(
    onNumberInput: (String) -> Unit,
    onDelete: () -> Unit,
    onOperator: (String) -> Unit,
    onSave: () -> Unit,
    onSaveAndNew: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3", "⌫"),
        listOf("4", "5", "6", "-"),
        listOf("7", "8", "9", "+"),
        listOf("再记", "0", ".", "保存")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(0xFFF7F7F7))
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                row.forEach { key ->
                    KeyboardKey(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key) {
                                "⌫" -> onDelete()
                                "-", "+" -> onOperator(key)
                                "再记" -> onSaveAndNew()
                                "保存" -> onSave()
                                else -> onNumberInput(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyboardKey(
    key: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isSpecial = key in listOf("⌫", "-", "+")
    val isSave = key == "保存"

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        color = when {
            isSave -> Color(0xFFFF6B6B)
            isSpecial -> Color(0xFFE8E8E8)
            else -> Color.White
        },
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = Color(0xFFDDDDDD)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = key,
                fontSize = if (key.length > 1) 16.sp else 20.sp,
                color = if (isSave) Color.White else Color.Black,
                fontWeight = if (isSave) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

// 临时数据类
data class TempCategory(val name: String, val icon: String)

val expenseCategories = listOf(
    TempCategory("买菜", "🛒"),
    TempCategory("早餐", "🍳"),
    TempCategory("下馆子", "🍜"),
    TempCategory("柴米油盐", "🧂"),
    TempCategory("水果", "🍎"),
    TempCategory("零食", "🍿"),
    TempCategory("饮料", "☕"),
    TempCategory("衣服", "👔"),
    TempCategory("交通", "🚗"),
    TempCategory("旅行", "✈️"),
    TempCategory("话费网费", "📱"),
    TempCategory("烟酒", "🍺"),
    TempCategory("学习", "📚"),
    TempCategory("日用品", "🧻"),
    TempCategory("住房", "🏠"),
    TempCategory("美妆", "💄"),
    TempCategory("医疗", "🏥"),
    TempCategory("发红包", "🧧"),
    TempCategory("娱乐", "🎮"),
    TempCategory("请客送礼", "🎁"),
    TempCategory("电器数码", "📱"),
    TempCategory("水电煤", "💡"),
    TempCategory("其它", "📦"),
    TempCategory("崔芳榕专用", "👤"),
    TempCategory("超市", "🏪")
)

val incomeCategories = listOf(
    TempCategory("工资", "💰"),
    TempCategory("奖金", "🏆"),
    TempCategory("投资", "📈"),
    TempCategory("兼职", "💼"),
    TempCategory("红包", "🧧")
)

// 辅助函数
private fun updateAmount(current: String, input: String): String {
    return if (current == "0.0") input else current + input
}

private fun deleteLastChar(current: String): String {
    return if (current.length > 1) current.dropLast(1) else "0.0"
}

private fun applyOperator(current: String, operator: String): String {
    return "$current $operator "
}
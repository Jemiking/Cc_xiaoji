package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.data.CategoryDataV2
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
    onSave: (AddBillState) -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    // 覆盖状态栏为白底黑字，避免看到底层蓝色
    if (visible) {
        com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar(color = Color.White, darkIcons = true)
    }
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
                        onSave = onSave,
                        navController = navController
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
    onSave: (AddBillState) -> Unit,
    navController: androidx.navigation.NavController?
) {
    var state by remember { mutableStateOf(AddBillState()) }
    var selectedCategoryId: String? by remember { mutableStateOf(null) }

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
                selectedCategoryId = null
            },
            onClose = onDismiss,
            onAdd = {
                // 跳转正式的“分类管理”页面
                navController?.navigate(com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation.CategoryManagementRoute)
            }
        )

        // 2-5. 像素规范绝对定位布局（分类网格 / 金额条 / 键盘 / 保存按钮）
        val scale = rememberScale()
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            val categories = when (state.selectedTab) {
                BillTab.EXPENSE -> CategoryDataV2.EXPENSE_CATEGORIES
                BillTab.INCOME -> CategoryDataV2.INCOME_CATEGORIES
                BillTab.TRANSFER -> emptyList()
            }

            if (state.selectedTab != BillTab.TRANSFER) {
                CategoryGridAbsolute(
                    scale = scale,
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelect = { cid -> selectedCategoryId = cid }
                )
            }

            // 工具胶囊（账户/时间/报销/图片/旗标）
            ToolPillsAbsolute(
                scale = scale,
                items = listOf("骋-微信零钱", "今天 17:11", "报销", "图片", "旗标")
            )

            AmountBarAbsolute(scale = scale, expr = state.amount)

            KeypadAbsolute(
                scale = scale,
                onNumber = { key -> state = state.copy(amount = demoAppendInput(state.amount, key)) },
                onDot = { state = state.copy(amount = demoAppendInput(state.amount, ".")) },
                onBackspace = { state = state.copy(amount = demoBackspace(state.amount)) },
                onPlus = { state = state.copy(amount = demoAppendOperator(state.amount, "+")) },
                onMinus = { state = state.copy(amount = demoAppendOperator(state.amount, "-")) },
                onAgain = {
                    val eval = demoEvalExpression(state.amount)
                    if (eval != null && eval > 0.0) {
                        onSave(state.copy(amount = String.format(java.util.Locale.getDefault(), "%.2f", eval)))
                        state = state.copy(amount = "0.0", note = "")
                    }
                }
            )

            SaveButtonAbsolute(
                scale = scale,
                onSave = {
                    val eval = demoEvalExpression(state.amount)
                    if (eval != null && eval > 0.0) {
                        onSave(state.copy(amount = String.format(java.util.Locale.getDefault(), "%.2f", eval)))
                        state = state.copy(amount = "0.0", note = "")
                    }
                }
            )

            
        }
    }
}

// Overlay 调试功能已移除（按需可再启用）

// ============ 图标资源接入（优先drawable） ============
@Composable
private fun rememberCategoryPainter(categoryId: String): Painter? {
    val context = LocalContext.current
    val resId = remember(categoryId) { context.resources.getIdentifier("cat_" + categoryId, "drawable", context.packageName) }
    return if (resId != 0) painterResource(id = resId) else null
}

/**
 * iOS风格顶部栏 - 1:1复刻版本
 */
@Composable
private fun IOSStyleTopBar(
    selectedTab: BillTab,
    onTabSelected: (BillTab) -> Unit,
    onClose: () -> Unit,
    onAdd: () -> Unit
) {
    val scale = rememberScale()
    val hit = scale.dp(88)
    val underlineH = scale.dp(4)
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
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 关闭按钮 (X) - 精确样式
            Box(
                modifier = Modifier
                    .size(hit)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    fontSize = 24.sp,
                    color = Color.Black
                )
            }

            // Tab组 - 中央位置
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                TabItemWithUnderline("支出", selectedTab == BillTab.EXPENSE, underlineH) {
                    onTabSelected(BillTab.EXPENSE)
                }
                TabItemWithUnderline("收入", selectedTab == BillTab.INCOME, underlineH) {
                    onTabSelected(BillTab.INCOME)
                }
                TabItemWithUnderline("转账", selectedTab == BillTab.TRANSFER, underlineH) {
                    onTabSelected(BillTab.TRANSFER)
                }
            }

            // 添加按钮 (+) - 黑色圆形背景
            Box(
                modifier = Modifier
                    .size(hit)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TabItemWithUnderline(text: String, selected: Boolean, underlineH: Dp, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) Color.Black else Color(0xFF999999)
        )
        if (selected) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .height(underlineH)
                    .width(IntrinsicSize.Min)
                    .background(Color.Black)
            )
        }
    }
}

/**
 * iOS风格分类网格 - 1:1复刻版本
 */
@Composable
private fun IOSStyleCategoryGrid(
    tab: BillTab,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit
) {
    // 使用CategoryDataV2的数据
    val categories = when (tab) {
        BillTab.EXPENSE -> CategoryDataV2.EXPENSE_CATEGORIES
        BillTab.INCOME -> CategoryDataV2.INCOME_CATEGORIES
        BillTab.TRANSFER -> emptyList()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize()
    ) {
        items(categories) { category ->
            IOSCategoryItemV2(
                category = category,
                selected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}

/**
 * iOS风格分类项 - 1:1复刻版本
 * 彩色圆形背景 + 白色图标
 */
@Composable
private fun IOSCategoryItemV2(
    category: CategoryDataV2.Category,
    selected: Boolean,
    onClick: () -> Unit
    ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        // 选中：彩色圆形底 + 白色图标；未选中：灰色图标无底
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(category.backgroundColor)
                )
            }
            val painter = rememberCategoryPainter(category.id)
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = if (selected) Color.White else Color(0xFF8D8F8E),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else Color(0xFF8D8F8E),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name,
            fontSize = 11.sp,
            color = if (selected) category.backgroundColor else Color(0xFF444444),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// 保留旧版本以兼容
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
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.name.take(1),
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name,
            fontSize = 11.sp,
            color = if (selected) Color(0xFF007AFF) else Color(0xFF333333),
            maxLines = 1
        )
    }
}

/**
 * iOS风格输入区域 - 1:1复刻版本
 */
@Composable
private fun IOSStyleInputSection(
    amountExpr: String,
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 分割线
        Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)

        // 备注输入
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable { /* TODO: 打开备注输入 */ }
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

        Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)

        // 金额显示
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val amountDisplay = remember(amountExpr) {
                    val v = demoEvalExpression(amountExpr)
                    if (v == null) "0.00" else String.format(java.util.Locale.getDefault(), "%.2f", v)
                }
                Text(
                    text = amountDisplay,
                    fontSize = 28.sp,
                    color = Color(0xFFFF3B30),
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CNY",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ">",
                    fontSize = 20.sp,
                    color = Color(0xFF999999)
                )
            }
        }

        Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)

        // 快捷按钮行 - 1:1复刻版本
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickButton("骆-微信零钱")  // 注意是"-"而不是"一"
            QuickButton("今天 17:11")
            QuickButton("报销")
            QuickButton("图片")

            // 购物车图标按钮
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { /* TODO: 购物车功能 */ },
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFDDDDDD)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "购物车",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Divider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
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
            isSave -> Color(0xFFFF6B6B)  // 保存按钮使用红色
            isSpecial -> Color(0xFFE8E8E8)  // 功能键使用浅灰色
            else -> Color.White  // 数字键使用白色
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

// ==== 表达式输入与计算（Demo版） ====
// 规则：仅支持 + 和 -，允许开头一元负号；每个操作数至多两位小数；末尾不得是运算符或裸小数点

private fun demoNormalizeBase(current: String): String {
    return if (current == "0" || current == "0.0" || current == "0.00") "" else current
}

private fun demoLastOperand(expr: String): String {
    val lastPlus = expr.lastIndexOf('+')
    val lastMinus = expr.lastIndexOf('-')
    val idx = maxOf(lastPlus, lastMinus)
    return if (idx >= 0) expr.substring(idx + 1) else expr
}

private fun demoAppendInput(current: String, input: String): String {
    var expr = demoNormalizeBase(current)
    if (input.length != 1) return expr
    val ch = input[0]
    return when (ch) {
        in '0'..'9' -> {
            val last = demoLastOperand(expr)
            val dotIdx = last.indexOf('.')
            if (dotIdx != -1 && last.length - dotIdx - 1 >= 2) expr else expr + ch
        }
        '.' -> {
            val last = demoLastOperand(expr)
            if (last.contains('.')) expr
            else {
                // 若当前操作数为空或仅为一元符号，则补0
                if (last.isEmpty() || (last.length == 1 && (last[0] == '+' || last[0] == '-'))) expr + "0." else expr + "."
            }
        }
        else -> expr
    }
}

private fun demoBackspace(current: String): String {
    val expr = demoNormalizeBase(current)
    if (expr.isEmpty()) return "0.0"
    val next = expr.dropLast(1)
    return if (next.isEmpty()) "0.0" else next
}

private fun demoAppendOperator(current: String, op: String): String {
    var expr = demoNormalizeBase(current)
    if (op != "+" && op != "-") return expr
    return if (expr.isEmpty()) {
        if (op == "-") "-" else expr // 允许一元负号，不处理一元+
    } else {
        val last = expr.last()
        if (last.isDigit()) expr + op else expr // 仅当末尾是数字时追加二元运算符
    }
}

private fun demoEvalExpression(exprRaw: String): Double? {
    val expr = demoNormalizeBase(exprRaw)
    if (expr.isEmpty()) return 0.0
    val ops = mutableListOf<Char>()
    val nums = mutableListOf<String>()
    val sb = StringBuilder()

    fun flush() {
        if (sb.isNotEmpty()) {
            nums += sb.toString()
            sb.clear()
        }
    }

    var i = 0
    while (i < expr.length) {
        val c = expr[i]
        when (c) {
            '+', '-' -> {
                if (i == 0) {
                    sb.append(c)
                } else {
                    val prev = expr[i - 1]
                    if (prev == '+' || prev == '-') {
                        if (c == '-' && sb.isEmpty()) sb.append(c) else return null
                    } else {
                        if (sb.isEmpty()) return null
                        if (sb.last() == '.') return null
                        flush(); ops += c
                    }
                }
            }
            '.' -> {
                if (sb.contains('.')) return null
                if (sb.isEmpty() || (sb.length == 1 && (sb[0] == '+' || sb[0] == '-'))) sb.append('0')
                sb.append('.')
            }
            in '0'..'9' -> {
                val dotIdx = sb.indexOf('.')
                if (dotIdx != -1 && sb.length - dotIdx - 1 >= 2) return null
                sb.append(c)
            }
            else -> return null
        }
        i++
    }
    if (sb.isEmpty()) return null
    if (sb.last() == '.') return null
    flush()
    if (nums.isEmpty() || nums.size != ops.size + 1) return null

    fun parseNum(s: String): Double? {
        val v = s.toDoubleOrNull() ?: return null
        return v
    }
    var acc = parseNum(nums[0]) ?: return null
    for (k in ops.indices) {
        val b = parseNum(nums[k + 1]) ?: return null
        when (ops[k]) {
            '+' -> acc += b
            '-' -> acc -= b
        }
    }
    if (acc <= 0.0) return null
    return kotlin.math.round(acc * 100.0) / 100.0
}

// ==== 像素规范：缩放与绝对定位组件（Demo版） ====

// 颜色与度量（像素规范）
private val Bg = Color(0xFFFFFFFF)
private val IconGray = Color(0xFF8D8F8E)
private val TextDark = Color(0xFF444444)
private val TextMid = Color(0xFF777777)
private val KeyBg = Color(0xFFF5F6F8)
private val AccentRed = Color(0xFFE65A57)
private val White = Color(0xFFFFFFFF)

private const val DESIGN_WIDTH = 920f
private const val CORNER = 24
private const val OUTER_LR = 32

// 分类网格
private const val CAT_ROW_H = 164
private val CatCols = listOf(32 to 184, 208 to 360, 384 to 536, 560 to 712, 736 to 888)
private val CatRows = listOf(160 to 324, 324 to 488, 488 to 652, 652 to 816, 816 to 980)
private const val ICON_SIZE = 64
private const val ICON_TOP_OFFSET = 28
private const val LABEL_TOP_OFFSET = 106
private const val LABEL_SIZE = 29

// 键盘与保存
private val KpCols = listOf(32 to 228, 252 to 448, 472 to 668, 692 to 888)
private val KpRows = listOf(1356 to 1472, 1496 to 1612, 1636 to 1752, 1776 to 1892)
private const val SAVE_X1 = 695
private const val SAVE_Y1 = 1895
private const val SAVE_W = 206
private const val SAVE_H = 96

private data class Scale(val s: Float, val density: Float) {
    fun px(px: Int): Int = (px * s).toInt()
    fun dp(px: Int): Dp = ((px * s) / density).dp
}

@Composable
private fun rememberScale(): Scale {
    val cfg = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidthPx = cfg.screenWidthDp * density
    val s = screenWidthPx / DESIGN_WIDTH
    return remember(cfg.screenWidthDp, density) { Scale(s, density) }
}

@Composable
private fun CategoryGridAbsolute(
    scale: Scale,
    categories: List<com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.data.CategoryDataV2.Category>,
    selectedCategoryId: String?,
    onSelect: (String) -> Unit
) {
    val items = categories.take(25)
    var idx = 0
    CatRows.forEach { (rowTop, _) ->
        CatCols.forEach { (left, right) ->
            if (idx >= items.size) return@forEach
            val c = items[idx++]
            val x = scale.px(left)
            val y = scale.px(rowTop)
            Box(
                modifier = Modifier
                    .offset { IntOffset(x, y) }
                    .size(width = scale.dp(right - left), height = scale.dp(CAT_ROW_H))
                    .clickable { onSelect(c.id) },
                contentAlignment = Alignment.TopStart
            ) {
                val selected = c.id == selectedCategoryId
                // 图标：选中时彩色圆底 + 白色图标；未选中为中灰图标
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, scale.px(ICON_TOP_OFFSET)) }
                        .size(scale.dp(ICON_SIZE)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(c.backgroundColor)
                        )
                    }
                    val painter = rememberCategoryPainter(c.id)
                    if (painter != null) {
                        androidx.compose.material3.Icon(
                            painter = painter,
                            contentDescription = c.name,
                            tint = if (selected) Color.White else TextMid,
                            modifier = Modifier.size(scale.dp((ICON_SIZE * 0.66).toInt()))
                        )
                    } else {
                        androidx.compose.material3.Icon(
                            imageVector = c.icon,
                            contentDescription = c.name,
                            tint = if (selected) Color.White else TextMid,
                            modifier = Modifier.size(scale.dp((ICON_SIZE * 0.66).toInt()))
                        )
                    }
                }
                Text(
                    text = c.name,
                    color = if (selected) c.backgroundColor else TextDark,
                    fontSize = (LABEL_SIZE * scale.s / scale.density).sp,
                    modifier = Modifier.offset { IntOffset(0, scale.px(LABEL_TOP_OFFSET)) }
                )
            }
        }
    }
}

@Composable
private fun AmountBarAbsolute(scale: Scale, expr: String) {
    val density = LocalDensity.current
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val value = demoEvalExpression(expr)
    val valueStr = if (value == null) "0.00" else String.format(java.util.Locale.getDefault(), "%.2f", value)

    // 规范尺寸（以像素为基，按设计宽缩放）：金额 46px，CNY 32px，间距 12px/4px，“>” 20px
    val amountFsSp = (46f * scale.s / scale.density).sp
    val cnyFsSp = (32f * scale.s / scale.density).sp
    val arrowFsSp = (20f * scale.s / scale.density).sp
    val gap1Px = scale.px(12)
    val gap2Px = scale.px(4)

    val amountMeasure = measurer.measure(
        text = androidx.compose.ui.text.AnnotatedString(valueStr),
        style = androidx.compose.ui.text.TextStyle(fontSize = amountFsSp, fontWeight = FontWeight.SemiBold)
    )
    val cnyMeasure = measurer.measure(
        text = androidx.compose.ui.text.AnnotatedString("CNY"),
        style = androidx.compose.ui.text.TextStyle(fontSize = cnyFsSp)
    )
    val arrowMeasure = measurer.measure(
        text = androidx.compose.ui.text.AnnotatedString(">"),
        style = androidx.compose.ui.text.TextStyle(fontSize = arrowFsSp)
    )
    val totalWidthPx = amountMeasure.size.width + gap1Px + cnyMeasure.size.width + gap2Px + arrowMeasure.size.width
    val y = scale.px(1330)
    val cfg = LocalConfiguration.current
    val screenWidthPx = (cfg.screenWidthDp * density.density).toInt()
    val rightEdgePx = screenWidthPx - scale.px(OUTER_LR)
    val x = rightEdgePx - totalWidthPx

    Row(
        modifier = Modifier
            .offset { IntOffset(x, y) }
            .width(with(density) { totalWidthPx.toDp() })
            .height(scale.dp(80)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(valueStr, color = AccentRed, fontSize = amountFsSp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(with(density) { gap1Px.toDp() }))
        Text("CNY", color = TextMid, fontSize = cnyFsSp)
        Spacer(Modifier.width(with(density) { gap2Px.toDp() }))
        Text(">", color = Color(0xFF999999), fontSize = arrowFsSp)
    }
}

@Composable
private fun ToolPillsAbsolute(scale: Scale, items: List<String>) {
    // 行顶约 1064px，按规范 44px 高度，圆角 24
    val density = LocalDensity.current
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val xStart = scale.px(OUTER_LR)
    val y = scale.px(1064)
    var cursorX = xStart
    val gap = scale.px(18)
    val rowH = scale.dp(44)
    val textSizeSp = (29f * scale.s / scale.density).sp
    val textStyle = androidx.compose.ui.text.TextStyle(fontSize = textSizeSp, color = TextDark)
    items.forEach { label ->
        val measure = measurer.measure(text = androidx.compose.ui.text.AnnotatedString(label), style = textStyle)
        val pillPx = measure.size.width + scale.px(18) * 2
        val pillW = with(density) { pillPx.toDp() }
        Box(
            modifier = Modifier
                .offset { IntOffset(cursorX, y) }
                .height(rowH)
                .width(pillW)
                .background(Color(0xFFF7F8FA), RoundedCornerShape(scale.dp(24)))
                .border(width = 1.dp, color = Color(0xFFE6E8EB), shape = RoundedCornerShape(scale.dp(24))),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = TextDark, fontSize = textSizeSp)
        }
        cursorX += pillPx + gap
    }
}

@Composable
private fun KeypadAbsolute(
    scale: Scale,
    onNumber: (String) -> Unit,
    onDot: () -> Unit,
    onBackspace: () -> Unit,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    onAgain: () -> Unit
) {
    val keys = listOf(
        listOf("1","2","3","⌫"),
        listOf("4","5","6","−"),
        listOf("7","8","9","+"),
        listOf("再记","0",".","")
    )
    KpRows.forEachIndexed { rIdx, (top, _) ->
        KpCols.forEachIndexed { cIdx, (left, right) ->
            val label = keys[rIdx][cIdx]
            val x = scale.px(left)
            val y = scale.px(top)
            Box(
                modifier = Modifier
                    .offset { IntOffset(x, y) }
                    .size(width = scale.dp(right - left), height = scale.dp(116))
                    .background(White, RoundedCornerShape(scale.dp(CORNER)))
                    .border(width = 1.dp, color = Color(0xFFE6E8EB), shape = RoundedCornerShape(scale.dp(CORNER)))
                    .clickable(enabled = label.isNotEmpty()) {
                        when (label) {
                            "⌫" -> onBackspace()
                            "−" -> onMinus()
                            "+" -> onPlus()
                            "." -> onDot()
                            "再记" -> onAgain()
                            else -> onNumber(label)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (label.isNotEmpty()) {
                    if (label == "⌫") {
                        Icon(
                            imageVector = Icons.Filled.Backspace,
                            contentDescription = "删除",
                            tint = Color.Black,
                            modifier = Modifier.size(scale.dp(48))
                        )
                    } else {
                        val fs = when (label) {
                            "再记" -> 33f
                            "−", "+" -> 48f
                            else -> 54f
                        }
                        Text(label, color = Color.Black, fontSize = (fs * scale.s / scale.density).sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveButtonAbsolute(scale: Scale, onSave: () -> Unit) {
    Box(
        modifier = Modifier
            .offset { IntOffset(scale.px(SAVE_X1), scale.px(SAVE_Y1)) }
            .size(width = scale.dp(SAVE_W), height = scale.dp(SAVE_H))
            .background(AccentRed, RoundedCornerShape(scale.dp(CORNER)))
            .clickable { onSave() },
        contentAlignment = Alignment.Center
    ) {
        Text("保存", color = White, fontSize = (33f * scale.s / scale.density).sp, fontWeight = FontWeight.Medium)
    }
}

// 临时数据类
data class TempCategory(
    val id: String,
    val name: String
)

val expenseCategories = listOf(
    TempCategory("grocery", "买菜"),
    TempCategory("breakfast", "早餐"),
    TempCategory("dining", "下馆子"),
    TempCategory("condiments", "柴米油盐"),
    TempCategory("fruit", "水果"),
    TempCategory("snack", "零食"),
    TempCategory("beverage", "饮料"),
    TempCategory("clothing", "衣服"),
    TempCategory("transport", "交通"),
    TempCategory("travel", "旅行"),
    TempCategory("phone_bill", "话费网费"),
    TempCategory("tobacco", "烟酒"),
    TempCategory("study", "学习"),
    TempCategory("daily", "日用品"),
    TempCategory("housing", "住房"),
    TempCategory("beauty", "美妆"),
    TempCategory("medical", "医疗"),
    TempCategory("red_packet", "发红包"),
    TempCategory("entertainment", "娱乐"),
    TempCategory("gift", "请客送礼"),
    TempCategory("electronics", "电器数码"),
    TempCategory("utilities", "水电煤"),
    TempCategory("other", "其它"),
    TempCategory("custom_1", "崔芳榕专用"),
    TempCategory("supermarket", "超市")
)

val incomeCategories = listOf(
    TempCategory("salary", "工资"),
    TempCategory("bonus", "奖金"),
    TempCategory("investment", "投资"),
    TempCategory("part_time", "兼职"),
    TempCategory("red_packet_in", "红包")
)

// 辅助函数
// 旧方法已替换为 demoAppendInput/demoBackspace/demoAppendOperator

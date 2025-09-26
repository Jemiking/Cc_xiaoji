package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

// import android.app.Activity (removed with in-app export)
import android.view.View
import android.view.ViewGroup
// import android.widget.FrameLayout (removed with in-app export)
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
// removed: FileDownload icon import
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ccxiaoji.common.util.DeviceUtils
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar
import androidx.core.view.ViewCompat
 

enum class ReportMode { Month, Year }

@Composable
fun BookReportV2Screen(navController: NavController) {
    SetStatusBar(color = ReportTokens.Palette.Card)

    var mode by remember { mutableStateOf(ReportMode.Month) }
    android.util.Log.d("BookReport_DEBUG", "🔄 Screen recompose, current mode: $mode")
    val periodLabel = when (mode) {
        ReportMode.Month -> "2021-05"
        ReportMode.Year -> "2024"
    }
    var dailyTab by remember { mutableStateOf(DailyTab.Expense) }
    var categoryDim by remember { mutableStateOf(CategoryDimension.Expense) }
    val context = LocalContext.current

    // 筛选对话框状态
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(FilterState()) }

    val overview = remember(mode) {
        android.util.Log.d("BookReport_DEBUG", "💰 Loading overview data for mode: $mode")
        when (mode) {
            ReportMode.Month -> {
                android.util.Log.d("BookReport_DEBUG", "📊 Using MONTH overview data")
                OverviewState(
                    expense = "\u00A510908.30",   // 去除千分位，统一视觉为黑色金额
                    income = "\u00A5260.00",
                    balance = "-\u00A510648.30",
                    averageDailyExpense = "\u00A5351.88",
                    extraMetrics = listOf(
                        Metric(label = "\u4FE1\u7528\u5361\u8FD8\u6B3E", value = "\u00A5380.00")
                    )
                )
            }
            ReportMode.Year -> {
                android.util.Log.d("BookReport_DEBUG", "📊 Using YEAR overview data")
                demoYearOverviewData()
            }
        }
    }
    val dailySeries = remember(mode) {
        when (mode) {
            ReportMode.Month -> demoDailySeries()
            ReportMode.Year -> demoYearSeries()
        }
    }
    val categoryState = remember(mode) {
        android.util.Log.d("BookReport_DEBUG", "🍰 Loading category data for mode: $mode")
        when (mode) {
            ReportMode.Month -> {
                android.util.Log.d("BookReport_DEBUG", "📈 Using MONTH category data")
                demoCategoryState()
            }
            ReportMode.Year -> {
                android.util.Log.d("BookReport_DEBUG", "📈 Using YEAR category data")
                demoYearCategoryState()
            }
        }
    }
    val tableRows = remember(mode) {
        when (mode) {
            ReportMode.Month -> demoDailyRows()
            ReportMode.Year -> demoMonthlyRows()
        }
    }

    Scaffold(
        containerColor = ReportTokens.Palette.PageBackground,
        topBar = {
            Surface(color = ReportTokens.Palette.Card, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = ReportTokens.Metrics.PagePadding)
                        .height(56.dp)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = ReportTokens.Palette.TextPrimary)
                    }

                    ModeSegmentedCN(
                        isMonth = mode == ReportMode.Month,
                        onChange = { isMonth ->
                            android.util.Log.d("BookReport_DEBUG", "🔄 Segmented control clicked! isMonth=$isMonth, current mode=$mode")
                            val newMode = if (isMonth) ReportMode.Month else ReportMode.Year
                            android.util.Log.d("BookReport_DEBUG", "🔄 Changing mode from $mode to $newMode")
                            mode = newMode
                            android.util.Log.d("BookReport_DEBUG", "✅ Mode changed successfully to $mode")
                        }
                    )

                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = null, tint = ReportTokens.Palette.TextPrimary)
                    }

                    
                }
            }
        }
    ) { padding ->
        // 鏍规嵁璁惧涓庣郴缁熷垽鏂槸鍚﹀惎鐢ㄤ簰鎿嶄綔瀹瑰櫒浠ユ彁鍗囩郴缁熼暱鎴睆璇嗗埆锛堜粎璋冭瘯椤甸潰榛樿寮€鍚級
        val ctx = LocalContext.current
        val useInterop = remember(ctx) { DeviceUtils.isLongShotInteropRecommended(ctx) }
        if (useInterop) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val host = androidx.core.widget.NestedScrollView(context).apply {
                        isFillViewport = true
                        overScrollMode = View.OVER_SCROLL_ALWAYS
                        isVerticalScrollBarEnabled = true
                    }
                    val composeView = androidx.compose.ui.platform.ComposeView(context)
                    host.addView(
                        composeView,
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    )
                    // 返回一个Pair，包含host和composeView，以便在update中访问
                    host.tag = composeView
                    host
                },
                update = { host ->
                    // 每次重组时更新ComposeView的内容
                    val composeView = host.tag as androidx.compose.ui.platform.ComposeView
                    android.util.Log.d("BookReport_DEBUG", "🔄 Updating AndroidView content for mode: $mode")
                    composeView.setContent {
                        MaterialTheme {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ReportTokens.Palette.PageBackground)
                                    .padding(padding)
                            ) {
                                DateRow(periodLabel, onPrev = { }, onNext = { }, onPick = { })
                                OverviewCard(
                                    state = overview,
                                    modifier = Modifier
                                        .padding(horizontal = ReportTokens.Metrics.PagePadding)
                                        .fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                DailyBarChartCard(
                                    series = dailySeries,
                                    tab = dailyTab,
                                    onTabChange = { dailyTab = it },
                                    periodLabel = periodLabel,
                                    modifier = Modifier
                                        .padding(horizontal = ReportTokens.Metrics.PagePadding)
                                        .fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                CategoryDonutCard(
                                    state = categoryState,
                                    dimension = categoryDim,
                                    onDimensionChange = { categoryDim = it },
                                    modifier = Modifier
                                        .padding(horizontal = ReportTokens.Metrics.PagePadding)
                                        .fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                DailyTableCard(
                                    rows = tableRows,
                                    modifier = Modifier
                                        .padding(horizontal = ReportTokens.Metrics.PagePadding)
                                        .fillMaxWidth()
                                )
                                Spacer(Modifier.height(72.dp))
                            }
                        }
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = padding
            ) {
                item { DateRow(periodLabel, onPrev = { }, onNext = { }, onPick = { }) }
                item(key = "${mode}_overview_${overview.expense}") {
                    // 使用key参数强制重组
                    OverviewCard(
                        state = overview,
                        modifier = Modifier
                            .padding(horizontal = ReportTokens.Metrics.PagePadding)
                            .fillMaxWidth()
                    )
                }
                item { Spacer(Modifier.height(10.dp)) }
                item(key = "${mode}_daily_chart") {
                    DailyBarChartCard(
                        series = dailySeries,
                        tab = dailyTab,
                        onTabChange = { dailyTab = it },
                        periodLabel = periodLabel,
                        modifier = Modifier
                            .padding(horizontal = ReportTokens.Metrics.PagePadding)
                            .fillMaxWidth()
                    )
                }
                item { Spacer(Modifier.height(10.dp)) }
                item(key = "${mode}_category_${categoryState.entries.firstOrNull()?.name}") {
                    // 使用key参数强制重组
                    CategoryDonutCard(
                        state = categoryState,
                        dimension = categoryDim,
                        onDimensionChange = { categoryDim = it },
                        modifier = Modifier
                            .padding(horizontal = ReportTokens.Metrics.PagePadding)
                            .fillMaxWidth()
                    )
                }
                item { Spacer(Modifier.height(10.dp)) }
                item {
                    DailyTableCard(
                        rows = tableRows,
                        modifier = Modifier
                            .padding(horizontal = ReportTokens.Metrics.PagePadding)
                            .fillMaxWidth()
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    // 显示筛选对话框
    FilterDialog(
        isVisible = showFilterDialog,
        currentFilter = filterState,
        onDismiss = { showFilterDialog = false },
        onConfirm = { newFilter ->
            filterState = newFilter
            showFilterDialog = false
            // 这里可以根据筛选条件更新数据
            android.util.Log.d("BookReport_DEBUG", "Filter applied: $filterState")
        }
    )
}

// removed legacy ModeSegmented (garbled labels)

@Composable
private fun SegItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) ReportTokens.Palette.TextPrimary else ReportTokens.Palette.TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun DateRow(
    periodLabel: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReportTokens.Metrics.PagePadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = ReportTokens.Palette.TextPrimary)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ReportTokens.Palette.Card)
                .clickable(onClick = onPick)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(periodLabel, color = ReportTokens.Palette.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = ReportTokens.Palette.TextMuted)
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = ReportTokens.Palette.TextPrimary)
        }
    }
}

// ---------------- Demo data for Preview -----------------

private fun demoDailySeries(): DailySeries {
    val days = 31
    val expense = List(days) { i -> if (i == 14) 820f else 120f + (i % 5) * 18f }
    val income = List(days) { i -> if (i % 10 == 0) 160f else 30f }
    val total = expense.zip(income) { e, inc -> e + inc }
    return DailySeries(expense, income, total)
}

private fun demoCategoryState(): CategoryState {
    android.util.Log.d("BookReport_DEBUG", "🎯 CALLED: demoCategoryState() - Returning MONTH category data")
    val colors = ReportTokens.Palette.Category
    val entries = listOf(
        CategoryEntry("电器数码", "¥5252.99", 0.4816f, colors[0], CategoryIcon.Electronics),
        CategoryEntry("学习", "¥1495.22", 0.1371f, colors[1], CategoryIcon.Study),
        CategoryEntry("衣服", "¥986.22", 0.0904f, colors[2], CategoryIcon.Clothes),
        CategoryEntry("下馆子", "¥679.82", 0.0623f, colors[3], CategoryIcon.Restaurant),
        CategoryEntry("超市", "¥550.80", 0.0505f, colors[4], CategoryIcon.Supermarket),
        CategoryEntry("买菜", "¥402.19", 0.0369f, colors[5], CategoryIcon.Groceries),
        CategoryEntry("其它", "¥210.74", 0.0193f, colors[6], CategoryIcon.Other),
        CategoryEntry("请客送礼", "¥200.00", 0.0183f, colors[7], CategoryIcon.Gift),
        CategoryEntry("娱乐", "¥180.13", 0.0165f, colors[8], CategoryIcon.Entertainment),
        CategoryEntry("交通", "¥177.40", 0.0163f, colors[9], CategoryIcon.Transport),
        CategoryEntry("医疗", "¥152.72", 0.0140f, colors[10], CategoryIcon.Medical),
        CategoryEntry("日用品", "¥147.94", 0.0136f, colors[11], CategoryIcon.Daily),
        CategoryEntry("零食", "¥143.08", 0.0131f, colors[0], CategoryIcon.Snacks),
        CategoryEntry("早餐", "¥113.50", 0.0104f, colors[1], CategoryIcon.Breakfast),
        CategoryEntry("话费网费", "¥103.00", 0.0094f, colors[2], CategoryIcon.Phone),
        CategoryEntry("柴米油盐", "¥55.05", 0.0050f, colors[3], CategoryIcon.Essentials),
        CategoryEntry("住房", "¥43.50", 0.0040f, colors[4], CategoryIcon.Housing),
        CategoryEntry("饮料", "¥14.00", 0.0013f, colors[5], CategoryIcon.Drinks)
    )
    return CategoryState(entries).also {
        android.util.Log.d("BookReport_DEBUG", "🍰 MONTH Categories: Top3 = ${it.entries.take(3).map { "${it.name}(${(it.percent*100).toInt()}%)" }}")
    }
}


private fun demoDailyRows(): List<DailyRow> = listOf(
    DailyRow("05-31", "\u00A50.00", "\u00A51,818.52", "-\u00A51,818.52"),
    DailyRow("05-30", "\u00A50.00", "\u00A5862.20", "-\u00A5862.20"),
    DailyRow("05-29", "\u00A50.00", "\u00A5320.50", "-\u00A5320.50"),
    DailyRow("05-28", "\u00A50.00", "\u00A5129.05", "-\u00A5129.05"),
    DailyRow("05-27", "\u00A50.00", "\u00A5492.05", "-\u00A5492.05"),
    DailyRow("05-26", "\u00A50.00", "\u00A5575.99", "-\u00A5575.99"),
    DailyRow("05-25", "\u00A50.00", "\u00A5198.00", "-\u00A5198.00"),
    DailyRow("05-24", "\u00A50.00", "\u00A545.30", "-\u00A545.30"),
    DailyRow("05-23", "\u00A50.00", "\u00A5268.59", "-\u00A5268.59"),
    DailyRow("05-22", "\u00A50.00", "\u00A5550.89", "-\u00A5550.89")
)

// ---------------- 年模式专用数据函数 -----------------

private fun demoYearOverviewData(): OverviewState {
    android.util.Log.d("BookReport_DEBUG", "📊 CALLED: demoYearOverviewData() - Returning YEAR data")
    return OverviewState(
        expense = "¥79200.32",
        income = "¥34834.31",
        balance = "-¥44575.01",
        averageDailyExpense = "¥8801.04",
        extraMetrics = listOf(
            Metric(label = "平均月支出", value = "¥778.02"),
            Metric(label = "信用卡还款", value = "¥10499.51"),
            Metric(label = "其他", value = "¥210.68")
        )
    ).also {
        android.util.Log.d("BookReport_DEBUG", "💰 YEAR Overview: expense=${it.expense}, income=${it.income}")
    }
    return OverviewState(
        expense = "\u00A579200.32",
        income = "\u00A534834.31",
        balance = "-\u00A544575.01",
        averageDailyExpense = "\u00A58801.04",
        extraMetrics = listOf(
            Metric(label = "\u5E73\u5747\u6708\u652F\u51FA", value = "\u00A5778.02"),
            Metric(label = "\u4FE1\u7528\u5361\u8FD8\u6B3E", value = "\u00A510499.51"),
            Metric(label = "\u5176\u4ED6", value = "\u00A5210.68")
        )
    )
}

private fun demoYearCategoryState(): CategoryState {
    android.util.Log.d("BookReport_DEBUG", "🎯 CALLED: demoYearCategoryState() - Returning YEAR category data")
    val colors = ReportTokens.Palette.Category
    val entries = listOf(
        CategoryEntry("住房", "¥19008.08", 0.2400f, colors[0], CategoryIcon.Housing),
        CategoryEntry("其他", "¥15602.46", 0.1970f, colors[1], CategoryIcon.Other),
        CategoryEntry("下馆子", "¥7365.63", 0.0930f, colors[2], CategoryIcon.Restaurant),
        CategoryEntry("交通", "¥6494.43", 0.0820f, colors[3], CategoryIcon.Transport),
        CategoryEntry("话费网费", "¥5464.82", 0.0690f, colors[4], CategoryIcon.Phone),
        CategoryEntry("请客送礼", "¥5227.22", 0.0660f, colors[5], CategoryIcon.Gift),
        CategoryEntry("日用品", "¥3009.61", 0.0380f, colors[6], CategoryIcon.Daily),
        CategoryEntry("学习用品", "¥2296.81", 0.0290f, colors[7], CategoryIcon.Study),
        CategoryEntry("买菜", "¥2217.61", 0.0280f, colors[8], CategoryIcon.Groceries),
        CategoryEntry("医疗", "¥2059.20", 0.0260f, colors[9], CategoryIcon.Medical),
        CategoryEntry("衣服", "¥1346.41", 0.0170f, colors[10], CategoryIcon.Clothes),
        CategoryEntry("柴米油盐", "¥633.60", 0.0080f, colors[11], CategoryIcon.Essentials),
        CategoryEntry("家具", "¥554.40", 0.0070f, colors[0], CategoryIcon.Other),
        CategoryEntry("学费", "¥396.00", 0.0050f, colors[1], CategoryIcon.Study),
        CategoryEntry("零食", "¥237.60", 0.0030f, colors[2], CategoryIcon.Snacks),
        CategoryEntry("娱乐服务", "¥158.40", 0.0020f, colors[3], CategoryIcon.Entertainment),
        CategoryEntry("租金", "¥79.20", 0.0010f, colors[4], CategoryIcon.Housing)
    )
    return CategoryState(entries).also {
        android.util.Log.d("BookReport_DEBUG", "🍰 YEAR Categories: Top3 = ${it.entries.take(3).map { "${it.name}(${(it.percent*100).toInt()}%)" }}")
    }
}

private fun demoMonthlyRows(): List<DailyRow> = listOf(
    DailyRow("10月", "\u00A5285.59", "\u00A56212.14", "-\u00A55926.55"),
    DailyRow("9月", "\u00A53500.00", "\u00A51530.85", "\u00A51969.15"),
    DailyRow("8月", "\u00A57000.00", "\u00A57241.35", "-\u00A5241.35"),
    DailyRow("7月", "\u00A50.00", "\u00A570.98", "-\u00A570.98"),
    DailyRow("6月", "\u00A5200.00", "\u00A59098.30", "-\u00A58898.30"),
    DailyRow("5月", "\u00A50.00", "\u00A50.00", "\u00A50.00"),
    DailyRow("4月", "\u00A50.00", "\u00A53538.17", "-\u00A53538.17"),
    DailyRow("3月", "\u00A50.00", "\u00A50.00", "\u00A50.00"),
    DailyRow("2月", "\u00A50.00", "\u00A58952.17", "-\u00A58952.17"),
    DailyRow("1月", "\u00A52086.27", "\u00A56536.68", "-\u00A54450.41"),
    DailyRow("平月", "\u00A53570.48", "\u00A58801.04", "-\u00A55230.56")
)

private fun demoYearSeries(): DailySeries {
    val months = 12
    val expense = List(months) { i ->
        when(i) {
            0 -> 6536.68f  // 1月
            1 -> 8952.17f  // 2月
            2 -> 0f        // 3月
            3 -> 3538.17f  // 4月
            4 -> 0f        // 5月
            5 -> 9098.30f  // 6月
            6 -> 70.98f    // 7月
            7 -> 7241.35f  // 8月
            8 -> 1530.85f  // 9月
            9 -> 6212.14f  // 10月
            10 -> 0f       // 11月
            11 -> 0f       // 12月
            else -> 0f
        }
    }
    val income = List(months) { i ->
        when(i) {
            0 -> 2086.27f  // 1月
            1 -> 0f        // 2月
            2 -> 0f        // 3月
            3 -> 0f        // 4月
            4 -> 0f        // 5月
            5 -> 200.00f   // 6月
            6 -> 0f        // 7月
            7 -> 7000.00f  // 8月
            8 -> 3500.00f  // 9月
            9 -> 285.59f   // 10月
            10 -> 0f       // 11月
            11 -> 0f       // 12月
            else -> 0f
        }
    }
    val total = expense.zip(income) { e, inc -> e + inc }
    return DailySeries(expense, income, total)
}

// ---------------- Preview -----------------

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun BookReportV2ScreenPreview() {
    val nav = rememberNavController()
    MaterialTheme {
        BookReportV2Screen(nav)
    }
}



@Composable
private fun ModeSegmentedCN(isMonth: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius))
            .background(ReportTokens.Palette.ChipContainer)
            .border(width = 1.dp, color = ReportTokens.Palette.Divider, shape = RoundedCornerShape(ReportTokens.Metrics.SegmentedRadius))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SegItem(label = "\u6708", selected = isMonth) { onChange(true) }
        SegItem(label = "\u5E74", selected = !isMonth) { onChange(false) }
    }
}









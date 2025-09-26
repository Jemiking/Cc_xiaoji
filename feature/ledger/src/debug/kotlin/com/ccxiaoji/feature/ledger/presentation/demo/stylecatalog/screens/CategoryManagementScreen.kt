package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.ccxiaoji.common.util.DeviceUtils
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar
import kotlinx.coroutines.launch

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val iconColor: Color,
    val isDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(navController: NavController) {
    SetStatusBar(color = Color.White)

    var selectedTab by remember { mutableStateOf(0) }
    var showTipsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val expenseCategories = remember {
        listOf(
            CategoryItem("买菜", Icons.Outlined.ShoppingCart, Color(0xFF4CAF50)),
            CategoryItem("早餐", Icons.Outlined.Restaurant, Color(0xFFFF9800)),
            CategoryItem("下饭子", Icons.Outlined.RamenDining, Color(0xFFE91E63)),
            CategoryItem("柴米油盐", Icons.Outlined.Kitchen, Color(0xFF9C27B0)),
            CategoryItem("水果", Icons.Outlined.Dining, Color(0xFF2196F3)),
            CategoryItem("零食", Icons.Outlined.Cookie, Color(0xFF00BCD4)),
            CategoryItem("饮料", Icons.Outlined.LocalDrink, Color(0xFF009688)),
            CategoryItem("衣服", Icons.Outlined.Checkroom, Color(0xFF607D8B)),
            CategoryItem("交通", Icons.Outlined.DirectionsBus, Color(0xFF795548)),
            CategoryItem("旅行", Icons.Outlined.Flight, Color(0xFF3F51B5)),
            CategoryItem("话费网费", Icons.Outlined.PhoneAndroid, Color(0xFFCDDC39)),
            CategoryItem("烟酒", Icons.Outlined.SmokingRooms, Color(0xFFFFEB3B)),
            CategoryItem("学习", Icons.Outlined.School, Color(0xFFFFC107)),
            CategoryItem("日用品", Icons.Outlined.Home, Color(0xFFFF5722)),
            CategoryItem("住房", Icons.Outlined.House, Color(0xFF673AB7)),
            CategoryItem("美妆", Icons.Outlined.Brush, Color(0xFFE91E63)),
            CategoryItem("医疗", Icons.Outlined.MedicalServices, Color(0xFF4CAF50)),
            CategoryItem("发红包", Icons.Outlined.Redeem, Color(0xFFF44336)),
            CategoryItem("娱乐", Icons.Outlined.SportsEsports, Color(0xFF9C27B0)),
            CategoryItem("电器数码", Icons.Outlined.Devices, Color(0xFF2196F3)),
            CategoryItem("其它", Icons.Outlined.MoreHoriz, Color(0xFF9E9E9E)),
            CategoryItem("维芳稻专用", Icons.Outlined.Agriculture, Color(0xFF8BC34A)),
            CategoryItem("超市", Icons.Outlined.Store, Color(0xFFFF9800))
        )
    }

    val incomeCategories = remember {
        listOf(
            CategoryItem("工资", Icons.Outlined.AccountBalance, Color(0xFF2196F3)),
            CategoryItem("生活费", Icons.Outlined.AttachMoney, Color(0xFF4CAF50)),
            CategoryItem("收红包", Icons.Outlined.CardGiftcard, Color(0xFFFF5722)),
            CategoryItem("外快", Icons.Outlined.WorkOutline, Color(0xFF9C27B0)),
            CategoryItem("股票基金", Icons.Outlined.TrendingUp, Color(0xFF00BCD4)),
            CategoryItem("其它", Icons.Outlined.MoreHoriz, Color(0xFF9E9E9E), isDefault = true),
            CategoryItem("转账", Icons.Outlined.SwapHoriz, Color(0xFF607D8B))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            modifier = Modifier.width(200.dp),
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color.Black,
                                    height = 2.dp
                                )
                            },
                            divider = { }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    "支出",
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Medium else FontWeight.Normal,
                                    color = if (selectedTab == 0) Color.Black else Color(0xFF9CA3AF)
                                )
                            }
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    "收入",
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Medium else FontWeight.Normal,
                                    color = if (selectedTab == 1) Color.Black else Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showTipsDialog = true }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = Color(0xFFF7F8FA)
    ) { paddingValues ->
        // 检测设备并决定使用哪种滚动模式
        val context = LocalContext.current
        val useInterop = remember(context) {
            DeviceUtils.isLongShotInteropRecommended(context)
        }

        if (useInterop) {
            // 小米系设备：使用AndroidView + NestedScrollView实现长截屏支持
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                factory = { ctx ->
                    val scrollView = androidx.core.widget.NestedScrollView(ctx).apply {
                        isFillViewport = true
                        overScrollMode = View.OVER_SCROLL_ALWAYS
                        isVerticalScrollBarEnabled = true
                    }
                    val composeView = androidx.compose.ui.platform.ComposeView(ctx)
                    scrollView.addView(
                        composeView,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    )
                    scrollView.tag = composeView
                    scrollView
                },
                update = { scrollView ->
                    val composeView = scrollView.tag as androidx.compose.ui.platform.ComposeView
                    composeView.setContent {
                        MaterialTheme {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF7F8FA))
                                    .padding(bottom = 88.dp)
                            ) {
                                Text(
                                    text = "长按一级或者二级分类可进行排序，单击可修改",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )

                                val categories = if (selectedTab == 0) expenseCategories else incomeCategories
                                categories.forEach { category ->
                                    CategoryListItem(category)
                                }
                            }
                        }
                    }
                }
            )
        } else {
            // 其他设备：使用标准LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                item {
                    Text(
                        text = "长按一级或者二级分类可进行排序，单击可修改",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                val categories = if (selectedTab == 0) expenseCategories else incomeCategories
                items(categories) { category ->
                    CategoryListItem(category)
                }
            }
        }
    }

    if (showTipsDialog) {
        BottomTipsSheet(
            onDismiss = { showTipsDialog = false }
        )
    }
}

@Composable
private fun CategoryListItem(category: CategoryItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(category.iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = category.name,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            if (category.isDefault) {
                Text(
                    text = "默认",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            IconButton(
                onClick = { },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomTipsSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFF000000),
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "提示",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TipItem(
                title = "二级分类",
                description = "分类支持两级，点开一级可以在下面添加二级"
            )

            Spacer(Modifier.height(20.dp))

            TipItem(
                title = "排序/修改",
                description = "长按一级或者二级分类可进行排序，单击可修改"
            )

            Spacer(Modifier.height(20.dp))

            TipItem(
                title = "保持简洁",
                description = "建议分类不要设置太详细，二级分类非必须，如果设置分类太多，会增加操作复杂度"
            )
        }
    }
}

@Composable
private fun TipItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            lineHeight = 20.sp
        )
    }
}
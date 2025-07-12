package com.ccxiaoji.app.presentation.ui.demo

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.core.view.WindowCompat
import com.ccxiaoji.ui.theme.DiscordColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.app.Activity

// 最优顶部间距百分比
private const val OPTIMAL_TOP_PADDING_PERCENTAGE = 26f

/**
 * Discord风格Demo V2 - 基于正确的架构理解重新实现
 * 
 * 架构设计：
 * - APP级别：底部导航栏（主页/通知/您）
 * - 主页内部：左侧模块栏（72dp宽）
 * 
 * 状态栏处理：
 * - 使用WindowInsets精确控制
 * - 固定26%的最优顶部间距
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscordStyleDemoV2Screen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    var isDarkTheme by remember { mutableStateOf(true) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // 添加手势检测状态
    var dragOffset by remember { mutableStateOf(0f) }
    
    // 设置edge-to-edge
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
        }
    }
    
    // 计算最优顶部间距
    val topPadding = calculateOptimalTopPadding()
    
    // 状态栏颜色控制
    StatusBarEffect(selectedTab, isDarkTheme)
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                else DiscordColors.Light.BackgroundPrimary
            )
            // 添加手势检测：左右滑动切换标签
            .pointerInput(selectedTab) {
                detectDragGestures(
                    onDragEnd = {
                        // 如果滑动距离超过阈值，切换标签
                        when {
                            dragOffset > 100 && selectedTab > 0 -> {
                                selectedTab--
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            dragOffset < -100 && selectedTab < 2 -> {
                                selectedTab++
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        dragOffset = 0f
                    }
                ) { _, dragAmount ->
                    dragOffset += dragAmount.x
                }
            },
        bottomBar = {
            DiscordBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { 
                    selectedTab = it
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                isDarkTheme = isDarkTheme
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = tween(300)) { width -> width } + 
                     fadeIn(animationSpec = tween(300))) togetherWith
                    (slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + 
                     fadeOut(animationSpec = tween(300)))
                } else {
                    (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + 
                     fadeIn(animationSpec = tween(300))) togetherWith
                    (slideOutHorizontally(animationSpec = tween(300)) { width -> width } + 
                     fadeOut(animationSpec = tween(300)))
                }.using(SizeTransform(clip = false))
            },
            label = "tabContent"
        ) { tab ->
            when (tab) {
                0 -> DiscordHomeScreen(
                    paddingValues = paddingValues, 
                    isDarkTheme = isDarkTheme,
                    topPadding = topPadding,
                    onThemeToggle = { isDarkTheme = !isDarkTheme }
                )
                1 -> DiscordNotificationScreen(paddingValues, isDarkTheme, topPadding)
                2 -> DiscordProfileScreen(paddingValues, isDarkTheme, topPadding)
            }
        }
    }
}

/**
 * 计算最优顶部间距
 */
@Composable
private fun calculateOptimalTopPadding(): Dp {
    val density = LocalDensity.current
    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    return statusBarHeight * (OPTIMAL_TOP_PADDING_PERCENTAGE / 100f)
}

/**
 * 状态栏效果控制
 */
@Composable
private fun StatusBarEffect(selectedTab: Int, isDarkTheme: Boolean) {
    val view = LocalView.current
    val darkIcons = !isDarkTheme && selectedTab == 2 // 个人中心页面且浅色主题时使用深色图标
    
    DisposableEffect(selectedTab, isDarkTheme) {
        val window = (view.context as? Activity)?.window
        window?.let {
            val insetsController = WindowCompat.getInsetsController(it, view)
            insetsController.isAppearanceLightStatusBars = darkIcons
        }
        
        onDispose { }
    }
}

/**
 * Discord风格主页 - 使用最优状态栏方案
 */
@Composable
private fun DiscordHomeScreen(
    paddingValues: PaddingValues,
    isDarkTheme: Boolean,
    topPadding: Dp,
    onThemeToggle: () -> Unit
) {
    var selectedModule by remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundDeepest 
                else DiscordColors.Light.BackgroundDeepest
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = topPadding, // 使用优化后的顶部间距
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            // 左侧模块栏
            DiscordModuleBar(
                selectedModule = selectedModule,
                onModuleSelected = { selectedModule = it },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.width(72.dp)
            )
            
            // 右侧内容区
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 16.dp)),
                color = if (isDarkTheme) DiscordColors.Dark.BackgroundSecondary 
                       else DiscordColors.Light.BackgroundSecondary,
                shadowElevation = 4.dp
            ) {
                Column {
                    // 模块标题栏
                    ModuleHeader(
                        moduleName = when (selectedModule) {
                            0 -> "Dashboard"
                            1 -> "记账"
                            2 -> "待办"
                            3 -> "习惯"
                            4 -> "排班"
                            5 -> "计划"
                            else -> "Dashboard"
                        },
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 模块内容
                    when (selectedModule) {
                        0 -> DashboardContent(isDarkTheme)
                        1 -> LedgerModuleContent(isDarkTheme)
                        2 -> TodoModuleContent(isDarkTheme)
                        3 -> HabitModuleContent(isDarkTheme)
                        4 -> ScheduleModuleContent(isDarkTheme)
                        5 -> PlanModuleContent(isDarkTheme)
                        else -> DashboardContent(isDarkTheme)
                    }
                }
            }
        }
    }
}

/**
 * 模块标题栏
 */
@Composable
private fun ModuleHeader(
    moduleName: String,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when (moduleName) {
                    "Dashboard" -> Icons.Default.Dashboard
                    "记账" -> Icons.Default.AccountBalanceWallet
                    "待办" -> Icons.Default.CheckCircle
                    "习惯" -> Icons.Default.Psychology
                    "排班" -> Icons.Default.CalendarMonth
                    "计划" -> Icons.Default.Timeline
                    else -> Icons.Default.Dashboard
                },
                contentDescription = moduleName,
                tint = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                       else DiscordColors.Light.TextNormal
            )
            Text(
                text = moduleName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                       else DiscordColors.Light.TextPrimary
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { /* 搜索功能 */ }) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                           else DiscordColors.Light.TextMuted
                )
            }
            IconButton(onClick = onThemeToggle) {
                Icon(
                    if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "切换主题",
                    tint = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                           else DiscordColors.Light.TextMuted
                )
            }
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
private fun DiscordBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDarkTheme: Boolean
) {
    NavigationBar(
        containerColor = if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                        else DiscordColors.Light.BackgroundPrimary,
        contentColor = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                       else DiscordColors.Light.TextNormal,
        modifier = Modifier.height(72.dp)
    ) {
        val tabs = listOf(
            Triple(Icons.Default.Home, "主页", 0),
            Triple(Icons.Default.Notifications, "通知", 4),
            Triple(Icons.Default.Person, "您", 0)
        )
        
        tabs.forEachIndexed { index, (icon, label, badge) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (badge > 0) {
                                Badge(
                                    containerColor = DiscordColors.Red,
                                    contentColor = Color.White
                                ) {
                                    Text(badge.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = { 
                    Text(
                        text = label,
                        fontSize = 12.sp
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DiscordColors.Blurple,
                    selectedTextColor = DiscordColors.Blurple,
                    unselectedIconColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                         else DiscordColors.Light.TextMuted,
                    unselectedTextColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                         else DiscordColors.Light.TextMuted,
                    indicatorColor = DiscordColors.Blurple.copy(alpha = 0.1f)
                )
            )
        }
    }
}

/**
 * 通知页面
 */
@Composable
private fun DiscordNotificationScreen(
    paddingValues: PaddingValues,
    isDarkTheme: Boolean,
    topPadding: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                else DiscordColors.Light.BackgroundPrimary
            )
            .padding(
                top = topPadding,
                bottom = paddingValues.calculateBottomPadding()
            )
    ) {
        // 标题
        Text(
            "通知中心",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                   else DiscordColors.Light.TextPrimary,
            modifier = Modifier.padding(16.dp)
        )
        
        // 通知列表
        NotificationItem(
            title = "任务提醒",
            content = "今日待办事项还有3项未完成",
            time = "10分钟前",
            isDarkTheme = isDarkTheme
        )
        NotificationItem(
            title = "习惯打卡",
            content = "别忘了完成今日的运动目标",
            time = "2小时前",
            isDarkTheme = isDarkTheme
        )
        NotificationItem(
            title = "记账提醒",
            content = "昨日有一笔支出未分类",
            time = "昨天",
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * 通知项
 */
@Composable
private fun NotificationItem(
    title: String,
    content: String,
    time: String,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DiscordColors.Dark.SurfaceDefault 
                           else DiscordColors.Light.SurfaceDefault
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                           else DiscordColors.Light.TextPrimary
                )
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                           else DiscordColors.Light.TextMuted
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                color = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                       else DiscordColors.Light.TextNormal
            )
        }
    }
}

/**
 * 个人中心页面
 */
@Composable
private fun DiscordProfileScreen(
    paddingValues: PaddingValues,
    isDarkTheme: Boolean,
    topPadding: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundSecondary 
                else DiscordColors.Light.BackgroundSecondary
            )
            .padding(
                top = topPadding,
                bottom = paddingValues.calculateBottomPadding()
            )
    ) {
        // 用户信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DiscordColors.Blurple
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "用户昵称",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "user@example.com",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        // 设置选项
        SettingItem(
            icon = Icons.Default.Settings,
            title = "设置",
            isDarkTheme = isDarkTheme
        )
        SettingItem(
            icon = Icons.Default.Security,
            title = "隐私与安全",
            isDarkTheme = isDarkTheme
        )
        SettingItem(
            icon = Icons.Default.Help,
            title = "帮助与支持",
            isDarkTheme = isDarkTheme
        )
        SettingItem(
            icon = Icons.Default.Info,
            title = "关于",
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * 设置项
 */
@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    isDarkTheme: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 处理点击 */ }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                       else DiscordColors.Light.TextNormal
            )
            Text(
                text = title,
                fontSize = 16.sp,
                color = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                       else DiscordColors.Light.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                       else DiscordColors.Light.TextMuted
            )
        }
    }
}

/**
 * Discord风格模块栏
 */
@Composable
private fun DiscordModuleBar(
    selectedModule: Int,
    onModuleSelected: (Int) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val modules = listOf(
        ModuleItem(Icons.Default.Dashboard, "首页", DiscordColors.Blurple, null),
        ModuleItem(Icons.Default.AccountBalanceWallet, "记账", DiscordColors.Green, 3),
        ModuleItem(Icons.Default.CheckCircle, "待办", DiscordColors.Yellow, 2),
        ModuleItem(Icons.Default.Psychology, "习惯", DiscordColors.Fuchsia, 1),
        ModuleItem(Icons.Default.CalendarMonth, "排班", DiscordColors.Red, null),
        ModuleItem(Icons.Default.Timeline, "计划", DiscordColors.Blurple, null)
    )
    
    val hapticFeedback = LocalHapticFeedback.current
    val indicatorAnimation = remember { Animatable(selectedModule.toFloat()) }
    
    LaunchedEffect(selectedModule) {
        indicatorAnimation.animateTo(
            targetValue = selectedModule.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundDeepest 
                else DiscordColors.Light.BackgroundDeepest
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 动画指示器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp * modules.size + 12.dp * (modules.size - 1))
        ) {
            // 选中指示器动画
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .offset(
                        x = 0.dp,
                        y = (indicatorAnimation.value * 60f).dp + 8.dp
                    )
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                modules.forEachIndexed { index, module ->
                    ModuleIcon(
                        module = module,
                        isSelected = selectedModule == index,
                        onClick = { 
                            onModuleSelected(index)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        showIndicator = false // 不显示内部指示器，使用外部动画指示器
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 添加按钮
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isDarkTheme) DiscordColors.Dark.SurfaceDefault
                    else DiscordColors.Light.SurfaceDefault
                )
                .clickable { 
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "添加",
                tint = DiscordColors.Green,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 模块图标组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModuleIcon(
    module: ModuleItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    showIndicator: Boolean = true
) {
    val scale = remember { Animatable(1f) }
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier.size(48.dp)
    ) {
        // 选中指示器（左侧白色竖条）
        if (isSelected && showIndicator) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterStart)
                    .offset(x = (-8).dp)
            )
        }
        
        // 图标容器
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale.value)
                .clip(if (isSelected) RoundedCornerShape(16.dp) else CircleShape) // 选中时变成圆角方形
                .background(
                    if (isSelected) module.color 
                    else module.color.copy(alpha = 0.8f) // 未选中时稍微透明
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        // 可以在这里显示模块名称提示
                    }
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            scope.launch {
                                scale.animateTo(
                                    targetValue = 0.9f,
                                    animationSpec = tween(100)
                                )
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy
                                    )
                                )
                            }
                        }
                    ) { _, _ -> }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = module.icon,
                contentDescription = module.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 徽章
        module.badgeCount?.let { count ->
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .background(DiscordColors.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Dashboard内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(isDarkTheme: Boolean) {
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 品牌横幅
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(DiscordColors.Blurple),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "CC小记品牌横幅",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Life Manager",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 待处理事项区块
        PendingItemsSection(
            isExpanded = expandedCategories["pending"] != false,
            onToggle = { expandedCategories["pending"] = !(expandedCategories["pending"] != false) },
            isDarkTheme = isDarkTheme
        )
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            color = if (isDarkTheme) DiscordColors.Dark.Border else DiscordColors.Light.Border
        )
        
        // 今日活动区块
        TodayActivitiesSection(
            isExpanded = expandedCategories["activities"] != false,
            onToggle = { expandedCategories["activities"] = !(expandedCategories["activities"] != false) },
            isDarkTheme = isDarkTheme
        )
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            color = if (isDarkTheme) DiscordColors.Dark.Border else DiscordColors.Light.Border
        )
        
        // 本周趋势区块
        WeeklyTrendsSection(
            isExpanded = expandedCategories["trends"] != false,
            onToggle = { expandedCategories["trends"] = !(expandedCategories["trends"] != false) },
            isDarkTheme = isDarkTheme
        )
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            color = if (isDarkTheme) DiscordColors.Dark.Border else DiscordColors.Light.Border
        )
        
        // 月度统计区块
        MonthlyStatsSection(
            isExpanded = expandedCategories["stats"] != false,
            onToggle = { expandedCategories["stats"] = !(expandedCategories["stats"] != false) },
            isDarkTheme = isDarkTheme
        )
    }
}

/**
 * 类别标题
 */
@Composable
private fun CategoryHeader(
    name: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                   else DiscordColors.Light.TextMuted
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "收起" else "展开",
            modifier = Modifier.size(16.dp),
            tint = if (isDarkTheme) DiscordColors.Dark.TextMuted
                   else DiscordColors.Light.TextMuted
        )
    }
}

/**
 * 待处理事项区块
 */
@Composable
private fun PendingItemsSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    Column {
        CategoryHeader(
            name = "待处理事项",
            isExpanded = isExpanded,
            onToggle = onToggle,
            isDarkTheme = isDarkTheme
        )
        
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PendingItemCard(
                    priority = "高",
                    title = "完成项目报告",
                    time = "今天 17:00",
                    priorityColor = DiscordColors.Red,
                    isDarkTheme = isDarkTheme
                )
                PendingItemCard(
                    priority = "中",
                    title = "回复客户邮件",
                    time = "今天 15:00",
                    priorityColor = DiscordColors.Yellow,
                    isDarkTheme = isDarkTheme
                )
                PendingItemCard(
                    priority = "低",
                    title = "整理文档",
                    time = "本周内",
                    priorityColor = DiscordColors.Green,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * 待处理事项卡片
 */
@Composable
private fun PendingItemCard(
    priority: String,
    title: String,
    time: String,
    priorityColor: Color,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DiscordColors.Dark.SurfaceDefault
                           else DiscordColors.Light.SurfaceDefault
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(priorityColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = priority,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = priorityColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                           else DiscordColors.Light.TextPrimary
                )
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                           else DiscordColors.Light.TextMuted
                )
            }
        }
    }
}

/**
 * 今日活动区块
 */
@Composable
private fun TodayActivitiesSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    Column {
        CategoryHeader(
            name = "今日活动",
            isExpanded = isExpanded,
            onToggle = onToggle,
            isDarkTheme = isDarkTheme
        )
        
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityItemRow(
                    time = "10:32",
                    content = "打卡 喝水 ✓",
                    isDarkTheme = isDarkTheme
                )
                ActivityItemRow(
                    time = "09:15",
                    content = "完成 晨会准备",
                    isDarkTheme = isDarkTheme
                )
                ActivityItemRow(
                    time = "昨天",
                    content = "记账 交通费 -¥12.5",
                    isDarkTheme = isDarkTheme
                )
                ActivityItemRow(
                    time = "昨天",
                    content = "计划 学习进度 65%",
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * 活动项组件
 */
@Composable
private fun ActivityItemRow(
    time: String,
    content: String,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = time,
            fontSize = 12.sp,
            color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                   else DiscordColors.Light.TextMuted,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = content,
            fontSize = 14.sp,
            color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                   else DiscordColors.Light.TextNormal
        )
    }
}

/**
 * 本周趋势区块
 */
@Composable
private fun WeeklyTrendsSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    Column {
        CategoryHeader(
            name = "本周趋势",
            isExpanded = isExpanded,
            onToggle = onToggle,
            isDarkTheme = isDarkTheme
        )
        
        AnimatedVisibility(visible = isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) DiscordColors.Dark.SurfaceDefault
                                   else DiscordColors.Light.SurfaceDefault
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrendChartRow(
                        label = "支出",
                        data = "▂▄▆█▄▂▁",
                        color = DiscordColors.Green,
                        isDarkTheme = isDarkTheme
                    )
                    TrendChartRow(
                        label = "习惯",
                        data = "▆▆▄█▆▄█",
                        color = DiscordColors.Fuchsia,
                        isDarkTheme = isDarkTheme
                    )
                    TrendChartRow(
                        label = "待办",
                        data = "▄▄█▆█▄▆",
                        color = DiscordColors.Yellow,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

/**
 * 趋势图表组件
 */
@Composable
private fun TrendChartRow(
    label: String,
    data: String,
    color: Color,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                   else DiscordColors.Light.TextNormal,
            modifier = Modifier.width(48.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(
                    if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary
                    else DiscordColors.Light.BackgroundPrimary,
                    RoundedCornerShape(4.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data,
                fontSize = 24.sp,
                color = color,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
        }
    }
}

/**
 * 月度统计区块
 */
@Composable
private fun MonthlyStatsSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    Column {
        CategoryHeader(
            name = "月度统计",
            isExpanded = isExpanded,
            onToggle = onToggle,
            isDarkTheme = isDarkTheme
        )
        
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItemRow(
                    label = "总支出",
                    value = "¥3,256",
                    trend = "↑12%",
                    trendColor = DiscordColors.Red,
                    isDarkTheme = isDarkTheme
                )
                StatItemRow(
                    label = "总收入",
                    value = "¥8,500",
                    trend = "→0%",
                    trendColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                              else DiscordColors.Light.TextMuted,
                    isDarkTheme = isDarkTheme
                )
                StatItemRow(
                    label = "习惯坚持",
                    value = "15天连续",
                    trend = null,
                    trendColor = Color.Transparent,
                    isDarkTheme = isDarkTheme
                )
                StatItemRow(
                    label = "待办完成",
                    value = "45个任务",
                    trend = null,
                    trendColor = Color.Transparent,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatItemRow(
    label: String,
    value: String,
    trend: String?,
    trendColor: Color,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                   else DiscordColors.Light.TextNormal
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                       else DiscordColors.Light.TextPrimary
            )
            trend?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = trendColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 记账模块内容
 */
@Composable
private fun LedgerModuleContent(isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = DiscordColors.Green
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "记账模块",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                   else DiscordColors.Light.TextPrimary
        )
    }
}

/**
 * 待办模块内容
 */
@Composable
private fun TodoModuleContent(isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = DiscordColors.Yellow
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "待办模块",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                   else DiscordColors.Light.TextPrimary
        )
    }
}

/**
 * 习惯模块内容
 */
@Composable
private fun HabitModuleContent(isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = DiscordColors.Fuchsia
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "习惯模块",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                   else DiscordColors.Light.TextPrimary
        )
    }
}

/**
 * 排班模块内容
 */
@Composable
private fun ScheduleModuleContent(isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = DiscordColors.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "排班模块",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                   else DiscordColors.Light.TextPrimary
        )
    }
}

/**
 * 计划模块内容
 */
@Composable
private fun PlanModuleContent(isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Timeline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = DiscordColors.Blurple
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "计划模块",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                   else DiscordColors.Light.TextPrimary
        )
    }
}

/**
 * 模块项数据类
 */
private data class ModuleItem(
    val icon: ImageVector,
    val name: String,
    val color: Color,
    val badgeCount: Int?
)
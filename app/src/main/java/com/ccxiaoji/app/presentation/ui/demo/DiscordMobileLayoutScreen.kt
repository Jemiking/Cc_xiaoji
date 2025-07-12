package com.ccxiaoji.app.presentation.ui.demo

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ccxiaoji.app.presentation.ui.components.*
import com.ccxiaoji.ui.theme.DiscordColors
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.compose.material3.RadioButton
import androidx.compose.ui.platform.LocalView
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.DisposableEffect
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * 状态栏控制模式
 */
enum class StatusBarMode {
    NONE,                    // 无控制
    ACCOMPANIST,            // Accompanist库（需要依赖）
    WINDOW_COMPAT,          // WindowCompat基础版
    WINDOW_COMPAT_DELAYED,  // WindowCompat延迟版（DisposableEffect）
    BOX_OVERLAY,            // Box布局模拟
    WINDOW_INSETS,          // WindowInsets方案
    VIEW_LISTENER,          // View监听器方案
    COMBINED,               // 组合方案
    CUSTOM_THEME            // 自定义主题方案
}

/**
 * 状态栏解决方案信息
 */
data class StatusBarSolution(
    val mode: StatusBarMode,
    val name: String,
    val description: String,
    val category: String,
    val successRate: String,
    val requiresDependency: Boolean = false
)

/**
 * Discord风格移动端布局演示（真实移动端版本）
 * 
 * 布局结构：
 * 1. 底部导航栏（主页/通知/您 三个标签）
 * 2. 主页标签：垂直滚动的服务器-频道列表
 * 3. 通知标签：通知消息列表
 * 4. 您标签：个人中心设置页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DiscordMobileLayoutScreen(
    navController: NavHostController
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 主页, 1: 通知, 2: 您
    var isDarkTheme by remember { mutableStateOf(true) } // 主题状态
    var selectedModule by remember { mutableStateOf(0) } // 选中的模块
    // 固定侧边栏宽度为72dp
    val sidebarWidth = 72.dp
    
    // 频道分类展开状态
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }
    
    // 圆角调节器状态
    var cornerRadius by remember { mutableStateOf(16f) }
    
    // 状态栏控制模式
    var statusBarMode by remember { mutableStateOf(StatusBarMode.NONE) }
    
    // 获取Context和View用于状态栏控制
    val context = LocalContext.current
    val view = LocalView.current
    
    // 状态栏解决方案列表
    val statusBarSolutions = remember {
        listOf(
            StatusBarSolution(StatusBarMode.NONE, "无控制", "保持系统默认状态", "基础方案", "100%"),
            StatusBarSolution(StatusBarMode.BOX_OVERLAY, "视觉模拟", "使用Box覆盖实现效果", "基础方案", "100%"),
            StatusBarSolution(StatusBarMode.ACCOMPANIST, "Accompanist", "使用第三方库控制", "基础方案", "需依赖", true),
            
            StatusBarSolution(StatusBarMode.WINDOW_COMPAT, "WindowCompat基础", "使用官方API直接设置", "系统API方案", "中"),
            StatusBarSolution(StatusBarMode.WINDOW_COMPAT_DELAYED, "WindowCompat延迟", "延迟100ms后设置", "系统API方案", "高"),
            StatusBarSolution(StatusBarMode.WINDOW_INSETS, "WindowInsets控制", "使用Insets API", "系统API方案", "中"),
            
            StatusBarSolution(StatusBarMode.VIEW_LISTENER, "View监听器", "监听Window Insets变化", "实验方案", "低"),
            StatusBarSolution(StatusBarMode.COMBINED, "组合方案", "Box模拟+API设置", "实验方案", "高"),
            StatusBarSolution(StatusBarMode.CUSTOM_THEME, "自定义主题", "需要修改主题文件", "实验方案", "需配置")
        )
    }
    
    // 诊断信息状态
    var showDiagnostics by remember { mutableStateOf(true) }
    var statusBarColor by remember { mutableStateOf("#FFFFFF") }
    var isStatusBarTransparent by remember { mutableStateOf(false) }
    
    // 应用状态栏控制 - 基础模式使用SideEffect
    SideEffect {
        when (statusBarMode) {
            StatusBarMode.NONE -> {
                // 不做任何操作，保持系统默认
                statusBarColor = "#FFFFFF"
                isStatusBarTransparent = false
            }
            StatusBarMode.ACCOMPANIST -> {
                // 尝试使用Accompanist库
                try {
                    val systemUiControllerClass = Class.forName("com.google.accompanist.systemuicontroller.SystemUiController")
                    // 如果类存在，说明有依赖，可以使用反射调用
                    statusBarColor = "Accompanist控制"
                } catch (e: ClassNotFoundException) {
                    statusBarColor = "需要依赖"
                }
            }
            StatusBarMode.WINDOW_COMPAT -> {
                // 使用WindowCompat API
                (context as? Activity)?.window?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    val windowInsetsController = WindowCompat.getInsetsController(window, view)
                    windowInsetsController?.isAppearanceLightStatusBars = !isDarkTheme
                    statusBarColor = "透明尝试"
                }
            }
            StatusBarMode.WINDOW_INSETS -> {
                // WindowInsets方案
                (context as? Activity)?.window?.let { window ->
                    window.decorView.systemUiVisibility = 
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                }
            }
            StatusBarMode.VIEW_LISTENER -> {
                // View监听器方案
                view.setOnApplyWindowInsetsListener { v, insets ->
                    insets
                }
            }
            else -> {
                // 其他模式在DisposableEffect中处理
            }
        }
    }
    
    // 延迟模式使用DisposableEffect
    DisposableEffect(statusBarMode, isDarkTheme) {
        when (statusBarMode) {
            StatusBarMode.WINDOW_COMPAT_DELAYED -> {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    (context as? Activity)?.window?.let { window ->
                        WindowCompat.setDecorFitsSystemWindows(window, false)
                        window.statusBarColor = android.graphics.Color.TRANSPARENT
                        val windowInsetsController = WindowCompat.getInsetsController(window, view)
                        windowInsetsController?.isAppearanceLightStatusBars = !isDarkTheme
                        statusBarColor = "延迟透明"
                        isStatusBarTransparent = true
                    }
                }, 100)
            }
            StatusBarMode.COMBINED -> {
                // 组合方案：先设置API，再用Box覆盖
                (context as? Activity)?.window?.let { window ->
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                }
            }
            else -> {}
        }
        
        onDispose {
            // 清理操作
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主内容
        Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundDeepest 
                else DiscordColors.Light.BackgroundDeepest
            ),
        bottomBar = {
                // Discord风格底部导航栏（3个标签）
                NavigationBar(
                    containerColor = if (isDarkTheme) DiscordColors.Dark.BackgroundDeepest 
                                    else DiscordColors.Light.BackgroundDeepest,
                    contentColor = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                                  else DiscordColors.Light.TextNormal
                ) {
                        // 主页标签
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Box {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = "主页"
                                    )
                                    // 未读消息徽章
                                    if (selectedTab != 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(8.dp)
                                                .offset(x = 2.dp, y = (-2).dp)
                                                .clip(CircleShape)
                                                .background(DiscordColors.Red)
                                        )
                                    }
                                }
                            },
                            label = { Text("主页") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                                                  else DiscordColors.Light.TextPrimary,
                                selectedTextColor = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                                                  else DiscordColors.Light.TextPrimary,
                                unselectedIconColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                                    else DiscordColors.Light.TextMuted,
                                unselectedTextColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                                    else DiscordColors.Light.TextMuted,
                                indicatorColor = if (isDarkTheme) DiscordColors.Dark.SurfaceSelected 
                                               else DiscordColors.Light.SurfaceSelected
                            )
                        )
                        
                        // 通知标签
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = DiscordColors.Red,
                                            contentColor = Color.White
                                        ) {
                                            Text("4")
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "通知"
                                    )
                                }
                            },
                            label = { Text("通知") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                                                  else DiscordColors.Light.TextPrimary,
                                selectedTextColor = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                                                  else DiscordColors.Light.TextPrimary,
                                unselectedIconColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                                    else DiscordColors.Light.TextMuted,
                                unselectedTextColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                                    else DiscordColors.Light.TextMuted,
                                indicatorColor = if (isDarkTheme) DiscordColors.Dark.SurfaceSelected 
                                               else DiscordColors.Light.SurfaceSelected
                            )
                        )
                        
                        // 您标签
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "您"
                                )
                            },
                            label = { Text("您") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                                                  else DiscordColors.Light.TextPrimary,
                                selectedTextColor = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                                                  else DiscordColors.Light.TextPrimary,
                                unselectedIconColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                                    else DiscordColors.Light.TextMuted,
                                unselectedTextColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                                    else DiscordColors.Light.TextMuted,
                                indicatorColor = if (isDarkTheme) DiscordColors.Dark.SurfaceSelected 
                                               else DiscordColors.Light.SurfaceSelected
                            )
                        )
                    }
                }
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            if (isDarkTheme) DiscordColors.Dark.BackgroundDeepest 
                            else DiscordColors.Light.BackgroundDeepest
                        )
                ) {
                    // 左侧服务器栏（固定72dp）
                    DiscordServerBar(
                        selectedModule = selectedModule,
                        onModuleSelected = { index -> selectedModule = index },
                        isDarkTheme = isDarkTheme,
                        statusBarMode = statusBarMode
                    )
                    
                    // 主内容区
                    AnimatedContent(
                        targetState = selectedTab,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = cornerRadius.dp))
                            .background(
                                if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                                else DiscordColors.Light.BackgroundPrimary
                            ),
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                            }.using(SizeTransform(clip = false))
                        },
                        label = "tabContent"
                    ) { tab ->
                        when (tab) {
                            0 -> DashboardContent(
                                expandedCategories = expandedCategories,
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = !isDarkTheme }
                            )
                            1 -> NotificationTabContent(
                                isDarkTheme = isDarkTheme
                            )
                            2 -> YouTabContent(
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        
        // Box覆盖模拟状态栏（用于BOX_OVERLAY和COMBINED模式）
        if (statusBarMode == StatusBarMode.BOX_OVERLAY || statusBarMode == StatusBarMode.COMBINED) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.systemBars)
                    .background(
                        if (isDarkTheme) DiscordColors.Dark.BackgroundSidebar
                        else DiscordColors.Light.BackgroundSidebar
                    )
            )
        }
        
        // 视觉效果调节器浮窗
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(360.dp)
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) DiscordColors.Dark.BackgroundSidebar
                                else DiscordColors.Light.BackgroundSidebar
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "视觉效果调节器",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                           else DiscordColors.Light.TextPrimary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "圆角大小",
                        fontSize = 14.sp,
                        color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                               else DiscordColors.Light.TextNormal
                    )
                    Text(
                        "${cornerRadius.toInt()}dp",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DiscordColors.Blurple
                    )
                }
                
                Slider(
                    value = cornerRadius,
                    onValueChange = { cornerRadius = it },
                    valueRange = 0f..32f,
                    colors = SliderDefaults.colors(
                        thumbColor = DiscordColors.Blurple,
                        activeTrackColor = DiscordColors.Blurple,
                        inactiveTrackColor = if (isDarkTheme) DiscordColors.Dark.SurfaceHover
                                            else DiscordColors.Light.SurfaceHover
                    )
                )
                
                // 快速选择按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 8, 16, 24).forEach { value ->
                        OutlinedButton(
                            onClick = { cornerRadius = value.toFloat() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (cornerRadius.toInt() == value) DiscordColors.Blurple
                                             else if (isDarkTheme) DiscordColors.Dark.TextNormal
                                             else DiscordColors.Light.TextNormal
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (cornerRadius.toInt() == value) DiscordColors.Blurple
                                else if (isDarkTheme) DiscordColors.Dark.Divider
                                else DiscordColors.Light.Divider
                            )
                        ) {
                            Text("${value}dp", fontSize = 12.sp)
                        }
                    }
                }
                
                // 分隔线
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (isDarkTheme) DiscordColors.Dark.Divider
                           else DiscordColors.Light.Divider
                )
                
                // 状态栏控制方案
                Text(
                    "状态栏控制方案",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                           else DiscordColors.Light.TextNormal
                )
                
                // 分类标签
                var selectedCategory by remember { mutableStateOf("基础方案") }
                val categories = listOf("基础方案", "系统API方案", "实验方案")
                
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory),
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent,
                    contentColor = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                                  else DiscordColors.Light.TextNormal,
                    edgePadding = 0.dp
                ) {
                    categories.forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = { 
                                Text(
                                    category, 
                                    fontSize = 12.sp,
                                    maxLines = 1
                                ) 
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 方案列表
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    statusBarSolutions
                        .filter { it.category == selectedCategory }
                        .forEach { solution ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { statusBarMode = solution.mode }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = statusBarMode == solution.mode,
                                    onClick = { statusBarMode = solution.mode },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = DiscordColors.Blurple,
                                        unselectedColor = if (isDarkTheme) DiscordColors.Dark.TextMuted
                                                         else DiscordColors.Light.TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = solution.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                                                   else DiscordColors.Light.TextNormal
                                        )
                                        Text(
                                            text = solution.successRate,
                                            fontSize = 11.sp,
                                            color = when (solution.successRate) {
                                                "100%" -> Color(0xFF43B581)
                                                "高" -> Color(0xFF43B581)
                                                "中" -> Color(0xFFFAA61A)
                                                "低" -> Color(0xFFF04747)
                                                else -> if (isDarkTheme) DiscordColors.Dark.TextMuted
                                                       else DiscordColors.Light.TextMuted
                                            }
                                        )
                                    }
                                    Text(
                                        text = solution.description,
                                        fontSize = 11.sp,
                                        color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                                               else DiscordColors.Light.TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                }
                
                // 诊断信息
                if (showDiagnostics) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = if (isDarkTheme) DiscordColors.Dark.Divider
                               else DiscordColors.Light.Divider
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            if (isDarkTheme) DiscordColors.Dark.SurfaceDefault
                            else DiscordColors.Light.SurfaceDefault
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "诊断信息",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                                       else DiscordColors.Light.TextNormal
                            )
                            Row {
                                Text(
                                    "状态栏: ",
                                    fontSize = 11.sp,
                                    color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                                           else DiscordColors.Light.TextMuted
                                )
                                Text(
                                    statusBarColor,
                                    fontSize = 11.sp,
                                    color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                                           else DiscordColors.Light.TextNormal
                                )
                            }
                            Row {
                                Text(
                                    "透明状态: ",
                                    fontSize = 11.sp,
                                    color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                                           else DiscordColors.Light.TextMuted
                                )
                                Text(
                                    if (isStatusBarTransparent) "✓ 已透明" else "✗ 不透明",
                                    fontSize = 11.sp,
                                    color = if (isStatusBarTransparent) Color(0xFF43B581) else Color(0xFFF04747)
                                )
                            }
                            if (!isStatusBarTransparent && statusBarMode != StatusBarMode.NONE) {
                                Text(
                                    "建议: 尝试视觉模拟或组合方案",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFAA61A),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
                
                // 依赖提示
                val currentSolution = statusBarSolutions.find { it.mode == statusBarMode }
                if (currentSolution?.requiresDependency == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFA500).copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            "需要添加依赖: implementation \"com.google.accompanist:accompanist-systemuicontroller:0.32.0\"",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 11.sp,
                            color = Color(0xFFFFA500)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dashboard内容 - 根据文档设计实现
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    expandedCategories: MutableMap<String, Boolean>,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                else DiscordColors.Light.BackgroundPrimary
            )
    ) {
        // 品牌横幅
        BrandBanner()
        
        // 标题栏
        TopBar(
            currentModule = "主页",
            isDarkTheme = isDarkTheme,
            onSearch = { },
            onThemeToggle = onThemeToggle
        )
        
        // 功能栏（搜索栏 + 功能图标）
        FunctionBar(
            isDarkTheme = isDarkTheme
        )
        
        Divider(
            color = if (isDarkTheme) DiscordColors.Dark.Divider 
                   else DiscordColors.Light.Divider
        )
        
        // Dashboard内容区 - 根据文档设计
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(
                    if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                    else DiscordColors.Light.BackgroundPrimary
                ),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 1. 今日概览区块
            item {
                TodayOverviewSection(
                    isExpanded = expandedCategories["今日概览"] ?: true,
                    onToggle = { expandedCategories["今日概览"] = !(expandedCategories["今日概览"] ?: true) },
                    isDarkTheme = isDarkTheme
                )
            }
            
            // 2. 待处理事项区块
            item {
                PendingTasksSection(
                    isExpanded = expandedCategories["待处理"] ?: true,
                    onToggle = { expandedCategories["待处理"] = !(expandedCategories["待处理"] ?: true) },
                    isDarkTheme = isDarkTheme
                )
            }
            
            // 3. 最近动态区块
            item {
                RecentActivitiesSection(
                    isExpanded = expandedCategories["最近动态"] ?: true,
                    onToggle = { expandedCategories["最近动态"] = !(expandedCategories["最近动态"] ?: true) },
                    isDarkTheme = isDarkTheme
                )
            }
            
            // 4. 本周趋势区块
            item {
                WeeklyTrendsSection(
                    isExpanded = expandedCategories["本周趋势"] ?: true,
                    onToggle = { expandedCategories["本周趋势"] = !(expandedCategories["本周趋势"] ?: true) },
                    isDarkTheme = isDarkTheme
                )
            }
            
            // 5. 月度统计区块
            item {
                MonthlyStatsSection(
                    isExpanded = expandedCategories["月度统计"] ?: true,
                    onToggle = { expandedCategories["月度统计"] = !(expandedCategories["月度统计"] ?: true) },
                    isDarkTheme = isDarkTheme
                )
            }
            
            // 底部填充
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * 通知标签内容 - 通知消息列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTabContent(
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                else DiscordColors.Light.BackgroundPrimary
            )
    ) {
        // 顶部栏
        TopAppBar(
            title = { 
                Text(
                    "通知", 
                    color = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                           else DiscordColors.Light.TextPrimary
                ) 
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "筛选",
                        tint = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                              else DiscordColors.Light.TextNormal
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                               else DiscordColors.Light.BackgroundPrimary
            )
        )
        
        Divider(
            color = if (isDarkTheme) DiscordColors.Dark.Divider 
                   else DiscordColors.Light.Divider
        )
        
        // 通知列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(6) { index ->
                NotificationItem(
                    title = when(index) {
                        0 -> "记账提醒"
                        1 -> "习惯打卡"
                        2 -> "待办事项"
                        3 -> "月度总结"
                        4 -> "排班提醒"
                        else -> "计划进度"
                    },
                    message = when(index) {
                        0 -> "记得记录今天的午餐支出哦"
                        1 -> "你已连续打卡15天，继续保持！"
                        2 -> "今日还有2个任务待完成"
                        3 -> "本月支出¥3,256，查看详细报告"
                        4 -> "明天早班 8:00-16:00"
                        else -> "「学习计划」进度已达65%"
                    },
                    time = when(index) {
                        0 -> "刚刚"
                        1 -> "1小时前"
                        2 -> "2小时前"
                        3 -> "今天早上"
                        4 -> "昨天"
                        else -> "3天前"
                    },
                    isRead = index > 2,
                    icon = when(index) {
                        0 -> Icons.Default.AttachMoney
                        1 -> Icons.Default.FitnessCenter
                        2 -> Icons.Default.CheckCircle
                        3 -> Icons.Default.BarChart
                        4 -> Icons.Default.CalendarMonth
                        else -> Icons.Default.Timeline
                    },
                    color = when(index) {
                        0 -> DiscordColors.Green
                        1 -> DiscordColors.Fuchsia
                        2 -> DiscordColors.Yellow
                        3 -> DiscordColors.Blurple
                        4 -> DiscordColors.Red
                        else -> DiscordColors.Blurple
                    },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * You标签内容 - 个人中心页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YouTabContent(
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                else DiscordColors.Light.BackgroundPrimary
            )
    ) {
        // 顶部栏
        TopAppBar(
            title = { 
                Text(
                    "我的", 
                    color = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                           else DiscordColors.Light.TextPrimary
                ) 
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                              else DiscordColors.Light.TextNormal
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary 
                               else DiscordColors.Light.BackgroundPrimary
            )
        )
        
        Divider(
            color = if (isDarkTheme) DiscordColors.Dark.Divider 
                   else DiscordColors.Light.Divider
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 用户信息卡片
            item {
                DiscordCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDarkTheme = isDarkTheme
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DiscordColors.Blurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "用户名",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) DiscordColors.Dark.TextPrimary 
                                       else DiscordColors.Light.TextPrimary
                            )
                            Text(
                                "user@example.com",
                                fontSize = 14.sp,
                                color = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                                       else DiscordColors.Light.TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DiscordButton(
                                onClick = { },
                                text = "编辑个人资料",
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
            
            // 设置分组
            item {
                SettingsGroup(
                    title = "数据管理",
                    items = listOf(
                        SettingItemData(Icons.Default.Sync, "数据同步", "同步数据到云端"),
                        SettingItemData(Icons.Default.CloudUpload, "数据备份", "备份数据到云端"),
                        SettingItemData(Icons.Default.CloudDownload, "数据恢复", "从云端恢复数据"),
                        SettingItemData(Icons.Default.FileDownload, "数据导出", "导出各模块数据")
                    ),
                    isDarkTheme = isDarkTheme
                )
            }
            
            item {
                SettingsGroup(
                    title = "应用设置",
                    items = listOf(
                        SettingItemData(Icons.Default.Palette, "主题设置", "选择应用主题"),
                        SettingItemData(Icons.Default.Notifications, "通知设置", "管理应用通知"),
                        SettingItemData(Icons.Default.Language, "语言设置", "中文简体")
                    ),
                    isDarkTheme = isDarkTheme
                )
            }
            
            item {
                SettingsGroup(
                    title = "其他",
                    items = listOf(
                        SettingItemData(Icons.Default.HelpOutline, "使用帮助", null),
                        SettingItemData(Icons.Default.Info, "关于我们", "v1.0.0"),
                        SettingItemData(Icons.Default.Logout, "退出登录", null)
                    ),
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * 分类标题 - Discord风格
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                  else DiscordColors.Light.TextMuted,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = name.uppercase(),
            color = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                   else DiscordColors.Light.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

/**
 * 通知项 - Discord风格
 */
@Composable
private fun NotificationItem(
    title: String,
    message: String,
    time: String,
    isRead: Boolean,
    icon: ImageVector,
    color: Color,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (!isRead) {
                    if (isDarkTheme) DiscordColors.Dark.SurfaceHover.copy(alpha = 0.3f)
                    else DiscordColors.Light.SurfaceHover.copy(alpha = 0.3f)
                } else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // 内容
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (!isRead) FontWeight.Bold else FontWeight.Normal,
                    color = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                           else DiscordColors.Light.TextNormal
                )
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                           else DiscordColors.Light.TextMuted
                )
            }
            Text(
                text = message,
                fontSize = 13.sp,
                color = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                       else DiscordColors.Light.TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * 设置分组组件
 */
@Composable
private fun SettingsGroup(
    title: String,
    items: List<SettingItemData>,
    isDarkTheme: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                   else DiscordColors.Light.TextMuted,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        DiscordCard(
            modifier = Modifier.fillMaxWidth(),
            isDarkTheme = isDarkTheme
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingItem(
                        icon = item.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        onClick = { },
                        isDarkTheme = isDarkTheme
                    )
                    if (index < items.size - 1) {
                        Divider(
                            color = if (isDarkTheme) DiscordColors.Dark.Divider 
                                   else DiscordColors.Light.Divider,
                            modifier = Modifier.padding(start = 56.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 设置项组件
 */
@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                  else DiscordColors.Light.TextMuted,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                fontSize = 16.sp,
                color = if (isDarkTheme) DiscordColors.Dark.TextNormal 
                      else DiscordColors.Light.TextNormal
            )
            subtitle?.let {
                Text(
                    it,
                    fontSize = 14.sp,
                    color = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                          else DiscordColors.Light.TextMuted
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                  else DiscordColors.Light.TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

data class SettingItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String?
)

/**
 * 品牌横幅组件
 */
@Composable
private fun BrandBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF5865F2), // Discord品牌紫
                        Color(0xFF7289DA)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CC小记品牌横幅",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Life Manager",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 标题栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    currentModule: String,
    isDarkTheme: Boolean,
    onSearch: () -> Unit,
    onThemeToggle: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                        else DiscordColors.Light.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$currentModule >",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                        else DiscordColors.Light.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onThemeToggle) {
                Icon(
                    if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "主题切换",
                    tint = if (isDarkTheme) DiscordColors.Dark.TextNormal
                        else DiscordColors.Light.TextNormal
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isDarkTheme) DiscordColors.Dark.BackgroundPrimary
                else DiscordColors.Light.BackgroundPrimary
        )
    )
}

/**
 * 功能栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FunctionBar(
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 搜索框
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = {
                Text(
                    "🔍 搜索",
                    color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                        else DiscordColors.Light.TextMuted
                )
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DiscordColors.Blurple,
                unfocusedBorderColor = if (isDarkTheme) DiscordColors.Dark.Divider
                    else DiscordColors.Light.Divider
            ),
            shape = RoundedCornerShape(8.dp)
        )
        
        // 统计图标
        IconButton(onClick = { }) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = "统计",
                tint = if (isDarkTheme) DiscordColors.Dark.TextNormal
                    else DiscordColors.Light.TextNormal
            )
        }
        
        // 设置图标
        IconButton(onClick = { }) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "设置",
                tint = if (isDarkTheme) DiscordColors.Dark.TextNormal
                    else DiscordColors.Light.TextNormal
            )
        }
    }
}

/**
 * 今日概览区块
 */
@Composable
private fun TodayOverviewSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    Column {
        // 分类标题
        CategoryHeader(
            name = "今日概览",
            isExpanded = isExpanded,
            onToggle = onToggle,
            isDarkTheme = isDarkTheme
        )
        
        // 内容
        AnimatedVisibility(visible = isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) DiscordColors.Dark.SurfaceDefault
                        else DiscordColors.Light.SurfaceDefault
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 第一行：支出和预算
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DataCard(
                            icon = "💸",
                            title = "今日支出",
                            value = "¥125.50",
                            modifier = Modifier.weight(1f),
                            isDarkTheme = isDarkTheme
                        )
                        DataCard(
                            icon = "💰",
                            title = "本月预算剩余",
                            value = "¥2,344",
                            modifier = Modifier.weight(1f),
                            isDarkTheme = isDarkTheme
                        )
                    }
                    
                    // 第二行：待办和习惯
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DataCard(
                            icon = "✅",
                            title = "待办完成率",
                            value = "60% (3/5)",
                            modifier = Modifier.weight(1f),
                            isDarkTheme = isDarkTheme
                        )
                        DataCard(
                            icon = "🏃",
                            title = "习惯达成率",
                            value = "67% (2/3)",
                            modifier = Modifier.weight(1f),
                            isDarkTheme = isDarkTheme
                        )
                    }
                    
                    // 第三行：排班信息
                    DataCard(
                        icon = "📅",
                        title = "今日排班",
                        value = "早班 8:00",
                        modifier = Modifier.fillMaxWidth(),
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

/**
 * 待处理事项区块
 */
@Composable
private fun PendingTasksSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    Column {
        CategoryHeader(
            name = "待处理",
            isExpanded = isExpanded,
            onToggle = onToggle,
            isDarkTheme = isDarkTheme
        )
        
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PendingItem(
                    priority = "🔴",
                    title = "待办：完成项目报告",
                    time = "14:00",
                    isDarkTheme = isDarkTheme
                )
                PendingItem(
                    priority = "🟡",
                    title = "习惯：晚上跑步未打卡",
                    time = "",
                    isDarkTheme = isDarkTheme
                )
                PendingItem(
                    priority = "🔵",
                    title = "账单：信用卡还款提醒",
                    time = "3天后",
                    isDarkTheme = isDarkTheme
                )
                PendingItem(
                    priority = "🟢",
                    title = "记账：昨日有3笔未分类",
                    time = "",
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

/**
 * 最近动态区块
 */
@Composable
private fun RecentActivitiesSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    Column {
        CategoryHeader(
            name = "最近动态",
            isExpanded = isExpanded,
            onToggle = onToggle,
            isDarkTheme = isDarkTheme
        )
        
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActivityItem(
                    time = "刚刚",
                    content = "记账 午餐 -¥35",
                    isDarkTheme = isDarkTheme
                )
                ActivityItem(
                    time = "10:32",
                    content = "打卡 喝水 ✓",
                    isDarkTheme = isDarkTheme
                )
                ActivityItem(
                    time = "09:15",
                    content = "完成 晨会准备",
                    isDarkTheme = isDarkTheme
                )
                ActivityItem(
                    time = "昨天",
                    content = "记账 交通费 -¥12.5",
                    isDarkTheme = isDarkTheme
                )
                ActivityItem(
                    time = "昨天",
                    content = "计划 学习进度 65%",
                    isDarkTheme = isDarkTheme
                )
            }
        }
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrendChart(
                        label = "支出",
                        data = "▂▄▆█▄▂▁",
                        color = DiscordColors.Green,
                        isDarkTheme = isDarkTheme
                    )
                    TrendChart(
                        label = "习惯",
                        data = "▆▆▄█▆▄█",
                        color = DiscordColors.Fuchsia,
                        isDarkTheme = isDarkTheme
                    )
                    TrendChart(
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
                StatItem(
                    label = "总支出",
                    value = "¥3,256",
                    trend = "↑12%",
                    trendColor = DiscordColors.Red,
                    isDarkTheme = isDarkTheme
                )
                StatItem(
                    label = "总收入",
                    value = "¥8,500",
                    trend = "→0%",
                    trendColor = if (isDarkTheme) DiscordColors.Dark.TextMuted 
                        else DiscordColors.Light.TextMuted,
                    isDarkTheme = isDarkTheme
                )
                StatItem(
                    label = "习惯坚持",
                    value = "15天连续",
                    trend = null,
                    trendColor = Color.Transparent,
                    isDarkTheme = isDarkTheme
                )
                StatItem(
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
 * 数据卡片组件
 */
@Composable
private fun DataCard(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DiscordColors.Dark.SurfaceHover
                else DiscordColors.Light.SurfaceHover
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$icon $title",
                fontSize = 12.sp,
                color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                    else DiscordColors.Light.TextMuted
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) DiscordColors.Dark.TextPrimary
                    else DiscordColors.Light.TextPrimary
            )
        }
    }
}

/**
 * 待处理项组件
 */
@Composable
private fun PendingItem(
    priority: String,
    title: String,
    time: String,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDarkTheme) DiscordColors.Dark.SurfaceHover
                    else DiscordColors.Light.SurfaceHover,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(priority, fontSize = 16.sp)
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = if (isDarkTheme) DiscordColors.Dark.TextNormal
                else DiscordColors.Light.TextNormal
        )
        if (time.isNotEmpty()) {
            Text(
                text = time,
                fontSize = 12.sp,
                color = if (isDarkTheme) DiscordColors.Dark.TextMuted
                    else DiscordColors.Light.TextMuted
            )
        }
    }
}

/**
 * 活动项组件
 */
@Composable
private fun ActivityItem(
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
 * 趋势图表组件
 */
@Composable
private fun TrendChart(
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
 * 统计项组件
 */
@Composable
private fun StatItem(
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
 * Discord风格左侧服务器栏
 */
@Composable
private fun DiscordServerBar(
    selectedModule: Int,
    onModuleSelected: (Int) -> Unit,
    isDarkTheme: Boolean,
    statusBarMode: StatusBarMode
) {
    val width = 72.dp // 固定宽度
    val modules = listOf(
        Triple(Icons.Default.Home, "主页", DiscordColors.Blurple),
        Triple(Icons.Default.AttachMoney, "记账", DiscordColors.Green),
        Triple(Icons.Default.CheckCircle, "待办", DiscordColors.Yellow),
        Triple(Icons.Default.FitnessCenter, "习惯", DiscordColors.Fuchsia),
        Triple(Icons.Default.CalendarMonth, "排班", DiscordColors.Red),
        Triple(Icons.Default.Timeline, "计划", DiscordColors.Blurple)
    )
    
    // 固定尺寸（标准72dp布局）
    val iconContainerSize = 48.dp
    val iconSize = 24.dp
    val selectionIndicatorHeight = 32.dp
    val iconButtonSize = 40.dp
    val indicatorOffset = (-20).dp
    
    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(
                if (isDarkTheme) DiscordColors.Dark.BackgroundSidebar
                else DiscordColors.Light.BackgroundSidebar
            )
            .then(
                if (statusBarMode == StatusBarMode.WINDOW_COMPAT) {
                    Modifier.statusBarsPadding()
                } else {
                    Modifier.padding(top = 8.dp)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 模块图标
        modules.forEachIndexed { index, (icon, label, color) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 图标容器
                Box(
                    modifier = Modifier.size(iconContainerSize),
                    contentAlignment = Alignment.Center
                ) {
                    // 图标按钮
                    Box(
                        modifier = Modifier
                            .size(iconButtonSize)
                            .clip(RoundedCornerShape(if (selectedModule == index) 12.dp else 16.dp))
                            .background(color)
                            .clickable { onModuleSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    
                    // 徽章
                    val badgeCount = when(index) {
                        1 -> 3  // 记账
                        2 -> 2  // 待办
                        3 -> 1  // 习惯
                        else -> null
                    }
                    badgeCount?.let { count ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
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
                
                // 选中指示器（横线）
                if (selectedModule == index) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(3.dp)
                            .background(Color.White, RoundedCornerShape(1.5.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 添加按钮
        Box(
            modifier = Modifier
                .size(iconButtonSize)
                .clip(CircleShape)
                .background(
                    if (isDarkTheme) DiscordColors.Dark.SurfaceDefault
                    else DiscordColors.Light.SurfaceDefault
                )
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "添加",
                tint = DiscordColors.Green,
                modifier = Modifier.size(iconSize)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

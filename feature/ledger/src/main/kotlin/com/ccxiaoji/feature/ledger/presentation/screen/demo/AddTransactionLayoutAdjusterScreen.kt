package com.ccxiaoji.feature.ledger.presentation.screen.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddTransactionViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp

// 增强版布局调节参数数据类
data class LayoutAdjustmentParams(
    // === 分类图标区域 ===
    val categoryIconSize: Float = 24f,              // 分类图标大小 (dp)
    val categoryHorizontalSpacing: Float = 8f,      // 分类水平间距 (dp)
    val categoryVerticalSpacing: Float = 16f,       // 分类垂直间距 (dp)
    val categoryGridPadding: Float = 16f,           // 分类网格边距 (dp)
    val categoryTextSize: Float = 9f,               // 分类标签字体大小 (sp)
    val categoryCardCornerRadius: Float = 8f,       // 分类卡片圆角 (dp)
    val categoryCardPadding: Float = 4f,            // 分类卡片内边距 (dp)
    val gridColumnCount: Int = 5,                   // 网格列数
    
    // === Tab切换区域 ===
    val tabRowHeight: Float = 40f,                  // Tab行高度 (dp)
    val tabRowWidth: Float = 200f,                  // Tab行宽度 (dp)
    val tabCornerRadius: Float = 8f,                // Tab圆角 (dp)
    val tabVerticalPadding: Float = 8f,             // Tab垂直内边距 (dp)
    
    // === 输入区域布局 ===
    val inputAreaHeight: Float = 250f,              // 输入区域总高度 (dp)
    val inputAreaCornerRadius: Float = 16f,         // 输入区域圆角 (dp)
    val inputAreaPadding: Float = 16f,              // 输入区域总边距 (dp)
    
    // === 备注区域细节 ===
    val noteFieldTopPadding: Float = 0f,           // 备注框顶部边距 (dp)
    val noteFieldBottomPadding: Float = 8f,        // 备注框底部边距 (dp)
    val noteFieldHorizontalPadding: Float = 0f,    // 备注框左右内边距 (dp)
    val noteFieldContentPadding: Float = 12f,      // 备注框内容内边距 (dp)
    val noteTextSize: Float = 14f,                 // 备注文字大小 (sp)
    val noteToAmountSpacing: Float = 8f,           // 备注到金额区域间距 (dp)
    
    // === 金额显示区域 ===
    val amountTextSize: Float = 28f,               // 金额文字大小 (sp)
    val amountTextPadding: Float = 8f,             // 金额文字右边距 (dp)
    val accountTextSize: Float = 14f,              // 账户信息文字大小 (sp)
    val accountTextLeftPadding: Float = 0f,        // 账户文字左边距 (dp)
    val accountToNoteSpacing: Float = 8f,          // 账户与备注区间距 (dp)
    val amountToKeypadSpacing: Float = 8f,         // 金额到键盘间距 (dp)
    
    // === 键盘区域 ===
    val keypadButtonSize: Float = 48f,             // 键盘按钮高度 (dp)
    val keypadButtonSpacing: Float = 8f,           // 键盘按钮水平间距 (dp)
    val keypadRowSpacing: Float = 8f,              // 键盘按钮行间距 (dp)
    val keypadButtonCornerRadius: Float = 8f,      // 键盘按钮圆角 (dp)
    val keypadTextSize: Float = 16f,               // 键盘按钮文字大小 (sp)
    val keypadBottomPadding: Float = 16f,          // 键盘底部内边距 (dp)
    val keypadHorizontalPadding: Float = 16f,      // 键盘左右边距 (dp)
    
    // === 整体布局权重 ===
    val categoryGridWeight: Float = 1f,            // 分类区域权重
    
    // === 调试模式 ===
    val debugMode: Boolean = false,                // 启用调试模式
    val showBorders: Boolean = false,              // 显示边界框
    val showSpacingValues: Boolean = false,        // 显示间距数值
    val showPaddingAreas: Boolean = false,         // 显示内边距区域
    val showContentAreas: Boolean = false          // 显示内容区域
) {
    // 获取默认值的静态方法，用于重置单个参数
    companion object {
        fun getDefault() = LayoutAdjustmentParams()
        
        // 从剪贴板粘贴的Kotlin代码中解析参数
        fun fromKotlinCode(code: String): LayoutAdjustmentParams? {
            return try {
                val default = getDefault()
                
                // 使用正则表达式提取参数值
                fun extractFloatParam(name: String): Float {
                    val regex = """$name\s*=\s*([0-9.]+)f?""".toRegex()
                    val match = regex.find(code)
                    return match?.groupValues?.get(1)?.toFloatOrNull() ?: when(name) {
                        "categoryIconSize" -> default.categoryIconSize
                        "categoryHorizontalSpacing" -> default.categoryHorizontalSpacing
                        "categoryVerticalSpacing" -> default.categoryVerticalSpacing
                        "categoryGridPadding" -> default.categoryGridPadding
                        "categoryTextSize" -> default.categoryTextSize
                        "categoryCardCornerRadius" -> default.categoryCardCornerRadius
                        "categoryCardPadding" -> default.categoryCardPadding
                        "tabRowHeight" -> default.tabRowHeight
                        "tabRowWidth" -> default.tabRowWidth
                        "tabCornerRadius" -> default.tabCornerRadius
                        "tabVerticalPadding" -> default.tabVerticalPadding
                        "inputAreaHeight" -> default.inputAreaHeight
                        "inputAreaCornerRadius" -> default.inputAreaCornerRadius
                        "inputAreaPadding" -> default.inputAreaPadding
                        "noteFieldTopPadding" -> default.noteFieldTopPadding
                        "noteFieldBottomPadding" -> default.noteFieldBottomPadding
                        "noteFieldHorizontalPadding" -> default.noteFieldHorizontalPadding
                        "noteFieldContentPadding" -> default.noteFieldContentPadding
                        "noteTextSize" -> default.noteTextSize
                        "noteToAmountSpacing" -> default.noteToAmountSpacing
                        "amountTextSize" -> default.amountTextSize
                        "amountTextPadding" -> default.amountTextPadding
                        "accountTextSize" -> default.accountTextSize
                        "accountTextLeftPadding" -> default.accountTextLeftPadding
                        "accountToNoteSpacing" -> default.accountToNoteSpacing
                        "amountToKeypadSpacing" -> default.amountToKeypadSpacing
                        "keypadButtonSize" -> default.keypadButtonSize
                        "keypadButtonSpacing" -> default.keypadButtonSpacing
                        "keypadRowSpacing" -> default.keypadRowSpacing
                        "keypadButtonCornerRadius" -> default.keypadButtonCornerRadius
                        "keypadTextSize" -> default.keypadTextSize
                        "keypadBottomPadding" -> default.keypadBottomPadding
                        "keypadHorizontalPadding" -> default.keypadHorizontalPadding
                        "categoryGridWeight" -> default.categoryGridWeight
                        else -> 0f
                    }
                }
                
                fun extractBooleanParam(name: String): Boolean {
                    val regex = """$name\s*=\s*(true|false)""".toRegex()
                    val match = regex.find(code)
                    return match?.groupValues?.get(1)?.toBooleanStrictOrNull() ?: when(name) {
                        "debugMode" -> default.debugMode
                        "showBorders" -> default.showBorders
                        "showSpacingValues" -> default.showSpacingValues
                        "showPaddingAreas" -> default.showPaddingAreas
                        "showContentAreas" -> default.showContentAreas
                        else -> false
                    }
                }
                
                fun extractIntParam(name: String): Int {
                    val regex = """$name\s*=\s*([0-9]+)""".toRegex()
                    val match = regex.find(code)
                    return match?.groupValues?.get(1)?.toIntOrNull() ?: when(name) {
                        "gridColumnCount" -> default.gridColumnCount
                        else -> 0
                    }
                }
                
                LayoutAdjustmentParams(
                    categoryIconSize = extractFloatParam("categoryIconSize"),
                    categoryHorizontalSpacing = extractFloatParam("categoryHorizontalSpacing"),
                    categoryVerticalSpacing = extractFloatParam("categoryVerticalSpacing"),
                    categoryGridPadding = extractFloatParam("categoryGridPadding"),
                    categoryTextSize = extractFloatParam("categoryTextSize"),
                    categoryCardCornerRadius = extractFloatParam("categoryCardCornerRadius"),
                    categoryCardPadding = extractFloatParam("categoryCardPadding"),
                    gridColumnCount = extractIntParam("gridColumnCount"),
                    tabRowHeight = extractFloatParam("tabRowHeight"),
                    tabRowWidth = extractFloatParam("tabRowWidth"),
                    tabCornerRadius = extractFloatParam("tabCornerRadius"),
                    tabVerticalPadding = extractFloatParam("tabVerticalPadding"),
                    inputAreaHeight = extractFloatParam("inputAreaHeight"),
                    inputAreaCornerRadius = extractFloatParam("inputAreaCornerRadius"),
                    inputAreaPadding = extractFloatParam("inputAreaPadding"),
                    noteFieldTopPadding = extractFloatParam("noteFieldTopPadding"),
                    noteFieldBottomPadding = extractFloatParam("noteFieldBottomPadding"),
                    noteFieldHorizontalPadding = extractFloatParam("noteFieldHorizontalPadding"),
                    noteFieldContentPadding = extractFloatParam("noteFieldContentPadding"),
                    noteTextSize = extractFloatParam("noteTextSize"),
                    noteToAmountSpacing = extractFloatParam("noteToAmountSpacing"),
                    amountTextSize = extractFloatParam("amountTextSize"),
                    amountTextPadding = extractFloatParam("amountTextPadding"),
                    accountTextSize = extractFloatParam("accountTextSize"),
                    accountTextLeftPadding = extractFloatParam("accountTextLeftPadding"),
                    accountToNoteSpacing = extractFloatParam("accountToNoteSpacing"),
                    amountToKeypadSpacing = extractFloatParam("amountToKeypadSpacing"),
                    keypadButtonSize = extractFloatParam("keypadButtonSize"),
                    keypadButtonSpacing = extractFloatParam("keypadButtonSpacing"),
                    keypadRowSpacing = extractFloatParam("keypadRowSpacing"),
                    keypadButtonCornerRadius = extractFloatParam("keypadButtonCornerRadius"),
                    keypadTextSize = extractFloatParam("keypadTextSize"),
                    keypadBottomPadding = extractFloatParam("keypadBottomPadding"),
                    keypadHorizontalPadding = extractFloatParam("keypadHorizontalPadding"),
                    categoryGridWeight = extractFloatParam("categoryGridWeight"),
                    
                    // 调试模式
                    debugMode = extractBooleanParam("debugMode"),
                    showBorders = extractBooleanParam("showBorders"),
                    showSpacingValues = extractBooleanParam("showSpacingValues"),
                    showPaddingAreas = extractBooleanParam("showPaddingAreas"),
                    showContentAreas = extractBooleanParam("showContentAreas")
                )
            } catch (e: Exception) {
                null // 解析失败返回null
            }
        }
        
        // 生成用于复制的Kotlin代码格式
        fun LayoutAdjustmentParams.toKotlinCode(): String {
            return """LayoutAdjustmentParams(
                // === 分类图标区域 ===
                categoryIconSize = ${categoryIconSize}f,
                categoryHorizontalSpacing = ${categoryHorizontalSpacing}f,
                categoryVerticalSpacing = ${categoryVerticalSpacing}f,
                categoryGridPadding = ${categoryGridPadding}f,
                categoryTextSize = ${categoryTextSize}f,
                categoryCardCornerRadius = ${categoryCardCornerRadius}f,
                categoryCardPadding = ${categoryCardPadding}f,
                gridColumnCount = $gridColumnCount,
                
                // === Tab切换区域 ===
                tabRowHeight = ${tabRowHeight}f,
                tabRowWidth = ${tabRowWidth}f,
                tabCornerRadius = ${tabCornerRadius}f,
                tabVerticalPadding = ${tabVerticalPadding}f,
                
                // === 输入区域布局 ===
                inputAreaHeight = ${inputAreaHeight}f,
                inputAreaCornerRadius = ${inputAreaCornerRadius}f,
                inputAreaPadding = ${inputAreaPadding}f,
                
                // === 备注区域细节 ===
                noteFieldTopPadding = ${noteFieldTopPadding}f,
                noteFieldBottomPadding = ${noteFieldBottomPadding}f,
                noteFieldHorizontalPadding = ${noteFieldHorizontalPadding}f,
                noteFieldContentPadding = ${noteFieldContentPadding}f,
                noteTextSize = ${noteTextSize}f,
                noteToAmountSpacing = ${noteToAmountSpacing}f,
                
                // === 金额显示区域 ===
                amountTextSize = ${amountTextSize}f,
                amountTextPadding = ${amountTextPadding}f,
                accountTextSize = ${accountTextSize}f,
                accountTextLeftPadding = ${accountTextLeftPadding}f,
                accountToNoteSpacing = ${accountToNoteSpacing}f,
                amountToKeypadSpacing = ${amountToKeypadSpacing}f,
                
                // === 键盘区域 ===
                keypadButtonSize = ${keypadButtonSize}f,
                keypadButtonSpacing = ${keypadButtonSpacing}f,
                keypadRowSpacing = ${keypadRowSpacing}f,
                keypadButtonCornerRadius = ${keypadButtonCornerRadius}f,
                keypadTextSize = ${keypadTextSize}f,
                keypadBottomPadding = ${keypadBottomPadding}f,
                keypadHorizontalPadding = ${keypadHorizontalPadding}f,
                
                // === 整体布局权重 ===
                categoryGridWeight = ${categoryGridWeight}f,
                
                // === 调试模式 ===
                debugMode = $debugMode,
                showBorders = $showBorders,
                showSpacingValues = $showSpacingValues,
                showPaddingAreas = $showPaddingAreas,
                showContentAreas = $showContentAreas
            )""".trimIndent()
        }
    }
}

// 调试辅助函数和颜色系统
object DebugColors {
    val PaddingBorder = Color(0xFFE53E3E)      // 红色 - Padding边界
    val SpacingBorder = Color(0xFF3182CE)      // 蓝色 - Spacing边界
    val ContentBorder = Color(0xFF38A169)      // 绿色 - Content边界
    val MarginBorder = Color(0xFFD69E2E)       // 黄色 - Margin边界
    val DebugBackground = Color(0x1AE53E3E)    // 半透明红色背景
    val TextDebug = Color(0xFF1A202C)          // 深色调试文字
}

@Composable
fun Modifier.debugBorder(
    enabled: Boolean,
    color: Color,
    width: androidx.compose.ui.unit.Dp = 1.dp,
    label: String = ""
): Modifier {
    return if (enabled) {
        this.border(BorderStroke(width, color))
    } else {
        this
    }
}

@Composable
fun Modifier.debugBackground(
    enabled: Boolean,
    color: Color = DebugColors.DebugBackground
): Modifier {
    return if (enabled) {
        this.background(color)
    } else {
        this
    }
}

@Composable
fun DebugSpacingLabel(
    value: Float,
    unit: String = "dp",
    show: Boolean,
    modifier: Modifier = Modifier
) {
    if (show && value > 0) {
        Box(
            modifier = modifier
                .background(
                    DebugColors.DebugBackground,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${value.toInt()}$unit",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    color = DebugColors.TextDebug,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionLayoutAdjusterScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // 布局调节参数状态
    var adjustmentParams by remember { mutableStateOf(LayoutAdjustmentParams()) }
    var showAdjustmentPanel by remember { mutableStateOf(true) }
    
    // 模拟分类数据
    val expenseCategories = remember {
        listOf(
            CategoryGridItem("buy_food", "买菜", Icons.Default.ShoppingCart),
            CategoryGridItem("breakfast", "早餐", Icons.Default.Restaurant),
            CategoryGridItem("dining", "下馆子", Icons.Default.Fastfood),
            CategoryGridItem("grocery", "柴米油盐", Icons.Default.Kitchen),
            CategoryGridItem("fruit", "水果", Icons.Default.LocalGroceryStore),
            CategoryGridItem("snack", "零食", Icons.Default.Fastfood),
            CategoryGridItem("drink", "饮料", Icons.Default.LocalBar),
            CategoryGridItem("clothes", "衣服", Icons.Default.Store),
            CategoryGridItem("transport", "交通", Icons.Default.DirectionsCar),
            CategoryGridItem("travel", "旅行", Icons.Default.Flight),
            CategoryGridItem("phone", "话费网费", Icons.Default.Phone),
            CategoryGridItem("alcohol", "烟酒", Icons.Default.LocalBar),
            CategoryGridItem("study", "学习", Icons.Default.School),
            CategoryGridItem("daily", "日用品", Icons.Default.ShoppingBasket),
            CategoryGridItem("housing", "住房", Icons.Default.Home),
            CategoryGridItem("beauty", "美妆", Icons.Default.Face),
            CategoryGridItem("medical", "医疗", Icons.Default.MedicalServices),
            CategoryGridItem("gift", "发红包", Icons.Default.CardGiftcard),
            CategoryGridItem("entertainment", "娱乐", Icons.Default.SportsEsports),
            CategoryGridItem("social", "请客送礼", Icons.Default.Cake),
            CategoryGridItem("digital", "电器数码", Icons.Default.Computer),
            CategoryGridItem("utility", "水电煤", Icons.Default.Power),
            CategoryGridItem("other", "其它", Icons.Default.MoreHoriz),
            CategoryGridItem("market", "超市", Icons.Default.Store)
        )
    }
    
    val incomeCategories = remember {
        listOf(
            CategoryGridItem("salary", "工资", Icons.Default.Work, true),
            CategoryGridItem("bonus", "奖金", Icons.Default.EmojiEvents, true),
            CategoryGridItem("investment", "投资", Icons.Default.TrendingUp, true),
            CategoryGridItem("business", "生意", Icons.Default.Business, true),
            CategoryGridItem("part_time", "兼职", Icons.Default.Schedule, true),
            CategoryGridItem("gift_income", "礼金", Icons.Default.Redeem, true),
            CategoryGridItem("other_income", "其它收入", Icons.Default.MoreHoriz, true)
        )
    }
    
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var amountText by remember { mutableStateOf("0.0") }
    
    val currentCategories = if (uiState.isIncome) incomeCategories else expenseCategories
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "布局调节器 - 方案六",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAdjustmentPanel = !showAdjustmentPanel }) {
                        Icon(
                            if (showAdjustmentPanel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showAdjustmentPanel) "隐藏调节面板" else "显示调节面板"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部：收入/支出切换（可调节尺寸）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = adjustmentParams.categoryGridPadding.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TabRow(
                        selectedTabIndex = if (uiState.isIncome) 1 else 0,
                        modifier = Modifier
                            .width(adjustmentParams.tabRowWidth.dp)
                            .height(adjustmentParams.tabRowHeight.dp),
                        indicator = { },
                        divider = { }
                    ) {
                        Tab(
                            selected = !uiState.isIncome,
                            onClick = { viewModel.setIncomeType(false) },
                            modifier = Modifier.background(
                                if (!uiState.isIncome) Color.Black else Color.Transparent,
                                RoundedCornerShape(adjustmentParams.tabCornerRadius.dp)
                            )
                        ) {
                            Text(
                                text = "支出",
                                color = if (!uiState.isIncome) Color.White else Color.Gray,
                                modifier = Modifier.padding(vertical = adjustmentParams.tabVerticalPadding.dp)
                            )
                        }
                        Tab(
                            selected = uiState.isIncome,
                            onClick = { viewModel.setIncomeType(true) },
                            modifier = Modifier.background(
                                if (uiState.isIncome) Color.Black else Color.Transparent,
                                RoundedCornerShape(adjustmentParams.tabCornerRadius.dp)
                            )
                        ) {
                            Text(
                                text = "收入",
                                color = if (uiState.isIncome) Color.White else Color.Gray,
                                modifier = Modifier.padding(vertical = adjustmentParams.tabVerticalPadding.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(adjustmentParams.categoryVerticalSpacing.dp))
                
                // 中间：分类网格（全面可调节）
                LazyVerticalGrid(
                    columns = GridCells.Fixed(adjustmentParams.gridColumnCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(adjustmentParams.categoryGridWeight)
                        .padding(horizontal = adjustmentParams.categoryGridPadding.dp),
                    verticalArrangement = Arrangement.spacedBy(adjustmentParams.categoryVerticalSpacing.dp),
                    horizontalArrangement = Arrangement.spacedBy(adjustmentParams.categoryHorizontalSpacing.dp)
                ) {
                    items(currentCategories) { category ->
                        AdjustableCategoryCard(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                            params = adjustmentParams
                        )
                    }
                }
                
                // 底部：输入区域（全面可调节）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(adjustmentParams.inputAreaHeight.dp)
                        .debugBorder(adjustmentParams.showBorders, DebugColors.MarginBorder, 3.dp, "输入区域")
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(
                                topStart = adjustmentParams.inputAreaCornerRadius.dp, 
                                topEnd = adjustmentParams.inputAreaCornerRadius.dp
                            )
                        )
                        .debugBackground(adjustmentParams.showContentAreas, DebugColors.DebugBackground.copy(alpha = 0.1f))
                        .padding(adjustmentParams.inputAreaPadding.dp)
                ) {
                    // 第一行：备注框（左）和金额数字（右）并排
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .debugBorder(adjustmentParams.showBorders, DebugColors.ContentBorder, 2.dp, "备注行")
                            .debugBackground(adjustmentParams.showPaddingAreas)
                            .padding(
                                top = adjustmentParams.noteFieldTopPadding.dp,
                                bottom = adjustmentParams.noteFieldBottomPadding.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(adjustmentParams.noteToAmountSpacing.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：备注输入框
                        Box(
                            modifier = Modifier
                                .weight(1f) // 占用大部分宽度
                                .height(56.dp) // 固定高度
                                .debugBorder(adjustmentParams.showBorders, DebugColors.PaddingBorder, 1.dp, "备注框")
                                .padding(horizontal = adjustmentParams.noteFieldHorizontalPadding.dp) // 水平内边距
                        ) {
                            OutlinedTextField(
                                value = "",
                                onValueChange = { },
                                placeholder = { 
                                    Text(
                                        "点此输入备注...",
                                        fontSize = adjustmentParams.noteTextSize.sp
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(adjustmentParams.noteFieldContentPadding.dp), // 内容内边距控制
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = adjustmentParams.noteTextSize.sp
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                        
                        // 右侧：金额数字显示
                        Box(
                            modifier = Modifier.debugBorder(adjustmentParams.showBorders, DebugColors.ContentBorder, 1.dp, "金额")
                        ) {
                            Text(
                                text = "$amountText CNY",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = adjustmentParams.amountTextSize.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isIncome) DesignTokens.BrandColors.Success else DesignTokens.BrandColors.Error,
                                modifier = Modifier.padding(end = adjustmentParams.amountTextPadding.dp) // 替换硬编码的8dp
                            )
                        }
                    }
                    
                    // 调试：显示间距数值
                    if (adjustmentParams.showSpacingValues || adjustmentParams.debugMode) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            DebugSpacingLabel(
                                value = adjustmentParams.accountToNoteSpacing,
                                show = true,
                                modifier = Modifier.background(
                                    DebugColors.SpacingBorder.copy(alpha = 0.2f),
                                    RoundedCornerShape(2.dp)
                                )
                            )
                        }
                    }
                    
                    Spacer(
                        modifier = Modifier
                            .height(adjustmentParams.accountToNoteSpacing.dp)
                            .debugBorder(adjustmentParams.showBorders, DebugColors.SpacingBorder, 1.dp, "accountToNote")
                    ) // 使用新的账户与备注间距
                    
                    // 第二行：账户信息和功能按钮在下方
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：账户和时间信息
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .debugBorder(adjustmentParams.showBorders, DebugColors.ContentBorder, 1.dp, "账户信息")
                                .padding(start = adjustmentParams.accountTextLeftPadding.dp) // 新增：账户文字左边距
                        ) {
                            Text(
                                text = "现金",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = adjustmentParams.accountTextSize.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "今天 15:49",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = adjustmentParams.accountTextSize.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 右侧：功能按钮
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Receipt, contentDescription = "报销")
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Image, contentDescription = "图片")
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "购物车")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(adjustmentParams.amountToKeypadSpacing.dp))
                    
                    // 数字键盘（全面可调节，包括左右边距）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = adjustmentParams.keypadHorizontalPadding.dp)
                    ) {
                        AdjustableNumberKeypad(
                            onNumberClick = { number ->
                                if (amountText == "0.0") {
                                    amountText = number
                                } else {
                                    amountText += number
                                }
                            },
                            onDotClick = {
                                if (!amountText.contains(".")) {
                                    amountText += "."
                                }
                            },
                            onDeleteClick = {
                                if (amountText.length > 1) {
                                    amountText = amountText.dropLast(1)
                                } else {
                                    amountText = "0.0"
                                }
                            },
                            onPlusClick = { },
                            onMinusClick = { },
                            onAgainClick = { },
                            onSaveClick = {
                                // TODO: 实现保存功能
                            },
                            params = adjustmentParams
                        )
                    }
                }
            }
            
            // 调节面板（悬浮，进一步优化避免遮挡）
            if (showAdjustmentPanel) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)  // 改为顶部对齐
                        .width(220.dp)  // 进一步减小宽度
                        .fillMaxHeight(0.6f)  // 减小高度，留出更多空间给键盘
                        .padding(end = 4.dp, top = 80.dp, bottom = 120.dp), // 调整边距，为键盘留出更多空间
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    AdjustmentPanel(
                        params = adjustmentParams,
                        onParamsChange = { adjustmentParams = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustableCategoryCard(
    category: CategoryGridItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    params: LayoutAdjustmentParams
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp, 
                DesignTokens.BrandColors.Ledger
            )
        } else null,
        shape = RoundedCornerShape(params.categoryCardCornerRadius.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(params.categoryCardPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(params.categoryIconSize.dp),
                tint = if (isSelected) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = params.categoryTextSize.sp
                ),
                textAlign = TextAlign.Center,
                color = if (isSelected) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun AdjustableNumberKeypad(
    onNumberClick: (String) -> Unit,
    onDotClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPlusClick: () -> Unit,
    onMinusClick: () -> Unit,
    onAgainClick: () -> Unit,
    onSaveClick: () -> Unit, // 新增保存按钮回调
    params: LayoutAdjustmentParams
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(params.keypadRowSpacing.dp)
    ) {
        // 第一行：1 2 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(params.keypadButtonSpacing.dp)
        ) {
            AdjustableKeypadButton("1", Modifier.weight(1f), params) { onNumberClick("1") }
            AdjustableKeypadButton("2", Modifier.weight(1f), params) { onNumberClick("2") }
            AdjustableKeypadButton("3", Modifier.weight(1f), params) { onNumberClick("3") }
            AdjustableKeypadButton("×", Modifier.weight(1f), params) { onDeleteClick() }
        }
        
        // 第二行：4 5 6 -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(params.keypadButtonSpacing.dp)
        ) {
            AdjustableKeypadButton("4", Modifier.weight(1f), params) { onNumberClick("4") }
            AdjustableKeypadButton("5", Modifier.weight(1f), params) { onNumberClick("5") }
            AdjustableKeypadButton("6", Modifier.weight(1f), params) { onNumberClick("6") }
            AdjustableKeypadButton("−", Modifier.weight(1f), params) { onMinusClick() }
        }
        
        // 第三行：7 8 9 +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(params.keypadButtonSpacing.dp)
        ) {
            AdjustableKeypadButton("7", Modifier.weight(1f), params) { onNumberClick("7") }
            AdjustableKeypadButton("8", Modifier.weight(1f), params) { onNumberClick("8") }
            AdjustableKeypadButton("9", Modifier.weight(1f), params) { onNumberClick("9") }
            AdjustableKeypadButton("+", Modifier.weight(1f), params) { onPlusClick() }
        }
        
        // 第四行：再记 0 . 保存
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(params.keypadButtonSpacing.dp)
        ) {
            AdjustableKeypadButton("再记", Modifier.weight(1f), params) { onAgainClick() }
            AdjustableKeypadButton("0", Modifier.weight(1f), params) { onNumberClick("0") }
            AdjustableKeypadButton(".", Modifier.weight(1f), params) { onDotClick() }
            AdjustableKeypadButton("保存", Modifier.weight(1f), params, DesignTokens.BrandColors.Error) { onSaveClick() } // 红色保存按钮
        }
    }
}

@Composable
private fun AdjustableKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    params: LayoutAdjustmentParams,
    buttonColor: Color? = null, // 新增：可选的按钮颜色
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(params.keypadButtonSize.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor ?: MaterialTheme.colorScheme.surface,
            contentColor = if (buttonColor != null) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(params.keypadButtonCornerRadius.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = params.keypadTextSize.sp
            ),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AdjustmentPanel(
    params: LayoutAdjustmentParams,
    onParamsChange: (LayoutAdjustmentParams) -> Unit
) {
    val defaultParams = remember { LayoutAdjustmentParams.getDefault() }
    val clipboardManager = LocalClipboardManager.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标题和复制/粘贴功能
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "增强版调节面板",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 粘贴按钮
                    Button(
                        onClick = {
                            // 从剪贴板粘贴参数
                            val clipboardText = clipboardManager.getText()?.text ?: ""
                            if (clipboardText.isNotEmpty()) {
                                val parsedParams = LayoutAdjustmentParams.fromKotlinCode(clipboardText)
                                if (parsedParams != null) {
                                    onParamsChange(parsedParams)
                                }
                            }
                        },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = "粘贴参数",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("粘贴", fontSize = 12.sp)
                    }
                    
                    // 复制按钮
                    Button(
                        onClick = {
                            // 复制参数到剪贴板
                            val kotlinCode = with(LayoutAdjustmentParams.Companion) {
                                params.toKotlinCode()
                            }
                            clipboardManager.setText(AnnotatedString(kotlinCode))
                        },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "复制参数",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("复制", fontSize = 12.sp)
                    }
                }
            }
        }
        
        // === 分类图标区域 ===
        item {
            Text(
                text = "🎯 分类图标区域",
                style = MaterialTheme.typography.titleSmall,
                color = DesignTokens.BrandColors.Ledger,
                fontWeight = FontWeight.Medium
            )
        }
        
        item {
            SliderWithResetButton(
                label = "图标大小",
                value = params.categoryIconSize,
                valueRange = 8f..80f,  // 大幅扩大范围
                defaultValue = defaultParams.categoryIconSize,
                onValueChange = { onParamsChange(params.copy(categoryIconSize = it)) },
                onReset = { onParamsChange(params.copy(categoryIconSize = defaultParams.categoryIconSize)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "文字大小",
                value = params.categoryTextSize,
                valueRange = 6f..20f,
                defaultValue = defaultParams.categoryTextSize,
                onValueChange = { onParamsChange(params.copy(categoryTextSize = it)) },
                onReset = { onParamsChange(params.copy(categoryTextSize = defaultParams.categoryTextSize)) },
                unit = "sp"
            )
        }
        
        item {
            IntSliderWithResetButton(
                label = "网格列数",
                value = params.gridColumnCount,
                valueRange = 3..8,
                defaultValue = defaultParams.gridColumnCount,
                onValueChange = { onParamsChange(params.copy(gridColumnCount = it)) },
                onReset = { onParamsChange(params.copy(gridColumnCount = defaultParams.gridColumnCount)) }
            )
        }
        
        item {
            SliderWithResetButton(
                label = "水平间距",
                value = params.categoryHorizontalSpacing,
                valueRange = 0f..60f,
                defaultValue = defaultParams.categoryHorizontalSpacing,
                onValueChange = { onParamsChange(params.copy(categoryHorizontalSpacing = it)) },
                onReset = { onParamsChange(params.copy(categoryHorizontalSpacing = defaultParams.categoryHorizontalSpacing)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "垂直间距",
                value = params.categoryVerticalSpacing,
                valueRange = 0f..80f,
                defaultValue = defaultParams.categoryVerticalSpacing,
                onValueChange = { onParamsChange(params.copy(categoryVerticalSpacing = it)) },
                onReset = { onParamsChange(params.copy(categoryVerticalSpacing = defaultParams.categoryVerticalSpacing)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "网格边距",
                value = params.categoryGridPadding,
                valueRange = 0f..80f,
                defaultValue = defaultParams.categoryGridPadding,
                onValueChange = { onParamsChange(params.copy(categoryGridPadding = it)) },
                onReset = { onParamsChange(params.copy(categoryGridPadding = defaultParams.categoryGridPadding)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "卡片圆角",
                value = params.categoryCardCornerRadius,
                valueRange = 0f..32f,
                defaultValue = defaultParams.categoryCardCornerRadius,
                onValueChange = { onParamsChange(params.copy(categoryCardCornerRadius = it)) },
                onReset = { onParamsChange(params.copy(categoryCardCornerRadius = defaultParams.categoryCardCornerRadius)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "卡片内边距",
                value = params.categoryCardPadding,
                valueRange = 0f..24f,
                defaultValue = defaultParams.categoryCardPadding,
                onValueChange = { onParamsChange(params.copy(categoryCardPadding = it)) },
                onReset = { onParamsChange(params.copy(categoryCardPadding = defaultParams.categoryCardPadding)) },
                unit = "dp"
            )
        }
        
        // === Tab切换区域 ===
        item {
            Text(
                text = "🔄 Tab切换区域",
                style = MaterialTheme.typography.titleSmall,
                color = DesignTokens.BrandColors.Success,
                fontWeight = FontWeight.Medium
            )
        }
        
        item {
            SliderWithResetButton(
                label = "Tab高度",
                value = params.tabRowHeight,
                valueRange = 24f..80f,
                defaultValue = defaultParams.tabRowHeight,
                onValueChange = { onParamsChange(params.copy(tabRowHeight = it)) },
                onReset = { onParamsChange(params.copy(tabRowHeight = defaultParams.tabRowHeight)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "Tab宽度",
                value = params.tabRowWidth,
                valueRange = 120f..400f,
                defaultValue = defaultParams.tabRowWidth,
                onValueChange = { onParamsChange(params.copy(tabRowWidth = it)) },
                onReset = { onParamsChange(params.copy(tabRowWidth = defaultParams.tabRowWidth)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "Tab圆角",
                value = params.tabCornerRadius,
                valueRange = 0f..24f,
                defaultValue = defaultParams.tabCornerRadius,
                onValueChange = { onParamsChange(params.copy(tabCornerRadius = it)) },
                onReset = { onParamsChange(params.copy(tabCornerRadius = defaultParams.tabCornerRadius)) },
                unit = "dp"
            )
        }
        
        // === 输入区域布局 ===
        item {
            Text(
                text = "📝 输入区域布局",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF9C27B0), // Purple
                fontWeight = FontWeight.Medium
            )
        }
        
        item {
            SliderWithResetButton(
                label = "总高度",
                value = params.inputAreaHeight,
                valueRange = 100f..800f,  // 大幅扩大范围
                defaultValue = defaultParams.inputAreaHeight,
                onValueChange = { onParamsChange(params.copy(inputAreaHeight = it)) },
                onReset = { onParamsChange(params.copy(inputAreaHeight = defaultParams.inputAreaHeight)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "区域圆角",
                value = params.inputAreaCornerRadius,
                valueRange = 0f..32f,
                defaultValue = defaultParams.inputAreaCornerRadius,
                onValueChange = { onParamsChange(params.copy(inputAreaCornerRadius = it)) },
                onReset = { onParamsChange(params.copy(inputAreaCornerRadius = defaultParams.inputAreaCornerRadius)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "区域边距",
                value = params.inputAreaPadding,
                valueRange = 0f..40f,
                defaultValue = defaultParams.inputAreaPadding,
                onValueChange = { onParamsChange(params.copy(inputAreaPadding = it)) },
                onReset = { onParamsChange(params.copy(inputAreaPadding = defaultParams.inputAreaPadding)) },
                unit = "dp"
            )
        }
        
        // === 备注区域细节 ===
        item {
            Text(
                text = "💬 备注区域细节",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF795548), // Brown
                fontWeight = FontWeight.Medium
            )
        }
        
        item {
            SliderWithResetButton(
                label = "备注顶部边距",
                value = params.noteFieldTopPadding,
                valueRange = 0f..40f,
                defaultValue = defaultParams.noteFieldTopPadding,
                onValueChange = { onParamsChange(params.copy(noteFieldTopPadding = it)) },
                onReset = { onParamsChange(params.copy(noteFieldTopPadding = defaultParams.noteFieldTopPadding)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "备注底部边距",
                value = params.noteFieldBottomPadding,
                valueRange = 0f..32f,
                defaultValue = defaultParams.noteFieldBottomPadding,
                onValueChange = { onParamsChange(params.copy(noteFieldBottomPadding = it)) },
                onReset = { onParamsChange(params.copy(noteFieldBottomPadding = defaultParams.noteFieldBottomPadding)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "备注文字大小",
                value = params.noteTextSize,
                valueRange = 10f..20f,
                defaultValue = defaultParams.noteTextSize,
                onValueChange = { onParamsChange(params.copy(noteTextSize = it)) },
                onReset = { onParamsChange(params.copy(noteTextSize = defaultParams.noteTextSize)) },
                unit = "sp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "备注框左右内边距",
                value = params.noteFieldHorizontalPadding,
                valueRange = 0f..24f,
                defaultValue = defaultParams.noteFieldHorizontalPadding,
                onValueChange = { onParamsChange(params.copy(noteFieldHorizontalPadding = it)) },
                onReset = { onParamsChange(params.copy(noteFieldHorizontalPadding = defaultParams.noteFieldHorizontalPadding)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "备注框内容内边距",
                value = params.noteFieldContentPadding,
                valueRange = 0f..32f,
                defaultValue = defaultParams.noteFieldContentPadding,
                onValueChange = { onParamsChange(params.copy(noteFieldContentPadding = it)) },
                onReset = { onParamsChange(params.copy(noteFieldContentPadding = defaultParams.noteFieldContentPadding)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "备注到金额间距",
                value = params.noteToAmountSpacing,
                valueRange = 0f..32f,
                defaultValue = defaultParams.noteToAmountSpacing,
                onValueChange = { onParamsChange(params.copy(noteToAmountSpacing = it)) },
                onReset = { onParamsChange(params.copy(noteToAmountSpacing = defaultParams.noteToAmountSpacing)) },
                unit = "dp"
            )
        }
        
        // === 金额显示区域 ===
        item {
            Text(
                text = "💰 金额显示区域",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFFF5722), // Deep Orange
                fontWeight = FontWeight.Medium
            )
        }
        
        item {
            SliderWithResetButton(
                label = "金额文字大小",
                value = params.amountTextSize,
                valueRange = 16f..48f,
                defaultValue = defaultParams.amountTextSize,
                onValueChange = { onParamsChange(params.copy(amountTextSize = it)) },
                onReset = { onParamsChange(params.copy(amountTextSize = defaultParams.amountTextSize)) },
                unit = "sp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "金额文字右边距",
                value = params.amountTextPadding,
                valueRange = 0f..32f,
                defaultValue = defaultParams.amountTextPadding,
                onValueChange = { onParamsChange(params.copy(amountTextPadding = it)) },
                onReset = { onParamsChange(params.copy(amountTextPadding = defaultParams.amountTextPadding)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "账户文字大小",
                value = params.accountTextSize,
                valueRange = 10f..20f,
                defaultValue = defaultParams.accountTextSize,
                onValueChange = { onParamsChange(params.copy(accountTextSize = it)) },
                onReset = { onParamsChange(params.copy(accountTextSize = defaultParams.accountTextSize)) },
                unit = "sp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "账户文字左边距",
                value = params.accountTextLeftPadding,
                valueRange = 0f..32f,
                defaultValue = defaultParams.accountTextLeftPadding,
                onValueChange = { onParamsChange(params.copy(accountTextLeftPadding = it)) },
                onReset = { onParamsChange(params.copy(accountTextLeftPadding = defaultParams.accountTextLeftPadding)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "账户与备注间距",
                value = params.accountToNoteSpacing,
                valueRange = 0f..32f,
                defaultValue = defaultParams.accountToNoteSpacing,
                onValueChange = { onParamsChange(params.copy(accountToNoteSpacing = it)) },
                onReset = { onParamsChange(params.copy(accountToNoteSpacing = defaultParams.accountToNoteSpacing)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "金额到键盘间距",
                value = params.amountToKeypadSpacing,
                valueRange = 0f..32f,
                defaultValue = defaultParams.amountToKeypadSpacing,
                onValueChange = { onParamsChange(params.copy(amountToKeypadSpacing = it)) },
                onReset = { onParamsChange(params.copy(amountToKeypadSpacing = defaultParams.amountToKeypadSpacing)) },
                unit = "dp"
            )
        }
        
        // === 键盘区域 ===
        item {
            Text(
                text = "🔢 键盘区域",
                style = MaterialTheme.typography.titleSmall,
                color = DesignTokens.BrandColors.Error,
                fontWeight = FontWeight.Medium
            )
        }
        
        item {
            SliderWithResetButton(
                label = "按钮高度",
                value = params.keypadButtonSize,
                valueRange = 24f..120f,  // 大幅扩大范围
                defaultValue = defaultParams.keypadButtonSize,
                onValueChange = { onParamsChange(params.copy(keypadButtonSize = it)) },
                onReset = { onParamsChange(params.copy(keypadButtonSize = defaultParams.keypadButtonSize)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "按钮水平间距",
                value = params.keypadButtonSpacing,
                valueRange = 0f..32f,
                defaultValue = defaultParams.keypadButtonSpacing,
                onValueChange = { onParamsChange(params.copy(keypadButtonSpacing = it)) },
                onReset = { onParamsChange(params.copy(keypadButtonSpacing = defaultParams.keypadButtonSpacing)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "按钮行间距",
                value = params.keypadRowSpacing,
                valueRange = 0f..32f,
                defaultValue = defaultParams.keypadRowSpacing,
                onValueChange = { onParamsChange(params.copy(keypadRowSpacing = it)) },
                onReset = { onParamsChange(params.copy(keypadRowSpacing = defaultParams.keypadRowSpacing)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "按钮圆角",
                value = params.keypadButtonCornerRadius,
                valueRange = 0f..24f,
                defaultValue = defaultParams.keypadButtonCornerRadius,
                onValueChange = { onParamsChange(params.copy(keypadButtonCornerRadius = it)) },
                onReset = { onParamsChange(params.copy(keypadButtonCornerRadius = defaultParams.keypadButtonCornerRadius)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "按钮文字大小",
                value = params.keypadTextSize,
                valueRange = 10f..24f,
                defaultValue = defaultParams.keypadTextSize,
                onValueChange = { onParamsChange(params.copy(keypadTextSize = it)) },
                onReset = { onParamsChange(params.copy(keypadTextSize = defaultParams.keypadTextSize)) },
                unit = "sp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "键盘底部边距",
                value = params.keypadBottomPadding,
                valueRange = 0f..80f,
                defaultValue = defaultParams.keypadBottomPadding,
                onValueChange = { onParamsChange(params.copy(keypadBottomPadding = it)) },
                onReset = { onParamsChange(params.copy(keypadBottomPadding = defaultParams.keypadBottomPadding)) },
                unit = "dp"
            )
        }
        
        item {
            SliderWithResetButton(
                label = "键盘左右边距",
                value = params.keypadHorizontalPadding,
                valueRange = 0f..80f,
                defaultValue = defaultParams.keypadHorizontalPadding,
                onValueChange = { onParamsChange(params.copy(keypadHorizontalPadding = it)) },
                onReset = { onParamsChange(params.copy(keypadHorizontalPadding = defaultParams.keypadHorizontalPadding)) },
                unit = "dp"
            )
        }
        
        // === 调试模式 ===
        item {
            Text(
                text = "🔧 调试模式",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF9C27B0), // Purple
                fontWeight = FontWeight.Medium
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "启用调试模式",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
                Switch(
                    checked = params.debugMode,
                    onCheckedChange = { onParamsChange(params.copy(debugMode = it)) },
                    modifier = Modifier.scale(0.7f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "显示边界框",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
                Switch(
                    checked = params.showBorders,
                    onCheckedChange = { onParamsChange(params.copy(showBorders = it)) },
                    modifier = Modifier.scale(0.7f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "显示间距数值",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
                Switch(
                    checked = params.showSpacingValues,
                    onCheckedChange = { onParamsChange(params.copy(showSpacingValues = it)) },
                    modifier = Modifier.scale(0.7f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "显示Padding区域",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
                Switch(
                    checked = params.showPaddingAreas,
                    onCheckedChange = { onParamsChange(params.copy(showPaddingAreas = it)) },
                    modifier = Modifier.scale(0.7f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "显示内容区域",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
                Switch(
                    checked = params.showContentAreas,
                    onCheckedChange = { onParamsChange(params.copy(showContentAreas = it)) },
                    modifier = Modifier.scale(0.7f)
                )
            }
        }
        
        // 调试说明卡片
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7FAFC)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "📊 调试色彩说明",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(DebugColors.PaddingBorder))
                        Text("红色-Padding", fontSize = 9.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(DebugColors.SpacingBorder))
                        Text("蓝色-Spacing", fontSize = 9.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(DebugColors.ContentBorder))
                        Text("绿色-Content", fontSize = 9.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(DebugColors.MarginBorder))
                        Text("黄色-Margin", fontSize = 9.sp)
                    }
                }
            }
        }
        
        // === 参数预览卡片 ===
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "📊 当前参数预览",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "图标: ${params.categoryIconSize.toInt()}dp (${params.gridColumnCount}列)",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "间距: H${params.categoryHorizontalSpacing.toInt()} V${params.categoryVerticalSpacing.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "键盘: ${params.keypadButtonSize.toInt()}dp (${params.keypadTextSize.toInt()}sp) 边距:${params.keypadHorizontalPadding.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "输入区: ${params.inputAreaHeight.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Tab: ${params.tabRowWidth.toInt()}×${params.tabRowHeight.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "调试: ${if (params.debugMode) "已启用" else "已关闭"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = if (params.debugMode) DebugColors.ContentBorder else Color.Gray
                    )
                }
            }
        }
    }
}

// 增强版滑块组件 - 带重置按钮
@Composable
private fun SliderWithResetButton(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    defaultValue: Float,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit,
    unit: String = ""
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${value.toInt()}$unit",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
                // 重置按钮
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "重置${label}",
                        modifier = Modifier.size(12.dp),
                        tint = if (value != defaultValue) {
                            DesignTokens.BrandColors.Ledger
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.height(24.dp)
        )
    }
}

// 整数版滑块组件
@Composable
private fun IntSliderWithResetButton(
    label: String,
    value: Int,
    valueRange: IntRange,
    defaultValue: Int,
    onValueChange: (Int) -> Unit,
    onReset: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
                // 重置按钮
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "重置${label}",
                        modifier = Modifier.size(12.dp),
                        tint = if (value != defaultValue) {
                            DesignTokens.BrandColors.Ledger
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = valueRange.last - valueRange.first - 1,
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    unit: String = ""
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${value.toInt()}$unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
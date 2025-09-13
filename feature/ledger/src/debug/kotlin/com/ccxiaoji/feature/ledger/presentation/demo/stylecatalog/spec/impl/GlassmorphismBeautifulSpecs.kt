package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.datetime.toJavaInstant

/**
 * 玻璃拟态美观风格 - 2025年流行趋势
 * 特点：毛玻璃效果、半透明层叠、柔和光晕、深度感知
 */
class GlassmorphismBeautifulSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.BALANCED
    override val description = "玻璃拟态设计，透明层叠与光影效果"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = GlassBeautifulListSpec()
    override val itemSpec = GlassBeautifulItemSpec()
    override val headerSpec = GlassBeautifulHeaderSpec()
    override val filterSpec = GlassBeautifulFilterSpec()
    override val formSpec = GlassBeautifulFormSpec()
    override val dialogSpec = GlassBeautifulDialogSpec()
    override val chartsSpec = GlassBeautifulChartsSpec()
    override val settingsSpec = GlassBeautifulSettingsSpec()
}

// ==================== 玻璃拟态颜色系统 ====================
object GlassColors {
    // 背景渐变
    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5FF), // Light Blue
            Color(0xFFF3E5F5), // Light Purple
            Color(0xFFFFF9E6)  // Light Yellow
        )
    )
    
    // 玻璃效果颜色
    val GlassWhite = Color.White.copy(alpha = 0.25f)
    val GlassBorder = Color.White.copy(alpha = 0.5f)
    val GlassShadow = Color(0x1A000000)
    
    // 彩色高光
    val AccentBlue = Color(0xFF2196F3)
    val AccentPurple = Color(0xFF9C27B0)
    val AccentGreen = Color(0xFF4CAF50)
    val AccentRed = Color(0xFFF44336)
    val AccentOrange = Color(0xFFFF9800)
    
    // 文字颜色
    val TextDark = Color(0xFF1A1A2E)
    val TextMedium = Color(0xFF16213E)
    val TextLight = Color(0xFF3D5A80)
}

// ==================== 玻璃效果修饰器 ====================
@Composable
fun Modifier.glassEffect(
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 10.dp
): Modifier {
    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.1f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.6f),
                    Color.White.copy(alpha = 0.2f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
        .blur(radiusX = 0.dp, radiusY = 0.dp) // 注意：实际模糊效果需要背景支持
}

// ==================== List Spec - 玻璃层叠列表 ====================
class GlassBeautifulListSpec : ListSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        var isHovered by remember { mutableStateOf(false) }
        val animatedElevation by animateFloatAsState(
            targetValue = if (isHovered) 8f else 0f,
            animationSpec = tween(200)
        )
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 彩色光晕背景
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 4.dp)
                    .blur(radius = 20.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                if (transaction.type == TransactionType.EXPENSE)
                                    GlassColors.AccentRed.copy(alpha = 0.3f)
                                else
                                    GlassColors.AccentGreen.copy(alpha = 0.3f),
                                GlassColors.AccentPurple.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
            
            // 玻璃卡片
            Card(
                onClick = {
                    isHovered = true
                    onClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = animatedElevation.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 圆形玻璃图标容器
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        if (transaction.type == TransactionType.EXPENSE)
                                            GlassColors.AccentRed.copy(alpha = 0.3f)
                                        else
                                            GlassColors.AccentGreen.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = GlassColors.GlassBorder,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(transaction.category),
                            contentDescription = null,
                            tint = if (transaction.type == TransactionType.EXPENSE)
                                GlassColors.AccentRed
                            else
                                GlassColors.AccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 信息区域
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = transaction.category?.name ?: "未分类",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassColors.TextDark
                        )
                        
                        if (!transaction.note.isNullOrEmpty()) {
                            Text(
                                text = transaction.note,
                                fontSize = 14.sp,
                                color = GlassColors.TextMedium.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 玻璃标签
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                border = BorderStroke(
                                    0.5.dp,
                                    GlassColors.GlassBorder
                                )
                            ) {
                                Text(
                                    text = transaction.account?.name ?: "现金",
                                    fontSize = 12.sp,
                                    color = GlassColors.TextLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            
                            Text(
                                text = DateTimeFormatter.ofPattern("HH:mm").format(
                                    transaction.dateTime.toJavaInstant()
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDateTime()
                                ),
                                fontSize = 12.sp,
                                color = GlassColors.TextLight.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // 金额 - 发光效果
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // 光晕
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .blur(radius = 15.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            if (transaction.type == TransactionType.EXPENSE)
                                                GlassColors.AccentRed.copy(alpha = 0.3f)
                                            else
                                                GlassColors.AccentGreen.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = if (transaction.type == TransactionType.EXPENSE) "-" else "+",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (transaction.type == TransactionType.EXPENSE)
                                    GlassColors.AccentRed
                                else
                                    GlassColors.AccentGreen
                            )
                            Text(
                                text = "¥${String.format("%.2f", transaction.amount)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (transaction.type == TransactionType.EXPENSE)
                                    GlassColors.AccentRed
                                else
                                    GlassColors.AccentGreen
                            )
                        }
                    }
                }
            }
        }
        
        LaunchedEffect(isHovered) {
            if (isHovered) {
                kotlinx.coroutines.delay(200)
                isHovered = false
            }
        }
    }
    
    private fun getCategoryIcon(category: DemoCategory?): ImageVector {
        return when (category?.icon) {
            "restaurant" -> Icons.Filled.Restaurant
            "shopping_cart" -> Icons.Filled.ShoppingCart
            "directions_car" -> Icons.Filled.DirectionsCar
            "home" -> Icons.Filled.Home
            "phone" -> Icons.Filled.Phone
            "medical" -> Icons.Filled.LocalHospital
            "school" -> Icons.Filled.School
            "flight" -> Icons.Filled.Flight
            "work" -> Icons.Filled.Work
            "savings" -> Icons.Filled.Savings
            else -> Icons.Filled.Category
        }
    }
    
    override fun getGroupingStrategy() = GroupingStrategy.BY_DAY
    
    @Composable
    override fun RenderGroupHeader(
        date: LocalDate,
        totalIncome: Double,
        totalExpense: Double,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 16.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when {
                                date == LocalDate.now() -> "今天"
                                date == LocalDate.now().minusDays(1) -> "昨天"
                                else -> DateTimeFormatter.ofPattern("MM月dd日").format(date)
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassColors.TextDark
                        )
                        Text(
                            text = DateTimeFormatter.ofPattern("EEEE").format(date),
                            fontSize = 14.sp,
                            color = GlassColors.TextMedium.copy(alpha = 0.7f)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (totalIncome > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GlassColors.AccentGreen.copy(alpha = 0.1f),
                                border = BorderStroke(
                                    0.5.dp,
                                    GlassColors.AccentGreen.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "+¥${String.format("%.0f", totalIncome)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GlassColors.AccentGreen,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        
                        if (totalExpense > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GlassColors.AccentRed.copy(alpha = 0.1f),
                                border = BorderStroke(
                                    0.5.dp,
                                    GlassColors.AccentRed.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "-¥${String.format("%.0f", totalExpense)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GlassColors.AccentRed,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    override fun ListContainer(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier.background(
                brush = GlassColors.BackgroundGradient
            )
        ) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 96.dp
            DemoDensity.Medium -> 116.dp
        }
    }
    
    override fun getItemSpacing(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 6.dp
            DemoDensity.Medium -> 8.dp
        }
    }
}

// ==================== Item Spec ====================
class GlassBeautifulItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.CARD
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.BELOW,
        notePosition = ItemSpec.Position.BELOW,
        dateTimePosition = ItemSpec.Position.BELOW
    )
    
    override fun showIcons() = true
    override fun showDividers() = false
}

// ==================== Header Spec - 玻璃概览卡片 ====================
class GlassBeautifulHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier.padding(16.dp)
        ) {
            // 彩色背景光晕
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 8.dp)
                    .blur(radius = 30.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                GlassColors.AccentBlue.copy(alpha = 0.4f),
                                GlassColors.AccentPurple.copy(alpha = 0.4f),
                                GlassColors.AccentOrange.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )
            
            // 玻璃卡片
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 24.dp, borderWidth = 1.5.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "财务概览",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassColors.TextDark
                        )
                        
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(
                                0.5.dp,
                                GlassColors.GlassBorder
                            )
                        ) {
                            Icon(
                                Icons.Outlined.BarChart,
                                contentDescription = null,
                                tint = GlassColors.AccentBlue,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 结余显示
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "本月结余",
                            fontSize = 14.sp,
                            color = GlassColors.TextMedium.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "¥",
                                fontSize = 24.sp,
                                color = if (balance >= 0) GlassColors.AccentBlue else GlassColors.AccentRed,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                text = String.format("%,.0f", kotlin.math.abs(balance)),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (balance >= 0) GlassColors.AccentBlue else GlassColors.AccentRed
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 收支详情
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 收入
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GlassColors.AccentGreen.copy(alpha = 0.1f),
                                border = BorderStroke(
                                    0.5.dp,
                                    GlassColors.AccentGreen.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = GlassColors.AccentGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "收入",
                                fontSize = 12.sp,
                                color = GlassColors.TextMedium.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "¥${String.format("%,.0f", income)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassColors.AccentGreen
                            )
                        }
                        
                        // 分隔线
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(60.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            GlassColors.GlassBorder,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        // 支出
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GlassColors.AccentRed.copy(alpha = 0.1f),
                                border = BorderStroke(
                                    0.5.dp,
                                    GlassColors.AccentRed.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = GlassColors.AccentRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "支出",
                                fontSize = 12.sp,
                                color = GlassColors.TextMedium.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "¥${String.format("%,.0f", expense)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GlassColors.AccentRed
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.LARGE
    override fun showDateSelector() = true
    override fun showQuickStats() = true
}

// ==================== 其他 Specs 实现 ====================
class GlassBeautifulFilterSpec : FilterSpec {
    @Composable
    override fun RenderFilterBar(
        selectedDateRange: DateRange,
        selectedCategories: List<DemoCategory>,
        selectedAccounts: List<DemoAccount>,
        searchQuery: String,
        onDateRangeChange: (DateRange) -> Unit,
        onCategoryChange: (List<DemoCategory>) -> Unit,
        onAccountChange: (List<DemoAccount>) -> Unit,
        onSearchChange: (String) -> Unit,
        modifier: Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 玻璃筛选芯片
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text(selectedDateRange.label) },
                leadingIcon = {
                    Icon(Icons.Outlined.DateRange, contentDescription = null, Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    labelColor = GlassColors.TextDark,
                    iconColor = GlassColors.TextDark,
                    selectedContainerColor = GlassColors.AccentBlue.copy(alpha = 0.2f),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = GlassColors.GlassBorder,
                    selectedBorderColor = GlassColors.AccentBlue.copy(alpha = 0.5f)
                )
            )
        }
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.FLOATING
    override fun isCollapsible() = true
}

class GlassBeautifulFormSpec : FormSpec {
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.MODAL
    override fun getFieldOrder() = listOf("amount", "category", "account", "note", "date")
    override fun showCalculator() = true
}

class GlassBeautifulDialogSpec : DialogSpec {
    @Composable
    override fun RenderDialog(
        title: String,
        content: @Composable () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier
    ) {
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.FLOATING
    override fun showOverlay() = true
}

class GlassBeautifulChartsSpec : ChartsSpec {
    @Composable
    override fun RenderPieChart(data: List<ChartData>, modifier: Modifier) {
        Box(
            modifier = modifier
                .height(240.dp)
                .glassEffect(cornerRadius = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("饼图", color = GlassColors.TextMedium)
        }
    }
    
    @Composable
    override fun RenderLineChart(data: List<ChartData>, modifier: Modifier) {
        Box(
            modifier = modifier
                .height(240.dp)
                .glassEffect(cornerRadius = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("趋势图", color = GlassColors.TextMedium)
        }
    }
    
    @Composable
    override fun RenderBarChart(data: List<ChartData>, modifier: Modifier) {
        Box(
            modifier = modifier
                .height(240.dp)
                .glassEffect(cornerRadius = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("柱状图", color = GlassColors.TextMedium)
        }
    }
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.GRADIENT
    override fun showLegend() = true
    override fun showGrid() = false
}

class GlassBeautifulSettingsSpec : SettingsSpec {
    @Composable
    override fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .glassEffect(cornerRadius = 16.dp),
            color = Color.Transparent
        ) {
            ListItem(
                headlineContent = { Text(title, color = GlassColors.TextDark) },
                supportingContent = subtitle?.let { { Text(it, color = GlassColors.TextMedium) } },
                leadingContent = icon,
                trailingContent = trailing,
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
    
    @Composable
    override fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    ) {
        Column(modifier = modifier) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassColors.TextMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            items()
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.CARDS
}
package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
 * 现代美观风格 - 灵感来自 Monarch Money, Ivy Wallet 等顶级财务应用
 * 特点：大胆的排版、鲜艳的渐变、流畅的动画、清晰的层次
 */
class ModernBeautifulSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.BALANCED
    override val description = "现代财务应用风格，清晰层次与视觉冲击力"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = ModernBeautifulListSpec()
    override val itemSpec = ModernBeautifulItemSpec()
    override val headerSpec = ModernBeautifulHeaderSpec()
    override val filterSpec = ModernBeautifulFilterSpec()
    override val formSpec = ModernBeautifulFormSpec()
    override val dialogSpec = ModernBeautifulDialogSpec()
    override val chartsSpec = ModernBeautifulChartsSpec()
    override val settingsSpec = ModernBeautifulSettingsSpec()
}

// ==================== 美观配色方案 ====================
object ModernColors {
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF8B5CF6)  // Purple
        )
    )
    
    val IncomeGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFF34D399)  // Light Emerald
        )
    )
    
    val ExpenseGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEF4444), // Red
            Color(0xFFF87171)  // Light Red
        )
    )
    
    val CardBackground = Color(0xFFF9FAFB)
    val CardBackgroundDark = Color(0xFF1F2937)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF9CA3AF)
    val BorderLight = Color(0xFFE5E7EB)
}

// ==================== List Spec - 流畅列表体验 ====================
class ModernBeautifulListSpec : ListSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .scale(scale),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 4.dp
            ),
            shape = RoundedCornerShape(20.dp),
            onClick = {
                isPressed = true
                onClick()
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类图标容器 - 渐变背景
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            brush = if (transaction.type == TransactionType.EXPENSE) 
                                ModernColors.ExpenseGradient 
                            else 
                                ModernColors.IncomeGradient,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 交易信息 - 清晰的层次
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = transaction.category?.name ?: "未分类",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernColors.TextPrimary
                    )
                    
                    if (!transaction.note.isNullOrEmpty()) {
                        Text(
                            text = transaction.note,
                            fontSize = 14.sp,
                            color = ModernColors.TextSecondary,
                            maxLines = 1
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 账户标签
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = transaction.account?.name ?: "现金",
                                fontSize = 12.sp,
                                color = ModernColors.TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        // 时间
                        Text(
                            text = DateTimeFormatter.ofPattern("HH:mm").format(
                                transaction.dateTime.toJavaInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDateTime()
                            ),
                            fontSize = 12.sp,
                            color = ModernColors.TextMuted
                        )
                    }
                }
                
                // 金额 - 突出显示
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (transaction.type == TransactionType.EXPENSE) "-" else "+",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (transaction.type == TransactionType.EXPENSE) 
                                Color(0xFFEF4444) 
                            else 
                                Color(0xFF10B981)
                        )
                        Text(
                            text = "¥",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (transaction.type == TransactionType.EXPENSE) 
                                Color(0xFFEF4444) 
                            else 
                                Color(0xFF10B981)
                        )
                        Text(
                            text = String.format("%.2f", transaction.amount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.type == TransactionType.EXPENSE) 
                                Color(0xFFEF4444) 
                            else 
                                Color(0xFF10B981)
                        )
                    }
                }
            }
        }
        
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(100)
                isPressed = false
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
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    color = ModernColors.TextPrimary
                )
                Text(
                    text = DateTimeFormatter.ofPattern("EEEE").format(date),
                    fontSize = 14.sp,
                    color = ModernColors.TextSecondary
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (totalIncome > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "+¥${String.format("%.2f", totalIncome)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF10B981)
                        )
                    }
                }
                
                if (totalExpense > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFEF4444), CircleShape)
                        )
                        Text(
                            text = "-¥${String.format("%.2f", totalExpense)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 0.5.dp,
            color = ModernColors.BorderLight
        )
    }
    
    @Composable
    override fun ListContainer(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier.background(Color(0xFFFAFAFB))
        ) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 88.dp
            DemoDensity.Medium -> 108.dp
        }
    }
    
    override fun getItemSpacing(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 4.dp
            DemoDensity.Medium -> 6.dp
        }
    }
}

// ==================== Item Spec ====================
class ModernBeautifulItemSpec : ItemSpec {
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

// ==================== Header Spec - 醒目的概览卡片 ====================
class ModernBeautifulHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Card(
            modifier = modifier.padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = ModernColors.PrimaryGradient
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "本月概览",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 结余 - 大字体突出
                    Column {
                        Text(
                            text = "结余",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "¥${String.format("%,.2f", balance)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 收支明细
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "收入",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "¥${String.format("%,.2f", income)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "支出",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "¥${String.format("%,.2f", expense)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
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

// ==================== Filter Spec ====================
class ModernBeautifulFilterSpec : FilterSpec {
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 简化的筛选器实现
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text(selectedDateRange.label) },
                leadingIcon = {
                    Icon(Icons.Outlined.DateRange, contentDescription = null, Modifier.size(16.dp))
                }
            )
            
            FilterChip(
                selected = selectedCategories.isNotEmpty(),
                onClick = { },
                label = { Text("分类") },
                leadingIcon = {
                    Icon(Icons.Outlined.Category, contentDescription = null, Modifier.size(16.dp))
                }
            )
            
            FilterChip(
                selected = selectedAccounts.isNotEmpty(),
                onClick = { },
                label = { Text("账户") },
                leadingIcon = {
                    Icon(Icons.Outlined.AccountBalance, contentDescription = null, Modifier.size(16.dp))
                }
            )
        }
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.CHIPS
    override fun isCollapsible() = true
}

// ==================== 其他 Specs 实现 ====================
class ModernBeautifulFormSpec : FormSpec {
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (transaction == null) "添加交易" else "编辑交易",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ModernColors.TextPrimary
            )
        }
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.VERTICAL
    override fun getFieldOrder() = listOf("amount", "category", "account", "note", "date")
    override fun showCalculator() = true
}

class ModernBeautifulDialogSpec : DialogSpec {
    @Composable
    override fun RenderDialog(
        title: String,
        content: @Composable () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { 
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = content,
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF6366F1)
                    )
                ) {
                    Text("确认", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            shape = RoundedCornerShape(20.dp),
            modifier = modifier
        )
    }
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.MATERIAL
    override fun showOverlay() = true
}

class ModernBeautifulChartsSpec : ChartsSpec {
    @Composable
    override fun RenderPieChart(data: List<ChartData>, modifier: Modifier) {
        Card(
            modifier = modifier.height(240.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "饼图",
                    fontSize = 16.sp,
                    color = ModernColors.TextSecondary
                )
            }
        }
    }
    
    @Composable
    override fun RenderLineChart(data: List<ChartData>, modifier: Modifier) {
        Card(
            modifier = modifier.height(240.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "趋势图",
                    fontSize = 16.sp,
                    color = ModernColors.TextSecondary
                )
            }
        }
    }
    
    @Composable
    override fun RenderBarChart(data: List<ChartData>, modifier: Modifier) {
        Card(
            modifier = modifier.height(240.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "柱状图",
                    fontSize = 16.sp,
                    color = ModernColors.TextSecondary
                )
            }
        }
    }
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.GRADIENT
    override fun showLegend() = true
    override fun showGrid() = true
}

class ModernBeautifulSettingsSpec : SettingsSpec {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            ListItem(
                headlineContent = { 
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ModernColors.TextPrimary
                    ) 
                },
                supportingContent = subtitle?.let { { 
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = ModernColors.TextSecondary
                    ) 
                } },
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
                color = ModernColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            items()
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.CARDS
}
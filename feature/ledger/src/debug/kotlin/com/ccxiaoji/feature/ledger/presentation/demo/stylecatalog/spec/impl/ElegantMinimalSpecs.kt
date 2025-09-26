package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 优雅极简主义规格实现
 * 
 * 设计原则：
 * 1. 精致的留白 - 充足但不过度的空间
 * 2. 细微的阴影 - 仅用于分层，不抢眼
 * 3. 单色调色板 - 黑白灰为主，点缀色极少
 * 4. 精心的排版 - 字体大小和粗细的层次
 * 5. 微妙的动效 - 平滑但不张扬
 * 
 * 参考：Stripe、Linear、Vercel的设计语言
 */
class ElegantMinimalSpecs : SpecsRegistry.StyleSpecs() {
    
    // 优雅的色彩系统
    object Colors {
        val Black = Color(0xFF000000)
        val DarkGray = Color(0xFF1A1A1A)
        val Gray900 = Color(0xFF404040)
        val Gray700 = Color(0xFF666666)
        val Gray500 = Color(0xFF999999)
        val Gray300 = Color(0xFFCCCCCC)
        val Gray100 = Color(0xFFE6E6E6)
        val Gray50 = Color(0xFFF5F5F5)
        val White = Color(0xFFFFFFFF)
        
        // 极少的强调色
        val Accent = Color(0xFF0066FF)  // 优雅的蓝色
        val Success = Color(0xFF00C896)  // 柔和的绿色
        val Danger = Color(0xFFFF3B30)   // 警告红
    }
    
    // 精致的间距系统
    object Spacing {
        val xs = 4.dp
        val s = 8.dp
        val m = 16.dp
        val l = 24.dp
        val xl = 32.dp
        val xxl = 48.dp
    }
    
    // 优雅的圆角
    object Radius {
        val small = 6.dp
        val medium = 8.dp
        val large = 12.dp
        val full = 100.dp
    }
    
    override val listSpec = ElegantMinimalListSpec()
    override val itemSpec = ElegantMinimalItemSpec()
    override val headerSpec = ElegantMinimalHeaderSpec()
    override val filterSpec = ElegantMinimalFilterSpec()
    override val formSpec = ElegantMinimalFormSpec()
    override val dialogSpec = ElegantMinimalDialogSpec()
    override val chartsSpec = ElegantMinimalChartsSpec()
    override val settingsSpec = ElegantMinimalSettingsSpec()
    
    override val baseStyle = BaseStyle.HIERARCHICAL
    override val description = "优雅极简，精致留白，细腻层次"
    override val recommendedDensity = DemoDensity.Medium
}

/**
 * 优雅极简列表规格
 */
class ElegantMinimalListSpec : ListSpec {
    
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        // 优雅的列表项：细微的悬浮感
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(ElegantMinimalSpecs.Colors.White)
                .clickable(
                    onClick = onClick
                )
                .padding(horizontal = ElegantMinimalSpecs.Spacing.m)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ElegantMinimalSpecs.Spacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：优雅的圆形指示器
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.type == TransactionType.INCOME)
                                ElegantMinimalSpecs.Colors.Success.copy(alpha = 0.1f)
                            else
                                ElegantMinimalSpecs.Colors.Gray50
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (transaction.type == TransactionType.INCOME) "+" else "−",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        color = if (transaction.type == TransactionType.INCOME)
                            ElegantMinimalSpecs.Colors.Success
                        else
                            ElegantMinimalSpecs.Colors.Gray700
                    )
                }
                
                Spacer(modifier = Modifier.width(ElegantMinimalSpecs.Spacing.m))
                
                // 中间：信息区域
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = transaction.category?.name ?: "未分类",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElegantMinimalSpecs.Colors.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!transaction.note.isNullOrEmpty()) {
                        Text(
                            text = transaction.note,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = ElegantMinimalSpecs.Colors.Gray500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // 右侧：金额（精致的排版）
                Text(
                    text = String.format("%.2f", transaction.amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (transaction.type == TransactionType.INCOME)
                        ElegantMinimalSpecs.Colors.Success
                    else
                        ElegantMinimalSpecs.Colors.DarkGray,
                    letterSpacing = (-0.5).sp  // 紧凑的字间距
                )
            }
            
            // 底部细线
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = ElegantMinimalSpecs.Spacing.m)
                    .height(0.5.dp)
                    .background(ElegantMinimalSpecs.Colors.Gray100)
            )
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
        // 优雅的日期标题
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ElegantMinimalSpecs.Spacing.m,
                    vertical = ElegantMinimalSpecs.Spacing.s
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("M月d日")),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = ElegantMinimalSpecs.Colors.Gray700,
                letterSpacing = 0.5.sp
            )
            
            // 当日汇总（可选显示）
            if (totalIncome > 0 || totalExpense > 0) {
                Text(
                    text = String.format("+%.0f -%.0f", totalIncome, totalExpense),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = ElegantMinimalSpecs.Colors.Gray500
                )
            }
        }
    }
    
    @Composable
    override fun ListContainer(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        // 优雅的容器：纯净的背景
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ElegantMinimalSpecs.Colors.Gray50)
        ) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity) = when (density) {
        DemoDensity.Compact -> 56.dp
        DemoDensity.Medium -> 68.dp
    }
    
    override fun getItemSpacing(density: DemoDensity) = 0.dp
}

/**
 * 优雅极简项目规格
 */
class ElegantMinimalItemSpec : ItemSpec {
    
    override fun getLayout() = ItemSpec.ItemLayout.TWO_LINE
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.HIDDEN,
        notePosition = ItemSpec.Position.BELOW,
        dateTimePosition = ItemSpec.Position.HIDDEN
    )
    
    override fun showIcons() = false  // 使用简约的指示器代替图标
    override fun showDividers() = true
}

/**
 * 优雅极简头部规格
 */
class ElegantMinimalHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        // 优雅的概览卡片
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(ElegantMinimalSpecs.Spacing.m),
            shape = RoundedCornerShape(ElegantMinimalSpecs.Radius.large),
            colors = CardDefaults.cardColors(
                containerColor = ElegantMinimalSpecs.Colors.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
            border = BorderStroke(0.5.dp, ElegantMinimalSpecs.Colors.Gray100)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ElegantMinimalSpecs.Spacing.l),
                verticalArrangement = Arrangement.spacedBy(ElegantMinimalSpecs.Spacing.m)
            ) {
                // 标题
                Text(
                    text = "本月概览",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = ElegantMinimalSpecs.Colors.Gray500,
                    letterSpacing = 0.5.sp
                )
                
                // 余额（突出显示）
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = String.format("¥%.2f", balance),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        color = ElegantMinimalSpecs.Colors.Black,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "结余",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = ElegantMinimalSpecs.Colors.Gray500
                    )
                }
                
                // 收支明细（次要信息）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ElegantMinimalSpecs.Spacing.xl)
                ) {
                    Column {
                        Text(
                            text = String.format("%.2f", income),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = ElegantMinimalSpecs.Colors.Success
                        )
                        Text(
                            text = "收入",
                            fontSize = 11.sp,
                            color = ElegantMinimalSpecs.Colors.Gray500
                        )
                    }
                    Column {
                        Text(
                            text = String.format("%.2f", expense),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = ElegantMinimalSpecs.Colors.Gray900
                        )
                        Text(
                            text = "支出",
                            fontSize = 11.sp,
                            color = ElegantMinimalSpecs.Colors.Gray500
                        )
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.SMALL
    override fun showDateSelector() = true
    override fun showQuickStats() = true
}

/**
 * 优雅极简筛选器规格
 */
class ElegantMinimalFilterSpec : FilterSpec {
    
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
        // 优雅的筛选栏
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(ElegantMinimalSpecs.Colors.White)
                .padding(ElegantMinimalSpecs.Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(ElegantMinimalSpecs.Spacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 日期选择器（精致的按钮）
            Surface(
                shape = RoundedCornerShape(ElegantMinimalSpecs.Radius.medium),
                color = ElegantMinimalSpecs.Colors.Gray50,
                modifier = Modifier.clickable { /* 日期选择 */ }
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = ElegantMinimalSpecs.Spacing.m,
                        vertical = ElegantMinimalSpecs.Spacing.s
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ElegantMinimalSpecs.Spacing.xs)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ElegantMinimalSpecs.Colors.Gray700
                    )
                    Text(
                        text = selectedDateRange.label,
                        fontSize = 13.sp,
                        color = ElegantMinimalSpecs.Colors.Gray900
                    )
                }
            }
            
            // 分类筛选（如果有选中）
            if (selectedCategories.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(ElegantMinimalSpecs.Radius.medium),
                    color = ElegantMinimalSpecs.Colors.Accent.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${selectedCategories.size} 分类",
                        fontSize = 13.sp,
                        color = ElegantMinimalSpecs.Colors.Accent,
                        modifier = Modifier.padding(
                            horizontal = ElegantMinimalSpecs.Spacing.m,
                            vertical = ElegantMinimalSpecs.Spacing.s
                        )
                    )
                }
            }
        }
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.CHIPS
    override fun isCollapsible() = false
}

/**
 * 优雅极简表单规格
 */
class ElegantMinimalFormSpec : FormSpec {
    
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        // 优雅的表单
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(ElegantMinimalSpecs.Colors.White)
                .padding(ElegantMinimalSpecs.Spacing.l),
            verticalArrangement = Arrangement.spacedBy(ElegantMinimalSpecs.Spacing.l)
        ) {
            // 金额输入（大号字体）
            var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
            Column {
                Text(
                    "金额",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = ElegantMinimalSpecs.Colors.Gray500,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(ElegantMinimalSpecs.Spacing.s))
                BasicTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = ElegantMinimalSpecs.Colors.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ElegantMinimalSpecs.Colors.Gray100)
                )
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ElegantMinimalSpecs.Spacing.m)
            ) {
                // 取消按钮（次要）
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(ElegantMinimalSpecs.Radius.medium),
                    border = BorderStroke(0.5.dp, ElegantMinimalSpecs.Colors.Gray300),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElegantMinimalSpecs.Colors.Gray700
                    )
                ) {
                    Text("取消", fontSize = 14.sp)
                }
                
                // 保存按钮（主要）
                Button(
                    onClick = { /* onSave */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(ElegantMinimalSpecs.Radius.medium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantMinimalSpecs.Colors.Black,
                        contentColor = ElegantMinimalSpecs.Colors.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text("保存", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.VERTICAL
    override fun getFieldOrder() = listOf("amount", "category", "note", "date")
    override fun showCalculator() = true
}

/**
 * 优雅极简对话框规格
 */
class ElegantMinimalDialogSpec : DialogSpec {
    
    @Composable
    override fun RenderDialog(
        title: String,
        content: @Composable () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier
    ) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismiss
        ) {
            Card(
                modifier = modifier,
                shape = RoundedCornerShape(ElegantMinimalSpecs.Radius.large),
                colors = CardDefaults.cardColors(
                    containerColor = ElegantMinimalSpecs.Colors.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 24.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(ElegantMinimalSpecs.Spacing.l)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElegantMinimalSpecs.Colors.Black
                    )
                    Spacer(modifier = Modifier.height(ElegantMinimalSpecs.Spacing.m))
                    content()
                    Spacer(modifier = Modifier.height(ElegantMinimalSpecs.Spacing.l))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = ElegantMinimalSpecs.Colors.Gray700
                            )
                        ) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = onConfirm,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = ElegantMinimalSpecs.Colors.Black
                            )
                        ) {
                            Text("确定", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.FLOATING
    override fun showOverlay() = true
}

/**
 * 优雅极简图表规格
 */
class ElegantMinimalChartsSpec : ChartsSpec {
    
    @Composable
    override fun RenderPieChart(
        data: List<ChartData>,
        modifier: Modifier
    ) {
        // 优雅的数据可视化（简约的条形图代替饼图）
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(ElegantMinimalSpecs.Colors.White, RoundedCornerShape(ElegantMinimalSpecs.Radius.large))
                .padding(ElegantMinimalSpecs.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(ElegantMinimalSpecs.Spacing.s)
        ) {
            data.sortedByDescending { it.value }.take(5).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 标签
                    Text(
                        text = item.label,
                        fontSize = 13.sp,
                        color = ElegantMinimalSpecs.Colors.Gray700,
                        modifier = Modifier.width(80.dp)
                    )
                    
                    // 进度条
                    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(ElegantMinimalSpecs.Colors.Gray50, RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((item.value / maxValue).toFloat())
                                .background(ElegantMinimalSpecs.Colors.Black, RoundedCornerShape(2.dp))
                        )
                    }
                    
                    // 数值
                    Text(
                        text = String.format("%.0f", item.value),
                        fontSize = 13.sp,
                        color = ElegantMinimalSpecs.Colors.Gray900,
                        modifier = Modifier.padding(start = ElegantMinimalSpecs.Spacing.s)
                    )
                }
            }
        }
    }
    
    @Composable
    override fun RenderLineChart(
        data: List<ChartData>,
        modifier: Modifier
    ) {
        RenderPieChart(data, modifier)
    }
    
    @Composable
    override fun RenderBarChart(
        data: List<ChartData>,
        modifier: Modifier
    ) {
        RenderPieChart(data, modifier)
    }
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.MINIMAL
    override fun showLegend() = false
    override fun showGrid() = false
}

/**
 * 优雅极简设置规格
 */
class ElegantMinimalSettingsSpec : SettingsSpec {
    
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
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            color = ElegantMinimalSpecs.Colors.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ElegantMinimalSpecs.Spacing.m,
                        vertical = ElegantMinimalSpecs.Spacing.m
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = ElegantMinimalSpecs.Colors.Black
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = ElegantMinimalSpecs.Colors.Gray500
                        )
                    }
                }
                
                trailing?.invoke() ?: Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ElegantMinimalSpecs.Colors.Gray300
                )
            }
        }
    }
    
    @Composable
    override fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    ) {
        Column(modifier = modifier) {
            if (title.isNotEmpty()) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = ElegantMinimalSpecs.Colors.Gray500,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(
                        horizontal = ElegantMinimalSpecs.Spacing.m,
                        vertical = ElegantMinimalSpecs.Spacing.s
                    )
                )
            }
            Surface(
                color = ElegantMinimalSpecs.Colors.White,
                shape = if (title.isEmpty()) RoundedCornerShape(0.dp) 
                       else RoundedCornerShape(ElegantMinimalSpecs.Radius.medium)
            ) {
                Column {
                    items()
                }
            }
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.GROUPED
}

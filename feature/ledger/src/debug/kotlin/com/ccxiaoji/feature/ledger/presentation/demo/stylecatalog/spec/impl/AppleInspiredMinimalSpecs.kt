package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * Apple启发的极简设计
 * 基于iOS HIG原则：清晰、克制、深度
 * 严格遵循8pt网格系统
 */
class AppleInspiredMinimalSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.BALANCED
    override val description = "基于Apple设计原则的极简风格"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = AppleMinimalListSpec()
    override val itemSpec = AppleMinimalItemSpec()
    override val headerSpec = AppleMinimalHeaderSpec()
    override val filterSpec = AppleMinimalFilterSpec()
    override val formSpec = AppleMinimalFormSpec()
    override val dialogSpec = AppleMinimalDialogSpec()
    override val chartsSpec = AppleMinimalChartsSpec()
    override val settingsSpec = AppleMinimalSettingsSpec()
}

/**
 * iOS系统色彩
 * 克制、优雅、功能性
 */
object AppleColors {
    // 系统主色调 - 来自iOS系统
    val SystemBlue = Color(0xFF007AFF)
    val SystemGreen = Color(0xFF34C759)
    val SystemRed = Color(0xFFFF3B30)
    val SystemOrange = Color(0xFFFF9500)
    val SystemGray = Color(0xFF8E8E93)
    val SystemGray2 = Color(0xFFAEAEB2)
    val SystemGray3 = Color(0xFFC7C7CC)
    val SystemGray4 = Color(0xFFD1D1D6)
    val SystemGray5 = Color(0xFFE5E5EA)
    val SystemGray6 = Color(0xFFF2F2F7)
    
    // 语义色彩
    val Label = Color(0xFF000000)
    val SecondaryLabel = Color(0x99000000) // 60% opacity
    val TertiaryLabel = Color(0x4D000000)  // 30% opacity
    val Separator = Color(0x1F000000)      // 12% opacity
    val Background = Color(0xFFF2F2F7)
    val SecondaryBackground = Color(0xFFFFFFFF)
    val GroupedBackground = Color(0xFFF2F2F7)
}

/**
 * 8pt网格系统常量
 */
object Grid8 {
    val x1 = 8.dp
    val x2 = 16.dp
    val x3 = 24.dp
    val x4 = 32.dp
    val x5 = 40.dp
    val x6 = 48.dp
    val x7 = 56.dp
    val x8 = 64.dp
}

// ==================== List Spec - 清晰的信息层次 ====================
class AppleMinimalListSpec : ListSpec {
    
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),
            color = AppleColors.SecondaryBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Grid8.x2, vertical = Grid8.x2), // 16dp padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标 - 简洁的圆形背景
                Box(
                    modifier = Modifier
                        .size(Grid8.x5) // 40dp
                        .background(
                            color = when (transaction.type) {
                                TransactionType.EXPENSE -> AppleColors.SystemRed.copy(alpha = 0.1f)
                                TransactionType.INCOME -> AppleColors.SystemGreen.copy(alpha = 0.1f)
                                else -> AppleColors.SystemBlue.copy(alpha = 0.1f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = null,
                        tint = when (transaction.type) {
                            TransactionType.EXPENSE -> AppleColors.SystemRed
                            TransactionType.INCOME -> AppleColors.SystemGreen
                            else -> AppleColors.SystemBlue
                        },
                        modifier = Modifier.size(Grid8.x3) // 24dp
                    )
                }
                
                Spacer(modifier = Modifier.width(Grid8.x2)) // 16dp
                
                // 信息区域 - 清晰的层次
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp) // 使用4pt作为小间距
                ) {
                    // 主要信息 - 分类
                    Text(
                        text = transaction.category?.name ?: "未分类",
                        fontSize = 17.sp, // iOS标准正文大小
                        fontWeight = FontWeight.Normal,
                        color = AppleColors.Label
                    )
                    
                    // 次要信息 - 备注和账户
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Grid8.x1)
                    ) {
                        if (!transaction.note.isNullOrEmpty()) {
                            Text(
                                text = transaction.note,
                                fontSize = 15.sp,
                                color = AppleColors.SecondaryLabel,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                        Text(
                            text = transaction.account?.name ?: "现金",
                            fontSize = 13.sp,
                            color = AppleColors.TertiaryLabel
                        )
                    }
                }
                
                // 金额区域 - 右对齐
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // 金额 - 使用系统色彩
                    Text(
                        text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}¥${String.format("%.2f", transaction.amount)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (transaction.type) {
                            TransactionType.EXPENSE -> AppleColors.SystemRed
                            TransactionType.INCOME -> AppleColors.SystemGreen
                            else -> AppleColors.Label
                        }
                    )
                    
                    // 时间 - 次要信息
                    Text(
                        text = DateTimeFormatter.ofPattern("HH:mm").format(
                            transaction.dateTime.toJavaInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime()
                        ),
                        fontSize = 13.sp,
                        color = AppleColors.TertiaryLabel
                    )
                }
            }
        }
        
        // 分隔线 - iOS风格的细线
        HorizontalDivider(
            modifier = Modifier.padding(start = Grid8.x8 + Grid8.x1), // 72dp (图标宽度+间距)
            thickness = 0.5.dp,
            color = AppleColors.Separator
        )
    }
    
    private fun getCategoryIcon(category: DemoCategory?): ImageVector {
        // 使用Outlined图标，更轻盈
        return when (category?.icon) {
            "restaurant" -> Icons.Outlined.Restaurant
            "shopping_cart" -> Icons.Outlined.ShoppingCart
            "directions_car" -> Icons.Outlined.DirectionsCar
            "home" -> Icons.Outlined.Home
            "phone" -> Icons.Outlined.Phone
            "medical" -> Icons.Outlined.LocalHospital
            "school" -> Icons.Outlined.School
            "flight" -> Icons.Outlined.FlightTakeoff
            "work" -> Icons.Outlined.Work
            "savings" -> Icons.Outlined.Savings
            else -> Icons.Outlined.Category
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
        // iOS风格的分组标题
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppleColors.GroupedBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Grid8.x2, vertical = Grid8.x1), // 16dp horizontal, 8dp vertical
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 日期
                Text(
                    text = when {
                        date == LocalDate.now() -> "今天"
                        date == LocalDate.now().minusDays(1) -> "昨天"
                        else -> DateTimeFormatter.ofPattern("M月d日 E").format(date)
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppleColors.SecondaryLabel,
                    letterSpacing = 0.sp
                )
                
                // 汇总金额
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Grid8.x2)
                ) {
                    if (totalIncome > 0) {
                        Text(
                            text = "+${String.format("%.0f", totalIncome)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppleColors.SystemGreen
                        )
                    }
                    if (totalExpense > 0) {
                        Text(
                            text = "-${String.format("%.0f", totalExpense)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppleColors.SystemRed
                        )
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
            modifier = modifier.background(AppleColors.GroupedBackground)
        ) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> Grid8.x7 // 56dp
            DemoDensity.Medium -> Grid8.x8  // 64dp
        }
    }
    
    override fun getItemSpacing(density: DemoDensity): Dp {
        return 0.dp // iOS使用分隔线而非间距
    }
}

// ==================== Item Spec ====================
class AppleMinimalItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.CARD
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.BELOW,
        notePosition = ItemSpec.Position.BELOW,
        dateTimePosition = ItemSpec.Position.RIGHT
    )
    
    override fun showIcons() = true
    override fun showDividers() = true
}

// ==================== Header Spec - 简洁的概览 ====================
class AppleMinimalHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        // iOS风格的卡片
        Surface(
            modifier = modifier.padding(Grid8.x2), // 16dp
            shape = RoundedCornerShape(12.dp), // iOS标准圆角
            color = AppleColors.SecondaryBackground,
            shadowElevation = 0.dp // 无阴影，更扁平
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Grid8.x3) // 24dp
            ) {
                // 标题
                Text(
                    text = "本月",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppleColors.SecondaryLabel,
                    letterSpacing = 0.sp
                )
                
                Spacer(modifier = Modifier.height(Grid8.x1)) // 8dp
                
                // 结余 - 突出显示
                Text(
                    text = "¥${String.format("%,.2f", balance)}",
                    fontSize = 34.sp, // iOS大标题尺寸
                    fontWeight = FontWeight.Bold,
                    color = AppleColors.Label,
                    letterSpacing = 0.35.sp
                )
                
                Spacer(modifier = Modifier.height(Grid8.x3)) // 24dp
                
                // 收支明细 - 水平排列
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    // 收入
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "收入",
                            fontSize = 13.sp,
                            color = AppleColors.SecondaryLabel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${String.format("%,.2f", income)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleColors.SystemGreen
                        )
                    }
                    
                    // 支出
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "支出",
                            fontSize = 13.sp,
                            color = AppleColors.SecondaryLabel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${String.format("%,.2f", expense)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppleColors.SystemRed
                        )
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.LARGE
    override fun showDateSelector() = true
    override fun showQuickStats() = false // 保持简洁
}

// ==================== Filter Spec - iOS风格的筛选器 ====================
class AppleMinimalFilterSpec : FilterSpec {
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
        // iOS风格的分段控制器
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(Grid8.x2), // 16dp
            horizontalArrangement = Arrangement.spacedBy(Grid8.x1) // 8dp
        ) {
            // 简洁的文本按钮
            TextButton(
                onClick = { },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AppleColors.SystemBlue
                )
            ) {
                Text(selectedDateRange.label, fontSize = 15.sp)
            }
            
            if (selectedCategories.isNotEmpty()) {
                TextButton(
                    onClick = { },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppleColors.SystemBlue
                    )
                ) {
                    Text("${selectedCategories.size}个分类", fontSize = 15.sp)
                }
            }
        }
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.CHIPS
    override fun isCollapsible() = false
}

// ==================== 其他 Specs 实现 ====================
class AppleMinimalFormSpec : FormSpec {
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
                .padding(Grid8.x2),
            verticalArrangement = Arrangement.spacedBy(Grid8.x2)
        ) {
            Text(
                text = if (transaction == null) "新增交易" else "编辑交易",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppleColors.Label
            )
        }
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.VERTICAL
    override fun getFieldOrder() = listOf("amount", "category", "account", "note", "date")
    override fun showCalculator() = false // 使用系统键盘
}

class AppleMinimalDialogSpec : DialogSpec {
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
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppleColors.Label
                ) 
            },
            text = content,
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppleColors.SystemBlue
                    )
                ) {
                    Text("确定", fontSize = 17.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppleColors.SystemBlue
                    )
                ) {
                    Text("取消", fontSize = 17.sp)
                }
            },
            shape = RoundedCornerShape(14.dp), // iOS标准对话框圆角
            containerColor = AppleColors.SecondaryBackground,
            modifier = modifier
        )
    }
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.FLOATING
    override fun showOverlay() = true
}

class AppleMinimalChartsSpec : ChartsSpec {
    @Composable
    override fun RenderPieChart(data: List<ChartData>, modifier: Modifier) {
        Surface(
            modifier = modifier.height(Grid8.x3 * 10), // 240dp
            shape = RoundedCornerShape(12.dp),
            color = AppleColors.SecondaryBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Grid8.x2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "饼图",
                    fontSize = 15.sp,
                    color = AppleColors.TertiaryLabel
                )
            }
        }
    }
    
    @Composable
    override fun RenderLineChart(data: List<ChartData>, modifier: Modifier) {
        Surface(
            modifier = modifier.height(Grid8.x3 * 10), // 240dp
            shape = RoundedCornerShape(12.dp),
            color = AppleColors.SecondaryBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Grid8.x2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "趋势",
                    fontSize = 15.sp,
                    color = AppleColors.TertiaryLabel
                )
            }
        }
    }
    
    @Composable
    override fun RenderBarChart(data: List<ChartData>, modifier: Modifier) {
        Surface(
            modifier = modifier.height(Grid8.x3 * 10), // 240dp
            shape = RoundedCornerShape(12.dp),
            color = AppleColors.SecondaryBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Grid8.x2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "柱状图",
                    fontSize = 15.sp,
                    color = AppleColors.TertiaryLabel
                )
            }
        }
    }
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.MINIMAL
    override fun showLegend() = false // 保持简洁
    override fun showGrid() = false
}

class AppleMinimalSettingsSpec : SettingsSpec {
    @Composable
    override fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        // iOS风格的设置项
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),
            color = AppleColors.SecondaryBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Grid8.x2, vertical = Grid8.x2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(modifier = Modifier.size(Grid8.x3)) { // 24dp
                        icon()
                    }
                    Spacer(modifier = Modifier.width(Grid8.x2))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        color = AppleColors.Label
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            color = AppleColors.SecondaryLabel
                        )
                    }
                }
                
                if (trailing != null) {
                    trailing()
                } else {
                    // 默认的右箭头
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = AppleColors.TertiaryLabel,
                        modifier = Modifier.size(Grid8.x3)
                    )
                }
            }
        }
        
        // 分隔线
        HorizontalDivider(
            modifier = Modifier.padding(start = if (icon != null) Grid8.x7 else Grid8.x2),
            thickness = 0.5.dp,
            color = AppleColors.Separator
        )
    }
    
    @Composable
    override fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    ) {
        Column(modifier = modifier) {
            // 分组标题 - iOS风格
            Text(
                text = title.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = AppleColors.SecondaryLabel,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(
                    start = Grid8.x2,
                    top = Grid8.x4,
                    bottom = Grid8.x1
                )
            )
            
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AppleColors.SecondaryBackground
            ) {
                Column {
                    items()
                }
            }
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.LIST
}

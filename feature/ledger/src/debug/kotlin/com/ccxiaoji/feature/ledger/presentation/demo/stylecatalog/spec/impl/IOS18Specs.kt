package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import kotlinx.datetime.toJavaInstant

/**
 * G iOS 18风格规格实现
 * 特点：大标题通透、圆角设计、系统级动效、分组列表
 */
class IOS18Specs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.BALANCED
    override val description = "iOS 18 大标题通透，现代系统设计"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = IOS18ListSpec()
    override val itemSpec = IOS18ItemSpec()
    override val headerSpec = IOS18HeaderSpec()
    override val filterSpec = IOS18FilterSpec()
    override val formSpec = IOS18FormSpec()
    override val dialogSpec = IOS18DialogSpec()
    override val chartsSpec = IOS18ChartsSpec()
    override val settingsSpec = IOS18SettingsSpec()
}

// iOS 18 色彩定义
private object iOS18Colors {
    val SystemBlue = Color(0xFF007AFF)
    val SystemGreen = Color(0xFF34C759)
    val SystemRed = Color(0xFFFF3B30)
    val SystemOrange = Color(0xFFFF9500)
    val SystemPurple = Color(0xFFAF52DE)
    val SystemGray = Color(0xFF8E8E93)
    val SystemGray2 = Color(0xFFAEAEB2)
    val SystemGray3 = Color(0xFFC7C7CC)
    val SystemGray4 = Color(0xFFD1D1D6)
    val SystemGray5 = Color(0xFFE5E5EA)
    val SystemGray6 = Color(0xFFF2F2F7)
    val LabelPrimary = Color(0xFF000000)
    val LabelSecondary = Color(0xFF3C3C43).copy(alpha = 0.6f)
}

// ==================== List Spec ====================
class IOS18ListSpec : ListSpec {
    
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // iOS 风格图标
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (transaction.category?.name) {
                                "餐饮" -> iOS18Colors.SystemOrange.copy(alpha = 0.15f)
                                "交通" -> iOS18Colors.SystemBlue.copy(alpha = 0.15f)
                                "购物" -> iOS18Colors.SystemPurple.copy(alpha = 0.15f)
                                "娱乐" -> iOS18Colors.SystemRed.copy(alpha = 0.15f)
                                else -> iOS18Colors.SystemGray.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.category?.name) {
                            "餐饮" -> Icons.Default.Restaurant
                            "交通" -> Icons.Default.DirectionsCar
                            "购物" -> Icons.Default.ShoppingBag
                            "娱乐" -> Icons.Default.MovieCreation
                            else -> Icons.Default.Category
                        },
                        contentDescription = null,
                        tint = when (transaction.category?.name) {
                            "餐饮" -> iOS18Colors.SystemOrange
                            "交通" -> iOS18Colors.SystemBlue
                            "购物" -> iOS18Colors.SystemPurple
                            "娱乐" -> iOS18Colors.SystemRed
                            else -> iOS18Colors.SystemGray
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // 主要内容
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = transaction.category?.name ?: "未分类",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = iOS18Colors.LabelPrimary,
                        fontSize = 17.sp
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.account?.name ?: "现金",
                            style = MaterialTheme.typography.bodySmall,
                            color = iOS18Colors.LabelSecondary,
                            fontSize = 15.sp
                        )
                        
                        if (!transaction.note.isNullOrEmpty()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = iOS18Colors.LabelSecondary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = transaction.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = iOS18Colors.LabelSecondary,
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // 右侧信息
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}¥${transaction.amount}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (transaction.type == TransactionType.EXPENSE) iOS18Colors.LabelPrimary
                               else iOS18Colors.SystemGreen,
                        fontSize = 17.sp
                    )
                    Text(
                        text = DateTimeFormatter.ofPattern("HH:mm").format(transaction.dateTime.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                        style = MaterialTheme.typography.bodySmall,
                        color = iOS18Colors.LabelSecondary,
                        fontSize = 15.sp
                    )
                }
                
                // iOS 风格右箭头
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = iOS18Colors.SystemGray3,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // 分隔线 - iOS 风格
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 80.dp)
                    .fillMaxWidth()
                    .height(0.33.dp)
                    .background(iOS18Colors.SystemGray4)
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
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(iOS18Colors.SystemGray6.copy(alpha = 0.8f))
                .padding(20.dp, 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeFormatter.ofPattern("M月d日 EEEE").format(date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = iOS18Colors.LabelPrimary,
                    fontSize = 16.sp
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (totalIncome > 0) {
                        Text(
                            text = "+¥$totalIncome",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = iOS18Colors.SystemGreen,
                            fontSize = 14.sp
                        )
                    }
                    if (totalExpense > 0) {
                        Text(
                            text = "-¥$totalExpense",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = iOS18Colors.LabelPrimary,
                            fontSize = 14.sp
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
            modifier = modifier
                .background(iOS18Colors.SystemGray6)
        ) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 60.dp
            DemoDensity.Medium -> 72.dp
        }
    }
    
    override fun getItemSpacing(density: DemoDensity): Dp = 0.dp
}

// ==================== Item Spec ====================
class IOS18ItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.SINGLE_LINE
    
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

// ==================== Header Spec ====================
class IOS18HeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Column(
            modifier = modifier
                .background(iOS18Colors.SystemGray6)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // iOS 18 大标题风格
            Text(
                text = "记账",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = iOS18Colors.LabelPrimary,
                fontSize = 34.sp
            )
            
            // 概览卡片 - iOS 风格
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "本月概览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = iOS18Colors.LabelPrimary,
                        fontSize = 20.sp
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 收入行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(iOS18Colors.SystemGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = iOS18Colors.SystemGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "收入",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = iOS18Colors.LabelPrimary,
                                    fontSize = 17.sp
                                )
                            }
                            Text(
                                text = "¥$income",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = iOS18Colors.SystemGreen,
                                fontSize = 17.sp
                            )
                        }
                        
                        // 支出行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(iOS18Colors.SystemRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = iOS18Colors.SystemRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "支出",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = iOS18Colors.LabelPrimary,
                                    fontSize = 17.sp
                                )
                            }
                            Text(
                                text = "¥$expense",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = iOS18Colors.SystemRed,
                                fontSize = 17.sp
                            )
                        }
                        
                        // 分隔线
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(iOS18Colors.SystemGray4)
                        )
                        
                        // 结余行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "结余",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = iOS18Colors.LabelPrimary,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "¥$balance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (balance >= 0) iOS18Colors.SystemBlue else iOS18Colors.SystemRed,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.LARGE  // iOS 18 大标题特色
    override fun showDateSelector() = false
    override fun showQuickStats() = true
}

// ==================== 其他 Specs 占位实现 ====================
class IOS18FilterSpec : FilterSpec {
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
        Box(modifier = modifier.height(44.dp))
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.SEGMENTS  // iOS 分段控件
    override fun isCollapsible() = false
}

class IOS18FormSpec : FormSpec {
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.GROUPED
    override fun getFieldOrder() = listOf("amount", "category", "account", "note")
    override fun showCalculator() = false
}

class IOS18DialogSpec : DialogSpec {
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
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.IOS
    override fun showOverlay() = true
}

class IOS18ChartsSpec : ChartsSpec {
    @Composable
    override fun RenderPieChart(data: List<ChartData>, modifier: Modifier) {
        Box(modifier = modifier.height(200.dp))
    }
    
    @Composable
    override fun RenderLineChart(data: List<ChartData>, modifier: Modifier) {
        Box(modifier = modifier.height(200.dp))
    }
    
    @Composable
    override fun RenderBarChart(data: List<ChartData>, modifier: Modifier) {
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.FLAT
    override fun showLegend() = false
    override fun showGrid() = true
}

class IOS18SettingsSpec : SettingsSpec {
    @Composable
    override fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (icon != null) icon()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = iOS18Colors.LabelPrimary,
                        fontSize = 17.sp
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = iOS18Colors.LabelSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
                if (trailing != null) {
                    trailing()
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = iOS18Colors.SystemGray3,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // iOS 分隔线
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = if (icon != null) 72.dp else 20.dp)
                    .fillMaxWidth()
                    .height(0.33.dp)
                    .background(iOS18Colors.SystemGray4)
            )
        }
    }
    
    @Composable
    override fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    ) {
        Column(modifier = modifier.padding(vertical = 20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = iOS18Colors.LabelSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                items()
            }
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.GROUPED
}
package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
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
 * F 账本质感风格规格实现
 * 特点：类纸张背景、手账风格、传统记账本布局、装饰线条
 */
class BookTextureSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.BALANCED
    override val description = "账本质感设计，类纸张背景，传统记账风格"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = BookTextureListSpec()
    override val itemSpec = BookTextureItemSpec()
    override val headerSpec = BookTextureHeaderSpec()
    override val filterSpec = BookTextureFilterSpec()
    override val formSpec = BookTextureFormSpec()
    override val dialogSpec = BookTextureDialogSpec()
    override val chartsSpec = BookTextureChartsSpec()
    override val settingsSpec = BookTextureSettingsSpec()
}

// 账本色彩定义
private object BookColors {
    val PaperBackground = Color(0xFFFFFBF0)  // 米黄纸张色
    val InkBlue = Color(0xFF1B365D)          // 墨蓝色
    val InkBlack = Color(0xFF2D2D2D)         // 墨黑色
    val RedInk = Color(0xFFB71C1C)           // 红色墨水（负数）
    val BlueInk = Color(0xFF1565C0)          // 蓝色墨水（正数）
    val LineColor = Color(0xFFE0E0E0)        // 表格线条色
    val MarginLine = Color(0xFFFFE0B2)       // 边界线
}

// ==================== List Spec ====================
class BookTextureListSpec : ListSpec {
    
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
                .background(BookColors.PaperBackground)
                .clickable { onClick() }
                .drawBehind {
                    // 绘制横线（像账本的行线）
                    val lineY = size.height
                    drawLine(
                        color = BookColors.LineColor,
                        start = Offset(32.dp.toPx(), lineY),
                        end = Offset(size.width - 16.dp.toPx(), lineY),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    // 绘制左边距线（像账本的边界）
                    drawLine(
                        color = BookColors.MarginLine,
                        start = Offset(48.dp.toPx(), 0f),
                        end = Offset(48.dp.toPx(), size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 日期栏 - 传统账本风格
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = DateTimeFormatter.ofPattern("dd").format(transaction.dateTime.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = BookColors.InkBlue,
                        fontSize = 20.sp
                    )
                    Text(
                        text = DateTimeFormatter.ofPattern("HH:mm").format(transaction.dateTime.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Serif,
                        color = BookColors.InkBlack,
                        fontSize = 10.sp
                    )
                }
                
                // 垂直分割线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(BookColors.LineColor)
                )
                
                // 摘要栏 - 主要信息
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = transaction.category?.name ?: "其他",
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = BookColors.InkBlack,
                        fontSize = 16.sp
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 账户标记
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = BookColors.InkBlue,
                                    shape = RectangleShape
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = transaction.account?.name ?: "现金",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Serif,
                                color = BookColors.InkBlue,
                                fontSize = 10.sp
                            )
                        }
                    }
                    
                    if (!transaction.note.isNullOrEmpty()) {
                        Text(
                            text = "备注：${transaction.note}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Serif,
                            color = BookColors.InkBlack.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
                
                // 金额栏 - 传统会计记录风格
                Column(
                    modifier = Modifier.width(80.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (transaction.type == TransactionType.EXPENSE) "支" else "收",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Serif,
                        color = if (transaction.type == TransactionType.EXPENSE) BookColors.RedInk 
                               else BookColors.BlueInk,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "¥${transaction.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = if (transaction.type == TransactionType.EXPENSE) BookColors.RedInk 
                               else BookColors.BlueInk,
                        fontSize = 16.sp
                    )
                }
            }
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
                .background(BookColors.PaperBackground)
                .drawBehind {
                    // 绘制装饰性双线
                    val topY = 12.dp.toPx()
                    val bottomY = size.height - 12.dp.toPx()
                    
                    // 上双线
                    drawLine(
                        color = BookColors.InkBlue,
                        start = Offset(16.dp.toPx(), topY),
                        end = Offset(size.width - 16.dp.toPx(), topY),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = BookColors.InkBlue,
                        start = Offset(16.dp.toPx(), topY + 4.dp.toPx()),
                        end = Offset(size.width - 16.dp.toPx(), topY + 4.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    // 下双线
                    drawLine(
                        color = BookColors.InkBlue,
                        start = Offset(16.dp.toPx(), bottomY),
                        end = Offset(size.width - 16.dp.toPx(), bottomY),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = BookColors.InkBlue,
                        start = Offset(16.dp.toPx(), bottomY + 4.dp.toPx()),
                        end = Offset(size.width - 16.dp.toPx(), bottomY + 4.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                .padding(16.dp, 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateTimeFormatter.ofPattern("yyyy年M月d日").format(date),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = BookColors.InkBlue,
                        fontSize = 18.sp
                    )
                    Text(
                        text = DateTimeFormatter.ofPattern("EEEE").format(date),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Serif,
                        color = BookColors.InkBlack.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                // 当日汇总 - 传统账本风格
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (totalIncome > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "收入合计:",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Serif,
                                color = BookColors.InkBlack,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "¥$totalIncome",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold,
                                color = BookColors.BlueInk,
                                fontSize = 13.sp
                            )
                        }
                    }
                    
                    if (totalExpense > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "支出合计:",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Serif,
                                color = BookColors.InkBlack,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "¥$totalExpense",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold,
                                color = BookColors.RedInk,
                                fontSize = 13.sp
                            )
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
            modifier = modifier
                .background(BookColors.PaperBackground)
        ) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 60.dp
            DemoDensity.Medium -> 76.dp
        }
    }
    
    override fun getItemSpacing(density: DemoDensity): Dp = 0.dp  // 账本风格不需要间距
}

// ==================== Item Spec ====================
class BookTextureItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.SINGLE_LINE  // 传统单行记录
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.CENTER,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.BELOW,
        notePosition = ItemSpec.Position.BELOW,
        dateTimePosition = ItemSpec.Position.LEFT
    )
    
    override fun showIcons() = false  // 传统账本不用图标
    override fun showDividers() = true
}

// ==================== Header Spec ====================
class BookTextureHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier
                .background(BookColors.PaperBackground)
                .border(
                    width = 2.dp,
                    color = BookColors.InkBlue,
                    shape = RectangleShape
                )
                .drawBehind {
                    // 绘制装饰性边框线条
                    val inset = 8.dp.toPx()
                    drawLine(
                        color = BookColors.LineColor,
                        start = Offset(inset, inset),
                        end = Offset(size.width - inset, inset),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = BookColors.LineColor,
                        start = Offset(inset, size.height - inset),
                        end = Offset(size.width - inset, size.height - inset),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = BookColors.LineColor,
                        start = Offset(inset, inset),
                        end = Offset(inset, size.height - inset),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = BookColors.LineColor,
                        start = Offset(size.width - inset, inset),
                        end = Offset(size.width - inset, size.height - inset),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 传统标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "本 月 账 目 汇 总",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = BookColors.InkBlue,
                        fontSize = 22.sp,
                        letterSpacing = 4.sp
                    )
                }
                
                // 分割线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(BookColors.InkBlue)
                )
                
                // 传统表格式布局
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 收入行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = BookColors.LineColor,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                                )
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "收入总计",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            color = BookColors.InkBlack,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "¥$income",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = BookColors.BlueInk,
                            fontSize = 20.sp
                        )
                    }
                    
                    // 支出行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = BookColors.LineColor,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                                )
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "支出总计",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            color = BookColors.InkBlack,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "¥$expense",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = BookColors.RedInk,
                            fontSize = 20.sp
                        )
                    }
                    
                    // 分隔线
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(BookColors.InkBlue)
                    )
                    
                    // 结余行 - 突出显示
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (balance >= 0) BookColors.BlueInk.copy(alpha = 0.1f)
                                else BookColors.RedInk.copy(alpha = 0.1f)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "结　　余",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = BookColors.InkBlack,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "¥$balance",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Black,
                            color = if (balance >= 0) BookColors.BlueInk else BookColors.RedInk,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.MEDIUM
    override fun showDateSelector() = true
    override fun showQuickStats() = true
}

// ==================== 其他 Specs 占位实现 ====================
class BookTextureFilterSpec : FilterSpec {
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
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.DROPDOWN
    override fun isCollapsible() = false
}

class BookTextureFormSpec : FormSpec {
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.VERTICAL
    override fun getFieldOrder() = listOf("category", "amount", "account", "note")
    override fun showCalculator() = false
}

class BookTextureDialogSpec : DialogSpec {
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
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.MATERIAL
    override fun showOverlay() = true
}

class BookTextureChartsSpec : ChartsSpec {
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
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.OUTLINED  // 传统线条图表
    override fun showLegend() = true
    override fun showGrid() = true
}

class BookTextureSettingsSpec : SettingsSpec {
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
                .background(BookColors.PaperBackground)
                .border(
                    width = 1.dp,
                    color = BookColors.LineColor,
                    shape = RectangleShape
                )
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) icon()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = BookColors.InkBlack
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Serif,
                            color = BookColors.InkBlack.copy(alpha = 0.7f)
                        )
                    }
                }
                if (trailing != null) trailing()
            }
        }
    }
    
    @Composable
    override fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    ) {
        Column(modifier = modifier.padding(vertical = 8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookColors.InkBlue)
                    .padding(16.dp, 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
            items()
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.LIST
}
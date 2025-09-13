package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * J Notion极简风格规格实现
 * 特点：黑白灰细分隔、表格化布局、清晰层次、无装饰
 */
class NotionMinimalSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.HIERARCHICAL
    override val description = "Notion极简设计，黑白灰细分隔，表格化布局"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = NotionMinimalListSpec()
    override val itemSpec = NotionMinimalItemSpec()
    override val headerSpec = NotionMinimalHeaderSpec()
    override val filterSpec = NotionMinimalFilterSpec()
    override val formSpec = NotionMinimalFormSpec()
    override val dialogSpec = NotionMinimalDialogSpec()
    override val chartsSpec = NotionMinimalChartsSpec()
    override val settingsSpec = NotionMinimalSettingsSpec()
}

// Notion 色彩定义
private object NotionColors {
    val White = Color(0xFFFFFFFF)            // 纯白背景
    val Gray900 = Color(0xFF0F0F0F)          // 最深文本色
    val Gray800 = Color(0xFF1F1F1F)          // 深文本色
    val Gray700 = Color(0xFF2F2F2F)          // 中深文本色
    val Gray600 = Color(0xFF545454)          // 中文本色
    val Gray500 = Color(0xFF737373)          // 次要文本色
    val Gray400 = Color(0xFF969696)          // 浅文本色
    val Gray300 = Color(0xFFD4D4D4)          // 浅边框色
    val Gray200 = Color(0xFFE4E4E7)          // 分割线色
    val Gray100 = Color(0xFFF4F4F5)          // 浅背景色
    val Gray50 = Color(0xFFFAFAFA)           // 最浅背景色
    val Blue = Color(0xFF2563EB)             // Notion 蓝色
    val Green = Color(0xFF16A34A)            // 成功绿色
    val Red = Color(0xFFDC2626)              // 错误红色
}

// ==================== List Spec ====================
class NotionMinimalListSpec : ListSpec {
    
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
                .background(NotionColors.White)
                .clickable { onClick() }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 类型指示器 - Notion 风格的小圆点
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(
                                if (transaction.type == TransactionType.EXPENSE) NotionColors.Red
                                else NotionColors.Green
                            )
                    )
                    
                    // 分类名称 - 表格第一列
                    Text(
                        text = transaction.category?.name ?: "未分类",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = NotionColors.Gray900,
                        fontSize = 16.sp,
                        modifier = Modifier.width(120.dp)
                    )
                    
                    // 账户信息 - 表格第二列
                    Text(
                        text = transaction.account?.name ?: "现金",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NotionColors.Gray600,
                        fontSize = 14.sp,
                        modifier = Modifier.width(80.dp)
                    )
                    
                    // 备注 - 表格第三列
                    Text(
                        text = transaction.note?.ifEmpty { "—" } ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NotionColors.Gray500,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    
                    // 金额 - 表格第四列
                    Text(
                        text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}¥${transaction.amount}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (transaction.type == TransactionType.EXPENSE) NotionColors.Red
                               else NotionColors.Green,
                        fontSize = 16.sp,
                        modifier = Modifier.width(100.dp)
                    )
                    
                    // 时间 - 表格第五列
                    Text(
                        text = DateTimeFormatter.ofPattern("HH:mm").format(transaction.dateTime.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                        style = MaterialTheme.typography.bodySmall,
                        color = NotionColors.Gray400,
                        fontSize = 12.sp,
                        modifier = Modifier.width(50.dp)
                    )
                }
                
                // Notion 风格的细分隔线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(NotionColors.Gray200.copy(alpha = 0.6f))
                        .padding(horizontal = 20.dp)
                )
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
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(NotionColors.Gray50)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Notion 风格的小图标
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(NotionColors.Gray300),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = DateTimeFormatter.ofPattern("d").format(date),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = NotionColors.Gray700,
                            fontSize = 10.sp
                        )
                    }
                    
                    Text(
                        text = DateTimeFormatter.ofPattern("M月d日 EEEE").format(date),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NotionColors.Gray800,
                        fontSize = 15.sp
                    )
                }
                
                // 当日汇总 - Notion 数据块风格
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (totalIncome > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "收入",
                                style = MaterialTheme.typography.labelSmall,
                                color = NotionColors.Gray500,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "¥$totalIncome",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = NotionColors.Green,
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
                                text = "支出",
                                style = MaterialTheme.typography.labelSmall,
                                color = NotionColors.Gray500,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "¥$totalExpense",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = NotionColors.Red,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            
            // 表头分隔线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NotionColors.Gray300)
            )
        }
    }
    
    @Composable
    override fun ListContainer(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        Column(
            modifier = modifier
                .background(NotionColors.White)
        ) {
            // Notion 表格头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NotionColors.Gray100)
                    .padding(20.dp, 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = NotionColors.Gray400,
                    fontSize = 12.sp,
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = "分类",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = NotionColors.Gray600,
                    fontSize = 12.sp,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    text = "账户",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = NotionColors.Gray600,
                    fontSize = 12.sp,
                    modifier = Modifier.width(80.dp)
                )
                Text(
                    text = "备注",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = NotionColors.Gray600,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "金额",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = NotionColors.Gray600,
                    fontSize = 12.sp,
                    modifier = Modifier.width(100.dp)
                )
                Text(
                    text = "时间",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = NotionColors.Gray600,
                    fontSize = 12.sp,
                    modifier = Modifier.width(50.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NotionColors.Gray300)
            )
            
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 44.dp  // Notion 紧凑行高
            DemoDensity.Medium -> 52.dp
        }
    }
    
    override fun getItemSpacing(density: DemoDensity): Dp = 0.dp
}

// ==================== Item Spec ====================
class NotionMinimalItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.SINGLE_LINE  // Notion 表格单行
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.CENTER,
        notePosition = ItemSpec.Position.CENTER,
        dateTimePosition = ItemSpec.Position.RIGHT
    )
    
    override fun showIcons() = false  // Notion 极简风格
    override fun showDividers() = true
}

// ==================== Header Spec ====================
class NotionMinimalHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Column(
            modifier = modifier
                .background(NotionColors.White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Notion 页面标题风格
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📊 财务概览",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NotionColors.Gray900,
                    fontSize = 28.sp
                )
                Text(
                    text = "本月账目汇总统计",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NotionColors.Gray500,
                    fontSize = 14.sp
                )
            }
            
            // Notion 数据块风格
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = NotionColors.Gray200,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(NotionColors.White)
            ) {
                Column {
                    // 收入行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(NotionColors.Green)
                            )
                            Text(
                                text = "总收入",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NotionColors.Gray800,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "¥$income",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = NotionColors.Green,
                            fontSize = 16.sp
                        )
                    }
                    
                    // 细分隔线
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NotionColors.Gray200.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp)
                    )
                    
                    // 支出行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(NotionColors.Red)
                            )
                            Text(
                                text = "总支出",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NotionColors.Gray800,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "¥$expense",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = NotionColors.Red,
                            fontSize = 16.sp
                        )
                    }
                    
                    // 粗分隔线
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NotionColors.Gray300)
                    )
                    
                    // 结余行 - 突出显示
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NotionColors.Gray50)
                            .padding(16.dp, 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(NotionColors.Blue)
                            )
                            Text(
                                text = "净结余",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NotionColors.Gray900,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "¥$balance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) NotionColors.Blue else NotionColors.Red,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.MEDIUM
    override fun showDateSelector() = true  // Notion 支持数据筛选
    override fun showQuickStats() = true
}

// ==================== 其他 Specs 占位实现 ====================
class NotionMinimalFilterSpec : FilterSpec {
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
        Box(modifier = modifier.height(40.dp))
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.INLINE  // Notion 内联筛选
    override fun isCollapsible() = false
}

class NotionMinimalFormSpec : FormSpec {
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

class NotionMinimalDialogSpec : DialogSpec {
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

class NotionMinimalChartsSpec : ChartsSpec {
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
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.MINIMAL
    override fun showLegend() = false
    override fun showGrid() = true
}

class NotionMinimalSettingsSpec : SettingsSpec {
    @Composable
    override fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(NotionColors.White)
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) icon()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NotionColors.Gray900,
                        fontSize = 15.sp
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = NotionColors.Gray500,
                            fontSize = 13.sp
                        )
                    }
                }
                if (trailing != null) trailing()
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NotionColors.Gray200.copy(alpha = 0.6f))
                    .padding(horizontal = 20.dp)
            )
        }
    }
    
    @Composable
    override fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    ) {
        Column(modifier = modifier.padding(vertical = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NotionColors.Gray800,
                modifier = Modifier.padding(20.dp, 8.dp),
                fontSize = 16.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = NotionColors.Gray200,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(NotionColors.White)
            ) {
                items()
            }
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.LIST
}
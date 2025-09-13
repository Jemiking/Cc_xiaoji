package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 真正的极简主义规格实现
 * 
 * 设计原则：
 * 1. 零装饰 - 没有圆角、阴影、边框、背景色
 * 2. 纯文字 - 文字即界面
 * 3. 最小间距 - 仅使用必要的间距
 * 4. 单色系 - 黑白为主，极少功能色
 * 5. 扁平化 - 没有层次感，纯平面
 */
class TrueMinimalSpecs : SpecsRegistry.StyleSpecs() {
    
    // 极简色彩：只有黑白灰
    object Colors {
        val Text = Color.Black
        val SecondaryText = Color(0xFF999999)
        val Divider = Color(0xFFEEEEEE)
        val Background = Color.White
    }
    
    // 极简间距：最小化
    object Spacing {
        val xs = 4.dp
        val s = 8.dp
        val m = 12.dp
        val l = 16.dp
    }
    
    override val listSpec = TrueMinimalListSpec()
    override val itemSpec = TrueMinimalItemSpec()
    override val headerSpec = TrueMinimalHeaderSpec()
    override val filterSpec = TrueMinimalFilterSpec()
    override val formSpec = TrueMinimalFormSpec()
    override val dialogSpec = TrueMinimalDialogSpec()
    override val chartsSpec = TrueMinimalChartsSpec()
    override val settingsSpec = TrueMinimalSettingsSpec()
    
    override val baseStyle = BaseStyle.HIERARCHICAL
    override val description = "零装饰、纯文字、最小间距的真正极简主义"
    override val recommendedDensity = DemoDensity.Compact
}

/**
 * 极简列表规格
 */
class TrueMinimalListSpec : ListSpec {
    
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        // 极简列表项：纯文字，无背景
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,  // 无点击效果
                    onClick = onClick
                )
                .padding(vertical = TrueMinimalSpecs.Spacing.s),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：分类和备注
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category?.name ?: "未分类",
                    fontSize = 14.sp,
                    color = TrueMinimalSpecs.Colors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!transaction.note.isNullOrEmpty()) {
                    Text(
                        text = transaction.note,
                        fontSize = 12.sp,
                        color = TrueMinimalSpecs.Colors.SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 右侧：金额
            Text(
                text = if (transaction.type == TransactionType.INCOME) 
                    "+${transaction.amount}" else "-${transaction.amount}",
                fontSize = 14.sp,
                color = if (transaction.type == TransactionType.INCOME)
                    TrueMinimalSpecs.Colors.Text else TrueMinimalSpecs.Colors.SecondaryText
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
        // 极简日期标题
        Text(
            text = date.format(DateTimeFormatter.ofPattern("M月d日")),
            fontSize = 12.sp,
            color = TrueMinimalSpecs.Colors.SecondaryText,
            modifier = modifier.padding(vertical = TrueMinimalSpecs.Spacing.xs)
        )
    }
    
    @Composable
    override fun ListContainer(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        // 纯白背景，无装饰
        Box(modifier = modifier) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity) = when (density) {
        DemoDensity.Compact -> 40.dp
        DemoDensity.Medium -> 48.dp
    }
    
    override fun getItemSpacing(density: DemoDensity) = 0.dp  // 零间距
}

/**
 * 极简项目规格
 */
class TrueMinimalItemSpec : ItemSpec {
    
    override fun getLayout() = ItemSpec.ItemLayout.SINGLE_LINE
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.HIDDEN,
        notePosition = ItemSpec.Position.BELOW,
        dateTimePosition = ItemSpec.Position.HIDDEN
    )
    
    override fun showIcons() = false  // 不显示图标
    override fun showDividers() = true  // 显示极细分割线
}

/**
 * 极简头部规格
 */
class TrueMinimalHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        // 极简概览：纯文字
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(TrueMinimalSpecs.Spacing.m),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("收入", fontSize = 12.sp, color = TrueMinimalSpecs.Colors.SecondaryText)
                Text(income.toString(), fontSize = 14.sp, color = TrueMinimalSpecs.Colors.Text)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("支出", fontSize = 12.sp, color = TrueMinimalSpecs.Colors.SecondaryText)
                Text(expense.toString(), fontSize = 14.sp, color = TrueMinimalSpecs.Colors.Text)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("结余", fontSize = 12.sp, color = TrueMinimalSpecs.Colors.SecondaryText)
                Text(balance.toString(), fontSize = 14.sp, color = TrueMinimalSpecs.Colors.Text)
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.SMALL
    override fun showDateSelector() = false
    override fun showQuickStats() = false
}

/**
 * 极简筛选器规格
 */
class TrueMinimalFilterSpec : FilterSpec {
    
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
        // 极简筛选：仅文字按钮
        Row(
            modifier = modifier.padding(TrueMinimalSpecs.Spacing.s),
            horizontalArrangement = Arrangement.spacedBy(TrueMinimalSpecs.Spacing.m)
        ) {
            Text(
                text = selectedDateRange.label,
                fontSize = 13.sp,
                color = TrueMinimalSpecs.Colors.Text,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* 日期选择 */ }
            )
            if (selectedCategories.isNotEmpty()) {
                Text(
                    text = "分类(${selectedCategories.size})",
                    fontSize = 13.sp,
                    color = TrueMinimalSpecs.Colors.SecondaryText
                )
            }
        }
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.INLINE
    override fun isCollapsible() = false
}

/**
 * 极简表单规格
 */
class TrueMinimalFormSpec : FormSpec {
    
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        // 极简表单：纯文字输入
        Column(
            modifier = modifier.padding(TrueMinimalSpecs.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(TrueMinimalSpecs.Spacing.m)
        ) {
            // 金额输入
            var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
            Column {
                Text("金额", fontSize = 12.sp, color = TrueMinimalSpecs.Colors.SecondaryText)
                BasicTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = TrueMinimalSpecs.Colors.Text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Divider(color = TrueMinimalSpecs.Colors.Divider, thickness = 0.5.dp)
            }
            
            // 保存/取消按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "取消",
                    fontSize = 14.sp,
                    color = TrueMinimalSpecs.Colors.SecondaryText,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onCancel() }
                )
                Text(
                    text = "保存",
                    fontSize = 14.sp,
                    color = TrueMinimalSpecs.Colors.Text,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* onSave */ }
                )
            }
        }
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.VERTICAL
    override fun getFieldOrder() = listOf("amount", "category", "note")
    override fun showCalculator() = false
}

/**
 * 极简对话框规格
 */
class TrueMinimalDialogSpec : DialogSpec {
    
    @Composable
    override fun RenderDialog(
        title: String,
        content: @Composable () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier
    ) {
        // 极简对话框：白色背景，无装饰
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = modifier
                    .background(TrueMinimalSpecs.Colors.Background)
                    .padding(TrueMinimalSpecs.Spacing.l)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = TrueMinimalSpecs.Colors.Text
                )
                Spacer(modifier = Modifier.height(TrueMinimalSpecs.Spacing.m))
                content()
                Spacer(modifier = Modifier.height(TrueMinimalSpecs.Spacing.m))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "取消",
                        fontSize = 14.sp,
                        color = TrueMinimalSpecs.Colors.SecondaryText,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() }
                    )
                    Text(
                        text = "确定",
                        fontSize = 14.sp,
                        color = TrueMinimalSpecs.Colors.Text,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onConfirm() }
                    )
                }
            }
        }
    }
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.INLINE
    override fun showOverlay() = false
}

/**
 * 极简图表规格
 */
class TrueMinimalChartsSpec : ChartsSpec {
    
    @Composable
    override fun RenderPieChart(
        data: List<ChartData>,
        modifier: Modifier
    ) {
        // 极简图表：仅显示数据列表
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(TrueMinimalSpecs.Spacing.xs)
        ) {
            data.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.label,
                        fontSize = 13.sp,
                        color = TrueMinimalSpecs.Colors.SecondaryText
                    )
                    Text(
                        text = "${item.value}",
                        fontSize = 13.sp,
                        color = TrueMinimalSpecs.Colors.Text
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
        // 与饼图相同的列表展示
        RenderPieChart(data, modifier)
    }
    
    @Composable
    override fun RenderBarChart(
        data: List<ChartData>,
        modifier: Modifier
    ) {
        // 与饼图相同的列表展示
        RenderPieChart(data, modifier)
    }
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.MINIMAL
    override fun showLegend() = false
    override fun showGrid() = false
}

/**
 * 极简设置规格
 */
class TrueMinimalSettingsSpec : SettingsSpec {
    
    @Composable
    override fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        // 极简设置项：纯文字
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(
                    horizontal = TrueMinimalSpecs.Spacing.l,
                    vertical = TrueMinimalSpecs.Spacing.m
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = TrueMinimalSpecs.Colors.Text
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = TrueMinimalSpecs.Colors.SecondaryText
                    )
                }
            }
            trailing?.invoke()
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
                fontSize = 12.sp,
                color = TrueMinimalSpecs.Colors.SecondaryText,
                modifier = Modifier.padding(
                    horizontal = TrueMinimalSpecs.Spacing.l,
                    vertical = TrueMinimalSpecs.Spacing.s
                )
            )
            items()
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.LIST
}
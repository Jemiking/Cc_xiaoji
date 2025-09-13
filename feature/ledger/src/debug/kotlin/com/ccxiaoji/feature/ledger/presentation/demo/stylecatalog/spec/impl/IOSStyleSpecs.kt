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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import kotlinx.datetime.toJavaInstant

/**
 * iOS 风格规格实现
 * 特点：分组式设计、层次结构、浅色背景、细线分割
 */
class IOSStyleSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.HIERARCHICAL
    override val description = "iOS 分组式设计，清晰的层次结构"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = IOSListSpec()
    override val itemSpec = IOSItemSpec()
    override val headerSpec = IOSHeaderSpec()
    override val filterSpec = IOSFilterSpec()
    override val formSpec = IOSFormSpec()
    override val dialogSpec = IOSDialogSpec()
    override val chartsSpec = IOSChartsSpec()
    override val settingsSpec = IOSSettingsSpec()
}

// ==================== List Spec ====================
class IOSListSpec : ListSpec {
    
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类图标 - iOS风格圆形背景
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 主信息列
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.category?.name ?: "未分类",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = transaction.account?.name ?: "现金",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!transaction.note.isNullOrEmpty()) {
                            Text(
                                text = "• ${transaction.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // 右侧金额和时间
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}¥${transaction.amount}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (transaction.type == TransactionType.EXPENSE) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = DateTimeFormatter.ofPattern("HH:mm").format(transaction.dateTime.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // iOS风格的箭头
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // iOS风格的细线分割
            Divider(
                modifier = Modifier.padding(start = 68.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
        // iOS风格的分组头部 - 更简洁
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp, 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = DateTimeFormatter.ofPattern("M月d日 EEEE").format(date),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "收入¥$totalIncome 支出¥$totalExpense",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    @Composable
    override fun ListContainer(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        // iOS风格的分组容器 - 圆角卡片包含每个日期组
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
class IOSItemSpec : ItemSpec {
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
class IOSHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        // iOS风格的概览卡 - 更加简洁扁平
        Box(
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "本月账单",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 横向布局的金额信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "¥$income",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "收入",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "¥$expense",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "支出",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "净结余 ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥$balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (balance >= 0) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.MEDIUM
    override fun showDateSelector() = false  // iOS风格不显示复杂选择器
    override fun showQuickStats() = true
}

// ==================== 其他 Specs 占位实现 ====================
class IOSFilterSpec : FilterSpec {
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
        Box(modifier = modifier.height(48.dp))
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.SEGMENTS  // iOS分段控件
    override fun isCollapsible() = false
}

class IOSFormSpec : FormSpec {
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.GROUPED  // iOS分组式表单
    override fun getFieldOrder() = listOf("amount", "category", "account", "note")
    override fun showCalculator() = false
}

class IOSDialogSpec : DialogSpec {
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
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.IOS  // iOS风格对话框
    override fun showOverlay() = true
}

class IOSChartsSpec : ChartsSpec {
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
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.FLAT  // iOS扁平风格
    override fun showLegend() = false
    override fun showGrid() = true
}

class IOSSettingsSpec : SettingsSpec {
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
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge)
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (trailing != null) {
                    trailing()
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Divider(
                modifier = Modifier.padding(start = if (icon != null) 52.dp else 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
    
    @Composable
    override fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    ) {
        Column(modifier = modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp, 8.dp)
            )
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp)
            ) {
                items()
            }
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.GROUPED  // iOS分组式设置
}
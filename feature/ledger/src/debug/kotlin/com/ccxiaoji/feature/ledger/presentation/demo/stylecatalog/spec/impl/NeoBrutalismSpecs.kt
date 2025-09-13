package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
 * Neo Brutalism 风格规格实现
 * 特点：粗黑边框、强烈阴影、单色调、字体粗重、方形几何
 */
class NeoBrutalismSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.HIERARCHICAL
    override val description = "Neo Brutalism 粗犷几何设计，强烈视觉冲击"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = NeoBrutalismListSpec()
    override val itemSpec = NeoBrutalismItemSpec()
    override val headerSpec = NeoBrutalismHeaderSpec()
    override val filterSpec = NeoBrutalismFilterSpec()
    override val formSpec = NeoBrutalismFormSpec()
    override val dialogSpec = NeoBrutalismDialogSpec()
    override val chartsSpec = NeoBrutalismChartsSpec()
    override val settingsSpec = NeoBrutalismSettingsSpec()
}

// ==================== List Spec ====================
class NeoBrutalismListSpec : ListSpec {
    
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
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RectangleShape,
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .border(
                    width = 4.dp,
                    color = Color.Black,
                    shape = RectangleShape
                )
                .background(
                    if (transaction.type == TransactionType.EXPENSE) Color(0xFFFF6B6B)
                    else Color(0xFF51CF66)
                )
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 分类 - 使用粗体等宽字体
                    Text(
                        text = (transaction.category?.name ?: "UNCATEGORIZED")?.uppercase() ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        ),
                        color = Color.Black
                    )
                    
                    // 账户信息
                    Box(
                        modifier = Modifier
                            .background(Color.Black)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = (transaction.account?.name ?: "CASH")?.uppercase() ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.White
                        )
                    }
                    
                    // 备注（如果有）
                    if (!transaction.note.isNullOrEmpty()) {
                        Text(
                            text = transaction.note?.uppercase() ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // 金额区域 - 超粗边框突出
                Box(
                    modifier = Modifier
                        .border(3.dp, Color.Black, RectangleShape)
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}¥${transaction.amount}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            ),
                            color = Color.Black
                        )
                        
                        Text(
                            text = DateTimeFormatter.ofPattern("HH:mm").format(transaction.dateTime.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
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
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RectangleShape,
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .border(6.dp, Color.Black, RectangleShape)
                .background(Color.Yellow)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DateTimeFormatter.ofPattern("yyyy/MM/dd EEE").format(date)?.uppercase() ?: "",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ),
                    color = Color.Black
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (totalIncome > 0) {
                        Box(
                            modifier = Modifier
                                .border(2.dp, Color.Black, RectangleShape)
                                .background(Color(0xFF51CF66))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "IN: +¥$totalIncome",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                ),
                                color = Color.Black
                            )
                        }
                    }
                    
                    if (totalExpense > 0) {
                        Box(
                            modifier = Modifier
                                .border(2.dp, Color.Black, RectangleShape)
                                .background(Color(0xFFFF6B6B))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "OUT: -¥$totalExpense",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                ),
                                color = Color.Black
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
                .background(Color(0xFFF8F8F8))  // 浅灰背景突出brutal元素
        ) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 90.dp  // 粗犷风格需要更大空间
            DemoDensity.Medium -> 110.dp
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
class NeoBrutalismItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.HIERARCHICAL
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.BELOW,
        notePosition = ItemSpec.Position.BELOW,
        dateTimePosition = ItemSpec.Position.RIGHT
    )
    
    override fun showIcons() = false  // Brutalism通常不用装饰性图标
    override fun showDividers() = false
}

// ==================== Header Spec ====================
class NeoBrutalismHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RectangleShape,
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .border(6.dp, Color.Black, RectangleShape)
                .background(Color.White)
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题 - 超大超粗
                Text(
                    text = "MONTHLY REPORT",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    ),
                    color = Color.Black
                )
                
                // 不规则布局的数据块
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 收入块 - 不对称设计
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(8.dp, RectangleShape, true)
                                .border(4.dp, Color.Black, RectangleShape)
                                .background(Color(0xFF51CF66))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "INCOME",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = "¥$income",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp
                                    ),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                    
                    // 支出块 - 右对齐
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(8.dp, RectangleShape, true)
                                .border(4.dp, Color.Black, RectangleShape)
                                .background(Color(0xFFFF6B6B))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "EXPENSE",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = "¥$expense",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp
                                    ),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                    
                    // 结余块 - 中央突出
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(12.dp, RectangleShape, true)
                                .border(5.dp, Color.Black, RectangleShape)
                                .background(Color.Yellow)
                                .padding(20.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "BALANCE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = "¥$balance",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 30.sp
                                    ),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.LARGE
    override fun showDateSelector() = false  // Brutalism通常避免复杂UI
    override fun showQuickStats() = true
}

// ==================== 其他 Specs 占位实现 ====================
class NeoBrutalismFilterSpec : FilterSpec {
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
        Box(modifier = modifier.height(60.dp))
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.INLINE
    override fun isCollapsible() = false
}

class NeoBrutalismFormSpec : FormSpec {
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
    override fun getFieldOrder() = listOf("amount", "category", "account", "note")
    override fun showCalculator() = false
}

class NeoBrutalismDialogSpec : DialogSpec {
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

class NeoBrutalismChartsSpec : ChartsSpec {
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
    override fun showGrid() = false
}

class NeoBrutalismSettingsSpec : SettingsSpec {
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
                .padding(4.dp)
                .shadow(6.dp, RectangleShape, true)
                .border(3.dp, Color.Black, RectangleShape)
                .background(Color.White)
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = title?.uppercase() ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = Color.Black
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle?.uppercase() ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
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
                    .padding(horizontal = 4.dp)
                    .shadow(8.dp, RectangleShape, true)
                    .border(4.dp, Color.Black, RectangleShape)
                    .background(Color.Yellow)
                    .padding(16.dp, 12.dp)
            ) {
                Text(
                    text = title?.uppercase() ?: "",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ),
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            items()
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.LIST
}
package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * I Discord风格规格实现
 * 特点：深色层次、侧边栏布局、频道风格、游戏化元素
 */
class DiscordSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.HIERARCHICAL
    override val description = "Discord 深色层次，游戏化界面设计"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = DiscordListSpec()
    override val itemSpec = DiscordItemSpec()
    override val headerSpec = DiscordHeaderSpec()
    override val filterSpec = DiscordFilterSpec()
    override val formSpec = DiscordFormSpec()
    override val dialogSpec = DiscordDialogSpec()
    override val chartsSpec = DiscordChartsSpec()
    override val settingsSpec = DiscordSettingsSpec()
}

// Discord 色彩定义
private object DiscordColors {
    val DarkPrimary = Color(0xFF2C2F33)      // 主深色背景
    val DarkSecondary = Color(0xFF36393F)    // 次级深色背景
    val DarkTertiary = Color(0xFF40444B)     // 第三级背景
    val Blurple = Color(0xFF5865F2)          // Discord 蓝紫色
    val Green = Color(0xFF57F287)            // 在线绿色
    val Yellow = Color(0xFFFEE75C)           // 警告黄色
    val Red = Color(0xFFED4245)              // 错误红色
    val White = Color(0xFFFFFFFF)            // 纯白
    val OffWhite = Color(0xFFDCDDDE)         // 灰白
    val LightGray = Color(0xFFB9BBBE)        // 浅灰
    val MediumGray = Color(0xFF8E9297)       // 中灰
    val DarkGray = Color(0xFF4F545C)         // 深灰
}

// ==================== List Spec ====================
class DiscordListSpec : ListSpec {
    
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
                .background(DiscordColors.DarkPrimary)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Discord 风格头像/图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (transaction.type) {
                                TransactionType.EXPENSE -> DiscordColors.Red.copy(alpha = 0.2f)
                                TransactionType.INCOME -> DiscordColors.Green.copy(alpha = 0.2f)
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = when (transaction.type) {
                                TransactionType.EXPENSE -> DiscordColors.Red
                                TransactionType.INCOME -> DiscordColors.Green
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (transaction.type) {
                            TransactionType.EXPENSE -> "−"
                            TransactionType.INCOME -> "+"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (transaction.type) {
                            TransactionType.EXPENSE -> DiscordColors.Red
                            TransactionType.INCOME -> DiscordColors.Green
                        },
                        fontSize = 18.sp
                    )
                }
                
                // 主要内容 - Discord 消息风格
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // 用户名风格的分类显示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = transaction.category?.name ?: "未知频道",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = DiscordColors.White,
                            fontSize = 16.sp
                        )
                        
                        // 时间戳 - Discord 风格
                        Text(
                            text = DateTimeFormatter.ofPattern("今天 HH:mm").format(transaction.dateTime.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                            style = MaterialTheme.typography.labelSmall,
                            color = DiscordColors.MediumGray,
                            fontSize = 12.sp
                        )
                        
                        // 状态指示器
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (transaction.type) {
                                        TransactionType.EXPENSE -> DiscordColors.Red
                                        TransactionType.INCOME -> DiscordColors.Green
                                    }
                                )
                        )
                    }
                    
                    // 消息内容风格
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 账户标签 - Discord 角色风格
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DiscordColors.Blurple.copy(alpha = 0.3f))
                                    .border(
                                        width = 1.dp,
                                        color = DiscordColors.Blurple,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "@${transaction.account?.name ?: "现金"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DiscordColors.Blurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        if (!transaction.note.isNullOrEmpty()) {
                            // 引用消息风格的备注
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DiscordColors.DarkSecondary)
                                    .padding(8.dp, 4.dp)
                            ) {
                                Text(
                                    text = "💬 ${transaction.note}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DiscordColors.LightGray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                
                // 金额 - Discord 按钮风格
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (transaction.type == TransactionType.EXPENSE) DiscordColors.Red
                            else DiscordColors.Green
                        )
                        .padding(12.dp, 8.dp)
                ) {
                    Text(
                        text = "¥${transaction.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DiscordColors.White,
                        fontSize = 15.sp
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
                .background(DiscordColors.DarkSecondary)
                .padding(16.dp, 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Discord 频道图标
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = DiscordColors.MediumGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = DateTimeFormatter.ofPattern("M月d日-EEEE").format(date),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DiscordColors.OffWhite,
                        fontSize = 14.sp
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (totalIncome > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DiscordColors.Green.copy(alpha = 0.2f))
                                .padding(6.dp, 3.dp)
                        ) {
                            Text(
                                text = "+¥$totalIncome",
                                style = MaterialTheme.typography.labelSmall,
                                color = DiscordColors.Green,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    if (totalExpense > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DiscordColors.Red.copy(alpha = 0.2f))
                                .padding(6.dp, 3.dp)
                        ) {
                            Text(
                                text = "-¥$totalExpense",
                                style = MaterialTheme.typography.labelSmall,
                                color = DiscordColors.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
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
                .background(DiscordColors.DarkPrimary)
        ) {
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return when (density) {
            DemoDensity.Compact -> 72.dp
            DemoDensity.Medium -> 88.dp
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
class DiscordItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.HIERARCHICAL
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.BELOW,
        notePosition = ItemSpec.Position.BELOW,
        dateTimePosition = ItemSpec.Position.ABOVE
    )
    
    override fun showIcons() = true
    override fun showDividers() = false  // Discord 使用间距而非分割线
}

// ==================== Header Spec ====================
class DiscordHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DiscordColors.DarkSecondary)
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Discord 服务器风格标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DiscordColors.Blurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = "财务服务器",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DiscordColors.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "本月数据频道",
                            style = MaterialTheme.typography.bodySmall,
                            color = DiscordColors.MediumGray,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Discord 嵌入式消息风格
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DiscordColors.DarkTertiary)
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(DiscordColors.Blurple)
                            )
                            Text(
                                text = "月度统计 Embed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DiscordColors.Blurple,
                                fontSize = 14.sp
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 收入字段
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "💚 总收入",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DiscordColors.LightGray,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "¥$income",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DiscordColors.Green,
                                    fontSize = 14.sp
                                )
                            }
                            
                            // 支出字段
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "❤️ 总支出",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DiscordColors.LightGray,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "¥$expense",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DiscordColors.Red,
                                    fontSize = 14.sp
                                )
                            }
                            
                            // 分隔线
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(DiscordColors.DarkGray)
                            )
                            
                            // 结余字段 - 突出显示
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💎 净结余",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DiscordColors.White,
                                    fontSize = 16.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (balance >= 0) DiscordColors.Green else DiscordColors.Red
                                        )
                                        .padding(8.dp, 4.dp)
                                ) {
                                    Text(
                                        text = "¥$balance",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DiscordColors.White,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.MEDIUM
    override fun showDateSelector() = false  // Discord 风格保持简洁
    override fun showQuickStats() = true
}

// ==================== 其他 Specs 占位实现 ====================
class DiscordFilterSpec : FilterSpec {
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
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.SIDEBAR  // Discord 侧边栏风格
    override fun isCollapsible() = true
}

class DiscordFormSpec : FormSpec {
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.MODAL  // Discord 弹窗风格
    override fun getFieldOrder() = listOf("amount", "category", "account", "note")
    override fun showCalculator() = false
}

class DiscordDialogSpec : DialogSpec {
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

class DiscordChartsSpec : ChartsSpec {
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
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.DETAILED  // Discord 详细风格
    override fun showLegend() = true
    override fun showGrid() = false
}

class DiscordSettingsSpec : SettingsSpec {
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
                .background(DiscordColors.DarkSecondary)
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
                        fontWeight = FontWeight.Medium,
                        color = DiscordColors.White
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = DiscordColors.LightGray
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
            Text(
                text = title?.uppercase() ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DiscordColors.MediumGray,
                modifier = Modifier.padding(16.dp, 8.dp),
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DiscordColors.DarkTertiary)
            ) {
                items()
            }
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.GROUPED
}
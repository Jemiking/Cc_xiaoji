package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
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
 * E 马卡龙风格规格实现
 * 特点：浅色圆润、柔和色调、可爱元素、高圆角半径
 */
class MacaronSpecs : SpecsRegistry.StyleSpecs() {
    
    override val baseStyle = BaseStyle.BALANCED
    override val description = "马卡龙配色，浅色圆润，温柔可爱"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = MacaronListSpec()
    override val itemSpec = MacaronItemSpec()
    override val headerSpec = MacaronHeaderSpec()
    override val filterSpec = MacaronFilterSpec()
    override val formSpec = MacaronFormSpec()
    override val dialogSpec = MacaronDialogSpec()
    override val chartsSpec = MacaronChartsSpec()
    override val settingsSpec = MacaronSettingsSpec()
}

// 马卡龙色彩定义
private object MacaronColors {
    val LightPink = Color(0xFFFFE1E6)
    val LightBlue = Color(0xFFE1F0FF)
    val LightGreen = Color(0xFFE8F5E8)
    val LightPurple = Color(0xFFF0E1FF)
    val LightYellow = Color(0xFFFFF8E1)
    val LightCoral = Color(0xFFFFE8E1)
    val SoftPink = Color(0xFFFF9BB5)
    val SoftBlue = Color(0xFF9BB5FF)
    val SoftGreen = Color(0xFFB5FF9B)
    val SoftPurple = Color(0xFFD1B5FF)
}

// ==================== List Spec ====================
class MacaronListSpec : ListSpec {
    
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
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Gray.copy(alpha = 0.2f),
                    spotColor = Color.Gray.copy(alpha = 0.2f)
                )
                .background(
                    if (transaction.type == TransactionType.EXPENSE) MacaronColors.LightCoral
                    else MacaronColors.LightGreen
                )
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 可爱的圆形图标容器
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (transaction.type == TransactionType.EXPENSE) MacaronColors.SoftPink
                            else MacaronColors.SoftGreen,
                            shape = CircleShape
                        )
                        .shadow(
                            elevation = 3.dp,
                            shape = CircleShape,
                            ambientColor = Color.Gray.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.type) {
                            TransactionType.EXPENSE -> Icons.Default.Favorite
                            TransactionType.INCOME -> Icons.Default.Star
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // 主信息区域
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transaction.category?.name ?: "未分类",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D2D2D),
                        fontSize = 16.sp
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 可爱的圆形账户标签
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MacaronColors.LightPurple)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = transaction.account?.name ?: "现金",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6B5B95),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // 小圆点分隔符
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    MacaronColors.SoftPurple,
                                    shape = CircleShape
                                )
                        )
                        
                        Text(
                            text = DateTimeFormatter.ofPattern("HH:mm").format(transaction.dateTime.toJavaInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6B6B6B),
                            fontSize = 11.sp
                        )
                    }
                    
                    if (!transaction.note.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "💭 ${transaction.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B8B8B),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // 可爱的金额胶囊
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color.Gray.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (transaction.type == TransactionType.EXPENSE) "💸" else "💰",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "¥${transaction.amount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (transaction.type == TransactionType.EXPENSE) Color(0xFFE57373)
                                else Color(0xFF81C784),
                                fontSize = 15.sp
                            )
                        }
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MacaronColors.LightYellow)
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Gray.copy(alpha = 0.1f)
                )
                .padding(16.dp)
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
                    Text(
                        text = "📅",
                        fontSize = 16.sp
                    )
                    Column {
                        Text(
                            text = DateTimeFormatter.ofPattern("M月d日").format(date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2D2D2D),
                            fontSize = 16.sp
                        )
                        Text(
                            text = DateTimeFormatter.ofPattern("EEEE").format(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B8B8B),
                            fontSize = 12.sp
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (totalIncome > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MacaronColors.SoftGreen)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "💚", fontSize = 12.sp)
                                Text(
                                    text = "¥$totalIncome",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    
                    if (totalExpense > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MacaronColors.SoftPink)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "💖", fontSize = 12.sp)
                                Text(
                                    text = "¥$totalExpense",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
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
                .background(MacaronColors.LightPink.copy(alpha = 0.3f))
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
            DemoDensity.Compact -> 6.dp
            DemoDensity.Medium -> 8.dp
        }
    }
}

// ==================== Item Spec ====================
class MacaronItemSpec : ItemSpec {
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

// ==================== Header Spec ====================
class MacaronHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MacaronColors.LightBlue)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Gray.copy(alpha = 0.15f),
                    spotColor = Color.Gray.copy(alpha = 0.15f)
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🌸", fontSize = 24.sp)
                    Text(
                        text = "本月小账本",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D2D2D),
                        fontSize = 20.sp
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 可爱收入卡片
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MacaronColors.SoftGreen)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    ambientColor = Color.Gray.copy(alpha = 0.1f)
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "💰", fontSize = 24.sp)
                                Text(
                                    text = "收入",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "¥$income",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        
                        // 可爱支出卡片
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MacaronColors.SoftPink)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    ambientColor = Color.Gray.copy(alpha = 0.1f)
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "💸", fontSize = 24.sp)
                                Text(
                                    text = "支出",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "¥$expense",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                    
                    // 超可爱结余卡片
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (balance >= 0) MacaronColors.SoftBlue else MacaronColors.SoftPink
                            )
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color.Gray.copy(alpha = 0.15f)
                            )
                            .padding(20.dp)
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
                                Text(
                                    text = if (balance >= 0) "🎉" else "🥺",
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "结余",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "¥$balance",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.MEDIUM
    override fun showDateSelector() = false  // 马卡龙风格保持简洁可爱
    override fun showQuickStats() = true
}

// ==================== 其他 Specs 占位实现 ====================
class MacaronFilterSpec : FilterSpec {
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
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.CHIPS
    override fun isCollapsible() = true
}

class MacaronFormSpec : FormSpec {
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
    override fun showCalculator() = false  // 保持界面简洁可爱
}

class MacaronDialogSpec : DialogSpec {
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

class MacaronChartsSpec : ChartsSpec {
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
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.GRADIENT  // 柔和渐变
    override fun showLegend() = true
    override fun showGrid() = false  // 保持清爽
}

class MacaronSettingsSpec : SettingsSpec {
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
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.8f))
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Gray.copy(alpha = 0.1f)
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
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D2D2D)
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B8B8B)
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
        Column(modifier = modifier.padding(vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(12.dp, 8.dp)
            ) {
                Text(text = "🌟", fontSize = 16.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B6B6B),
                    fontSize = 15.sp
                )
            }
            items()
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.CARDS
}
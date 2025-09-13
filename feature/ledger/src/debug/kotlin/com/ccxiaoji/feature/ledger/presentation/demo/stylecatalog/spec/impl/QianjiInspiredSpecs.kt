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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
 * 钱迹风格设计规范 - 1:1精确复制Web版本
 * 基于TailwindCSS标准色值和间距系统
 */
class QianjiInspiredSpecs : SpecsRegistry.StyleSpecs() {
    
    // ========== Tailwind → Android 1:1映射系统 ==========
    
    // 一、标准色值系统 (Tailwind Colors)
    object Colors {
        // 主色系 - Tailwind标准色
        val Blue500 = Color(0xFF3B82F6)           // bg-blue-500 标准蓝
        val Red500 = Color(0xFFEF4444)            // bg-red-500/text-red-500 标准红
        val Gray100 = Color(0xFFF3F4F6)           // bg-gray-100 页面背景
        val White = Color(0xFFFFFFFF)             // bg-white 卡片背景
        
        // 文字色阶 - Tailwind标准灰度
        val Gray900 = Color(0xFF111827)           // text-gray-900 主文字
        val Gray500 = Color(0xFF6B7280)           // text-gray-500 次要文字  
        val Gray400 = Color(0xFF9CA3AF)           // text-gray-400 提示文字
        val WhiteText = Color(0xFFFFFFFF)         // text-white 白色文字
        
        // 边框分割线
        val Gray100Border = Color(0xFFF3F4F6)     // border-gray-100
    }
    
    // 二、TailwindCSS间距系统
    object Spacing {
        // 标准间距映射 (Tailwind * 4 = dp)
        const val px1 = 4                        // px-1 → 4dp
        const val px2 = 8                        // px-2 → 8dp  
        const val px3 = 12                       // px-3 → 12dp
        const val px4 = 16                       // px-4 → 16dp
        const val px6 = 24                       // px-6 → 24dp
        const val px8 = 32                       // px-8 → 32dp
        
        const val py1 = 4                        // py-1 → 4dp
        const val py2 = 8                        // py-2 → 8dp
        const val py3 = 12                       // py-3 → 12dp
        const val py4 = 16                       // py-4 → 16dp
        const val py6 = 24                       // py-6 → 24dp
        
        const val mt4 = 16                       // mt-4 → 16dp
        const val mb2 = 8                        // mb-2 → 8dp
        const val mb6 = 24                       // mb-6 → 24dp
        const val mr3 = 12                       // mr-3 → 12dp
        
        const val spaceY4 = 16                   // space-y-4 → 16dp
    }
    
    // 三、Tailwind字体系统
    object Typography {
        const val textXs = 12                    // text-xs → 12sp
        const val textSm = 14                    // text-sm → 14sp  
        const val textBase = 16                  // text-base → 16sp
        const val textLg = 18                    // text-lg → 18sp
        const val textXl = 20                    // text-xl → 20sp
        const val text3Xl = 48                   // text-3xl → 48sp (超大金额)
    }
    
    // 四、尺寸系统 (w-* h-*)
    object Sizes {
        const val w2h2 = 8                       // w-2 h-2 → 8dp (红点)
        const val w8h8 = 32                      // w-8 h-8 → 32dp (顶部按钮)
        const val w14h14 = 56                    // w-14 h-14 → 56dp (FAB)
    }
    
    // 五、圆角系统
    object Corners {
        const val roundedLg = 8                  // rounded-lg → 8dp
        const val roundedFull = 50               // rounded-full → 50% (圆形)
    }
    
    override val baseStyle = BaseStyle.BALANCED
    override val description = "钱迹风格 - 蓝色渐变顶部，红色支出标记"
    override val recommendedDensity = DemoDensity.Medium
    
    override val listSpec = QianjiListSpec()
    override val itemSpec = QianjiItemSpec()
    override val headerSpec = QianjiHeaderSpec()
    override val filterSpec = QianjiFilterSpec()
    override val formSpec = QianjiFormSpec()
    override val dialogSpec = QianjiDialogSpec()
    override val chartsSpec = QianjiChartsSpec()
    override val settingsSpec = QianjiSettingsSpec()
}

// ==================== List Spec ====================  
class QianjiListSpec : ListSpec {
    
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        // 单个交易项 - 1:1复制Web版本 (flex items-center px-4 py-3 border-b border-gray-100)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = QianjiInspiredSpecs.Spacing.px4.dp,
                        vertical = QianjiInspiredSpecs.Spacing.py3.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 红色圆点 (w-2 h-2 bg-red-500 rounded-full mr-3)
                Box(
                    modifier = Modifier
                        .size(QianjiInspiredSpecs.Sizes.w2h2.dp)
                        .background(
                            QianjiInspiredSpecs.Colors.Red500,
                            CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(QianjiInspiredSpecs.Spacing.mr3.dp))
                
                // 中间信息区域 (flex-1)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 分类名称 (text-gray-900 font-medium)
                    Text(
                        text = transaction.category?.name ?: "日用品",
                        fontSize = QianjiInspiredSpecs.Typography.textBase.sp,
                        fontWeight = FontWeight.Medium,
                        color = QianjiInspiredSpecs.Colors.Gray900
                    )
                    
                    // 描述信息 (text-sm text-gray-500)
                    if (!transaction.note.isNullOrEmpty()) {
                        Text(
                            text = transaction.note,
                            fontSize = QianjiInspiredSpecs.Typography.textSm.sp,
                            color = QianjiInspiredSpecs.Colors.Gray500
                        )
                    }
                }
                
                // 右侧金额和账户信息 (text-right)
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // 金额显示 (text-red-500 font-medium)
                    Text(
                        text = if (transaction.type == TransactionType.EXPENSE)
                            "-¥${String.format("%.2f", transaction.amount)}"
                        else
                            "+¥${String.format("%.2f", transaction.amount)}",
                        fontSize = QianjiInspiredSpecs.Typography.textBase.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (transaction.type == TransactionType.EXPENSE)
                            QianjiInspiredSpecs.Colors.Red500
                        else
                            QianjiInspiredSpecs.Colors.Gray900
                    )
                    
                    // 账户信息 (text-xs text-gray-400)
                    Text(
                        text = transaction.account?.name ?: "榕-微信零钱",
                        fontSize = QianjiInspiredSpecs.Typography.textXs.sp,
                        color = QianjiInspiredSpecs.Colors.Gray400
                    )
                }
            }
            
            // 分割线 (border-b border-gray-100)
            Divider(
                color = QianjiInspiredSpecs.Colors.Gray100Border,
                thickness = 1.dp
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
        // 日期分组头部 - 1:1复制 (flex justify-between items-center px-4 py-3 border-b border-gray-100)
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QianjiInspiredSpecs.Colors.White)
                    .padding(
                        horizontal = QianjiInspiredSpecs.Spacing.px4.dp,
                        vertical = QianjiInspiredSpecs.Spacing.py3.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 日期显示 (text-gray-900 font-medium)
                Text(
                    text = DateTimeFormatter.ofPattern("MM.dd").format(date) + 
                           " 周${getDayOfWeekChinese(date.dayOfWeek.value)}",
                    fontSize = QianjiInspiredSpecs.Typography.textBase.sp,
                    fontWeight = FontWeight.Medium,
                    color = QianjiInspiredSpecs.Colors.Gray900
                )
                
                // 当日支出 (text-gray-900 font-medium)  
                Text(
                    text = "支:¥${String.format("%.2f", totalExpense)}",
                    fontSize = QianjiInspiredSpecs.Typography.textBase.sp,
                    fontWeight = FontWeight.Medium,
                    color = QianjiInspiredSpecs.Colors.Gray900
                )
            }
            
            // 分割线 (border-b border-gray-100)
            Divider(
                color = QianjiInspiredSpecs.Colors.Gray100Border,
                thickness = 1.dp
            )
        }
    }
    
    // 辅助函数：获取中文星期
    private fun getDayOfWeekChinese(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "一"
            2 -> "二" 
            3 -> "三"
            4 -> "四"
            5 -> "五"
            6 -> "六"
            7 -> "日"
            else -> ""
        }
    }
    
    @Composable
    override fun ListContainer(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        // 列表容器 - 在灰色背景上显示白色卡片 (mt-4 space-y-4)
        Column(
            modifier = modifier
                .background(QianjiInspiredSpecs.Colors.Gray100)
                .padding(top = QianjiInspiredSpecs.Spacing.mt4.dp),
            verticalArrangement = Arrangement.spacedBy(QianjiInspiredSpecs.Spacing.spaceY4.dp)
        ) {
            // 每个日期分组作为独立的白色卡片 (bg-white rounded-lg overflow-hidden)
            content()
        }
    }
    
    override fun getItemHeight(density: DemoDensity): Dp {
        return 60.dp // Web版本的py-3约等于60dp总高度
    }
    
    override fun getItemSpacing(density: DemoDensity): Dp {
        return 0.dp // 间距通过space-y-4处理
    }
}

// ==================== Header Spec ====================
class QianjiHeaderSpec : HeaderSpec {
    
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Column(modifier = modifier) {
            // 蓝色顶部区域 - 1:1复制Web版本 (bg-blue-500 text-white px-4 py-4)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QianjiInspiredSpecs.Colors.Blue500)
                    .padding(
                        horizontal = QianjiInspiredSpecs.Spacing.px4.dp,
                        vertical = QianjiInspiredSpecs.Spacing.py4.dp
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 顶部导航栏 (flex justify-between items-center mb-6)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = QianjiInspiredSpecs.Spacing.mb6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧菜单按钮 (text-white text-xl)
                        Text(
                            text = "☰",
                            fontSize = QianjiInspiredSpecs.Typography.textXl.sp,
                            color = QianjiInspiredSpecs.Colors.WhiteText
                        )
                        
                        // 中间日期选择器 (flex items-center gap-1)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2023-07",
                                fontSize = QianjiInspiredSpecs.Typography.textLg.sp,
                                fontWeight = FontWeight.Medium,
                                color = QianjiInspiredSpecs.Colors.WhiteText
                            )
                            Text(
                                text = " ▼",
                                fontSize = QianjiInspiredSpecs.Typography.textSm.sp,
                                color = QianjiInspiredSpecs.Colors.WhiteText
                            )
                        }
                        
                        // 右侧功能按钮组 (flex gap-3)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 日历按钮 (w-8 h-8 border border-white/30 rounded)
                            Box(
                                modifier = Modifier
                                    .size(QianjiInspiredSpecs.Sizes.w8h8.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        QianjiInspiredSpecs.Colors.WhiteText.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📅",
                                    fontSize = QianjiInspiredSpecs.Typography.textSm.sp
                                )
                            }
                            
                            // 统计按钮
                            Box(
                                modifier = Modifier
                                    .size(QianjiInspiredSpecs.Sizes.w8h8.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        QianjiInspiredSpecs.Colors.WhiteText.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📊",
                                    fontSize = QianjiInspiredSpecs.Typography.textSm.sp
                                )
                            }
                            
                            // 刷新按钮
                            Box(
                                modifier = Modifier
                                    .size(QianjiInspiredSpecs.Sizes.w8h8.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        QianjiInspiredSpecs.Colors.WhiteText.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "↻",
                                    fontSize = QianjiInspiredSpecs.Typography.textSm.sp
                                )
                            }
                        }
                    }
                    
                    // 月支出信息区域 (mb-2)
                    Column(
                        modifier = Modifier.padding(bottom = QianjiInspiredSpecs.Spacing.mb2.dp)
                    ) {
                        // 月支出标题 (text-sm opacity-90)
                        Text(
                            text = "月支出",
                            fontSize = QianjiInspiredSpecs.Typography.textSm.sp,
                            color = QianjiInspiredSpecs.Colors.WhiteText.copy(alpha = 0.9f)
                        )
                        
                        // 超大金额显示 (text-3xl font-light)
                        Text(
                            text = "¥${String.format("%.2f", expense)}",
                            fontSize = QianjiInspiredSpecs.Typography.text3Xl.sp,
                            fontWeight = FontWeight.Light,
                            color = QianjiInspiredSpecs.Colors.WhiteText
                        )
                    }
                    
                    // 底部收入结余信息 (flex justify-between text-sm)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "月收入 ¥${String.format("%.2f", income)}",
                            fontSize = QianjiInspiredSpecs.Typography.textSm.sp,
                            color = QianjiInspiredSpecs.Colors.WhiteText
                        )
                        
                        Text(
                            text = "本月结余 ${if (balance >= 0) "" else "-"}¥${String.format("%.2f", Math.abs(balance))}",
                            fontSize = QianjiInspiredSpecs.Typography.textSm.sp,
                            color = QianjiInspiredSpecs.Colors.WhiteText
                        )
                    }
                }
            }
            
            // 页面内容区域开始 (px-4 pb-20)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QianjiInspiredSpecs.Colors.Gray100)
                    .padding(horizontal = QianjiInspiredSpecs.Spacing.px4.dp)
                    .padding(bottom = 80.dp) // pb-20
            ) {
                // 预算卡片 (bg-white rounded-lg px-4 py-3 mt-4)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QianjiInspiredSpecs.Spacing.mt4.dp),
                    shape = RoundedCornerShape(QianjiInspiredSpecs.Corners.roundedLg.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = QianjiInspiredSpecs.Colors.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = QianjiInspiredSpecs.Spacing.px4.dp,
                                vertical = QianjiInspiredSpecs.Spacing.py3.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧预算信息
                        Column {
                            Text(
                                text = "预算",
                                fontSize = QianjiInspiredSpecs.Typography.textBase.sp,
                                fontWeight = FontWeight.Medium,
                                color = QianjiInspiredSpecs.Colors.Gray900
                            )
                            Text(
                                text = "剩余: --",
                                fontSize = QianjiInspiredSpecs.Typography.textSm.sp,
                                color = QianjiInspiredSpecs.Colors.Gray500,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        // 右侧更多信息
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "⋯",
                                fontSize = QianjiInspiredSpecs.Typography.textXs.sp,
                                color = QianjiInspiredSpecs.Colors.Gray400
                            )
                            Text(
                                text = "总额: 未设置",
                                fontSize = QianjiInspiredSpecs.Typography.textSm.sp,
                                color = QianjiInspiredSpecs.Colors.Gray500,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.LARGE
    override fun showDateSelector() = true
    override fun showQuickStats() = false
}

// ==================== Item Spec ====================
class QianjiItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.CARD // 使用卡片布局
    
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.RIGHT,
        notePosition = ItemSpec.Position.BELOW,
        dateTimePosition = ItemSpec.Position.HIDDEN
    )
    
    override fun showIcons() = false // 使用红点，不是图标
    override fun showDividers() = true
}

// ==================== 其他 Specs 占位实现 ====================
class QianjiFilterSpec : FilterSpec {
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
        // 占位实现
        Box(modifier = modifier.height(48.dp))
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.CHIPS
    override fun isCollapsible() = false
}

class QianjiFormSpec : FormSpec {
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        // 占位实现
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.VERTICAL
    override fun getFieldOrder() = listOf("amount", "category", "account", "note")
    override fun showCalculator() = true
}

class QianjiDialogSpec : DialogSpec {
    @Composable
    override fun RenderDialog(
        title: String,
        content: @Composable () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier
    ) {
        // 占位实现
        Box(modifier = modifier.height(200.dp))
    }
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.BOTTOM_SHEET
    override fun showOverlay() = true
}

class QianjiChartsSpec : ChartsSpec {
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
    override fun showLegend() = true
    override fun showGrid() = false
}

class QianjiSettingsSpec : SettingsSpec {
    @Composable
    override fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(QianjiInspiredSpecs.Colors.White)
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke()
            if (icon != null) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = QianjiInspiredSpecs.Colors.Gray900
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = QianjiInspiredSpecs.Colors.Gray500
                    )
                }
            }
            trailing?.invoke()
        }
        Divider(
            color = QianjiInspiredSpecs.Colors.Gray100Border,
            thickness = 0.5.dp
        )
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = QianjiInspiredSpecs.Colors.Gray500,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            items()
        }
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.LIST
}
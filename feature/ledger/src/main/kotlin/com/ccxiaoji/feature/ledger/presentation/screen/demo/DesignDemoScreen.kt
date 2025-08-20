package com.ccxiaoji.feature.ledger.presentation.screen.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.CategoryDetails
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.minus
import java.time.format.DateTimeFormatter

enum class DesignScheme {
    CURRENT,        // 当前设计（对照组）
    BALANCED,       // 方案一：平衡增强设计
    PROGRESSIVE,    // 方案二：渐进式优化
    HIERARCHICAL    // 方案三：重新设计布局层次
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignDemoScreen(
    onNavigateBack: (() -> Unit)? = null
) {
    var selectedScheme by remember { mutableStateOf(DesignScheme.CURRENT) }
    
    // 模拟数据 - 不同日期的交易
    val mockTransactions = listOf(
        createMockTransaction("买菜", "🥬", "EXPENSE", 10.00, "收款方备注：二维码收款", daysAgo = 0),
        createMockTransaction("其他", "📝", "EXPENSE", 9599.00, "iPhone 16 Pro Max 全网通 5G 手机国行正品", daysAgo = 0),
        createMockTransaction("外出就餐", "🍔", "EXPENSE", 30.00, "英姐粥面店龙华店订单", daysAgo = 0),
        createMockTransaction("饮料", "🍔", "EXPENSE", 3.00, "收款方备注：二维码收款", daysAgo = 1),
        createMockTransaction("饮料", "🍔", "EXPENSE", 4.00, "美团收银909700193694057351", daysAgo = 1),
        createMockTransaction("工资", "💰", "INCOME", 3500.00, "公司发薪", daysAgo = 2),
        createMockTransaction("地铁", "🚇", "EXPENSE", 6.00, "地铁刷卡", daysAgo = 2)
    )
    
    val monthlyIncome = 3500.00
    val monthlyExpense = 22749.85
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设计方案对比Demo") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 方案选择器
            SchemeSelector(
                selectedScheme = selectedScheme,
                onSchemeSelected = { selectedScheme = it },
                modifier = Modifier.padding(16.dp)
            )
            
            HorizontalDivider()
            
            // 预览区域
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    // 方案说明
                    SchemeDescription(selectedScheme)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    // 总览卡片
                    when (selectedScheme) {
                        DesignScheme.CURRENT -> CurrentOverviewCard(monthlyIncome, monthlyExpense)
                        DesignScheme.BALANCED -> BalancedOverviewCard(monthlyIncome, monthlyExpense)
                        DesignScheme.PROGRESSIVE -> ProgressiveOverviewCard(monthlyIncome, monthlyExpense)
                        DesignScheme.HIERARCHICAL -> HierarchicalOverviewCard(monthlyIncome, monthlyExpense)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "交易记录",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 根据不同方案显示交易列表
                when (selectedScheme) {
                    DesignScheme.CURRENT, DesignScheme.PROGRESSIVE -> {
                        // 当前设计和方案二：简单列表显示
                        items(mockTransactions) { transaction ->
                            when (selectedScheme) {
                                DesignScheme.CURRENT -> CurrentTransactionItem(transaction)
                                DesignScheme.PROGRESSIVE -> ProgressiveTransactionItem(transaction)
                                else -> {}
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    DesignScheme.BALANCED -> {
                        // 方案一：日期分组显示
                        val groupedTransactions = groupTransactionsByDate(mockTransactions)
                        items(groupedTransactions.size) { index ->
                            val group = groupedTransactions[index]
                            
                            // 日期标题
                            BalancedDateHeader(group.date)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // 该日期下的所有交易
                            group.transactions.forEach { transaction ->
                                BalancedTransactionItem(transaction)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    DesignScheme.HIERARCHICAL -> {
                        // 方案三：日期分组显示
                        val groupedTransactions = groupTransactionsByDate(mockTransactions)
                        items(groupedTransactions.size) { index ->
                            val group = groupedTransactions[index]
                            
                            // 日期标题
                            HierarchicalDateHeader(group.date)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // 该日期下的所有交易
                            group.transactions.forEach { transaction ->
                                HierarchicalTransactionItem(transaction)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchemeSelector(
    selectedScheme: DesignScheme,
    onSchemeSelected: (DesignScheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "选择设计方案：",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val schemes = listOf(
                DesignScheme.CURRENT to "当前",
                DesignScheme.BALANCED to "方案一",
                DesignScheme.PROGRESSIVE to "方案二", 
                DesignScheme.HIERARCHICAL to "方案三"
            )
            
            schemes.forEach { (scheme, label) ->
                FilterChip(
                    onClick = { onSchemeSelected(scheme) },
                    label = { Text(label) },
                    selected = selectedScheme == scheme,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SchemeDescription(scheme: DesignScheme) {
    val (title, description) = when (scheme) {
        DesignScheme.CURRENT -> "当前设计" to "对照组 - 总览信息较小，交易列表较大"
        DesignScheme.BALANCED -> "平衡增强设计" to "增强总览卡片视觉权重，适度紧凑交易列表"
        DesignScheme.PROGRESSIVE -> "渐进式优化" to "先优化总览卡片，保持交易列表不变"
        DesignScheme.HIERARCHICAL -> "重新设计布局层次" to "引入卡片层次设计，突出重要信息"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 模拟数据创建函数
private fun createMockTransaction(
    categoryName: String,
    icon: String,
    type: String,
    amount: Double,
    note: String? = null,
    daysAgo: Int = 0
): Transaction {
    val now = Clock.System.now()
    val transactionTime = now.minus(DateTimePeriod(days = daysAgo), TimeZone.currentSystemDefault())
    
    return Transaction(
        id = "mock_${categoryName}_${amount}_${daysAgo}",
        accountId = "demo_account",
        categoryId = "demo_category",
        amountCents = (amount * 100).toInt(),
        note = note,
        ledgerId = "demo_ledger",
        createdAt = transactionTime,
        updatedAt = transactionTime,
        transactionDate = transactionTime,
        location = null,
        categoryDetails = CategoryDetails(
            id = "demo_category",
            name = categoryName,
            icon = icon,
            color = if (type == "INCOME") "#4CAF50" else "#F44336",
            type = type
        )
    )
}

// 按日期分组的数据结构
data class TransactionGroup(
    val date: kotlinx.datetime.LocalDate,
    val transactions: List<Transaction>
)

// 按日期分组交易数据
private fun groupTransactionsByDate(transactions: List<Transaction>): List<TransactionGroup> {
    return transactions
        .groupBy { 
            it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date 
        }
        .map { (date, transactions) ->
            TransactionGroup(date, transactions.sortedByDescending { it.createdAt })
        }
        .sortedByDescending { it.date }
}

// 日期标题组件 - 方案一风格
@Composable
private fun BalancedDateHeader(date: kotlinx.datetime.LocalDate) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dateText = when {
        date == now -> "今天"
        date == now.minus(DatePeriod(days = 1)) -> "昨天"
        date == now.minus(DatePeriod(days = 2)) -> "前天"
        else -> "${date.monthNumber}月${date.dayOfMonth}日"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        DesignTokens.BrandColors.Ledger,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = DesignTokens.BrandColors.Ledger
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = date.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// 日期标题组件 - 方案三风格
@Composable
private fun HierarchicalDateHeader(date: kotlinx.datetime.LocalDate) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dateText = when {
        date == now -> "今天"
        date == now.minus(DatePeriod(days = 1)) -> "昨天"
        date == now.minus(DatePeriod(days = 2)) -> "前天"
        else -> "${date.monthNumber}月${date.dayOfMonth}日"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
        )
    }
}

// 当前设计（对照组）
@Composable
private fun CurrentOverviewCard(monthlyIncome: Double, monthlyExpense: Double) {
    // 使用当前的简单设计
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "收入",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "¥${monthlyIncome}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
        
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "支出",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "¥${monthlyExpense}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "结余",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "¥${monthlyIncome - monthlyExpense}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CurrentTransactionItem(transaction: Transaction) {
    // 使用当前的ModernCard设计
    ModernCard(
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = DesignTokens.BrandColors.Error.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.categoryDetails?.icon ?: "📝",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                Column {
                    Text(
                        text = transaction.categoryDetails?.name ?: "未分类",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    transaction.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                            .toJavaLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            
            Text(
                text = "-¥${transaction.amountYuan}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = DesignTokens.BrandColors.Error
            )
        }
    }
}

// 方案一：平衡增强设计
@Composable
private fun BalancedOverviewCard(monthlyIncome: Double, monthlyExpense: Double) {
    val balance = monthlyIncome - monthlyExpense
    
    // 美化的渐变卡片设计
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f),
                            DesignTokens.BrandColors.Ledger.copy(alpha = 0.05f),
                            Color.White
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 顶部标题区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    DesignTokens.BrandColors.Ledger.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💰",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "本月财务概览",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignTokens.BrandColors.Ledger
                        )
                    }
                    
                    // 趋势图标
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (balance >= 0) DesignTokens.BrandColors.Success.copy(alpha = 0.2f)
                                else DesignTokens.BrandColors.Error.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (balance >= 0) "📈" else "📉",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 主要数据区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 收入卡片
                    FinanceItem(
                        label = "收入",
                        amount = monthlyIncome,
                        color = DesignTokens.BrandColors.Success,
                        icon = "💚",
                        isPositive = true
                    )
                    
                    // 支出卡片  
                    FinanceItem(
                        label = "支出",
                        amount = monthlyExpense,
                        color = DesignTokens.BrandColors.Error,
                        icon = "💸",
                        isPositive = false
                    )
                    
                    // 结余卡片
                    FinanceItem(
                        label = "结余",
                        amount = kotlin.math.abs(balance),
                        color = if (balance >= 0) DesignTokens.BrandColors.Ledger else DesignTokens.BrandColors.Error,
                        icon = if (balance >= 0) "💎" else "⚠️",
                        isPositive = balance >= 0
                    )
                }
            }
        }
    }
}

@Composable
private fun FinanceItem(
    label: String,
    amount: Double,
    color: Color,
    icon: String,
    isPositive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        // 图标区域
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color.copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 标签
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 金额
        Text(
            text = "${if (isPositive) "+" else "-"}¥${String.format("%.0f", amount)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun BalancedTransactionItem(transaction: Transaction) {
    // 紧凑的交易项设计
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 更小的图标
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = DesignTokens.BrandColors.Error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.categoryDetails?.icon ?: "📝",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Column {
                    Text(
                        text = transaction.categoryDetails?.name ?: "未分类",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // 只显示一行备注（如果有）
                    transaction.note?.let { note ->
                        Text(
                            text = if (note.length > 15) "${note.take(15)}..." else note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-¥${transaction.amountYuan}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Error
                )
                Text(
                    text = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                        .toJavaLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("MM-dd")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// 方案二：渐进式优化
@Composable
private fun ProgressiveOverviewCard(monthlyIncome: Double, monthlyExpense: Double) {
    // 只优化总览卡片，更突出显示
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "收入",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "¥${monthlyIncome}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.BrandColors.Success
                )
            }
            
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "支出",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "¥${monthlyExpense}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.BrandColors.Error
                )
            }
            
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "结余",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "¥${monthlyIncome - monthlyExpense}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if ((monthlyIncome - monthlyExpense) >= 0) 
                        DesignTokens.BrandColors.Ledger 
                    else 
                        DesignTokens.BrandColors.Error
                )
            }
        }
    }
}

@Composable
private fun ProgressiveTransactionItem(transaction: Transaction) {
    // 保持当前交易列表设计不变
    CurrentTransactionItem(transaction)
}

// 方案三：重新设计布局层次
@Composable
private fun HierarchicalOverviewCard(monthlyIncome: Double, monthlyExpense: Double) {
    // 层次化的卡片设计
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.BrandColors.Ledger
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "本月财务概览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥${monthlyIncome}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥${monthlyExpense}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "结余",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥${monthlyIncome - monthlyExpense}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun HierarchicalTransactionItem(transaction: Transaction) {
    // 极简化的交易项设计
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.categoryDetails?.icon ?: "📝",
                style = MaterialTheme.typography.titleSmall
            )
            
            Text(
                text = transaction.categoryDetails?.name ?: "未分类",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = "-¥${transaction.amountYuan}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = DesignTokens.BrandColors.Error
        )
    }
}
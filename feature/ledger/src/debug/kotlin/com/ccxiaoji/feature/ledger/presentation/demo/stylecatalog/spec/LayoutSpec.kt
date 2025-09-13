package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoTransaction
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoAccount
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoCategory
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoBudget
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import java.time.LocalDate

/**
 * 布局规格协议族 - 定义每种风格的布局结构差异
 * 而不仅仅是颜色和圆角的变化
 */

// ==================== 列表布局协议 ====================
interface ListSpec {
    // 列表项渲染
    @Composable
    fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    )
    
    // 分组策略
    fun getGroupingStrategy(): GroupingStrategy
    
    // 分组头渲染
    @Composable
    fun RenderGroupHeader(
        date: LocalDate,
        totalIncome: Double,
        totalExpense: Double,
        modifier: Modifier
    )
    
    // 列表容器装饰
    @Composable
    fun ListContainer(
        modifier: Modifier,
        content: @Composable () -> Unit
    )
    
    // 获取行高
    fun getItemHeight(density: DemoDensity): Dp
    
    // 获取项间距
    fun getItemSpacing(density: DemoDensity): Dp
}

// ==================== 单项布局协议 ====================
interface ItemSpec {
    // 布局类型
    enum class ItemLayout {
        SINGLE_LINE,      // 单行紧凑
        TWO_LINE,         // 双行标准
        CARD,             // 卡片式
        EXPANDABLE,       // 可展开式
        HIERARCHICAL      // 层次式
    }
    
    // 字段排列方式
    data class FieldArrangement(
        val categoryPosition: Position,
        val amountPosition: Position,
        val accountPosition: Position,
        val notePosition: Position,
        val dateTimePosition: Position
    )
    
    enum class Position {
        LEFT, CENTER, RIGHT, HIDDEN, BELOW, ABOVE
    }
    
    fun getLayout(): ItemLayout
    fun getFieldArrangement(): FieldArrangement
    fun showIcons(): Boolean
    fun showDividers(): Boolean
}

// ==================== 表头/概览协议 ====================
interface HeaderSpec {
    @Composable
    fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    )
    
    // 标题样式
    enum class TitleStyle {
        SMALL,          // 小标题
        MEDIUM,         // 中等标题
        LARGE,          // 大标题（iOS风格）
        INLINE,         // 内联标题
        FLOATING        // 浮动标题
    }
    
    fun getTitleStyle(): TitleStyle
    fun showDateSelector(): Boolean
    fun showQuickStats(): Boolean
}

// ==================== 筛选器布局协议 ====================
interface FilterSpec {
    @Composable
    fun RenderFilterBar(
        selectedDateRange: DateRange,
        selectedCategories: List<DemoCategory>,
        selectedAccounts: List<DemoAccount>,
        searchQuery: String,
        onDateRangeChange: (DateRange) -> Unit,
        onCategoryChange: (List<DemoCategory>) -> Unit,
        onAccountChange: (List<DemoAccount>) -> Unit,
        onSearchChange: (String) -> Unit,
        modifier: Modifier
    )
    
    // 筛选器样式
    enum class FilterStyle {
        CHIPS,          // 标签式（Material）
        SEGMENTS,       // 分段控件（iOS）
        DROPDOWN,       // 下拉式
        SIDEBAR,        // 侧边栏式（Discord）
        INLINE,         // 内联式（Notion）
        FLOATING        // 浮动式（玻璃拟态）
    }
    
    fun getFilterStyle(): FilterStyle
    fun isCollapsible(): Boolean
}

// ==================== 表单布局协议 ====================
interface FormSpec {
    @Composable
    fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    )
    
    // 表单布局
    enum class FormLayout {
        VERTICAL,       // 垂直排列
        GROUPED,        // 分组式
        WIZARD,         // 向导式
        INLINE,         // 内联编辑
        MODAL           // 模态式
    }
    
    fun getFormLayout(): FormLayout
    fun getFieldOrder(): List<String>
    fun showCalculator(): Boolean
}

// ==================== 对话框布局协议 ====================
interface DialogSpec {
    @Composable
    fun RenderDialog(
        title: String,
        content: @Composable () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier
    )
    
    // 对话框样式
    enum class DialogStyle {
        MATERIAL,       // Material标准
        IOS,            // iOS风格
        BOTTOM_SHEET,   // 底部弹出
        FULL_SCREEN,    // 全屏
        FLOATING,       // 浮动卡片
        INLINE          // 内联展开
    }
    
    fun getDialogStyle(): DialogStyle
    fun showOverlay(): Boolean
}

// ==================== 图表布局协议 ====================
interface ChartsSpec {
    @Composable
    fun RenderPieChart(
        data: List<ChartData>,
        modifier: Modifier
    )
    
    @Composable
    fun RenderLineChart(
        data: List<ChartData>,
        modifier: Modifier
    )
    
    @Composable
    fun RenderBarChart(
        data: List<ChartData>,
        modifier: Modifier
    )
    
    // 图表样式
    enum class ChartStyle {
        FLAT,           // 扁平
        GRADIENT,       // 渐变
        OUTLINED,       // 描边
        MINIMAL,        // 极简
        DETAILED        // 详细
    }
    
    fun getChartStyle(): ChartStyle
    fun showLegend(): Boolean
    fun showGrid(): Boolean
}

// ==================== 设置页布局协议 ====================
interface SettingsSpec {
    @Composable
    fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    )
    
    @Composable
    fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    )
    
    // 设置项样式
    enum class SettingsStyle {
        LIST,           // 列表式
        CARDS,          // 卡片式
        GROUPED,        // 分组式
        GRID            // 网格式
    }
    
    fun getSettingsStyle(): SettingsStyle
}

// ==================== 辅助数据类 ====================
data class DateRange(
    val start: LocalDate,
    val end: LocalDate,
    val label: String
)

data class ChartData(
    val label: String,
    val value: Double,
    val color: androidx.compose.ui.graphics.Color
)

enum class GroupingStrategy {
    BY_DAY,             // 按日分组
    BY_WEEK,            // 按周分组
    BY_MONTH,           // 按月分组
    BY_CATEGORY,        // 按分类分组
    NO_GROUPING         // 不分组
}
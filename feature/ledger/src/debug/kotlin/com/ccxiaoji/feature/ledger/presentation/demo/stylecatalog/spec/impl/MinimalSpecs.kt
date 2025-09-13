package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoTransaction
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoCategory
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoAccount
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoStyle
import java.time.LocalDate

/**
 * 最小化的Specs实现 - 用于确保编译通过
 */

class MinimalSpecs : SpecsRegistry.StyleSpecs() {
    override val listSpec = MinimalListSpec()
    override val itemSpec = MinimalItemSpec()
    override val headerSpec = MinimalHeaderSpec()
    override val filterSpec = MinimalFilterSpec()
    override val formSpec = MinimalFormSpec()
    override val dialogSpec = MinimalDialogSpec()
    override val chartsSpec = MinimalChartsSpec()
    override val settingsSpec = MinimalSettingsSpec()
    
    override val baseStyle = SpecsRegistry.StyleSpecs.BaseStyle.BALANCED
    override val description = "简化实现"
    override val recommendedDensity = DemoDensity.Medium
}

// 最小化实现类
class MinimalListSpec : ListSpec {
    @Composable
    override fun RenderListItem(
        transaction: DemoTransaction,
        modifier: Modifier,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        Text("Transaction: ${transaction.amount}")
    }
    
    override fun getGroupingStrategy() = GroupingStrategy.BY_DAY
    
    @Composable
    override fun RenderGroupHeader(
        date: LocalDate,
        totalIncome: Double,
        totalExpense: Double,
        modifier: Modifier
    ) {
        Text("Date: $date")
    }
    
    @Composable
    override fun ListContainer(modifier: Modifier, content: @Composable () -> Unit) {
        content()
    }
    
    override fun getItemHeight(density: DemoDensity) = 48.dp
    override fun getItemSpacing(density: DemoDensity) = 8.dp
}

class MinimalItemSpec : ItemSpec {
    override fun getLayout() = ItemSpec.ItemLayout.SINGLE_LINE
    override fun getFieldArrangement() = ItemSpec.FieldArrangement(
        categoryPosition = ItemSpec.Position.LEFT,
        amountPosition = ItemSpec.Position.RIGHT,
        accountPosition = ItemSpec.Position.HIDDEN,
        notePosition = ItemSpec.Position.HIDDEN,
        dateTimePosition = ItemSpec.Position.HIDDEN
    )
    override fun showIcons() = false
    override fun showDividers() = true
}

class MinimalHeaderSpec : HeaderSpec {
    @Composable
    override fun RenderOverviewCard(
        income: Double,
        expense: Double,
        balance: Double,
        modifier: Modifier
    ) {
        Text("Overview: Income: $income, Expense: $expense")
    }
    
    override fun getTitleStyle() = HeaderSpec.TitleStyle.MEDIUM
    override fun showDateSelector() = false
    override fun showQuickStats() = false
}

class MinimalFilterSpec : FilterSpec {
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
        Text("Filters")
    }
    
    override fun getFilterStyle() = FilterSpec.FilterStyle.CHIPS
    override fun isCollapsible() = false
}

class MinimalFormSpec : FormSpec {
    @Composable
    override fun RenderTransactionForm(
        transaction: DemoTransaction?,
        onSave: (DemoTransaction) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier
    ) {
        Text("Form")
    }
    
    override fun getFormLayout() = FormSpec.FormLayout.VERTICAL
    override fun getFieldOrder() = listOf("amount", "category")
    override fun showCalculator() = false
}

class MinimalDialogSpec : DialogSpec {
    @Composable
    override fun RenderDialog(
        title: String,
        content: @Composable () -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier
    ) {
        Text("Dialog: $title")
    }
    
    override fun getDialogStyle() = DialogSpec.DialogStyle.MATERIAL
    override fun showOverlay() = true
}

class MinimalChartsSpec : ChartsSpec {
    @Composable
    override fun RenderPieChart(data: List<ChartData>, modifier: Modifier) {
        Text("Pie Chart")
    }
    
    @Composable
    override fun RenderLineChart(data: List<ChartData>, modifier: Modifier) {
        Text("Line Chart")
    }
    
    @Composable
    override fun RenderBarChart(data: List<ChartData>, modifier: Modifier) {
        Text("Bar Chart")
    }
    
    override fun getChartStyle() = ChartsSpec.ChartStyle.FLAT
    override fun showLegend() = false
    override fun showGrid() = false
}

class MinimalSettingsSpec : SettingsSpec {
    @Composable
    override fun RenderSettingsItem(
        title: String,
        subtitle: String?,
        icon: @Composable (() -> Unit)?,
        trailing: @Composable (() -> Unit)?,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        Text("Setting: $title")
    }
    
    @Composable
    override fun RenderSettingsGroup(
        title: String,
        items: @Composable () -> Unit,
        modifier: Modifier
    ) {
        Text("Group: $title")
        items()
    }
    
    override fun getSettingsStyle() = SettingsSpec.SettingsStyle.LIST
}
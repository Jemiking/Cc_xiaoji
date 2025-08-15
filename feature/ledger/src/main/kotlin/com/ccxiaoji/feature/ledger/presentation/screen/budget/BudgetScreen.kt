package com.ccxiaoji.feature.ledger.presentation.screen.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.presentation.viewmodel.BudgetViewModel
import com.ccxiaoji.feature.ledger.presentation.screen.budget.components.MonthSelector
import com.ccxiaoji.feature.ledger.presentation.screen.budget.components.EmptyBudgetState
import com.ccxiaoji.feature.ledger.presentation.screen.budget.components.TotalBudgetCard
import com.ccxiaoji.feature.ledger.presentation.screen.budget.components.EmptyTotalBudgetCard
import com.ccxiaoji.feature.ledger.presentation.screen.budget.components.CategoryBudgetCard
import com.ccxiaoji.feature.ledger.presentation.screen.budget.components.AddCategoryBudgetCard
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatFAB
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddEditBudget: (categoryId: String?) -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.CHINA) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "预算管理",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FlatFAB(
                onClick = { onNavigateToAddEditBudget(null) },
                containerColor = DesignTokens.BrandColors.Ledger
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "添加预算",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    ) { paddingValues ->
        if (uiState.totalBudget == null && uiState.budgets.isEmpty()) {
            // 空状态
            EmptyBudgetState(
                onAddBudget = { onNavigateToAddEditBudget(null) }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 月份选择器
                MonthSelector(
                    selectedYear = uiState.selectedYear,
                    selectedMonth = uiState.selectedMonth,
                    onMonthChange = viewModel::changeMonth
                )
                
                // 预算内容
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 总预算卡片
                    uiState.totalBudget?.let { totalBudget ->
                        item {
                            TotalBudgetCard(
                                budget = totalBudget,
                                currencyFormat = currencyFormat,
                                onClick = { onNavigateToAddEditBudget(null) }
                            )
                        }
                    } ?: item {
                        EmptyTotalBudgetCard(
                            onClick = { onNavigateToAddEditBudget(null) }
                        )
                    }
                    
                    // 分类预算标题
                    if (uiState.budgets.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = DesignTokens.Spacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "分类预算",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = DesignTokens.BrandColors.Ledger
                                )
                                Text(
                                    text = "${uiState.budgets.size} 项",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    
                    // 分类预算列表
                    items(uiState.budgets) { budget ->
                        val category = uiState.categories.find { it.id == budget.categoryId }
                        category?.let {
                            CategoryBudgetCard(
                                budget = budget,
                                category = it,
                                currencyFormat = currencyFormat,
                                onClick = { onNavigateToAddEditBudget(budget.categoryId) },
                                onDelete = { viewModel.deleteBudget(budget.id) }
                            )
                        }
                    }
                    
                    // 添加分类预算按钮
                    item {
                        AddCategoryBudgetCard(
                            onClick = { onNavigateToAddEditBudget(null) }
                        )
                    }
                    
                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
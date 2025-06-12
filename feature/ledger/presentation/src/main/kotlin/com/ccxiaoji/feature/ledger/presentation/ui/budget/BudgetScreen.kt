package com.ccxiaoji.feature.ledger.presentation.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.api.BudgetItem
import com.ccxiaoji.feature.ledger.api.CategoryItem
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.presentation.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.util.*

/**
 * 预算管理主界面
 * 显示总预算和分类预算，支持添加、编辑、删除预算
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navigator: LedgerNavigator,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.CHINA) }
    
    // 显示错误信息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // 在实际应用中，可以使用SnackBar显示错误
            println("预算管理错误: $error")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预算管理") },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddEditDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "添加预算")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 月份选择器
                MonthSelector(
                    selectedYear = uiState.selectedYear,
                    selectedMonth = uiState.selectedMonth,
                    onMonthChange = viewModel::selectYearMonth
                )
                
                // 预算内容
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 总预算卡片
                    uiState.totalBudget?.let { totalBudget ->
                        item {
                            TotalBudgetCard(
                                budget = totalBudget,
                                currencyFormat = currencyFormat,
                                onClick = { viewModel.showAddEditDialog(totalBudget) }
                            )
                        }
                    } ?: item {
                        EmptyTotalBudgetCard(
                            onClick = { viewModel.showAddEditDialog() }
                        )
                    }
                    
                    // 分类预算标题
                    if (uiState.budgets.isNotEmpty()) {
                        item {
                            Text(
                                text = "分类预算",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    // 分类预算列表
                    items(uiState.budgets.filter { it.categoryId != null }) { budget ->
                        CategoryBudgetCard(
                            budget = budget,
                            currencyFormat = currencyFormat,
                            onClick = { viewModel.showAddEditDialog(budget) },
                            onDelete = { viewModel.deleteBudget(budget.id) }
                        )
                    }
                    
                    // 添加分类预算按钮
                    item {
                        AddCategoryBudgetCard(
                            onClick = { viewModel.showAddEditDialog() }
                        )
                    }
                }
            }
            
            // 加载指示器
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // 添加/编辑预算对话框
    if (uiState.showAddEditDialog) {
        AddEditBudgetDialog(
            editingBudget = uiState.editingBudget,
            categories = uiState.categories,
            onDismiss = viewModel::hideAddEditDialog,
            onSave = viewModel::saveBudget
        )
    }
}

@Composable
private fun MonthSelector(
    selectedYear: Int,
    selectedMonth: Int,
    onMonthChange: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (selectedMonth == 1) {
                        onMonthChange(selectedYear - 1, 12)
                    } else {
                        onMonthChange(selectedYear, selectedMonth - 1)
                    }
                }
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上一月")
            }
            
            Text(
                text = "${selectedYear}年${selectedMonth}月",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = {
                    if (selectedMonth == 12) {
                        onMonthChange(selectedYear + 1, 1)
                    } else {
                        onMonthChange(selectedYear, selectedMonth + 1)
                    }
                }
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下一月")
            }
        }
    }
}

@Composable
private fun TotalBudgetCard(
    budget: BudgetItem,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    val usagePercentage = budget.usagePercentage / 100f
    val isExceeded = budget.isExceeded
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isExceeded) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "总预算",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currencyFormat.format(budget.budgetAmountYuan),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = { usagePercentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isExceeded) {
                    MaterialTheme.colorScheme.error
                } else if (budget.isAlert) {
                    Color(0xFFFFA500) // Orange
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "已支出: ${currencyFormat.format(budget.spentAmountYuan)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${budget.usagePercentage.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            val note = budget.note
            if (note?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyTotalBudgetCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "设置总预算",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    budget: BudgetItem,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val usagePercentage = budget.usagePercentage / 100f
    val isExceeded = budget.isExceeded
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        budget.categoryColor?.let { 
                            Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.2f) 
                        } ?: MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = budget.categoryIcon ?: "📁",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 预算信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = budget.categoryName ?: "未知分类",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currencyFormat.format(budget.budgetAmountYuan),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 进度条
                LinearProgressIndicator(
                    progress = { usagePercentage.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isExceeded) {
                        MaterialTheme.colorScheme.error
                    } else if (budget.isAlert) {
                        Color(0xFFFFA500) // Orange
                    } else {
                        budget.categoryColor?.let { 
                            Color(android.graphics.Color.parseColor(it)) 
                        } ?: MaterialTheme.colorScheme.primary
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已支出: ${currencyFormat.format(budget.spentAmountYuan)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${budget.usagePercentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isExceeded) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // 删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddCategoryBudgetCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加分类预算",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
package com.ccxiaoji.feature.ledger.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.YearMonth

@Composable
fun LedgerScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    
    // 添加调试日志
    LaunchedEffect(selectedMonth) {
        android.util.Log.e("LEDGER_DEBUG", "========== LedgerScreen ==========")
        android.util.Log.e("LEDGER_DEBUG", "当前查看月份: $selectedMonth")
        android.util.Log.e("LEDGER_DEBUG", "交易记录数量: ${uiState.transactions.size}")
        android.util.Log.e("LEDGER_DEBUG", "月收入: ${uiState.monthlyIncome}")
        android.util.Log.e("LEDGER_DEBUG", "月支出: ${uiState.monthlyExpense}")
        
        if (uiState.transactions.isNotEmpty()) {
            val first = uiState.transactions.first()
            val instant = first.createdAt
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            android.util.Log.e("LEDGER_DEBUG", "第一条记录日期: ${localDateTime.date}")
            android.util.Log.e("LEDGER_DEBUG", "第一条记录金额: ${first.amountCents}")
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 月份选择器
        MonthSelector(
            selectedMonth = selectedMonth,
            onMonthChange = viewModel::selectMonth
        )
        
        // 月度统计
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("收入", style = MaterialTheme.typography.labelMedium)
                    Text("¥${uiState.monthlyIncome}", style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("支出", style = MaterialTheme.typography.labelMedium)
                    Text("¥${uiState.monthlyExpense}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        // 交易列表
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "本月暂无交易记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // 这里应该添加交易列表的UI
            Text("共 ${uiState.transactions.size} 条记录", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun MonthSelector(
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
            Text("上月")
        }
        
        Text(
            text = "${selectedMonth.year}年${selectedMonth.monthValue}月",
            style = MaterialTheme.typography.titleMedium
        )
        
        TextButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
            Text("下月")
        }
    }
}
package com.ccxiaoji.feature.ledger.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.YearMonth
import java.util.Date
import kotlinx.datetime.*

@HiltViewModel
class LedgerDatabaseDebugViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val userApi: UserApi
) : ViewModel() {
    
    data class DebugState(
        val totalCount: Int = 0,
        val userCount: Int = 0,
        val monthCount: Int = 0,
        val queryResult: String = "",
        val isLoading: Boolean = false
    )
    
    var state by mutableStateOf(DebugState())
        private set
    
    fun runDebugQuery(yearMonth: YearMonth) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            
            val userId = userApi.getCurrentUserId()
            val year = yearMonth.year
            val month = yearMonth.monthValue
            
            // 计算日期范围（使用不同的方法）
            val startDate = LocalDate(year, month, 1)
            val endDate = if (month == 12) {
                LocalDate(year + 1, 1, 1)
            } else {
                LocalDate(year, month + 1, 1)
            }
            
            val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            
            android.util.Log.e("DATABASE_DEBUG", "========== 数据库直接查询 ==========")
            android.util.Log.e("DATABASE_DEBUG", "查询月份: $year-$month")
            android.util.Log.e("DATABASE_DEBUG", "用户ID: $userId")
            android.util.Log.e("DATABASE_DEBUG", "开始时间: $startMillis (${Date(startMillis)})")
            android.util.Log.e("DATABASE_DEBUG", "结束时间: $endMillis (${Date(endMillis)})")
            
            // 1. 查询总数
            val totalCount = transactionDao.getAllTransactionsCount()
            android.util.Log.e("DATABASE_DEBUG", "数据库总交易数: $totalCount")
            
            // 2. 查询用户交易数
            val userCount = transactionDao.getUserTransactionsCount(userId)
            android.util.Log.e("DATABASE_DEBUG", "用户交易总数: $userCount")
            
            // 3. 使用原始SQL查询该月份的交易
            val monthTransactions = transactionDao.getTransactionsPaginatedData(
                userId = userId,
                offset = 0,
                limit = 100,
                accountId = null,
                startDateMillis = startMillis,
                endDateMillis = endMillis
            )
            android.util.Log.e("DATABASE_DEBUG", "月份交易数: ${monthTransactions.size}")
            
            // 4. 尝试不同的查询条件
            val monthTransactions2 = transactionDao.getTransactionsByDateRangeSync(
                userId = userId,
                startTime = startMillis,
                endTime = endMillis
            )
            android.util.Log.e("DATABASE_DEBUG", "月份交易数(方法2): ${monthTransactions2.size}")
            
            // 5. 获取最近的交易
            val recentTransactions = transactionDao.getLatestTransactions(10)
            android.util.Log.e("DATABASE_DEBUG", "最近10条交易:")
            recentTransactions.forEach { trans ->
                val date = Date(trans.createdAt)
                android.util.Log.e("DATABASE_DEBUG", "  - ${date}: ${trans.amountCents}分, 用户=${trans.userId}")
            }
            
            // 6. 获取该用户最早和最新的交易时间
            val userTransactions = transactionDao.getTransactionsByUserSync(userId)
            if (userTransactions.isNotEmpty()) {
                val earliest = userTransactions.minByOrNull { it.createdAt }
                val latest = userTransactions.maxByOrNull { it.createdAt }
                
                android.util.Log.e("DATABASE_DEBUG", "用户交易时间范围:")
                android.util.Log.e("DATABASE_DEBUG", "  最早: ${earliest?.let { Date(it.createdAt) }}")
                android.util.Log.e("DATABASE_DEBUG", "  最新: ${latest?.let { Date(it.createdAt) }}")
            }
            
            val queryResult = """
                查询月份: $year-$month
                用户ID: $userId
                
                时间范围:
                开始: ${Date(startMillis)}
                结束: ${Date(endMillis)}
                
                查询结果:
                数据库总数: $totalCount
                用户总数: $userCount
                本月交易(方法1): ${monthTransactions.size}
                本月交易(方法2): ${monthTransactions2.size}
                
                最近交易:
                ${recentTransactions.take(5).joinToString("\n") { 
                    "${Date(it.createdAt)}: ${it.amountCents}分"
                }}
            """.trimIndent()
            
            state = state.copy(
                totalCount = totalCount,
                userCount = userCount,
                monthCount = monthTransactions.size,
                queryResult = queryResult,
                isLoading = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDatabaseDebugScreen(
    onBack: () -> Unit,
    viewModel: LedgerDatabaseDebugViewModel = hiltViewModel()
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.of(2024, 10)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据库调试") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 月份选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { 
                    selectedMonth = selectedMonth.minusMonths(1)
                }) {
                    Text("上个月")
                }
                
                Text(
                    text = "${selectedMonth.year}年${selectedMonth.monthValue}月",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Button(onClick = { 
                    selectedMonth = selectedMonth.plusMonths(1)
                }) {
                    Text("下个月")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 查询按钮
            Button(
                onClick = { viewModel.runDebugQuery(selectedMonth) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.state.isLoading
            ) {
                if (viewModel.state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("运行调试查询")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 显示结果
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "查询结果",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.state.queryResult.ifEmpty { "点击按钮运行查询" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
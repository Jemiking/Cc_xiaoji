package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@HiltViewModel
class LedgerDebugViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) : ViewModel() {
    
    data class DebugInfo(
        val totalTransactions: Int = 0,
        val userTransactions: Int = 0,
        val latestTransactions: List<String> = emptyList(),
        val monthlyStats: Map<String, Int> = emptyMap(),
        val accounts: List<String> = emptyList(),
        val categories: List<String> = emptyList()
    )
    
    private val _debugInfo = MutableStateFlow(DebugInfo())
    val debugInfo: StateFlow<DebugInfo> = _debugInfo
    
    fun loadDebugInfo(userId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.e("DEBUG_DB", "========== 数据库调试信息 ==========")
                
                // 1. 交易总数
                val total = transactionDao.getAllTransactionsCount()
                android.util.Log.e("DEBUG_DB", "数据库总交易数: $total")
                
                // 2. 用户交易数
                val userTrans = transactionDao.getTransactionsByUserSync(userId).size
                android.util.Log.e("DEBUG_DB", "用户 $userId 的交易数: $userTrans")
                
                // 3. 最新的5条交易
                val latest = transactionDao.getLatestTransactions(5)
                val latestInfo = latest.map { trans ->
                    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(trans.createdAt)
                    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    "日期: ${localDateTime.date}, 金额: ${trans.amountCents / 100.0}元"
                }
                android.util.Log.e("DEBUG_DB", "最新交易: ${latestInfo.joinToString("\n")}")
                
                // 4. 按月统计
                val allTrans = transactionDao.getTransactionsByUserSync(userId)
                val monthlyStats = allTrans.groupBy { trans ->
                    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(trans.createdAt)
                    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    "${localDateTime.year}-${localDateTime.monthNumber}"
                }.mapValues { it.value.size }
                android.util.Log.e("DEBUG_DB", "按月统计: $monthlyStats")
                
                // 5. 账户信息
                val accounts = accountDao.getAccountsByUserSync(userId).map { account ->
                    "${account.name}: ${account.balanceCents / 100.0}元"
                }
                android.util.Log.e("DEBUG_DB", "账户: ${accounts.joinToString(", ")}")
                
                // 6. 分类信息
                val categories = categoryDao.getCategoriesByUserSync(userId).map { category ->
                    category.name
                }
                android.util.Log.e("DEBUG_DB", "分类: ${categories.joinToString(", ")}")
                
                _debugInfo.value = DebugInfo(
                    totalTransactions = total,
                    userTransactions = userTrans,
                    latestTransactions = latestInfo,
                    monthlyStats = monthlyStats,
                    accounts = accounts,
                    categories = categories
                )
                
            } catch (e: Exception) {
                android.util.Log.e("DEBUG_DB", "加载调试信息失败", e)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDebugScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: LedgerDebugViewModel = hiltViewModel()
) {
    val debugInfo by viewModel.debugInfo.collectAsStateWithLifecycle()
    
    LaunchedEffect(userId) {
        viewModel.loadDebugInfo(userId)
    }
    
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("交易统计", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("总交易数: ${debugInfo.totalTransactions}")
                        Text("用户交易数: ${debugInfo.userTransactions}")
                    }
                }
            }
            
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("最新交易", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        debugInfo.latestTransactions.forEach { transInfo ->
                            Text(transInfo)
                        }
                    }
                }
            }
            
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("按月统计", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        debugInfo.monthlyStats.forEach { entry ->
                            Text("${entry.key}: ${entry.value} 条")
                        }
                    }
                }
            }
            
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("账户列表", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        debugInfo.accounts.forEach { accountInfo ->
                            Text(accountInfo)
                        }
                    }
                }
            }
            
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("分类列表", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(debugInfo.categories.joinToString(", "))
                    }
                }
            }
        }
    }
}
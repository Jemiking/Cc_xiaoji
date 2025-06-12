package com.ccxiaoji.feature.ledger.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    navigator: LedgerNavigator,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 从交易列表中查找当前交易
    val transaction = remember(uiState.transactions, transactionId) {
        uiState.transactions.find { it.id == transactionId }
    }
    
    LaunchedEffect(transactionId) {
        viewModel.loadTransactionDetail(transactionId)
    }
    
    if (transaction == null && !uiState.isLoading) {
        // 交易未找到，返回上一页
        LaunchedEffect(Unit) {
            navigator.navigateUp()
        }
        return
    }
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易详情") },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (transaction != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (transaction != null) {
            TransactionDetailContent(
                transaction = transaction,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    
    // 编辑对话框
    val editingDetail = uiState.editingTransactionDetail
    if (showEditDialog && editingDetail != null) {
        EditTransactionDialog(
            transactionDetail = editingDetail,
            categories = uiState.categories,
            onDismiss = { 
                showEditDialog = false
                viewModel.setEditingTransaction(null)
            },
            onConfirm = { amountCents, categoryId, note ->
                viewModel.updateTransaction(
                    transactionId = transaction!!.id,
                    amountCents = amountCents,
                    categoryId = categoryId,
                    note = note
                )
                showEditDialog = false
                viewModel.setEditingTransaction(null)
            }
        )
    }
    
    // 当点击编辑时，加载交易详情
    LaunchedEffect(showEditDialog) {
        if (showEditDialog && transaction != null) {
            viewModel.setEditingTransaction(transaction.id)
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog && transaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这笔交易记录吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction.id)
                        showDeleteDialog = false
                        navigator.navigateUp()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: com.ccxiaoji.feature.ledger.api.TransactionItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 交易图标和金额卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = transaction.categoryIcon ?: "📝",
                    style = MaterialTheme.typography.displayLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val isIncome = transaction.amount > 0
                Text(
                    text = if (isIncome) {
                        "+¥%.2f".format(transaction.amount)
                    } else {
                        "-¥%.2f".format(-transaction.amount)
                    },
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                
                Text(
                    text = transaction.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // 交易详情卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 日期和时间
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "日期时间",
                    value = transaction.date
                        .toJavaLocalDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
                )
                
                HorizontalDivider()
                
                // 账户
                DetailRow(
                    icon = Icons.Default.AccountBalance,
                    label = "账户",
                    value = transaction.accountName
                )
                
                HorizontalDivider()
                
                // 备注
                DetailRow(
                    icon = Icons.AutoMirrored.Filled.Note,
                    label = "备注",
                    value = transaction.note ?: "无备注"
                )
                
                HorizontalDivider()
                
                // 交易ID
                DetailRow(
                    icon = Icons.Default.Tag,
                    label = "交易ID",
                    value = transaction.id.take(8) + "..."
                )
            }
        }
        
        // 未来功能占位符
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "即将推出",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• 交易位置信息\n• 附件和图片\n• 交易标签\n• 关联定期交易",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
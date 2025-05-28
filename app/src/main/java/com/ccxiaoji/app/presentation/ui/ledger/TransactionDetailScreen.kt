package com.ccxiaoji.app.presentation.ui.ledger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.app.domain.model.Transaction
import com.ccxiaoji.app.domain.model.TransactionCategory
import com.ccxiaoji.app.presentation.viewmodel.LedgerViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    navController: NavController,
    viewModel: LedgerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val transaction = uiState.transactions.find { it.id == transactionId }
    
    if (transaction == null) {
        // Transaction not found, navigate back
        LaunchedEffect(Unit) {
            navController.popBackStack()
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Icon and Amount
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
                        text = transaction.categoryDetails?.icon ?: transaction.category?.icon ?: "📝",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (transaction.category == TransactionCategory.INCOME) {
                            "+¥%.2f".format(transaction.amountYuan)
                        } else {
                            "-¥%.2f".format(transaction.amountYuan)
                        },
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.category == TransactionCategory.INCOME) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Text(
                        text = transaction.categoryDetails?.name ?: transaction.category?.displayName ?: "其他",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Transaction Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date and Time
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "日期时间",
                        value = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                            .toJavaLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))
                    )
                    
                    HorizontalDivider()
                    
                    // Note
                    DetailRow(
                        icon = Icons.Default.Note,
                        label = "备注",
                        value = transaction.note ?: "无备注"
                    )
                    
                    HorizontalDivider()
                    
                    // Transaction ID
                    DetailRow(
                        icon = Icons.Default.Tag,
                        label = "交易ID",
                        value = transaction.id.take(8) + "..."
                    )
                    
                    HorizontalDivider()
                    
                    // Last Update
                    DetailRow(
                        icon = Icons.Default.Update,
                        label = "最后更新",
                        value = transaction.updatedAt.toLocalDateTime(TimeZone.currentSystemDefault())
                            .toJavaLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    )
                }
            }
            
            // Future features placeholder
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
                        text = "• 交易位置信息\n• 附件和图片\n• 交易标签\n• 关联账户信息",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Edit Dialog
    if (showEditDialog) {
        EditTransactionDialog(
            transaction = transaction,
            categories = uiState.categories,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedTransaction ->
                viewModel.updateTransaction(updatedTransaction)
                showEditDialog = false
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这笔交易记录吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction.id)
                        showDeleteDialog = false
                        navController.popBackStack()
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
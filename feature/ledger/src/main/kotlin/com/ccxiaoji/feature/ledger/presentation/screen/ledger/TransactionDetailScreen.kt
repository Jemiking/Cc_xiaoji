package com.ccxiaoji.feature.ledger.presentation.screen.ledger

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
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
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
                title = { Text(stringResource(R.string.transaction_detail)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
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
                        text = transaction.categoryDetails?.icon ?: "📝",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (transaction.categoryDetails?.type == "INCOME") {
                            stringResource(R.string.amount_format_positive, stringResource(R.string.currency_symbol), transaction.amountYuan)
                        } else {
                            stringResource(R.string.amount_format_negative, stringResource(R.string.currency_symbol), transaction.amountYuan)
                        },
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.categoryDetails?.type == "INCOME") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Text(
                        text = transaction.categoryDetails?.name ?: stringResource(R.string.transaction_detail_other),
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
                        label = stringResource(R.string.transaction_detail_date_time),
                        value = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                            .toJavaLocalDateTime()
                            .format(DateTimeFormatter.ofPattern(stringResource(R.string.transaction_detail_date_format)))
                    )
                    
                    HorizontalDivider()
                    
                    // Note
                    DetailRow(
                        icon = Icons.Default.Note,
                        label = stringResource(R.string.transaction_detail_note),
                        value = transaction.note ?: stringResource(R.string.transaction_detail_no_note)
                    )
                    
                    HorizontalDivider()
                    
                    // Transaction ID
                    DetailRow(
                        icon = Icons.Default.Tag,
                        label = stringResource(R.string.transaction_detail_id),
                        value = transaction.id.take(8) + "..."
                    )
                    
                    HorizontalDivider()
                    
                    // Last Update
                    DetailRow(
                        icon = Icons.Default.Update,
                        label = stringResource(R.string.transaction_detail_last_update),
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
                        text = stringResource(R.string.transaction_detail_coming_soon),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.transaction_detail_coming_features),
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
            title = { Text(stringResource(R.string.transaction_detail_confirm_delete)) },
            text = { Text(stringResource(R.string.transaction_detail_delete_message)) },
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
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
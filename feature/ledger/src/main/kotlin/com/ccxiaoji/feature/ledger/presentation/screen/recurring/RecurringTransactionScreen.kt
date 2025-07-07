package com.ccxiaoji.feature.ledger.presentation.screen.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatFAB
import com.ccxiaoji.feature.ledger.presentation.screen.recurring.components.EmptyRecurringState
import com.ccxiaoji.feature.ledger.presentation.screen.recurring.components.RecurringTransactionItem
import com.ccxiaoji.feature.ledger.presentation.viewmodel.RecurringTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddEdit: (String?) -> Unit,
    viewModel: RecurringTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recurringTransactions by viewModel.recurringTransactions.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("定期交易") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "返回"
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
                onClick = { onNavigateToAddEdit(null) },
                containerColor = DesignTokens.BrandColors.Ledger
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "添加定期交易",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    ) { paddingValues ->
        if (recurringTransactions.isEmpty()) {
            EmptyRecurringState(
                onAddRecurring = { viewModel.showAddDialog() }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                items(recurringTransactions) { transaction ->
                    RecurringTransactionItem(
                        transaction = transaction,
                        onToggleEnabled = { viewModel.toggleEnabled(transaction) },
                        onEdit = { onNavigateToAddEdit(transaction.id) },
                        onDelete = { viewModel.deleteRecurringTransaction(transaction) },
                        formatNextExecutionDate = viewModel::formatNextExecutionDate,
                        getFrequencyText = viewModel::getFrequencyText
                    )
                }
            }
        }
    }
}
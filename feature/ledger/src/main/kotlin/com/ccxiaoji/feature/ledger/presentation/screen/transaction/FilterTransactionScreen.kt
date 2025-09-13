package com.ccxiaoji.feature.ledger.presentation.screen.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.component.DynamicCategoryIcon
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import com.ccxiaoji.feature.ledger.presentation.viewmodel.FilterTransactionViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUIStyleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTransactionScreen(
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: FilterTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // 鑾峰彇鍥炬爣鏄剧ず妯″紡
    val uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
    val uiPreferences by uiStyleViewModel.uiPreferences.collectAsStateWithLifecycle()
    
    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.startDate?.let { date ->
            date.atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.endDate?.let { date ->
            date.atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    )
// 系统返回：优先回调（回到记账首页）
BackHandler { onNavigateBack?.invoke() ?: navController.popBackStack() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.filter_transactions)) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.clearFilter()
                                onNavigateBack?.invoke() ?: navController.popBackStack()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.clear_filter))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onNavigateBack?.invoke() ?: navController.popBackStack() }) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.applyFilter()
                                onNavigateBack?.invoke() ?: navController.popBackStack()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.apply))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Transaction Type Filter
            Column {
                Text(
                    text = stringResource(R.string.transaction_type),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.transactionType == TransactionType.ALL,
                        onClick = { viewModel.updateTransactionType(TransactionType.ALL) },
                        label = { Text(stringResource(R.string.all)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.transactionType == TransactionType.INCOME,
                        onClick = { viewModel.updateTransactionType(TransactionType.INCOME) },
                        label = { Text(stringResource(R.string.income)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.transactionType == TransactionType.EXPENSE,
                        onClick = { viewModel.updateTransactionType(TransactionType.EXPENSE) },
                        label = { Text(stringResource(R.string.expense)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Category Filter
            Column {
                Text(
                    text = stringResource(R.string.category),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(uiState.filteredCategories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategoryIds.contains(category.id),
                            onClick = { viewModel.toggleCategory(category.id) },
                            label = { 
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DynamicCategoryIcon(
                                        category = category,
                                        iconDisplayMode = uiPreferences.iconDisplayMode,
                                        size = 16.dp,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        category.name, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Amount Range Filter
            Column {
                Text(
                    text = stringResource(R.string.amount_range),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.minAmount,
                        onValueChange = viewModel::updateMinAmount,
                        label = { Text(stringResource(R.string.min_amount)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.maxAmount,
                        onValueChange = viewModel::updateMaxAmount,
                        label = { Text(stringResource(R.string.max_amount)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Date Range Filter
            Column {
                Text(
                    text = stringResource(R.string.date_range),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quick date range selections
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setToday() },
                        label = { Text(stringResource(R.string.today)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setThisWeek() },
                        label = { Text(stringResource(R.string.this_week)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setThisMonth() },
                        label = { Text(stringResource(R.string.this_month)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setThisYear() },
                        label = { Text(stringResource(R.string.this_year)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Custom date range selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.startDate?.toString() ?: "",
                        onValueChange = { },
                        label = { Text(stringResource(R.string.start_date)) },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showStartDatePicker = true }
                    )
                    OutlinedTextField(
                        value = uiState.endDate?.toString() ?: "",
                        onValueChange = { },
                        label = { Text(stringResource(R.string.end_date)) },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEndDatePicker = true }
                    )
                }
            }
        }
    }
    
    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { millis ->
                millis?.let {
                    val date = java.time.Instant.ofEpochMilli(it)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    viewModel.updateStartDate(date)
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { millis ->
                millis?.let {
                    val date = java.time.Instant.ofEpochMilli(it)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    viewModel.updateEndDate(date)
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}



package com.ccxiaoji.feature.ledger.presentation.screen.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AccountViewModel
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController? = null,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.account_management_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (uiState.accounts.size >= 2) {
                        IconButton(onClick = { showTransferDialog = true }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = stringResource(R.string.account_transfer))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_account))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Total Balance Card
            TotalBalanceCard(
                totalBalance = uiState.totalBalance,
                modifier = Modifier.padding(16.dp)
            )
            
            // Accounts List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.accounts) { account ->
                    AccountItem(
                        account = account,
                        onEdit = {
                            selectedAccount = account
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedAccount = account
                            showDeleteDialog = true
                        },
                        onSetDefault = {
                            viewModel.setDefaultAccount(account.id)
                        },
                        onClick = {
                            // Navigate to account detail or transaction list
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // For FAB
                }
            }
        }
    }
    
    // Add Account Dialog
    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, initialBalance ->
                viewModel.createAccount(name, type, initialBalance)
                showAddDialog = false
            }
        )
    }
    
    // Edit Account Dialog
    if (showEditDialog && selectedAccount != null) {
        EditAccountDialog(
            account = selectedAccount!!,
            onDismiss = { 
                showEditDialog = false
                selectedAccount = null
            },
            onConfirm = { updatedAccount ->
                viewModel.updateAccount(updatedAccount)
                showEditDialog = false
                selectedAccount = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedAccount != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                selectedAccount = null
            },
            title = { Text(stringResource(R.string.account_delete_title)) },
            text = { 
                Text(stringResource(R.string.account_delete_message, selectedAccount!!.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(selectedAccount!!.id)
                        showDeleteDialog = false
                        selectedAccount = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedAccount = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Transfer Dialog
    if (showTransferDialog) {
        AccountTransferDialog(
            accounts = uiState.accounts,
            onDismiss = { showTransferDialog = false },
            onConfirm = { fromId, toId, amountCents ->
                viewModel.transferBetweenAccounts(fromId, toId, amountCents)
                showTransferDialog = false
            }
        )
    }
}

@Composable
fun TotalBalanceCard(
    totalBalance: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                text = stringResource(R.string.account_total_assets),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.amount_format, stringResource(R.string.currency_symbol), totalBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Account Icon
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = account.color?.let { Color(it.toInt()) } 
                            ?: MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = account.icon ?: account.type.icon,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (account.isDefault) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = stringResource(R.string.account_default_label),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        Text(
                            text = account.type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = stringResource(R.string.amount_format, stringResource(R.string.currency_symbol), account.balanceYuan),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (account.balanceYuan >= 0) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (!account.isDefault) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.account_set_as_default)) },
                    onClick = {
                        onSetDefault()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Star, contentDescription = null)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                onClick = {
                    onEdit()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                onClick = {
                    onDelete()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AccountType, initialBalanceCents: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var balance by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_account)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.account_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Account Type Selection
                ExposedDropdownMenuBox(
                    expanded = showTypeDropdown,
                    onExpandedChange = { showTypeDropdown = !showTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = "${selectedType.icon} ${selectedType.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.account_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        AccountType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text("${type.icon} ${type.displayName}") },
                                onClick = {
                                    selectedType = type
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Initial Balance
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(stringResource(R.string.initial_balance)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balanceCents = ((balance.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    onConfirm(name, selectedType, balanceCents)
                },
                enabled = name.isNotEmpty()
            ) {
                Text(stringResource(R.string.account_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountDialog(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var balance by remember { mutableStateOf(account.balanceYuan.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_account)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.account_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Account Type (Read-only)
                OutlinedTextField(
                    value = "${account.type.icon} ${account.type.displayName}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("账户类型") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Balance
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it.filter { char -> char.isDigit() || char == '.' || char == '-' } },
                    label = { Text(stringResource(R.string.account_current_balance)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balanceCents = ((balance.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val updatedAccount = account.copy(
                        name = name,
                        balanceCents = balanceCents,
                        updatedAt = Instant.DISTANT_FUTURE // Will be set by repository
                    )
                    onConfirm(updatedAccount)
                },
                enabled = name.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
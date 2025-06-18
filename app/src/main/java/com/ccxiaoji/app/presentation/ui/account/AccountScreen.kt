package com.ccxiaoji.app.presentation.ui.account

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
import com.ccxiaoji.app.R
import com.ccxiaoji.app.domain.model.Account
import com.ccxiaoji.app.domain.model.AccountType
import com.ccxiaoji.app.presentation.viewmodel.AccountViewModel
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
                        text = "账户管理",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.accounts.size >= 2) {
                        IconButton(onClick = { showTransferDialog = true }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "转账")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加账户")
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
            title = { Text("删除账户") },
            text = { 
                Text("确定要删除账户 \"${selectedAccount!!.name}\" 吗？删除后该账户的所有交易记录将被保留但无法恢复账户。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(selectedAccount!!.id)
                        showDeleteDialog = false
                        selectedAccount = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedAccount = null
                    }
                ) {
                    Text("取消")
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
                text = "总资产",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¥%.2f".format(totalBalance),
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
                                        text = "默认",
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
                    text = "¥%.2f".format(account.balanceYuan),
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
                    text = { Text("设为默认") },
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
                text = { Text("编辑") },
                onClick = {
                    onEdit()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("删除") },
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
    var selectedType by remember { mutableStateOf(AccountType.BANK_CARD) }
    var balance by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加账户") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账户名称") },
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
                        label = { Text("账户类型") },
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
                    label = { Text("初始余额") },
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
                Text("添加")
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
        title = { Text("编辑账户") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账户名称") },
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
                    label = { Text("当前余额") },
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
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
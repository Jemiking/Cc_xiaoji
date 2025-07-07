package com.ccxiaoji.feature.ledger.presentation.screen.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.presentation.screen.account.components.*
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AccountViewModel
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController? = null,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    
    
    // 按账户类型分组
    val groupedAccounts = remember(uiState.accounts) {
        uiState.accounts.groupBy { it.type }
    }
    
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (uiState.accounts.size >= 2) {
                        IconButton(onClick = { 
                            navController?.navigate(LedgerNavigation.TransferRoute)
                        }) {
                            Icon(
                                Icons.Default.SwapHoriz, 
                                contentDescription = stringResource(R.string.account_transfer)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController?.navigate(LedgerNavigation.addAccountRoute())
                },
                containerColor = DesignTokens.BrandColors.Ledger,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = stringResource(R.string.add_account),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (uiState.accounts.isEmpty()) {
            // 空状态视图
            EmptyAccountState(
                onAddAccount = { 
                    navController?.navigate(LedgerNavigation.addAccountRoute())
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    horizontal = DesignTokens.Spacing.medium,
                    vertical = DesignTokens.Spacing.small
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                // Total Balance Card
                item {
                    TotalBalanceCard(
                        totalBalance = uiState.totalBalance,
                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.medium)
                    )
                }
                
                // 按账户类型分组显示
                groupedAccounts.forEach { (type, accounts) ->
                    item {
                        AccountGroupHeader(
                            accountType = type,
                            count = accounts.size,
                            totalBalance = accounts.sumOf { it.balanceYuan }
                        )
                    }
                    
                    items(accounts, key = { it.id }) { account ->
                        AccountItem(
                            account = account,
                            onEdit = {
                                navController?.navigate(LedgerNavigation.editAccountRoute(account.id))
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
                                navController?.navigate("ledger?accountId=${account.id}")
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // For FAB
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedAccount != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                selectedAccount = null
            },
            title = { 
                Text(
                    text = stringResource(R.string.account_delete_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = { 
                Text(
                    text = stringResource(
                        R.string.account_delete_message, 
                        selectedAccount!!.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(selectedAccount!!.id)
                        showDeleteDialog = false
                        selectedAccount = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete), 
                        color = DesignTokens.BrandColors.Error
                    )
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
    
}

@Composable
fun AccountGroupHeader(
    accountType: AccountType,
    count: Int,
    totalBalance: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.Spacing.small,
                vertical = DesignTokens.Spacing.xs
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
        ) {
            Text(
                text = accountType.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "($count)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        
        Text(
            text = stringResource(
                R.string.amount_format,
                stringResource(R.string.currency_symbol),
                totalBalance
            ),
            style = MaterialTheme.typography.titleSmall,
            color = if (totalBalance >= 0) {
                MaterialTheme.colorScheme.onSurface
            } else {
                DesignTokens.BrandColors.Error
            }
        )
    }
}
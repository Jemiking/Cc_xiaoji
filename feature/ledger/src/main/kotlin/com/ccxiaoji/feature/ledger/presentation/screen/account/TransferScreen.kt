package com.ccxiaoji.feature.ledger.presentation.screen.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransferViewModel
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    
    // 系统返回
    BackHandler { onNavigateBack?.invoke() ?: navController.navigateUp() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.account_transfer),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.performTransfer()
                        },
                        enabled = uiState.canTransfer && !uiState.isLoading
                    ) {
                        Text(stringResource(R.string.transfer))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // From Account
                ExposedDropdownMenuBox(
                    expanded = showFromDropdown,
                    onExpandedChange = { showFromDropdown = !showFromDropdown }
                ) {
                    OutlinedTextField(
                        value = uiState.fromAccount?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.from_account)) },
                        placeholder = { Text(stringResource(R.string.select_account)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFromDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = uiState.fromAccountError != null,
                        supportingText = {
                            uiState.fromAccountError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = showFromDropdown,
                        onDismissRequest = { showFromDropdown = false }
                    ) {
                        uiState.accounts.forEach { account ->
                            if (account.id != uiState.toAccount?.id) {
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(account.name)
                                            Text(
                                                text = "¥${account.balanceYuan}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectFromAccount(account)
                                        showFromDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // To Account
                ExposedDropdownMenuBox(
                    expanded = showToDropdown,
                    onExpandedChange = { showToDropdown = !showToDropdown }
                ) {
                    OutlinedTextField(
                        value = uiState.toAccount?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.to_account)) },
                        placeholder = { Text(stringResource(R.string.select_account)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = uiState.toAccountError != null,
                        supportingText = {
                            uiState.toAccountError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = showToDropdown,
                        onDismissRequest = { showToDropdown = false }
                    ) {
                        uiState.accounts.forEach { account ->
                            if (account.id != uiState.fromAccount?.id) {
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(account.name)
                                            Text(
                                                text = "¥${account.balanceYuan}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectToAccount(account)
                                        showToDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Transfer Amount
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text(stringResource(R.string.transfer_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.amountError != null,
                    supportingText = {
                        uiState.amountError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                
                // Available balance hint
                uiState.fromAccount?.let { account ->
                    Text(
                        text = "${stringResource(R.string.available_balance)}：¥${account.balanceYuan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Transfer Note
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { viewModel.updateNote(it) },
                    label = { Text(stringResource(R.string.note_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // Transfer Result
                LaunchedEffect(uiState.transferSuccess) {
                    if (uiState.transferSuccess) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("transfer_completed", true)
                        navController.navigateUp()
                    }
                }
            }
        }
    }
}

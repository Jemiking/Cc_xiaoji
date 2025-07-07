package com.ccxiaoji.feature.ledger.presentation.screen.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.presentation.viewmodel.EditAccountViewModel
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    accountId: String,
    navController: NavController,
    viewModel: EditAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.edit_account),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveAccount()
                        },
                        enabled = uiState.name.isNotEmpty() && !uiState.isLoading
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
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
                // Account Name
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text(stringResource(R.string.account_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.nameError != null,
                    supportingText = {
                        uiState.nameError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                
                // Account Type (Read-only)
                OutlinedTextField(
                    value = uiState.accountTypeDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.account_type)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                
                // Balance
                OutlinedTextField(
                    value = uiState.balance,
                    onValueChange = { viewModel.updateBalance(it) },
                    label = { Text(stringResource(R.string.account_current_balance)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.balanceError != null,
                    supportingText = {
                        uiState.balanceError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                
                // Save Result
                LaunchedEffect(uiState.isSaved) {
                    if (uiState.isSaved) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("account_updated", true)
                        navController.navigateUp()
                    }
                }
            }
        }
    }
}
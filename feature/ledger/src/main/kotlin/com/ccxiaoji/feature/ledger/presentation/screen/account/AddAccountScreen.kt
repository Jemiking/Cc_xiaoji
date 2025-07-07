package com.ccxiaoji.feature.ledger.presentation.screen.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddAccountViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    navController: NavController,
    viewModel: AddAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.add_account),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                modifier = Modifier.fillMaxSize(),
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
                // Account Name Input
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text(stringResource(R.string.account_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } }
                )
                
                // Account Type Selection
                var showTypeDropdown by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = showTypeDropdown,
                    onExpandedChange = { showTypeDropdown = !showTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = "${uiState.selectedType.icon} ${uiState.selectedType.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.account_type)) },
                        trailingIcon = { 
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small)
                    )
                    ExposedDropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        AccountType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(
                                            DesignTokens.Spacing.small
                                        )
                                    ) {
                                        Text(type.icon)
                                        Text(type.displayName)
                                    }
                                },
                                onClick = {
                                    viewModel.selectType(type)
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Initial Balance Input
                OutlinedTextField(
                    value = uiState.balance,
                    onValueChange = viewModel::updateBalance,
                    label = { Text(stringResource(R.string.initial_balance)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                    isError = uiState.balanceError != null,
                    supportingText = uiState.balanceError?.let { { Text(it) } }
                )
                
                // Error message
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Create Account Button
                FlatButton(
                    text = stringResource(R.string.account_add),
                    onClick = { 
                        scope.launch {
                            viewModel.createAccount {
                                navController.navigateUp()
                            }
                        }
                    },
                    enabled = uiState.canCreate && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DesignTokens.BrandColors.Ledger
                )
            }
        }
    }
}
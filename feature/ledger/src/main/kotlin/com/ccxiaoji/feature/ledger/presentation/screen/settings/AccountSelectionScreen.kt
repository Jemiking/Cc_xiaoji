package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AccountSelectionViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 账户选择页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectionScreen(
    navController: NavController,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AccountSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 系统返回
    BackHandler { onNavigateBack?.invoke() ?: navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择默认账户") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = DesignTokens.Spacing.small)
            ) {
                items(uiState.accounts) { account ->
                    ListItem(
                        headlineContent = { 
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyLarge
                            ) 
                        },
                        supportingContent = { 
                            Text(
                                text = "余额：¥${account.balanceYuan}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            ) 
                        },
                        leadingContent = {
                            RadioButton(
                                selected = account.id == uiState.selectedAccountId?.toString(),
                                onClick = { 
                                    viewModel.selectAccount(account.id.toLong())
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("selected_account_id", account.id.toLong())
                                    onNavigateBack?.invoke() ?: navController.popBackStack()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        },
                        modifier = Modifier.clickable {
                            viewModel.selectAccount(account.id.toLong())
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_account_id", account.id.toLong())
                            onNavigateBack?.invoke() ?: navController.popBackStack()
                        }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = DesignTokens.Spacing.medium),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

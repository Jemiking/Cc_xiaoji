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
                    label = { 
                        Text(
                            if (uiState.selectedType == AccountType.CREDIT_CARD) "初始欠款" 
                            else stringResource(R.string.initial_balance)
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                    isError = uiState.balanceError != null,
                    supportingText = uiState.balanceError?.let { { Text(it) } }
                )
                
                // 信用卡专用字段 - 条件渲染
                if (uiState.selectedType == AccountType.CREDIT_CARD) {
                    // 信用额度输入
                    OutlinedTextField(
                        value = uiState.creditLimit,
                        onValueChange = viewModel::updateCreditLimit,
                        label = { Text("信用额度") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                        isError = uiState.creditLimitError != null,
                        supportingText = uiState.creditLimitError?.let { { Text(it) } },
                        prefix = { Text("¥") }
                    )
                    
                    // 账单日和还款日并排显示
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                    ) {
                        // 账单日输入
                        OutlinedTextField(
                            value = uiState.billingDay,
                            onValueChange = viewModel::updateBillingDay,
                            label = { Text("账单日") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                            isError = uiState.billingDayError != null,
                            supportingText = uiState.billingDayError?.let { { Text(it) } },
                            suffix = { Text("日") }
                        )
                        
                        // 还款日输入
                        OutlinedTextField(
                            value = uiState.paymentDueDay,
                            onValueChange = viewModel::updatePaymentDueDay,
                            label = { Text("还款日") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.BorderRadius.small),
                            isError = uiState.paymentDueDayError != null,
                            supportingText = uiState.paymentDueDayError?.let { { Text(it) } },
                            suffix = { Text("日") }
                        )
                    }
                    
                    // 信用卡说明文本
                    Text(
                        text = "• 初始欠款：信用卡当前已使用金额（负数表示欠款）\n• 账单日/还款日：每月几号（1-28日）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = DesignTokens.Spacing.small)
                    )
                }
                
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
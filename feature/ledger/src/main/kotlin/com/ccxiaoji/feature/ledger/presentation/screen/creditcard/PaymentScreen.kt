package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.PaymentViewModel
import com.ccxiaoji.feature.ledger.presentation.utils.CurrencyFormatter.formatCurrency
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    accountId: String,
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var paymentAmount by remember { mutableStateOf("") }
    
    // 初始化时设置全额还款金额
    LaunchedEffect(uiState.currentDebt) {
        if (paymentAmount.isEmpty() && uiState.currentDebt > 0) {
            paymentAmount = uiState.currentDebt.toString()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("信用卡还款") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 信用卡信息卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        uiState.cardName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Divider(modifier = Modifier.padding(vertical = DesignTokens.Spacing.small))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "当前欠款",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    formatCurrency(uiState.currentDebt),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "信用额度",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    formatCurrency(uiState.creditLimit),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "可用额度",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    formatCurrency(uiState.availableCredit),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    // 还款金额输入
                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { 
                            paymentAmount = it.filter { char -> char.isDigit() || char == '.' }
                        },
                        label = { Text("还款金额") },
                        placeholder = { Text("请输入还款金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        suffix = { Text("元") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = (paymentAmount.toDoubleOrNull() ?: 0.0) > uiState.currentDebt,
                        supportingText = {
                            if ((paymentAmount.toDoubleOrNull() ?: 0.0) > uiState.currentDebt) {
                                Text(
                                    "还款金额不能超过当前欠款",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    
                    // 快捷还款选项
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        FilterChip(
                            selected = paymentAmount == uiState.currentDebt.toString(),
                            onClick = { paymentAmount = uiState.currentDebt.toString() },
                            label = { Text("全额还款") },
                            leadingIcon = if (paymentAmount == uiState.currentDebt.toString()) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                        
                        FilterChip(
                            selected = paymentAmount == (uiState.currentDebt * 0.1).toString(),
                            onClick = { paymentAmount = String.format("%.2f", uiState.currentDebt * 0.1) },
                            label = { Text("最低还款(10%)") },
                            leadingIcon = if (paymentAmount == String.format("%.2f", uiState.currentDebt * 0.1)) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                    
                    // 提示信息
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium),
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                            ) {
                                Text(
                                    "还款提示",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "• 还款将记录为一笔收入交易",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    "• 还款后信用卡可用额度会相应增加",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    "• 建议在还款日前完成还款避免逾期",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // 确认还款按钮
                    Button(
                        onClick = {
                            val amount = paymentAmount.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && amount <= uiState.currentDebt) {
                                viewModel.makePayment(amount)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && 
                                 (paymentAmount.toDoubleOrNull() ?: 0.0) > 0 &&
                                 (paymentAmount.toDoubleOrNull() ?: 0.0) <= uiState.currentDebt
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                        Text("确认还款")
                    }
                }
            }
        }
    }
    
    // 处理还款成功
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("payment_made", true)
            navController.popBackStack()
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: 显示错误提示
        }
    }
}
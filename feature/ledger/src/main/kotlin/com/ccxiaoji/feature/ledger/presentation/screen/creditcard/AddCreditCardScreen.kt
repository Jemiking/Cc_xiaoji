package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddCreditCardViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 添加信用卡页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCardScreen(
    navController: NavController,
    viewModel: AddCreditCardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 处理保存成功
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("credit_card_added", true)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加信用卡") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveCreditCard() },
                        enabled = !uiState.isLoading && viewModel.isFormValid(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignTokens.BrandColors.Ledger
                        )
                    ) {
                        Text("添加")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 信用卡图标和说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Column {
                        Text(
                            "添加信用卡账户",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "管理信用卡消费、还款和账单",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 卡片名称
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("卡片名称") },
                placeholder = { Text("如：招行信用卡") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                enabled = !uiState.isLoading
            )
            
            // 信用额度
            OutlinedTextField(
                value = uiState.creditLimit,
                onValueChange = viewModel::updateCreditLimit,
                label = { Text("信用额度") },
                placeholder = { Text("10000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("元") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.creditLimitError != null,
                supportingText = uiState.creditLimitError?.let { { Text(it) } },
                enabled = !uiState.isLoading
            )
            
            // 当前已用额度
            OutlinedTextField(
                value = uiState.usedAmount,
                onValueChange = viewModel::updateUsedAmount,
                label = { Text("当前已用额度") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("元") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.usedAmountError != null,
                supportingText = {
                    val error = uiState.usedAmountError
                    if (error != null) {
                        Text(error)
                    } else {
                        Text("已消费但未还款的金额")
                    }
                },
                enabled = !uiState.isLoading
            )
            
            // 账单日和还款日
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.billingDay,
                    onValueChange = viewModel::updateBillingDay,
                    label = { Text("账单日") },
                    placeholder = { Text("1-28") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text("号") },
                    modifier = Modifier.weight(1f),
                    isError = uiState.billingDayError != null,
                    supportingText = uiState.billingDayError?.let { { Text(it) } },
                    enabled = !uiState.isLoading
                )
                
                OutlinedTextField(
                    value = uiState.paymentDueDay,
                    onValueChange = viewModel::updatePaymentDueDay,
                    label = { Text("还款日") },
                    placeholder = { Text("1-28") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text("号") },
                    modifier = Modifier.weight(1f),
                    isError = uiState.paymentDueDayError != null,
                    supportingText = uiState.paymentDueDayError?.let { { Text(it) } },
                    enabled = !uiState.isLoading
                )
            }
            
            // 提示信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "提示",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "• 账单日：每月生成账单的日期",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• 还款日：每月最后还款期限",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• 请填写1-28之间的数字，避免月末日期问题",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // 加载中指示器
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
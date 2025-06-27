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
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CreditCardSettingsViewModel

/**
 * 信用卡设置界面
 * 允许用户编辑信用卡的详细信息，包括额度、费用、利率等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardSettingsScreen(
    accountId: String,
    navController: NavController,
    viewModel: CreditCardSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    LaunchedEffect(accountId) {
        viewModel.loadCreditCard(accountId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("信用卡设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.hasChanges) {
                        TextButton(
                            onClick = { viewModel.saveChanges() },
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("保存")
                            }
                        }
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
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 基本信息
                BasicInfoSection(
                    cardName = uiState.cardName,
                    onCardNameChange = viewModel::updateCardName
                )
                
                Divider()
                
                // 额度设置
                CreditLimitSection(
                    creditLimit = uiState.creditLimit,
                    cashAdvanceLimit = uiState.cashAdvanceLimit,
                    onCreditLimitChange = viewModel::updateCreditLimit,
                    onCashAdvanceLimitChange = viewModel::updateCashAdvanceLimit
                )
                
                Divider()
                
                // 账单日期设置
                BillingDatesSection(
                    billingDay = uiState.billingDay,
                    paymentDueDay = uiState.paymentDueDay,
                    gracePeriodDays = uiState.gracePeriodDays,
                    onBillingDayChange = viewModel::updateBillingDay,
                    onPaymentDueDayChange = viewModel::updatePaymentDueDay,
                    onGracePeriodDaysChange = viewModel::updateGracePeriodDays
                )
                
                Divider()
                
                // 费用设置
                FeeSection(
                    annualFee = uiState.annualFee,
                    annualFeeWaiverThreshold = uiState.annualFeeWaiverThreshold,
                    interestRate = uiState.interestRate,
                    onAnnualFeeChange = viewModel::updateAnnualFee,
                    onWaiverThresholdChange = viewModel::updateAnnualFeeWaiverThreshold,
                    onInterestRateChange = viewModel::updateInterestRate
                )
                
                Divider()
                
                // 提醒设置
                ReminderSection(
                    isReminderEnabled = uiState.isReminderEnabled,
                    onReminderToggle = viewModel::toggleReminder
                )
            }
        }
    }
    
    // 保存成功提示
    uiState.saveSuccessMessage?.let { message ->
        LaunchedEffect(message) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            navController.navigateUp()
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("错误") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun BasicInfoSection(
    cardName: String,
    onCardNameChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "基本信息",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            value = cardName,
            onValueChange = onCardNameChange,
            label = { Text("卡片名称") },
            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun CreditLimitSection(
    creditLimit: String,
    cashAdvanceLimit: String,
    onCreditLimitChange: (String) -> Unit,
    onCashAdvanceLimitChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "额度设置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            value = creditLimit,
            onValueChange = onCreditLimitChange,
            label = { Text("信用额度") },
            prefix = { Text("¥") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = cashAdvanceLimit,
            onValueChange = onCashAdvanceLimitChange,
            label = { Text("取现额度") },
            prefix = { Text("¥") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("通常为信用额度的50%") }
        )
    }
}

@Composable
private fun BillingDatesSection(
    billingDay: String,
    paymentDueDay: String,
    gracePeriodDays: String,
    onBillingDayChange: (String) -> Unit,
    onPaymentDueDayChange: (String) -> Unit,
    onGracePeriodDaysChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "账单日期",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = billingDay,
                onValueChange = onBillingDayChange,
                label = { Text("账单日") },
                suffix = { Text("号") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            OutlinedTextField(
                value = paymentDueDay,
                onValueChange = onPaymentDueDayChange,
                label = { Text("还款日") },
                suffix = { Text("号") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        OutlinedTextField(
            value = gracePeriodDays,
            onValueChange = onGracePeriodDaysChange,
            label = { Text("免息期") },
            suffix = { Text("天") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("账单日到还款日的天数，通常为20-56天") }
        )
    }
}

@Composable
private fun FeeSection(
    annualFee: String,
    annualFeeWaiverThreshold: String,
    interestRate: String,
    onAnnualFeeChange: (String) -> Unit,
    onWaiverThresholdChange: (String) -> Unit,
    onInterestRateChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "费用设置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            value = annualFee,
            onValueChange = onAnnualFeeChange,
            label = { Text("年费") },
            prefix = { Text("¥") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = annualFeeWaiverThreshold,
            onValueChange = onWaiverThresholdChange,
            label = { Text("免年费门槛") },
            prefix = { Text("¥") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("年消费达到此金额可免年费") }
        )
        
        OutlinedTextField(
            value = interestRate,
            onValueChange = onInterestRateChange,
            label = { Text("日利率") },
            suffix = { Text("%") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("通常为0.05%（万分之五）") }
        )
    }
}

@Composable
private fun ReminderSection(
    isReminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "提醒设置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isReminderEnabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "还款提醒",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "在还款日前3天、1天和当天发送提醒",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = onReminderToggle
                )
            }
        }
    }
}
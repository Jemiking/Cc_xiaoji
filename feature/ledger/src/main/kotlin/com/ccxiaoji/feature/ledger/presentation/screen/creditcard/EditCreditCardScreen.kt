package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.EditCreditCardViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 编辑信用卡页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCreditCardScreen(
    accountId: String,
    navController: NavController,
    viewModel: EditCreditCardViewModel = hiltViewModel()
) {
    val TAG = "EditCreditCardScreen"
    val uiState by viewModel.uiState.collectAsState()
    var creditLimit by remember { mutableStateOf("") }
    var usedAmount by remember { mutableStateOf("") }
    var billingDay by remember { mutableStateOf("") }
    var paymentDueDay by remember { mutableStateOf("") }
    
    // 调试初始化信息
    LaunchedEffect(accountId) {
        Log.d(TAG, "EditCreditCardScreen初始化，信用卡ID: $accountId")
        Log.d(TAG, "ViewModel将自动加载信用卡数据")
    }
    
    // 调试状态变化
    LaunchedEffect(uiState) {
        Log.d(TAG, "UIState更新: isLoading=${uiState.isLoading}, creditCard=${uiState.creditCard?.name}")
        if (uiState.errorMessage != null) {
            Log.e(TAG, "EditCreditCard错误: ${uiState.errorMessage}")
        }
    }
    
    // 初始化数据
    LaunchedEffect(uiState.creditCard) {
        uiState.creditCard?.let { card ->
            Log.d(TAG, "初始化信用卡数据: ${card.name}")
            creditLimit = (card.creditLimitYuan ?: 0.0).toString()
            usedAmount = (-card.balanceYuan).toString()
            billingDay = (card.billingDay ?: 1).toString()
            paymentDueDay = (card.paymentDueDay ?: 20).toString()
            Log.d(TAG, "数据初始化完成 - 额度: $creditLimit, 已用: $usedAmount")
        }
    }
    
    // 处理保存成功
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("credit_card_updated", true)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑信用卡信息") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val limitYuan = creditLimit.toDoubleOrNull() ?: 0.0
                            val usedYuan = usedAmount.toDoubleOrNull() ?: 0.0
                            val billing = billingDay.toIntOrNull() ?: 1
                            val payment = paymentDueDay.toIntOrNull() ?: 20
                            
                            if (limitYuan > 0 && billing in 1..28 && payment in 1..28) {
                                viewModel.updateCreditCard(limitYuan, usedYuan, billing, payment)
                            }
                        },
                        enabled = !uiState.isLoading &&
                                 (creditLimit.toDoubleOrNull() ?: 0.0) > 0 &&
                                 (billingDay.toIntOrNull() ?: 0) in 1..28 &&
                                 (paymentDueDay.toIntOrNull() ?: 0) in 1..28
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("保存")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.creditCard == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!uiState.isLoading && uiState.creditCard == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("信用卡不存在", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 提示信息
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                "信用卡名称：${uiState.creditCard?.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "信用卡名称创建后不可修改",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // 信用额度
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("信用额度") },
                    placeholder = { Text("10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    suffix = { Text("元") },
                    supportingText = { Text("信用卡的总额度") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 当前已用额度
                OutlinedTextField(
                    value = usedAmount,
                    onValueChange = { usedAmount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("当前已用额度") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    suffix = { Text("元") },
                    supportingText = { Text("当前已使用的额度") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 验证已用额度
                if ((usedAmount.toDoubleOrNull() ?: 0.0) > (creditLimit.toDoubleOrNull() ?: 0.0)) {
                    Text(
                        "已用额度不能大于信用额度",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // 账单日和还款日
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = billingDay,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.isEmpty() || (filtered.toIntOrNull() ?: 0) in 1..28) {
                                billingDay = filtered
                            }
                        },
                        label = { Text("账单日") },
                        placeholder = { Text("1-28") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        suffix = { Text("号") },
                        supportingText = { Text("每月生成账单的日期") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = paymentDueDay,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.isEmpty() || (filtered.toIntOrNull() ?: 0) in 1..28) {
                                paymentDueDay = filtered
                            }
                        },
                        label = { Text("还款日") },
                        placeholder = { Text("1-28") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        suffix = { Text("号") },
                        supportingText = { Text("最后还款期限") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 日期说明
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "日期设置说明",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "• 账单日和还款日请填写1-28之间的数字",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• 避免选择29、30、31号，以免在某些月份出现问题",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• 还款日通常在账单日后20天左右",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // 错误信息
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
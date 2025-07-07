package com.ccxiaoji.feature.ledger.presentation.screen.savings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.ContributionViewModel
import com.ccxiaoji.ui.components.FlatSelectChip
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionScreen(
    navController: NavController,
    goalId: String,
    viewModel: ContributionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("contribution_added", true)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isDeposit) "存入金额" else "取出金额") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                        .padding(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 储蓄目标信息卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium)
                        ) {
                            Text(
                                text = "储蓄目标",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.goalName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { uiState.goalProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "¥${uiState.currentAmount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "¥${uiState.targetAmount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // 存入/取出切换
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FlatSelectChip(
                            selected = uiState.isDeposit,
                            onSelectedChange = { viewModel.updateDepositMode(true) },
                            label = "存入",
                            selectedIcon = Icons.Default.Add,
                            modifier = Modifier.padding(end = DesignTokens.Spacing.small)
                        )
                        
                        FlatSelectChip(
                            selected = !uiState.isDeposit,
                            onSelectedChange = { viewModel.updateDepositMode(false) },
                            label = "取出",
                            selectedIcon = Icons.Default.Remove
                        )
                    }
                    
                    // 金额输入
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = viewModel::updateAmount,
                        label = { Text("金额") },
                        prefix = { Text("¥") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = uiState.amountError != null,
                        supportingText = {
                            uiState.amountError?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    // 快捷金额按钮
                    if (uiState.isDeposit) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("100", "500", "1000", "5000").forEach { amount ->
                                AssistChip(
                                    onClick = { viewModel.updateAmount(amount) },
                                    label = { Text("¥$amount") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // 备注输入
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = viewModel::updateNote,
                        label = { Text("备注 (可选)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // 确认按钮
                    Button(
                        onClick = viewModel::saveContribution,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (uiState.isDeposit) "确认存入" else "确认取出")
                    }
                }
            }
            
            // 错误提示
            uiState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("关闭")
                        }
                    },
                    content = { Text(error) }
                )
            }
        }
    }
}
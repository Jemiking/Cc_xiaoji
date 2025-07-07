package com.ccxiaoji.feature.ledger.presentation.screen.transaction.delete

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.feature.ledger.presentation.viewmodel.DeleteTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTransactionScreen(
    transactionId: String,
    navController: NavController,
    viewModel: DeleteTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("确认删除") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 警告图标
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = DesignTokens.BrandColors.Error
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            
            // 标题
            Text(
                text = "确认删除交易",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 交易信息
            uiState.transaction?.let { transaction ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.Spacing.medium)
                    ) {
                        Text(
                            text = transaction.categoryDetails?.name ?: "其他",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                        Text(
                            text = "${if (transaction.categoryDetails?.type == "INCOME") "+" else "-"}¥${transaction.amountYuan}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (transaction.categoryDetails?.type == "INCOME") 
                                DesignTokens.BrandColors.Success 
                            else 
                                DesignTokens.BrandColors.Error
                        )
                        if (!transaction.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                            Text(
                                text = transaction.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            
            // 警告信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "此操作无法撤销，删除后交易记录将永久丢失。",
                    modifier = Modifier.padding(DesignTokens.Spacing.medium),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                FlatButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text("取消")
                }
                
                FlatButton(
                    onClick = { 
                        viewModel.deleteTransaction()
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("transaction_deleted", true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = DesignTokens.BrandColors.Error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text("删除")
                }
            }
        }
    }
}
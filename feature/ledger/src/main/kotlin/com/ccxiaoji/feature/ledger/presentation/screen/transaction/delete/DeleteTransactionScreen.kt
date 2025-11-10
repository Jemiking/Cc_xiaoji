package com.ccxiaoji.feature.ledger.presentation.screen.transaction.delete

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.presentation.component.DeleteConfirmDialog
import com.ccxiaoji.feature.ledger.presentation.viewmodel.DeleteTransactionViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 删除交易确认页面
 *
 * 使用Dialog形式替代全屏页面，减少页面数量，提升用户体验。
 * 保持原有的ViewModel和导航逻辑不变。
 */
@Composable
fun DeleteTransactionScreen(
    transactionId: String,
    navController: NavController,
    viewModel: DeleteTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(true) }

    // 加载交易数据
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    // 使用通用删除确认弹窗
    DeleteConfirmDialog(
        visible = showDialog,
        itemCount = 1,
        title = "确认删除交易",
        message = "此操作无法撤销，删除后交易记录将永久丢失。",
        isDeleting = uiState.isDeleting,
        onDismiss = {
            showDialog = false
            navController.popBackStack()
        },
        onConfirm = {
            viewModel.deleteTransaction()
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("transaction_deleted", true)
            showDialog = false
            navController.popBackStack()
        }
    ) {
        // 交易详情预览
        uiState.transaction?.let { transaction ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(DesignTokens.Spacing.medium)) {
                    // 分类名称
                    Text(
                        text = transaction.categoryDetails?.name ?: "其他",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))

                    // 金额显示
                    val isIncome = transaction.categoryDetails?.type == "INCOME"
                    Text(
                        text = "${if (isIncome) "+" else "-"}¥${transaction.amountYuan}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isIncome)
                            DesignTokens.BrandColors.Success
                        else
                            DesignTokens.BrandColors.Error
                    )

                    // 备注（如果有）
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
    }
}
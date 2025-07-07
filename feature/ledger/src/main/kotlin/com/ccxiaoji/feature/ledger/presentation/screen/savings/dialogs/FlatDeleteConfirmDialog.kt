package com.ccxiaoji.feature.ledger.presentation.screen.savings.dialogs

import androidx.compose.runtime.Composable
import com.ccxiaoji.ui.components.FlatAlertDialog


/**
 * 扁平化删除贡献记录对话框
 */
@Composable
fun FlatDeleteContributionDialog(
    isDeposit: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    FlatAlertDialog(
        dialogTitle = "删除记录",
        dialogText = "确定要删除这条${if (isDeposit) "存入" else "取出"}记录吗？",
        confirmText = "删除",
        dismissText = "取消",
        onConfirmation = onConfirm,
        onDismissRequest = onDismiss
    )
}
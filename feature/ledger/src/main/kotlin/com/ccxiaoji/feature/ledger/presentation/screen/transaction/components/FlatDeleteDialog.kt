package com.ccxiaoji.feature.ledger.presentation.screen.transaction.components

import androidx.compose.runtime.Composable
import com.ccxiaoji.ui.components.FlatAlertDialog

@Composable
fun FlatDeleteDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    FlatAlertDialog(
        onDismissRequest = onDismiss,
        onConfirmation = onConfirm,
        dialogTitle = title,
        dialogText = message,
        confirmText = confirmText,
        dismissText = cancelText
    )
}
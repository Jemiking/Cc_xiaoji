package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category

/**
 * 批量操作结果提示
 */
@Composable
fun BatchOperationResultSnackbar(
    message: String,
    isSuccess: Boolean = true,
    onDismiss: () -> Unit = {},
    onUndo: (() -> Unit)? = null
) {
    Snackbar(
        action = if (onUndo != null) {
            {
                TextButton(onClick = onUndo) {
                    Text(stringResource(R.string.undo))
                }
            }
        } else null,
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        containerColor = if (isSuccess) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    ) {
        Text(message)
    }
}
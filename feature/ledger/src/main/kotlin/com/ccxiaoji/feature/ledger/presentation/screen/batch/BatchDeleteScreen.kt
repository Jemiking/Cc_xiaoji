package com.ccxiaoji.feature.ledger.presentation.screen.batch

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.presentation.component.DeleteConfirmDialog
import com.ccxiaoji.feature.ledger.presentation.viewmodel.BatchDeleteViewModel

/**
 * 批量删除确认页面
 *
 * 使用Dialog形式替代全屏页面，支持批量删除确认。
 * 根据选中数量自动调整显示文案。
 */
@Composable
fun BatchDeleteScreen(
    navController: NavController,
    viewModel: BatchDeleteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(true) }

    // 使用通用删除确认弹窗
    DeleteConfirmDialog(
        visible = showDialog,
        itemCount = uiState.selectedCount,
        title = stringResource(R.string.batch_delete_confirm_title),
        message = stringResource(
            R.string.batch_delete_confirm_message,
            uiState.selectedCount
        ),
        isDeleting = uiState.isDeleting,
        onDismiss = {
            showDialog = false
            navController.popBackStack()
        },
        onConfirm = {
            // 设置返回结果并返回
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "batch_delete_confirmed",
                true
            )
            showDialog = false
            navController.popBackStack()
        }
    ) {
        // 批量删除警告信息
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.batch_delete_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
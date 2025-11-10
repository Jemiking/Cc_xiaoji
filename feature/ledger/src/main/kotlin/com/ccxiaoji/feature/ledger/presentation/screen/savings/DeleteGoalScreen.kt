package com.ccxiaoji.feature.ledger.presentation.screen.savings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.ccxiaoji.feature.ledger.presentation.viewmodel.DeleteGoalViewModel

/**
 * 删除储蓄目标确认页面
 *
 * 使用Dialog形式替代全屏页面，保持原有的ViewModel和导航逻辑。
 * 支持显示储蓄目标的贡献记录警告。
 */
@Composable
fun DeleteGoalScreen(
    navController: NavController,
    viewModel: DeleteGoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(true) }

    // 使用通用删除确认弹窗
    DeleteConfirmDialog(
        visible = showDialog,
        itemCount = 1,
        title = stringResource(R.string.delete_savings_goal),
        message = stringResource(
            R.string.delete_goal_confirm_message,
            uiState.goalName
        ),
        isDeleting = uiState.isDeleting,
        onDismiss = {
            showDialog = false
            navController.popBackStack()
        },
        onConfirm = {
            // 设置返回结果并返回
            navController.previousBackStackEntry?.savedStateHandle?.set(
                "delete_goal_confirmed",
                true
            )
            showDialog = false
            navController.popBackStack()
        }
    ) {
        // 警告信息卡片
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.delete_goal_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        // 如果有贡献记录，显示额外提示
        if (uiState.hasContributions) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.delete_goal_contributions_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
package com.ccxiaoji.feature.schedule.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.uikit.ActionConfig
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleBottomActions
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleCard
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSimpleScaffold
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 清除数据确认页面 - 使用 Schedule UI Kit 组件
 */
@Composable
fun ClearDataScreen(
    navController: NavController
) {
    ScheduleSimpleScaffold(
        title = stringResource(R.string.schedule_settings_confirm_clear_title),
        onNavigateBack = { navController.popBackStack() }
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
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))

            // 标题
            Text(
                text = stringResource(R.string.schedule_settings_confirm_clear_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))

            // 警告消息卡片
            ScheduleCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = stringResource(R.string.schedule_settings_confirm_clear_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部操作栏 - 危险操作使用错误色
            ScheduleBottomActions(
                primaryAction = ActionConfig(
                    text = stringResource(R.string.schedule_confirm),
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("data_cleared", true)
                        navController.popBackStack()
                    }
                ),
                secondaryAction = ActionConfig(
                    text = stringResource(R.string.schedule_cancel),
                    onClick = { navController.popBackStack() }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

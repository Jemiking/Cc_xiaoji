package com.ccxiaoji.feature.schedule.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleBottomActions
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleCard
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSelectableCard
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSimpleScaffold
import com.ccxiaoji.feature.schedule.presentation.uikit.confirmCancelActions
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 备份位置选择页面 - 使用 Schedule UI Kit 组件
 */
@Composable
fun BackupLocationScreen(
    navController: NavController
) {
    var selectedOption by remember { mutableStateOf("") }

    ScheduleSimpleScaffold(
        title = stringResource(R.string.schedule_settings_backup_location_dialog_title),
        onNavigateBack = { navController.popBackStack() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 说明文字
            ScheduleCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.schedule_settings_backup_location_dialog_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))

            // 外部备份选项
            ScheduleSelectableCard(
                selected = selectedOption == "external",
                onClick = { selectedOption = "external" },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.RadioButton
                        selected = selectedOption == "external"
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (selectedOption == "external")
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.schedule_settings_backup_external),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedOption == "external")
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "选择保存位置，可随时找回",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedOption == "external")
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    RadioButton(
                        selected = selectedOption == "external",
                        onClick = { selectedOption = "external" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))

            // 内部备份选项
            ScheduleSelectableCard(
                selected = selectedOption == "internal",
                onClick = { selectedOption = "internal" },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.RadioButton
                        selected = selectedOption == "internal"
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (selectedOption == "internal")
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.schedule_settings_backup_internal),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedOption == "internal")
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "保存到应用内部存储",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedOption == "internal")
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    RadioButton(
                        selected = selectedOption == "internal",
                        onClick = { selectedOption = "internal" }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部操作栏
            val actions = confirmCancelActions(
                onConfirm = {
                    when (selectedOption) {
                        "external" -> {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("backup_location", "external")
                        }
                        "internal" -> {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("backup_location", "internal")
                        }
                    }
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                confirmEnabled = selectedOption.isNotEmpty()
            )
            ScheduleBottomActions(
                primaryAction = actions.first,
                secondaryAction = actions.second,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
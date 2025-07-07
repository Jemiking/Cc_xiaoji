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
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 清除数据确认页面 - 替代原ClearDataDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearDataScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(stringResource(R.string.schedule_settings_confirm_clear_title))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.schedule_settings_confirm_clear_message),
                    modifier = Modifier.padding(DesignTokens.Spacing.medium),
                    style = MaterialTheme.typography.bodyLarge,
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
                    onClick = { 
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(stringResource(R.string.schedule_cancel))
                }
                
                FlatButton(
                    onClick = { 
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("data_cleared", true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(stringResource(R.string.schedule_confirm))
                }
            }
        }
    }
}
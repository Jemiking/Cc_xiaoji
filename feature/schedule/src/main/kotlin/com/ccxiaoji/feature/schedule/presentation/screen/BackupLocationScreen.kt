package com.ccxiaoji.feature.schedule.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 备份位置选择页面 - 替代原BackupLocationDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupLocationScreen(
    navController: NavController
) {
    var selectedOption by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(stringResource(R.string.schedule_settings_backup_location_dialog_title))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 说明文字
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                ) {
                    Text(
                        text = stringResource(R.string.schedule_settings_backup_location_dialog_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            
            // 外部备份选项
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedOption == "external",
                        onClick = { selectedOption = "external" },
                        role = Role.RadioButton
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedOption == "external") 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
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
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedOption == "internal",
                        onClick = { selectedOption = "internal" },
                        role = Role.RadioButton
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedOption == "internal") 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
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
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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
                    modifier = Modifier.weight(1f),
                    enabled = selectedOption.isNotEmpty()
                ) {
                    Text(stringResource(R.string.schedule_confirm))
                }
            }
        }
    }
}